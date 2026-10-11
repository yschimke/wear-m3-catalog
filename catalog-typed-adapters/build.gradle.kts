plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
}

// The typed pilot: compiled entry points, their Kotlin adapter definitions, a parallel
// `wear-m3-typed-pilot` catalog pair and a separate browser runtime. Only included under
// `-PtypedWearCatalog=true` (settings.gradle.kts); production delivery never reads its outputs.
kotlin {
  jvmToolchain(21)
  jvm()

  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    outputModuleName.set("wearM3TypedCatalogRenderer")
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      // Synthetic: substituted from the tools source build, with no published fallback.
      implementation("ee.schimke.composeai:screen-model:0")
      implementation(libs.composeai.ui.builder.renderer.sdk.source)
      implementation(project(":ui-builder-foundation-adapters"))
      implementation(project(":ui-builder-wear-adapters"))
      implementation(libs.wearcmp.compose.material3)
      implementation(libs.wearcmp.compose.foundation)
      @Suppress("DEPRECATION") implementation(compose.runtime)
      @Suppress("DEPRECATION") implementation(compose.foundation)
      @Suppress("DEPRECATION") implementation(compose.ui)
    }
    // The tools build's nested gradle-plugin build supplies discovery from source.
    jvmMain.dependencies {
      implementation("ee.schimke.composeai:preview-discovery:0")
      // The authored policy compiled in below needs this catalog's contracts line, not the older
      // one the pinned tools checkout would otherwise resolve `ui-builder-protocol` to.
      implementation(project.dependencies.platform(libs.composeai.contracts.bom))
      implementation("ee.schimke.composeai:ui-builder-protocol")
    }
    jvmTest.dependencies {
      implementation(kotlin("test"))
      @Suppress("DEPRECATION") implementation(compose.desktop.uiTestJUnit4)
      @Suppress("DEPRECATION") implementation(compose.desktop.currentOs)
    }
  }
}

// The authored production policy is the base the pilot keeps its builtins, menu and templates from.
kotlin.sourceSets.named("jvmMain") {
  kotlin.srcDir(rootProject.layout.projectDirectory.dir("catalog-typed-policy/src/main/kotlin"))
}

val catalogRoot = rootProject.layout.projectDirectory
val generationDirectory = layout.buildDirectory.dir("catalog")
val generatedRecords = layout.buildDirectory.dir("generated/typedRecords")
val jvmMain = kotlin.jvm().compilations.getByName("main")

val generateTypedCatalog by
  tasks.registering(JavaExec::class) {
    group = "code generation"
    description = "Discover the compiled Wear entry points and generate the typed pilot pair."
    classpath(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
    mainClass.set("ee.schimke.wearm3catalog.typed.GenerateTypedCatalogKt")
    inputs.files(fileTree(catalogRoot.dir("ui-builder/designs")))
    outputs.dir(generationDirectory)
    outputs.dir(generatedRecords)
    val root = catalogRoot.asFile.absolutePath
    val catalogOutput = generationDirectory
    val recordsOutput = generatedRecords
    argumentProviders.add(
      CommandLineArgumentProvider {
        listOf(
          root,
          catalogOutput.get().asFile.absolutePath,
          recordsOutput.get().asFile.absolutePath,
        )
      }
    )
  }

kotlin.sourceSets.named("wasmJsMain") {
  kotlin.srcDir(files(generatedRecords).builtBy(generateTypedCatalog))
}

tasks.named<Test>("jvmTest") {
  val evidence = layout.buildDirectory.dir("render-evidence")
  systemProperty("typedRenderEvidence", evidence.get().asFile.absolutePath)
  outputs.dir(evidence)
}

val verifyTypedCatalog by tasks.registering {
  group = "verification"
  description = "Generate the typed pilot pair and verify its renderers on the JVM and Wasm."
  dependsOn("jvmTest", generateTypedCatalog, "compileKotlinWasmJs")
  dependsOn(":catalog-typed-consumer:compileKotlin")
}

val uiBuilderCheckout =
  providers.gradleProperty("composeUiBuilderDir").map { rootProject.file(it).canonicalFile }

val runtimeAssets by
  tasks.registering(Sync::class) {
    dependsOn(
      "wasmJsDevelopmentExecutableCompileSync",
      "wasmJsProcessResources",
      "processSkikoRuntimeForKWasm",
    )
    from(layout.buildDirectory.dir("compileSync/wasmJs/main/developmentExecutable/kotlin"))
    from(layout.buildDirectory.dir("compose/skiko-runtime-processed-wasmjs")) {
      include("skiko.mjs", "skiko.wasm")
    }
    from(layout.projectDirectory.dir("src/wasmJsMain/resources")) { include("index.html") }
    from(uiBuilderCheckout.map { it.resolve("assets/js-joda") }) { include("js-joda.esm.js") }
    from(uiBuilderCheckout.map { it.resolve("assets/rc-fonts") }) {
      include("*.ttf", "fonts.json", "*OFL.txt", "LICENSE.txt")
      into("fonts")
    }
    val uiBuilderBuild =
      gradle.includedBuilds.single { it.projectDir.canonicalFile == uiBuilderCheckout.get() }
    dependsOn(uiBuilderBuild.task(":ui-builder-renderer-sdk:generateMaterialIconData"))
    from(
      uiBuilderCheckout.map {
        it.resolve("ui-builder-renderer-sdk/build/generated/materialIconData")
      }
    ) {
      into("icons")
    }
    into(layout.buildDirectory.dir("runtimeAssets"))
  }

// A separate trial identity: the production runtime is `wear-m3-p2-…`.
val runtimeIdentity =
  providers
    .environmentVariable("GITHUB_SHA")
    .map { "wear-m3-typed-pilot-${it.take(12)}" }
    .orElse("wear-m3-typed-pilot-development")
val runtimeDirectory = layout.buildDirectory.dir("wasmRendererDist")

val wasmRendererDist by
  tasks.registering(JavaExec::class) {
    group = "distribution"
    description = "Assemble the typed pilot runtime with its manifest and integrity digest."
    dependsOn(runtimeAssets)
    classpath(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
    mainClass.set("ee.schimke.wearm3catalog.typed.AssembleTypedRuntimeKt")
    inputs.dir(runtimeAssets.map { it.destinationDir })
    inputs.property("runtimeId", runtimeIdentity)
    outputs.dir(runtimeDirectory)
    val assets = runtimeAssets.map { it.destinationDir }
    val output = runtimeDirectory
    val identity = runtimeIdentity
    argumentProviders.add(
      CommandLineArgumentProvider {
        listOf(assets.get().absolutePath, output.get().asFile.absolutePath, identity.get())
      }
    )
  }

val rendererArchive by
  tasks.registering(Zip::class) {
    group = "distribution"
    description = "Package the typed pilot runtime, without attaching it to production delivery."
    dependsOn(wasmRendererDist)
    from(runtimeDirectory)
    archiveFileName.set("wear-m3-typed-pilot-renderer.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }
