#!/usr/bin/env python3
"""Vendor the pinned AndroidX Wear Compose sources into `upstream/`.

Downloads the `-sources.jar` of each artifact named in `upstream.json` from Google Maven,
verifies it against the `.sha1` published beside it, and unpacks the Kotlin source — and only
the Kotlin source — into `upstream/<artifact>/`.

`upstream/` is committed verbatim and never edited by hand. It is the base every patch in
`patches/` applies against, and `git diff` over it after a version bump is the review of what
AndroidX actually changed.
"""

from __future__ import annotations

import argparse
import hashlib
import io
import json
import pathlib
import shutil
import sys
import urllib.request
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent

# What a sources jar carries besides Kotlin. None of it survives the copy: R.java is generated
# from Android resources that do not exist off-Android, and the rest is packaging.
DROP_SUFFIXES = (".java", ".md")
DROP_DIRS = ("META-INF/",)


def fetch(url: str) -> bytes:
    with urllib.request.urlopen(url, timeout=120) as response:
        return response.read()


def sync_artifact(config: dict, entry: dict, dest_root: pathlib.Path) -> dict:
    version = entry.get("version", config["version"])
    artifact = entry["artifact"]
    base = f"{config['repository']}/{config['group'].replace('.', '/')}/{artifact}/{version}"
    jar_url = f"{base}/{artifact}-{version}-sources.jar"

    print(f"  fetching {jar_url}")
    payload = fetch(jar_url)
    digest = hashlib.sha1(payload).hexdigest()

    # Google Maven publishes a .sha1 beside every artifact. Verify it rather than trusting the
    # transfer: the sources are checked into this repository and compiled into a published
    # library, so a truncated download must fail loudly here and not surface as a Kotlin error.
    expected = fetch(f"{jar_url}.sha1").decode().split()[0].strip()
    if digest != expected:
        raise SystemExit(f"sha1 mismatch for {jar_url}: got {digest}, expected {expected}")

    dest = dest_root / artifact
    if dest.exists():
        shutil.rmtree(dest)

    kept = 0
    with zipfile.ZipFile(io.BytesIO(payload)) as jar:
        for name in jar.namelist():
            if name.endswith("/") or name.startswith(DROP_DIRS) or name.endswith(DROP_SUFFIXES):
                continue
            if not name.endswith(".kt"):
                print(f"    skipping unexpected entry {name}")
                continue
            target = dest / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(jar.read(name))
            kept += 1

    resources = 0
    if entry.get("resources"):
        # The sources jar has no `res/`, and the accessibility strings the components read are
        # real content: every content description on a slider, a switch and a dialog. They live in
        # the AAR, as the merged `res/values/values.xml` aapt produces, so the port takes them from
        # there rather than re-typing them into Kotlin where they would drift.
        aar_url = f"{base}/{artifact}-{version}.aar"
        print(f"  fetching {aar_url}")
        with zipfile.ZipFile(io.BytesIO(fetch(aar_url))) as aar:
            values = aar.read("res/values/values.xml")
        (dest / "resources.xml").write_bytes(values)
        resources = values.decode().count("<string ") + values.decode().count("<plurals ")
        print(f"    {resources} string/plural resources -> upstream/{artifact}/resources.xml")

    print(f"    {kept} Kotlin files -> upstream/{artifact}")
    return {
        "artifact": artifact,
        "version": version,
        "url": jar_url,
        "sha1": digest,
        "files": kept,
        "resources": resources,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", default=str(ROOT / "upstream.json"))
    parser.add_argument("--dest", default=str(ROOT / "upstream"))
    args = parser.parse_args()

    config = json.loads(pathlib.Path(args.config).read_text())
    dest_root = pathlib.Path(args.dest)
    dest_root.mkdir(parents=True, exist_ok=True)

    print(f"syncing {config['group']} {config['version']}")
    manifest = {
        "group": config["group"],
        "version": config["version"],
        "artifacts": [sync_artifact(config, entry, dest_root) for entry in config["artifacts"]],
    }
    (dest_root / "MANIFEST.json").write_text(json.dumps(manifest, indent=2) + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
