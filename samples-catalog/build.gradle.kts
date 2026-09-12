// `:samples-catalog` — the AndroidX Wear Compose material3 samples, rendered.
//
// An **Android** module, like `:catalog` and for the same reason:
// `androidx.wear.compose:compose-material3` ships only for Android, so there is no desktop target
// to render on and the compose-preview plugin routes this to Robolectric. That also keeps both
// sides of the compare page on ONE rasteriser, so a sample beside its kit sticker shows an
// API-usage difference rather than a renderer difference.
//
// Deliberately the SIMPLE Android lane (`android.application`), not `:catalog`'s KMP module with
// its CMP Wear port substitution. That machinery exists so one set of component bodies can compile
// for more than one target; these are upstream's samples, they are Android-only by construction,
// and there is nothing to port.
//
// Everything under `src/main/kotlin/upstream/` is VENDORED: upstream's bytes, in upstream's
// package (`androidx.wear.compose.material3.samples`), fetched by `scripts/import-samples.mjs`
// from the commit pinned in `samples/import.json`. Never edited in place and never formatted — a
// fix is a patch in `samples/patches/` with a stated reason. See docs/design/ANDROIDX_SAMPLES.md.
//
// The samples carry `@Preview` upstream, so discovery finds them directly and nothing generates a
// wrapper. What an imported project cannot declare for itself is a theme, which
// `catalog.spec.json` supplies through its `themes` block.
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.composePreview)
}

composePreview {
  // Robolectric SDK 35, which runs on the JDK 17 toolchain below. Same pin `:catalog` and
  // `:remote-catalog` carry.
  sdkVersion.set(35)
}

android {
  // UPSTREAM'S OWN PACKAGE, deliberately. AGP generates `R` into the module's `namespace`, and the
  // vendored samples reference an unqualified `R` from inside
  // `androidx.wear.compose.material3.samples` — so the generated class has to land in that package
  // for `R.drawable.ic_wifi` to resolve. Setting it here costs nothing and keeps the sources
  // byte-identical; the alternative is an import patched into every file that draws artwork, which
  // is a patch per file carrying no fix.
  //
  // `applicationId` below stays this repo's own: namespace and applicationId are independent, and
  // only the former decides where `R` is generated.
  namespace = "androidx.wear.compose.material3.samples"
  // wear-compose 1.7.0-beta requires compileSdk 37, as `:catalog` documents.
  compileSdk = 37

  defaultConfig {
    applicationId = "ee.schimke.wearm3catalog.samples"
    minSdk = 30
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures { compose = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin {
  compilerOptions {
    // AndroidX compiles its own samples with these opt-ins: a sample demonstrating an experimental
    // API cannot avoid naming it. Opting in here rather than patching every file keeps the vendored
    // tree byte-identical, which is the whole bargain.
    optIn.addAll(
      "androidx.wear.compose.material3.ExperimentalWearMaterial3Api",
      "androidx.wear.compose.foundation.ExperimentalWearFoundationApi",
      "androidx.compose.material3.ExperimentalMaterial3Api",
      "androidx.compose.foundation.ExperimentalFoundationApi",
      "androidx.compose.ui.ExperimentalComposeUiApi",
      "androidx.compose.animation.ExperimentalAnimationApi",
      "kotlin.ExperimentalStdlibApi",
    )
  }
}

dependencies {
  implementation(platform(libs.compose.bom))
  implementation(libs.wear.compose.material3)
  implementation(libs.wear.compose.foundation)
  implementation(libs.wear.compose.ui.tooling)
  implementation(libs.wear.tooling.preview)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material.icons.extended)
  // `LocalOnBackPressedDispatcherOwner`, read by the one-handed-gesture samples. Upstream's own
  // samples build.gradle declares activity-compose for exactly this.
  implementation(libs.androidx.activity.compose)

  // NOT used by any code in this module -- the vendored samples carry plain `@Preview`, and the
  // inventory lives in `catalog.spec.json` rather than in `@CatalogComponent` annotations. It is
  // here to pin a version that EXISTS.
  //
  // `catalog.spec.json` declares `themes`, so the design-artifacts lane runs
  // `generate-theme-catalogs.mjs` over this module in its throwaway checkout. That generator
  // appends `ee.schimke.composeai:preview-annotations:$(compose-preview --version)` -- the CLI's
  // version, on the belief that "the two ship from one release". They no longer do:
  // `preview-annotations` moved to the compose-preview-daemon line, so the CLI's 2.9.0 is a
  // coordinate that 404s while the daemon's 3.4.1 is real. Declaring the daemon version here lets
  // Gradle's newest-wins conflict resolution select it, and the injected 2.9.0 is never fetched.
  //
  // Reproduced by appending that exact line locally: without this, `debugRuntimeClasspath` reports
  // `preview-annotations:2.9.0 FAILED`; with it, 2.9.0 -> 3.4.1 and resolution succeeds. The phone
  // repo's samples module has carried this dependency from the start, which is the only reason
  // `m3-samples` published and this sheet did not.
  implementation(libs.composeai.preview.annotations)
}

// Two directories this module's formatter must not touch, for two different reasons.
//
// `upstream/` is upstream's bytes. ktfmt would rewrite it into a permanent diff against every
// future import — the formatting counterpart of "a fix is a patch, never an edit".
//
// `SamplePreviews.kt` is `scripts/samples-previews.mjs`'s output, and its canonical form is
// whatever that script emits: `--check` regenerates and diffs, exactly as `design-map.json` is
// checked. Letting a formatter rewrite it would put the two checks in direct conflict — ktfmt wraps
// the long fully-qualified calls at 100 columns, the generator does not, and whichever ran last
// would make the other fail. The generator is the single source of truth, so the formatter stays
// out. Named by FILE rather than by a `generated/` directory because it now lives under the
// directories its own package names, beside hand-written sources it must not drag out of the
// formatter with it.
//
// The root build applies ktfmt to every project, so this narrows its inputs here.
tasks.withType<com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask>().configureEach {
  exclude {
    val path = it.file.absolutePath.replace('\\', '/')
    path.contains("/src/main/kotlin/upstream/") ||
      path.endsWith("/src/main/kotlin/ee/schimke/wearm3catalog/samples/SamplePreviews.kt")
  }
}
