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

A few are a render with an **overlay drawn on top** — the boxes the preview server's inspection
layers put over the same frame, baked in so a PR body can show what a reader saw on the page. They
are named for the question they answer rather than for the preview
(`alert-dialog-confirm-a11y-stops.png`: the two nested accessibility stops on the alert dialog's
confirm button, [#76](https://github.com/yschimke/wear-m3-catalog/issues/76)). The frame underneath
is still an ordinary `composePreviewRender` output, so it moves with the component like the rest.
