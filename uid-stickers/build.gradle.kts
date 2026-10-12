// `:uid-stickers` — the SHADOW catalog: wear-m3 stickers whose body is a UI builder `.uid`
// document.
//
// Opt-in (`-PuidStickers=true`), outside the published inventory. `stickers/<name>.uid` is a
// sticker body (one component definition placed once) and its sibling `stickers/<name>.json` lists
// its cells (builder operations applied before export) and `pairs` (shell → hand-written sticker).
// `generateUidStickers` exports every document and cell through the UI builder's own exporter
// (`ScreenExportGate.exportComponents`, the code behind the builder's code pane) into
// `build/generated/uid/kotlin`. With the same flag, `:catalog` compiles that output and the shells
// in
// `shells/kotlin` beside the hand-written stickers, so one Robolectric render draws both and
// `compare-local.py` pairs them capture for capture. The same method as m3-catalog's
// `uid-stickers`.
plugins {
  // By id: AGP 9 already put KGP on the buildscript classpath (see `catalog/build.gradle.kts`).
  id("org.jetbrains.kotlin.jvm")
}

kotlin { jvmToolchain(21) }

// `exportComponents` and the Wear vocabulary are not in a released exporter yet, nor the
// generator's toggle intents (compose-ai-tools#5896, after 2.52.0), so this resolves both from
// Maven local: `:ui-builder-export:publishToMavenLocal` in yschimke/compose-ui-builder and
// `:screen-model:publishToMavenLocal` in yschimke/compose-ai-tools.
repositories {
  mavenLocal()
  mavenCentral()
  google()
}

val generator by sourceSets.creating

dependencies {
  // The exporter the builder's code pane and the server's export.compose route run.
  "generatorImplementation"(
    "ee.schimke.composeai:compose-preview-ui-builder-export-jvm:" +
      providers.gradleProperty("uidExportVersion").getOrElse("3.109.1-SNAPSHOT")
  )
  // The generator the exporter writes through, pinned past the exporter's own pin.
  val screenModel = providers.gradleProperty("screenModelVersion").getOrElse("2.52.1-SNAPSHOT")
  "generatorImplementation"("ee.schimke.composeai:screen-model-jvm") {
    version { strictly(screenModel) }
  }
  "generatorImplementation"("ee.schimke.composeai:screen-model") {
    version { strictly(screenModel) }
  }
}

val generatedDir = layout.buildDirectory.dir("generated/uid/kotlin")

val generateUidStickers by
  tasks.registering(JavaExec::class) {
    group = "build"
    description = "Exports every sticker document and variant cell to Kotlin with the UI builder."
    classpath = generator.runtimeClasspath
    mainClass = "ee.schimke.wearm3catalog.uid.GenerateUidStickersKt"
    // The vocabulary is `:catalog`'s own published pair (its discovered record and builder policy),
    // so a document can name only what the builder serves for `wear-m3`. Read in place without a
    // task dependency: with the flag on, `:catalog` compiles this task's output, so depending on
    // its
    // discovery would be a cycle. Run `:catalog:composePreviewDiscover` once first; the shadow
    // sources only add call sites, so the library entries a document resolves against do not move.
    val catalogDiscovery = rootProject.layout.projectDirectory.dir("catalog/build/compose-previews")
    inputs.dir("stickers")
    inputs.file("stickers.json")
    inputs.file("builder-foundation-record.json")
    inputs.files(fileTree(".") { include("project-components*.json") })
    inputs.file(catalogDiscovery.file("components.json"))
    inputs.file(catalogDiscovery.file("ui-builder.json"))
    outputs.dir(generatedDir)
    args(
      layout.projectDirectory.asFile.absolutePath,
      generatedDir.get().asFile.absolutePath,
      catalogDiscovery.asFile.absolutePath,
    )
  }
