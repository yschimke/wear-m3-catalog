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

package com.google.android.horologist.remotecompose.lottie.renderer

import android.annotation.SuppressLint
import androidx.compose.remote.creation.compose.shaders.RemoteLinearShader
import androidx.compose.remote.creation.compose.shaders.RemoteRadialShader
import androidx.compose.remote.creation.compose.shaders.RemoteShader
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemotePaint
import androidx.compose.remote.creation.compose.state.lerp
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.selectIfLt
import androidx.compose.remote.creation.compose.state.sqrt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.FillRule
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.GradientType
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.LineCap
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.LineJoin
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.StrokeDash
import com.google.android.horologist.remotecompose.lottie.format.properties.AnimatedScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticScalarProperty
import com.google.android.horologist.remotecompose.lottie.renderer.properties.Point
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteGradientValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar

internal interface RemoteStyle {
  fun getPaint(inheritedOpacity: RemoteFloat = 1f.rf): RemotePaint
}

@SuppressLint("RestrictedApi")
internal class RemoteStyleWithOpacity(
  val baseStyle: RemoteStyle,
  val opacityMultiplier: RemoteFloat,
) : RemoteStyle {
  override fun getPaint(inheritedOpacity: RemoteFloat): RemotePaint {
    return baseStyle.getPaint(inheritedOpacity * opacityMultiplier)
  }
}

@SuppressLint("RestrictedApi")
internal class RemoteFill(
  val fillColor: RemoteColor,
  val opacity: RemoteFloat = 100f.rf,
  val fillRule: FillRule = FillRule.NonZero,
) : RemoteStyle {
  override fun getPaint(inheritedOpacity: RemoteFloat): RemotePaint {
    return RemotePaint {
      val effectiveAlpha = fillColor.alpha * (opacity / 100f) * inheritedOpacity
      this.color = fillColor.copy(alpha = effectiveAlpha)
    }
  }
}

@SuppressLint("RestrictedApi")
internal class RemoteStroke(
  val strokeColor: RemoteColor,
  val strokeWidth: RemoteFloat,
  val opacity: RemoteFloat,
  val lineCap: LineCap = LineCap.Round,
  val lineJoin: LineJoin = LineJoin.Round,
  val miterLimit: RemoteFloat? = null,
  val dashPattern: PathEffect? = null,
) : RemoteStyle {
  override fun getPaint(inheritedOpacity: RemoteFloat): RemotePaint {
    return RemotePaint {
      val baseAlpha = strokeColor.alpha * (opacity / 100f) * inheritedOpacity
      val effectiveAlpha = selectIfLt(this@RemoteStroke.strokeWidth, 0.001f.rf, 0f.rf, baseAlpha)
      this.color = strokeColor.copy(alpha = effectiveAlpha)
      this.style = PaintingStyle.Stroke
      this.strokeWidth = this@RemoteStroke.strokeWidth
      this.strokeCap =
        when (lineCap) {
          LineCap.Butt -> StrokeCap.Butt
          LineCap.Round -> StrokeCap.Round
          LineCap.Square -> StrokeCap.Square
        }
      this.strokeJoin =
        when (lineJoin) {
          LineJoin.Miter -> StrokeJoin.Miter
          LineJoin.Round -> StrokeJoin.Round
          LineJoin.Bevel -> StrokeJoin.Bevel
        }
      if (this@RemoteStroke.dashPattern != null) {
        this.pathEffect = this@RemoteStroke.dashPattern
      }
    }
  }
}

@SuppressLint("RestrictedApi")
internal class RemoteGradientFill(
  val gradient: RemoteGradientValue,
  val startPoint: Point,
  val endPoint: Point,
  val gradientType: GradientType,
  val opacity: RemoteFloat,
  val fillRule: FillRule = FillRule.NonZero,
) : RemoteStyle {
  override fun getPaint(inheritedOpacity: RemoteFloat): RemotePaint {
    return RemotePaint {
      this.style = PaintingStyle.Fill
      this.shader =
        createGradientShader(
          gradient = gradient,
          startPoint = startPoint,
          endPoint = endPoint,
          gradientType = gradientType,
          opacity = opacity,
          inheritedOpacity = inheritedOpacity,
        )
    }
  }
}

