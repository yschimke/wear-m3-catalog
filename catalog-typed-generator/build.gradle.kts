plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktfmt)
}

kotlin { jvmToolchain(21) }

ktfmt { googleStyle() }

kotlin.sourceSets.named("main") {
  kotlin.srcDir("../catalog-typed-policy/src/main/kotlin")
  kotlin.srcDir("../catalog-typed-policy/src/commonMain/kotlin")
}

dependencies {
  implementation(platform(libs.composeai.contracts.bom))
  implementation(platform(libs.composeai.tools.bom))
  implementation("ee.schimke.composeai:screen-model")
  implementation("ee.schimke.composeai:ui-builder-protocol")
  testImplementation(kotlin("test"))
}

val catalogRoot = layout.projectDirectory.dir("..")
val generateCatalogs =
  tasks.register<JavaExec>("generateCatalogs") {
    group = "code generation"
    description =
      "Generate complete parallel catalog pairs from compiled annotations and Kotlin policies."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ee.schimke.catalog.typed.GenerateCatalogsKt")
    inputs.files(
      catalogRoot.file("catalog/build/compose-previews/components.json"),
      catalogRoot.file("catalog/build/compose-previews/ui-builder.json"),
      catalogRoot.file("samples-catalog/build/compose-previews/components.json"),
      catalogRoot.file("samples-catalog/build/compose-previews/ui-builder.json"),
      catalogRoot.file("catalog.spec.json"),
      catalogRoot.file("samples-catalog/catalog.spec.json"),
    )
    inputs.files(fileTree(catalogRoot.dir("ui-builder/designs")))
    val output = layout.buildDirectory.dir("catalog")
    outputs.dir(output)
    argumentProviders.add(
      CommandLineArgumentProvider {
        listOf(catalogRoot.asFile.absolutePath, output.get().asFile.absolutePath)
      }
    )
  }

tasks.test {
  dependsOn(generateCatalogs)
  systemProperty(
    "generatedCatalogRoot",
    layout.buildDirectory.dir("catalog").get().asFile.absolutePath,
  )
  systemProperty("catalogRoot", catalogRoot.asFile.absolutePath)
}
