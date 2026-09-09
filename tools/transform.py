#!/usr/bin/env python3
"""Turn the vendored Android sources in `upstream/` into Compose Multiplatform source sets.

Three passes, in this order:

  1. copy      `upstream/<artifact>/**.kt` -> `modules/<module>/src/commonMain/kotlin/**.kt`,
               minus the files `transform-rules.json` excludes;
  2. rewrite   the mechanical rules from `transform-rules.json` — drop Android build annotations,
               move imports Compose Multiplatform republishes elsewhere;
  3. patch     `patches/<module>/<same path as the source>.patch` — at most one per file, cut by
               `tools/make_patch.py` — rewiring the generated code onto the `expect` declarations
               in `src/commonPort/kotlin`. That directory and the `actual`s under
               `src/wasmJsMain` / `src/jvmMain` are ordinary hand-written source, never generated.

Then it writes `docs/ANDROID_SURFACE.md`: what is still excluded, and every file that still
imports something Android-only. That report is the remaining work, measured rather than guessed.

`modules/*/src/commonMain` is generated output and is committed anyway — the wasm build must not
need Python, and the point of committing it is that the diff at each AndroidX release is
reviewable. `scripts/check-generated.sh` re-runs this and fails if the tree moves.
"""

from __future__ import annotations

import argparse
import json
import pathlib
import re
import shutil
import subprocess
import xml.etree.ElementTree
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

BANNER = (
    "// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.\n"
    "// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.\n"
)

# Deliberately version-free. The pinned version lives in upstream.json and upstream/MANIFEST.json;
# stamping it into 222 file headers would rewrite every generated file on every release bump and
# bury the real diff.


def strip_annotation(text: str, name: str) -> str:
    """Remove `@Name`, with or without a balanced single-level argument list.

    Line-oriented on purpose: an annotation alone on its line takes the line with it, and one
    sitting inline before a declaration is cut out where it stands.
    """
    # `@file:`/`@get:`/... use-site targets included: `@file:SuppressLint(...)` is how AndroidX
    # silences a lint check for a whole file, and it is the form that appears most often.
    pattern = re.compile(
        r"@(?:file:|get:|set:|param:|property:|setparam:|delegate:|receiver:)?"
        + name
        + r"(\((?:[^()]|\([^()]*\))*\))?"
    )
    out = []
    for line in text.splitlines(keepends=True):
        if not pattern.search(line):
            out.append(line)
            continue
        stripped = pattern.sub("", line)
        if stripped.strip() == "":
            continue  # the annotation was the whole line
        out.append(stripped.lstrip() if stripped.strip() and not stripped[0].isspace() else stripped)
    return "".join(out)


def ensure_imports(text: str, rules: dict) -> str:
    """Add an import the sources never needed on Android.

    `kotlin.jvm.JvmInline` and friends are default-imported by the Kotlin/JVM compiler and are
    written without an import all over AndroidX. In common code they are ordinary declarations that
    have to be imported like any other, so the transform adds what the platform used to supply.
    """
    for token, fqn in rules["ensureImports"].items():
        if f"import {fqn}" in text or not re.search(token, text):
            continue
        line = f"import {fqn}"
        imports = list(re.finditer(r"^import .*$", text, flags=re.M))
        if not imports:
            # A file with no imports at all still has a package declaration to hang one off.
            package = re.search(r"^package .*$", text, flags=re.M)
            if not package:
                continue
            text = text[: package.end()] + f"\n\n{line}" + text[package.end() :]
            continue
        after = None
        for match in imports:
            if match.group(0) < line:
                after = match
        # Kotlin style keeps the import block sorted; inserting in place keeps the generated file
        # byte-identical to what a formatter would produce, so nothing downstream reformats it.
        insert_at = after.end() if after else imports[0].start() - 1
        text = text[:insert_at] + "\n" + line + text[insert_at:]
    return text


