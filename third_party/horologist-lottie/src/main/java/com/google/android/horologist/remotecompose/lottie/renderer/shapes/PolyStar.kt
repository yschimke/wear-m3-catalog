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
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.geometry.PolyStar
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.geometry.PolyStarType
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.RoundedCorners
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.TrimPath
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticBezierProperty
import com.google.android.horologist.remotecompose.lottie.format.values.BezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.RemoteLottiePath
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animatePosition
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

// Note: We deliberately do not use `androidx.graphics.shapes.RoundedPolygon` here because:
// 1. Lottie defines its own exact Bézier tangent calculation and rounding constants (0.47829 for
//    stars, 0.25 for polygons) matching Bodymovin/After Effects and lottie-android, whereas
//    RoundedPolygon cuts corner arcs with a different circular/smoothed curvature profile.
// 2. Lottie polystars support fractional points (e.g. 5.5 points) smoothly morphing the last
//    vertex, while RoundedPolygon requires an integer vertex count.
// 3. Lottie shapes support explicit path direction (e.g. counter-clockwise d=3) affecting fill
//    winding rules.
// 4. Constructing RemoteBezierValue enables affine transform baking in GeometryTransform.kt and
//    preserves 1:1 visual parity across group transformations.

/** Evaluates a Lottie [PolyStar] parametric shape into a [RemoteLottiePath]. */
@SuppressLint("RestrictedApi")
internal fun evaluatePolyStar(
  star: PolyStar,
  animationSettings: LottieSettings,
  trimPath: TrimPath? = null,
  roundedCorners: RoundedCorners? = null,
): RemoteLottiePath? {
  if (star.hidden == true) return null

  val pos = animatePosition(star.position, animationSettings)
  val posX = pos.x.constantValueOrNull ?: 0f
  val posY = pos.y.constantValueOrNull ?: 0f

  val points = animateScalar(star.points, animationSettings).constantValueOrNull ?: 0f
  val rotation = animateScalar(star.rotation, animationSettings).constantValueOrNull ?: 0f
  val outerRadius = animateScalar(star.outerRadius, animationSettings).constantValueOrNull ?: 0f
  val outerRoundedness =
    (animateScalar(star.outerRoundedness, animationSettings).constantValueOrNull ?: 0f) / 100f

  val subpath =
    when (star.starType) {
      PolyStarType.Star -> {
        val innerRadius =
          star.innerRadius?.let { animateScalar(it, animationSettings).constantValueOrNull } ?: 0f
        val innerRoundedness =
          (star.innerRoundedness?.let { animateScalar(it, animationSettings).constantValueOrNull }
            ?: 0f) / 100f
        createStarBezier(
          points = points,
          positionX = posX,
          positionY = posY,
          rotation = rotation,
          innerRadius = innerRadius,
          outerRadius = outerRadius,
          innerRoundedness = innerRoundedness,
          outerRoundedness = outerRoundedness,
        )
      }
      PolyStarType.Polygon -> {
        createPolygonBezier(
          points = points,
          positionX = posX,
          positionY = posY,
          rotation = rotation,
          radius = outerRadius,
          roundedness = outerRoundedness,
        )
      }
    }

  val hasTrim = trimPath != null && trimPath.hidden != true
  val hasRounding = roundedCorners != null && roundedCorners.hidden != true
  if (hasTrim || hasRounding) {
    val bezierValue =
      BezierValue(
        closed = subpath.closed,
        vertices = subpath.vertices.map { pt -> pt.map { it.constantValueOrNull ?: 0f } },
        inTangents = subpath.inTangents.map { pt -> pt.map { it.constantValueOrNull ?: 0f } },
        outTangents = subpath.outTangents.map { pt -> pt.map { it.constantValueOrNull ?: 0f } },
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

  return RemoteLottiePath(listOf(subpath))
}

@SuppressLint("RestrictedApi")
private fun createStarBezier(
  points: Float,
  positionX: Float,
  positionY: Float,
  rotation: Float,
  innerRadius: Float,
  outerRadius: Float,
  innerRoundedness: Float,
  outerRoundedness: Float,
): RemoteBezierValue {
  if (points <= 0f) {
    return RemoteBezierValue(closed = true, emptyList(), emptyList(), emptyList())
  }

  var currentAngle = Math.toRadians((rotation - 90.0)).toFloat()
  val anglePerPoint = (2.0 * PI / points).toFloat()
  val halfAnglePerPoint = anglePerPoint / 2.0f
  val partialPointAmount = points - points.toInt()

  var x: Float
  var y: Float
  var previousX: Float
  var previousY: Float
  var partialPointRadius = 0f

  if (partialPointAmount != 0f) {
    partialPointRadius = innerRadius + partialPointAmount * (outerRadius - innerRadius)
    x = (partialPointRadius * cos(currentAngle.toDouble())).toFloat()
    y = (partialPointRadius * sin(currentAngle.toDouble())).toFloat()
    currentAngle += anglePerPoint * partialPointAmount / 2f
  } else {
    x = (outerRadius * cos(currentAngle.toDouble())).toFloat()
    y = (outerRadius * sin(currentAngle.toDouble())).toFloat()
    currentAngle += halfAnglePerPoint
  }

  val numPoints = ceil(points.toDouble()).toInt() * 2
  val vertices = ArrayList<List<RemoteFloat>>(numPoints)
  val inTangents = ArrayList<List<RemoteFloat>>(numPoints)
  val outTangents = ArrayList<List<RemoteFloat>>(numPoints)

  for (k in 0 until numPoints) {
    inTangents.add(listOf(0f.rf, 0f.rf))
    outTangents.add(listOf(0f.rf, 0f.rf))
  }

  vertices.add(listOf((x + positionX).rf, (y + positionY).rf))

  var longSegment = false
  for (i in 0 until numPoints) {
    var radius = if (longSegment) outerRadius else innerRadius
    var dTheta = halfAnglePerPoint
    if (partialPointRadius != 0f && i == numPoints - 2) {
      dTheta = anglePerPoint * partialPointAmount / 2f
    }
    if (partialPointRadius != 0f && i == numPoints - 1) {
      radius = partialPointRadius
    }
    previousX = x
    previousY = y
    x = (radius * cos(currentAngle.toDouble())).toFloat()
    y = (radius * sin(currentAngle.toDouble())).toFloat()

    val targetIndex = (i + 1) % numPoints
    if (i < numPoints - 1) {
      vertices.add(listOf((x + positionX).rf, (y + positionY).rf))
    }

    if (innerRoundedness != 0f || outerRoundedness != 0f) {
      val cp1Theta = (atan2(previousY.toDouble(), previousX.toDouble()) - PI / 2.0).toFloat()
      val cp1Dx = cos(cp1Theta.toDouble()).toFloat()
      val cp1Dy = sin(cp1Theta.toDouble()).toFloat()

      val cp2Theta = (atan2(y.toDouble(), x.toDouble()) - PI / 2.0).toFloat()
      val cp2Dx = cos(cp2Theta.toDouble()).toFloat()
      val cp2Dy = sin(cp2Theta.toDouble()).toFloat()

      val cp1Roundedness = if (longSegment) innerRoundedness else outerRoundedness
      val cp2Roundedness = if (longSegment) outerRoundedness else innerRoundedness
      val cp1Radius = if (longSegment) innerRadius else outerRadius
      val cp2Radius = if (longSegment) outerRadius else innerRadius

      var cp1x = cp1Radius * cp1Roundedness * 0.47829f * cp1Dx
      var cp1y = cp1Radius * cp1Roundedness * 0.47829f * cp1Dy
      var cp2x = cp2Radius * cp2Roundedness * 0.47829f * cp2Dx
      var cp2y = cp2Radius * cp2Roundedness * 0.47829f * cp2Dy
      if (partialPointAmount != 0f) {
        if (i == 0) {
          cp1x *= partialPointAmount
          cp1y *= partialPointAmount
        } else if (i == numPoints - 1) {
          cp2x *= partialPointAmount
          cp2y *= partialPointAmount
        }
      }

      outTangents[i] = listOf((-cp1x).rf, (-cp1y).rf)
      inTangents[targetIndex] = listOf(cp2x.rf, cp2y.rf)
    }

    currentAngle += dTheta
    longSegment = !longSegment
  }

  return RemoteBezierValue(
    closed = true,
    inTangents = inTangents,
    outTangents = outTangents,
    vertices = vertices,
  )
}

@SuppressLint("RestrictedApi")
private fun createPolygonBezier(
  points: Float,
  positionX: Float,
  positionY: Float,
  rotation: Float,
  radius: Float,
  roundedness: Float,
): RemoteBezierValue {
  if (points < 3f) {
    return RemoteBezierValue(closed = true, emptyList(), emptyList(), emptyList())
  }

  val pts = floor(points.toDouble()).toInt()
  var currentAngle = Math.toRadians((rotation - 90.0)).toFloat()
  val anglePerPoint = (2.0 * PI / pts).toFloat()

  var x = (radius * cos(currentAngle.toDouble())).toFloat()
  var y = (radius * sin(currentAngle.toDouble())).toFloat()
  currentAngle += anglePerPoint

  var previousX: Float
  var previousY: Float
  val numPoints = ceil(points.toDouble()).toInt()

  val vertices = ArrayList<List<RemoteFloat>>(numPoints)
  val inTangents = ArrayList<List<RemoteFloat>>(numPoints)
  val outTangents = ArrayList<List<RemoteFloat>>(numPoints)

  for (k in 0 until numPoints) {
    inTangents.add(listOf(0f.rf, 0f.rf))
    outTangents.add(listOf(0f.rf, 0f.rf))
  }

  vertices.add(listOf((x + positionX).rf, (y + positionY).rf))

  for (i in 0 until numPoints) {
    previousX = x
    previousY = y
    x = (radius * cos(currentAngle.toDouble())).toFloat()
    y = (radius * sin(currentAngle.toDouble())).toFloat()

    val targetIndex = (i + 1) % numPoints
    if (i < numPoints - 1) {
      vertices.add(listOf((x + positionX).rf, (y + positionY).rf))
    }

    if (roundedness != 0f) {
      val cp1Theta = (atan2(previousY.toDouble(), previousX.toDouble()) - PI / 2.0).toFloat()
      val cp1Dx = cos(cp1Theta.toDouble()).toFloat()
      val cp1Dy = sin(cp1Theta.toDouble()).toFloat()

      val cp2Theta = (atan2(y.toDouble(), x.toDouble()) - PI / 2.0).toFloat()
      val cp2Dx = cos(cp2Theta.toDouble()).toFloat()
      val cp2Dy = sin(cp2Theta.toDouble()).toFloat()

      val cp1x = radius * roundedness * 0.25f * cp1Dx
      val cp1y = radius * roundedness * 0.25f * cp1Dy
      val cp2x = radius * roundedness * 0.25f * cp2Dx
      val cp2y = radius * roundedness * 0.25f * cp2Dy

      outTangents[i] = listOf((-cp1x).rf, (-cp1y).rf)
      inTangents[targetIndex] = listOf(cp2x.rf, cp2y.rf)
    }

    currentAngle += anglePerPoint
  }

  return RemoteBezierValue(
    closed = true,
    inTangents = inTangents,
    outTangents = outTangents,
    vertices = vertices,
  )
}
