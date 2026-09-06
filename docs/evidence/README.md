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

`new-components.png` and `new-motion-frames.png` are the evidence for the six components added
for [#310](https://github.com/yschimke/wear-m3-catalog/issues/310)–[#315](https://github.com/yschimke/wear-m3-catalog/issues/315).
The first is a contact sheet in the `kit-*-cells*.png` sense — each new card's base render beside
the cells that say what its knobs do, plus the `Stepper` card now drawing the level rail its kit
node has. The second is a FILMSTRIP: six evenly spaced frames out of each of the four new
`Motion.kt` recordings, with the frame number under each. A recording is the deliverable for a
component whose subject is a transition, and a still of one says nothing about it — the frames are
what show the pager's page scaling under its scrim and the fading label's wipe mid-sweep. Frames are
`composePreviewRender` outputs composited onto the sheet's `#141418` for the reason the paragraph
below gives; no pixel of any render is otherwise touched.

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

`parity-*.png` are before/after BOARDS: one component's affected cells rendered from `main` on the
top band and from the change on the bottom, with the cell's name under each frame and the lane
(`:catalog` or `:remote-catalog`) in the title. They evidence the compare-page parity fixes of
[#292](https://github.com/yschimke/wear-m3-catalog/issues/292),
[#294](https://github.com/yschimke/wear-m3-catalog/issues/294) and
[#295](https://github.com/yschimke/wear-m3-catalog/issues/295), where the interesting thing is
almost always a *pair* — a type role, a glyph, a second label — rather than one frame, and reading
it means seeing the same cell twice. The frames are ordinary `composePreviewRender` outputs
composited onto the sheet's dark board for the reason `remote-m3-folded-cells.png` gives; no pixel
of a render is otherwise touched.

`parity-card-width.png` and `parity-titlecard3-timestamp.png` are `parity-*.png` boards in the sense
above, for the two fixture differences that survived
[#294](https://github.com/yschimke/wear-m3-catalog/issues/294): the card width, where the top band is
180dp and the bottom the kit's own 172, and `Title Card 3`'s timestamp, where the top band omits a
slot the kit cell fills. The second one is worth reading beside its `with-subtitle` neighbour, which
is included unchanged for exactly that: it is what says the timestamp moved rather than the card.

`remote-m3-snapshot-lane.png` is a two-lane contact sheet rather than a before/after of a change to
this repo's code: the same stickers rendered against the released alphas and against an androidx.dev
snapshot (`-PremoteSnapshot=<build id>`, see AGENTS.md → Dependencies), plus all eight cells of a
component that only exists on the snapshot lane. What it evidences is what the LIBRARY moved, which
is the only question that lane is asked — and its bottom two bands are the clearest form of this
repo's favourite kind of finding: `RemoteCheckboxButton` and `RemoteSplitCheckboxButton` given the
same two knobs, one drawing its disabled states and one not. Frames are composited onto the sheet's `#141418` for the reason the
paragraph above gives; no pixel of any render is otherwise touched.

`remote-m3-card-background-image.png` is the `Title Card` set's `Style=Background Image` column as
the snapshot lane draws it: the tonal base beside the three image-backed cells. It documents a
BREAK — the container renders near-black with no image in it, where the placeholder is `#ECECEC` —
and the `Title Card 2` cell additionally clips its subtitle, because the painter overload defaults
to a larger content padding than the plain card's. Both are the library's, both are published rather
than withheld, and the picture is what makes that legible without running the sheet.

`remote-m3-*-break.png` are the **known-broken baselines** `remote-snapshot-probe.py` compares each
tracked issue's weekly capture against; byte-identical means "still broken" with certainty. Refresh
one only when THIS repo moved the sticker and the symptom is verified unchanged — never to quiet a
probe that has started reporting, because a capture that stopped matching is the single most
interesting thing that job can say. `remote-m3-button-disabled-break.png` was refreshed when #116
folded `Button/Disabled` into `Button/Filled`'s `disabled` cell, which put the base sticker's
`buttonSizeModifier()` under it and changed the pill: the symptom is unchanged (max alpha 31, the
container, and no label anywhere), only this catalog's framing of it.

It was refreshed a SECOND time, for the same kind of reason and on the same evidence. #138 gave the
button `.width(KitRowWidth)` so it stops publishing 50dp narrower than the kit cell it is compared
against, and that widened this capture too: the drawn box goes 122.5x52.0dp -> 172.0x52.0dp, which
is that 172dp arriving. Everything that makes it a #91 capture is untouched — max alpha 31 on both,
the same 49 colours, the same dominant `(247, 239, 255, 31)` container at 12%, no pixel anywhere
above the container's alpha and so no label. This repo moved the sticker; the library did not move
the bug.

Worth stating because the stale capture was not harmless: `identicalToKnownBroken` had been False
for #91 ever since, so the probe could say neither "still broken" nor "somebody look". The
[find_render] density fix landed alongside #249 exposed that as the remaining cause once the 160dpi
mismatch was gone.

`remote-m3-edge-button-label-spill-break.png` is #249's baseline: `EdgeButtonRemote`'s base cell,
where the label is drawn 20.5dp outside the arc that is supposed to contain it. It is a SNAPSHOT
LANE capture and cannot be reproduced with an empty `-PremoteSnapshot=` — `RemoteEdgeButton` is
absent from released alpha10 — which is a first for this directory and is fine, because the probe
workflow always renders on its own snapshot overlay.

It is also the first baseline whose probe carries a metric that reads the other way round. `max
alpha 0` means "still broken" for #130; `edge_button_label_spill_dp` is an overhang, so **0 there
means fixed**. Read it accordingly before concluding anything from the number alone.

`remote-m3-disabled-resolve-triptych.png` is the evidence for the disabled-text-button report: the
same `enabled = false` state across the three button families that publish it, on one build and at
one density, with the measured max alpha under each. `RemoteIconButton` resolves it exactly as
specified (97 = 0.38), `RemoteButton` resolves the container and loses the label (31, and #91),
`RemoteTextButton` resolves neither (0). Three siblings on one build is what makes the report a
claim about the library rather than about this catalog.

`remote-m3-crossing-cells.png` is the #116 phase-3 companion to the sheet above: the crossings the
`secondary` tier made publishable, with each second-tier cell ringed amber so the picture shows
which rows left the navigation, and the one PUBLISHED LIBRARY GAP ringed red — `Button/Text`'s
`disabled` cell, which bakes fully transparent because `RemoteTextButton(enabled = false)` draws
nothing on the alpha line. That empty frame is the point of the file: it is what a reader meets on
the sheet, and `StickerBakeCoverageTest` fails the day it stops being empty. It is also the
before/after for two fixes that pass no other review — `Progress/Circular`'s disabled ring, which was byte-identical to its base until the knob
was wired, and `Button/Icon`'s three sizes, which had collapsed to two.

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
