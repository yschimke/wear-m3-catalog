package ee.schimke.wearcmp.port

/**
 * `navigator.vibrate`, which is the web's whole haptics API: a duration in milliseconds, no
 * amplitude and no waveform composition. So the three effects differ only in length, and the
 * durations are chosen to read the way the Wear ones do rather than to match them — they cannot.
 *
 * It is a no-op wherever the browser has no vibrator (every desktop) or has not had a user
 * gesture yet, and `vibrate` returning false is the specified way to say so. Nothing here throws.
 */
public actual fun platformPerformRotaryHaptic(kind: RotaryHapticKind) {
    val milliseconds =
        when (kind) {
            RotaryHapticKind.ScrollTick -> 1
            RotaryHapticKind.ScrollItemFocus -> 3
            RotaryHapticKind.ScrollLimit -> 10
        }
    vibrate(milliseconds)
}

private fun vibrate(milliseconds: Int): Boolean =
    js("(navigator.vibrate ? navigator.vibrate(milliseconds) : false)")
