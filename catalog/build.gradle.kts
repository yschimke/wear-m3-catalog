// `:catalog` — the M3 Wear OS Apps Design Kit rebuilt as design-led `@Preview`s.
//
// **Kotlin Multiplatform**, with the component bodies in `commonMain` and the Robolectric render
// lane on the `android` target. It was an Android application module until this change, and the
// reason it could be one is the reason it no longer needs to be: the manifest said outright that
// the catalog is "a render target, not a shipped watch app… it exists so the Robolectric renderer
// has an Android application to compose the `@Preview`s inside". Since compose-ai-tools#5375 the
// Robolectric lane runs on a `com.android.kotlin.multiplatform.library` module, so the application
// wrapper bought nothing and cost a source set that could not be shared.
//
// ── What renders the kit ──────────────────────────────────────────────────────────────────────
//
// The published rendition is still drawn by the REAL AndroidX library. `commonMain` compiles
// against the CMP Wear port (`ee.schimke.wearcmp`, from this repository's own
// `wear-compose-cmp-maven` branch — see `settings.gradle.kts` for the fenced repository), and every
// `android` configuration substitutes that straight back to `androidx.wear.compose`. The port is
// what makes ONE set of component bodies compile for more than one target; it is not what the kit
// is measured against, and the substitution below is what keeps those two facts apart.
//
// Same FQNs on both sides (`androidx.wear.compose.material3.Button` either way) is what makes the
// substitution invisible to the source. That is a property of the port, not a coincidence: it is
// built by rewriting the AndroidX sources, not by re-authoring them.
//
// ── The six Android-only files ────────────────────────────────────────────────────────────────
//
// `androidMain` holds what genuinely cannot leave Android: `HorologistSamples.kt` and the four
// sections drawn with Horologist (`Auth`, `FastScrolling`, `MediaControls`, `Motion`), whose
// artifacts are Android AARs with no multiplatform line, and `CatalogFonts.kt`, which resolves its
// typefaces as DOWNLOADABLE Google fonts through `ui-text-google-fonts` — an Android-only provider
// backed by a system font provider, not a library that could be ported.
//
// They render exactly as before. What they lose is only the ability to appear on a non-Android
// tier, which is the honest cost of drawing them with Android-only libraries.
//
// ── The inventory ─────────────────────────────────────────────────────────────────────────────
//
// The catalog's inventory lives in **annotations next to the previews** (`@CatalogGroup` /
// `@CatalogComponent` / `@CatalogVariant` / `@OverrideVariant`). `catalog.spec.json` carries only
// the cover-sheet fields.
plugins {
  // KGP-multiplatform, the KMP-Android plugin and `kotlin.plugin.compose` all ship inside the same
  // buildscript-classpath bundle AGP 9 brings in for any module already applying an AGP plugin
  // elsewhere in the build (`:remote-catalog` does). Applying them via `alias(libs.plugins…)`
  // errors with "already on the classpath with an unknown version, so compatibility cannot be
  // checked" — so they go by id, and Gradle resolves the unknown-version entry from that bundle.
  // Same treatment as compose-ai-tools' own `:samples:cmp-shared`.
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
  alias(libs.plugins.composePreview)
}

composePreview {
  // Robolectric SDK 35, which runs on the JDK 17 toolchain below. SDK 36 requires JDK 21+.
  sdkVersion.set(35)

  // The opt-in that keeps this module on Robolectric. A KMP-Android module takes the CMP Desktop
  // lane by default (compose-ai-tools#248), and Desktop would render these previews against the
  // PORT rather than against AndroidX — the one thing this module must not publish. Without this
  // line the build stays green and the kit rendition quietly changes what it is measuring.
  kmpAndroidRobolectric = true

  // `CatalogRenderTest` reads the real renderer output to prove no sticker publishes an empty
  // frame — the one failure mode a green build, a successful render and a human reviewer all miss
  // on a dark-first catalog. Rendering first is what makes it a test of the artifact rather than of
  // a fixture; it costs the test job one render pass (~15s at this size).
  renderBeforeUnitTests.set(true)
}

