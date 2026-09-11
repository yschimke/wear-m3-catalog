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
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.ZigZag
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.ZigZagType
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteGroup
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteLottiePath
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteShape
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar
import kotlin.math.ceil
import kotlin.math.hypot

/** Evaluates a [ZigZag] modifier across [shapes], inserting serrated ridges along edges. */
@SuppressLint("RestrictedApi")
internal fun evaluateZigZag(
  shapes: List<RemoteShape>,
  zigZag: ZigZag,
  animationSettings: LottieSettings,
): List<RemoteShape> {
  if (zigZag.hidden == true || shapes.isEmpty()) return shapes

  val size = animateScalar(zigZag.size, animationSettings).constantValueOrNull ?: 0f
  val ridges = animateScalar(zigZag.ridgesPerSegment, animationSettings).constantValueOrNull ?: 0f
  if (size == 0f || ridges <= 0f) return shapes

  return shapes.map { shape ->
    when (shape) {
      is RemoteLottiePath -> {
        val newSubpaths =
          shape.path.map { subpath ->
            applyZigZagToSubpath(subpath, size, ridges, zigZag.pointType)
          }
        RemoteLottiePath(newSubpaths, shape.fillRule)
      }
      is RemoteGroup -> {
        val newChildShapes =
          shape.childShapes.map { styledShapes ->
            com.google.android.horologist.remotecompose.lottie.renderer.StyledShapes(
              shapes = evaluateZigZag(styledShapes.shapes, zigZag, animationSettings),
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
private fun applyZigZagToSubpath(
  subpath: RemoteBezierValue,
  size: Float,
  ridges: Float,
  pointType: ZigZagType,
): RemoteBezierValue {
  val count = subpath.vertices.size
  if (count < 2) return subpath

  val newVertices = mutableListOf<List<RemoteFloat>>()
  val newInTangents = mutableListOf<List<RemoteFloat>>()
  val newOutTangents = mutableListOf<List<RemoteFloat>>()

  val segmentCount = if (subpath.closed) count else count - 1
  val rCount = ceil(ridges).toInt()
  val intermediateCount = 2 * rCount

  for (i in 0 until segmentCount) {
    val nextIdx = (i + 1) % count
    val p0x = subpath.vertices[i].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val p0y = subpath.vertices[i].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f
    val p1x = subpath.vertices[nextIdx].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val p1y = subpath.vertices[nextIdx].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f

    val dx = p1x - p0x
    val dy = p1y - p0y
    val len = hypot(dx, dy)

    val p0InTan = subpath.inTangents.getOrNull(i) ?: listOf(0f.rf, 0f.rf)
    val p0OutTan = subpath.outTangents.getOrNull(i) ?: listOf(0f.rf, 0f.rf)

    newVertices.add(listOf(p0x.rf, p0y.rf))
    newInTangents.add(p0InTan)
    newOutTangents.add(p0OutTan)

    if (len > 0.0001f) {
      val ux = dx / len
      val uy = dy / len
      val nx = -uy
      val ny = ux

      val segLen = len / (intermediateCount + 1).toFloat()
      val tanLen = segLen * 0.5519f

      for (j in 1..intermediateCount) {
        val t = j.toFloat() / (intermediateCount + 1).toFloat()
        val bx = p0x + dx * t
        val by = p0y + dy * t

        val dir = if (j % 2 != 0) 1f else -1f
        val disp = dir * (size / 2f)

        val vx = bx + nx * disp
        val vy = by + ny * disp

        newVertices.add(listOf(vx.rf, vy.rf))

        if (pointType == ZigZagType.Smooth) {
          val outX = ux * tanLen
          val outY = uy * tanLen
          newInTangents.add(listOf((-outX).rf, (-outY).rf))
          newOutTangents.add(listOf(outX.rf, outY.rf))
        } else {
          newInTangents.add(listOf(0f.rf, 0f.rf))
          newOutTangents.add(listOf(0f.rf, 0f.rf))
        }
      }
    }
  }

  if (!subpath.closed) {
    val lastIdx = count - 1
    val lastV = subpath.vertices[lastIdx]
    val lastIn = subpath.inTangents.getOrNull(lastIdx) ?: listOf(0f.rf, 0f.rf)
    val lastOut = subpath.outTangents.getOrNull(lastIdx) ?: listOf(0f.rf, 0f.rf)
    newVertices.add(lastV)
    newInTangents.add(lastIn)
    newOutTangents.add(lastOut)
  }

  return RemoteBezierValue(
    closed = subpath.closed,
    inTangents = newInTangents,
    outTangents = newOutTangents,
    vertices = newVertices,
  )
}
