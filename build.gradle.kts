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
