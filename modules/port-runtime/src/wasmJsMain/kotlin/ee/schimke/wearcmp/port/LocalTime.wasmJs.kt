package ee.schimke.wearcmp.port

/**
 * `Date`'s local getters, which read the browser's own time zone — the same zone the user sees in
 * every other clock on their machine, which is what a watch face portraying their watch should
 * show.
 *
 * The epoch value crosses the boundary as a Double: a Kotlin `Long` is not a JavaScript number, and
 * milliseconds since the epoch are exactly representable as one until the year 287396.
 */
public actual fun platformLocalTime(epochMillis: Long): LocalTimeParts {
    val millis = epochMillis.toDouble()
    return LocalTimeParts(
        hour = jsHours(millis),
        minute = jsMinutes(millis),
        second = jsSeconds(millis),
    )
}

private fun jsHours(millis: Double): Int = js("new Date(millis).getHours()")

private fun jsMinutes(millis: Double): Int = js("new Date(millis).getMinutes()")

private fun jsSeconds(millis: Double): Int = js("new Date(millis).getSeconds()")
