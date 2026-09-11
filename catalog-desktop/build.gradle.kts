// `:catalog-desktop` — the same kit, drawn by Compose Multiplatform Desktop.
//
// It declares NO previews of its own. Every sticker it renders is one of `:catalog`'s, discovered
// through the `composePreviewSource` configuration (compose-ai-tools#5402) and drawn here on the
// CMP Desktop lane instead of Robolectric. That is the whole module: a second renderer pointed at
// one set of component bodies.
//
// ── Why a second module at all ────────────────────────────────────────────────────────────────
//
// The compose-preview plugin registers exactly ONE render lane per module — Robolectric or CMP
// Desktop, decided once — and `:catalog` is Robolectric, because the published kit rendition must
// be drawn by the real AndroidX library. A module cannot be both, so the desktop tier is a module
// rather than another target on that one.
//
// ── What this renders, and what it is NOT ─────────────────────────────────────────────────────
//
// The CMP Wear port, straight: no substitution here, because there is nothing to substitute to —
// `androidx.wear.compose` ships for Android only, which is the entire reason the port exists.
//
// So these renders are **not** the kit rendition and must never be published as it. `:catalog`'s
// Robolectric sheet remains the thing this repository measures against the kit; what this module
// answers is a different question — does the shared source draw the same picture through a
// different renderer — and the two lanes disagreeing is a finding about the port, not about the
// catalog.
//
// ── What it cannot draw ───────────────────────────────────────────────────────────────────────
//
// The six Android-only files in `:catalog/src/androidMain` — `HorologistSamples.kt`,
// `CatalogAvatar.kt` and the four Horologist sections — are absent from the desktop variant, so
// their previews are simply not discovered here. That is correct rather than a gap to paper over:
// Horologist publishes AARs with no multiplatform line, and a desktop sheet claiming to draw them
// would be drawing something else.
plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.compose.multiplatform)
  id("org.jetbrains.kotlin.plugin.compose")
  alias(libs.plugins.composePreview)
}

kotlin {
  // The lane's compilation. `jvm("desktop")` to match `:catalog`'s own desktop target name, so the
  // two resolve the same variant of everything they share.
  jvm("desktop")

  sourceSets {
    getByName("desktopMain").dependencies {
      // `:catalog`'s classes on the runtime classpath — what the renderer actually executes.
      // `composePreviewSource` below is a separate declaration because it answers a separate
      // question: this one makes the composables resolvable, that one makes their `@Preview`s
      // THIS module's to render.
      implementation(project(":catalog"))

      @Suppress("DEPRECATION") implementation(compose.runtime)
      @Suppress("DEPRECATION") implementation(compose.foundation)
      implementation(libs.compose.multiplatform.material3)
      implementation(libs.compose.multiplatform.ui.tooling.preview)
      implementation(libs.wearcmp.compose.material3)
      implementation(libs.wearcmp.compose.foundation)
      implementation(libs.materialkolor)
      implementation(libs.composeai.preview.annotations)
      implementation(libs.composeai.preview.overrides)
    }
  }
}

dependencies {
  // Render `:catalog`'s previews on THIS module's lane. Its `desktop` variant is what an
  // unadorned `jvm` consumer selects, so what gets discovered is exactly `commonMain` — the
  // `androidMain` previews are not in that variant and are correctly invisible here.
  composePreviewSource(project(":catalog"))
}

composePreview {
  // `:catalog`'s shared sources, so each preview resolves back to the file that declares it and
  // the `@file:CatalogGroup` on it still applies. Classes alone find a preview; only the source
  // file places it — without this every sticker here lands ungrouped, with a green build.
  //
  // `commonMain` only: `androidMain` holds previews this lane cannot draw, and naming its sources
  // would attribute files whose classes are not on this classpath.
  previewSourceRoots.from(file("../catalog/src/commonMain"))
}
