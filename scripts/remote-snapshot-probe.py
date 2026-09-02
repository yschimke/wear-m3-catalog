#!/usr/bin/env python3
"""Track the alpha Remote Compose line against androidx.dev snapshots.

WHY THIS EXISTS. `:remote-catalog` is pinned to released alphas, and several
divergences on its sheet are the render pipeline drawing the API wrongly rather
than this catalog asking for the wrong thing. The only way to know is to build
the sheet against a newer artifact and look. Doing that by hand costs an
afternoon and gets done once; this makes it a weekly job.

NOT ALL OF THEM ARE UPSTREAM, and the file used to claim they were. #89 and #90
were both re-diagnosed as OURS on 2026-08-28: their symptoms are conditional on
render density — the outlined card's border closes at 1.0 and loses its arcs at
2.0, the compact button's pill is right at 1.0 and half at 2.0 — which no
snapshot bump can be expected to fix. They stay probed anyway, because the
question "did the picture move" is worth asking of a bug wherever it lives, and
because a snapshot that changes the drawing path would show up here first.

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
import io
import json
import re
import sys
import urllib.request
import zipfile
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

# THE DENSITY SWEEP, and why the probe grew one.
#
# #89 and #90 were both filed as upstream defects and both re-diagnosed as this repo's, on the same
# evidence: they are CONDITIONAL ON RENDER DENSITY. The outlined card's border closes at 1.0 and
# loses its arcs at 2.0; the compact button's pill is right at 1.0, half at 2.0 and gone at 3.0.
# Both issues say the same thing about this job — "the probe renders at the same density, so it
# re-measures the same conditional failure" — and ask for its baselines to be rebuilt around that.
#
# This is that. The overlay adds the three densities the committed previews do not carry, so every
# probe is measured across the range rather than at the one point where the bug happens to sit.
#
# 320 is absent deliberately: it is the density the committed `@Preview`s already declare, and
# adding it again would render every sticker twice for nothing.
SWEEP_DPI = (160, 240, 480)
# The multipreview annotations the probes' stickers wear. Patching the annotation rather than each
# preview is what makes a CELL sweep too: a cell inherits its component's frame, so it inherits the
# extra densities with it and needs no wrapper of its own.
SWEEP_ANNOTATIONS = ("CatalogRemoteModes", "CatalogRemoteLarge")
SWEEP_SOURCE = "remote-catalog/src/main/kotlin/ee/schimke/wearm3catalog/remote/CatalogTheme.kt"

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

# ── THE API WATCHLIST ──────────────────────────────────────────────────────────
#
# Components this catalog is WAITING FOR, named by the class the library would have to publish.
#
# WHY A SYMBOL AND NOT A CHANGE. The obvious way to track an upstream component is to watch its
# Gerrit change, and that is the wrong signal twice over: a merged change is not a published
# artifact (`RemoteSplitCheckboxButton` merged at 15:58 and reached build 16245930 at 17:18, and a
# build cut in between has the change and not the class), and android-review is a host this repo
# does not otherwise talk to. The class either is in the AAR the sheet would compile against or it
# is not — and that is the question a sticker actually waits on. The change URL rides along as a
# LINK for the human, never as the thing being polled.
#
# WHAT IT COSTS. Nothing extra: `fingerprint_of` already downloads `remote-material3`'s AAR to hash
# it, so the class list is read out of bytes the gate was fetching anyway.
#
# THE PRECEDENT THIS EXISTS FOR. `Toggle+Selection-Buttons` sat at `—` on the Remote sheet for as
# long as the sheet has existed, and the day `RemoteCheckboxButton` appeared nothing said so — it
# was found by hand, by diffing two AARs, weeks later than it could have been. An entry here is the
# fix for that: it is a claim that a component is missing, and the probe retires the claim out loud.
#
# Retire an entry the week it flips. A watch that has arrived reports "present" every week
# thereafter and means nothing by it — the same silence-by-noise `PROBES` above is careful about.
AWAITED_API = [
    {
        "symbol": "androidx/wear/compose/remote/material3/RemoteSwitchButtonKt",
        "unlocks": (
            "`Toggle+Selection-Buttons` `Type=Switch`. Its SPLIT form (`RemoteSplitSwitchButton`) "
            "already ships; the plain row does not, and a component published as split-only would "
            "map its base render onto the kit's `Split=No` node"
        ),
        "change": "https://android-review.googlesource.com/c/platform/frameworks/support/+/4260122",
    },
    {
        "symbol": "androidx/wear/compose/remote/material3/RemoteRadioButtonKt",
        "unlocks": (
            "`Toggle+Selection-Buttons` `Type=Radio`, on the same terms as `Switch` above — "
            "`RemoteSplitRadioButton` ships, the plain row does not"
        ),
        "change": "https://android-review.googlesource.com/c/platform/frameworks/support/+/4260142",
    },
]
# The artifact the watchlist's symbols are looked for in. One AAR, because every awaited symbol so
# far is a `remote-material3` component; widen this to a per-entry field the first time one is not.
AWAITED_API_ARTIFACT = FINGERPRINT_ARTIFACTS["wear-compose-remote"]

# Renders are captured at `dpi=320`, i.e. density 2.0 — see CatalogTheme.kt. The
# filename carries it, so the conversion is read rather than assumed.
_DPI_IN_NAME = re.compile(r"_dpi_(\d+)")

# One entry per tracked issue. `baseline` is the KNOWN-BROKEN capture committed
# under docs/evidence — identical to it is the only verdict this script states
# with confidence.
# A probe names the RENDER it watches, and a render is a preview plus optionally a CELL.
#
# `variant` is not decoration. #116 folded most of this sheet's variants from top-level components
# into `@OverrideVariant` cells, which renames their captures from `DisabledRemoteButton_*` to
# `FilledRemoteButton_*_VARIANT_disabled-*` — and #90 and #91 kept naming the old stems, matched
# nothing, and reported "not rendered" week after week without ever saying a human should look.
# That is the exact silence this job exists to prevent, so `probe_states` now treats a probe that
# resolves to no render as a reason to report (see `compare`).
# #89 (outlined border) and #90 (compact pill height) were CLOSED on 2026-08-29 and their
# entries removed. A probe's whole verdict is "identical to the committed known-broken capture",
# so a probe whose bug is fixed reports `changed` every week for the rest of time and means
# nothing by it — the same silence-by-noise this job exists to avoid, from the other direction.
# Retire the entry when the issue closes; the evidence PNG stays under docs/evidence as the record
# of what the break looked like.
#
# A density sweep at 160/240/320/480 is what closed both: the compact pill measures 32dp at every
# density (its baseline capture is 16dp), and the card's outline closes with its corner arcs
# present at 2.0. Notably it was NOT the renderer — 1.46.1 and 1.46.2 render identically today.
PROBES = [
    {
        "issue": 91,
        "preview": "FilledRemoteButton",
        "variant": "disabled",
        "baseline": "docs/evidence/remote-m3-button-disabled-break.png",
        "summary": "a disabled RemoteButton draws no label",
        "metrics": ("disabled_max_alpha",),
    },
    {
        "issue": 130,
        "preview": "TextRemoteButton",
        "variant": "disabled",
        "baseline": "docs/evidence/remote-m3-text-button-disabled-break.png",
        "summary": "a disabled RemoteTextButton draws nothing at all — neither colour resolves",
        "metrics": ("text_disabled_max_alpha",),
    },
]


def find_render(renders: Path, preview: str, variant: str | None = None):
    """The capture a probe watches, or None.

    A base capture is `<preview>_<device>-<digest>.png`; a cell adds `_VARIANT_<cell>` before the
    digest. Both halves are matched EXPLICITLY, and that matters in both directions: `<preview>_*`
    alone also matches every cell of that preview, so a base probe would silently start reading a
    cell's bytes the day one is added, with sort order the only thing keeping them apart.
    """
    candidates = sorted(renders.glob(f"{preview}_*.png"))
    if variant is None:
        matches = [p for p in candidates if "_VARIANT_" not in p.name]
    else:
        matches = [p for p in candidates if f"_VARIANT_{variant}-" in p.name]
    return matches[0] if matches else None


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


def classes_in(aar: bytes) -> set[str]:
    """Every top-level class in an AAR's `classes.jar`, as `a/b/C` (no `.class`, no `$` nested)."""
    with zipfile.ZipFile(io.BytesIO(aar)) as bundle:
        jar = bundle.read("classes.jar")
    with zipfile.ZipFile(io.BytesIO(jar)) as classes:
        return {
            name[: -len(".class")]
            for name in classes.namelist()
            if name.endswith(".class") and "$" not in name
        }


