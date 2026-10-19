// Independent policy consumer: no Android, Remote Compose or renderer implementation graph.
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
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
  versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}

rootProject.name = "catalog-typed-policy"
