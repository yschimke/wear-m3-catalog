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

  // `CatalogRenderTest` reads the real renderer output to prove no sticker publishes an empty
  // frame — the one failure mode a green build, a successful render and a human reviewer all miss
  // on a dark-first catalog. Rendering first is what makes it a test of the artifact rather than of
  // a fixture; it costs the test job one render pass (~15s at this size).
  renderBeforeUnitTests.set(true)
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

kotlin {
  compilerOptions {
    // Horologist marks essentially its whole surface `@ExperimentalHorologistApi`, which is a
    // `@RequiresOptIn` at the default ERROR level — `MediaUiModel`, `TrackPositionUiModel`,
    // `PlayerScreen` and the auth screens all carry it. Opting in module-wide rather than
    // annotating each sticker: the alternative is an `@OptIn` on every composable in three files,
    // which says nothing a reader does not already know from the `Horologist` section they are in.
    // It is genuinely experimental — the artifacts are on an alpha line and Renovate holds their
    // bumps for a human to read the visual diff (see .github/renovate.json).
    optIn.add("com.google.android.horologist.annotations.ExperimentalHorologistApi")
  }
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

  // The declared themes in `CatalogThemes.kt`. `ui-text-google-fonts` resolves their typefaces —
  // Roboto Flex, Inter, JetBrains Mono, Google Sans Flex — as downloadable Google fonts, so no TTF
  // is vendored here; materialkolor builds the conference palettes from their seed colours the way
  // Confetti Wear itself does, rather than this repo transcribing the resolved roles by hand.
  implementation(libs.compose.ui.text.google.fonts)
  implementation(libs.materialkolor)

  // HOROLOGIST — the second library on the sheet, and the reason there is a `Horologist` section.
  //
  // Wear Compose Material 3 stops at the component set; the kit does not. Its `Media-Player` set is
  // a whole screen, and the catalog's answer to it used to be an exclusion reading "assembled by an
  // app (or by Horologist), not a library component" — which was true of Wear Compose and false of
  // the ecosystem: Horologist publishes exactly that screen, and the parts it is built from, as
  // library components. Same for the sign-in screens and the fast-scrolling list.
  //
  // The `*-material3` artifacts only. Horologist still ships its original Material 2 line under the
  // un-suffixed names (`horologist-media-ui`, `horologist-auth-composables`), and a sticker drawn
  // from those would be comparing the kit against the wrong design system.
  implementation(libs.horologist.media.ui.material3)
  implementation(libs.horologist.media.ui.model)
  // The player's FOOTER. `PlayerScreen`'s `buttons` slot ships empty, and the two compact buttons
  // the kit draws in it — output device with a volume badge, and overflow — are published here
  // rather than in `media-ui`. Leaving this off the classpath is how the slot came to hold a
  // playlist chip instead (issue #67); see the note in `sections/MediaControls.kt`.
  implementation(libs.horologist.audio.ui.material3)
  implementation(libs.horologist.auth.composables.material3)
  implementation(libs.horologist.compose.layout)
  implementation(libs.horologist.images.base)

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
  // `kit-sets.json` is read by CatalogKitCoverageTest. org.json ships in the Android SDK as stubs
  // only, so the unit test needs the real implementation on its own classpath.
  testImplementation("org.json:json:20250517")
  testImplementation(libs.robolectric)
  testImplementation(platform(libs.compose.bom))
  testImplementation(libs.compose.ui.test.junit4)
  // `debugImplementation`, not `testImplementation`: `createComposeRule` launches a
  // `ComponentActivity`, and this is an application module, so the activity has to be MERGED into
  // the debug manifest Robolectric resolves against.
  debugImplementation(libs.compose.ui.test.manifest)
}
