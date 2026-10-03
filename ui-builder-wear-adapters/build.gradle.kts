plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  jvm()

  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

  sourceSets {
    commonMain.dependencies {
      api(libs.composeai.ui.builder.renderer.sdk.source)
      implementation(libs.wearcmp.compose.material3)
      @Suppress("DEPRECATION") implementation(compose.foundation)
      @Suppress("DEPRECATION") implementation(compose.runtime)
      @Suppress("DEPRECATION") implementation(compose.ui)
    }
    // `WearAdapterPropertyParityTest` draws every adapter on the desktop, which is why the adapters
    // are common code: the JVM is where a Compose UI test runs without a browser.
    jvmTest.dependencies {
      implementation(kotlin("test"))
      @Suppress("DEPRECATION") implementation(compose.desktop.uiTestJUnit4)
      @Suppress("DEPRECATION") implementation(compose.desktop.currentOs)
    }
  }
}
