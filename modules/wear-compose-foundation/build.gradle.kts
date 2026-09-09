plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  sourceSets {
    // Curved text is drawn with Skia directly — see src/skikoMain/.../CurvedTextDelegate.kt.
    named("skikoMain").dependencies { api(libs.skiko) }

    commonMain.dependencies {
      api(libs.compose.runtime)
      api(libs.compose.foundation)
      api(libs.compose.ui)
      api(libs.compose.animation)
      api(project(":port-runtime"))
      implementation(libs.androidx.annotation)
      implementation(libs.androidx.collection)
    }
  }
}
