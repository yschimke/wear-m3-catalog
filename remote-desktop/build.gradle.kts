plugins {
  id("org.jetbrains.kotlin.jvm")
  application
  id("org.jetbrains.kotlin.plugin.compose")
}

application { mainClass.set("ee.schimke.remote.desktop.MainKt") }

dependencies {
  implementation(project(":vendor:remote-creation-compose"))
  implementation(project(":vendor:remote-write-core"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  testImplementation(kotlin("test"))
  testImplementation(libs.junit)
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

configurations.configureEach {
  resolutionStrategy.dependencySubstitution {
    substitute(module("androidx.compose.remote:remote-core")).using(project(":vendor:remote-core"))
    substitute(module("androidx.compose.remote:remote-creation-core"))
      .using(project(":vendor:remote-core"))
    substitute(module("androidx.compose.remote:remote-creation"))
      .using(project(":vendor:remote-core"))
  }
}
