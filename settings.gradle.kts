pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositories {
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