def awaited_api_in(aar: bytes) -> dict:
    """`{symbol: present}` for [AWAITED_API] against one artifact's classes."""
    present = classes_in(aar)
    return {entry["symbol"]: entry["symbol"] in present for entry in AWAITED_API}


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
        body = _read(artifact)
        prints[ref] = {
            "version": version,
            "sha256": hashlib.sha256(body).hexdigest(),
        }
        # Read the watchlist out of the SAME bytes rather than fetching the AAR twice. It is
        # deliberately not folded into the entry above: `unchanged_artifacts` compares fingerprint
        # dicts to decide whether a run is worth doing, and an extra key in there would make every
        # build with a new symbol look like a byte change (it is one — but the gate should stay a
        # statement about bytes, not about this file's opinions).
        if path == AWAITED_API_ARTIFACT:
            prints["_awaitedApi"] = awaited_api_in(body)
    return prints


def awaited_api_of(fingerprint: dict) -> dict:
    """The watchlist map a `fingerprint_of` result carries, or `{}`."""
    return fingerprint.get("_awaitedApi") or {}


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


def apply_density_sweep(root: Path) -> list[str]:
    """Give the probes' multipreview annotations the densities the committed sheet does not carry.

    PROBE-ONLY, like the repository overlay beside it: rendering the whole sheet four times is a
    cost this job is happy to pay weekly and `main` is not, and a committed sweep would quadruple
    every PR's visual diff to answer a question only this job asks.

    Patches the ANNOTATION rather than each preview, which is what makes cells sweep too — a cell
    inherits its component's frame, so it inherits the extra densities with it.
    """
    source = root / SWEEP_SOURCE
    text = source.read_text()
    changed = []
    for name in SWEEP_ANNOTATIONS:
        anchor = f"annotation class {name}"
        if anchor not in text:
            raise SystemExit(
                f"no `{anchor}` in {SWEEP_SOURCE} — the probe patches it to sweep densities, and "
                "silently measuring one density is the failure #89 and #90 asked it to stop making."
            )
        head, _, tail = text.partition(anchor)
        preview_at = head.rfind("@Preview(")
        line_end = head.index("\n", preview_at)
        line = head[preview_at:line_end]
        if 'dpi=320' not in line:
            raise SystemExit(f"the @Preview above `{anchor}` is not the dpi=320 one this patches: {line}")
        extra = "\n".join(line.replace("dpi=320", f"dpi={dpi}") for dpi in SWEEP_DPI)
        text = head[:line_end] + "\n" + extra + head[line_end:] + anchor + tail
        changed.append(f"{name}: + dpi {', '.join(str(d) for d in SWEEP_DPI)}")
    source.write_text(text)
    return changed


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

    changed += apply_density_sweep(root)

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

    def one(stem: str, variant: str | None = None):
        match = find_render(renders, stem, variant)
        return (Image.open(match).convert("RGBA"), match.name) if match else (None, None)

    def max_alpha(stem: str, variant: str | None = None):
        image, _ = one(stem, variant)
        return None if image is None else image.getchannel("A").getextrema()[1]

    metrics: dict[str, object] = {}

    # #91 — the disabled label resolves to nothing above the 12% container. A CELL of
    # `FilledRemoteButton` since #116 folded it; it was `DisabledRemoteButton` before.
    alpha = max_alpha("FilledRemoteButton", "disabled")
    if alpha is not None:
        metrics["disabled_max_alpha"] = alpha

    # #130 — the disabled TEXT button resolves neither of its colours, so the whole capture is
    # transparent. 0 is "still broken"; anything above it means a human should look.
    alpha = max_alpha("TextRemoteButton", "disabled")
    if alpha is not None:
        metrics["text_disabled_max_alpha"] = alpha

    return {"captures": captures, "metrics": metrics}


