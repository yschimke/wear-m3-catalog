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
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedVectorProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseVectorProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticVectorProperty
import com.google.android.horologist.remotecompose.lottie.renderer.lookupValueInBezier
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingIn
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingOut

internal data class VectorAnimationSegment(val startFrame: Float, val value: List<RemoteFloat>)

/**
 * Animates a vector property.
 *
 * Takes a [BaseVectorProperty] (either static or animated) and resolves it to a list of
 * [RemoteFloat]s. Supports keyframed transitions with cubic Bézier easing curves, hold keyframes,
 * and delayed starts.
 */
@SuppressLint("RestrictedApi")
internal fun animateVector(
  vector: BaseVectorProperty,
  animationSettings: LottieSettings,
): List<RemoteFloat> {
  return when (vector) {
    is StaticVectorProperty -> vector.value.map { it.rf }
    is AnimatedVectorProperty -> {
      if (vector.keyframes.isEmpty()) {
        return emptyList()
      }
      if (vector.keyframes.size == 1) {
        return vector.keyframes[0].value.map { it.rf }
      }

      val animationSegments = mutableListOf<VectorAnimationSegment>()

      val firstKeyframe = vector.keyframes[0]
      if (firstKeyframe.frame != 0f) {
        animationSegments.add(
          VectorAnimationSegment(startFrame = 0f, value = firstKeyframe.value.map { it.rf })
        )
      }

      for (i in 0 until vector.keyframes.size - 1) {
        val startKeyframe = vector.keyframes[i]
        val endKeyframe = vector.keyframes[i + 1]
        val duration = endKeyframe.frame - startKeyframe.frame
        val frameInAnimation = animationSettings.currentFrame - startKeyframe.frame

        val segmentValue =
          if (startKeyframe.hold || duration <= 0f) {
            startKeyframe.value.mapIndexed { index, startVal ->
              val endVal = endKeyframe.value.getOrElse(index) { startVal }
              selectIfLt(frameInAnimation, duration.rf, startVal.rf, endVal.rf)
            }
          } else {
            val outTan0 = startKeyframe.outTangent?.getTangent(0) ?: scalarLinearEasingOut
            val inTan0 = startKeyframe.inTangent?.getTangent(0) ?: scalarLinearEasingIn
            val progress0 =
              lookupValueInBezier(
                outTan0.x,
                outTan0.y,
                inTan0.x,
                inTan0.y,
                duration,
                frameInAnimation,
              )

            startKeyframe.value.mapIndexed { index, startVal ->
              val endVal = endKeyframe.value.getOrElse(index) { startVal }
              val progress =
                if (index == 0) {
                  progress0
                } else {
                  val outTan = startKeyframe.outTangent?.getTangent(index) ?: scalarLinearEasingOut
                  val inTan = startKeyframe.inTangent?.getTangent(index) ?: scalarLinearEasingIn
                  if (outTan == outTan0 && inTan == inTan0) {
                    progress0
                  } else {
                    lookupValueInBezier(
                      outTan.x,
                      outTan.y,
                      inTan.x,
                      inTan.y,
                      duration,
                      frameInAnimation,
                    )
                  }
                }
              lerp(startVal.rf, endVal.rf, progress)
            }
          }

        animationSegments.add(VectorAnimationSegment(startKeyframe.frame, segmentValue))
      }

      chainVectorAnimation(animationSegments, animationSettings.currentFrame)
    }
  }
}

/**
 * Support keyframed vector animations (and delayed start animations) by chaining multiple animation
 * segments together across timeline thresholds.
 */
@SuppressLint("RestrictedApi")
private fun chainVectorAnimation(
  segments: List<VectorAnimationSegment>,
  frame: RemoteFloat,
): List<RemoteFloat> {
  if (segments.size == 1) {
    return segments[0].value
  }

  val firstSegment = segments[0]
  val remainingChained = chainVectorAnimation(segments.subList(1, segments.size), frame)
  val nextStartFrame = segments[1].startFrame.rf

  return firstSegment.value.mapIndexed { index, coordVal ->
    val remainingVal = remainingChained.getOrElse(index) { coordVal }
    selectIfLt(frame, nextStartFrame, coordVal, remainingVal)
  }
}
