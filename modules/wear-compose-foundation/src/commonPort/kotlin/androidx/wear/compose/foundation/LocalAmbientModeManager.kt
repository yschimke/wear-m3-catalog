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

package androidx.wear.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive

/*
 * Replaces the generated `LocalAmbientModeManager.kt`, which is excluded in
 * `transform-rules.json`.
 *
 * Upstream's implementation is `com.google.wear.services.ambient.AmbientManager` — a Wear OS
 * system service, reached through the hosting `Activity`, that reports when the watch dims to its
 * low-power always-on state and ticks once a minute while it is there. A browser has no such
 * state, and inventing one would be worse than not having it: a component that dimmed itself
 * because a tab lost focus would be wrong in a way that is hard to notice.
 *
 * So what is kept is the INTERFACE — [AmbientModeManager], unchanged from upstream, because a
 * browser host portraying a watch in its low-power state is a thing the UI builder genuinely wants
 * to do, and it can only do it if there is something to implement. The port never implements it
 * for real: [rememberAmbientModeManager] hands back whatever the host provided through
 * [LocalAmbientModeManager], and only falls back to [InteractiveAmbientModeManager] — always
 * [AmbientMode.Interactive], never ticking — when nothing was provided.
 *
 * That fallback's `withAmbientTick` suspends forever rather than returning, so `AmbientTickEffect`'s
 * loop parks instead of spinning. The loop is only entered in ambient mode anyway, which without a
 * host-provided manager never happens.
 */

/**
 * [ProvidableCompositionLocal] carrying the [AmbientModeManager] for the composition, or `null`
 * when none was provided.
 */
public val LocalAmbientModeManager: ProvidableCompositionLocal<AmbientModeManager?> =
    staticCompositionLocalOf {
        null
    }

/**
 * Remembers an [AmbientModeManager].
 *
 * Upstream builds one over the Wear ambient service and ties it to the hosting Activity's
 * lifecycle. Here it resolves the host's manager from [LocalAmbientModeManager], falling back to
 * [InteractiveAmbientModeManager] when there is none — so a host opts in by providing the local
 * once, above everything, rather than by replacing every call site.
 */
@Composable
public fun rememberAmbientModeManager(): AmbientModeManager =
    LocalAmbientModeManager.current ?: InteractiveAmbientModeManager

public interface AmbientModeManager {

    /** The current state of the display. */
    public val currentAmbientMode: AmbientMode

    /** Suspends until the next ambient tick. */
    public suspend fun withAmbientTick(block: () -> Unit)
}

/**
 * Runs [block] once per ambient tick while the device is in [AmbientMode.Ambient]. Verbatim from
 * upstream — it is platform-free, and only its dependencies were not.
 */
@Composable
public fun AmbientModeManager.AmbientTickEffect(block: () -> Unit) {
    if (currentAmbientMode is AmbientMode.Ambient) {
        LaunchedEffect(this, block) {
            while (isActive) {
                withAmbientTick(block)
            }
        }
    }
}

/**
 * The fallback [AmbientModeManager]: the display is always interactive and no ambient tick ever
 * arrives. Public so a host can name it — to say explicitly that it is not portraying ambient
 * mode, or to delegate to it for the parts it does not implement.
 */
public object InteractiveAmbientModeManager : AmbientModeManager {
    override val currentAmbientMode: AmbientMode = AmbientMode.Interactive

    override suspend fun withAmbientTick(block: () -> Unit) {
        // No tick will ever arrive; park rather than returning, so a caller's `while (isActive)`
        // loop suspends instead of spinning on an immediate return.
        awaitCancellation()
    }
}
