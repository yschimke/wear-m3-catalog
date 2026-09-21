/*
 * Copyright (C) 2024 The Android Open Source Project
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

package androidx.compose.remote.creation.compose.modifier

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.common.GeneralEasing
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.RemoteTweenSpec
import androidx.compose.remote.creation.compose.state.remoteTween
import androidx.compose.remote.creation.common.RemoteModifierOperation

/** Transition effect applied when a component enters a state layout. */
public enum class RemoteAnimation {
    FADE_IN,
    FADE_OUT,
    SLIDE_LEFT,
    SLIDE_RIGHT,
    SLIDE_TOP,
    SLIDE_BOTTOM,
    ROTATE,
    PARTICLE,
}

public class RemoteEnterTransition internal constructor(internal val animation: RemoteAnimation) {
    public companion object {
        /** Fades the component in from transparent to opaque. */
        public val FadeIn: RemoteEnterTransition = RemoteEnterTransition(RemoteAnimation.FADE_IN)

        /** Slides the component in towards the left. */
        public val SlideInLeft: RemoteEnterTransition =
            RemoteEnterTransition(RemoteAnimation.SLIDE_LEFT)

        /** Slides the component in towards the right. */
        public val SlideInRight: RemoteEnterTransition =
            RemoteEnterTransition(RemoteAnimation.SLIDE_RIGHT)

        /** Slides the component in towards the top. */
        public val SlideInTop: RemoteEnterTransition =
            RemoteEnterTransition(RemoteAnimation.SLIDE_TOP)

        /** Slides the component in towards the bottom. */
        public val SlideInBottom: RemoteEnterTransition =
            RemoteEnterTransition(RemoteAnimation.SLIDE_BOTTOM)

        /** Rotates the component during entry. */
        public val Rotate: RemoteEnterTransition = RemoteEnterTransition(RemoteAnimation.ROTATE)

        /** Applies a particle effect during entry. */
        public val Particle: RemoteEnterTransition = RemoteEnterTransition(RemoteAnimation.PARTICLE)
    }

    override fun equals(other: Any?): Boolean =
        other is RemoteEnterTransition && animation == other.animation

    override fun hashCode(): Int = animation.hashCode()

    override fun toString(): String = "RemoteEnterTransition.${animation.name}"
}

/** Transition effect applied when a component exits a state layout. */
public class RemoteExitTransition internal constructor(internal val animation: RemoteAnimation) {
    public companion object {
        /** Fades the component out from opaque to transparent. */
        public val FadeOut: RemoteExitTransition = RemoteExitTransition(RemoteAnimation.FADE_OUT)

        /** Slides the component out towards the left. */
        public val SlideOutLeft: RemoteExitTransition =
            RemoteExitTransition(RemoteAnimation.SLIDE_LEFT)

        /** Slides the component out towards the right. */
        public val SlideOutRight: RemoteExitTransition =
            RemoteExitTransition(RemoteAnimation.SLIDE_RIGHT)

        /** Slides the component out towards the top. */
        public val SlideOutTop: RemoteExitTransition =
            RemoteExitTransition(RemoteAnimation.SLIDE_TOP)

        /** Slides the component out towards the bottom. */
        public val SlideOutBottom: RemoteExitTransition =
            RemoteExitTransition(RemoteAnimation.SLIDE_BOTTOM)

        /** Rotates the component during exit. */
        public val Rotate: RemoteExitTransition = RemoteExitTransition(RemoteAnimation.ROTATE)

        /** Applies a particle effect during exit. */
        public val Particle: RemoteExitTransition = RemoteExitTransition(RemoteAnimation.PARTICLE)
    }

    override fun equals(other: Any?): Boolean =
        other is RemoteExitTransition && animation == other.animation

    override fun hashCode(): Int = animation.hashCode()

    override fun toString(): String = "RemoteExitTransition.${animation.name}"
}

/** Creates a fade-in enter transition for Remote Compose state layouts. */
public fun remoteFadeIn(): RemoteEnterTransition = RemoteEnterTransition.FadeIn

/** Creates a fade-out exit transition for Remote Compose state layouts. */
public fun remoteFadeOut(): RemoteExitTransition = RemoteExitTransition.FadeOut

internal class AnimateSpecModifier(
    val animationId: Int,
    val motionDuration: Float,
    val motionEasingType: Int,
    val visibilityDuration: Float,
    val visibilityEasingType: Int,
    val enterAnimation: RemoteAnimation,
    val exitAnimation: RemoteAnimation,
) : RemoteModifier.Element {
    override fun RemoteStateScope.toRemoteModifierOperation(): RemoteModifierOperation =
        RemoteModifierOperation.AnimationSpec(
            animationId,
            motionDuration,
            motionEasingType,
            visibilityDuration,
            visibilityEasingType,
            enterAnimation.ordinal,
            exitAnimation.ordinal,
        )
}

private fun extractDurationAndEasing(spec: RemoteTweenSpec): Pair<Float, Int> =
    Pair(spec.durationMillis.toFloat(), spec.easing.type)

/**
 * Marks this component as a shared element that smoothly transitions its bounds, position, and
 * styling between states in a state layout.
 *
 * @param key unique integer identifier matching this element across states
 * @param spec animation specification for bounds and transforms (e.g. [remoteTween])
 * @param enter transition effect when component enters the layout
 * @param exit transition effect when component exits the layout
 */
