#!/usr/bin/env python3
"""Track the alpha Remote Compose line against androidx.dev snapshots.

WHY THIS EXISTS. `:remote-catalog` is pinned to released alphas, and three
divergences on its sheet are the library rendering its own API wrongly rather
than this catalog asking for the wrong thing — issues #89, #90 and #91. Each is
"fixed upstream or not at all", and the only way to know is to build the sheet
against a newer artifact and look. Doing that by hand costs an afternoon and
gets done once; this makes it a weekly job.

WHAT IT IS NOT. It never decides that a bug is *fixed*. It decides that a render
**changed**, which is the honest limit of a machine here: the captures it
compares against are the known-broken ones, so byte-identical means "still
broken" with certainty, while anything else means "a human should look". Where a
defect has a crisp numeric expression (a container height, a label's alpha) the
measurement rides along in the report so the human starts with a number instead
of a picture.

The overlay this writes is PROBE-ONLY and must never reach `main`: it repoints
the three Remote groups at a snapshot repository, which is exactly the skew
`AGENTS.md` forbids in the committed build. The workflow applies it to a
throwaway checkout and publishes only the report.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
import urllib.request
from pathlib import Path

# The artifact whose `latest` redirect names the build, and the groups the
# overlay repoints. The Remote trio MOVES TOGETHER (AGENTS.md): a skewed pair
# fails inside the player at render time rather than at compile time, so all
# three come from one snapshot build or the probe is meaningless.
LATEST_URL = (
    "https://androidx.dev/snapshots/latest/artifacts/repository"
    "/androidx/wear/compose/remote/remote-material3/maven-metadata.xml"
)
# Written verbatim into a Kotlin string literal, so the backslashes are DOUBLED here: `\.` in
# settings.gradle.kts is the regex dot-escape, while a single `\.` is a Kotlin "unsupported escape
# sequence" and fails script compilation before any dependency resolves. Pinned by
# `test_the_group_regexes_survive_kotlin_string_escaping`.
SNAPSHOT_GROUPS = (
    r"androidx\\.compose\\.remote.*",
    r"androidx\\.wear\\.compose\\.remote.*",
    r"androidx\\.glance\\.wear.*",
)
VERSION_REFS = ("compose-remote", "wear-compose-remote", "glance-wear")

# One artifact per version ref, hashed to decide whether a run is worth doing at all.
#
# The build id is NOT that signal. androidx.dev publishes many builds a day and most of them carry
# byte-identical Remote artifacts — builds 16201507 and 16202139, two hours apart, ship the same
# AAR under different timestamps for all three groups. Gating on the id would spend a runner on a
# full render to learn nothing, weekly. Gating on the bytes makes the common week a 30-second
# no-op and reserves the render for a build that actually changed something.
FINGERPRINT_ARTIFACTS = {
    "wear-compose-remote": "androidx/wear/compose/remote/remote-material3",
    "compose-remote": "androidx/compose/remote/remote-creation-compose",
    "glance-wear": "androidx/glance/wear/wear",
}

# Renders are captured at `dpi=320`, i.e. density 2.0 — see CatalogTheme.kt. The
# filename carries it, so the conversion is read rather than assumed.
_DPI_IN_NAME = re.compile(r"_dpi_(\d+)")

# One entry per tracked issue. `baseline` is the KNOWN-BROKEN capture committed
# under docs/evidence — identical to it is the only verdict this script states
# with confidence.
PROBES = [
    {
        "issue": 89,
        "preview": "OutlinedCardRemote",
        "baseline": "docs/evidence/remote-m3-card-outlined-break.png",
        "summary": "RemoteOutlinedCard draws two hairlines instead of a border",
    },
    {
        "issue": 90,
        "preview": "CompactIconOnlyRemoteButton",
        "baseline": "docs/evidence/remote-m3-button-compact-icononly-break.png",
        "summary": "RemoteCompactButton renders at half height; icon-only collapses the glyph",
        "metrics": ("compact_heights_dp", "icononly_glyph_dp"),
    },
    {
        "issue": 91,
        "preview": "DisabledRemoteButton",
        "baseline": "docs/evidence/remote-m3-button-disabled-break.png",
        "summary": "a disabled RemoteButton draws no label",
        "metrics": ("disabled_max_alpha",),
    },
]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


# ── resolve ────────────────────────────────────────────────────────────────────


def resolve_latest(url: str = LATEST_URL, *, fingerprint: bool = True) -> dict:
    """The build `androidx.dev/snapshots/latest` points at, and what it ships.

    `latest` is a redirect, so the build id is read off the URL it lands on
    rather than scraped from the builds page. Pinning that id is what makes a
    run reproducible: `latest` moves several times a day.

    The fingerprint is what makes a run *worth doing* — see
    [FINGERPRINT_ARTIFACTS].
    """
    with urllib.request.urlopen(url, timeout=60) as response:
        final = response.geturl()
        body = response.read().decode("utf-8", "replace")
    build_id = build_id_from_url(final)
    resolved = {"buildId": build_id, "lastUpdated": last_updated(body)}
    if fingerprint:
        resolved["fingerprint"] = fingerprint_of(build_id)
    return resolved


def _read(url: str) -> bytes:
    with urllib.request.urlopen(url, timeout=120) as response:
        return response.read()


def fingerprint_of(build_id: str) -> dict:
    """`{version ref: sha256}` for one artifact per group in ``build_id``."""
    base = f"https://androidx.dev/snapshots/builds/{build_id}/artifacts/repository"
    prints = {}
    for ref, path in FINGERPRINT_ARTIFACTS.items():
        metadata = _read(f"{base}/{path}/1.0.0-SNAPSHOT/maven-metadata.xml").decode("utf-8", "replace")
        match = re.search(r"1\.0\.0-\d{8}\.\d{6}-\d+", metadata)
        if not match:
            raise SystemExit(f"no timestamped snapshot version for {path} in build {build_id}")
        version = match.group(0)
        artifact = f"{base}/{path}/1.0.0-SNAPSHOT/{path.rsplit('/', 1)[1]}-{version}.aar"
        prints[ref] = {
            "version": version,
            "sha256": hashlib.sha256(_read(artifact)).hexdigest(),
        }
    return prints


def build_id_from_url(url: str) -> str:
    match = re.search(r"/snapshots/builds/(\d+)/", url)
    if not match:
        raise SystemExit(f"could not read a build id out of {url!r}")
    return match.group(1)


def last_updated(metadata_xml: str) -> str | None:
    match = re.search(r"<lastUpdated>(\d+)</lastUpdated>", metadata_xml)
    return match.group(1) if match else None


# ── apply ──────────────────────────────────────────────────────────────────────


def repository_block(build_id: str) -> str:
    groups = "\n".join(f'        includeGroupByRegex("{g}")' for g in SNAPSHOT_GROUPS)
    return (
        "    // PROBE ONLY, written by scripts/remote-snapshot-probe.py. Never commit this to\n"
        "    // main: it repoints the Remote trio at an unreleased build.\n"
        f'    maven("https://androidx.dev/snapshots/builds/{build_id}/artifacts/repository") {{\n'
        "      content {\n"
        f"{groups}\n"
        "      }\n"
        "    }\n"
    )


def apply_overlay(root: Path, build_id: str) -> list[str]:
    """Repoint the Remote groups at ``build_id``. Returns what it changed."""
    changed = []

    settings = root / "settings.gradle.kts"
    text = settings.read_text()
    anchor = "dependencyResolutionManagement {\n  repositories {\n"
    if anchor not in text:
        raise SystemExit(
            "settings.gradle.kts no longer opens dependencyResolutionManagement the way this "
            "script patches it — fix the anchor rather than letting the probe silently test the "
            "released artifacts."
        )
    marker = "/snapshots/builds/"
    if marker in text:
        raise SystemExit("settings.gradle.kts already carries a snapshot repository")
    settings.write_text(text.replace(anchor, anchor + repository_block(build_id), 1))
    changed.append(f"settings.gradle.kts: + snapshot repository for build {build_id}")

    toml = root / "gradle" / "libs.versions.toml"
    text = toml.read_text()
    for ref in VERSION_REFS:
        pattern = re.compile(rf'^{re.escape(ref)} = "([^"]+)"$', re.MULTILINE)
        match = pattern.search(text)
        if not match:
            raise SystemExit(f"no version ref {ref!r} in gradle/libs.versions.toml")
        changed.append(f"{ref}: {match.group(1)} -> 1.0.0-SNAPSHOT")
        text = pattern.sub(f'{ref} = "1.0.0-SNAPSHOT"', text, count=1)
    toml.write_text(text)
    return changed


# ── measure ────────────────────────────────────────────────────────────────────


def density_of(name: str) -> float:
    match = _DPI_IN_NAME.search(name)
    return (int(match.group(1)) / 160.0) if match else 2.0


def _drawn_bbox(image, predicate):
    pixels = image.load()
    width, height = image.size
    xs, ys = [], []
    for y in range(height):
        for x in range(width):
            if predicate(pixels[x, y]):
                xs.append(x)
                ys.append(y)
    if not xs:
        return None
    return min(xs), min(ys), max(xs) - min(xs) + 1, max(ys) - min(ys) + 1


def measure(renders: Path) -> dict:
    """Per-render hashes, plus the numeric probes the issues are written around."""
    from PIL import Image

    captures = {}
    for png in sorted(renders.glob("*.png")):
        captures[png.name.rsplit("-", 1)[0]] = sha256(png)

    def one(stem: str):
        matches = sorted(renders.glob(f"{stem}_*.png"))
        return (Image.open(matches[0]).convert("RGBA"), matches[0].name) if matches else (None, None)

    metrics: dict[str, object] = {}

    # #91 — the disabled label resolves to nothing above the 12% container.
    image, name = one("DisabledRemoteButton")
    if image:
        alpha = image.getchannel("A")
        metrics["disabled_max_alpha"] = alpha.getextrema()[1]

    # #90 — the compact container renders at half its declared 32dp, and the
    # icon-only overload collapses its 24dp glyph.
    heights = {}
    for stem in ("CompactRemoteButton", "CompactTextOnlyRemoteButton", "CompactIconOnlyRemoteButton"):
        image, name = one(stem)
        if not image:
            continue
        scale = density_of(name)
        box = _drawn_bbox(image, lambda p: p[3] > 40)
        if box:
            heights[stem] = round(box[3] / scale)
        if stem == "CompactIconOnlyRemoteButton":
            glyph = _drawn_bbox(image, lambda p: p[3] > 40 and p[0] < 120)
            if glyph:
                metrics["icononly_glyph_dp"] = [round(glyph[2] / scale), round(glyph[3] / scale)]
    if heights:
        metrics["compact_heights_dp"] = heights

    return {"captures": captures, "metrics": metrics}


def probe_states(report: dict, root: Path, renders: Path) -> list[dict]:
    """Whether each tracked issue's capture still matches its known-broken one."""
    states = []
    for probe in PROBES:
        matches = sorted(renders.glob(f"{probe['preview']}_*.png"))
        baseline = root / probe["baseline"]
        current = sha256(matches[0]) if matches else None
        known_broken = sha256(baseline) if baseline.exists() else None
        states.append(
            {
                "issue": probe["issue"],
                "preview": probe["preview"],
                "summary": probe["summary"],
                "rendered": current is not None,
                "identicalToKnownBroken": (
                    None if current is None or known_broken is None else current == known_broken
                ),
                "metrics": {k: report["metrics"][k] for k in probe.get("metrics", ()) if k in report["metrics"]},
            }
        )
    return states


