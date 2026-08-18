# design-map.json, and why this repo has none yet

`design-map.json` is the correspondence file design-parity reads to know which kit node a rendered
component is meant to look like. It is not hand-written: `scripts/design-map.sh` projects it out of
the discovered preview manifest, joining each component's
`@CatalogComponent(reference = "figma:<fileKey>/<nodeId>")` to one of its rendered captures.

**One of its captures — the light one.** `@yschimke/compose-design-map` pairs a component's
reference with the capture whose id ends `_Light`, on the reasoning that design kits draw their
frames in light mode and diffing a dark render against a light reference reports the whole palette
as a finding. Every filter in the projector is spelled that way:

```js
const LIGHT_CAPTURE = /_Light$/;
const LIGHT_VARIANT_CAPTURE = /_Light_VARIANT_/;
```

This catalog is **dark-only** — Wear is a black watch face, so `@CatalogModes` bakes a single dark
capture and no preview id ends in `_Light`. Running the projector today therefore reports
`0 mapped component(s)` even though every component carries a reference, and the empty map is worse
than no map: it reads as "nothing here maps to the kit" rather than "the projector could not see
these".

So this repo:

- keeps the reference on every annotation, and a test
  (`CatalogInventoryTest.every component maps to the Wear kit`) that fails the build for a component
  without one;
- keeps `scripts/design-map.sh`, which is otherwise correct and picks the kit-index step up as soon
  as `figma-kit-index.json` is committed;
- commits **no** `design-map.json` and runs **no** staleness check in CI, until the projector can
  name the capture it should pair with;
- runs `design-parity.yml` on **workflow_dispatch only**. An empty map does not skip the way a
  missing token does — design-parity exits 1 with `no components: pass --components, or commit a
  design-map.json with entries` — and a known red X on every push teaches everyone to ignore the
  signal. The workflow is otherwise complete; restoring its push and schedule triggers is the same
  commit that commits the first non-empty map.

The fix belongs upstream, not here — the assumption is about *design kits and render modes*, not
about this catalog, and a fork of the projector in this repo would drift from the one every other
catalog runs. That is the same rule `AGENTS.md` states for CI capabilities. Tracked as
[yschimke/compose-ai-tools#4192](https://github.com/yschimke/compose-ai-tools/issues/4192); the
shape of the fix is a fallback to the sole capture when a catalog publishes one mode, so a dark-first catalog pairs with
its dark capture instead of with nothing.