@SuppressLint("RestrictedApi")
internal class RemoteGradientStroke(
  val gradient: RemoteGradientValue,
  val startPoint: Point,
  val endPoint: Point,
  val gradientType: GradientType,
  val opacity: RemoteFloat,
  val strokeWidth: RemoteFloat,
  val lineCap: LineCap = LineCap.Round,
  val lineJoin: LineJoin = LineJoin.Round,
  val miterLimit: RemoteFloat? = null,
  val dashPattern: PathEffect? = null,
) : RemoteStyle {
  override fun getPaint(inheritedOpacity: RemoteFloat): RemotePaint {
    return RemotePaint {
      val effectiveOpacity =
        selectIfLt(this@RemoteGradientStroke.strokeWidth, 0.001f.rf, 0f.rf, 1f.rf) * opacity
      this.style = PaintingStyle.Stroke
      this.strokeWidth = this@RemoteGradientStroke.strokeWidth
      this.strokeCap =
        when (lineCap) {
          LineCap.Butt -> StrokeCap.Butt
          LineCap.Round -> StrokeCap.Round
          LineCap.Square -> StrokeCap.Square
        }
      this.strokeJoin =
        when (lineJoin) {
          LineJoin.Miter -> StrokeJoin.Miter
          LineJoin.Round -> StrokeJoin.Round
          LineJoin.Bevel -> StrokeJoin.Bevel
        }
      if (this@RemoteGradientStroke.dashPattern != null) {
        this.pathEffect = this@RemoteGradientStroke.dashPattern
      }
      this.shader =
        createGradientShader(
          gradient = gradient,
          startPoint = startPoint,
          endPoint = endPoint,
          gradientType = gradientType,
          opacity = effectiveOpacity,
          inheritedOpacity = inheritedOpacity,
        )
    }
  }
}

@SuppressLint("RestrictedApi")
internal fun createDashPathEffect(
  dashes: List<StrokeDash>?,
  animationSettings: LottieSettings,
): PathEffect? {
  if (dashes.isNullOrEmpty()) return null

  val intervalsList = mutableListOf<Float>()
  var phase = 0f

  for (dash in dashes) {
    val property = dash.value ?: continue
    val value = resolveScalarFloat(property, animationSettings)
    val type = dash.dashType?.lowercase() ?: dash.name?.lowercase()
    if (type == "o" || type == "offset" || type?.startsWith("o") == true) {
      phase = value
    } else {
      intervalsList.add(value)
    }
  }

  if (intervalsList.isEmpty()) return null

  val finalIntervals =
    if (intervalsList.size % 2 != 0) {
      (intervalsList + intervalsList).toFloatArray()
    } else {
      intervalsList.toFloatArray()
    }

  if (finalIntervals.all { it == 0f }) return null

  return PathEffect.dashPathEffect(finalIntervals, phase)
}

@SuppressLint("RestrictedApi")
private fun resolveScalarFloat(
  property: BaseScalarProperty,
  animationSettings: LottieSettings,
): Float {
  val rf = animateScalar(property, animationSettings)
  val constVal = rf.constantValueOrNull
  if (constVal != null) return constVal
  return when (property) {
    is StaticScalarProperty -> property.value
    is AnimatedScalarProperty -> property.keyframes.firstOrNull()?.value ?: 0f
  }
}

@SuppressLint("RestrictedApi")
private fun createGradientShader(
  gradient: RemoteGradientValue,
  startPoint: Point,
  endPoint: Point,
  gradientType: GradientType,
  opacity: RemoteFloat,
  inheritedOpacity: RemoteFloat,
): RemoteShader {
  val (colors, positions) = extractGradientColorsAndPositions(gradient, opacity, inheritedOpacity)
  return when (gradientType) {
    GradientType.Linear -> {
      RemoteLinearShader(
        x0 = startPoint.x,
        y0 = startPoint.y,
        x1 = endPoint.x,
        y1 = endPoint.y,
        colors = colors,
        positions = positions,
        tileMode = TileMode.Clamp,
      )
    }
    GradientType.Radial -> {
      val dx = endPoint.x - startPoint.x
      val dy = endPoint.y - startPoint.y
      val radius = sqrt((dx * dx) + (dy * dy))
      RemoteRadialShader(
        centerX = startPoint.x,
        centerY = startPoint.y,
        radius = radius,
        colors = colors,
        positions = positions,
        tileMode = TileMode.Clamp,
      )
    }
  }
}

