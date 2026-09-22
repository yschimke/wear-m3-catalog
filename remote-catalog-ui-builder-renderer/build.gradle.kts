import java.security.MessageDigest
import java.util.zip.ZipFile

abstract class GenerateRuntimePolicy : DefaultTask() {
  @get:InputFile abstract val policy: RegularFileProperty

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun generate() {
    @Suppress("UNCHECKED_CAST")
    val source =
      groovy.json.JsonSlurper().parseText(policy.get().asFile.readText()) as Map<String, Any?>
    fun projected(section: String): Map<String, Any?> =
      (source[section] as? Map<String, Any?>)
        .orEmpty()
        .mapNotNull { (id, raw) ->
          val entry = raw as? Map<String, Any?> ?: return@mapNotNull null
          val canvas = entry["canvas"] ?: return@mapNotNull null
          id to
            buildMap<String, Any?> {
              put("canvas", canvas)
              entry["canvasMapping"]?.let { put("canvasMapping", it) }
            }
        }
        .toMap()
    val runtimePolicy =
      groovy.json.JsonOutput.toJson(
        mapOf("builtins" to projected("builtins"), "components" to projected("components"))
      )
    val encoded =
      runtimePolicy
        .replace("\\", "\\\\")
        .replace("$", "\\$")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
    outputDirectory
      .get()
      .file("ee/schimke/wearm3catalog/uibuilder/GeneratedRuntimePolicy.kt")
      .asFile
      .apply {
        parentFile.mkdirs()
        writeText(
          """
        package ee.schimke.wearm3catalog.remoteuibuilder

        internal const val catalogUiBuilderPolicyJson = "$encoded"
        """
            .trimIndent()
        )
      }
  }
}

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

val runtimeIdentity =
  providers
    .environmentVariable("GITHUB_SHA")
    .map { "remote-m3-p3-${it.take(12)}" }
    .orElse("remote-m3-p3-development")

val remoteComposePort =
  groovy.json.JsonSlurper().parse(rootProject.file("vendor/remote-compose-upstream.json"))
    as Map<String, Any>
val remoteComposeWriterVersion =
  "${remoteComposePort.getValue("change")}-ps${remoteComposePort.getValue("patchSet")}-cmp" +
    "%02d".format((remoteComposePort.getValue("portRevision") as Number).toInt())

check(libs.versions.remote.compose.cmp.get() == remoteComposeWriterVersion) {
  "remote-m3 Browser Preview must consume the published port $remoteComposeWriterVersion; " +
    "found ${libs.versions.remote.compose.cmp.get()}"
}

val rcPlayerVersion = libs.versions.rcEmbeddedPlayer.get()

val generatedRuntimePolicy = layout.buildDirectory.dir("generated/uiBuilderRuntimePolicy")
val generateRuntimePolicy by
  tasks.registering(GenerateRuntimePolicy::class) {
    policy.set(rootProject.layout.projectDirectory.file("remote-catalog/ui-builder.policy.json"))
    outputDirectory.set(generatedRuntimePolicy)
  }

kotlin {
  jvm()

  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    outputModuleName.set("remoteM3CatalogRenderer")
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.composeai.ui.builder.renderer.sdk.source)
      implementation(libs.remotecompose.write.core)
      implementation(libs.remotecompose.creation.compose)
      implementation(libs.remotecompose.material3)
      implementation(project(":ui-builder-foundation-adapters"))
      implementation(project(":ui-builder-material-adapters"))
      implementation(project(":ui-builder-wear-adapters"))
      implementation(project.dependencies.platform(libs.composeai.rc.players.bom))
      implementation(libs.composeai.rc.player.compose)
      implementation(libs.wearcmp.compose.material3)
      @Suppress("DEPRECATION") implementation(compose.runtime)
      @Suppress("DEPRECATION") implementation(compose.ui)
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
    jvmTest.dependencies {
      implementation(kotlin("test"))
      @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) @Suppress("DEPRECATION")
      implementation(compose.uiTest)
      implementation(compose.desktop.currentOs)
    }
    wasmJsMain { kotlin.srcDir(generateRuntimePolicy) }
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
    // Skiko's browser loader and the Wear font resolver consume these at runtime. They are assets,
    // not executable UI Builder code; the renderer links only the source/composite SDK.
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

  @get:Input abstract val writerVersion: Property<String>

  @get:Input abstract val playerVersion: Property<String>

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
        """{"schema":"compose-ui-builder-runtime/v1","runtimeId":"${runtimeId.get()}","protocolVersion":2,"entrypoint":"index.html","remoteComposeWriter":"${writerVersion.get()}","rcPlayer":"${playerVersion.get()}","integritySha256":"$integrity"}"""
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
    description = "Assemble the transitional catalog-owned Remote M3 UI-builder renderer."
    group = "distribution"
    dependsOn(runtimeAssets)
    assetsDirectory.set(layout.buildDirectory.dir("runtimeAssets"))
    runtimeId.set(runtimeIdentity)
    writerVersion.set(remoteComposeWriterVersion)
    playerVersion.set(rcPlayerVersion)
    outputDirectory.set(layout.buildDirectory.dir("wasmRendererDist"))
  }

val rendererArchive =
  tasks.register<Zip>("rendererArchive") {
    description = "Package the immutable catalog-owned Remote M3 UI-builder renderer."
    group = "distribution"
    dependsOn(wasmRendererDist)
    from(wasmRendererDist.flatMap { it.outputDirectory })
    archiveFileName.set("remote-m3-ui-builder-renderer.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }

abstract class VerifyCatalogRendererRuntime : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val archiveFile: RegularFileProperty

  @get:Input abstract val expectedRuntimeId: Property<String>

  @get:Input abstract val expectedWriterVersion: Property<String>

  @get:Input abstract val expectedPlayerVersion: Property<String>

  @TaskAction
  fun verify() {
    ZipFile(archiveFile.get().asFile).use { zip ->
      val names = zip.entries().asSequence().map { it.name }.toSet()
      val required =
        setOf(
          "runtime-manifest.json",
          "index.html",
          "remoteM3CatalogRenderer.mjs",
          "remoteM3CatalogRenderer.wasm",
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
      check(manifest.contains("\"remoteComposeWriter\":\"${expectedWriterVersion.get()}\""))
      check(manifest.contains("\"rcPlayer\":\"${expectedPlayerVersion.get()}\""))
      check(manifest.contains(Regex("\"integritySha256\":\"[a-f0-9]{64}\"")))
    }
  }
}

tasks.register<VerifyCatalogRendererRuntime>("verifyRendererRuntime") {
  group = "verification"
  dependsOn(rendererArchive, "jvmTest")
  archiveFile.set(rendererArchive.flatMap { it.archiveFile })
  expectedRuntimeId.set(runtimeIdentity)
  expectedWriterVersion.set(remoteComposeWriterVersion)
  expectedPlayerVersion.set(rcPlayerVersion)
}

tasks.named("check") { dependsOn("verifyRendererRuntime") }