// ── One font file, two lanes ───────────────────────────────────────────────────────────────────
//
// The variable Roboto Flex is committed ONCE, under `src/androidMain/res/font/`, because that is
// where AGP has to find it to make `R.font.roboto_flex`. The desktop compilation cannot read an
// Android resource, so the file is republished into the desktop source set's resources under a
// package-shaped path — `ee/schimke/wearm3catalog/fonts/` rather than the jar root, so a
// dependency of `:catalog-desktop` cannot collide with it.
//
// A second committed copy would have been three fewer lines and one more thing to forget: the two
// lanes are compared sticker for sticker, and that comparison is only about the component if both
// sides are drawing with the identical face.
val desktopFontResources by
  tasks.registering(Sync::class) {
    description = "Republishes the vendored Roboto Flex onto the desktop compilation's classpath."
    from(layout.projectDirectory.dir("src/androidMain/res/font")) {
      into("ee/schimke/wearm3catalog/fonts")
    }
    into(layout.buildDirectory.dir("generated/desktopFontResources"))
  }

kotlin {
  // AGP 9 / KMP names this block `android { }` (it was `androidLibrary { }` in earlier previews).
  android {
    namespace = "ee.schimke.wearm3catalog"
    // wear-compose 1.7.0-beta requires compileSdk 37.
    compileSdk = 37
    minSdk = 30

    // The Robolectric lane merges the unit-test `R.jar` and needs resources enabled to do it; the
    // plugin turns this on itself when `kmpAndroidRobolectric` is set, and it is repeated here
    // because `CatalogFonts.kt` reads `res/font` and `res/values` directly.
    androidResources.enable = true

    withHostTest {
      // Robolectric resolves the merged resources through `apk-for-local-test.ap_`, which AGP only
      // writes when the host-test variant includes Android resources.
      isIncludeAndroidResources = true
    }

    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
      }
    }
  }

  // JVM target so `commonMain` is genuinely compiled for something other than Android — the check
  // that keeps the shared source honest. Nothing RENDERS it yet: the plugin registers one lane per
  // module and this one is Robolectric. The desktop sticker sheet is a sibling module consuming
  // these previews through `composePreviewSource` (compose-ai-tools#5402), which needs a plugin
  // release carrying it; until then this target's job is to fail the build the moment a component
  // body reaches for something only Android has.
  jvm("desktop")

  compilerOptions {
    // Horologist marks essentially its whole surface `@ExperimentalHorologistApi`, which is a
    // `@RequiresOptIn` at the default ERROR level — `MediaUiModel`, `TrackPositionUiModel`,
    // `PlayerScreen` and the auth screens all carry it. Opting in module-wide rather than
    // annotating each sticker: the alternative is an `@OptIn` on every composable in three files,
    // which says nothing a reader does not already know from the `Horologist` section they are in.
    // It is genuinely experimental — the artifacts are on an alpha line and Renovate holds their
    // bumps for a human to read the visual diff (see .github/renovate.json).
    optIn.add("com.google.android.horologist.annotations.ExperimentalHorologistApi")
  }

  sourceSets {
    commonMain.dependencies {
      @Suppress("DEPRECATION") implementation(compose.runtime)
      @Suppress("DEPRECATION") implementation(compose.foundation)
      @Suppress("DEPRECATION") implementation(compose.animation)
      @Suppress("DEPRECATION") implementation(compose.materialIconsExtended)
      // The JetBrains-relocated androidx artifact, which ships
      // `androidx.compose.ui.tooling.preview.Preview` on every target — the FQN discovery scans
      // for. The CMP-bundled `compose.components.uiToolingPreview` republishes it under
      // `org.jetbrains.compose.…` instead, which discovery does not recognise.
      implementation(libs.compose.multiplatform.ui.tooling.preview)

      // MOBILE Material 3, for `MaterialShapes` and `RoundedPolygon.toShape()` ONLY — the CMP
      // flavour of the same pin the Android build already carried.
      //
      // The kit's Shapes page publishes the 35 expressive shapes — Circle, Square, … Heart — and
      // Wear Compose has no shape library that names them: `androidx.wear.compose.material3` ships
      // `ShapeDefaults` (corner radii) and `AnimatedMorphShape`, and
      // `androidx.graphics:graphics-shapes` ships only the polygon primitives the shapes are BUILT
      // from. `MaterialShapes` itself is plain `RoundedPolygon` data — no mobile theming, no mobile
      // component pulled into a render. So the specimen sheet draws Material's own finished
      // polygons rather than this repo's arithmetic, which is the whole point of a design-led
      // catalog.
      implementation(libs.compose.multiplatform.material3)

      // The CMP Wear port. SUBSTITUTED BACK to `androidx.wear.compose` on every android
      // configuration below — this coordinate is how the bodies compile for more than one target,
      // not what the kit is rendered against.
      implementation(libs.wearcmp.compose.material3)
      implementation(libs.wearcmp.compose.foundation)

      // The declared themes in `CatalogThemes.kt`: materialkolor builds the conference palettes
      // from their seed colours the way Confetti Wear itself does, rather than this repo
      // transcribing the resolved roles by hand.
      implementation(libs.materialkolor)

      // `@CatalogGroup`, `@CatalogComponent`, `@CatalogVariant`, `@OverrideVariant`, and the token
      // catalog annotations — multiplatform since compose-preview-daemon 3.0.0.
      implementation(libs.composeai.preview.annotations)
      // `previewOverride*` — the knob surface `@OverrideVariant` seeds, so ONE `@Preview` can carry
      // a whole variant matrix instead of one near-identical composable per cell. Multiplatform
      // since compose-preview-daemon#58; before that it was the single artifact keeping these
      // bodies out of `commonMain`.
      implementation(libs.composeai.preview.overrides)
    }

    androidMain.dependencies {
      // MOBILE Material 3, at the androidx pin this module has always used.
      //
      // `commonMain` gets `MaterialShapes` from the CMP flavour, and the CMP 1.12.0-alpha03 line
      // carries androidx material3 **1.5.0-alpha22** — OLDER than the 1.5.0-alpha27 this module
      // pins. Letting that win silently downgraded the library on the android target, and
      // material3 supplies the ripple as well as the shapes: 455 of 1161 stickers changed bytes,
      // with no error anywhere. Declaring the androidx pin here puts it back — Gradle takes the
      // higher version — so what Robolectric renders is the same material3 as before the split.
      //
      // The two pins are one decision and must move together; `compose-material3` and
      // `compose-multiplatform-material3` in the version catalog say so beside each other.
      implementation(libs.compose.material3)

      // Wear tooling preview annotations (`@WearPreviewDevices`) — Android-only, and only the
      // android compilation needs them.
      implementation(libs.wear.compose.ui.tooling)
      implementation(libs.wear.tooling.preview)

      // `ui-text-google-fonts` resolves `CatalogFonts.kt`'s typefaces — Roboto Flex, Inter,
      // JetBrains Mono, Google Sans Flex — as DOWNLOADABLE Google fonts through an Android system
      // font provider, so no TTF is vendored here. There is no multiplatform equivalent; that is
      // why the file is in `androidMain`.
      implementation(libs.compose.ui.text.google.fonts)

      // HOROLOGIST — the second library on the sheet, and the reason there is a `Horologist`
      // section.
      //
      // Wear Compose Material 3 stops at the component set; the kit does not. Its `Media-Player`
      // set is a whole screen, and the catalog's answer to it used to be an exclusion reading
      // "assembled by an app (or by Horologist), not a library component" — which was true of Wear
      // Compose and false of the ecosystem: Horologist publishes exactly that screen, and the parts
      // it is built from, as library components. Same for the sign-in screens and the
      // fast-scrolling list.
      //
      // The `*-material3` artifacts only. Horologist still ships its original Material 2 line under
      // the un-suffixed names (`horologist-media-ui`, `horologist-auth-composables`), and a sticker
      // drawn from those would be comparing the kit against the wrong design system.
      implementation(libs.horologist.media.ui.material3)
      implementation(libs.horologist.media.ui.model)
      // The player's FOOTER. `PlayerScreen`'s `buttons` slot ships empty, and the two compact
      // buttons the kit draws in it — output device with a volume badge, and overflow — are
      // published here rather than in `media-ui`. Leaving this off the classpath is how the slot
      // came to hold a playlist chip instead (issue #67); see the note in
      // `sections/MediaControls.kt`.
      implementation(libs.horologist.audio.ui.material3)
      implementation(libs.horologist.auth.composables.material3)
      implementation(libs.horologist.compose.layout)
      implementation(libs.horologist.images.base)
    }

    // The desktop lane draws with the SAME vendored variable Roboto Flex the Android lane does,
    // read as a classpath resource — see `CatalogFonts.desktop.kt`. `desktopFontResources` above
    // is what puts the one committed TTF there; this is the source set that consumes it.
    getByName("desktopMain").resources.srcDir(desktopFontResources)

    getByName("androidHostTest").dependencies {
      implementation(libs.junit)
      // `kit-sets.json` is read by CatalogKitCoverageTest. org.json ships in the Android SDK as
      // stubs only, so the unit test needs the real implementation on its own classpath.
      implementation("org.json:json:20250517")
      implementation(libs.robolectric)
      implementation(project.dependencies.platform(libs.compose.bom))
      implementation(libs.compose.ui.test.junit4)
      // `createComposeRule` launches a `ComponentActivity`, which has to be in the manifest
      // Robolectric resolves against. On the KMP-Android plugin there is no `debugImplementation`
      // to merge it through, so the host-test source set declares it directly.
      implementation(libs.compose.ui.test.manifest)
    }
  }
}

