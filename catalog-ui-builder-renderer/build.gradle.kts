import java.security.MessageDigest
import java.util.zip.ZipFile

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

val runtimeIdentity =
  providers
    .environmentVariable("GITHUB_SHA")
    .map { "wear-m3-p2-${it.take(12)}" }
    .orElse("wear-m3-p2-development")

kotlin {
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    outputModuleName.set("wearM3CatalogRenderer")
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      // Transitional compatibility bridge: one interpreter, still sourced from the combined
      // build. Once UiBuilderSurface's generic half moves into the SDK, the second dependency and
      // the compatibility callback disappear while this module keeps the catalog-owned adapters.
      implementation(libs.composeai.ui.builder.renderer.sdk.source)
      implementation(libs.composeai.ui.builder.source)
      implementation(libs.wearcmp.compose.material3)
      @Suppress("DEPRECATION") implementation(compose.runtime)
      @Suppress("DEPRECATION") implementation(compose.ui)
    }
  }
}

val uiBuilderCheckout =
  providers.gradleProperty("composeUiBuilderDir").map { rootProject.file(it).canonicalFile }

val runtimeAssets =
  tasks.register<Sync>("runtimeAssets") {
    dependsOn("wasmJsDevelopmentExecutableCompileSync", "processSkikoRuntimeForKWasm")
    dependsOn("wasmJsProcessResources")
    from(layout.buildDirectory.dir("compileSync/wasmJs/main/developmentExecutable/kotlin"))
    from(layout.buildDirectory.dir("compose/skiko-runtime-processed-wasmjs")) {
      include("skiko.mjs", "skiko.wasm")
    }
    from(layout.buildDirectory.dir("kotlin-multiplatform-resources/aggregated-resources/wasmJs"))
    from(layout.projectDirectory.dir("src/wasmJsMain/resources")) { include("index.html") }
    // UiBuilderSurface still owns these compatibility adapters. Keep their runtime assets beside
    // the linked Wasm until those adapters move into their catalogs with the interpreter split.
    from(uiBuilderCheckout.map { it.resolve("assets/js-joda") }) { include("js-joda.esm.js") }
    from(uiBuilderCheckout.map { it.resolve("assets/rc-fonts") }) {
      include("*.ttf", "fonts.json", "*OFL.txt", "LICENSE.txt")
      into("fonts")
    }
    into(layout.buildDirectory.dir("runtimeAssets"))
  }

abstract class AssembleCatalogRendererRuntime : DefaultTask() {
  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val assetsDirectory: DirectoryProperty

  @get:Input abstract val runtimeId: Property<String>

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @get:Inject abstract val fileSystemOperations: FileSystemOperations

  @TaskAction
  fun assemble() {
    val source = assetsDirectory.get().asFile
    val output = outputDirectory.get().asFile
    fileSystemOperations.sync {
      from(source)
      into(output)
    }
    val digest = MessageDigest.getInstance("SHA-256")
    output
      .walkTopDown()
      .filter(File::isFile)
      .map { it.relativeTo(output).invariantSeparatorsPath to it.readBytes() }
      .sortedWith { left, right -> compareUnsignedUtf8(left.first, right.first) }
      .forEach { (path, bytes) ->
        digest.update(path.encodeToByteArray())
        digest.update(0)
        digest.update(bytes.size.toString().encodeToByteArray())
        digest.update(0)
        digest.update(bytes)
      }
    val integrity = digest.digest().joinToString("") { "%02x".format(it) }
    output
      .resolve("runtime-manifest.json")
      .writeText(
        """{"schema":"compose-ui-builder-runtime/v1","runtimeId":"${runtimeId.get()}","protocolVersion":2,"entrypoint":"index.html","integritySha256":"$integrity"}"""
      )
  }

  private fun compareUnsignedUtf8(left: String, right: String): Int {
    val leftBytes = left.encodeToByteArray()
    val rightBytes = right.encodeToByteArray()
    val shared = minOf(leftBytes.size, rightBytes.size)
    for (index in 0 until shared) {
      val difference = (leftBytes[index].toInt() and 0xff) - (rightBytes[index].toInt() and 0xff)
      if (difference != 0) return difference
    }
    return leftBytes.size - rightBytes.size
  }
}

val wasmRendererDist =
  tasks.register<AssembleCatalogRendererRuntime>("wasmRendererDist") {
    description = "Assemble the transitional catalog-owned Wear UI-builder renderer."
    group = "distribution"
    dependsOn(runtimeAssets)
    assetsDirectory.set(layout.buildDirectory.dir("runtimeAssets"))
    runtimeId.set(runtimeIdentity)
    outputDirectory.set(layout.buildDirectory.dir("wasmRendererDist"))
  }

val rendererArchive =
  tasks.register<Zip>("rendererArchive") {
    description = "Package the immutable catalog-owned Wear UI-builder renderer."
    group = "distribution"
    dependsOn(wasmRendererDist)
    from(wasmRendererDist.flatMap { it.outputDirectory })
    archiveFileName.set("wear-m3-ui-builder-renderer.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }

abstract class VerifyCatalogRendererRuntime : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val archiveFile: RegularFileProperty

  @get:Input abstract val expectedRuntimeId: Property<String>

  @TaskAction
  fun verify() {
    ZipFile(archiveFile.get().asFile).use { zip ->
      val names = zip.entries().asSequence().map { it.name }.toSet()
      val required =
        setOf(
          "runtime-manifest.json",
          "index.html",
          "wearM3CatalogRenderer.mjs",
          "wearM3CatalogRenderer.wasm",
          "skiko.mjs",
          "skiko.wasm",
        )
      check(names.containsAll(required)) { "renderer archive is missing ${required - names}" }
      check(names.none { it.startsWith('/') || it.contains("../") || '\\' in it }) {
        "renderer archive contains an unsafe path"
      }
      val manifest = zip.getInputStream(zip.getEntry("runtime-manifest.json")).reader().readText()
      check(manifest.contains("\"runtimeId\":\"${expectedRuntimeId.get()}\""))
      check(manifest.contains("\"protocolVersion\":2"))
      check(manifest.contains(Regex("\"integritySha256\":\"[a-f0-9]{64}\"")))
    }
  }
}

tasks.register<VerifyCatalogRendererRuntime>("verifyRendererRuntime") {
  group = "verification"
  dependsOn(rendererArchive)
  archiveFile.set(rendererArchive.flatMap { it.archiveFile })
  expectedRuntimeId.set(runtimeIdentity)
}

tasks.named("check") { dependsOn("verifyRendererRuntime") }
