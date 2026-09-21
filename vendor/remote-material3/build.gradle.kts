plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  android {
    namespace = "androidx.wear.compose.remote.material3"
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
        api(project(":vendor:remote-foundation"))
        api(libs.wearcmp.compose.material3)
        implementation(project(":vendor:remote-creation-compose"))
        @Suppress("DEPRECATION") implementation(compose.runtime)
        @Suppress("DEPRECATION") implementation(compose.ui)
        @Suppress("DEPRECATION") implementation(compose.foundation)
        implementation(libs.compose.multiplatform.material3)
      }
    }
    val jvmAndAndroidMain by creating {
      dependsOn(commonMain)
    }
    val androidMain by getting { dependsOn(jvmAndAndroidMain) }
    val jvmMain by getting { dependsOn(jvmAndAndroidMain) }
  }
}

val wearComposeVersion = libs.versions.wear.compose.asProvider().get()

configurations
  .matching { it.name.startsWith("android") }
  .configureEach {
    resolutionStrategy.dependencySubstitution {
      substitute(module("ee.schimke.wearcmp:wear-compose-material3"))
        .using(module("androidx.wear.compose:compose-material3:$wearComposeVersion"))
        .because("Android uses the real AndroidX Wear Compose library")
      substitute(module("ee.schimke.wearcmp:wear-compose-foundation"))
        .using(module("androidx.wear.compose:compose-foundation:$wearComposeVersion"))
        .because("transitive of Wear Compose Material 3")
      substitute(module("ee.schimke.wearcmp:wear-compose-material-core"))
        .using(module("androidx.wear.compose:compose-material-core:$wearComposeVersion"))
        .because("transitive of Wear Compose Material 3")
    }
    exclude(group = "org.jetbrains.skiko")
  }