@SuppressLint("RestrictedApi")
private fun extractGradientColorsAndPositions(
  gradient: RemoteGradientValue,
  opacity: RemoteFloat,
  inheritedOpacity: RemoteFloat,
): Pair<List<RemoteColor>, List<RemoteFloat>> {
  val effectiveBaseOpacity = (opacity / 100f) * inheritedOpacity
  val values = gradient.values
  val requestedCount = if (gradient.numberOfColors > 0) gradient.numberOfColors else values.size / 4

  if (requestedCount <= 0 || values.size < 4) {
    val transparent = Color.Transparent.rc
    return Pair(listOf(transparent, transparent), listOf(0f.rf, 1f.rf))
  }

  val maxPossibleColors = values.size / 4
  val colorCount = requestedCount.coerceAtMost(maxPossibleColors)
  if (colorCount <= 0) {
    val transparent = Color.Transparent.rc
    return Pair(listOf(transparent, transparent), listOf(0f.rf, 1f.rf))
  }

  val totalColorFloats = colorCount * 4
  val alphaFloats = values.size - totalColorFloats
  val alphaCount = if (alphaFloats >= 2) alphaFloats / 2 else 0

  val colors = ArrayList<RemoteColor>(colorCount)
  val positions = ArrayList<RemoteFloat>(colorCount)

  for (i in 0 until colorCount) {
    val offset = values[i * 4]
    val r = values[i * 4 + 1]
    val g = values[i * 4 + 2]
    val b = values[i * 4 + 3]

    val alpha =
      if (alphaCount > 0) {
        if (alphaCount == colorCount && (totalColorFloats + i * 2 + 1) < values.size) {
          values[totalColorFloats + i * 2 + 1]
        } else {
          sampleAlpha(offset, values, totalColorFloats, alphaCount)
        }
      } else {
        1f.rf
      }

    val finalAlpha = alpha * effectiveBaseOpacity
    colors.add(RemoteColor(alpha = finalAlpha, red = r, green = g, blue = b))
    positions.add(offset)
  }

  return Pair(colors, positions)
}

@SuppressLint("RestrictedApi")
private fun sampleAlpha(
  offset: RemoteFloat,
  values: List<RemoteFloat>,
  totalColorFloats: Int,
  alphaCount: Int,
): RemoteFloat {
  if (alphaCount <= 0 || totalColorFloats + 1 >= values.size) {
    return 1f.rf
  }
  if (alphaCount <= 1) {
    return values[totalColorFloats + 1]
  }
  val constOffset = offset.constantValueOrNull
  if (constOffset != null) {
    val firstPos = values[totalColorFloats].constantValueOrNull
    val lastPosIndex = totalColorFloats + (alphaCount - 1) * 2
    if (lastPosIndex + 1 >= values.size) return values[totalColorFloats + 1]
    val lastPos = values[lastPosIndex].constantValueOrNull
    if (firstPos != null && constOffset <= firstPos) {
      return values[totalColorFloats + 1]
    }
    if (lastPos != null && constOffset >= lastPos) {
      return values[lastPosIndex + 1]
    }
    for (j in 0 until alphaCount - 1) {
      val p0Index = totalColorFloats + j * 2
      val p1Index = totalColorFloats + (j + 1) * 2
      if (p1Index + 1 < values.size) {
        val p0 = values[p0Index].constantValueOrNull
        val p1 = values[p1Index].constantValueOrNull
        if (p0 != null && p1 != null && constOffset >= p0 && constOffset <= p1) {
          val a0 = values[p0Index + 1]
          val a1 = values[p1Index + 1]
          val fraction = if (p1 > p0) (constOffset - p0) / (p1 - p0) else 0f
          return lerp(a0, a1, fraction.rf)
        }
      }
    }
  }
  return values[totalColorFloats + 1]
}

internal class NoopStyle() : RemoteStyle {
  override fun getPaint(inheritedOpacity: RemoteFloat): RemotePaint {
    return RemotePaint()
  }
}
