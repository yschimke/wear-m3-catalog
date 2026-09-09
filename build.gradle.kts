plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.compose.compiler) apply false
}

subprojects {
  // Every module is the same shape — the same two targets and the same opt-ins — because every
  // module is the same transform run over a different artifact. Configuring it here keeps each
  // module's build file down to its dependencies, which are the only thing that actually differs.
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
      jvm()
      @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

      // The port's own common code: the `expect` declarations the patches in `patches/` rewire
      // the generated sources onto. It is a second directory rather than a file in
      // `src/commonMain/kotlin` because transform.py deletes that directory wholesale on every
      // run — generated and hand-written source cannot share a root and both survive.
      sourceSets.commonMain { kotlin.srcDir("src/commonPort/kotlin") }

      // AndroidX compiles its own sources with these opt-ins; the code is full of call sites that
      // assume them. They are not a porting decision — upstream's build.gradle carries the same
      // list — so they belong to the harness rather than to any one module.
      sourceSets.all {
        languageSettings {
          optIn("androidx.compose.animation.core.ExperimentalAnimationSpecApi")
          optIn("androidx.compose.foundation.ExperimentalFoundationApi")
          optIn("androidx.compose.material.ExperimentalMaterialApi")
          optIn("androidx.compose.ui.ExperimentalComposeUiApi")
          optIn("androidx.compose.ui.text.ExperimentalTextApi")
          optIn("kotlin.contracts.ExperimentalContracts")
        }
      }
    }
  }
}
