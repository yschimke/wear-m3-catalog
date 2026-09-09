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

/**
 * The two numbers `RotaryFlingHandler` reads out of `android.view.ViewConfiguration`: the slowest
 * rotary velocity that still counts as a fling, and the fastest one that will be honoured.
 *
 * Android's per-device values come from the framework's own resources, and on Wear they are
 * additionally overridden per input device since API 34. Off-Android there is no such table, so
 * these are the framework defaults — the values every device inherits unless its OEM overrides
 * them. They are in pixels per second, as upstream's are.
 *
 * Kept as a class with Android's method names so the patch on `RotaryScrollable.kt` is a rename
 * plus the deletion of a version check, and not a rewrite of the fling handler.
 */
internal class RotaryViewConfiguration(
    val scaledMinimumFlingVelocity: Int = DefaultMinimumFlingVelocity,
    val scaledMaximumFlingVelocity: Int = DefaultMaximumFlingVelocity,
) {
    /**
     * Android 34+ lets a device declare different limits per input device and axis. Nothing
     * off-Android does, so the arguments are accepted and ignored — which keeps the call site
     * identical to upstream's.
     */
    fun getScaledMinimumFlingVelocity(inputDeviceId: Int, axis: Int, source: Int): Int =
        scaledMinimumFlingVelocity

    fun getScaledMaximumFlingVelocity(inputDeviceId: Int, axis: Int, source: Int): Int =
        scaledMaximumFlingVelocity

    companion object {
        /** `ViewConfiguration.MINIMUM_FLING_VELOCITY`, in pixels per second. */
        const val DefaultMinimumFlingVelocity: Int = 50

        /** `ViewConfiguration.MAXIMUM_FLING_VELOCITY`, in pixels per second. */
        const val DefaultMaximumFlingVelocity: Int = 8000
    }
}
