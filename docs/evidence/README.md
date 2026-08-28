# Visual evidence for pull requests

Renders committed here so a PR body can **embed** the pixels a change produces, per the visual
evidence rule in [AGENTS.md](../../AGENTS.md). A reviewer must see the actual before/after in the
description, and a container with no image host has nowhere else to put them.

These are `./gradlew :catalog:composePreviewRender` outputs, copied out of
`catalog/build/compose-previews/renders/` with the content hash stripped from the name. They are
**evidence, not inventory**: the published sheet renders from the annotations on every run, the CI
visual-diff bot posts its own comparison, and nothing reads this directory. Replace a file when the
render it shows moves, and delete one whose component is gone.

`front-door-hero-{before,after}.png` are the exception to the "these are `composePreviewRender`
outputs" line above: they are the *preview server's front-door card* fronted by this catalog's
hero, captured from the server's committed page fixture with the catalog's own published renders
served into it. What they show is not a component but a **choice** — which component this catalog
puts on the index (`catalog.spec.json`'s `display.hero`) — and that is a picture of the server, not
of a sticker, so no render task here produces it.

`kit-*-cells*.png` are the other exception: a CONTACT SHEET of one component's variant cells,
every `_VARIANT_` render of it composited onto one dark board with the cell's name under each. A
change that adds cells by the dozen (the kit's `Edge-Button` set alone is 64) cannot be evidenced
one PNG at a time, and the question a reviewer has — does every cell draw something, and is it the
cell it claims to be — is a question about the grid rather than about any one frame. The frames
inside are ordinary `composePreviewRender` outputs; only the board is assembled.

`remote-m3-folded-cells.png` is a contact sheet in the `kit-*-cells*.png` sense above, for the
Remote sheet rather than the Wear one: every family #116 folded, its base render beside each
`@OverrideVariant` cell it gained, with the cell's name under each frame. The question it answers
is the same grid-shaped one — does every cell draw something, and is it different from its
neighbour — and it is the picture that caught the extra-small icon-button cell being byte-identical
to the small one. Unlike the Wear contact sheets, every frame in it is composited onto the sheet's
`#141418` for the reason the next paragraph gives: the `remote-m3` stickers rasterise onto
transparency and half of these are near-white on it. No pixel of any render is otherwise touched.

`remote-m3-text-body-*.png` are the one set here composited onto the catalog's own stage rather
than left transparent. Every other `remote-m3-*` file is the raw render, which is correct: those
stickers draw a coloured container, so they read on any background. These three are near-white body
copy on transparency — the `remote-m3` sheet declares `display.surface: "dark"` and the SERVER
supplies the ground, so the bare PNG is invisible against a light page and a reviewer opening the
PR sees nothing at all. The composite is the sheet's own `#141418` behind an unmodified render; no
pixel of the sticker is touched.

A few are a render with an **overlay drawn on top** — the boxes the preview server's inspection
layers put over the same frame, baked in so a PR body can show what a reader saw on the page. They
are named for the question they answer rather than for the preview
(`alert-dialog-confirm-a11y-stops.png`: the two nested accessibility stops on the alert dialog's
confirm button, [#76](https://github.com/yschimke/wear-m3-catalog/issues/76)). The frame underneath
is still an ordinary `composePreviewRender` output, so it moves with the component like the rest.
