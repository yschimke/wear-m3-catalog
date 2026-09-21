plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  android {
    namespace = "androidx.compose.remote.creation.compose"
    compileSdk = 37
    minSdk = 29
    androidResources.enable = true
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
        api("androidx.annotation:annotation:1.9.1")
        api("androidx.collection:collection:1.5.0")
        implementation(project(":vendor:remote-write-core"))
        @Suppress("DEPRECATION") api(compose.ui)
        @Suppress("DEPRECATION") implementation(compose.foundation)
        @Suppress("DEPRECATION") implementation(compose.runtime)
        implementation("androidx.graphics:graphics-shapes:1.1.0")
      }
    }
    val commonTest by getting { dependencies { implementation(kotlin("test")) } }
    val jvmAndAndroidMain by creating {
      dependsOn(commonMain)
      dependencies { api(libs.compose.remote.creation) }
    }
    val androidMain by getting {
      dependsOn(jvmAndAndroidMain)
      dependencies {
        implementation("androidx.graphics:graphics-path:1.1.0-rc01")
        implementation("androidx.core:core-ktx:1.16.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
        implementation("androidx.tracing:tracing-ktx:1.3.0")
        implementation("androidx.savedstate:savedstate:1.3.1")
        implementation("androidx.appcompat:appcompat:1.7.1")
      }
    }
    val jvmMain by getting { dependsOn(jvmAndAndroidMain) }
    jvmMain.dependencies {
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
    val jvmTest by getting { dependencies { implementation(kotlin("test")) } }
  }
}

// Phase 3 replaces AndroidX's three-module Java writer stack on JVM. Android continues to consume
// the published platform variant until the Android-only bitmap/path adapters are split from it.
configurations
  .matching { it.name.startsWith("jvm") }
  .configureEach {
    resolutionStrategy.dependencySubstitution {
      substitute(module("androidx.compose.remote:remote-core"))
        .using(project(":vendor:remote-core"))
      substitute(module("androidx.compose.remote:remote-creation-core"))
        .using(project(":vendor:remote-core"))
      substitute(module("androidx.compose.remote:remote-creation"))
        .using(project(":vendor:remote-core"))
    }
  }
