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

package com.google.android.horologist.remotecompose.lottie.renderer.shapes

import android.annotation.SuppressLint
import androidx.compose.remote.creation.compose.state.clamp
import androidx.compose.remote.creation.compose.state.min
import androidx.compose.remote.creation.compose.state.rf
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.geometry.Rectangle
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.RoundedCorners
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.TrimPath
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticBezierProperty
import com.google.android.horologist.remotecompose.lottie.format.values.BezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteLottiePath
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animatePosition
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateVector

private const val RECTANGLE_CORNER_RADIUS_CONTROL_POINT_CONSTANT = 0.55228475f

/** Evaluates a Lottie [Rectangle] parametric shape into a [RemoteLottiePath]. */
@SuppressLint("RestrictedApi")
internal fun evaluateRectangle(
  rect: Rectangle,
  animationSettings: LottieSettings,
  trimPath: TrimPath? = null,
  roundedCorners: RoundedCorners? = null,
): RemoteLottiePath? {
  if (rect.hidden == true) return null

  val pos = animatePosition(rect.position, animationSettings)
  val size = animateVector(rect.size, animationSettings)
  val width = size.getOrElse(0) { 0f.rf }
  val height = size.getOrElse(1) { 0f.rf }
  val halfWidth = width / 2f
  val halfHeight = height / 2f

  val cornerRadius = animateScalar(rect.cornerRadius, animationSettings)
  val maxRadius = min(halfWidth, halfHeight)
  val clampedR = clamp(cornerRadius, 0f.rf, maxRadius)
  val kr = clampedR * RECTANGLE_CORNER_RADIUS_CONTROL_POINT_CONSTANT
  val rr = clampedR

  val vertices =
    listOf(
      listOf(pos.x + halfWidth, pos.y - halfHeight + rr),
      listOf(pos.x + halfWidth, pos.y + halfHeight - rr),
      listOf(pos.x + halfWidth - rr, pos.y + halfHeight),
      listOf(pos.x - halfWidth + rr, pos.y + halfHeight),
      listOf(pos.x - halfWidth, pos.y + halfHeight - rr),
      listOf(pos.x - halfWidth, pos.y - halfHeight + rr),
      listOf(pos.x - halfWidth + rr, pos.y - halfHeight),
      listOf(pos.x + halfWidth - rr, pos.y - halfHeight),
    )
  val inTangents =
    listOf(
      listOf(0f.rf, -kr),
      listOf(0f.rf, 0f.rf),
      listOf(kr, 0f.rf),
      listOf(0f.rf, 0f.rf),
      listOf(0f.rf, kr),
      listOf(0f.rf, 0f.rf),
      listOf(-kr, 0f.rf),
      listOf(0f.rf, 0f.rf),
    )
  val outTangents =
    listOf(
      listOf(0f.rf, 0f.rf),
      listOf(0f.rf, kr),
      listOf(0f.rf, 0f.rf),
      listOf(-kr, 0f.rf),
      listOf(0f.rf, 0f.rf),
      listOf(0f.rf, -kr),
      listOf(0f.rf, 0f.rf),
      listOf(kr, 0f.rf),
    )

  val remoteBezier =
    RemoteBezierValue(
      closed = true,
      inTangents = inTangents,
      outTangents = outTangents,
      vertices = vertices,
    )

  val hasTrim = trimPath != null && trimPath.hidden != true
  val hasRounding = roundedCorners != null && roundedCorners.hidden != true
  if (hasTrim || hasRounding) {
    val bezierValue =
      BezierValue(
        closed = remoteBezier.closed,
        vertices = remoteBezier.vertices.map { pt -> pt.map { it.constantValueOrNull ?: 0f } },
        inTangents = remoteBezier.inTangents.map { pt -> pt.map { it.constantValueOrNull ?: 0f } },
        outTangents = remoteBezier.outTangents.map { pt -> pt.map { it.constantValueOrNull ?: 0f } },
      )
    val evaluated =
      evaluatePathGeometry(
        StaticBezierProperty(value = bezierValue),
        trimPath,
        roundedCorners,
        animationSettings,
      )
    return RemoteLottiePath(evaluated)
  }

  return RemoteLottiePath(listOf(remoteBezier))
}