# ── compare ────────────────────────────────────────────────────────────────────


def unchanged_artifacts(previous: dict | None, current_fingerprint: dict) -> bool:
    """Whether this build ships the same Remote bytes the last probe already tested."""
    if not previous or not current_fingerprint:
        return False
    before = (previous.get("build") or {}).get("fingerprint") or {}
    if set(before) != set(current_fingerprint):
        return False
    return all(before[ref]["sha256"] == current_fingerprint[ref]["sha256"] for ref in before)


def compare(previous: dict | None, current: dict) -> dict:
    """What changed since the last run, and whether a human needs to look."""
    reasons: list[str] = []

    if current.get("build", {}).get("compiled") is False:
        reasons.append("the snapshot no longer compiles against this catalog")

    prev_probes = {p["issue"]: p for p in (previous or {}).get("probes", [])}
    flipped = []
    for probe in current.get("probes", []):
        before = prev_probes.get(probe["issue"], {}).get("identicalToKnownBroken")
        now = probe["identicalToKnownBroken"]
        if now is False:
            flipped.append(probe)
        elif before is not None and before != now:
            flipped.append(probe)
    if flipped:
        reasons.append(
            "a tracked issue's capture no longer matches its known-broken one: "
            + ", ".join(f"#{p['issue']}" for p in flipped)
        )

    # A failed build measures nothing, so its empty capture set is not "every render was
    # removed" — the compile reason above already says what happened, and adding a phantom
    # removal list to it would bury the one line that matters.
    compiled = current.get("build", {}).get("compiled") is not False
    prev_caps = (previous or {}).get("captures", {}) if compiled else {}
    cur_caps = current.get("captures", {})
    moved = sorted(k for k in cur_caps if k in prev_caps and prev_caps[k] != cur_caps[k])
    added = sorted(set(cur_caps) - set(prev_caps)) if prev_caps else []
    removed = sorted(set(prev_caps) - set(cur_caps)) if prev_caps else []
    if prev_caps and (moved or added or removed):
        reasons.append(f"{len(moved)} render(s) changed, {len(added)} added, {len(removed)} removed")

    return {
        "report": bool(reasons),
        "reasons": reasons,
        "flipped": flipped,
        "movedCaptures": moved,
        "addedCaptures": added,
        "removedCaptures": removed,
        "firstRun": previous is None,
    }


