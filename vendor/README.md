# Vendored Remote Compose

These modules are copied from AndroidX change
[4307936](https://android-review.googlesource.com/c/platform/frameworks/support/+/4307936), patch set
17, commit `01398020c7ec20f51e51bfc2b65b9b425e338be9`:

| Local module | AndroidX source |
| --- | --- |
| `:vendor:remote-creation-compose` | `compose/remote/remote-creation-compose` |
| `:vendor:remote-foundation` | `compose/remote/foundation/foundation` |
| `:vendor:remote-material3` | `wear/compose/remote/remote-material3` |
| `:vendor:remote-core` | write-side sources from `remote-core`, `remote-creation-core`, and `remote-creation` |

The copied Kotlin sources are upstream bytes. Local `build.gradle.kts` files adapt AndroidX's
internal build to this repository and expose Android and JVM targets. Foundation and Material 3 are
placed in the shared JVM/Android source set because their upstream `src/main` sources contain no
Android APIs. One local compatibility source, `FontVariationSettingsCompat.kt`, bridges the
`Font.variationSettings` API included by the CL's Compose UI prerequisite but not yet present in the
published Compose JVM artifact; member resolution supersedes the bridge when that API is released.
Two narrow platform adapters complete the JVM surface: `RemoteTimeDefaults.jvm.kt` obtains the
12/24-hour preference from Java's locale formatter, and `CurrentScreenHeight` uses Compose's window
metrics on JVM while retaining `LocalConfiguration` on Android.

`:remote-desktop` is the phase-2 client. Its `run` task performs a real Compose recomposition through
the vendored JVM applier and writes the encoded document to
`remote-desktop/build/desktop-sample.rc`; it does not use Robolectric or any Android API.
`DesktopCaptureTest` repeats the capture and checks that its bytes are non-empty and deterministic.

## Write-only core extraction

`:vendor:remote-core` begins phase 3 by merging the JVM writer stack that AndroidX publishes as
`remote-core`, `remote-creation-core`, and `remote-creation`. The JSON-to-document importer has been
removed. The Desktop graph substitutes all three published coordinates with this one project; the
Android graph temporarily retains the published `remote-creation` platform variant for its Android
bitmap and path adapters.

`remote-core`, `remote-creation-core`, and `remote-creation` remain Maven dependencies: their
AndroidX artifacts already publish standard JVM variants. On JVM, Remote Material 3 uses this
repository's existing CMP Wear Compose port for the Wear token types it references. Android
configurations substitute that port back to the real AndroidX Wear Compose artifacts.

The vendored source is Apache 2.0 licensed; each source file retains its Android Open Source Project
header.