def rewrite(text: str, rules: dict) -> str:
    for fqn in rules["dropImports"]:
        text = re.sub(rf"^import {re.escape(fqn)}\n", "", text, flags=re.M)
    for name in rules["dropAnnotations"]:
        text = strip_annotation(text, name)
    for rule in rules["rewritePatterns"]:
        # A regex layer, for the shapes a literal rename cannot reach. Used sparingly: a pattern
        # that stops matching fails the build at the next sync rather than silently doing nothing,
        # which is the same failure mode as a patch and the reason to prefer a literal rule when
        # one will do.
        text = re.sub(rule["pattern"], rule["replacement"], text, flags=re.S)
    for old, new in rules["rewriteReferences"].items():
        # Textual, not import-only: AndroidX writes `java.util.concurrent.atomic.AtomicReference`
        # inline in at least one file, and an import-only rule would silently miss it. The word
        # boundary keeps `android.util.Log` from matching `android.util.LogPrinter`.
        # `\b` only where the pattern actually ends in a word character: a key like
        # `javaClass.hashCode()` ends in `)`, and a trailing `\b` there would never match.
        left = r"\b" if old[0].isalnum() or old[0] == "_" else ""
        right = r"\b" if old[-1].isalnum() or old[-1] == "_" else ""
        text = re.sub(left + re.escape(old) + right, new.replace("\\", "\\\\"), text)
    return ensure_imports(text, rules)


def android_imports(text: str, prefixes: list[str]) -> list[str]:
    found = []
    for match in re.finditer(r"^import (?:[\w.]+\.)?([\w.]+)$", text, flags=re.M):
        fqn = match.group(0)[len("import ") :]
        if any(fqn.startswith(prefix) for prefix in prefixes):
            found.append(fqn)
    return sorted(set(found))


def apply_patches(module_dir: pathlib.Path, patch_dir: pathlib.Path) -> list[str]:
    if not patch_dir.is_dir():
        return []
    applied = []
    for patch in sorted(patch_dir.rglob("*.patch")):
        result = subprocess.run(
            ["patch", "-p1", "--forward", "--no-backup-if-mismatch", "-i", str(patch.resolve())],
            cwd=module_dir,
            capture_output=True,
            text=True,
        )
        if result.returncode != 0:
            # A patch that no longer applies is the designed failure mode of a version bump: the
            # upstream file moved under it and a human has to decide what the port now means.
            sys.stderr.write(result.stdout + result.stderr)
            raise SystemExit(
                f"\npatch failed: {patch.relative_to(ROOT)}\n"
                f"  Upstream moved under it. Re-cut the patch against the regenerated file:\n"
                f"  see docs/PIPELINE.md -> 'When a patch stops applying'."
            )
        applied.append(patch.name)
    return applied


def generate_resources(source_root: pathlib.Path, common: pathlib.Path, package: str) -> int:
    """Turn the AAR's `res/values/values.xml` into a Kotlin lookup table.

    Android resolves `R.string.x` through the resource table an APK is built with. Off-Android
    there is no such table, so the strings become what they always were underneath: a map from
    resource name to text, generated so that an upstream wording change arrives with the next sync
    instead of being re-typed here.
    """
    resources = source_root / "resources.xml"
    if not resources.is_file():
        return 0

    root = xml.etree.ElementTree.fromstring(resources.read_text())
    strings = {node.get("name"): "".join(node.itertext()) for node in root.findall("string")}
    plurals = {
        node.get("name"): {item.get("quantity"): "".join(item.itertext()) for item in node}
        for node in root.findall("plurals")
    }

    def literal(value: str) -> str:
        escaped = value.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")
        # aapt escapes an apostrophe for the XML parser; the text itself does not carry the slash.
        return '"' + escaped.replace("\\'", "'") + '"'

    lines = [
        BANNER.rstrip("\n"),
        "// Source: the `res/values/values.xml` of the AAR pinned in upstream.json.",
        "",
        f"package {package}.internal",
        "",
        "/** Every `<string>` in the upstream resource table, by resource name. */",
        "internal val GeneratedStrings: Map<String, String> =",
        "    mapOf(",
    ]
    for name, value in sorted(strings.items()):
        lines.append(f"        \"{name}\" to {literal(value)},")
    lines += [
        "    )",
        "",
        "/** Every `<plurals>`, by resource name and then by quantity keyword. */",
        "internal val GeneratedPlurals: Map<String, Map<String, String>> =",
        "    mapOf(",
    ]
    for name, quantities in sorted(plurals.items()):
        lines.append(f"        \"{name}\" to")
        lines.append("            mapOf(")
        for quantity, value in sorted(quantities.items()):
            lines.append(f"                \"{quantity}\" to {literal(value)},")
        lines.append("            ),")
    lines += ["    )", ""]

    target = common / package.replace(".", "/") / "internal" / "GeneratedResources.kt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(lines))
    return len(strings) + len(plurals)


