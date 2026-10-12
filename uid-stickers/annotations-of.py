#!/usr/bin/env python3
"""Print the annotation block above a hand-written sticker, verbatim, for its generated shell.

`python3 uid-stickers/annotations-of.py FilledCard` → the `@CatalogComponent(…)` … `@Composable`
lines above `fun FilledCard(` in `:catalog`, plus the file's `@file:CatalogGroup`. A shell that
carries exactly these renders exactly the hand-written sticker's captures.
"""
import glob, os, re, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../catalog/src")
name = sys.argv[1]
for path in glob.glob(os.path.join(ROOT, "**/*.kt"), recursive=True):
    lines = open(path).read().split("\n")
    for i, line in enumerate(lines):
        if re.match(rf"(internal |private )?fun {re.escape(name)}\(", line):
            j = i
            while j > 0 and (lines[j - 1].startswith("@") or lines[j - 1].startswith(" ") or lines[j - 1].startswith(")")
                             or lines[j - 1].startswith("//")):
                j -= 1
            group = next((l for l in lines if l.startswith("@file:CatalogGroup")), "")
            print(f"// {os.path.relpath(path, ROOT)}:{i + 1}\n{group}\n" + "\n".join(lines[j:i + 1]))
            sys.exit(0)
sys.exit(f"no fun {name}( in :catalog")
