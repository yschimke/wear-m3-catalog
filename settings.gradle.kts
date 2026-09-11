pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositories {
    // AndroidX snapshot build for the Remote Compose trio. Keep this build ID and the three
    // 1.0.0-SNAPSHOT version refs in libs.versions.toml together so the artifacts cannot skew.
    maven("https://androidx.dev/snapshots/builds/16323089/artifacts/repository") {
      content {
        includeGroupByRegex("androidx\\.compose\\.remote.*")
        includeGroupByRegex("androidx\\.wear\\.compose\\.remote.*")
        includeGroupByRegex("androidx\\.glance\\.wear.*")
      }
    }
    mavenCentral()
    google()
  }
}

rootProject.name = "wear-m3-catalog"

include(":catalog")

// The Remote Compose rendition of the same Wear surface — the third column of the comparison this
// repo publishes. Separate module, not a source set: it is on the alpha Remote Compose line at
// compileSdk 37 with no Compose BOM, and that must not reach `:catalog`. See
// remote-catalog/build.gradle.kts.
include(":remote-catalog")
