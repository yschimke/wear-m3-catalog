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
                f"  Upstream moved under it. The rejected hunks are in the matching .rej file\n"
                f"  under {module_dir.relative_to(ROOT)}; re-cut the patch against the\n"
                f"  regenerated source. See docs/PIPELINE.md -> 'When a patch stops applying'."
            )
        applied.append(patch.name)
    return applied


def android_text(value: str) -> str:
    """Undo the encoding an Android string resource carries, as aapt would at build time.

    Two conventions, both load-bearing in the AAR's translations. A value wrapped in double quotes
    is quoted so its leading and trailing whitespace survives, and the quotes are not part of the
    text: French's "fermer" is the word, not the word in quotes. And a backslash escapes the
    character after it for the XML parser, which is how an apostrophe and a newline get in.
    """
    text = value
    if len(text) >= 2 and text.startswith('"') and text.endswith('"'):
        text = text[1:-1]
    out = []
    index = 0
    while index < len(text):
        char = text[index]
        if char == "\\" and index + 1 < len(text):
            following = text[index + 1]
            out.append({"n": "\n", "t": "\t"}.get(following, following))
            index += 2
            continue
        out.append(char)
        index += 1
    return "".join(out)


def kotlin_string(value: str) -> str:
    text = android_text(value)
    escaped = text
    for old, replacement in (
        ("\\", "\\\\"),
        ('"', '\\"'),
        ("$", "\\$"),
        ("\n", "\\n"),
        ("\t", "\\t"),
    ):
        escaped = escaped.replace(old, replacement)
    return '"' + escaped + '"'


def kotlin_map(entries: dict, indent: int) -> list[str]:
    """Render a (possibly nested) dict as a Kotlin `mapOf(...)`, ktfmt-shaped."""
    pad = " " * indent
    lines = [f"{pad}mapOf("]
    for key, value in entries.items():
        if isinstance(value, dict):
            lines.append(f'{pad}    {kotlin_string(key)} to')
            lines += kotlin_map(value, indent + 8)
            lines[-1] += ","
        else:
            lines.append(f"{pad}    {kotlin_string(key)} to {kotlin_string(value)},")
    lines.append(f"{pad})")
    return lines


def generate_resources(source_root: pathlib.Path, common: pathlib.Path, package: str) -> int:
    """Turn the AAR's `res/values*` into Kotlin lookup tables, one per locale.

    Android resolves `R.string.x` through the resource table an APK is built with, choosing the
    locale at runtime. Off-Android there is no such table, so the strings become what they always
    were underneath: maps from resource name to text, generated so that an upstream wording or
    translation change arrives with the next sync rather than by hand.

    Every locale the AAR ships is generated, not just the default. These strings are what a screen
    reader announces, and announcing them in English to someone using their watch in Arabic is a
    defect rather than a simplification.
    """
    resources = source_root / "resources"
    if not resources.is_dir():
        return 0

    def read(path: pathlib.Path) -> tuple[dict, dict]:
        root = xml.etree.ElementTree.fromstring(path.read_text())
        strings = {node.get("name"): "".join(node.itertext()) for node in root.findall("string")}
        plurals = {
            node.get("name"): {item.get("quantity"): "".join(item.itertext()) for item in node}
            for node in root.findall("plurals")
        }
        return dict(sorted(strings.items())), dict(sorted(plurals.items()))

    default_strings, default_plurals = read(resources / "default.xml")
    translated = {
        path.stem: read(path) for path in sorted(resources.glob("*.xml")) if path.stem != "default"
    }

    lines = [
        BANNER.rstrip("\n"),
        "// Source: the `res/values*` of the AAR pinned in upstream.json.",
        "",
        f"package {package}.internal",
        "",
        "/** Every `<string>` of the default locale, by resource name. */",
        "internal val GeneratedStrings: Map<String, String> =",
        *kotlin_map(default_strings, 4),
        "",
        "/** Every `<plurals>` of the default locale, by resource name then CLDR quantity keyword. */",
        "internal val GeneratedPlurals: Map<String, Map<String, String>> =",
        *kotlin_map(default_plurals, 4),
        "",
        "/**",
        " * The translations, by BCP 47 language tag. A tag that is absent falls back to",
        " * [GeneratedStrings] — the AAR's default locale, which is English.",
        " */",
        "internal val GeneratedLocalizedStrings: Map<String, Map<String, String>> =",
        *kotlin_map({tag: strings for tag, (strings, _) in translated.items()}, 4),
        "",
        "/** The translated plurals, by language tag, then resource name, then quantity keyword. */",
        "internal val GeneratedLocalizedPlurals: Map<String, Map<String, Map<String, String>>> =",
        *kotlin_map({tag: plurals for tag, (_, plurals) in translated.items()}, 4),
        "",
    ]

    target = common / package.replace(".", "/") / "internal" / "GeneratedResources.kt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(lines))
    return len(default_strings) + len(default_plurals) + len(translated)


