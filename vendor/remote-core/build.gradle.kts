plugins {
  id("org.jetbrains.kotlin.jvm")
  `java-library`
  `maven-publish`
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
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      artifactId = "remote-core"
      from(components["java"])
    }
  }
}
