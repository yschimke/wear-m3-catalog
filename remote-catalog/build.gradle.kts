// `:remote-catalog` — the **Remote Compose** rendition of the same Wear surface `:catalog` draws.
//
// The third column of this repo's three-way comparison. `:catalog` reproduces the M3 Wear OS Apps
// Design Kit with Wear Compose Material 3; this module reproduces the same components with
// **Wear Compose Remote Material 3** plus the `remote-creation-compose` primitives, and the kit
// itself is the column both are measured against. Each component here names its `:catalog`
// counterpart through `parallel` in `catalog.spec.json`, which is what pairs the two on the
// published compare page.
//
// Every sticker is a real `RemoteDocument`: the composable emits remote content, `RemotePreview`
// builds the document, and the player rasterises it — exactly the path a watch face, tile or
// widget takes on-device. That is why this module carries the alpha Remote Compose runtime
// (compileSdk 37, no Compose BOM) rather than the stable line `:catalog` is on. The two are
// separate modules precisely so that alpha line cannot leak into the catalog that reproduces the
// kit.
//
// Moved here from `:samples:design-catalog-remote-m3` in yschimke/compose-ai-tools — see that
// repo's issue #4588 for why. It publishes the `remote-m3` system, unchanged.
@file:Suppress("RestrictedApiAndroidX")

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.composePreview)
}

composePreview {
  // Pin Robolectric to SDK 35; this module compiles against `compileSdk = 37` but Robolectric only
  // ships up to API 36 (and needs JDK 21+ for that). Same pin `:catalog` carries.
  sdkVersion.set(35)

  // `WidgetContainerIrCaptureTest` and `StickerBakeCoverageTest` read the renders and the `.rc`
  // sidecars out of `build/compose-previews/renders/`, so the render has to run before the unit
  // tests rather than beside them. Same wiring `:catalog` uses for `CatalogRenderTest`.
  renderBeforeUnitTests.set(true)
}