def _render_metric(key: str, value) -> str:
    """One metric as a markdown fragment. Dicts read as `a=1, b=2`, lists as `2x1`."""
    if isinstance(value, dict):
        body = ", ".join(f"{k}={v}" for k, v in sorted(value.items()))
    elif isinstance(value, list):
        body = "x".join(str(v) for v in value)
    else:
        body = str(value)
    return f"`{key}` {body}"


def summary_markdown(current: dict, verdict: dict) -> str:
    build = current.get("build", {})
    lines = [
        f"## Remote Compose snapshot probe — androidx.dev build "
        f"[{build.get('buildId')}](https://androidx.dev/snapshots/builds/{build.get('buildId')}/artifacts)",
        "",
        f"Snapshot artifacts last updated `{build.get('lastUpdated')}`. "
        f"Compiled: **{'yes' if build.get('compiled') else 'no'}**.",
        "",
    ]
    if verdict["reasons"]:
        lines += ["**Why this is being reported**", ""]
        lines += [f"- {r}" for r in verdict["reasons"]]
        lines.append("")

    lines += ["| issue | preview | still byte-identical to the known-broken capture | measured |",
              "| --- | --- | --- | --- |"]
    for probe in current.get("probes", []):
        identical = probe["identicalToKnownBroken"]
        mark = {True: "yes — still broken", False: "**no — look**", None: "not rendered"}[identical]
        measured = "<br>".join(_render_metric(k, v) for k, v in probe["metrics"].items()) or "—"
        lines.append(f"| #{probe['issue']} | `{probe['preview']}` | {mark} | {measured} |")
    lines.append("")

    if verdict["movedCaptures"]:
        lines += ["<details><summary>Renders that changed since the previous probe</summary>", ""]
        lines += [f"- `{name}`" for name in verdict["movedCaptures"]]
        lines += ["", "</details>", ""]

    lines.append(
        "_This job never claims a bug is fixed — it reports that something moved. "
        "`scripts/remote-snapshot-probe.py` explains the limit._"
    )
    return "\n".join(lines)


