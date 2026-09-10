plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.composePreview) apply false
  alias(libs.plugins.ktfmt)
}

allprojects {
  apply(plugin = "com.ncorti.ktfmt.gradle")
  ktfmt { googleStyle() }

  // Generated Kotlin is checked in to be COMPILED, not to be read, and reformatting it breaks the
  // only thing it is for: `WidgetExportRoundTripTest` asserts the exporter still produces exactly
  // the text the compiler accepted, and ktfmt rewriting that text makes the golden disagree with
  // its generator the moment anyone runs the formatter. Matched by name rather than by the
  // plugin's task type so this does not need the plugin's classes on the buildscript classpath.
  tasks
    .matching { it.name.startsWith("ktfmt") }
    .configureEach { if (this is SourceTask) exclude("**/remote/generated/**") }
}
