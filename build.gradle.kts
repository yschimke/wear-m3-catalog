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

  // AndroidX sources under vendor/ are pinned upstream bytes. Formatting them here would turn a
  // source import into a repository-wide rewrite and make the next upstream comparison useless.
  // The small JVM adapters intentionally live with that imported code and follow its formatting.
  if (path.startsWith(":vendor:")) {
    tasks.matching { it.name.startsWith("ktfmt") }.configureEach { enabled = false }
  }

  // Generated Kotlin is checked in to be COMPILED, not to be read, and reformatting it breaks the
  // only thing it is for: `WidgetExportRoundTripTest` (the Remote widgets) and
  // `WearScreenTemplateRoundTripTest` (the Wear screens) assert the exporter still produces exactly
  // the text the compiler accepted, and ktfmt rewriting that text makes the golden disagree with
  // its generator the moment anyone runs the formatter. Matched by name rather than by the
  // plugin's task type so this does not need the plugin's classes on the buildscript classpath.
  tasks
    .matching { it.name.startsWith("ktfmt") }
    .configureEach {
      if (this is SourceTask) {
        exclude("**/remote/generated/**")
        exclude("**/uitemplate/generated/**")
      }
    }
}