# Alpha above which a pixel counts as drawn. 8 matches what the rest of this repo means by
# "opaque" — `rc-compare-pixels.mjs` and `StickerBakeCoverageTest` both use it — and it has to be
# this low to see #91 at all: a disabled container resolves to alpha 31 and a 40 floor calls the
# whole capture empty.
INK_ALPHA = 8

# How much the ink measure may wander between densities before it counts as varying.
#
# It cannot be zero. Antialiasing puts a different number of partly-covered pixels on the same
# shape at each density, so a perfectly correct component still drifts a little: the compact button
# measures 1452 / 1454 / 1451 / 1447 dp² across the sweep, a spread of 0.5%. #89, which is the
# thing this is meant to catch, loses 3132 -> 2403 dp² over the same range — 23%. Five percent sits
# an order of magnitude clear of the noise and an order of magnitude below the signal.
INK_TOLERANCE = 0.05


def drawn_ink_dp2(image, density: float) -> float:
    """How much ink the render carries, in dp² — the measure that sees a stroke go missing.

    The bounding box below cannot: #89's card keeps its full extent while its corner arcs vanish,
    because the content inside the box is what sets the box. Ink counts the pixels themselves, so a
    border that degenerates shows up as ink lost.

    Normalised by density², which is what makes it comparable across the sweep: the same shape at
    twice the density covers four times the pixels and the same dp².
    """
    alpha = image.getchannel("A")
    drawn = sum(count for value, count in enumerate(alpha.histogram()) if value > INK_ALPHA)
    return round(drawn / (density * density), 1)


