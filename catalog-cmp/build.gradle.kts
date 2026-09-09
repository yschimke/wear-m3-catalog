// `:catalog-cmp` — `:catalog`'s sources, compiled against the Compose Multiplatform port of Wear
// Compose Material 3 instead of the Android AAR.
//
// ## It is not a second catalog
//
// This module owns no catalog sources. It srcDirs `:catalog`'s and subtracts the files that cannot
// leave Android, which is what makes the two lanes a COMPARISON rather than a fork: a sticker
// edited in `:catalog` is edited here, or it is edited nowhere. A parallel tree of hand-maintained
// copies would answer the same question for about a month and then quietly stop.
//
// ## Why the same sources compile twice
//
// `ee.schimke.wearcmp` is `androidx.wear.compose` run through a source transform and republished
// under the ORIGINAL `androidx.wear.compose.material3` / `.foundation` package names. Every public
// facade signature the two share is identical — mangling included, so `Text--4IGK_g` is
// `Text--4IGK_g` on both sides. So no import moves, no `expect`/`actual` is needed for the
// component surface, and the library is chosen by the module, not by the code.
//
// What the port omits, and the two Android-only libraries `:catalog` uses, are subtracted below and
// written up in docs/CMP_PORT.md.
//
// ## `:catalog` stays the oracle
//
// The AAR is the thing this repository reproduces the kit against; the port is a stand-in for it.
// So the Android lane is authoritative and this one is measured against it — never the reverse. A
// difference between the two renders is a finding about the PORT until proven otherwise.
plugins {
  // No version: AGP 9 puts the Kotlin Gradle plugin on the build classpath for the whole build
  // (built-in Kotlin support), and requesting a version here fails with "already on the classpath
  // with an unknown version". `kotlin-multiplatform` in the version catalog documents the id.
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  // No `composePreview { }` block: this module applies `org.jetbrains.compose` with no AGP plugin,
  // so the compose-preview plugin routes it to the DESKTOP renderer — `ImageComposeScene`, no
  // Robolectric and no Android SDK. That routing is the second half of what this module is for.
  // `:catalog`'s `sdkVersion` / `renderBeforeUnitTests` are Robolectric settings with no meaning
  // here.
  alias(libs.plugins.composePreview)
}

kotlin {
  // ONE target, and not the one the endgame wants. `wasmJs()` is what would feed the ui-builder's
  // canvas, and the port publishes a wasmJs klib for it — but
  // `ee.schimke.composeai:preview-annotations`
  // publishes a JVM target only and `data-preview-overrides-runtime` is not multiplatform at all,
  // so a wasm compilation has no `@CatalogComponent` and no knob surface to compile against. That
  // is an upstream gap in compose-ai-tools rather than anything this module can route around;
  // until it closes, wasm would mean giving up the annotations the whole inventory is built from.
  jvm()

  sourceSets {
    val jvmMain by getting {
      // `:catalog`'s sources, minus what cannot cross.
      kotlin.srcDir(rootProject.file("catalog/src/main/kotlin"))

      // Every exclusion is a WHOLE FILE, and each is listed with its reason in docs/CMP_PORT.md —
      // a file is excluded because something in it is Android-only, never because a sticker was
      // inconvenient. The patterns are specific enough not to touch this module's own sources.
      kotlin.exclude(
        // Horologist: the `*-material3` artifacts are Android AARs with no port. Five files.
        "**/HorologistSamples.kt",
        "**/sections/Auth.kt",
        "**/sections/FastScrolling.kt",
        "**/sections/MediaControls.kt",
        "**/sections/Motion.kt",
        // The port omits the surfaces bound to `java.time`, `Calendar`, Android resources and a
        // BroadcastReceiver — `DatePicker` / `TimePicker`, the confirmation and open-on-phone
        // dialogs, and `AnimatedText`. Plain `Picker` and `PickerGroup` ARE ported; it is the
        // composed pickers that are not.
        "**/sections/Pickers.kt",
        "**/sections/Dialogs.kt",
        "**/sections/TextComponents.kt",
        // Downloadable Google fonts are an Android FontsContract. Replaced rather than dropped —
        // see CatalogFontsCmp.kt in this module, and read its warning before comparing a theme.
        "**/CatalogFonts.kt",
        // The port carries the one-handed-gesture MANAGER and its configuration, but not the
        // indicators or the modifier the stickers call — `OneHandedGestureClickIndicator`,
        // `…ScrollIndicator`, the two page indicators and `Modifier.oneHandedGesture` are absent
        // (they carry a gesture registry and an accessibility announcer). A third omission
        // category, beside Horologist and the java.time-bound surfaces.
        "**/sections/OneHandedGestures.kt",
      )

      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.ui)
        implementation(compose.desktop.currentOs)
        // MOBILE Material 3, for `MaterialShapes` and `RoundedPolygon.toShape()` only — the same
        // narrow reason `:catalog` depends on the Android one, and the same forward pin: the
        // expressive shape surface is not in stable material3 on either platform.
        implementation(libs.compose.multiplatform.material3)
        implementation(libs.compose.multiplatform.material.icons.extended)
        // Republishes `androidx.compose.ui.tooling.preview.Preview` — the FQN preview discovery
        // scans for — on the JVM target.
        implementation(libs.compose.multiplatform.ui.tooling.preview)

        // THE POINT OF THIS MODULE. Same package names as `androidx.wear.compose:compose-material3`
        // and `:compose-foundation`, which is why the sources above need no edit at all.
        implementation(libs.wearcmp.material3)
        implementation(libs.wearcmp.foundation)

        implementation(libs.materialkolor)
        implementation(libs.composeai.preview.annotations)
        implementation(libs.composeai.preview.overrides)
      }
    }
  }
}
