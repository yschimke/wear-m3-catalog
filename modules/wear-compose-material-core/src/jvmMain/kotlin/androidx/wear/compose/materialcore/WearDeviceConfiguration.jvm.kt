package androidx.wear.compose.materialcore

/**
 * A desktop JVM host is not a watch and has nothing to report, so it takes the reference watch —
 * 192dp round, 24-hour. The JVM target exists to compile-check the port an order of magnitude
 * faster than the wasm one, not to ship; a host that renders for real overrides
 * [LocalWearDeviceConfiguration].
 */
internal actual fun platformWearDeviceConfiguration(): WearDeviceConfiguration =
    WearDeviceConfiguration()