def drawn_box_dp(image, density: float):
    """The drawn bounding box in dp, or None if nothing was drawn.

    Reported beside the ink because it is the legible half — `52x32` is a number a reader can check
    against the kit — but it is the coarser of the two: it catches a component whose whole extent
    scales wrongly (#90) and misses ink going missing INSIDE a box of the right size (#89).
    """
    box = _drawn_bbox(image, lambda p: p[3] > INK_ALPHA)
    return None if box is None else [round(box[2] / density), round(box[3] / density)]


def density_sweep(renders: Path, preview: str, variant: str | None) -> dict:
    """Every density this probe's render was captured at, mapped to what it drew there.

    The committed sheet declares one density, so on an ordinary checkout this returns a single
    entry and says nothing. The probe's overlay adds the rest (see [SWEEP_DPI]), which is what
    turns it into evidence.
    """
    from PIL import Image

    sweep = {}
    marker = f"_VARIANT_{variant}-" if variant else None
    for path in sorted(renders.glob(f"{preview}_*.png")):
        if marker is None and "_VARIANT_" in path.name:
            continue
        if marker is not None and marker not in path.name:
            continue
        density = density_of(path.name)
        image = Image.open(path).convert("RGBA")
        sweep[f"{density:g}"] = {
            "box": drawn_box_dp(image, density),
            "ink": drawn_ink_dp2(image, density),
        }
    return sweep


def is_density_invariant(sweep: dict) -> bool | None:
    """Whether the component draws the same thing at every density it was measured at.

    None when there is nothing to compare: fewer than two densities, or nothing drawn at any of
    them. "Nothing at every density" is absence, not invariance, and reporting it as True would let
    #130 — a cell that is blank everywhere — read as healthy.

    False is the #89 signature: layout that resolves correctly and paint that does not.
    """
    inks = [entry["ink"] for entry in sweep.values() if entry["box"] is not None]
    if len(inks) < 2:
        return None
    reference = max(inks)
    return all(abs(ink - reference) <= reference * INK_TOLERANCE for ink in inks)


