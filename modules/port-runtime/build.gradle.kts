plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
    commonMain.dependencies {
      api(libs.compose.runtime)
      api(libs.compose.ui)
    }
  }
}
