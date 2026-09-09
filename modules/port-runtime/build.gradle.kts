plugins {
  alias(libs.plugins.kotlin.multiplatform)
  // For `LocalWearDeviceConfiguration` and the seams read from composition: a module that declares
  // anything @Composable needs the Compose compiler, and the JVM backend crashes rather than
  // reporting an error when it does not have it.
  alias(libs.plugins.compose.compiler)
}

// The port's shared runtime: multiplatform stand-ins for the handful of Android and JDK types the
// AndroidX sources reach for. Everything here is named after the type it replaces, so
// `transform-rules.json` can rewire a call site by rewriting its import and nothing else — no
// patch, and nothing to re-cut when upstream edits the file around it.
//
// It is a module rather than a per-module `commonPort` file because the same three types turn up
// in all three ported artifacts, and `LocalWearDeviceConfiguration` in particular has to be ONE
// composition local: `materialcore` and `foundation` both ask whether the screen is round, and a
// copy each would let them disagree.

kotlin {
  sourceSets {
    // Skia is already on both targets' classpaths through Compose; naming it here is what lets
    // `skikoMain` compile against it directly.
    named("skikoMain").dependencies { api(libs.skiko) }

    commonMain.dependencies {
      api(libs.compose.runtime)
      api(libs.compose.ui)
      // For the Morph -> Compose Path transcription in ShapePaths.kt.
      api(libs.androidx.graphics.shapes)
      // The date types the pickers are written against; see PlatformDateTimeFormat.
      api(libs.kotlinx.datetime)
    }
  }
}
