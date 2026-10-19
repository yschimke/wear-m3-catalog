pluginManagement {
  repositories {
    // Google Maven first, scoped to its own groups: androidx and AGP resolve there and never
    // reach Central or the plugin portal, which answer a miss with HTTP 429 under load instead
    // of the 404 that would have moved on.
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    maven(
      "https://raw.githubusercontent.com/yschimke/wear-m3-catalog-out/wear-compose-cmp-maven/"
    ) {
      content { includeGroup("ee.schimke.wearcmp") }
    }
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

rootProject.name = "wasm-typography-repro"
