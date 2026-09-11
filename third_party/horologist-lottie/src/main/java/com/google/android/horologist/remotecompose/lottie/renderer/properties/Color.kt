/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.horologist.remotecompose.lottie.renderer.properties

import android.annotation.SuppressLint
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.tween
import androidx.compose.ui.graphics.Color
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedColorProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseColorProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticColorProperty
import com.google.android.horologist.remotecompose.lottie.renderer.lookupValueInBezier
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingIn
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingOut

internal data class ColorAnimationSegment(val startFrame: Float, val value: RemoteColor)

/**
 * Animates a color property.
 *
 * Takes a [BaseColorProperty] (either static or animated) and resolves it to a [RemoteColor].
 * Supports dynamic slot color overrides from [LottieSettings.slotMap], keyframed transitions with
 * cubic Bézier easing curves, and hold keyframes.
 */
@SuppressLint("RestrictedApi")
internal fun animateColor(
  property: BaseColorProperty,
  animationSettings: LottieSettings,
): RemoteColor {
  val slotColor = property.slotId?.let { animationSettings.slotMap.getColor(it) }
  if (slotColor != null) {
    return slotColor
  }

  return when (property) {
    is StaticColorProperty -> property.value
    is AnimatedColorProperty -> {
      if (property.keyframes.isEmpty()) {
        return Color.Transparent.rc
      }
      if (property.keyframes.size == 1) {
        return property.keyframes[0].value
      }

      val animationSegments = mutableListOf<ColorAnimationSegment>()

      val firstKeyframe = property.keyframes[0]
      if (firstKeyframe.frame != 0f) {
        animationSegments.add(ColorAnimationSegment(0f, firstKeyframe.value))
      }

      for (i in 0 until property.keyframes.size - 1) {
        val startKeyframe = property.keyframes[i]
        val endKeyframe = property.keyframes[i + 1]
        val duration = endKeyframe.frame - startKeyframe.frame
        val frameInAnimation = animationSettings.currentFrame - startKeyframe.frame

        val segmentValue =
          if (startKeyframe.hold) {
            frameInAnimation
              .isLessThan(duration.rf)
              .select(ifTrue = startKeyframe.value, ifFalse = endKeyframe.value)
          } else {
            val outTangent = startKeyframe.outTangent ?: scalarLinearEasingOut
            val inTangent = startKeyframe.inTangent ?: scalarLinearEasingIn

            val currentBezierValue =
              lookupValueInBezier(
                outTangent.x,
                outTangent.y,
                inTangent.x,
                inTangent.y,
                duration,
                frameInAnimation,
              )

            tween(startKeyframe.value, endKeyframe.value, currentBezierValue)
          }

        animationSegments.add(ColorAnimationSegment(startKeyframe.frame, segmentValue))
      }

      chainColorAnimation(animationSegments, animationSettings.currentFrame)
    }
  }
}

/**
 * Support keyframed color animations (and delayed start animations) by chaining multiple animations
 * together.
 */
@SuppressLint("RestrictedApi")
private fun chainColorAnimation(
  segments: List<ColorAnimationSegment>,
  frame: RemoteFloat,
): RemoteColor {
  if (segments.size == 1) {
    return segments[0].value
  }

  return frame
    .isLessThan(segments[1].startFrame.rf)
    .select(
      ifTrue = segments[0].value,
      ifFalse = chainColorAnimation(segments.subList(1, segments.size), frame),
    )
}
