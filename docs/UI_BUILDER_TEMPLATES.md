# The UI builder's template designs, and why six of them belong here

The UI builder offers a starting point when somebody makes a new design — a blank Wear screen, a
worked Wear list, two widget host frames, two worked widget samples. All six are **Kotlin document
builders in the preview server today**, under `ui-builder-export/…/UiBuilderTemplates.kt`, drawing
components only this repository publishes. `wear-list` is this catalog's own activity list, row for
row, transcribed there because there was nowhere else to put it.

The plan to move them is
[`UI_BUILDER_SEED_TEMPLATES.md`](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_SEED_TEMPLATES.md)
in that repository, under the
[catalog contract](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_CATALOG_CONTRACT.md)'s
phase 3a and phase 2 item 11. **The four `:remote-catalog` templates have arrived** — as documents
under `remote-catalog/ui-builder/designs/`, declared in that module's `ui-builder.policy.json` and
gated by `WidgetTemplateRoundTripTest`. The two `:catalog` templates are still due; this note
records them and the test that makes carrying any of it worth anything.

## What arrives, and in which module

| Module | Catalog | Template | What it draws |
| --- | --- | --- | --- |
| `:catalog` | `wear-m3` | `wear-screen` | `ScreenScaffold` with its clock and scroll indicator over an empty `TransformingLazyColumn` |
| `:catalog` | `wear-m3` | `wear-list` | the same shape holding six title cards under a list header — this catalog's activity list |
| `:remote-catalog` | `remote-m3` | `wear-widget-small` | the 216×76dp host frame, one empty content slot |
| `:remote-catalog` | `remote-m3` | `wear-widget-large` | the 216×124dp host frame, one empty content slot |
| `:remote-catalog` | `remote-m3` | `hello-widget` | centred text on the theme's primary, in the small host |
| `:remote-catalog` | `remote-m3` | `weather-widget` | location over a large reading on the sample's sunny blue, in the large host |

Each arrives as a design document under `ui-builder/designs/<template>.json`, named from the
module's `ui-builder.policy.json` in a `templates` entry carrying its id, path, chooser label and
supporting text. The pipeline copies `ui-builder/designs/` to the delivery branch beside
`ui-builder.json`, the same way it copies the record. (The schema takes paths only today; the
label / supporting-text / order / default fields are
[SEED_TEMPLATES step 3](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_SEED_TEMPLATES.md)
in compose-ai-tools, and the policy's `$comment_templates` records them until then.)

The two host frames are worth one line of their own: their dimensions are **already** authored here,
in `remote-catalog/ui-builder.policy.json`'s `frame.geometry.sizesDp`, from
`WidgetContainerPreviews.kt`'s pinned `@Preview(widthDp = 216, heightDp = 76)` and `(216, 124)`. The
document that starts a design in one of those frames being authored somewhere else is the split this
move closes.

One deviation from the server's Kotlin seeds, found by the round trip the seeds could never run: the
empty host frames carry a `layout/box` with `fillMaxSize` in their content slot rather than nothing,
because `WearWidgetCodeExporter` writes a literal `RemoteBox(modifier = RemoteModifier.fillMaxSize())`
for an empty content slot **without the imports for any of the three** — source that generates and
does not compile
([compose-ui-builder#26](https://github.com/yschimke/compose-ui-builder/issues/26)). The box is the
same starter `wearWidgetSampleDocument` gives the worked samples, and the compile gate holds it
there; drop it from both host documents when that lands.

## The test is the deliverable, not the copy

A template is the one document nobody authored, so nothing catches a property its catalog does not
declare except a check that runs. The preview server measured all six before proposing the move: all
six validate against their catalog and all six generate Kotlin. **What it could not do is compile
that Kotlin** — the generated `hello-widget` names `androidx.glance.wear.GlanceWearWidget`,
`WearWidgetDocument`, `androidx.wear.compose.remote.material3.RemoteText` and three
`WearWidgetPreview` parameter sets, and `wear-screen` names `AppScaffold`, `ScreenScaffold` and
`rememberTransformationSpec`. Nothing in that repository has any of it on a classpath. Both modules
here do.

So the round trip is what this repository owes, per module: every template document →
`ui-builder.json` → generated Kotlin → **compiles against that module's own classpath**, and for
`:remote-catalog` rasterises on Robolectric through the real player, the same lane that produces
every sticker in it. It consumes the published `ui-builder-export` and `screen-model` coordinates,
which the layer rule allows — a leaf depends down.

## Two of them are somebody else's sample, and the vendoring rule does not apply

`hello-widget` and `weather-widget` reproduce the widgets in [android/wear-os-samples'
`WearWidget` sample](https://github.com/android/wear-os-samples/pull/1386). [`AGENTS.md`](../AGENTS.md)
has a deliberate exception for vendored sources — `:samples-catalog` and `:glimmer-samples` hold
upstream's bytes under upstream's package and declare their inventory in `groups`, because an
annotation written into one would be destroyed by the next import.

**These are not that.** They are designs drawn from the sample's layout, colours, type sizes and
strings, authored by hand, re-expressible in any catalog — not upstream's bytes, not re-fetched, not
patched. They enter as ordinary authored documents under `ui-builder/designs/`, and no import script
will overwrite them. What they are not is a compile of the sample: the sample runs Remote Compose on
a watch and these reproduce its *design*, so the fidelity question belongs to the parity lanes rather
than to a template.
