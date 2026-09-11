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
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.lerp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.selectIfLt
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticScalarProperty
import com.google.android.horologist.remotecompose.lottie.renderer.lookupValueInBezier
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingIn
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingOut

internal data class ScalarAnimationSegment(val startFrame: Float, val value: RemoteFloat)

/**
 * Animates a scalar property.
 *
 * Takes a [BaseScalarProperty] (either static or animated) and resolves it to a [RemoteFloat].
 * Supports keyframed transitions with cubic Bézier easing curves, hold keyframes, and delayed
 * starts.
 */
@SuppressLint("RestrictedApi")
internal fun animateScalar(
  scalar: BaseScalarProperty,
  animationSettings: LottieSettings,
): RemoteFloat {
  return when (scalar) {
    is StaticScalarProperty -> scalar.value.rf
    is AnimatedScalarProperty -> {
      if (scalar.keyframes.isEmpty()) {
        return 0f.rf
      }
      if (scalar.keyframes.size == 1) {
        return scalar.keyframes[0].value.rf
      }

      val animationSegments = mutableListOf<ScalarAnimationSegment>()

      val firstKeyframe = scalar.keyframes[0]
      if (firstKeyframe.frame != 0f) {
        animationSegments.add(
          ScalarAnimationSegment(startFrame = 0f, value = firstKeyframe.value.rf)
        )
      }

      for (i in 0 until scalar.keyframes.size - 1) {
        val startKeyframe = scalar.keyframes[i]
        val endKeyframe = scalar.keyframes[i + 1]
        val duration = endKeyframe.frame - startKeyframe.frame
        val frameInAnimation = animationSettings.currentFrame - startKeyframe.frame

        val segmentValue =
          if (startKeyframe.hold || duration <= 0f) {
            selectIfLt(frameInAnimation, duration.rf, startKeyframe.value.rf, endKeyframe.value.rf)
          } else {
            val outTangent = startKeyframe.outTangent ?: scalarLinearEasingOut
            val inTangent = startKeyframe.inTangent ?: scalarLinearEasingIn

            val progress =
              lookupValueInBezier(
                outTangent.x,
                outTangent.y,
                inTangent.x,
                inTangent.y,
                duration,
                frameInAnimation,
              )

            lerp(startKeyframe.value.rf, endKeyframe.value.rf, progress)
          }

        animationSegments.add(ScalarAnimationSegment(startKeyframe.frame, segmentValue))
      }

      chainScalarAnimation(animationSegments, animationSettings.currentFrame)
    }
  }
}

/**
 * Support keyframed scalar animations (and delayed start animations) by chaining multiple animation
 * segments together across timeline thresholds.
 */
@SuppressLint("RestrictedApi")
private fun chainScalarAnimation(
  segments: List<ScalarAnimationSegment>,
  frame: RemoteFloat,
): RemoteFloat {
  if (segments.size == 1) {
    return segments[0].value
  }

  return selectIfLt(
    frame,
    segments[1].startFrame.rf,
    segments[0].value,
    chainScalarAnimation(segments.subList(1, segments.size), frame),
  )
}
