# The UI builder's template designs, and why two of them belong here

The UI builder offers a starting point when somebody makes a new design — a blank Wear screen, a
worked Wear list, two widget host frames, two worked widget samples. All six were **Kotlin document
builders in the preview server**, under `ui-builder-export/…/UiBuilderTemplates.kt`, drawing
components only this repository (and, since the split, its Remote sibling) publishes. `wear-list` is
this catalog's own activity list, row for row, transcribed there because there was nowhere else to
put it.

The plan to move them is
[`UI_BUILDER_SEED_TEMPLATES.md`](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_SEED_TEMPLATES.md)
in that repository, under the
[catalog contract](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_CATALOG_CONTRACT.md)'s
phase 3a and phase 2 item 11. **All six have arrived**, each as a design document the owning module
declares in its `ui-builder.policy.json` and gates with a round-trip test. The two `:catalog` ones are
here, under `ui-builder/designs/` (`WearScreenTemplateRoundTripTest`). The four `remote-m3` ones — the
two widget host frames and the two worked widget samples, gated by `WidgetTemplateRoundTripTest` —
moved with the Remote sheet to
[yschimke/remote-m3-catalog](https://github.com/yschimke/remote-m3-catalog), which documents them.

## What arrived here

| Module | Catalog | Template | What it draws |
| --- | --- | --- | --- |
| `:catalog` | `wear-m3` | `wear-screen` | `AppScaffold` with a frozen `10:10` `TimeText` over an empty `TransformingLazyColumn` |
| `:catalog` | `wear-m3` | `wear-list` | the same shape holding six title cards under a list header — this catalog's activity list |

Each is a design document under `ui-builder/designs/<template>.json` at the repository root, where
`:catalog`'s policy is, named from the root `ui-builder.policy.json` in a `templates` entry. The
pipeline copies `ui-builder/designs/` to the delivery branch beside `ui-builder.json`, the same way it
copies the record. (The schema takes paths only today; the label / supporting-text / order / default
fields are
[SEED_TEMPLATES step 3](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_SEED_TEMPLATES.md)
in compose-ai-tools, and the policy's `$comment_templates` records them until then.)

## The test is the deliverable, not the copy

A template is the one document nobody authored, so nothing catches a property its catalog does not
declare except a check that runs. The preview server measured all six before proposing the move: all
six validate against their catalog and all six generate Kotlin. **What it could not do is compile
that Kotlin** — the generated `wear-screen` names `AppScaffold`, `ScreenScaffold` and
`rememberTransformationSpec`, and nothing in that repository has any of it on a classpath. `:catalog`
does.

So the round trip is what this repository owes: every template document → `ui-builder.json` →
generated Kotlin → **compiles against the module's own classpath**. It consumes the published
`ui-builder-export` and `screen-model` coordinates, which the layer rule allows — a leaf depends down.

`WearScreenTemplateRoundTripTest` reads each document from `ui-builder/designs/`, generates it and
compiles it in the unit-test source set against Wear Compose Material 3 — `AppScaffold`, `TimeText`
with its frozen `10:10`, `ScreenScaffold` and the `TransformingLazyColumn` the template opens on. The
environment claims the two round sizes the seed names, `wearos_small_round` (192dp) and
`wearos_xl_round` (240dp), and the generated `@WearPreviewDevices` preview fans out over every round
size. **Regenerating a golden is deliberate, never hand-edit:** run the module's test with
`-PwriteGolden=true` and read the diff; a green compile on the new text is the review.

The generated Kotlin is **compiled, not rasterised, in this repository's tests**. The render lane runs
`composePreviewRender` over the main source set's `@Preview`s, and a generated file placed there would
join the component record and need an exclusion per preview symbol. Pixels are the native render
lane's answer — `compose-preview-server design render` against the same documents — which is also the
lane the deployment uses.
