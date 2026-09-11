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

package com.google.android.horologist.remotecompose.lottie.renderer.layers

import android.annotation.SuppressLint
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemotePaint
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import com.google.android.horologist.remotecompose.lottie.LocalAnimationSettings
import com.google.android.horologist.remotecompose.lottie.format.FontChar
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.grouping.Transform
import com.google.android.horologist.remotecompose.lottie.format.layer.TextDocument
import com.google.android.horologist.remotecompose.lottie.format.layer.TextDocumentProperty
import com.google.android.horologist.remotecompose.lottie.format.layer.TextJustify
import com.google.android.horologist.remotecompose.lottie.format.layer.TextLayer
import com.google.android.horologist.remotecompose.lottie.format.mask.MaskMode
import com.google.android.horologist.remotecompose.lottie.renderer.NoopStyle
import com.google.android.horologist.remotecompose.lottie.renderer.applyLayerMasks
import com.google.android.horologist.remotecompose.lottie.renderer.applyMatteClip
import com.google.android.horologist.remotecompose.lottie.renderer.gatherShapes
import com.google.android.horologist.remotecompose.lottie.renderer.inverseTransform
import com.google.android.horologist.remotecompose.lottie.renderer.properties.animateScalar
import com.google.android.horologist.remotecompose.lottie.renderer.transform

/** Evaluates the active [TextDocument] at the given animation frame. */
internal fun evaluateTextDocument(
  property: TextDocumentProperty,
  currentFrame: Float,
): TextDocument? {
  val keyframes = property.keyframes
  if (keyframes.isEmpty()) return null
  if (keyframes.size == 1) return keyframes[0].start

  var activeDoc: TextDocument? = keyframes[0].start
  for (kf in keyframes) {
    val kfTime = kf.time ?: 0f
    if (currentFrame >= kfTime) {
      if (kf.start != null) {
        activeDoc = kf.start
      }
    } else {
      break
    }
  }
  return activeDoc
}

/** Parses RGB/RGBA float lists `[r, g, b]` or `[r, g, b, a]` into a [Color]. */
internal fun parseColorFromList(list: List<Float>): Color {
  if (list.size < 3) return Color.Black
  val r = list[0].coerceIn(0f, 1f)
  val g = list[1].coerceIn(0f, 1f)
  val b = list[2].coerceIn(0f, 1f)
  val a = if (list.size >= 4) list[3].coerceIn(0f, 1f) else 1f
  return Color(r, g, b, a)
}