android {
  namespace = "ee.schimke.wearm3catalog.remote"
  // compose-remote alpha08+ / wear-compose-remote alpha02+ raise the AAR minCompileSdk to 37.
  // `:catalog` is already at 37 for wear-compose 1.7.0-beta, so the two agree here by coincidence
  // rather than by coupling.
  compileSdk = 37

  defaultConfig {
    applicationId = "ee.schimke.wearm3catalog.remote"
    // The Remote Compose alpha artifacts require API 29+.
    minSdk = 29
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures { compose = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  // The Remote Compose APIs are `@RestrictTo(LIBRARY_GROUP)`. The source-level
  // `@file:Suppress("RestrictedApiAndroidX")` quiets the IDE inspection, but AGP's lint runs
  // `RestrictedApi` separately. Mirror what AndroidX's own samples do and disable it here.
  lint { disable += "RestrictedApi" }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  // NO Compose BOM, deliberately — `:catalog` has one and this module must not share it.
  // `wear-compose-remote-material3`'s POM pulls the Compose 1.11 runtime for foundation / runtime /
  // ui, and the alpha remote runtime aligns with that line. Pinning explicit prerelease versions
  // keeps resolution aligned instead of fighting the 2026.08.00 BOM the rest of the repo is on.
  implementation(libs.compose.ui.tooling.preview.prerelease)
  implementation(libs.compose.remote.tooling.preview)
  implementation(libs.compose.remote.creation)
  implementation(libs.compose.remote.creation.compose)
  implementation(libs.wear.compose.remote.material3)

  // Glance Wear — the Wear OS widget layer on Remote Compose. The widget-container stickers
  // (`WidgetContainerPreviews.kt`) render through its `wear-tooling-preview` `WearWidgetPreview`
  // wrapper, which recreates the host-drawn squircle container (background + rounded corners +
  // padding) around remote content. `wear` carries the brush/document types, `wear-core` the
  // `WearWidgetParams` / `ContainerInfo` container spec; both are compile-scope needs of the
  // sticker code, so they are declared rather than trusted to transitive scoping.
  implementation(libs.glance.wear)
  implementation(libs.glance.wear.core)
  implementation(libs.glance.wear.tooling.preview)

  implementation(libs.activity.compose)

  // `@WearThemeCatalog` — the declared themes in `RemoteThemeCatalogs.kt`, which populate the
  // preview server's Theme select and re-point the document's default font family.
  implementation(libs.composeai.preview.annotations)

  // materialkolor — the dynamic-colour engine the conference palettes are BUILT with rather than
  // transcribed from (`AGENTS.md` → Themes). `:catalog` declares it for the same reason and for
  // the same four seeds, which is what keeps the two Theme selects one set instead of two tables
  // of hex that drift apart (#99).
  //
  // The one pair here that is not on the alpha Remote line, and deliberately contained: it is read
  // for a `ColorScheme` of plain `Color`s, and nothing it returns reaches a RemoteDocument except
  // as a named colour value (`WearM3.<role>`). No Compose BOM is involved — both are explicit
  // versions, the same two `:catalog` resolves, which is the point: two modules running the same
  // recipe. The prerelease Compose UI this module pins is higher than either asks for, so
  // resolution is unchanged.
  //
  // `material3` is declared because materialkolor's `dynamicColorScheme` RETURNS
  // `androidx.compose.material3.ColorScheme` and does not put it on a consumer's compile classpath
  // — the same runtime-scope hazard the Remote trio above carries, and it fails the same way:
  // "Cannot access class androidx.compose.material3.ColorScheme".
  implementation(libs.materialkolor)
  implementation(libs.compose.material3)

  // The sticker frame captures through the connector's `RemoteOverridablePreview` rather than raw
  // upstream `RemotePreview`, so the named-value stickers (`NamedLabelRemoteButton`,
  // `ShaderGradientSticker`) honour `renderNow.overrides.remoteCompose.namedValues` in trusted
  // live re-renders and the captured RemoteDocument lands in the bundle's `.rc` sidecar. With no
  // seeded overrides — the vanilla render this repo's CI does — the output is byte-for-byte what
  // `RemotePreview` produces.
  implementation(libs.composeai.remotecompose.connector)

  // The knob runtime the kit AXES are read through (`previewOverrideChoice` and friends), and the
  // reason this module can carry `@OverrideVariant` cells at all. `:catalog` has declared it since
  // it started folding the kit's axes into cells (#101); this module published one component per
  // variant instead, so it never needed it. It does now: `Text/Body`'s `Alignment` and then the
  // whole button surface ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116) phase 2).
  // A knob read inside a `RemoteSticker` resolves at composition, before the document is built, so
  // what it turns is the Compose call rather than anything in the RemoteDocument. A render with no
  // override seeded is byte-for-byte what the sticker produced before.
  //
  // It is NOT the connector's `rememberOverridableRemote*` next door, and both are wanted. Those
  // bind a value into the recorded document as a named value, so the player reseeds it live
  // without re-recording; these are read at RECORD time and decide what gets recorded at all —
  // which colours, which slots, which size. A cell that changes the container has to be a
  // record-time choice, because the document does not carry the alternative.
  implementation(libs.composeai.preview.overrides)

  // The widget-container stickers render through `CapturingWearWidgetPreview` rather than
  // upstream's `WearWidgetPreview`, so each one emits its encoded RemoteCompose document as the
  // render's `.rc` sidecar and the bundle packs it as the sticker's IR.
  implementation(libs.composeai.wear.preview.runtime)

  // THE EMBEDDED PLAYER, on the RUNTIME classpath, and deliberately the vendored coordinate rather
  // than upstream's published `androidx.compose.remote:remote-player-compose` (which now ships an
  // embedded player of its own).
  //
  // The connector declares the player `compileOnly`, so a consumer wanting the embedded replay lane
  // has to supply it; `RemoteComposeIrReplay`'s gate then resolves the entry-point METHOD, and the
  // signature it was compiled against names `ee.schimke.composeai.rcembedded.player.RcImageLoader`
  // — the relocated vendored type. Substituting upstream's artifact does not fail loudly: the gate
  // simply returns false and a `player = "embedded"` request falls back to the View player, so the
  // lane goes quiet while every render still succeeds. Swapping to upstream needs the connector to
  // learn a second signature first; that is compose-ai-tools' change to make, not this repo's.
  // `implementation`, matching what this module declared before the move: the bundle's re-render
  // classpath is built from it, and the sticker sources deliberately compile against none of it.
  implementation(libs.composeai.rc.embedded.player)

  debugImplementation(libs.compose.ui.tooling.prerelease)

  testImplementation(libs.junit)
  testImplementation(libs.truth)
}
