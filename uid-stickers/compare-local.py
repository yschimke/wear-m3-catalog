#!/usr/bin/env python3
"""The cutover check: every capture of a hand-written sticker against its generated replacement.

`python3 uid-stickers/compare-local.py` after `-PuidStickers=true :catalog:composePreviewRender`,
which renders the shells beside the hand-written stickers in one Robolectric pass. Each
`stickers/<name>.json` names its `pairs`: a shell preview function (package
`ee.schimke.wearm3catalog.uid`) and the hand-written function it shadows (package
`ee.schimke.wearm3catalog.sections`). A shell takes the hand-written function's own name, so the
cutover is then a body swap. The shell carries the
hand-written sticker's annotations verbatim, so both render the same set of captures (modes,
override variants, interaction states, exhaustive kit cells). Every hand-written capture must have
a counterpart here, and every pair must be pixel-identical. Anything else fails.

`--only <shell>[,<shell>…]` checks just those shells. `--report <file.json>` also writes per-sticker counts for the cutover table.
"""
import glob, json, os, re, sys
from PIL import Image, ImageChops

HERE = os.path.dirname(os.path.abspath(__file__))
# One render holds both sides; they are told apart by package. `CATALOG_RENDERS` points at another
# checkout's renders.
THEIRS = os.environ.get("CATALOG_RENDERS") or os.path.join(HERE, "../catalog/build/compose-previews/renders")
OURS = THEIRS
SHELL_PACKAGE = "ee.schimke.wearm3catalog.uid."
HAND_PACKAGE = "ee.schimke.wearm3catalog.sections."


def captures(directory, function, package):
    """`{key: path}` for every capture of `function`, keyed by its preview id without the class and function.

    Capture file names are truncated, so two cells can share a name; the preview id cannot.
    `previews.json` sits beside the renders (`build/compose-previews/`, or in the snapshot dir).
    """
    manifest = next(p for p in (os.path.join(directory, "previews.json"), os.path.join(directory, "../previews.json"))
                    if os.path.isfile(p))
    found = {}
    for preview in json.load(open(manifest))["previews"]:
        if preview["functionName"] != function or not preview["className"].startswith(package):
            continue
        key = preview["id"][len(preview["className"]) + 1 + len(function):]
        for i, capture in enumerate(preview["captures"]):
            out = capture.get("renderOutput")
            if out:
                found[f"{key}#{i}"] = os.path.join(directory, os.path.basename(out))
    return found


def frames(path):
    """Every frame (an animated capture, `.apng`, has many) with its duration."""
    image = Image.open(path)
    out = []
    for i in range(getattr(image, "n_frames", 1)):
        image.seek(i)
        out.append((image.convert("RGBA"), image.info.get("duration")))
    return out


def same(a, b):
    if not os.path.isfile(b):
        return "no reference capture"
    xs, ys = frames(a), frames(b)
    if len(xs) != len(ys):
        return f"{len(xs)} frames vs {len(ys)}"
    for i, ((x, dx), (y, dy)) in enumerate(zip(xs, ys)):
        if x.size != y.size:
            return f"frame {i}: size {x.size} vs {y.size}"
        if dx != dy:
            return f"frame {i}: duration {dx} vs {dy}"
        box = ImageChops.difference(x, y).getbbox(alpha_only=False)  # the default only reads alpha
        if box is not None:
            return f"frame {i} differs within {box}"
    return None


ONLY = set(sys.argv[sys.argv.index("--only") + 1].split(",")) if "--only" in sys.argv else None
report, failed = {}, 0
for side in sorted(glob.glob(os.path.join(HERE, "stickers/*.json"))):
    for shell, hand in json.load(open(side)).get("pairs", {}).items():
        if ONLY and shell not in ONLY:
            continue
        # `self:` names a reference rendered in this module, e.g. the hand-written sticker in a locale
        # `:catalog` does not capture.
        theirs = captures(OURS, hand[5:], SHELL_PACKAGE) if hand.startswith("self:") else captures(THEIRS, hand, HAND_PACKAGE)
        ours = captures(OURS, shell, SHELL_PACKAGE)
        if not theirs:
            print(f"FAIL  {shell}: no captures of {hand} in :catalog renders"); failed += 1; continue
        missing = sorted(set(theirs) - set(ours))
        extra = sorted(set(ours) - set(theirs))
        diffs = {s: d for s in sorted(set(theirs) & set(ours)) if (d := same(ours[s], theirs[s]))}
        ok = not missing and not extra and not diffs
        failed += 0 if ok else 1
        report[shell] = {"replaces": hand, "captures": len(theirs), "identical": len(theirs) - len(missing) - len(diffs),
                         "missing": missing, "extra": extra, "different": diffs}
        print(f"{'SAME' if ok else 'FAIL'}  {shell} == {hand}: {len(theirs) - len(missing) - len(diffs)}/{len(theirs)} captures"
              + (f"; missing {missing[:5]}" if missing else "") + (f"; extra {extra[:5]}" if extra else "")
              + (f"; different {list(diffs.items())[:3]}" if diffs else ""))
if "--report" in sys.argv:
    json.dump(report, open(sys.argv[sys.argv.index("--report") + 1], "w"), indent=2)
print(f"{len(report) - failed}/{len(report)} stickers ready to cut over")
sys.exit(1 if failed else 0)
