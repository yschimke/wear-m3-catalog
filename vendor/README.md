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
| `:vendor:remote-write-core` | common write-only encoder extracted from those core sources |

The copied Kotlin sources started as upstream bytes; the port then moved the portable Creation,
Foundation, and Material 3 closure to `commonMain`. Local build files expose Android, JVM, and Wasm
targets. Narrow actuals retain Android/JVM time, display, image, and legacy-writer integration.

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

## Published artifacts

The five vendored modules publish under `ee.schimke.remotecompose` at
`4307936-ps17-cmp03`. The version is derived from `remote-compose-upstream.json`; bump its
`portRevision` whenever published bytes change without moving to a newer AndroidX patch set.

As with the repository's Wear Compose CMP port, CI publishes to GitHub Packages and to a
credential-free Maven tree on the `remote-compose-cmp-maven` branch of the output repository,
`yschimke/wear-m3-catalog-out`:

```kotlin
repositories {
  maven("https://raw.githubusercontent.com/yschimke/wear-m3-catalog-out/remote-compose-cmp-maven/")
}

dependencies {
  implementation("ee.schimke.remotecompose:remote-material3:4307936-ps17-cmp03")
}
```

`./gradlew publishToMavenLocal` publishes locally. `./gradlew publishRemoteComposeToBuildDir`
produces the exact repository tree CI pushes under `build/remote-compose-maven`.

The vendored source is Apache 2.0 licensed; each source file retains its Android Open Source Project
header.
