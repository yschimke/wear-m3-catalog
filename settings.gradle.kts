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

    // ── The androidx.dev snapshot lane, OPT-IN and GROUP-FENCED ───────────────────────────────
    // Off unless `-PremoteSnapshot=<androidx.dev build id>` (or `latest`) is passed, so the
    // committed build still resolves the released alphas `gradle/libs.versions.toml` pins and no
    // unreleased artifact reaches `main` — the skew AGENTS.md forbids.
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
    val remoteSnapshot =
      providers.gradleProperty("remoteSnapshot").orNull?.takeIf { it.isNotBlank() }
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

// The Remote Compose rendition of the same Wear surface — the third column of the comparison this
// repo publishes. Separate module, not a source set: it is on the alpha Remote Compose line at
// compileSdk 37 with no Compose BOM, and that must not reach `:catalog`. See
// remote-catalog/build.gradle.kts.
include(":remote-catalog")
