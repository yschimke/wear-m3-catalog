pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositories {
    maven("https://raw.githubusercontent.com/yschimke/wear-m3-catalog/wear-compose-cmp-maven/") {
      content { includeGroup("ee.schimke.wearcmp") }
    }
    mavenCentral()
    google()
  }
}

rootProject.name = "wasm-typography-repro"
