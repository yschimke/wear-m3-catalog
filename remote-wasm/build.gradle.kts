plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    outputModuleName.set("remoteComposeWasm")
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":vendor:remote-write-core"))
      implementation(project(":vendor:remote-creation-compose"))
      @Suppress("DEPRECATION") implementation(compose.runtime)
    }
  }
}