# Framework colours an AAR may reference by name. Compose Multiplatform's vector parser resolves
# nothing outside the file it is reading, and Android's own values are fixed, so substituting them
# is exact rather than a guess. An unknown `@` reference stops the build instead of rendering wrong.
ANDROID_COLORS = {
    "@android:color/white": "#FFFFFFFF",
    "@android:color/black": "#FF000000",
    "@android:color/transparent": "#00000000",
}

ANDROID_NAMESPACE = 'xmlns:android="http://schemas.android.com/apk/res/android"'


def freeze_animated_vector(text: str) -> str:
    """Unwrap an `<animated-vector>` to its artwork, at the END of the animation.

    The `<vector>` nested inside an AVD is its FIRST frame, and for a drawing animation that frame
    is blank: the check mark's path carries `trimPathEnd="0"`, and the animation takes it to 1.
    Extracting the vector alone therefore yields an invisible icon — which is what the first
    attempt at this rendered, and what the rendered check caught.

    So each `<target>`'s animators are read for the value they end on, and that value is written
    onto the named element as a plain attribute. The result is a still of the last frame: no
    animation, but the artwork the component is trying to show.
    """
    document = xml.etree.ElementTree.fromstring(text)
    android = "{http://schemas.android.com/apk/res/android}"
    aapt = "{http://schemas.android.com/aapt}"

    # `<aapt:attr name="android:drawable">` holds the artwork; the rest of the file animates it.
    artwork = None
    for attr in document.findall(f"{aapt}attr"):
        if attr.get("name") == "android:drawable":
            artwork = attr.find("vector")
    if artwork is None:
        raise SystemExit("an <animated-vector> with no <vector> inside its aapt:attr")

    # target name -> property -> the value its last animator ends on.
    final: dict[str, dict[str, str]] = {}
    for target in document.findall(f"{android}target") + document.findall("target"):
        name = target.get(f"{android}name") or target.get("name")
        if name is None:
            continue
        for animator in target.iter():
            if not animator.tag.endswith("objectAnimator"):
                continue
            property_name = animator.get(f"{android}propertyName")
            value_to = animator.get(f"{android}valueTo")
            if property_name is None or value_to is None:
                continue
            final.setdefault(name, {})[property_name] = value_to

    for element in artwork.iter():
        name = element.get(f"{android}name")
        for property_name, value in final.get(name, {}).items():
            element.set(f"{android}{property_name}", value)

    xml.etree.ElementTree.register_namespace("android", "http://schemas.android.com/apk/res/android")
    return xml.etree.ElementTree.tostring(artwork, encoding="unicode")


# Framework colours an AAR may reference by name. Compose Multiplatform's vector parser resolves
# nothing outside the file it is reading, and Android's own values are fixed, so substituting them
# is exact rather than a guess. An unknown `@` reference stops the build instead of rendering wrong.
ANDROID_COLORS = {
    "@android:color/white": "#FFFFFFFF",
    "@android:color/black": "#FF000000",
    "@android:color/transparent": "#00000000",
}

ANDROID_NAMESPACE = 'xmlns:android="http://schemas.android.com/apk/res/android"'


