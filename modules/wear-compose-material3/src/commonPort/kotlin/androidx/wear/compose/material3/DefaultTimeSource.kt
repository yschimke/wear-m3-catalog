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

package androidx.wear.compose.material3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.wear.compose.materialcore.currentTimeMillis
import ee.schimke.wearcmp.port.formatTime
import ee.schimke.wearcmp.port.platformLocalTime
import kotlinx.coroutines.delay

/*
 * The clock behind `TimeText`, replacing what the patch on the generated `TimeText.kt` removes.
 * Upstream's `TimeSource` interface — the seam a host overrides to drive the clock itself — is
 * untouched and still generated; this is only its default implementation.
 *
 * What changes is how the clock ticks. Upstream registers a BroadcastReceiver for
 * ACTION_TIME_TICK, which Android delivers once a minute, plus ACTION_TIME_CHANGED and
 * ACTION_TIMEZONE_CHANGED. Off-Android nothing announces those, so this polls — and polls on the
 * minute boundary rather than every second, so a minute-resolution clock costs one recomposition
 * per minute, the same as the broadcast did.
 *
 * A manual time or time-zone change is therefore picked up at the next boundary rather than
 * immediately. On a watch that would matter; in a browser portraying one, a clock that is a few
 * seconds late to a manual zone change is not a defect worth a wake-up for.
 */
internal class DefaultTimeSource(val timeFormat: String) : TimeSource {
    @Composable
    override fun currentTime(): String = currentTime({ currentTimeMillis() }, timeFormat).value
}

@Composable
internal fun currentTime(time: () -> Long, timeFormat: String): State<String> {
    val formatted = remember(timeFormat) { mutableStateOf(format(time(), timeFormat)) }

    LaunchedEffect(time, timeFormat) {
        while (true) {
            val now = time()
            formatted.value = format(now, timeFormat)
            // Sleep to the next minute boundary, plus a few milliseconds so that rounding never
            // wakes us a hair early and formats the same minute twice.
            delay(MillisPerMinute - now % MillisPerMinute + 5L)
        }
    }

    return formatted
}

private fun format(epochMillis: Long, timeFormat: String): String =
    formatTime(platformLocalTime(epochMillis), timeFormat)

private const val MillisPerMinute = 60_000L
