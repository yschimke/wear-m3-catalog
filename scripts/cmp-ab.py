#!/usr/bin/env python3
"""Compare the two Wear lanes sticker by sticker.

`:catalog` renders on Robolectric against the real `androidx.wear.compose` AAR; `:catalog-cmp`
renders the same sources on the desktop renderer against the `ee.schimke.wearcmp` port. Every
sticker the two share should draw the same pixels, and where it does not, this says by how much.

    ./gradlew :catalog:composePreviewRenderAll :catalog-cmp:composePreviewRenderAll
    python3 scripts/cmp-ab.py

`:catalog` is the ORACLE — a difference is a finding about the port, or about this repository's
wiring of it, never a reason to change a sticker. docs/CMP_PORT.md has the standing causes; check a
new difference against those before reporting it as a port defect.

Requires Pillow (`pip install pillow`).
"""

import argparse
import json
import os
import sys

try:
    from PIL import Image, ImageChops
except ImportError:  # pragma: no cover - a missing optional dep, not a code path
    sys.exit("Pillow is required: pip install pillow")

ANDROID = "catalog/build/compose-previews/renders"
CMP = "catalog-cmp/build/compose-previews/renders"

# Below this share of pixels differing, a sticker is "clean": the renderers disagree only where
# antialiasing does. Compared at a channel tolerance so a half-covered edge pixel is not a finding.
CLEAN_PCT = 0.01
CHANNEL_TOLERANCE = 16


def compare(a_dir: str, c_dir: str):
    names = sorted(
        n for n in set(os.listdir(a_dir)) & set(os.listdir(c_dir)) if n.endswith(".png")
    )
    for name in names:
        a = Image.open(os.path.join(a_dir, name)).convert("RGB")
        c = Image.open(os.path.join(c_dir, name)).convert("RGB")
        if a.size != c.size:
            # Almost always the density gap rather than a drawing difference — see
            # docs/CMP_PORT.md, "The device-less previews are not comparable yet".
            yield {"name": name, "status": "size", "android": a.size, "cmp": c.size}
            continue
        grey = ImageChops.difference(a, c).convert("L")
        histogram = grey.histogram()
        total = a.size[0] * a.size[1]
        over = sum(histogram[CHANNEL_TOLERANCE:]) / total * 100
        yield {
            "name": name,
            "status": "clean" if over < CLEAN_PCT else "differs",
            "pct": over,
            "any_pct": (total - histogram[0]) / total * 100,
        }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--android", default=ANDROID)
    parser.add_argument("--cmp", default=CMP)
    parser.add_argument("--json", help="write the full per-sticker table here")
    parser.add_argument(
        "--fail-over",
        type=float,
        help="exit 1 if any comparable sticker differs by more than this percent",
    )
    args = parser.parse_args()

    for path in (args.android, args.cmp):
        if not os.path.isdir(path):
            sys.exit(f"{path} does not exist — render both lanes first (see the module docstring)")

    rows = list(compare(args.android, args.cmp))
    if args.json:
        with open(args.json, "w") as handle:
            json.dump(rows, handle, indent=2)

    sized = [r for r in rows if r["status"] == "size"]
    clean = [r for r in rows if r["status"] == "clean"]
    differs = sorted(
        (r for r in rows if r["status"] == "differs"), key=lambda r: -r["pct"]
    )
    print(f"compared {len(rows)} shared stickers")
    print(f"  not comparable (frame size differs): {len(sized)}")
    print(f"  clean (< {CLEAN_PCT}% of pixels past a {CHANNEL_TOLERANCE}/255 tolerance): {len(clean)}")
    print(f"  differ: {len(differs)}")
    for row in differs[:20]:
        print(f"     {row['pct']:6.2f}%  {row['name']}")
    if len(differs) > 20:
        print(f"     … and {len(differs) - 20} more")

    if args.fail_over is not None and differs and differs[0]["pct"] > args.fail_over:
        print(f"\nFAIL: {differs[0]['name']} differs by {differs[0]['pct']:.2f}%")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
