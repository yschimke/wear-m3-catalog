# Visual evidence for pull requests

Renders committed here so a PR body can **embed** the pixels a change produces, per the visual
evidence rule in [AGENTS.md](../../AGENTS.md). A reviewer must see the actual before/after in the
description, and a container with no image host has nowhere else to put them.

Most are `./gradlew :<module>:composePreviewRender` outputs, copied out of
`<module>/build/compose-previews/renders/` with the content hash stripped from the name. They are
**evidence, not inventory**: the published sheet renders from the annotations on every run, the CI
visual-diff bot posts its own comparison, and nothing reads this directory. Replace a file when the
render it shows moves, and delete one whose component is gone.

## Kinds of file

**Plain renders.** A single `composePreviewRender` output, untouched.

**Contact sheets** (`kit-*-cells*.png`, `remote-m3-folded-cells.png`, `remote-m3-shapes*.png`,
`remote-m3-position-and-text-cells.png`, `remote-m3-crossing-cells.png`). One component's variant
cells composited onto a dark board with each cell's name under it. A change that adds cells by the
dozen cannot be evidenced one PNG at a time, and the reviewer's question — does every cell draw
something, and is it the cell it claims to be — is about the grid rather than any one frame.

**Before/after boards** (`parity-*.png`, `remote-m3-snapshot-lane.png`,
`remote-stepper-level-rail-{before,after}.png`). One component's affected cells rendered from `main`
on the top band and from the change on the bottom, with the lane in the title. The interesting thing
is almost always a *pair* — a type role, a glyph, a second label — so reading it means seeing the
same cell twice.

**Filmstrips** (`new-motion-frames.png`). Six evenly spaced frames out of a `Motion.kt` recording,
with the frame number under each. A recording is the deliverable for a component whose subject is a
transition, and a still of one says nothing about it.

**Overlay captures** (e.g. `alert-dialog-confirm-a11y-stops.png`). A render with the preview
server's inspection layers baked in, so a PR body can show what a reader saw on the page. Named for
the question they answer rather than for the preview.

**Server captures** (`front-door-hero-{before,after}.png`). The preview server's front-door card
fronted by this catalog's hero, captured from the server's committed page fixture. What they show is
a *choice* — which component this catalog puts on the index (`catalog.spec.json`'s `display.hero`) —
which is a picture of the server, not of a sticker, so no render task here produces it.

## Compositing

A `remote-m3` sticker rasterises onto transparency, and several are near-white or a flat
`primaryDim` silhouette — invisible on a light page, so a reviewer opening the PR sees nothing at
all. Those frames are composited onto the sheet's own `#141418` ground; **no pixel of a render is
otherwise touched.** Stickers that draw a coloured container read on any background and are left
raw.

## Known-broken baselines

`remote-m3-*-break.png` are what `remote-snapshot-probe.py` compares each tracked issue's weekly
capture against; byte-identical means "still broken" with certainty.

**Refresh one only when THIS repo moved the sticker and the symptom is verified unchanged** — never
to quiet a probe that has started reporting, because a capture that stopped matching is the single
most interesting thing that job can say. Record what was verified: for `#91`'s button baseline, that
is max alpha 31, the same container colour, and no pixel above the container's alpha (so no label)
across both captures — the framing moved, the bug did not.

**Check which way a probe's metric reads.** `max alpha 0` means "still broken" for #130, but
`edge_button_label_spill_dp` is an overhang, so **0 there means fixed**.

A snapshot-lane baseline (`remote-m3-edge-button-label-spill-break.png`) cannot be reproduced with
an empty `-PremoteSnapshot=`, because the component is absent from the released alphas. That is fine
— the probe workflow always renders on its own snapshot overlay.
