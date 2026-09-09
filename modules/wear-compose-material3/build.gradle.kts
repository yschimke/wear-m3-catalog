plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":wear-compose-foundation"))
      api(project(":wear-compose-material-core"))
      api(libs.compose.runtime)
      api(libs.compose.foundation)
      api(libs.compose.ui)
      api(libs.compose.animation)
      implementation(libs.compose.material)
      implementation(project(":port-runtime"))
      implementation(libs.androidx.annotation)
      implementation(libs.androidx.collection)
      // The expressive shape library. Already multiplatform — `graphics-shapes-wasm-js` is
      // published — so the shape morphing in Wear Material 3 needs no port at all.
      implementation(libs.androidx.graphics.shapes)
    }
  }
}
