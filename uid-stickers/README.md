# `uid-stickers` — the shadow catalog

wear-m3 stickers whose body is a UI builder document. The same method as
[m3-catalog's `uid-stickers`](https://github.com/yschimke/m3-catalog/tree/main/uid-stickers), whose 50
generated stickers are now the catalog's own.

| File | Holds |
| --- | --- |
| `stickers/<name>.uid` | the body: one component definition (`components`) placed once (`roots`) |
| `stickers/<name>.json` | `variants` (cells: builder operations applied before export) and `pairs` (shell → hand-written sticker) |
| `shells/kotlin/…/uid/<Section>.kt` | the shells: the hand-written sticker's annotations and name, its knobs, and which cell to call |
| `tools/*.py` | the scripts that write the documents for a family of stickers |

The generated Kotlin is exactly what the builder's code pane exports
(`ScreenExportGate.exportComponents`): `internal` components whose parameters are the document's
bindings (copy, `enabled`) and its click intents. The shell supplies `kitCopy`, `counted` and the
structural knobs; nothing in the generated body is catalog code.

## Running it

Opt-in, and nothing ships from it: with `-PuidStickers=true`, `:catalog` compiles the generated
bodies and the shells beside the stickers they shadow, so one Robolectric render draws both.

```sh
# Once, without the flag: the vocabulary (`catalog/build/compose-previews/{components,ui-builder}.json`).
scripts/agent-gradle.sh :catalog:composePreviewDiscover
# Export, render both, compare every capture.
scripts/agent-gradle.sh --exclusive -PuidStickers=true :catalog:composePreviewRender
python3 uid-stickers/compare-local.py
```

`exportComponents` and the Wear vocabulary it needs are not in a released exporter yet, so the
generator resolves `compose-preview-ui-builder-export-jvm:3.109.1-SNAPSHOT` from Maven local: publish
yschimke/compose-ui-builder's branch with `:ui-builder-export:publishToMavenLocal`
(`-PuidExportVersion=` picks another).

`compare-local.py` passes when every capture a hand-written sticker publishes (modes, override
variants) has a pixel-identical capture from its shell.

## Status

| Sticker | Document | Captures |
| --- | --- | --- |
| `Button/Tonal` | `tonal-button` | 10/10 identical |
| `Button/Outlined` | `outlined-button` | 10/10 identical |
| `Button/Child` | `child-button` | 10/10 identical |
| `ListHeader` | `list-header` | 2/2 identical |
| `ListSubHeader` | `list-sub-header` | 3/3 identical |
| `Text/Body` | `text-body` | 2/2 identical |
| `Text/Caption` | `text-caption` | 2/2 identical |
