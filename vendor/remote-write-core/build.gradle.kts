plugins { id("org.jetbrains.kotlin.multiplatform") }

kotlin {
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    nodejs()
  }

  sourceSets {
    commonTest.dependencies { implementation(kotlin("test")) }
    jvmTest.dependencies { implementation(project(":vendor:remote-core")) }
  }
}
