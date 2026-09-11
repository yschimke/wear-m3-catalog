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
import androidx.compose.remote.creation.RemotePath
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteDrawScope
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.remotePath
import androidx.compose.remote.creation.compose.state.rf
import com.google.android.horologist.remotecompose.lottie.LottieSettings
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.grouping.Transform
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles.FillRule
import com.google.android.horologist.remotecompose.lottie.renderer.properties.RemoteBezierValue
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar

@SuppressLint("RestrictedApi")
internal interface RemoteShape {
  fun draw(drawScope: RemoteDrawScope, canvas: RemoteCanvas, inheritedOpacity: RemoteFloat = 1f.rf)

  fun withFillRule(fillRule: FillRule): RemoteShape = this
}

@SuppressLint("RestrictedApi")
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal fun RemoteCanvas.drawPathWithFillRule(
  path: RemotePath,
  fillRule: FillRule,
  paint: androidx.compose.remote.creation.compose.state.RemotePaint? = null,
) {
  val winding = if (fillRule == FillRule.EvenOdd) 1 else 0
  val op =
    internalCanvas.recordRenderingOp(paint) {
      val pathId = document.addPathData(path, winding)
      document.drawPath(pathId)
    }
  internalCanvas.buffer.addRoots(op, path)
}

@SuppressLint("RestrictedApi")
internal class RemoteCompiledPath(val path: RemotePath, val fillRule: FillRule = FillRule.NonZero) :
  RemoteShape {
  override fun draw(
    drawScope: RemoteDrawScope,
    canvas: RemoteCanvas,
    inheritedOpacity: RemoteFloat,
  ) {
    canvas.drawPathWithFillRule(path, fillRule)
  }

  override fun withFillRule(fillRule: FillRule): RemoteCompiledPath =
    if (this.fillRule == fillRule) this else RemoteCompiledPath(path, fillRule)
}

@SuppressLint("RestrictedApi")
internal class RemoteLottiePath(
  val path: List<RemoteBezierValue>,
  val fillRule: FillRule = FillRule.NonZero,
) : RemoteShape {
  override fun draw(
    drawScope: RemoteDrawScope,
    canvas: RemoteCanvas,
    inheritedOpacity: RemoteFloat,
  ) {
    if (path.isEmpty()) return

    val rcPath = drawScope.remotePath {
      for (subpath in path) {
        val vertices = subpath.vertices
        val inTangents = subpath.inTangents
        val outTangents = subpath.outTangents

        if (vertices.isEmpty()) continue

        val startX = vertices[0].getOrElse(0) { 0f.rf }
        val startY = vertices[0].getOrElse(1) { 0f.rf }
        moveTo(startX, startY)

        val maxIndex = if (subpath.closed) vertices.size else vertices.size - 1
        for (i in 0 until maxIndex) {
          val p0 = vertices[i]
          val lastIndex = if (i == vertices.size - 1 && subpath.closed) 0 else i + 1
          val p4 = vertices[lastIndex]
          val inTangent = inTangents.getOrNull(lastIndex)
          val outTangent = outTangents.getOrNull(i)

          val p0x = p0.getOrElse(0) { 0f.rf }
          val p0y = p0.getOrElse(1) { 0f.rf }
          val p4x = p4.getOrElse(0) { 0f.rf }
          val p4y = p4.getOrElse(1) { 0f.rf }

          val inTangentX = inTangent?.getOrElse(0) { 0f.rf } ?: 0f.rf
          val inTangentY = inTangent?.getOrElse(1) { 0f.rf } ?: 0f.rf
          val outTangentX = outTangent?.getOrElse(0) { 0f.rf } ?: 0f.rf
          val outTangentY = outTangent?.getOrElse(1) { 0f.rf } ?: 0f.rf

          val p1x = p0x + outTangentX
          val p1y = p0y + outTangentY
          val p2x = p4x + inTangentX
          val p2y = p4y + inTangentY

          curveTo(p1x, p1y, p2x, p2y, p4x, p4y)
        }

        if (subpath.closed) {
          close()
        }
      }
    }

    canvas.drawPathWithFillRule(rcPath, fillRule)
  }

  override fun withFillRule(fillRule: FillRule): RemoteLottiePath =
    if (this.fillRule == fillRule) this else RemoteLottiePath(path, fillRule)
}

@SuppressLint("RestrictedApi")
internal class RemoteGroup(
  val childShapes: List<StyledShapes>,
  val animationSettings: LottieSettings,
  val transform: Transform?,
) : RemoteShape {
  override fun draw(
    drawScope: RemoteDrawScope,
    canvas: RemoteCanvas,
    inheritedOpacity: RemoteFloat,
  ) {
    val groupOpacity =
      if (transform != null) {
        val o = animateScalar(transform.opacity, animationSettings)
        inheritedOpacity * (o / 100f)
      } else {
        inheritedOpacity
      }

    for (shapeGroup in childShapes) {
      canvas.save()

      if (transform != null) {
        transform(transform, null, animationSettings, canvas)
      }

      if (shapeGroup.style is NoopStyle) {
        for (shape in shapeGroup.shapes) {
          shape.draw(drawScope, canvas, groupOpacity)
        }
      } else {
        val paint = shapeGroup.style.getPaint(groupOpacity)
        drawScope.usePaint(paint) {
          for (shape in shapeGroup.shapes) {
            shape.draw(drawScope, canvas, groupOpacity)
          }
        }
      }

      canvas.restore()
    }
  }

  override fun withFillRule(fillRule: FillRule): RemoteGroup {
    val newChildShapes = childShapes.map { styledShapes ->
      StyledShapes(
        shapes = styledShapes.shapes.map { it.withFillRule(fillRule) },
        style = styledShapes.style,
      )
    }
    return RemoteGroup(newChildShapes, animationSettings, transform)
  }
}
