import groovy.json.JsonSlurper
import org.gradle.api.publish.PublishingExtension

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.composePreview) apply false
  alias(libs.plugins.ktfmt)
}

val remoteComposeUpstream =
  JsonSlurper().parse(file("vendor/remote-compose-upstream.json")) as Map<*, *>
val remoteComposePortVersion =
  "%s-ps%02d-cmp%02d"
    .format(
      remoteComposeUpstream["change"],
      (remoteComposeUpstream["patchSet"] as Number).toInt(),
      (remoteComposeUpstream["portRevision"] as Number).toInt(),
    )
val publishedRemoteComposeProjects =
  setOf(
    ":vendor:remote-core",
    ":vendor:remote-write-core",
    ":vendor:remote-creation-compose",
    ":vendor:remote-foundation",
    ":vendor:remote-material3",
  )

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

configure(publishedRemoteComposeProjects.map(::project)) {
  group = "ee.schimke.remotecompose"
  version = remoteComposePortVersion
  apply(plugin = "maven-publish")

  extensions.configure<PublishingExtension> {
    repositories {
      // CI pushes this credential-free Maven tree to remote-compose-cmp-maven. Keeping the local
      // destination identical makes publishRemoteComposeToBuildDir the exact preflight for CI.
      maven(rootProject.layout.buildDirectory.dir("remote-compose-maven")) { name = "BuildDir" }

      val githubToken = providers.environmentVariable("GITHUB_TOKEN").orNull
      if (githubToken != null) {
        maven("https://maven.pkg.github.com/yschimke/wear-m3-catalog") {
          name = "GitHubPackages"
          credentials {
            username = providers.environmentVariable("GITHUB_ACTOR").orNull ?: "yschimke"
            password = githubToken
          }
        }
      }
    }
  }
}

tasks.register("publishRemoteComposeToBuildDir") {
  group = "publishing"
  description = "Publish the vendored Remote Compose port into build/remote-compose-maven."
  dependsOn(publishedRemoteComposeProjects.map { "$it:publishAllPublicationsToBuildDirRepository" })
}

tasks.register("printRemoteComposePortVersion") {
  inputs.property("remoteComposePortVersion", remoteComposePortVersion)
  doLast { println(inputs.properties.getValue("remoteComposePortVersion")) }
}