# ── cli ────────────────────────────────────────────────────────────────────────


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)

    p_resolve = sub.add_parser("resolve", help="describe a snapshot build as JSON")
    p_resolve.add_argument(
        "--build-id", default="", help="a specific build; empty resolves whatever `latest` points at"
    )
    p_resolve.add_argument("--out", type=Path, help="write the JSON here as well as to stdout")

    p_apply = sub.add_parser("apply", help="repoint the Remote groups at a snapshot build")
    p_apply.add_argument("--build-id", required=True)
    p_apply.add_argument("--root", type=Path, default=Path("."))

    p_measure = sub.add_parser("measure", help="hash every render and run the issue probes")
    p_measure.add_argument("--renders", type=Path, required=True)
    p_measure.add_argument("--root", type=Path, default=Path("."))
    p_measure.add_argument("--resolved", type=Path, required=True, help="output of `resolve`")
    p_measure.add_argument("--compiled", choices=("true", "false"), default="true")
    p_measure.add_argument("--out", type=Path, required=True)

    p_gate = sub.add_parser("gate", help="print true when this build is worth rendering")
    p_gate.add_argument("--previous", type=Path)
    p_gate.add_argument("--resolved", type=Path, required=True)

    p_compare = sub.add_parser("compare", help="diff two reports and write a markdown summary")
    p_compare.add_argument("--previous", type=Path)
    p_compare.add_argument("--current", type=Path, required=True)
    p_compare.add_argument("--out", type=Path, required=True)

    args = parser.parse_args(argv)

    if args.command == "resolve":
        if args.build_id:
            resolved = {
                "buildId": args.build_id,
                "lastUpdated": None,
                "fingerprint": fingerprint_of(args.build_id),
            }
        else:
            resolved = resolve_latest()
        body = json.dumps(resolved, indent=2, sort_keys=True)
        if args.out:
            args.out.write_text(body + "\n")
        print(body)
        return 0

    if args.command == "apply":
        for line in apply_overlay(args.root, args.build_id):
            print(line)
        return 0

    if args.command == "measure":
        compiled = args.compiled == "true"
        report = measure(args.renders) if compiled else {"captures": {}, "metrics": {}}
        resolved = json.loads(args.resolved.read_text())
        report["build"] = {
            "buildId": resolved.get("buildId"),
            "lastUpdated": resolved.get("lastUpdated"),
            "compiled": compiled,
            "fingerprint": resolved.get("fingerprint") or {},
        }
        report["probes"] = probe_states(report, args.root, args.renders) if compiled else []
        args.out.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n")
        print(f"wrote {args.out} — {len(report['captures'])} capture(s)")
        return 0

    if args.command == "gate":
        previous = (
            json.loads(args.previous.read_text())
            if args.previous and args.previous.exists()
            else None
        )
        resolved = json.loads(args.resolved.read_text())
        same = unchanged_artifacts(previous, resolved.get("fingerprint") or {})
        print("false" if same else "true")
        return 0

    if args.command == "compare":
        previous = (
            json.loads(args.previous.read_text())
            if args.previous and args.previous.exists()
            else None
        )
        current = json.loads(args.current.read_text())
        verdict = compare(previous, current)
        args.out.write_text(summary_markdown(current, verdict) + "\n")
        print(json.dumps({k: v for k, v in verdict.items() if k != "flipped"}))
        return 0

    return 2


if __name__ == "__main__":
    sys.exit(main())
