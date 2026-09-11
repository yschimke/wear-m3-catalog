pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositories {
    // AndroidX snapshot build for the Remote Compose trio. Keep this build ID and the three
    // 1.0.0-SNAPSHOT version refs in libs.versions.toml together so the artifacts cannot skew.
    maven("https://androidx.dev/snapshots/builds/16323089/artifacts/repository") {
      content {
        includeGroupByRegex("androidx\\.compose\\.remote.*")
        includeGroupByRegex("androidx\\.wear\\.compose\\.remote.*")
        includeGroupByRegex("androidx\\.glance\\.wear.*")
      }
    }
    mavenCentral()
    google()

    // ── The CMP Wear port, GROUP-FENCED ───────────────────────────────────────────────────────
    // `ee.schimke.wearcmp:*` — Wear Compose Material 3 / Foundation compiled for Compose
    // Multiplatform, published from this repository's own `wear-compose-cmp-maven` branch by the
    // port lane on `wear-compose-cmp`. `:catalog` declares it in `commonMain` so the component
    // bodies compile once for every target; the `android` configurations substitute it back to the
    // real `androidx.wear.compose` AARs, so what the Robolectric lane renders — and what the
    // published kit rendition therefore IS — stays the genuine library. See
    // `catalog/build.gradle.kts` for that substitution.
    //
    // Fenced the same way and for the same reason as the snapshot lane below: a settings-level
    // repository is visible to every project, so it is scoped to the one group it can legitimately
    // answer for. `ee.schimke.wearcmp` is a coordinate namespace nothing else in this build or on
    // Maven Central uses, so the filter is exact rather than a prefix guess — this repository can
    // never satisfy a request for an `androidx.*` or `ee.schimke.composeai` artifact even by
    // accident.
    //
    // Serving a build dependency from a git branch of the same repository is a real coupling, and
    // it is deliberate: the port is this project's own artifact, versioned by `portRevision` and
    // gated by the port lane's CI (#413 requires that revision to increase). The alternative —
    // vendoring the port's sources into `:catalog` — would put a fork of Wear Compose in the
    // catalog's history and lose that gate.
    maven("https://raw.githubusercontent.com/yschimke/wear-m3-catalog/wear-compose-cmp-maven/") {
      name = "wearComposeCmpPort"
      content { includeGroup("ee.schimke.wearcmp") }
    }

    // ── The androidx.dev snapshot lane, PINNED IN-TREE and GROUP-FENCED ───────────────────────
    // Selected by `.github/ci/remote-snapshot-pin` — one line, an androidx.dev build id or
    // `latest` — with `-PremoteSnapshot=<id>` as a per-invocation override and an empty or absent
    // pin file meaning the released line.
    //
    // IT READS THE FILE rather than requiring the property because a Gradle property is not
    // reachable from where it has to be. `design-artifacts.yml` renders this catalog through a
    // reusable workflow it cannot pass arguments to, and the one hook it does have —
    // `design-map-command` — is documented as running "before every step that READS the map",
    // which is after the render, not before it. Appending `remoteSnapshot=` to `gradle.properties`
    // there therefore reached the design map and not the stickers: run #164 rendered 392 previews
    // on the released lane at 20:00:40 and projected a 419-preview map at 20:07:49, so the eleven
    // snapshot-only cells were declared with no sticker behind them and every job stayed green.
    // A file the checkout already carries is reachable from every invocation, including that one.
    //
    // The `content` filter is what makes the lane SAFE rather than merely off by default. A
    // settings-level repository is visible to every project, so scoping matters twice over: this
    // one can only ever serve the Remote groups, and `:catalog` depends on none of them
    // (`:catalog:dependencies --configuration debugCompileClasspath` names zero `*.remote.*` or
    // `androidx.glance.wear` modules). So there is no coordinate `:catalog` asks for that this
    // repository is allowed to answer, and the isolation holds by construction rather than by
    // reviewers remembering it.
    //
    // The version substitution that actually selects `1.0.0-SNAPSHOT` is deliberately NOT here —
    // it lives in `remote-catalog/build.gradle.kts` as a resolution strategy on that module's own
    // configurations, which is a second, independent fence: even if a coordinate did become
    // shared, `:catalog` would keep resolving the pinned alpha.
    // A PRESENT property wins outright, blank included — `-PremoteSnapshot=` is how you force the
    // released lane for one invocation now that the pin file is on by default, and it can only mean
    // that if a blank property is distinguished from an absent one before the file is consulted.
    val remoteSnapshotProperty = providers.gradleProperty("remoteSnapshot").orNull
    val remoteSnapshot =
      if (remoteSnapshotProperty != null) {
        remoteSnapshotProperty.takeIf { it.isNotBlank() }
      } else {
        rootDir
          .resolve(".github/ci/remote-snapshot-pin")
          .takeIf { it.isFile }
          ?.readText()
          ?.trim()
          ?.takeIf { it.isNotBlank() }
      }
    if (remoteSnapshot != null) {
      val path = if (remoteSnapshot == "latest") "latest" else "builds/$remoteSnapshot"
      maven("https://androidx.dev/snapshots/$path/artifacts/repository") {
        content {
          includeGroupByRegex("androidx\\.compose\\.remote.*")
          includeGroupByRegex("androidx\\.wear\\.compose\\.remote.*")
          // Glance Wear is the THIRD group and is opted into separately — see
          // `remoteSnapshotGlance` in remote-catalog/build.gradle.kts for the incompatibility
          // that earned it its own switch. The filter has to admit the group for the
          // substitution over there to have anywhere to resolve from, so the two properties are
          // read in both files.
          if (providers.gradleProperty("remoteSnapshotGlance").orNull == "true") {
            includeGroupByRegex("androidx\\.glance\\.wear.*")
          }
        }
      }
    }
  }
}

rootProject.name = "wear-m3-catalog"

include(":catalog")

// The same component bodies, drawn by Compose Multiplatform Desktop instead of Robolectric. It
// declares no previews: it names `:catalog` in `composePreviewSource` and renders that module's
// `commonMain` stickers on its own lane, because the plugin allows one lane per module and
// `:catalog`'s is Robolectric. Its renders are NOT the kit rendition — see
// catalog-desktop/build.gradle.kts.
include(":catalog-desktop")

// The Remote Compose rendition of the same Wear surface — the third column of the comparison this
// repo publishes. Separate module, not a source set: it is on the alpha Remote Compose line at
// compileSdk 37 with no Compose BOM, and that must not reach `:catalog`. See
// remote-catalog/build.gradle.kts.
include(":remote-catalog")

// The AndroidX Wear samples rendition — `androidx.wear.compose.material3`'s own `@Sampled`
// composables, vendored from a pinned upstream commit and rendered beside the kit catalog.
// Separate module, not a source set: the vendored sources are upstream's bytes under upstream's
// package, and must not be formatted, linted or refactored with this repo's own code.
// See docs/design/ANDROIDX_SAMPLES.md.
include(":samples-catalog")
