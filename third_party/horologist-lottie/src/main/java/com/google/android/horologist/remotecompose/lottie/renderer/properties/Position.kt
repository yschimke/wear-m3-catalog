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
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedPositionProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BasePositionProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.SplitPositionProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticPositionProperty
import com.google.android.horologist.remotecompose.lottie.renderer.lookupValueInBezier
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingIn
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingOut

/** A 2D point represented with RemoteFloats. */
internal data class Point(val x: RemoteFloat, val y: RemoteFloat)

internal data class PositionAnimationSegment(
  val startFrame: Float,
  val x: RemoteFloat,
  val y: RemoteFloat,
)

/**
 * Animates a position property.
 *
 * Takes a [BasePositionProperty] (either static, split, or animated) and resolves it to a [Point]
 * of [RemoteFloat]s (x, y). Supports keyframed transitions with cubic Bézier easing curves, hold
 * keyframes, delayed starts, and split dimensional scalar animations.
 */
@SuppressLint("RestrictedApi")
internal fun animatePosition(
  position: BasePositionProperty,
  animationSettings: LottieSettings,
): Point {
  return when (position) {
    // Static constant position: directly wrap the [x, y] coordinates into RemoteFloats.
    is StaticPositionProperty -> {
      Point(x = position.value.getOrElse(0) { 0f }.rf, y = position.value.getOrElse(1) { 0f }.rf)
    }
    // Split position: evaluate x, y scalar properties independently.
    is SplitPositionProperty -> {
      Point(
        x = animateScalar(position.x, animationSettings),
        y = animateScalar(position.y, animationSettings),
      )
    }
    // Keyframed animated position: interpolate [x, y] across keyframes using Bézier easing curves.
    is AnimatedPositionProperty -> {
      if (position.keyframes.isEmpty()) {
        return Point(0f.rf, 0f.rf)
      }
      // Single keyframe: hold static position at that single value.
      if (position.keyframes.size == 1) {
        return Point(
          x = position.keyframes[0].value.getOrElse(0) { 0f }.rf,
          y = position.keyframes[0].value.getOrElse(1) { 0f }.rf,
        )
      }

      val animationSegments = mutableListOf<PositionAnimationSegment>()

      // If the first keyframe starts after frame 0, prepend an initial static segment
      // holding the first keyframe's value from frame 0 until the first keyframe.
      val firstKeyframe = position.keyframes[0]
      val firstX = firstKeyframe.value.getOrElse(0) { 0f }
      val firstY = firstKeyframe.value.getOrElse(1) { 0f }
      if (firstKeyframe.frame != 0f) {
        animationSegments.add(
          PositionAnimationSegment(startFrame = 0f, x = firstX.rf, y = firstY.rf)
        )
      }

      // Build interpolation segments between adjacent keyframe pairs.
      for (i in 0 until position.keyframes.size - 1) {
        val startKeyframe = position.keyframes[i]
        val endKeyframe = position.keyframes[i + 1]
        val duration = endKeyframe.frame - startKeyframe.frame
        val frameInAnimation = animationSettings.currentFrame - startKeyframe.frame

        val startX = startKeyframe.value.getOrElse(0) { 0f }
        val startY = startKeyframe.value.getOrElse(1) { 0f }
        val endX = endKeyframe.value.getOrElse(0) { startX }
        val endY = endKeyframe.value.getOrElse(1) { startY }

        val (segX, segY) =
          if (startKeyframe.hold || duration <= 0f) {
            val hX = selectIfLt(frameInAnimation, duration.rf, startX.rf, endX.rf)
            val hY = selectIfLt(frameInAnimation, duration.rf, startY.rf, endY.rf)
            hX to hY
          } else {
            val outTanX = startKeyframe.outTangent?.getTangent(0) ?: scalarLinearEasingOut
            val inTanX = startKeyframe.inTangent?.getTangent(0) ?: scalarLinearEasingIn
            val outTanY = startKeyframe.outTangent?.getTangent(1) ?: scalarLinearEasingOut
            val inTanY = startKeyframe.inTangent?.getTangent(1) ?: scalarLinearEasingIn

            val progressX =
              lookupValueInBezier(
                outTanX.x,
                outTanX.y,
                inTanX.x,
                inTanX.y,
                duration,
                frameInAnimation,
              )

            val spatialOut = startKeyframe.spatialOutTangent
            val spatialIn = startKeyframe.spatialInTangent ?: endKeyframe.spatialInTangent

            if (spatialOut != null || spatialIn != null) {
              val toX = spatialOut?.getOrElse(0) { 0f } ?: 0f
              val toY = spatialOut?.getOrElse(1) { 0f } ?: 0f
              val tiX = spatialIn?.getOrElse(0) { 0f } ?: 0f
              val tiY = spatialIn?.getOrElse(1) { 0f } ?: 0f

              val c1x = startX + toX
              val c1y = startY + toY
              val c2x = endX + tiX
              val c2y = endY + tiY

              val s = progressX
              val oneMinusS = 1f.rf - s
              val oneMinusS2 = oneMinusS * oneMinusS
              val oneMinusS3 = oneMinusS2 * oneMinusS
              val s2 = s * s
              val s3 = s2 * s

              val c0 = oneMinusS3
              val c1 = 3f.rf * oneMinusS2 * s
              val c2 = 3f.rf * oneMinusS * s2
              val c3 = s3

              val bX = c0 * startX.rf + c1 * c1x.rf + c2 * c2x.rf + c3 * endX.rf
              val bY = c0 * startY.rf + c1 * c1y.rf + c2 * c2y.rf + c3 * endY.rf
              bX to bY
            } else {
              val progressY =
                if (outTanX == outTanY && inTanX == inTanY) {
                  progressX
                } else {
                  lookupValueInBezier(
                    outTanY.x,
                    outTanY.y,
                    inTanY.x,
                    inTanY.y,
                    duration,
                    frameInAnimation,
                  )
                }
              val lX = lerp(startX.rf, endX.rf, progressX)
              val lY = lerp(startY.rf, endY.rf, progressY)
              lX to lY
            }
          }

        animationSegments.add(
          PositionAnimationSegment(startFrame = startKeyframe.frame, x = segX, y = segY)
        )
      }

      // Chain individual segments together across timeline thresholds.
      chainPositionAnimation(animationSegments, animationSettings.currentFrame)
    }
  }
}

/**
 * Support keyframed position animations (and delayed start animations) by chaining multiple
 * animation segments together across timeline thresholds.
 */
@SuppressLint("RestrictedApi")
private fun chainPositionAnimation(
  segments: List<PositionAnimationSegment>,
  frame: RemoteFloat,
): Point {
  if (segments.size == 1) {
    return Point(segments[0].x, segments[0].y)
  }

  val firstSegment = segments[0]
  val remainingChained = chainPositionAnimation(segments.subList(1, segments.size), frame)
  val nextStartFrame = segments[1].startFrame.rf

  val chainedX = selectIfLt(frame, nextStartFrame, firstSegment.x, remainingChained.x)
  val chainedY = selectIfLt(frame, nextStartFrame, firstSegment.y, remainingChained.y)

  return Point(x = chainedX, y = chainedY)
}