def generate_drawables(
    source_root: pathlib.Path,
    module_dir: pathlib.Path,
    excluded: dict,
    common: pathlib.Path,
    package: str,
) -> int:
    """Copy the AAR's vector drawables into the module's Compose resources.

    Compose Multiplatform's resource pipeline parses Android `<vector>` XML on every target,
    including wasm, so these are upstream's own artwork rather than a redrawing of it.

    An `<animated-vector>` is still written out frozen at its last frame — see
    [freeze_animated_vector] — so a component that draws one WITHOUT the animation gets the right
    still, which is what every component did before the animation existed. The motion is emitted
    alongside it as data, by [vector_animation_tracks], and applied at draw time as a
    `VectorConfig` override; a drawable whose motion this generator cannot express emits no tracks
    and simply stays frozen. `@android:color/…` is substituted, because the parser resolves no
    reference it cannot see.
    """
    drawables = source_root / "drawables"
    if not drawables.is_dir():
        return 0

    destination = module_dir / "src" / "commonMain" / "composeResources" / "drawable"
    if destination.exists():
        shutil.rmtree(destination)
    destination.mkdir(parents=True, exist_ok=True)

    animations: dict = {}

    header = "<!-- Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT. -->\n"
    written = 0
    for source in sorted(drawables.glob("*.xml")):
        if source.name in excluded:
            continue
        text = source.read_text()
        if "<animated-vector" in text:
            vector = freeze_animated_vector(text)
        else:
            vector = text[text.index("<vector") :]

        if ANDROID_NAMESPACE not in vector:
            vector = vector.replace("<vector", f"<vector {ANDROID_NAMESPACE}", 1)

        for reference, value in ANDROID_COLORS.items():
            vector = vector.replace(reference, value)
        unresolved = re.findall(r'"(@[^"]+)"', vector)
        if unresolved:
            raise SystemExit(
                f"{source.name} references {', '.join(sorted(set(unresolved)))}, which Compose's "
                f"vector parser cannot resolve. Add it to ANDROID_COLORS in tools/transform.py if "
                f"it is a framework colour, or exclude the drawable."
            )

        (destination / source.name).write_text(header + vector)
        written += 1

        if "<animated-vector" in text:
            tracks = vector_animation_tracks(text)
            if tracks:
                animations[source.stem] = tracks

    write_vector_animations(animations, common, package)
    return written


# The `android:propertyName`s an `<objectAnimator>` can carry that map onto a
# `VectorProperty<Float>` in compose-ui. Everything else — `pathData`, `fillColor`, `strokeColor` —
# animates a type this generator does not express, and a drawable using one stays frozen.
ANIMATABLE_FLOAT_PROPERTIES = {
    "trimPathStart": "TrimPathStart",
    "trimPathEnd": "TrimPathEnd",
    "trimPathOffset": "TrimPathOffset",
    "rotation": "Rotation",
    "scaleX": "ScaleX",
    "scaleY": "ScaleY",
    "translateX": "TranslateX",
    "translateY": "TranslateY",
    "pivotX": "PivotX",
    "pivotY": "PivotY",
    "fillAlpha": "FillAlpha",
    "strokeAlpha": "StrokeAlpha",
    "strokeWidth": "StrokeLineWidth",
}

# The exporter puts a `time_group` target in every one of these files, carrying a ~10 s no-op
# translateX purely to hold the timeline open. It is not artwork and animating it would stretch
# every drawable to ten seconds.
TIMELINE_KEEPER = "time_group"

# The one non-float property this generator does express: a `pathData` morph, which the runtime
# interpolates control point by control point.
PATH_PROPERTY = "pathData"


def path_commands_match(start: str, end: str) -> bool:
    """Whether two path strings are the same sequence of commands, so a per-point lerp is defined.

    AVD requires it of a `pathData` animation and the exporter honours it; this is the check that
    the requirement actually held, because the alternative to noticing here is a drawable that
    interpolates two shapes into a third that is neither.
    """
    letters = re.compile(r"[A-Za-z]")
    return bool(start) and bool(end) and letters.findall(start) == letters.findall(end)


