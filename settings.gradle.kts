pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()

    // ── The CMP Wear port, GROUP-FENCED ───────────────────────────────────────────────────────
    // `ee.schimke.wearcmp:*` — Wear Compose Material 3 / Foundation compiled for Compose
    // Multiplatform, published to the `wear-compose-cmp-maven` branch of the output repository,
    // `yschimke/wear-m3-catalog-out`, by the port lane on `wear-compose-cmp`. `:catalog` declares
    // it in `commonMain` so the component bodies compile once for every target; the `android`
    // configurations substitute it back to the real `androidx.wear.compose` AARs, so what the
    // Robolectric lane renders — and what the published kit rendition therefore IS — stays the
    // genuine library. See `catalog/build.gradle.kts` for that substitution.
    //
    // Fenced the same way and for the same reason as the snapshot lane below: a settings-level
    // repository is visible to every project, so it is scoped to the one group it can legitimately
    // answer for. `ee.schimke.wearcmp` is a coordinate namespace nothing else in this build or on
    // Maven Central uses, so the filter is exact rather than a prefix guess — this repository can
    // never satisfy a request for an `androidx.*` or `ee.schimke.composeai` artifact even by
    // accident.
    //
    // Serving a build dependency from a git branch of this project's own output repository is a
    // real coupling, and it is deliberate. The branch lives beside the design artifacts in
    // `wear-m3-catalog-out` rather than here, so this source repository carries no generated
    // branch (#604): the port is this project's own artifact, versioned by `portRevision` and
    // gated by the port lane's CI (#413 requires that revision to increase). The alternative —
    // vendoring the port's sources into `:catalog` — would put a fork of Wear Compose in the
    // catalog's history and lose that gate.
    maven(
      "https://raw.githubusercontent.com/yschimke/wear-m3-catalog-out/wear-compose-cmp-maven/"
    ) {
      name = "wearComposeCmpPort"
      content { includeGroup("ee.schimke.wearcmp") }
    }
  }
}

rootProject.name = "wear-m3-catalog"

include(":catalog")

// Catalog renderer builds consume the renderer SDK from source. Opt in explicitly so ordinary
// catalog builds keep resolving exactly as before:
//
//   ./gradlew :catalog-ui-builder-renderer:rendererArchive \
//     -PcomposeUiBuilderDir=../compose-ui-builder
//
// The synthetic coordinate is intentionally absent from Maven, so asking for a renderer without
// this checkout fails closed rather than silently compiling against a different release.
providers.gradleProperty("composeUiBuilderDir").orNull?.let { path ->
  val directory = file(path).canonicalFile
  require(directory.resolve("settings.gradle.kts").isFile) {
    "-PcomposeUiBuilderDir names $directory, which is not a compose-ui-builder Gradle checkout."
  }
  logger.lifecycle("Composite build: uiBuilder -> $directory")
  includeBuild(directory) {
    dependencySubstitution {
      substitute(module("ee.schimke.composeai:ui-builder-renderer-sdk-source"))
        .using(project(":ui-builder-renderer-sdk"))
    }
  }
}

// The catalog-owned renderer links the source-only SDK. Keep it outside ordinary catalog builds:
// without the composite there is deliberately no Maven fallback for that coordinate.
if (providers.gradleProperty("composeUiBuilderDir").isPresent) {
  include(":catalog-ui-builder-renderer")
  include(":ui-builder-foundation-adapters")
  include(":ui-builder-wear-adapters")
}

// The same component bodies, drawn by Compose Multiplatform Desktop instead of Robolectric. It
// declares no previews: it names `:catalog` in `composePreviewSource` and renders that module's
// `commonMain` stickers on its own lane, because the plugin allows one lane per module and
// `:catalog`'s is Robolectric. Its renders are NOT the kit rendition — see
// catalog-desktop/build.gradle.kts.
include(":catalog-desktop")

// The Remote Compose rendition of the same Wear surface — the `remote-m3` system, the third column
// of the comparison — lives in yschimke/remote-m3-catalog, together with the vendored Remote
// Compose CMP port it builds against. It pairs with `:catalog` through `parallel` declarations on
// its own side, and holds the pairing gate there.

// The AndroidX Wear samples rendition — `androidx.wear.compose.material3`'s own `@Sampled`
// composables, vendored from a pinned upstream commit and rendered beside the kit catalog.
// Separate module, not a source set: the vendored sources are upstream's bytes under upstream's
// package, and must not be formatted, linted or refactored with this repo's own code.
// See docs/design/ANDROIDX_SAMPLES.md.
include(":samples-catalog")
