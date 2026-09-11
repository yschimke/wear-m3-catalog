/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// `:third-party-horologist-lottie` — a vendored snapshot of Horologist's `remotecompose/lottie`:
// a Lottie **compiler**, not a Lottie player. It parses a Lottie JSON animation and re-emits every
// layer, shape, gradient and transform as Remote Compose *creation* operations whose keyframes are
// expressions over the document's own animation clock. The animation therefore ends up INSIDE the
// `.rc` document: nothing on the device needs a Lottie runtime, the JSON, or a network fetch.
//
// See `PROVENANCE.md` for the pinned upstream commit, what is and is not vendored, and why this
// repository is the one that holds it.
//
// It is here because `:remote-catalog` is its only consumer and cannot reach it any other way.
// Horologist publishes no artifact for this module — no `maven-publishing` plugin upstream, and
// nothing under `com.google.android.horologist:horologist-remotecompose-*` on Maven Central — so a
// consumer either vendors it or does without. `:remote-catalog` needs it twice over: to DRAW the
// `remote-m3/lottie` sticker, and to COMPILE the exporter-generated widget checked in beside it,
// which writes `LottieAnimation(json = …)` for a design carrying a Lottie node.
//
// Android-library, like upstream: the creation API (`androidx.compose.remote.creation.*`) is
// Android-only.

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
  // NOT upstream's `com.google.android.horologist.remotecompose.lottie`: the sources keep their
  // package, so a diff against a newer Horologist checkout stays a plain `diff -r`, but the
  // manifest namespace is ours — the vendored copy can never be mistaken for a published
  // Horologist library if one ever appears.
  namespace = "ee.schimke.wearm3catalog.horologist.lottie"

  // The alpha `compose-remote` AARs declare `minCompileSdk = 37`, which is why `:remote-catalog`
  // is at 37 too.
  compileSdk = 37

  defaultConfig {
    // Upstream's floor. Nothing here reaches an API newer than that.
    minSdk = 26
    aarMetadata { minCompileSdk = 36 }
  }

  buildFeatures { compose = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  // The renderer reaches `androidx.compose.remote.creation.*` members marked
  // `@RestrictTo(LIBRARY_GROUP)` — unavoidable for an out-of-tree copy of in-tree code, and
  // upstream suppresses the same lint at its own call sites.
  lint { disable += "RestrictedApi" }
}

// `src/main/` is upstream's bytes, and ktfmt would rewrite it into a permanent diff against every
// future import — the same reason `:samples-catalog` keeps the formatter out of its `upstream/`
// directory, and the formatting counterpart of "a fix is a patch, never an edit".
//
// `src/test/` is deliberately NOT excluded: `ExportedCallShape.kt` is ours, carries our copyright,
// and should be formatted like the rest of this repository's code.
//
// The root build applies ktfmt to every project, so this narrows its inputs here.
tasks.withType<com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask>().configureEach {
  exclude { it.file.absolutePath.replace('\\', '/').contains("/src/main/java/com/google/") }
}

dependencies {
  // The creation API this module writes through: `RemoteBox`, `RemoteCanvas`, `RemoteModifier`,
  // `RemoteFloat` and the expression builders that turn Lottie keyframes into document operations.
  api(libs.compose.remote.creation)
  api(libs.compose.remote.creation.compose)

  // NO Compose BOM, and the prerelease line rather than 2026.08.00 — `:remote-catalog` is the
  // only module that links this one and carries no BOM on purpose (its `wear-compose-remote`
  // dependencies pull the Compose 1.12 runtime). Compiling the compiler against a different
  // Compose than the module that links it is how a `NoSuchMethodError` reaches a render.
  implementation(libs.compose.runtime.prerelease)
  implementation(libs.compose.ui.prerelease)
  // `CubicBezierEasing`, for keyframe easing.
  implementation(libs.compose.animation.core.prerelease)
  // `MathUtils.clamp` on the scalar path.
  implementation(libs.androidx.core)
  implementation(libs.kotlinx.serialization.json)
}
