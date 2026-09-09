#!/usr/bin/env python3
"""Ask Google Maven whether a newer AndroidX Wear Compose release exists.

The release feed is `group-index.xml`, published beside the artifacts themselves: one element per
artifact carrying every version ever released. A version counts only if EVERY artifact in
`upstream.json` has it — the Wear Compose artifacts move as a train, and a port pinned to a version
one of them has not published yet cannot resolve.

Prints the newest such version, or nothing at all when the pin is already current. With `--update`
it writes the pin back to `upstream.json` and resets `portRevision`, which is the whole of what the
sync workflow has to do before running `scripts/sync-upstream.sh`.
"""

from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys
import urllib.request
import xml.etree.ElementTree

ROOT = pathlib.Path(__file__).resolve().parent.parent

# alpha < beta < rc < release. Anything unrecognised sorts below alpha rather than above the
# release: an unknown suffix is not something to upgrade onto unasked.
STAGES = {"alpha": 1, "beta": 2, "rc": 3}


def version_key(version: str) -> tuple:
    match = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)(?:-([a-z]+)(\d+))?", version)
    if not match:
        return (0, 0, 0, 0, 0)
    major, minor, patch, stage, iteration = match.groups()
    return (
        int(major),
        int(minor),
        int(patch),
        4 if stage is None else STAGES.get(stage, 0),
        int(iteration or 0),
    )


def is_prerelease(version: str) -> bool:
    return version_key(version)[3] != 4


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--stable-only",
        action="store_true",
        help="ignore alpha/beta/rc releases. Off by default, because the pin is itself a beta and "
        "the Wear Compose line spends most of its time in prerelease.",
    )
    parser.add_argument("--update", action="store_true", help="write the new pin to upstream.json")
    args = parser.parse_args()

    config = json.loads((ROOT / "upstream.json").read_text())
    group_path = config["group"].replace(".", "/")
    index_url = f"{config['repository']}/{group_path}/group-index.xml"

    with urllib.request.urlopen(index_url, timeout=60) as response:
        index = xml.etree.ElementTree.fromstring(response.read())

    wanted = {entry["artifact"] for entry in config["artifacts"]}
    published = {
        node.tag: set(node.get("versions", "").split(","))
        for node in index
        if node.tag in wanted
    }
    missing = wanted - published.keys()
    if missing:
        raise SystemExit(f"not in {index_url}: {', '.join(sorted(missing))}")

    common = set.intersection(*published.values())
    if args.stable_only:
        common = {version for version in common if not is_prerelease(version)}

    latest = max(common, key=version_key)
    current = config["version"]
    if version_key(latest) <= version_key(current):
        print(f"up to date: {current}", file=sys.stderr)
        return 0

    print(latest)
    if args.update:
        text = (ROOT / "upstream.json").read_text()
        text = text.replace(f'"version": "{current}"', f'"version": "{latest}"', 1)
        text = re.sub(r'"portRevision": \d+', '"portRevision": 1', text)
        (ROOT / "upstream.json").write_text(text)
        print(f"upstream.json: {current} -> {latest}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
