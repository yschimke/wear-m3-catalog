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
 * `OneHandedGestureModifier` IS ported now, over this interface, and it was the change this shape
 * was designed for: the modifier holds the [GestureRegistration] it was handed and gives it back on
 * detach, where upstream reconstructs the arguments to identify what to remove. So a host that
 * knows how to detect its own gestures — a browser, a test — implements this, provides it through
 * [LocalOneHandedGestureManager], and every component above it works unchanged.
 *
 * What is still not ported is the hint INDICATORS, which draw from animated vector drawables. A
 * host gets working gestures from this; it does not get the built-in visual hint.
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

    /**
     * Change what an existing registration delivers, without tearing it down.
     *
     * Upstream takes the old and the new [OneHandedGestureConfiguration]; here the old one is
     * whatever [registration] was made with, which is the point of handing back a handle.
     *
     * The default unregisters and registers again, which is the correct behaviour for any manager
     * that has nothing cheaper to offer — so a host implements [registerGesture] and
     * [unregisterGesture] and is done. Override it when re-registering would cost something the
     * host would rather avoid, such as a round trip to a system service.
     *
     * @return the registration to use from now on, which may or may not be [registration].
     */
    public fun updateGesture(
        registration: GestureRegistration,
        haptic: HapticFeedback,
        gestureConfiguration: OneHandedGestureConfiguration,
        enabledInAmbient: Boolean,
        onGestureLabel: String?,
        onGestureAvailable: () -> Unit,
        onGesture: suspend (centerOffset: Offset) -> Unit,
        isActive: () -> Boolean,
        size: () -> IntSize,
    ): GestureRegistration {
        unregisterGesture(registration)
        return registerGesture(
            haptic = haptic,
            gestureConfiguration = gestureConfiguration,
            enabledInAmbient = enabledInAmbient,
            onGestureLabel = onGestureLabel,
            onGestureAvailable = onGestureAvailable,
            onGesture = onGesture,
            isActive = isActive,
            size = size,
        )
    }

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
