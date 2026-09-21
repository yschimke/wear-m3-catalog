plugins {
  id("org.jetbrains.kotlin.jvm")
  `java-library`
}

dependencies {
  api("androidx.annotation:annotation:1.9.1")
  api("org.jspecify:jspecify:1.0.0")
  testImplementation(kotlin("test"))
}

kotlin {
  sourceSets.main { kotlin.srcDir("src/main/java") }
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}
