import org.gradle.api.attributes.Bundling

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.composePreview) apply false
  alias(libs.plugins.ktfmt)
}

allprojects {
  apply(plugin = "com.ncorti.ktfmt.gradle")
  ktfmt { googleStyle() }

  // Generated Kotlin is checked in to be COMPILED, not to be read, and reformatting it breaks the
  // only thing it is for: `WearScreenTemplateRoundTripTest` asserts the exporter still produces
  // exactly the text the compiler accepted, and ktfmt rewriting that text makes the golden disagree
  // with its generator the moment anyone runs the formatter. Matched by name rather than by the
  // plugin's task type so this does not need the plugin's classes on the buildscript classpath.
  tasks
    .matching { it.name.startsWith("ktfmt") }
    .configureEach {
      if (this is SourceTask) {
        exclude("**/uitemplate/generated/**")
      }
    }
}

// The UI-builder renderer graph is intentionally absent unless `composeUiBuilderDir` enables its
// source composite (settings.gradle.kts). Its Kotlin still needs the same formatter on ordinary CI
// runs, so this root task invokes ktfmt directly over those source trees without adding their
// unresolvable synthetic SDK dependency to the build graph.
val uiBuilderRendererKtfmt by configurations.creating {
  attributes {
    attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class, Bundling.SHADOWED))
  }
}

dependencies { uiBuilderRendererKtfmt(libs.ktfmt.cli) }

val uiBuilderRendererKotlinSources =
  files(
    fileTree("catalog-ui-builder-renderer") { include("src/**/*.kt") },
    fileTree("ui-builder-foundation-adapters") { include("src/**/*.kt") },
    fileTree("ui-builder-wear-adapters") { include("src/**/*.kt") },
  )
val ktfmtCheckUiBuilderRendererSources by
  tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Checks the opt-in UI-builder renderer sources with ktfmt."
    classpath = uiBuilderRendererKtfmt
    mainClass.set("com.facebook.ktfmt.cli.Main")
    inputs.files(uiBuilderRendererKotlinSources).withPathSensitivity(PathSensitivity.RELATIVE)
    args("--google-style", "--dry-run", "--set-exit-if-changed")
    args(uiBuilderRendererKotlinSources.files)
  }

tasks.named("ktfmtCheck") { dependsOn(ktfmtCheckUiBuilderRendererSources) }

// composePreviewDaemon (the compose-preview-daemon-bom version every ee.schimke.composeai runtime
// module resolves through) must equal the daemon release the compose-preview plugin bakes into its
// jar (`previewDaemon` in plugin-version.properties): the catalogs compile against the same runtime
// the plugin's renderer hosts. Renovate cannot read that value, so it never moves the BOM on its
// own (.github/renovate.json); this check is what keeps the two equal when the plugin moves.
val bakedPreviewDaemonVersion: String =
  javaClass.classLoader
    .getResource("ee/schimke/composeai/plugin/plugin-version.properties")
    ?.openStream()
    ?.use { java.util.Properties().apply { load(it) } }
    ?.getProperty("previewDaemon") ?: "<missing from the plugin jar>"

val verifyComposePreviewDaemonAlignment by tasks.registering {
  group = "verification"
  description = "Fail when composePreviewDaemon differs from the compose-preview plugin's daemon."
  val plugin = libs.versions.composePreviewPlugin.get()
  val catalog = libs.versions.composePreviewDaemon.get()
  val bakedPreviewDaemon = bakedPreviewDaemonVersion
  inputs.property("baked", bakedPreviewDaemon)
  inputs.property("catalog", catalog)
  doLast {
    check(bakedPreviewDaemon == catalog) {
      "gradle/libs.versions.toml has composePreviewDaemon = \"$catalog\", but compose-preview " +
        "plugin $plugin bakes daemon \"$bakedPreviewDaemon\" (plugin-version.properties). " +
        "Set composePreviewDaemon = \"$bakedPreviewDaemon\"."
    }
  }
}

// Every module that applies the compose-preview plugin runs the check before it compiles, so a
// plugin bump that bakes a different daemon fails its own PR rather than skewing silently.
subprojects {
  pluginManager.withPlugin("ee.schimke.composeai.preview") {
    tasks
      .matching { it.name == "preBuild" || it.name.startsWith("compileKotlin") }
      .configureEach { dependsOn(verifyComposePreviewDaemonAlignment) }
  }
}
