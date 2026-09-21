plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  android {
    namespace = "androidx.compose.remote.foundation"
    compileSdk = 37
    minSdk = 29
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
      }
    }
  }
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs { browser() }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":vendor:remote-creation-compose"))
        @Suppress("DEPRECATION") api(compose.runtime)
        @Suppress("DEPRECATION") api(compose.ui)
        @Suppress("DEPRECATION") api(compose.foundation)
      }
    }
    val jvmAndAndroidMain by creating {
      dependsOn(commonMain)
    }
    val androidMain by getting { dependsOn(jvmAndAndroidMain) }
    val jvmMain by getting { dependsOn(jvmAndAndroidMain) }
  }
}