public fun RemoteModifier.sharedElement(
    key: Int,
    spec: RemoteTweenSpec = remoteTween(),
    enter: RemoteEnterTransition = RemoteEnterTransition.FadeIn,
    exit: RemoteExitTransition = RemoteExitTransition.FadeOut,
): RemoteModifier {
    val (duration, easing) = extractDurationAndEasing(spec)
    return then(
        AnimateSpecModifier(
            animationId = key,
            motionDuration = duration,
            motionEasingType = easing,
            visibilityDuration = duration,
            visibilityEasingType = easing,
            enterAnimation = enter.animation,
            exitAnimation = exit.animation,
        )
    )
}

/**
 * Marks this component as a shared bounds container that smoothly transitions its bounds, position,
 * and styling between states in a state layout.
 *
 * @param key unique integer identifier matching this container across states
 * @param spec animation specification for bounds and transforms (e.g. [remoteTween])
 * @param enter transition effect when component enters the layout
 * @param exit transition effect when component exits the layout
 */
public fun RemoteModifier.sharedBounds(
    key: Int,
    spec: RemoteTweenSpec = remoteTween(),
    enter: RemoteEnterTransition = RemoteEnterTransition.FadeIn,
    exit: RemoteExitTransition = RemoteExitTransition.FadeOut,
): RemoteModifier = sharedElement(key = key, spec = spec, enter = enter, exit = exit)

/**
 * Applies enter and exit visibility transitions to a component in a state layout.
 *
 * @param enter transition effect when component enters the layout
 * @param exit transition effect when component exits the layout
 * @param spec animation specification for visibility transitions (e.g. [remoteTween])
 */
public fun RemoteModifier.animateEnterExit(
    enter: RemoteEnterTransition = RemoteEnterTransition.FadeIn,
    exit: RemoteExitTransition = RemoteExitTransition.FadeOut,
    spec: RemoteTweenSpec = remoteTween(),
): RemoteModifier {
    val (duration, easing) = extractDurationAndEasing(spec)
    return then(
        AnimateSpecModifier(
            animationId = 0,
            motionDuration = duration,
            motionEasingType = easing,
            visibilityDuration = duration,
            visibilityEasingType = easing,
            enterAnimation = enter.animation,
            exitAnimation = exit.animation,
        )
    )
}

/**
 * Applies an animation specification for shared bounds and visibility transitions to a component.
 *
 * @param animationId identifier used to match elements for shared transitions (-1 for auto)
 * @param motionSpec animation specification for motion/bounds transitions (e.g. [remoteTween])
 * @param visibilitySpec animation specification for enter/exit visibility transitions
 * @param enter transition effect when component enters the layout
 * @param exit transition effect when component exits the layout
 */
public fun RemoteModifier.animationSpec(
    animationId: Int = -1,
    motionSpec: RemoteTweenSpec = remoteTween(),
    visibilitySpec: RemoteTweenSpec = motionSpec,
    enter: RemoteEnterTransition = RemoteEnterTransition.FadeIn,
    exit: RemoteExitTransition = RemoteExitTransition.FadeOut,
): RemoteModifier {
    val (motionDuration, motionEasing) = extractDurationAndEasing(motionSpec)
    val (visibilityDuration, visibilityEasing) = extractDurationAndEasing(visibilitySpec)
    return then(
        AnimateSpecModifier(
            animationId = animationId,
            motionDuration = motionDuration,
            motionEasingType = motionEasing,
            visibilityDuration = visibilityDuration,
            visibilityEasingType = visibilityEasing,
            enterAnimation = enter.animation,
            exitAnimation = exit.animation,
        )
    )
}

/**
 * Applies an animation specification to match elements for shared transitions across states.
 *
 * @param animationId identifier used to match elements for shared transitions
 * @param enabled whether animation transitions are enabled for this component
 */
public fun RemoteModifier.animationSpec(
    animationId: Int,
    enabled: Boolean,
): RemoteModifier = animationSpec(animationId = if (enabled) animationId else 0)

/**
 * Enables or disables animation transitions for this component.
 *
 * @param enabled whether animation transitions are enabled for this component
 */
public fun RemoteModifier.animationSpec(enabled: Boolean): RemoteModifier =
    animationSpec(animationId = -1, enabled = enabled)

/** Applies an animation specification to a component using explicit durations and easing types. */
@Deprecated(
    "Use sharedElement, animateEnterExit, or animationSpec taking RemoteAnimationSpec instead",
    ReplaceWith(
        "sharedElement(key = animationId, spec = remoteTween(motionDuration.toInt()))",
        "androidx.compose.remote.creation.compose.state.remoteTween",
    ),
)
public fun RemoteModifier.animationSpec(
    animationId: Int = -1,
    motionDuration: Float = 300f,
    motionEasingType: Int = GeneralEasing.CUBIC_STANDARD,
    visibilityDuration: Float = motionDuration,
    visibilityEasingType: Int = motionEasingType,
    enterAnimation: RemoteAnimation = RemoteAnimation.FADE_IN,
    exitAnimation: RemoteAnimation = RemoteAnimation.FADE_OUT,
    enabled: Boolean = true,
): RemoteModifier {
    val id = if (enabled) animationId else 0
    return then(
        AnimateSpecModifier(
            animationId = id,
            motionDuration = motionDuration,
            motionEasingType = motionEasingType,
            visibilityDuration = visibilityDuration,
            visibilityEasingType = visibilityEasingType,
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
        )
    )
}
