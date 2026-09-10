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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.wear.compose.material3.internal.GeneratedVectorAnimations
import androidx.wear.compose.material3.internal.LocalWristOrientation
import androidx.wear.compose.material3.internal.VectorAnimationTrack
import androidx.wear.compose.material3.internal.totalDurationMillis
import androidx.wear.compose.material3.internal.isLeftWrist
import androidx.wear.compose.material3.internal.rememberAnimatedVectorPainter
import ee.schimke.wearcmp.material3.resources.Res
import ee.schimke.wearcmp.material3.resources.wear_one_handed_gesture_dismiss_indicator_animation
import ee.schimke.wearcmp.material3.resources.wear_one_handed_gesture_primary_indicator_animation
import org.jetbrains.compose.resources.vectorResource

/**
 * Replaces upstream's file of the same name, which the exclusion in `transform-rules.json` removes.
 *
 * Everything here is upstream's, member for member; the two that could not come across are the ones
 * that reach for `R.drawable` through `androidx.compose.animation.graphics`, which publishes for
 * Android only. The artwork is the same `<animated-vector>` XML, loaded through Compose resources,
 * and it MOVES: these three drawables morph `pathData`, which is what the port's own
 * `AnimatedVectorPainter` learned to interpolate for them.
 */
@Composable
internal fun GestureIndicatorImage(
    painter: Painter,
    size: DpSize,
    tint: Color,
    scaleX: () -> Float = { 1.0f },
    scaleY: () -> Float = { 1.0f },
) {
    // animatedVectorPainter hardcodes autoMirror = true, which reacts to
    // LocalLayoutDirection. To gain manual control over mirroring, we force
    // LayoutDirection.Ltr and apply a horizontal scale based on the wrist orientation.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val wristOrientation = LocalWristOrientation.current
        Image(
            painter = painter,
            contentDescription = null,
            modifier =
                Modifier.size(size).graphicsLayer {
                    // Mirror the image only when worn on the right hand
                    this.scaleX = if (wristOrientation.isLeftWrist()) scaleX() else -scaleX()
                    this.scaleY = scaleY()
                },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(tint),
        )
    }
}

/**
 * The hint artwork for this action, with the motion the generator read out of the same file.
 *
 * Upstream returns an `AnimatedImageVector`, whose `targets` are `internal` to a library that does
 * not publish off Android — so the pairing is carried here instead: the still, and its tracks.
 */
internal class GestureIndicatorAnimation(
    val image: ImageVector,
    val tracks: List<VectorAnimationTrack>,
) {
    /**
     * How long the whole thing runs, in milliseconds — `AnimatedImageVector.totalDuration`, which
     * the indicators read to decide how long to keep themselves on screen.
     */
    val totalDuration: Int = tracks.totalDurationMillis
}

@Composable
internal fun OneHandedGestureAction.animatedImageVector(): GestureIndicatorAnimation {
    val name =
        when (this) {
            OneHandedGestureAction.Primary ->
                "wear_one_handed_gesture_primary_indicator_animation"
            else -> "wear_one_handed_gesture_dismiss_indicator_animation"
        }
    val image =
        when (this) {
            OneHandedGestureAction.Primary ->
                vectorResource(Res.drawable.wear_one_handed_gesture_primary_indicator_animation)
            else ->
                vectorResource(Res.drawable.wear_one_handed_gesture_dismiss_indicator_animation)
        }
    return GestureIndicatorAnimation(image, GeneratedVectorAnimations.getValue(name))
}

/** Upstream's `rememberAnimatedVectorPainter(animatedImageVector, atEnd)`, over the port's own. */
@Composable
internal fun rememberAnimatedVectorPainter(
    animatedImageVector: GestureIndicatorAnimation,
    atEnd: Boolean,
): Painter =
    rememberAnimatedVectorPainter(
        image = animatedImageVector.image,
        tracks = animatedImageVector.tracks,
        atEnd = atEnd,
        // The indicators run their own reduce-motion handling upstream — they simply are not shown
        // — so the painter is never asked to shorten anything here.
        reduceMotion = false,
    )

internal val EXPRESSIVE_DEFAULT_SPATIAL_SPRING_FLOAT =
    spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 350f)

internal val EXPRESSIVE_DEFAULT_EFFECTS_SPRING_FLOAT =
    spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f)

internal val EXPRESSIVE_DEFAULT_EFFECTS_SPRING_COLOR =
    spring<Color>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f)

internal const val INDICATOR_ANIMATION_START_DELAY_MILLIS = 450L
internal const val POST_INDICATOR_ANIMATION_DELAY_MILLIS = 200L
