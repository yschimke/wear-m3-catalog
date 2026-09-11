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
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.RoundedCorners
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers.TrimPath
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedBezierProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseBezierProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BezierPropertyKeyframe
import com.google.android.horologist.remotecompose.lottie.format.values.BezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateBezier
import com.google.android.horologist.remotecompose.lottie.renderer.properties.toRemote
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingIn
import com.google.android.horologist.remotecompose.lottie.renderer.scalarLinearEasingOut
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot

private const val ROUNDED_CORNER_CONTROL_POINT_CONSTANT = 0.5519f

/**
 * Rounds sharp corners of a [BezierValue] subpath with the given [radius].
 *
 * For each sharp vertex (where in and out tangents are zero), the corner is replaced by two
 * vertices and cubic Bézier control points approximating a circular arc of radius `r` (clamped to
 * at most half the length of adjacent edges).
 */
internal fun roundBezierValue(subpath: BezierValue, radius: Float): BezierValue {
  if (subpath.vertices.size < 2) {
    return subpath
  }

  val count = subpath.vertices.size
  val newVertices = mutableListOf<List<Float>>()
  val newInTangents = mutableListOf<List<Float>>()
  val newOutTangents = mutableListOf<List<Float>>()

  for (i in 0 until count) {
    val currX = subpath.vertices[i].getOrElse(0) { 0f }
    val currY = subpath.vertices[i].getOrElse(1) { 0f }
    val inTan = subpath.inTangents.getOrNull(i)
    val inX = inTan?.getOrElse(0) { 0f } ?: 0f
    val inY = inTan?.getOrElse(1) { 0f } ?: 0f
    val outTan = subpath.outTangents.getOrNull(i)
    val outX = outTan?.getOrElse(0) { 0f } ?: 0f
    val outY = outTan?.getOrElse(1) { 0f } ?: 0f

    val isSharp =
      abs(inX) < 0.0001f && abs(inY) < 0.0001f && abs(outX) < 0.0001f && abs(outY) < 0.0001f

    val prevPoint: Point? =
      when {
        i > 0 ->
          Point(
            subpath.vertices[i - 1].getOrElse(0) { 0f },
            subpath.vertices[i - 1].getOrElse(1) { 0f },
          )
        subpath.closed ->
          Point(
            subpath.vertices[count - 1].getOrElse(0) { 0f },
            subpath.vertices[count - 1].getOrElse(1) { 0f },
          )
        else -> null
      }

    val nextPoint: Point? =
      when {
        i < count - 1 ->
          Point(
            subpath.vertices[i + 1].getOrElse(0) { 0f },
            subpath.vertices[i + 1].getOrElse(1) { 0f },
          )
        subpath.closed ->
          Point(subpath.vertices[0].getOrElse(0) { 0f }, subpath.vertices[0].getOrElse(1) { 0f })
        else -> null
      }

    if (prevPoint == null || nextPoint == null || !isSharp) {
      newVertices.add(listOf(currX, currY))
      newInTangents.add(listOf(inX, inY))
      newOutTangents.add(listOf(outX, outY))
    } else {
      val dxPrev = prevPoint.x - currX
      val dyPrev = prevPoint.y - currY
      val lenPrev = hypot(dxPrev, dyPrev)

      val dxNext = nextPoint.x - currX
      val dyNext = nextPoint.y - currY
      val lenNext = hypot(dxNext, dyNext)

      if (lenPrev < 0.0001f || lenNext < 0.0001f) {
        newVertices.add(listOf(currX, currY))
        newInTangents.add(listOf(inX, inY))
        newOutTangents.add(listOf(outX, outY))
      } else {
        val maxR = minOf(maxOf(0f, radius), lenPrev / 2f, lenNext / 2f)
        val tPrev = if (lenPrev > 0.0001f) maxR / lenPrev else 0f
        val tNext = if (lenNext > 0.0001f) maxR / lenNext else 0f

        val pStartX = currX + dxPrev * tPrev
        val pStartY = currY + dyPrev * tPrev
        val pEndX = currX + dxNext * tNext
        val pEndY = currY + dyNext * tNext

        val outTanStartX = (currX - pStartX) * ROUNDED_CORNER_CONTROL_POINT_CONSTANT
        val outTanStartY = (currY - pStartY) * ROUNDED_CORNER_CONTROL_POINT_CONSTANT
        val inTanEndX = (currX - pEndX) * ROUNDED_CORNER_CONTROL_POINT_CONSTANT
        val inTanEndY = (currY - pEndY) * ROUNDED_CORNER_CONTROL_POINT_CONSTANT

        // Add start vertex of rounded corner
        newVertices.add(listOf(pStartX, pStartY))
        newInTangents.add(listOf(0f, 0f))
        newOutTangents.add(listOf(outTanStartX, outTanStartY))

        // Add end vertex of rounded corner
        newVertices.add(listOf(pEndX, pEndY))
        newInTangents.add(listOf(inTanEndX, inTanEndY))
        newOutTangents.add(listOf(0f, 0f))
      }
    }
  }

  return BezierValue(
    closed = subpath.closed,
    inTangents = newInTangents,
    outTangents = newOutTangents,
    vertices = newVertices,
  )
}

