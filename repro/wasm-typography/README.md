# Optimized Wasm `Typography()` reproducer

This standalone build reduces the production-only failure originally observed while composing
Remote Material 3. It has no Remote Compose dependency, no composition, and no function-valued
application code: it only constructs Wear Compose Material 3's `Typography`.

It consumes the published Compose Multiplatform Wear port used by the browser client:

- Kotlin and Compose compiler `2.4.20`
- Compose Multiplatform `1.12.0`
- `ee.schimke.wearcmp:wear-compose-material3:1.7.0-beta02-cmp09`

Node is intentionally not used: Compose's Skia font manager is a browser-hosted Wasm import, so a
Node executable fails in both modes for an unrelated missing import. From the repository root,
build the two browser distributions:

```sh
./gradlew -p repro/wasm-typography \
  wasmJsBrowserDevelopmentExecutableDistribution
./gradlew -p repro/wasm-typography wasmJsBrowserDistribution
```

Serve each output from a fresh port and open it in a browser:

```sh
python3 -m http.server 18801 \
  --directory repro/wasm-typography/build/dist/wasmJs/developmentExecutable
python3 -m http.server 18802 \
  --directory repro/wasm-typography/build/dist/wasmJs/productionExecutable
```

Expected development output:

```text
before Typography()
after Typography(): 16.0.sp
```

The production executable currently prints the first line and then fails from minified JavaScript:

```text
before Typography()
TypeError: <minified> is not a function
```

The equivalent constructor works on JVM, Android, and a Wasm development executable. This places
the defect below Remote Material 3's composable content lambdas: `RemoteTypography()` initializes
its defaults from Wear `Typography()`, so the constructor failed before composition began.
