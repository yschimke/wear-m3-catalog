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
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.rf
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.PuckerBloat
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteGroup
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteLottiePath
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteShape
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar

/**
 * Evaluates a [PuckerBloat] modifier across [shapes], bowing vertices and tangents inward/outward.
 */
@SuppressLint("RestrictedApi")
internal fun evaluatePuckerBloat(
  shapes: List<RemoteShape>,
  puckerBloat: PuckerBloat,
  animationSettings: LottieSettings,
): List<RemoteShape> {
  if (puckerBloat.hidden == true || shapes.isEmpty()) return shapes

  val amount = animateScalar(puckerBloat.amount, animationSettings).constantValueOrNull ?: 0f
  if (amount == 0f) return shapes

  return shapes.map { shape ->
    when (shape) {
      is RemoteLottiePath -> {
        val newSubpaths = shape.path.map { subpath -> applyPuckerBloatToSubpath(subpath, amount) }
        RemoteLottiePath(newSubpaths, shape.fillRule)
      }
      is RemoteGroup -> {
        val newChildShapes =
          shape.childShapes.map { styledShapes ->
            com.google.android.horologist.remotecompose.lottie.renderer.StyledShapes(
              shapes = evaluatePuckerBloat(styledShapes.shapes, puckerBloat, animationSettings),
              style = styledShapes.style,
            )
          }
        RemoteGroup(newChildShapes, shape.animationSettings, shape.transform)
      }
      else -> shape
    }
  }
}

@SuppressLint("RestrictedApi")
private fun applyPuckerBloatToSubpath(
  subpath: RemoteBezierValue,
  amount: Float,
): RemoteBezierValue {
  val count = subpath.vertices.size
  if (count < 2) return subpath

  var sumX = 0f
  var sumY = 0f
  for (v in subpath.vertices) {
    sumX += v.getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    sumY += v.getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f
  }
  val cx = sumX / count.toFloat()
  val cy = sumY / count.toFloat()

  val f = amount / 100f

  val newVertices = mutableListOf<List<RemoteFloat>>()
  val newInTangents = mutableListOf<List<RemoteFloat>>()
  val newOutTangents = mutableListOf<List<RemoteFloat>>()

  for (i in 0 until count) {
    val vx = subpath.vertices[i].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val vy = subpath.vertices[i].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f

    val inTan = subpath.inTangents.getOrNull(i)
    val inX = inTan?.getOrElse(0) { 0f.rf }?.constantValueOrNull ?: 0f
    val inY = inTan?.getOrElse(1) { 0f.rf }?.constantValueOrNull ?: 0f

    val outTan = subpath.outTangents.getOrNull(i)
    val outX = outTan?.getOrElse(0) { 0f.rf }?.constantValueOrNull ?: 0f
    val outY = outTan?.getOrElse(1) { 0f.rf }?.constantValueOrNull ?: 0f

    val dx = vx - cx
    val dy = vy - cy

    // For pucker (f < 0), pull vertex inward towards center
    val newVx = if (f < 0f) vx + dx * f * 0.5f else vx + dx * f * 0.1f
    val newVy = if (f < 0f) vy + dy * f * 0.5f else vy + dy * f * 0.1f

    // Tangents bow relative to the center vector
    val newOutX = outX - dx * f * 0.5519f
    val newOutY = outY - dy * f * 0.5519f
    val newInX = inX + dx * f * 0.5519f
    val newInY = inY + dy * f * 0.5519f

    newVertices.add(listOf(newVx.rf, newVy.rf))
    newInTangents.add(listOf(newInX.rf, newInY.rf))
    newOutTangents.add(listOf(newOutX.rf, newOutY.rf))
  }

  return RemoteBezierValue(
    closed = subpath.closed,
    inTangents = newInTangents,
    outTangents = newOutTangents,
    vertices = newVertices,
  )
}
