// `:catalog` — the M3 Wear OS Apps Design Kit rebuilt as design-led `@Preview`s.
//
// An **Android** module, unlike the phone-side sibling (yschimke/m3-catalog), and not by choice:
// `androidx.wear.compose:compose-material3` ships only for Android, so there is no Compose
// Multiplatform desktop target to render on. The compose-preview plugin therefore routes this
// module to the Robolectric renderer — the same lane the `wear-m3` catalog in compose-ai-tools
// uses.
//
// The catalog's inventory lives in **annotations next to the previews** (`@CatalogGroup` /
// `@CatalogComponent` / `@CatalogVariant` / `@OverrideVariant`). `catalog.spec.json` carries only
// the cover-sheet fields.
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.composePreview)
}

composePreview {
  // Robolectric SDK 35, which runs on the JDK 17 toolchain below. SDK 36 requires JDK 21+.
  sdkVersion.set(35)
}

android {
  namespace = "ee.schimke.wearm3catalog"
  // wear-compose 1.7.0-beta requires compileSdk 37.
  compileSdk = 37

  defaultConfig {
    applicationId = "ee.schimke.wearm3catalog"
    minSdk = 30
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures { compose = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material.icons.core)
  implementation(libs.wear.compose.material3)
  implementation(libs.wear.compose.foundation)
  implementation(libs.wear.compose.ui.tooling)
  implementation(libs.wear.tooling.preview)

  // MOBILE Material 3, for `MaterialShapes` and `RoundedPolygon.toShape()` ONLY.
  //
  // The kit's Shapes page publishes the 35 expressive shapes — Circle, Square, … Heart — and Wear
  // Compose has no shape library that names them: `androidx.wear.compose.material3` ships
  // `ShapeDefaults` (corner radii) and `AnimatedMorphShape`, and
  // `androidx.graphics:graphics-shapes`
  // ships only the polygon primitives the shapes are BUILT from. `MaterialShapes` itself lives in
  // `androidx.compose.material3`, and it is plain `RoundedPolygon` data — no mobile theming, no
  // mobile component pulled into a render. So the specimen sheet draws Material's own finished
  // polygons rather than this repo's arithmetic, which is the whole point of a design-led catalog.
  implementation(libs.compose.material3)

  // `@CatalogGroup`, `@CatalogComponent`, `@CatalogVariant`, `@OverrideVariant`, and the token
  // catalog annotations.
  implementation(libs.composeai.preview.annotations)
  // `previewOverride*` — the knob surface `@OverrideVariant` seeds, so ONE `@Preview` can carry a
  // whole variant matrix instead of one near-identical composable per cell.
  implementation(libs.composeai.preview.overrides)

  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(platform(libs.compose.bom))
  testImplementation(libs.compose.ui.test.junit4)
  // `debugImplementation`, not `testImplementation`: `createComposeRule` launches a
  // `ComponentActivity`, and this is an application module, so the activity has to be MERGED into
  // the debug manifest Robolectric resolves against.
  debugImplementation(libs.compose.ui.test.manifest)
}
