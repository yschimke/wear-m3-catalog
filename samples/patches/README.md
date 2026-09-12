# Sample patches

Small fixes to the vendored AndroidX samples, applied by `scripts/import-samples.mjs` after the
copy and before the tree is committed.

A fix is **a patch with a stated reason, never an edit to a vendored file**. The vendored tree is
upstream's bytes; an untracked edit to it is silently reverted by the next import, and nobody finds
out until a render changes. A patch is re-applied every import, and one that stops applying **fails
the import** rather than disappearing — which is the point, because upstream moving under a fix is
exactly when someone needs to look at it again.

Patch paths are relative to the **vendored root**, so they carry the package directories the
samples declare — `androidx/wear/compose/material3/samples/Foo.kt`, not `Foo.kt`. `applyPatches`
hands the whole tree to `git apply --directory`, and a path missing the package fails the import
with "upstream has moved under this patch", which is the one thing it would not mean.

Cut a patch against the freshly imported tree:

```
node scripts/import-samples.mjs --out /tmp/samples-fresh
cp -r /tmp/samples-fresh /tmp/samples-orig
# edit /tmp/samples-fresh/androidx/wear/compose/material3/samples/Foo.kt
git -C /tmp diff --no-index --no-prefix samples-orig samples-fresh \
  | sed -E 's#samples-(orig|fresh)/##' > samples/patches/0001-foo.patch
```

The `sed` strips the two throwaway directory names so what remains is the package-relative path
`applyPatches` expects; check the `--- ` / `+++ ` lines before committing.

Open every patch with a comment saying **why** — which compile error or renderer limitation it
answers — because the next reader has to decide whether it still holds after an upstream bump.

A sample that cannot be fixed with a small patch does not get a large one: it goes in
`samples/quarantine.json` with its reason. See `docs/design/ANDROIDX_SAMPLES.md`.
