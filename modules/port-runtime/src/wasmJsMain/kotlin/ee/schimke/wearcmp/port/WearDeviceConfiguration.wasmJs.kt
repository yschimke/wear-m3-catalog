package ee.schimke.wearcmp.port

/**
 * In the browser the emulated watch is the canvas the host gave us, so the viewport is the closest
 * thing to `Configuration.screenWidthDp` — and the locale's clock is a real answer to
 * `DateFormat.is24HourFormat`, which is the one question the platform genuinely does know.
 *
 * The shape is the one thing a browser cannot know: a viewport is rectangular, and whether it is
 * *portraying* a round watch is the host's decision. It defaults to round because that is what the
 * Wear kit is drawn for, and a host that means square says so through
 * [LocalWearDeviceConfiguration].
 */
public actual fun platformWearDeviceConfiguration(): WearDeviceConfiguration =
    WearDeviceConfiguration(
        isScreenRound = true,
        screenWidthDp = viewportWidthDp(),
        screenHeightDp = viewportHeightDp(),
        is24HourFormat = localeIs24Hour(),
    )

// `window.innerWidth` is already in CSS pixels, which is what a dp is on the web.
private fun viewportWidthDp(): Int = jsViewportWidth().takeIf { it > 0 }
    ?: WearDeviceConfiguration.DEFAULT_SCREEN_DP

private fun viewportHeightDp(): Int = jsViewportHeight().takeIf { it > 0 }
    ?: WearDeviceConfiguration.DEFAULT_SCREEN_DP

@Suppress("UNUSED_PARAMETER")
private fun jsViewportWidth(): Int = js("window.innerWidth")

@Suppress("UNUSED_PARAMETER")
private fun jsViewportHeight(): Int = js("window.innerHeight")

/**
 * `Intl.DateTimeFormat().resolvedOptions().hour12` is the browser's own answer to the question
 * `android.text.format.DateFormat.is24HourFormat(context)` asks the system settings. It is
 * `undefined` for locales that do not say, hence the explicit `=== false`.
 */
private fun localeIs24Hour(): Boolean =
    js("Intl.DateTimeFormat().resolvedOptions().hour12 === false")
