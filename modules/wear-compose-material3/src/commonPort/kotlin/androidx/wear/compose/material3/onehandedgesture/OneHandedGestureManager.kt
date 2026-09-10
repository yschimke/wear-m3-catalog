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
import kotlin.time.Duration

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
 * The hint INDICATORS are ported too, and they need three more members than gestures do — the
 * bookkeeping that lets an indicator find out how long its own animation is meant to run. Upstream
 * keeps that in a map inside its manager implementation; here it has a default implementation, so a
 * host implements gestures and gets indicator bookkeeping for free. Only `notifyIndicatorShown`
 * genuinely wants a host: on Android it tells the system service a hint was seen, which is how the
 * platform stops showing it forever, and off Android there is nobody to tell.
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

    /**
     * Record how the indicator for [gestureConfiguration] is drawn and how long it runs.
     *
     * Registering the same configuration twice with different values is a programming error, as it
     * is upstream: two indicators for one gesture would each believe their own timing.
     */
    public fun registerGestureIndicator(
        gestureConfiguration: OneHandedGestureConfiguration,
        isFloating: Boolean,
        duration: Duration,
    ) {
        val registered = RegisteredIndicator(isFloating, duration)
        val current = indicatorRegistry(this).getOrPut(gestureConfiguration) { registered }
        require(current == registered) {
            "Incompatible Gesture Indicators registered for the same " +
                "OneHandedGestureConfiguration - see $gestureConfiguration"
        }
    }

    /** What [registerGestureIndicator] recorded for [gestureConfiguration], if anything. */
    public fun getRegisteredGestureIndicator(
        gestureConfiguration: OneHandedGestureConfiguration
    ): RegisteredIndicator? = indicatorRegistry(this)[gestureConfiguration]

    /**
     * A hint indicator has just been shown to the wearer.
     *
     * Upstream forwards this to the Wear input service, which counts how often a hint has been
     * presented and eventually stops asking for it. Nothing off Android is keeping that count, so
     * the default does nothing; a host that has somewhere to put it overrides this.
     */
    public fun notifyIndicatorShown(gestureConfiguration: OneHandedGestureConfiguration) {}

    /** What a registration is; opaque, and owned by the implementation. */
    public interface GestureRegistration
}

/** How one gesture's hint indicator is drawn. Upstream's own data class, made public here. */
public data class RegisteredIndicator(val isFloating: Boolean, val duration: Duration)

/**
 * Per-manager indicator bookkeeping, kept beside the interface because an interface cannot hold
 * state and this is not something a host should have to reimplement.
 *
 * Keyed by manager IDENTITY rather than shared globally: two managers in one process — a test's and
 * a host's — register indicators for the same configurations and must not see each other's.
 */
private val indicators =
    mutableMapOf<
        OneHandedGestureManager,
        MutableMap<OneHandedGestureConfiguration, RegisteredIndicator>,
    >()

private fun indicatorRegistry(
    manager: OneHandedGestureManager
): MutableMap<OneHandedGestureConfiguration, RegisteredIndicator> =
    indicators.getOrPut(manager) { mutableMapOf() }

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
