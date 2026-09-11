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
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.OffsetPath
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.LineJoin
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteGroup
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteLottiePath
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteShape
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar
import kotlin.math.hypot

/** Evaluates an [OffsetPath] modifier across [shapes], expanding or shrinking path contours. */
@SuppressLint("RestrictedApi")
internal fun evaluateOffsetPath(
  shapes: List<RemoteShape>,
  offsetPath: OffsetPath,
  animationSettings: LottieSettings,
): List<RemoteShape> {
  if (offsetPath.hidden == true || shapes.isEmpty()) return shapes

  val amount = animateScalar(offsetPath.amount, animationSettings).constantValueOrNull ?: 0f
  if (amount == 0f) return shapes

  val miterLimit =
    offsetPath.miterLimit?.let { animateScalar(it, animationSettings).constantValueOrNull }
      ?: offsetPath.miterLimitNumeric
      ?: 4f

  return shapes.map { shape ->
    when (shape) {
      is RemoteLottiePath -> {
        val newSubpaths =
          shape.path.map { subpath ->
            applyOffsetToSubpath(subpath, amount, offsetPath.lineJoin, miterLimit)
          }
        RemoteLottiePath(newSubpaths, shape.fillRule)
      }
      is RemoteGroup -> {
        val newChildShapes =
          shape.childShapes.map { styledShapes ->
            com.google.android.horologist.remotecompose.lottie.renderer.StyledShapes(
              shapes = evaluateOffsetPath(styledShapes.shapes, offsetPath, animationSettings),
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
private fun applyOffsetToSubpath(
  subpath: RemoteBezierValue,
  amount: Float,
  lineJoin: LineJoin,
  miterLimit: Float,
): RemoteBezierValue {
  val count = subpath.vertices.size
  if (count < 2) return subpath

  // Determine winding order via signed polygon area
  var signedArea = 0f
  for (i in 0 until count) {
    val nextIdx = (i + 1) % count
    val x0 = subpath.vertices[i].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val y0 = subpath.vertices[i].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f
    val x1 = subpath.vertices[nextIdx].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val y1 = subpath.vertices[nextIdx].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f
    signedArea += (x0 * y1 - x1 * y0)
  }
  val isClockwise = signedArea > 0f

  val edgeNormals = mutableListOf<Point>()
  val edgeDirs = mutableListOf<Point>()

  val segmentCount = if (subpath.closed) count else count - 1
  for (i in 0 until segmentCount) {
    val nextIdx = (i + 1) % count
    val x0 = subpath.vertices[i].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val y0 = subpath.vertices[i].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f
    val x1 = subpath.vertices[nextIdx].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
    val y1 = subpath.vertices[nextIdx].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f

    val dx = x1 - x0
    val dy = y1 - y0
    val len = hypot(dx, dy)
    if (len > 0.0001f) {
      val ux = dx / len
      val uy = dy / len
      edgeDirs.add(Point(ux, uy))
      // Outward normal:
      // In standard screen coords (Y down):
      // For CW winding: (dy/len, -dx/len) points outward / right
      // For CCW winding: (-dy/len, dx/len) points outward / left
      val nx = if (isClockwise) uy else -uy
      val ny = if (isClockwise) -ux else ux
      edgeNormals.add(Point(nx, ny))
    } else {
      edgeDirs.add(Point(1f, 0f))
      edgeNormals.add(Point(0f, 1f))
    }
  }

  val newVertices = mutableListOf<List<RemoteFloat>>()
  val newInTangents = mutableListOf<List<RemoteFloat>>()
  val newOutTangents = mutableListOf<List<RemoteFloat>>()

  if (subpath.closed) {
    for (i in 0 until count) {
      val prevIdx = if (i == 0) count - 1 else i - 1
      val vx = subpath.vertices[i].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
      val vy = subpath.vertices[i].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f

      val nPrev = edgeNormals[prevIdx]
      val nCurr = edgeNormals[i]
      val dPrev = edgeDirs[prevIdx]
      val dCurr = edgeDirs[i]

      // Offset points along previous and current edge
      val pPrev = Point(vx + nPrev.x * amount, vy + nPrev.y * amount)
      val pCurr = Point(vx + nCurr.x * amount, vy + nCurr.y * amount)

      // Compute intersection of offset lines
      val det = dPrev.x * dCurr.y - dPrev.y * dCurr.x
      val offsetVertex =
        if (kotlin.math.abs(det) > 0.001f) {
          val t = ((pCurr.x - pPrev.x) * dCurr.y - (pCurr.y - pPrev.y) * dCurr.x) / det
          val ix = pPrev.x + t * dPrev.x
          val iy = pPrev.y + t * dPrev.y
          val miterDist = hypot(ix - vx, iy - vy)
          if (miterDist > miterLimit * kotlin.math.abs(amount)) {
            pCurr
          } else {
            Point(ix, iy)
          }
        } else {
          pCurr
        }

      newVertices.add(listOf(offsetVertex.x.rf, offsetVertex.y.rf))
      newInTangents.add(subpath.inTangents.getOrNull(i) ?: listOf(0f.rf, 0f.rf))
      newOutTangents.add(subpath.outTangents.getOrNull(i) ?: listOf(0f.rf, 0f.rf))
    }
  } else {
    for (i in 0 until count) {
      val vx = subpath.vertices[i].getOrElse(0) { 0f.rf }.constantValueOrNull ?: 0f
      val vy = subpath.vertices[i].getOrElse(1) { 0f.rf }.constantValueOrNull ?: 0f

      val offsetVertex =
        if (i == 0 && edgeNormals.isNotEmpty()) {
          val n = edgeNormals[0]
          Point(vx + n.x * amount, vy + n.y * amount)
        } else if (i == count - 1 && edgeNormals.isNotEmpty()) {
          val n = edgeNormals.last()
          Point(vx + n.x * amount, vy + n.y * amount)
        } else if (edgeNormals.isNotEmpty()) {
          val nPrev = edgeNormals[i - 1]
          val nCurr = edgeNormals[i]
          Point(vx + (nPrev.x + nCurr.x) * 0.5f * amount, vy + (nPrev.y + nCurr.y) * 0.5f * amount)
        } else {
          Point(vx, vy)
        }

      newVertices.add(listOf(offsetVertex.x.rf, offsetVertex.y.rf))
      newInTangents.add(subpath.inTangents.getOrNull(i) ?: listOf(0f.rf, 0f.rf))
      newOutTangents.add(subpath.outTangents.getOrNull(i) ?: listOf(0f.rf, 0f.rf))
    }
  }

  return RemoteBezierValue(
    closed = subpath.closed,
    inTangents = newInTangents,
    outTangents = newOutTangents,
    vertices = newVertices,
  )
}
