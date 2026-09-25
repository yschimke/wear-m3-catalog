# Remote M3 UI Builder rendering contract

`remote-m3` deliberately has three rendering surfaces. They share one semantic UI Builder document,
but they do not share a renderer and must not be collapsed into one.

| Surface | Purpose | Implementation | Fidelity |
| --- | --- | --- | --- |
| Visual Editor | Select, insert, move and inspect nodes | Wear Compose Material 3 CMP components used as catalog-declared stand-ins | Approximate |
| Browser Preview | Inspect the Remote Compose result in each selected widget host | CMP/Wasm Remote M3 records a `.rc` document; CMP/Wasm `RcComposePlayer` plays those bytes | Real Remote Compose browser preview |
| Native / Live | Make the release decision | AndroidX Remote M3 records on Android; the Android player renders it | Authoritative |

The Visual Editor is allowed to optimize for authoring. It must preserve authored Remote M3
properties, slots, node ids and state, and its `canvasMapping` may only translate them into the names
the Wear stand-in reads. It must never rewrite the stored document to Wear component ids.

The Browser Preview is not another set of lookalikes. The catalog runtime handles protocol-v2
`DEVICE` surfaces by walking the same semantic document with the CMP Remote creation APIs, recording
real Remote M3 components, decoding the resulting document, and passing it to `RcComposePlayer`.
Glance Wear host chrome remains outside the document, just as it does on Android, and is applied per
small/large widget frame around the player.

The catalog does **not** declare the legacy `browserPreview: remote-compose-document` capability.
That capability asks the host/server to compile an RC document and causes the editor to bypass the
catalog runtime's DEVICE surface. It remains useful to catalogs without an executable runtime, but it
is the wrong ownership boundary here.

## Compatibility and pinning

Writer and player move as an explicit compatibility pair:

- writer: the vendored Remote Compose port identified by `vendor/remote-compose-upstream.json`
  (`4307936-ps17-cmp01` at the time this contract was added);
- player: the `rc-players` release in `gradle/libs.versions.toml` (`1.69.0` at the same point).

Both values are pinned by the renderer's dependency graph and declared by the versioned runtime
contract. `remote-m3` emits `compose-ui-builder-runtime/v2`, which adds `remoteComposeWriter` and
`rcPlayer` to v1's five fields. The renderer ZIP verification checks the exact seven-field set and
both pinned values; neither schema accepts undeclared keys. The runtime is then published under an
immutable, exact runtime id. No part of recovery or preview may resolve `latest`, substitute a
merely compatible system, or follow a different catalog pin.

## Failure rules

- Unknown components and unsupported modifiers fail visibly in Browser Preview; they are not
  silently redrawn by a generic Compose component.
- Browser Preview is evidence that the CMP writer/player pair can represent the document, not a
  replacement for Native / Live.
- Native / Live remains authoritative for rectangular, Samsung squircle and Pixel Watch round hosts.
- A writer/player upgrade is reviewed as a rendering change and ships with the catalog runtime that
  names both exact versions.

This division keeps the editor responsive, makes browser feedback real, and preserves Android as the
final source of truth without introducing a second stored design model.

## Layout vocabulary: what a Wear widget can carry

`remote-creation-compose` publishes more layouts than a widget can use. A `remote-m3` design is a
Glance Wear widget, and `GlanceWearProfiles` (glance-wear 1.0.0-alpha18) fixes the operations a
widget document may contain. The palette offers a layout only when that profile admits it, because
an entry the watch cannot play is only found out at the end.

| Remote Compose | Builder | In the widget profile | Palette |
| --- | --- | --- | --- |
| `RemoteBox`, `RemoteRow`, `RemoteColumn` | `layout/box`, `layout/row`, `layout/column` | yes | offered |
| `RemoteFitBox` | `layout/fit-box` | `LAYOUT_FIT_BOX` | offered |
| `RemoteStateLayout` | "Show by state" on `layout/box` | `LAYOUT_STATE` | offered (Remote Compose authoring builds) |
| `Modifier.sharedElement` | `sharedElement` modifier | `ANIMATION_SPEC` | offered |
| `RemoteFlowRow` | `layout/flow-row` | no (`LAYOUT_FLOW` is experimental-only) | withheld |
| `RemoteCollapsibleColumn` / `Row`, `collapsiblePriority` | — | no | withheld |

Each surface handles a withheld layout the same way. The exporter refuses it by name, the Browser
Preview draws "Unsupported: <id>", and the Android writer throws "Operation … is not supported for
this version" while it captures the document. That last one is why the list is not a guess.

`sharedElement` exports as `animationSpec(key, true)`. That overload exists in both the released
alpha19 (the native lane) and the vendored port the Browser Preview records with, and it produces the
same `AnimationSpec` operation as `sharedElement(key)`.

![Browser Preview, squircle host](../evidence/remote-m3-fit-box-state/browser-preview-squircle.png)
![Native, Android player](../evidence/remote-m3-fit-box-state/native-android-squircle.png)
