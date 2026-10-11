plugins {
  id("org.jetbrains.kotlin.jvm")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin { jvmToolchain(21) }

dependencies {
  implementation(project(":catalog-typed-adapters"))
  @Suppress("DEPRECATION") implementation(compose.runtime)
  @Suppress("DEPRECATION") implementation(compose.ui)
}

// A separate compilation from discovery and registration: generated call sites must resolve
// against the public wrapper API, with default and explicitly authored arguments and state reads.
val generation = project(":catalog-typed-adapters").tasks.named("generateTypedCatalog")
val generatedSources =
  project(":catalog-typed-adapters").layout.buildDirectory.dir("catalog/generated-source")

kotlin.sourceSets.named("main") { kotlin.srcDir(files(generatedSources).builtBy(generation)) }