def cubic_easing(path_data: str) -> str | None:
    """An `<pathInterpolator>`'s `pathData` as a Compose `CubicBezierEasing`.

    Android writes the easing curve as an SVG path from (0,0) to (1,1) whose two control points are
    the four numbers Compose wants — `M 0.0,0.0 c0.2,0 0,1 1.0,1.0` is `CubicBezierEasing(0.2, 0,
    0, 1)`. Only that shape is recognised; anything else returns None and the caller falls back to
    the linear default rather than guessing at a curve.
    """
    match = re.fullmatch(
        r"\s*[Mm]\s*0(?:\.0*)?\s*,\s*0(?:\.0*)?\s*c\s*"
        r"([-\d.]+)\s*,\s*([-\d.]+)\s+([-\d.]+)\s*,\s*([-\d.]+)\s+"
        r"1(?:\.0*)?\s*,\s*1(?:\.0*)?\s*",
        path_data.strip(),
    )
    if not match:
        return None
    a, b, c, d = (float(g) for g in match.groups())
    return f"CubicBezierEasing({a}f, {b}f, {c}f, {d}f)"


def vector_animation_tracks(text: str) -> list | None:
    """The tracks of an `<animated-vector>`, or None if it animates something else.

    Two kinds come out: float segments, which move a `VectorProperty<Float>`, and path segments,
    which morph `pathData` between two path strings. Anything else — a `fillColor` or
    `strokeColor` animation — still returns None for the whole drawable, which is the honest answer
    rather than a partial one: animating a drawable's trims while holding its colour still reads as
    a bug rather than as a missing feature. Those stay frozen at the last frame.
    """
    # Elements are unqualified here; only the ATTRIBUTES carry the android namespace.
    android = "{http://schemas.android.com/apk/res/android}"
    document = xml.etree.ElementTree.fromstring(text)
    tracks = []
    by_name: dict = {}
    for target in document.iter("target"):
        name = target.get(android + "name")
        if name == TIMELINE_KEEPER:
            continue
        segments = []
        path_segments = []
        for animator in target.iter("objectAnimator"):
            prop = animator.get(android + "propertyName")
            if prop != PATH_PROPERTY and prop not in ANIMATABLE_FLOAT_PROPERTIES:
                return None
            easing = "LinearEasing"
            for interpolator in animator.iter("pathInterpolator"):
                derived = cubic_easing(interpolator.get(android + "pathData", ""))
                if derived:
                    easing = derived
            common_fields = {
                "start": int(float(animator.get(android + "startOffset", "0"))),
                "duration": int(float(animator.get(android + "duration", "0"))),
                "easing": easing,
            }
            if prop == PATH_PROPERTY:
                start_path = animator.get(android + "valueFrom", "")
                end_path = animator.get(android + "valueTo", "")
                # A morph the runtime cannot interpolate is not a morph. Both sides have to be the
                # same sequence of commands — which is what AVD guarantees and what the exporter
                # produces — so a pair that is not is refused here rather than drawn wrong.
                if not path_commands_match(start_path, end_path):
                    return None
                path_segments.append(
                    dict(common_fields, **{"from": start_path, "to": end_path})
                )
            else:
                segments.append(
                    dict(
                        common_fields,
                        **{
                            "property": ANIMATABLE_FLOAT_PROPERTIES[prop],
                            "from": float(animator.get(android + "valueFrom", "0")),
                            "to": float(animator.get(android + "valueTo", "0")),
                        },
                    )
                )
        if segments or path_segments:
            # The exporter emits SEVERAL <target> elements for one element when it animates more
            # than one kind of property — the same name twice, once for its floats and once for its
            # path. They are one track: the painter keys its overrides by name, so two entries for
            # one name would silently drop whichever came first.
            merged = by_name.get(name)
            if merged is None:
                merged = {"target": name, "segments": [], "pathSegments": []}
                by_name[name] = merged
                tracks.append(merged)
            merged["segments"].extend(segments)
            merged["pathSegments"].extend(path_segments)
    return tracks or None



