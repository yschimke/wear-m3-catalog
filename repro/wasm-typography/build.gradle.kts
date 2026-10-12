plugins {
  kotlin("multiplatform") version "2.4.21"
  id("org.jetbrains.compose") version "1.12.1"
  id("org.jetbrains.kotlin.plugin.compose") version "2.4.21"
}

kotlin {
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets.commonMain.dependencies {
    implementation("ee.schimke.wearcmp:wear-compose-material3:1.7.0-beta02-cmp09")
  }
}
