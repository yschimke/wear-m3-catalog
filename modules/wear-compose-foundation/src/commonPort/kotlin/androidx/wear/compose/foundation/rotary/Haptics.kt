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

package androidx.wear.compose.foundation.rotary

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ee.schimke.wearcmp.port.LocalRotaryHapticFeedback
import ee.schimke.wearcmp.port.NoRotaryHapticFeedback
import ee.schimke.wearcmp.port.RotaryHapticFeedback
import ee.schimke.wearcmp.port.RotaryHapticKind

/*
 * Replaces the generated `rotary/Haptics.kt`, which is excluded in `transform-rules.json`.
 *
 * Upstream is 419 lines, and essentially all of it is Android: a `Vibrator`, `VibrationEffect`
 * composition, a per-OEM constants table keyed off `Build.MANUFACTURER` and the OS version, a
 * background thread, and a conflated channel to keep vibration off the UI thread. What
 * `RotaryScrollable` actually depends on is the three-method interface below and one factory —
 * that is the seam, and it is what this file keeps. The rest has no meaning off a watch.
 *
 * The de-duplication upstream does with a conflated channel is not reproduced: `navigator.vibrate`
 * is already cheap and non-blocking, and a channel per scrollable would cost more than it saves.
 */

/** Handles haptics for rotary usage */
internal interface RotaryHapticHandler {

    /** Handles haptics when scroll is used */
    fun handleScrollHaptic(timestamp: Long, deltaInPixels: Float, inputDeviceId: Int, axis: Int)

    /** Handles haptics when scroll with snap is used */
    fun handleSnapHaptic(timestamp: Long, deltaInPixels: Float, inputDeviceId: Int, axis: Int)

    /** Handles haptics when edge of the list is reached */
    fun handleLimitHaptic(isStart: Boolean, inputDeviceId: Int, axis: Int)
}

@Composable
internal fun rememberRotaryHapticHandler(
    scrollableState: ScrollableState,
    hapticsEnabled: Boolean,
): RotaryHapticHandler {
    // `LocalRotaryHapticFeedback` is the public seam — upstream's own RotaryHapticHandler is
    // internal, so a host could not otherwise reach it. `hapticsEnabled` is still the component's
    // own switch and still wins: a component that asks for no haptics gets none.
    val feedback = if (hapticsEnabled) LocalRotaryHapticFeedback.current else NoRotaryHapticFeedback
    return remember(feedback) { SeamRotaryHapticHandler(feedback) }
}

private class SeamRotaryHapticHandler(private val feedback: RotaryHapticFeedback) :
    RotaryHapticHandler {
    override fun handleScrollHaptic(
        timestamp: Long,
        deltaInPixels: Float,
        inputDeviceId: Int,
        axis: Int,
    ) {
        feedback.performRotaryHaptic(RotaryHapticKind.ScrollTick)
    }

    override fun handleSnapHaptic(
        timestamp: Long,
        deltaInPixels: Float,
        inputDeviceId: Int,
        axis: Int,
    ) {
        feedback.performRotaryHaptic(RotaryHapticKind.ScrollItemFocus)
    }

    override fun handleLimitHaptic(isStart: Boolean, inputDeviceId: Int, axis: Int) {
        feedback.performRotaryHaptic(RotaryHapticKind.ScrollLimit)
    }
}
