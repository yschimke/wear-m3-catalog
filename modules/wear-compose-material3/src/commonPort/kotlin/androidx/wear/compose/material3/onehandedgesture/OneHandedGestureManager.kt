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

package androidx.wear.compose.material3.onehandedgesture

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.unit.IntSize

/*
 * Replaces the generated `onehandedgesture/OneHandedGestureManager.kt`, which is excluded in
 * `transform-rules.json`.
 *
 * Upstream's implementation is `com.google.wear.input.GestureInputManager` — the Wear OS service
 * that watches for a wrist turn or a double pinch — and its interface is not portable either:
 * every method takes the `android.view.View` the gesture is registered against, because that is
 * what the system service keys its subscriptions on.
 *
 * So the interface IS ported, with one deliberate change: the `View` parameter is gone. It was
 * only ever an identity for the registration, and a host outside Android has its own notion of
 * what a gesture is attached to — the [GestureRegistration] returned by [registerGesture] is that
 * identity here, which also makes unregistering a matter of holding on to what you were given
 * rather than reconstructing the arguments.
 *
 * Nothing in the port calls this yet: the modifier that would (`OneHandedGestureModifier`) is the
 * other half of the Android coupling and is not ported. It exists so a host CAN — a browser that
 * knows how to detect its own gestures implements this and provides it, and the day the modifier
 * is ported over this interface, everything above it works unchanged.
 */

/** Registers and delivers Wear's one-handed gestures — a wrist turn, a double pinch. */
public interface OneHandedGestureManager {
    /**
     * Register a gesture.
     *
     * @param haptic played when the gesture fires.
     * @param gestureConfiguration which gesture, and how it is presented.
     * @param enabledInAmbient whether it stays live while the display is in ambient mode.
     * @param onGestureLabel a label for accessibility and debugging.
     * @param onGestureAvailable invoked when the gesture becomes available, so the caller can show
     *   its hint indicator.
     * @param onGesture invoked when the gesture fires, with the centre of the target.
     * @param isActive whether the component that owns the gesture is currently active.
     * @param size the size of that component.
     * @return a handle to pass to [unregisterGesture]; upstream uses the View for this.
     */
    public fun registerGesture(
        haptic: HapticFeedback,
        gestureConfiguration: OneHandedGestureConfiguration,
        enabledInAmbient: Boolean,
        onGestureLabel: String?,
        onGestureAvailable: () -> Unit,
        onGesture: suspend (centerOffset: Offset) -> Unit,
        isActive: () -> Boolean,
        size: () -> IntSize,
    ): GestureRegistration

    /** Stop delivering a gesture registered by [registerGesture]. */
    public fun unregisterGesture(registration: GestureRegistration)

    /** What a registration is; opaque, and owned by the implementation. */
    public interface GestureRegistration
}

/**
 * The fallback manager: registers nothing and fires nothing. Public so a host can name it — to say
 * explicitly that it supports no gestures, or to delegate to it for the ones it does not.
 */
public object NoOneHandedGestureManager : OneHandedGestureManager {
    private object Registration : OneHandedGestureManager.GestureRegistration

    override fun registerGesture(
        haptic: HapticFeedback,
        gestureConfiguration: OneHandedGestureConfiguration,
        enabledInAmbient: Boolean,
        onGestureLabel: String?,
        onGestureAvailable: () -> Unit,
        onGesture: suspend (centerOffset: Offset) -> Unit,
        isActive: () -> Boolean,
        size: () -> IntSize,
    ): OneHandedGestureManager.GestureRegistration = Registration

    override fun unregisterGesture(registration: OneHandedGestureManager.GestureRegistration) {}
}

/**
 * The manager in force for this composition. Upstream's equivalent is `internal` and resolves to
 * the Wear service; this one is public, because providing it is how a host takes part at all.
 */
public val LocalOneHandedGestureManager: ProvidableCompositionLocal<OneHandedGestureManager> =
    staticCompositionLocalOf {
        NoOneHandedGestureManager
    }