def transform_module(config: dict, rules: dict, entry: dict) -> dict:
    artifact, module = entry["artifact"], entry["module"]
    source_root = ROOT / "upstream" / artifact
    module_dir = ROOT / "modules" / module
    common = module_dir / "src" / "commonMain" / "kotlin"

    if common.exists():
        shutil.rmtree(common)

    excluded = rules["modules"].get(module, {}).get("exclude", {})
    copied, skipped, offenders = 0, [], {}

    for source in sorted(source_root.rglob("*.kt")):
        rel = source.relative_to(source_root).as_posix()
        if rel in excluded:
            skipped.append(rel)
            continue
        text = BANNER + rewrite(source.read_text(), rules)
        target = common / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(text)
        copied += 1

    resources = generate_resources(source_root, common, entry["package"])
    applied = apply_patches(module_dir, ROOT / "patches" / module)

    for generated in sorted(common.rglob("*.kt")):
        found = android_imports(generated.read_text(), rules["androidOnlyPrefixes"])
        if found:
            offenders[generated.relative_to(common).as_posix()] = found

    print(
        f"  {module}: {copied} files, {len(skipped)} excluded, "
        f"{len(applied)} patches, {len(offenders)} still Android-bound"
        + (f", {resources} resources" if resources else "")
    )
    return {
        "module": module,
        "artifact": artifact,
        "copied": copied,
        "excluded": {rel: excluded[rel] for rel in skipped},
        "patches": applied,
        "offenders": offenders,
    }


def write_report(config: dict, results: list[dict]) -> None:
    lines = [
        "# Android surface",
        "",
        "Generated by `tools/transform.py`. Do not edit — it is a measurement, not a plan.",
        "",
        f"Upstream: `{config['group']}` **{config['version']}**.",
        "",
        "| Module | In common | Excluded | Patches | Still Android-bound |",
        "| --- | ---: | ---: | ---: | ---: |",
    ]
    for result in results:
        lines.append(
            f"| `{result['module']}` | {result['copied']} | {len(result['excluded'])} "
            f"| {len(result['patches'])} | {len(result['offenders'])} |"
        )
    for result in results:
        lines += ["", f"## `{result['module']}`", ""]
        if result["excluded"]:
            lines += ["### Excluded from `commonMain`", ""]
            for rel, reason in sorted(result["excluded"].items()):
                lines.append(f"- `{rel}` — {reason}")
            lines.append("")
        if result["offenders"]:
            lines += [
                "### Still importing an Android-only package",
                "",
                "Each of these needs a patch in "
                f"[`patches/{result['module']}/`](../patches/{result['module']}) introducing an "
                "`expect` declaration, and an `actual` under `src/wasmJsMain`.",
                "",
            ]
            for rel, imports in sorted(result["offenders"].items()):
                lines.append(f"- `{rel}` — {', '.join(f'`{i}`' for i in imports)}")
            lines.append("")
        if not result["excluded"] and not result["offenders"]:
            lines += ["Fully ported: nothing excluded, nothing Android-bound.", ""]
    (ROOT / "docs" / "ANDROID_SURFACE.md").write_text("\n".join(lines).rstrip() + "\n")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--module", action="append", help="only this module (repeatable)")
    args = parser.parse_args()

    config = json.loads((ROOT / "upstream.json").read_text())
    rules = json.loads((ROOT / "transform-rules.json").read_text())

    entries = config["artifacts"]
    if args.module:
        entries = [entry for entry in entries if entry["module"] in args.module]

    print(f"transforming {config['group']} {config['version']}")
    results = [transform_module(config, rules, entry) for entry in entries]
    if not args.module:
        write_report(config, results)
    return 0


if __name__ == "__main__":
    sys.exit(main())