def kotlin_path_literal(value: str) -> str:
    """A path string as a Kotlin string literal.

    Not `kotlin_string`: that one runs the value through `android_text` first, which is right for a
    resource string and wrong for path data — this is markup the vector parser reads back verbatim.
    """
    escaped = value.strip().replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")
    return '"' + escaped + '"'


def write_vector_animations(animations: dict, common: pathlib.Path, package: str) -> None:
    """Emit the animated drawables' motion as Kotlin.

    The shape is deliberately data rather than code: a list of tracks, each a target name and the
    segments that move one of its float properties. `AnimatedVectorPainter` in `commonPort` is what
    turns that into a running animation, so an upstream retiming arrives with the next sync and
    nothing here has to be re-reasoned about.
    """
    target = common / package.replace(".", "/") / "internal" / "GeneratedVectorAnimations.kt"
    target.parent.mkdir(parents=True, exist_ok=True)

    lines = [
        BANNER.rstrip("\n"),
        f"package {package}.internal",
        "",
        "import androidx.compose.animation.core.CubicBezierEasing",
        "import androidx.compose.animation.core.LinearEasing",
        "",
        "/**",
        " * The motion of each animated vector drawable, keyed by drawable name.",
        " *",
        " * A drawable absent from this map is one whose `<animated-vector>` moves something this",
        " * generator does not express — a `fillColor` or `strokeColor` animation — and is drawn as",
        " * the still it has always been.",
        " */",
        "internal val GeneratedVectorAnimations: Map<String, List<VectorAnimationTrack>> =",
        "    mapOf(",
    ]
    for name, tracks in sorted(animations.items()):
        lines.append(f'        "{name}" to')
        lines.append("            listOf(")
        for track in tracks:
            lines.append("                VectorAnimationTrack(")
            lines.append(f'                    targetName = "{track["target"]}",')
            lines.append("                    segments =")
            lines.append("                        listOf(")
            for segment in track["segments"]:
                lines.append("                            VectorAnimationSegment(")
                lines.append(
                    f"                                property = VectorAnimatedProperty."
                    f"{segment['property']},"
                )
                lines.append(f"                                startOffsetMillis = {segment['start']},")
                lines.append(f"                                durationMillis = {segment['duration']},")
                lines.append(f"                                from = {segment['from']}f,")
                lines.append(f"                                to = {segment['to']}f,")
                lines.append(f"                                easing = {segment['easing']},")
                lines.append("                            ),")
            lines.append("                        ),")
            if track["pathSegments"]:
                lines.append("                    pathSegments =")
                lines.append("                        listOf(")
                for segment in track["pathSegments"]:
                    lines.append("                            VectorPathSegment(")
                    lines.append(
                        f"                                startOffsetMillis = {segment['start']},"
                    )
                    lines.append(
                        f"                                durationMillis = {segment['duration']},"
                    )
                    lines.append(
                        f"                                from = {kotlin_path_literal(segment['from'])},"
                    )
                    lines.append(
                        f"                                to = {kotlin_path_literal(segment['to'])},"
                    )
                    lines.append(f"                                easing = {segment['easing']},")
                    lines.append("                            ),")
                lines.append("                        ),")
            lines.append("                ),")
        lines.append("            ),")
    lines.append("    )")
    lines.append("")
    target.write_text("\n".join(lines))


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
    drawables = generate_drawables(
        source_root,
        module_dir,
        rules["modules"].get(module, {}).get("excludeDrawables", {}),
        common,
        entry["package"],
    )
    applied = apply_patches(module_dir, ROOT / "patches" / module)

    for generated in sorted(common.rglob("*.kt")):
        found = android_imports(generated.read_text(), rules["androidOnlyPrefixes"])
        if found:
            offenders[generated.relative_to(common).as_posix()] = found

    print(
        f"  {module}: {copied} files, {len(skipped)} excluded, "
        f"{len(applied)} patches, {len(offenders)} still Android-bound"
        + (f", {resources} resources" if resources else "")
        + (f", {drawables} drawables" if drawables else "")
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