/** A Layer rendering vector typographic text or glyph shapes. */
@SuppressLint("RestrictedApi")
@Composable
@RemoteComposable
internal fun TextLayer(
  layer: TextLayer,
  transformStack: List<Transform> = emptyList(),
  matteContext: MatteContext? = null,
  layerVisibility: RemoteFloat = 1f.rf,
) {
  if (layer.hidden == true) {
    return
  }

  val textData = layer.text ?: return
  val docProperty = textData.document ?: return
  val animationSettings = LocalAnimationSettings.current

  val constFrame = animationSettings.currentFrame.constantValueOrNull ?: 0f
  val currentDoc = evaluateTextDocument(docProperty, constFrame) ?: return
  val text = currentDoc.text
  if (text.isEmpty()) {
    return
  }

  val updatedTransformStack =
    if (layer.transform != null) transformStack + layer.transform else transformStack

  val layerOpacity =
    (updatedTransformStack.lastOrNull()?.opacity?.let {
      animateScalar(it, animationSettings) / 100f
    } ?: 1f.rf) * layerVisibility

  val fontSize = currentDoc.fontSize
  val tracking = currentDoc.tracking
  val justification = currentDoc.justification

  val fillColor = currentDoc.fillColor?.let { parseColorFromList(it) } ?: Color.Black
  val strokeColor = currentDoc.strokeColor?.let { parseColorFromList(it) }
  val strokeWidth = currentDoc.strokeWidth ?: 0f

  val fillPaint = RemotePaint {
    this.color = fillColor.rc.copy(alpha = fillColor.rc.alpha * layerOpacity)
  }
  val strokePaint =
    if (strokeColor != null && strokeWidth > 0f) {
      RemotePaint {
        this.color = strokeColor.rc.copy(alpha = strokeColor.rc.alpha * layerOpacity)
        this.style = PaintingStyle.Stroke
        this.strokeWidth = strokeWidth.rf
      }
    } else {
      null
    }

  val charsMap = animationSettings.chars
  val hasMasks = layer.masksProperties.any { it.mode != MaskMode.None && it.path != null }
  val needsSave = matteContext != null || hasMasks

  RemoteCanvas(modifier = RemoteModifier.fillMaxSize()) {
    if (needsSave) {
      remoteCanvas.save()
    }

    if (matteContext != null) {
      applyMatteClip(matteContext, animationSettings, remoteCanvas)
    }

    if (hasMasks) {
      for (transform in updatedTransformStack) {
        transform(transform, null, animationSettings, remoteCanvas)
      }
      applyLayerMasks(layer.masksProperties, animationSettings, remoteCanvas)
      for (transform in updatedTransformStack.reversed()) {
        inverseTransform(transform, animationSettings, remoteCanvas)
      }
    }

    for (transform in updatedTransformStack) {
      remoteCanvas.save()
      transform(transform, null, animationSettings, remoteCanvas)
    }

    // Render glyph vector shapes if chars are available in animation
    if (charsMap.isNotEmpty()) {
      val lines = text.split(Regex("\r\n|\r|\n"))
      val effLineHeight = currentDoc.lineHeight ?: (fontSize * 1.2f)
      val baselineShift = currentDoc.baselineShift ?: 0f
      val strokeOverFill = currentDoc.strokeOverFill ?: true

      for ((lineIndex, lineText) in lines.withIndex()) {
        var totalLineWidth = 0f
        val glyphs = mutableListOf<Pair<FontChar, Float>>()
        for (ch in lineText) {
          val fontChar =
            charsMap.firstOrNull {
              it.character == ch.toString() &&
                (it.family == currentDoc.fontName || it.family.isEmpty())
            } ?: charsMap.firstOrNull { it.character == ch.toString() }
          if (fontChar != null) {
            val fontScale = if (fontChar.size > 0f) fontSize / fontChar.size else fontSize / 100f
            val advance = (fontChar.width * fontScale) + (tracking * (fontSize / 1000f))
            glyphs.add(fontChar to advance)
            totalLineWidth += advance
          }
        }

        var currentX =
          when (justification) {
            TextJustify.Right,
            TextJustify.JustifyWithLastLineRight -> -totalLineWidth
            TextJustify.Center,
            TextJustify.JustifyWithLastLineCenter -> -totalLineWidth / 2f
            else -> 0f
          }
        val currentY = (lineIndex * effLineHeight) - baselineShift

        for ((fontChar, advance) in glyphs) {
          val fontScale = if (fontChar.size > 0f) fontSize / fontChar.size else fontSize / 100f
          val shapes = fontChar.shapeData?.shapes.orEmpty()
          if (shapes.isNotEmpty()) {
            val glyphGroups = gatherShapes(shapes, animationSettings, inheritedStyle = NoopStyle())
            remoteCanvas.save()
            remoteCanvas.translate(currentX.rf, currentY.rf)
            remoteCanvas.scale(fontScale.rf, fontScale.rf)

            for (group in glyphGroups) {
              if (group.style is NoopStyle) {
                if (strokeOverFill) {
                  usePaint(fillPaint) {
                    for (shape in group.shapes) {
                      shape.draw(this, remoteCanvas, layerOpacity)
                    }
                  }
                  if (strokePaint != null) {
                    usePaint(strokePaint) {
                      for (shape in group.shapes) {
                        shape.draw(this, remoteCanvas, layerOpacity)
                      }
                    }
                  }
                } else {
                  if (strokePaint != null) {
                    usePaint(strokePaint) {
                      for (shape in group.shapes) {
                        shape.draw(this, remoteCanvas, layerOpacity)
                      }
                    }
                  }
                  usePaint(fillPaint) {
                    for (shape in group.shapes) {
                      shape.draw(this, remoteCanvas, layerOpacity)
                    }
                  }
                }
              } else {
                val groupPaint = group.style.getPaint(layerOpacity)
                usePaint(groupPaint) {
                  for (shape in group.shapes) {
                    shape.draw(this, remoteCanvas, layerOpacity)
                  }
                }
              }
            }
            remoteCanvas.restore()
          }
          currentX += advance
        }
      }
    }

    for (transform in updatedTransformStack) {
      remoteCanvas.restore()
    }

    if (needsSave) {
      remoteCanvas.restore()
    }
  }
}