// ── The port → AndroidX substitution ────────────────────────────────────────────────────────────
//
// `commonMain` declares the CMP port so the component bodies compile for every target. Every
// ANDROID configuration swaps it back for the real AndroidX AARs, so the Robolectric renders — the
// published kit rendition — are drawn by the library this catalog exists to reproduce.
//
// Matched by configuration-name prefix rather than by naming each one: the KMP-Android plugin names
// configurations after the KMP TARGET and compilation (`androidRuntimeClasspath`,
// `androidHostTestRuntimeClasspath`, `androidMainImplementation`, …) and adds more of them across
// AGP versions. A prefix catches the ones that exist now and the ones that arrive later; a list
// would silently stop covering a new configuration, and a render against the port would look
// exactly like a render against AndroidX until someone compared pixels.
// Read once, outside the `configureEach`: the version-catalog accessor is not reachable from the
// configuration-container scope below. `asProvider()` because `wear-compose-remote` makes
// `wear.compose` an accessor GROUP rather than a leaf — reading it without that is an unresolved
// reference whose message names neither the catalog nor the sibling key that caused it.
val wearComposeVersion = libs.versions.wear.compose.asProvider().get()

configurations
  .matching { it.name.startsWith("android") }
  .configureEach {
    resolutionStrategy.dependencySubstitution {
      substitute(module("ee.schimke.wearcmp:wear-compose-material3"))
        .using(module("androidx.wear.compose:compose-material3:$wearComposeVersion"))
        .because("the kit is measured against the real AndroidX library, not against the port")
      substitute(module("ee.schimke.wearcmp:wear-compose-foundation"))
        .using(module("androidx.wear.compose:compose-foundation:$wearComposeVersion"))
        .because("the kit is measured against the real AndroidX library, not against the port")
      substitute(module("ee.schimke.wearcmp:wear-compose-material-core"))
        .using(module("androidx.wear.compose:compose-material-core:$wearComposeVersion"))
        .because("transitive of the two above; same reason")
    }
    // Skiko is the port's platform layer — the reason it cannot BE an Android artifact. It is not
    // published for Android at all, so an android configuration that resolved it would fail; after
    // the substitution above nothing asks for it, and this makes that explicit rather than
    // incidental.
    exclude(group = "org.jetbrains.skiko")
  }