def probe_states(report: dict, root: Path, renders: Path) -> list[dict]:
    """Whether each tracked issue's capture still matches its known-broken one."""
    states = []
    for probe in PROBES:
        match = find_render(renders, probe["preview"], probe.get("variant"))
        baseline = root / probe["baseline"]
        current = sha256(match) if match else None
        known_broken = sha256(baseline) if baseline.exists() else None
        variant = probe.get("variant")
        sweep = density_sweep(renders, probe["preview"], variant) if match else {}
        states.append(
            {
                "issue": probe["issue"],
                "preview": probe["preview"] + (f"#{variant}" if variant else ""),
                "summary": probe["summary"],
                "rendered": current is not None,
                "identicalToKnownBroken": (
                    None if current is None or known_broken is None else current == known_broken
                ),
                "densitySweep": sweep,
                "densityInvariant": is_density_invariant(sweep),
                "metrics": {k: report["metrics"][k] for k in probe.get("metrics", ()) if k in report["metrics"]},
            }
        )
    return states


# ── compare ────────────────────────────────────────────────────────────────────


def unchanged_artifacts(previous: dict | None, current_fingerprint: dict) -> bool:
    """Whether this build ships the same Remote bytes the last probe already tested."""
    if not previous or not current_fingerprint:
        return False
    # `_`-prefixed keys are not artifacts. `fingerprint_of` stows the API watchlist under
    # `_awaitedApi` because it reads it out of an AAR it was already downloading, and this gate must
    # stay a statement about BYTES: a watchlist that gained an entry is a change to this repo's
    # source, not to the build being probed, and letting it force a render would spend a runner to
    # learn nothing. A build whose classes really did move has different bytes and is caught below.
    def artifacts(d):
        return {k: v for k, v in d.items() if not k.startswith("_")}

    before = artifacts((previous.get("build") or {}).get("fingerprint") or {})
    current = artifacts(current_fingerprint)
    if set(before) != set(current):
        return False
    return all(before[ref]["sha256"] == current[ref]["sha256"] for ref in before)


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

    # A DENSITY SWEEP THAT MOVED is the finding #89 and #90 asked this job to be able to make.
    #
    # Both are conditional on render density, and both said the byte comparison above cannot see it:
    # it re-measures the one density the committed sheet declares, which is the density the bug
    # happens to sit at. The sweep measures the drawn box in dp across the range instead, so
    # "invariant" flipping either way is reportable — a component that starts varying has regressed,
    # and one that stops varying is fixed, which is as close to that word as this job ever gets.
    swept = []
    for probe in current.get("probes", []):
        before = prev_probes.get(probe["issue"], {})
        if probe.get("densitySweep") and before.get("densitySweep") != probe["densitySweep"]:
            swept.append(probe)
    if swept:
        reasons.append(
            "a tracked issue's density sweep moved: "
            + ", ".join(
                f"#{p['issue']} (invariant: {p.get('densityInvariant')})" for p in swept
            )
        )

    # A PROBE THAT RESOLVES TO NO RENDER IS BLIND, AND SAYING SO IS THE POINT OF THIS JOB.
    #
    # It used to be silent: `identicalToKnownBroken` is None when nothing matched, which the table
    # printed as "not rendered" and no reason picked up — so the row read like a fact about the
    # snapshot rather than a fault in the probe. #116 folded `DisabledRemoteButton` and
    # `CompactIconOnlyRemoteButton` into cells of other previews, #90 and #91 kept naming the old
    # stems, and both watched nothing at all while the report still looked complete. A probe is
    # only worth having if it is loud when it stops working, so a missing render is now a reason.
    #
    # Guarded on `compiled`, because a build that failed to compile rendered nothing by definition
    # and the compile reason above already says so — the same carve-out the removed-captures block
    # below makes, and for the same reason.
    blind = [
        probe
        for probe in current.get("probes", [])
        if not probe.get("rendered") and current.get("build", {}).get("compiled") is not False
    ]
    if blind:
        reasons.append(
            "a tracked issue's preview no longer renders, so the probe is watching nothing: "
            + ", ".join(f"#{p['issue']} (`{p['preview']}`)" for p in blind)
        )

    # A WATCHED SYMBOL THAT ARRIVED. Reported whether or not the build compiled and whether or not
    # anything rendered: it is a fact about the ARTIFACT, read out of the AAR before a single
    # sticker is drawn, and it is the one thing this job can say that is unambiguously good news.
    # Absent -> present only; a symbol that goes away again is a reason too, because a watch whose
    # answer reversed is exactly as interesting.
    prev_api = (previous or {}).get("build", {}).get("awaitedApi") or {}
    cur_api = current.get("build", {}).get("awaitedApi") or {}
    arrived = sorted(s for s, here in cur_api.items() if here and not prev_api.get(s))
    withdrawn = sorted(s for s, here in cur_api.items() if not here and prev_api.get(s))
    if arrived:
        reasons.append(
            "a component this catalog is waiting for is now PUBLISHED: "
            + ", ".join(f"`{s.rsplit('/', 1)[1]}`" for s in arrived)
        )
    if withdrawn:
        reasons.append(
            "a component that had appeared is gone again: "
            + ", ".join(f"`{s.rsplit('/', 1)[1]}`" for s in withdrawn)
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
        "arrivedApi": arrived,
        "withdrawnApi": withdrawn,
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


def _render_sweep(probe: dict) -> str:
    """One probe's density sweep as a markdown fragment, with the verdict in front of it."""
    sweep = probe.get("densitySweep") or {}
    if not sweep:
        return "—"
    invariant = probe.get("densityInvariant")
    if invariant is None:
        # Two different Nones, and calling both "one density" would misread the interesting one:
        # #130 is measured at four densities and draws nothing at any of them.
        drawn = [e for e in sweep.values() if e["box"] is not None]
        head = "one density only" if len(sweep) < 2 else ("nothing drawn at any density" if not drawn else "one density drew")
    else:
        head = "invariant ✓" if invariant else "**varies with density**"
    body = ", ".join(
        f"{d}: " + ("nothing drawn" if e["box"] is None else f"{e['box'][0]}x{e['box'][1]}dp {e['ink']:g}dp²")
        for d, e in sorted(sweep.items())
    )
    return f"{head}<br>`{body}`"


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

    lines += ["| issue | preview | still byte-identical to the known-broken capture | drawn, by density | measured |",
              "| --- | --- | --- | --- | --- |"]
    for probe in current.get("probes", []):
        identical = probe["identicalToKnownBroken"]
        mark = {True: "yes — still broken", False: "**no — look**", None: "not rendered"}[identical]
        measured = "<br>".join(_render_metric(k, v) for k, v in probe["metrics"].items()) or "—"
        lines.append(
            f"| #{probe['issue']} | `{probe['preview']}` | {mark} | {_render_sweep(probe)} | {measured} |"
        )
    lines.append("")
    lines += [
        "A component that lays out correctly occupies the **same dp box at every density**; one "
        "whose paint is density-conditional does not. That is the shared signature of #89 and #90, "
        "and it is why this table sweeps rather than reporting the one density the sheet declares.",
        "",
    ]

    awaited = current.get("build", {}).get("awaitedApi") or {}
    if awaited:
        lines += ["**Components this catalog is waiting for**", "",
                  "| symbol | published in this build | what it unlocks |",
                  "| --- | --- | --- |"]
        by_symbol = {e["symbol"]: e for e in AWAITED_API}
        for symbol, here in sorted(awaited.items()):
            entry = by_symbol.get(symbol, {})
            name = symbol.rsplit("/", 1)[1]
            link = entry.get("change")
            cell = f"[`{name}`]({link})" if link else f"`{name}`"
            lines.append(
                f"| {cell} | {'**YES — draw it**' if here else 'not yet'} | {entry.get('unlocks', '—')} |"
            )
        lines += ["", "_The symbol is the signal; the link is for reading, not polling — a merged "
                  "change is not a published artifact._", ""]

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
            "awaitedApi": awaited_api_of(resolved.get("fingerprint") or {}),
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
