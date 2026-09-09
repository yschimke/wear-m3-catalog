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

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/*
 * The host-facing seams.
 *
 * Each one replaces a Wear OS capability that has no off-Android implementation — a system
 * service, a settings provider, a vibrator. The port could have stubbed them and been done, and
 * for a while it did. It does not, because a stub is a dead end: the UI builder wants to portray a
 * watch in ambient mode, or with a screen reader running, and a hardcoded `false` cannot be asked
 * to.
 *
 * So each is an INTERFACE with a composition local carrying it and a no-op default. The port never
 * implements them for real; the host does, for whatever it is portraying, and the components go on
 * calling the same API they call on a watch.
 */

/**
 * Whether a touch-exploration screen reader is driving the UI — Android's
 * `AccessibilityManager.isTouchExplorationEnabled`, which several Wear components branch on to
 * offer a different interaction.
 *
 * Defaults to [platformTouchExplorationEnabled], which is `false` everywhere off-Android: the web
 * deliberately does not let a page detect assistive technology. A host that IS portraying a screen
 * reader — a preview harness, a test — provides its own value.
 */
public val LocalTouchExplorationEnabled: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf {
        platformTouchExplorationEnabled()
    }

/**
 * Plays the haptic effects Wear fires while a rotary crown is turned.
 *
 * The decision of WHEN to buzz is ordinary common code in `RotaryScrollable` and ports unchanged;
 * this is only the primitive underneath it. The default plays through the platform —
 * `navigator.vibrate` on the web, which is a real implementation — so a host that wants nothing
 * provides [NoRotaryHapticFeedback] rather than turning haptics off component by component.
 */
public fun interface RotaryHapticFeedback {
    public fun performRotaryHaptic(kind: RotaryHapticKind)
}

/** The default: whatever the platform can do. See [platformPerformRotaryHaptic]. */
public object PlatformRotaryHapticFeedback : RotaryHapticFeedback {
    override fun performRotaryHaptic(kind: RotaryHapticKind) {
        platformPerformRotaryHaptic(kind)
    }
}

/** Plays nothing. For a host that is rendering rather than being worn. */
public object NoRotaryHapticFeedback : RotaryHapticFeedback {
    override fun performRotaryHaptic(kind: RotaryHapticKind) {}
}

public val LocalRotaryHapticFeedback: ProvidableCompositionLocal<RotaryHapticFeedback> =
    staticCompositionLocalOf {
        PlatformRotaryHapticFeedback
    }
