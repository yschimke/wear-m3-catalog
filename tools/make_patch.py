#!/usr/bin/env python3
"""Cut a patch from a generated file you have just edited by hand.

The workflow the pipeline is built around:

    scripts/regenerate.sh                       # start from pristine generated sources
    $EDITOR modules/<module>/src/commonMain/kotlin/<path>.kt
    tools/make_patch.py <module> <path>.kt      # record the edit as a patch
    scripts/regenerate.sh                       # prove it replays from scratch

`<path>` is relative to `src/commonMain/kotlin`, and the patch lands at
`patches/<module>/<path>.patch` — one patch per file, named after the file, so there is never a
question of which patch owns which source or what order they apply in.

The pristine side of the diff is recomputed here from `upstream/` with the same rules
`tools/transform.py` uses, so the diff contains your edit and nothing else, even if the file
already has a patch applied to it.
"""

from __future__ import annotations

import argparse
import difflib
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from transform import BANNER, ROOT, rewrite  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("module", help="e.g. wear-compose-material-core")
    parser.add_argument("path", help="path under src/commonMain/kotlin, e.g. androidx/.../X.kt")
    args = parser.parse_args()

    config = json.loads((ROOT / "upstream.json").read_text())
    rules = json.loads((ROOT / "transform-rules.json").read_text())
    entry = next(e for e in config["artifacts"] if e["module"] == args.module)

    upstream = ROOT / "upstream" / entry["artifact"] / args.path
    current = ROOT / "modules" / args.module / "src" / "commonMain" / "kotlin" / args.path
    if not upstream.is_file():
        raise SystemExit(f"no upstream source at {upstream}")
    if not current.is_file():
        raise SystemExit(f"no generated file at {current} — run scripts/regenerate.sh first")

    pristine = (BANNER + rewrite(upstream.read_text(), rules)).splitlines(keepends=True)
    edited = current.read_text().splitlines(keepends=True)

    rel = f"src/commonMain/kotlin/{args.path}"
    diff = list(difflib.unified_diff(pristine, edited, fromfile=f"a/{rel}", tofile=f"b/{rel}"))
    patch_path = ROOT / "patches" / args.module / f"{args.path}.patch"

    if not diff:
        if patch_path.exists():
            patch_path.unlink()
            print(f"no edits left — removed {patch_path.relative_to(ROOT)}")
        else:
            print("no edits to record")
        return 0

    patch_path.parent.mkdir(parents=True, exist_ok=True)
    patch_path.write_text("".join(diff))
    print(f"wrote {patch_path.relative_to(ROOT)} ({sum(1 for l in diff if l[0] in '+-')} lines)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
