plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  sourceSets {
    // Curved text is drawn with Skia directly — see src/skikoMain/.../CurvedTextDelegate.kt.
    named("skikoMain").dependencies { api(libs.skiko) }

    // The warper's arithmetic is checkable exactly — a rendered arc cannot say whether a point
    // landed where the geometry says it should.
    jvmTest.dependencies { implementation(kotlin("test")) }

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
