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

package ee.schimke.wearcmp.port

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The device facts Wear Compose reads out of Android's `Configuration` and `Settings.System`.
 *
 * Upstream `Resources.kt` asks `LocalConfiguration` and `LocalContext` five separate questions —
 * is the screen round, how wide is it, is the watch worn on the right wrist, is the clock 24-hour.
 * Neither composition local exists off-Android, and none of the five is really a *platform*
 * question: they are questions about a **watch**, and a browser host answering them is describing
 * a watch it is emulating, not the machine it runs on.
 *
 * So the seam is one value with a default per platform rather than five `expect` functions. A host
 * — the UI builder, a preview renderer, a test — overrides it for the device it is portraying:
 *
 *     CompositionLocalProvider(
 *         LocalWearDeviceConfiguration provides WearDeviceConfiguration(screenWidthDp = 227)
 *     ) { MyWatchFace() }
 *
 * @param isScreenRound upstream `Configuration.isScreenRound`.
 * @param screenWidthDp upstream `Configuration.screenWidthDp` — the size of the watch face, which
 *   is also what drives every `isSmallScreen()` / `isLargeScreen()` branch in Material 3.
 * @param screenHeightDp upstream `Configuration.screenHeightDp`.
 * @param isLeftyModeEnabled upstream `Settings.System.USER_ROTATION == ROTATION_180`.
 * @param is24HourFormat upstream `DateFormat.is24HourFormat(context)`.
 */
public class WearDeviceConfiguration(
    public val isScreenRound: Boolean = true,
    public val screenWidthDp: Int = DEFAULT_SCREEN_DP,
    public val screenHeightDp: Int = DEFAULT_SCREEN_DP,
    public val isLeftyModeEnabled: Boolean = false,
    public val is24HourFormat: Boolean = true,
) {
    public companion object {
        /**
         * 192dp square — the small round reference watch, and the size AndroidX's own previews
         * and Robolectric qualifiers use. It is the honest answer when the host has not said.
         */
        public const val DEFAULT_SCREEN_DP: Int = 192
    }
}

/**
 * The device the composition is drawing for. Defaults to [platformWearDeviceConfiguration], which
 * is as much as each platform can work out on its own.
 *
 * `static` because a change to it invalidates essentially every Wear component; there is nothing
 * to gain from tracking reads individually.
 */
public val LocalWearDeviceConfiguration: androidx.compose.runtime.ProvidableCompositionLocal<
    WearDeviceConfiguration
> =
    staticCompositionLocalOf {
        platformWearDeviceConfiguration()
    }

/**
 * What this platform can say about the device without being told: the browser reports its viewport
 * and its locale's clock, the JVM reports nothing and takes the reference watch.
 */
public expect fun platformWearDeviceConfiguration(): WearDeviceConfiguration

/**
 * Wall-clock milliseconds. Upstream calls `System.currentTimeMillis()`, which is JVM-only; the
 * multiplatform stdlib clock says the same thing everywhere, so this needs no `expect`.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
public fun platformCurrentTimeMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

/** Convenience for the generated sources, which read the configuration in composable position. */
@Composable
@ReadOnlyComposable
public fun currentWearDeviceConfiguration(): WearDeviceConfiguration =
    LocalWearDeviceConfiguration.current
