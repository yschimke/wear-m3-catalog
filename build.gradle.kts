import groovy.json.JsonSlurper

// The published coordinates track upstream: `1.7.0-beta02-cmp01` is the first port of AndroidX
// 1.7.0-beta02. Both halves come from upstream.json, so a version bump is still a one-file edit
// and no consumer has to guess which AndroidX release an artifact came from.
val upstream = JsonSlurper().parse(file("upstream.json")) as Map<*, *>
val portVersion = "${upstream["version"]}-cmp%02d".format((upstream["portRevision"] as Number).toInt())

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.compose.compiler) apply false
}

subprojects {
  group = "ee.schimke.wearcmp"
  version = portVersion

  // Every module publishes: `:port-runtime` carries the `expect`s the other three compile against,
  // so a consumer that resolves one resolves all four.
  apply(plugin = "maven-publish")

  extensions.configure<PublishingExtension> {
    repositories {
      // A self-contained Maven repository under the root build directory. `publishToBuildDir`
      // fills it, and CI pushes it to the `wear-compose-cmp-maven` branch, which is then a real
      // Maven repository served over raw.githubusercontent with no credentials — the form a wasm
      // consumer can actually resolve from. Everything else here needs a token.
      maven(rootProject.layout.buildDirectory.dir("maven")) { name = "BuildDir" }

      // GitHub Packages, when the environment carries credentials for it. Absent those — a local
      // build, a fork's CI — publishing still works to mavenLocal, which is what the UI builder
      // consumes while this is pre-release.
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

  // Every module is the same shape — the same two targets and the same opt-ins — because every
  // module is the same transform run over a different artifact. Configuring it here keeps each
  // module's build file down to its dependencies, which are the only thing that actually differs.
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
      jvm()
      @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
      wasmJs {
        browser {
          // These are libraries, and there are no wasm tests to run. The browser test task is off
          // because standing it up downloads a karma tarball from codeload.github.com at
          // configuration time — a network dependency that fails a sandboxed or firewalled build
          // on a target that has nothing to test. Add tests and this comes back on.
          testTask { enabled = false }
        }
      }

      // Both published targets render through Skia — wasmJs via skiko-wasm, the JVM via
      // skiko-awt — so `skikoMain` is where the port can reach the graphics stack Compose itself
      // draws with. That is not a detail: curved text and named font families are IMPOSSIBLE in
      // common code (Compose exposes no glyph positions and no font-by-name lookup) and ordinary
      // in Skia, which has RSXform and FontMgr for exactly these two jobs.
      //
      // It is wired by hand because the default hierarchy has no intermediate source set for
      // "jvm and wasmJs and nothing else". Adding an Android or a JS target later would need a
      // decision here rather than inheriting one silently, which is the right way round.
      applyDefaultHierarchyTemplate()
      val skikoMain = sourceSets.create("skikoMain") { dependsOn(sourceSets.getByName("commonMain")) }
      sourceSets.getByName("jvmMain") { dependsOn(skikoMain) }
      sourceSets.getByName("wasmJsMain") { dependsOn(skikoMain) }

      // The port's own common code: the `expect` declarations the patches in `patches/` rewire
      // the generated sources onto. It is a second directory rather than a file in
      // `src/commonMain/kotlin` because transform.py deletes that directory wholesale on every
      // run — generated and hand-written source cannot share a root and both survive.
      sourceSets.commonMain { kotlin.srcDir("src/commonPort/kotlin") }

      // AndroidX compiles its own sources with these opt-ins; the code is full of call sites that
      // assume them. They are not a porting decision — upstream's build.gradle carries the same
      // list — so they belong to the harness rather than to any one module.
      sourceSets.all {
        languageSettings {
          optIn("androidx.compose.animation.core.ExperimentalAnimationSpecApi")
          optIn("androidx.compose.foundation.ExperimentalFoundationApi")
          optIn("androidx.compose.material.ExperimentalMaterialApi")
          optIn("androidx.compose.ui.ExperimentalComposeUiApi")
          optIn("androidx.compose.ui.text.ExperimentalTextApi")
          optIn("kotlin.contracts.ExperimentalContracts")
        }
      }
    }
  }
}


// One task to produce the whole repository, so CI (and a local check of what CI would publish)
// does not have to know the publication names.
tasks.register("publishToBuildDir") {
  group = "publishing"
  description = "Publish every module into build/maven — a self-contained Maven repository."
  dependsOn(subprojects.map { "${it.path}:publishAllPublicationsToBuildDirRepository" })
}

// CI reads the version from here rather than re-deriving it from upstream.json in shell.
tasks.register("printPortVersion") { doLast { println(portVersion) } }
