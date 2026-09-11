# Vendored: `horologist-lottie` (Horologist's Lottie → Remote Compose compiler)

A **Lottie compiler**, not a Lottie player. `LottieAnimation(json = …)` is a `@RemoteComposable`:
it parses a Lottie JSON animation, and re-emits every layer, shape, gradient and transform as
Remote Compose *creation* operations whose animated properties are expressions over the document's
own animation clock (`Rc.Time.ANIMATION_TIME`) or over an author-supplied `progress`.

The consequence is the reason this is here: the animation ends up **inside the `.rc` document**.
Nothing on the device needs a Lottie runtime, or the JSON, or a network fetch — a player that can
read a `.rc` document can play the animation, which is exactly what a Wear widget host does.

## Upstream

- Repository: <https://github.com/google/horologist>
- Path: `remotecompose/lottie`
- Commit: `5a5e0cda24170f581711f8c678f028420d79dbfe` (`main`, 2026-09-04)
- License: Apache-2.0 (see the per-file headers, kept verbatim)

Horologist publishes no artifact for this module — it carries no `maven-publishing` plugin, and
nothing under `com.google.android.horologist:horologist-remotecompose-*` exists on Maven Central.
So a consumer either vendors it or does without.

## Why this repository vendors it

`:remote-catalog` is the only consumer, and it needs the compiler twice over:

1. to **draw** the `remote-m3/lottie` sticker (`LottiePreviews.kt`). Until it did, the published
   `remote-m3` catalog could not offer the component at all — a published component needs a
   record, a record needs a call site, and this catalog had none. It was the last component the
   published shelf lost to the synthesised one
   ([compose-preview-server#674](https://github.com/yschimke/compose-preview-server/issues/674)).
2. to **compile** the exporter-generated widget checked in under `remote/generated/`. A design
   carrying a Lottie node makes `RemoteContentEmitter` write `LottieAnimation(json = …)` into that
   file, and exported source that compiles against nothing is a promise nobody checks.

The copy came here from `yschimke/rc-players`, which vendored it first for reason (2) alone. It is
a project dependency rather than a published coordinate, so the compiler and the sticker that
draws it move together in one commit.

**Temporarily**, and the word is load-bearing: the moment Horologist publishes this module, the
vendored copy should be deleted and replaced with the coordinate. Nothing here is a local
improvement worth keeping — see *Local modifications*, which is deliberately almost empty.

## What is vendored

`src/main/` only — 40 Kotlin files, ~3.6k lines, in two halves:

| Package | What it is |
| --- | --- |
| `…/lottie/format/` | The Lottie JSON model and its `kotlinx.serialization` decoder: layers, shapes, keyframed properties, beziers, gradients. |
| `…/lottie/renderer/` | The other half of each of those types, emitting Remote Compose operations for it. |
| `…/lottie/LottieAnimation.kt` | The entry points — `LottieAnimation(json)` and `LottieAnimation(@RawRes)` — plus the frame expression and the fit-and-centre scale modifier. |
| `…/lottie/SlotMap.kt` | Lottie slot id → `RemoteColor`, for theming an animation from the host. |

Deliberately **not** vendored:

- `src/debug/` — upstream's `@Preview` harness and its raw-resource sample animations. It exists to
  look at the output in Android Studio, and pulls in `remote-player-*` and the tooling previews.
- `src/test/` and `src/test/screenshots/` — Robolectric screenshot tests bound to upstream's
  `:roboscreenshots` module, which is Horologist infrastructure this repository does not have. The
  parity question they answer (does the compiled document match `lottie-compose`'s own render?) is
  upstream's to answer at the pinned commit; ours is whether the exported source compiles and the
  builder's element round-trips.
- `api/current.api` — a metalava dump for a module Horologist publishes an API surface for. This
  copy is an implementation detail of one catalog module, not a published API, and this repository
  gates no ABI.

## What is ours

One file, and it is not in `src/main`:
`src/test/java/ee/schimke/composeai/horologist/lottie/ExportedCallShape.kt`. It asserts nothing at
runtime — it is a *compilation* of exactly the call the UI builder's `remote-m3/lottie` export
writes, so a renamed argument or a moved package in a newer Horologist breaks the build here
instead of in the file somebody pasted into their app.

## Copyright

Every file keeps its `Copyright … The Android Open Source Project` header and the Apache-2.0
notice verbatim. No header was rewritten, and no file was re-attributed.

## Local modifications

**One line, and it is whitespace.** `format/graphicelement/geometry/Ellipse.kt` had a 101-column
declaration that ktfmt in Google style wraps onto two lines. It was wrapped while the copy lived in
`yschimke/rc-players`, whose formatter reached it, and it travelled here with the sources.

It will acquire no more. This module excludes `src/main/` from ktfmt — the same rule
`:samples-catalog` applies to its `upstream/` directory, and the formatting counterpart of "a fix
is a patch, never an edit" — so upstream's bytes stay upstream's. A behaviour fix belongs upstream,
where the screenshot tests that can prove it live.

The build file is ours (`build.gradle.kts`), because upstream's applies Horologist's own
convention plugins, metalava and roborazzi. It differs from upstream's in three ways worth naming:

1. `namespace` is `ee.schimke.composeai.horologist.lottie`, not upstream's package. The *sources*
   keep `com.google.android.horologist.remotecompose.lottie` so the diff above stays empty; only
   the manifest namespace moves, so the vendored AAR can never be mistaken for a published
   Horologist one.
2. No `metalava`, no `roborazzi`, no `composeAiPreview` — the modules those need are not here.
3. Not published, and it does not need to be: `:remote-catalog` depends on it as a project, so
   there is no coordinate, no release to wait on, and no second pin to keep in step.

## Version skew

Compiled against the same `androidx.compose.remote` line as `:remote-catalog`, and against the
same **prerelease** Compose pins rather than the repository's `compose-bom`. That is not a detail:
`:remote-catalog` carries no BOM on purpose, and compiling the compiler against a different
Compose than the module that links it is how a `NoSuchMethodError` reaches a render.

Upstream compiles against Horologist's own catalog, so a creation-API change can break this module
here before it breaks upstream. That early warning is worth having and is why the pinned commit is
recorded rather than tracked.
