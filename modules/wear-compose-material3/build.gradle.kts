plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
  // For `composeResources/` — the vector drawables the dialogs draw. It is the one thing that
  // parses Android `<vector>` XML on every target, wasm included, so the icons are the upstream
  // artwork rather than a redrawing of it.
  alias(libs.plugins.compose)
}

compose.resources {
  // A package of this port's own: the generated `Res` is not upstream API, and putting it under
  // `androidx.wear.compose.material3` would suggest it is.
  packageOfResClass = "ee.schimke.wearcmp.material3.resources"
  publicResClass = true
}

kotlin {
  sourceSets {
    named("skikoMain").dependencies { api(libs.skiko) }

    // The port's own tests. They run on the JVM because that is the fast target; everything they
    // cover is common code.
    jvmTest.dependencies { implementation(kotlin("test")) }

    commonMain.dependencies {
      api(project(":wear-compose-foundation"))
      api(project(":wear-compose-material-core"))
      api(libs.compose.runtime)
      api(libs.compose.foundation)
      api(libs.compose.ui)
      api(libs.compose.animation)
      implementation(libs.compose.material)
      api(project(":port-runtime"))
      implementation(libs.androidx.annotation)
      implementation(libs.androidx.collection)
      implementation(compose.components.resources)
      // The expressive shape library. Already multiplatform — `graphics-shapes-wasm-js` is
      // published — so the shape morphing in Wear Material 3 needs no port at all.
      implementation(libs.androidx.graphics.shapes)
    }
  }
}