/**
 * Evaluates a [BaseBezierProperty] together with optional [TrimPath] and [RoundedCorners] modifiers
 * into a list of [RemoteBezierValue]s.
 */
@SuppressLint("RestrictedApi")
internal fun evaluatePathGeometry(
  bezierProperty: BaseBezierProperty,
  trimPath: TrimPath?,
  roundedCorners: RoundedCorners?,
  animationSettings: LottieSettings,
): List<RemoteBezierValue> {
  val hasRounding = roundedCorners != null && roundedCorners.hidden != true
  val hasTrim = trimPath != null && trimPath.hidden != true

  if (!hasRounding && !hasTrim) {
    return animateBezier(bezierProperty, animationSettings)
  }

  if (!hasRounding) {
    return evaluateTrimmedBezier(bezierProperty, trimPath, animationSettings)
  }

  val isRadiusAnimated = roundedCorners!!.radius is AnimatedScalarProperty
  val isTrimAnimated =
    hasTrim &&
      (trimPath!!.start is AnimatedScalarProperty ||
        trimPath.end is AnimatedScalarProperty ||
        trimPath.offset is AnimatedScalarProperty)
  val isBezierAnimated = bezierProperty is AnimatedBezierProperty

  if (!isRadiusAnimated && !isTrimAnimated && !isBezierAnimated) {
    val r = sampleScalar(roundedCorners.radius, 0f)
    val baseSubpaths = sampleBezier(bezierProperty, 0f)
    val roundedSubpaths = baseSubpaths.map { roundBezierValue(it, r) }
    if (!hasTrim) {
      return roundedSubpaths.map { it.toRemote() }
    }
    val s = sampleScalar(trimPath!!.start, 0f) / 100f
    val e = sampleScalar(trimPath.end, 0f) / 100f
    val o = sampleScalar(trimPath.offset, 0f) / 360f
    val trimmed = roundedSubpaths.flatMap {
      trimBezierValue(it, s, e, o, keepStructureIfDegenerate = false)
    }
    return trimmed.map { it.toRemote() }
  }

  val keyframeTimes = mutableSetOf<Float>()
  (roundedCorners.radius as? AnimatedScalarProperty)?.keyframes?.forEach {
    keyframeTimes.add(it.frame)
  }
  if (hasTrim) {
    (trimPath!!.start as? AnimatedScalarProperty)?.keyframes?.forEach {
      keyframeTimes.add(it.frame)
    }
    (trimPath.end as? AnimatedScalarProperty)?.keyframes?.forEach { keyframeTimes.add(it.frame) }
    (trimPath.offset as? AnimatedScalarProperty)?.keyframes?.forEach { keyframeTimes.add(it.frame) }
  }
  (bezierProperty as? AnimatedBezierProperty)?.keyframes?.forEach { keyframeTimes.add(it.frame) }

  if (keyframeTimes.isEmpty()) {
    val r = sampleScalar(roundedCorners.radius, 0f)
    val baseSubpaths = sampleBezier(bezierProperty, 0f)
    val roundedSubpaths = baseSubpaths.map { roundBezierValue(it, r) }
    if (!hasTrim) {
      return roundedSubpaths.map { it.toRemote() }
    }
    val s = sampleScalar(trimPath!!.start, 0f) / 100f
    val e = sampleScalar(trimPath.end, 0f) / 100f
    val o = sampleScalar(trimPath.offset, 0f) / 360f
    val trimmed = roundedSubpaths.flatMap {
      trimBezierValue(it, s, e, o, keepStructureIfDegenerate = false)
    }
    return trimmed.map { it.toRemote() }
  }

  val sortedTimes = keyframeTimes.sorted()
  val keyframes = mutableListOf<BezierPropertyKeyframe>()

  val sampleTimes =
    if (hasTrim && isTrimAnimated) {
      val frames = mutableSetOf<Float>()
      if (sortedTimes.size <= 1) {
        frames.addAll(sortedTimes)
        frames.add(0f)
      } else {
        for (i in 0 until sortedTimes.size - 1) {
          val t0 = sortedTimes[i]
          val t1 = sortedTimes[i + 1]
          frames.add(t0)
          frames.add(t1)
          val startInt = ceil(t0).toInt()
          val endInt = floor(t1).toInt()
          for (frameInt in startInt..endInt) {
            frames.add(frameInt.toFloat())
          }
        }
      }
      frames.sorted()
    } else {
      sortedTimes
    }

  for (f in sampleTimes) {
    val r = sampleScalar(roundedCorners.radius, f)
    val baseSubpaths = sampleBezier(bezierProperty, f)
    val roundedSubpaths = baseSubpaths.map { roundBezierValue(it, r) }
    val finalSubpaths =
      if (hasTrim) {
        val s = sampleScalar(trimPath!!.start, f) / 100f
        val e = sampleScalar(trimPath.end, f) / 100f
        val o = sampleScalar(trimPath.offset, f) / 360f
        roundedSubpaths.flatMap { trimBezierValue(it, s, e, o, keepStructureIfDegenerate = true) }
      } else {
        roundedSubpaths
      }

    if (hasTrim && isTrimAnimated) {
      keyframes.add(
        BezierPropertyKeyframe(
          frame = f,
          value = finalSubpaths,
          inTangent = scalarLinearEasingIn,
          outTangent = scalarLinearEasingOut,
          hold = false,
        )
      )
    } else {
      val primaryScalarKf =
        (roundedCorners.radius as? AnimatedScalarProperty)?.keyframes?.firstOrNull { it.frame == f }
          ?: if (hasTrim) {
            (trimPath!!.start as? AnimatedScalarProperty)?.keyframes?.firstOrNull { it.frame == f }
              ?: (trimPath.end as? AnimatedScalarProperty)?.keyframes?.firstOrNull { it.frame == f }
              ?: (trimPath.offset as? AnimatedScalarProperty)?.keyframes?.firstOrNull {
                it.frame == f
              }
          } else null

      val bezierKf =
        (bezierProperty as? AnimatedBezierProperty)?.keyframes?.firstOrNull { it.frame == f }

      val inTangent = primaryScalarKf?.inTangent ?: bezierKf?.inTangent
      val outTangent = primaryScalarKf?.outTangent ?: bezierKf?.outTangent
      val hold = primaryScalarKf?.hold ?: bezierKf?.hold ?: false

      keyframes.add(
        BezierPropertyKeyframe(
          frame = f,
          value = finalSubpaths,
          inTangent = inTangent,
          outTangent = outTangent,
          hold = hold,
        )
      )
    }
  }

  val animatedEvaluatedBezier = AnimatedBezierProperty(keyframes = keyframes)
  return animateBezier(animatedEvaluatedBezier, animationSettings)
}
