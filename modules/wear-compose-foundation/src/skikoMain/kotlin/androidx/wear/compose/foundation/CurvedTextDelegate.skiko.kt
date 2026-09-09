/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.wear.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import kotlin.math.min
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RSXform
import org.jetbrains.skia.TextBlobBuilder
import org.jetbrains.skia.Typeface

/**
 * Curved text, drawn glyph by glyph around the arc.
 *
 * Upstream shapes the run with `android.text.TextRunShaper` and hands the glyphs to
 * `Canvas.drawTextOnPath`, which internally builds one rotate-and-translate transform per glyph.
 * Skia exposes that mechanism directly as [RSXform] and `appendRunRSXform`, so this is the same
 * technique rather than an approximation of it: each glyph is placed at its own point on the
 * baseline circle and rotated to the tangent there, and the whole run goes to the canvas as a
 * single text blob.
 *
 * What is NOT reproduced is upstream's *warping* renderer, which additionally bends each glyph's
 * outline so its verticals converge on the centre of the circle. That needs the glyph outlines,
 * which Skia will give but Compose's font stack will not hand over cheaply; at watch radii the
 * difference is a fraction of a pixel on the diagonals of tall glyphs. See
 * `WarpedCurvedTextRenderer` in `docs/ANDROID_SURFACE.md`.
 */
internal actual class CurvedTextDelegate {
    private var text: String = ""
    private var clockwise: Boolean = true
    private var fontSizePx: Float = 0f
    private var letterSpacingPx: Float = 0f
    private var lastLineHeightPx: Float = -1f

    actual var textWidth: Float by mutableFloatStateOf(0f)
    actual var textHeight: Float by mutableFloatStateOf(0f)
    actual var baseLinePosition: Float = 0f

    private var typeface: Typeface? by mutableStateOf(null)
    private var font: Font? = null

    /** Per-glyph advances at the current size, including letter spacing. */
    private var glyphs: ShortArray = ShortArray(0)
    private var advances: FloatArray = FloatArray(0)

    private val backgroundPath = Path()
    private val paint = Paint().apply { isAntiAlias = true }

    @Composable
    actual fun UpdateFontIfNeeded(
        fontFamily: FontFamily?,
        fontWeight: FontWeight?,
        fontStyle: FontStyle?,
        fontSynthesis: FontSynthesis?,
    ) {
        val resolver = LocalFontFamilyResolver.current
        // Compose's own resolver, so a caller's `fontFamily` — including one loaded from resources
        // — is honoured exactly as it is for straight text. On every Skia target the resolved
        // value IS the Skia typeface; the fallback covers a target where it is wrapped in
        // something else rather than pretending the text cannot be drawn.
        val resolved =
            remember(fontFamily, fontWeight, fontStyle, fontSynthesis, resolver) {
                runCatching {
                        resolver
                            .resolve(
                                fontFamily,
                                fontWeight ?: FontWeight.Normal,
                                fontStyle ?: FontStyle.Normal,
                                fontSynthesis ?: FontSynthesis.All,
                            )
                            .value as? Typeface
                    }
                    .getOrNull() ?: FontMgr.default.legacyMakeTypeface("", skiaStyle(fontWeight))
            }
        if (resolved !== typeface) {
            typeface = resolved
            remeasure()
        }
    }

    actual fun updateIfNeeded(
        text: String,
        clockwise: Boolean,
        fontSizePx: Float,
        letterSpacing: TextUnit,
        density: Float,
        lineHeightPx: Float,
        warpOffset: CurvedTextStyle.WarpOffset,
    ) {
        // Upstream converts letter spacing against the paint's em width; the em is the font size,
        // so an Sp value is a length and an Em value is a multiple of it.
        val spacingPx =
            when (letterSpacing.type) {
                TextUnitType.Em -> letterSpacing.value * fontSizePx
                TextUnitType.Sp -> letterSpacing.value * density
                else -> 0f
            }
        if (
            text != this.text ||
                clockwise != this.clockwise ||
                fontSizePx != this.fontSizePx ||
                spacingPx != this.letterSpacingPx ||
                lineHeightPx != this.lastLineHeightPx
        ) {
            this.text = text
            this.clockwise = clockwise
            this.fontSizePx = fontSizePx
            this.letterSpacingPx = spacingPx
            this.lastLineHeightPx = lineHeightPx
            remeasure()
        }
    }

    actual fun getMeasureOffset(): Float = 0f

    private fun remeasure() {
        val typeface = typeface
        if (typeface == null || fontSizePx <= 0f) return

        val font = Font(typeface, fontSizePx)
        this.font = font
        glyphs = font.getStringGlyphs(text)
        // Letter spacing is applied per glyph, as the paint's own spacing does: it widens each
        // advance, which is what makes the run's total width match what the layout was told.
        advances = font.getWidths(glyphs).map { it + letterSpacingPx }.toFloatArray()
        textWidth = advances.sum()

        // Upstream's arithmetic exactly, over the same metrics: ascent is negative above the
        // baseline, and a specified line height is distributed evenly above and below.
        val metrics = font.metrics
        val height = metrics.descent - metrics.ascent
        val diff = if (lastLineHeightPx >= 0f) lastLineHeightPx - height else 0f
        val actualAscent = -metrics.ascent + diff / 2
        val actualDescent = metrics.descent + diff / 2
        textHeight = actualAscent + actualDescent
        baseLinePosition = if (clockwise) actualAscent else actualDescent
    }

    actual fun DrawScope.doDraw(
        layoutInfo: CurvedLayoutInfo,
        parentSweepRadians: Float,
        overflow: TextOverflow,
        color: Color,
        background: Color,
    ) {
        val font = font ?: return
        if (glyphs.isEmpty()) return

        with(layoutInfo) {
            val sweepRadians = min(sweepRadians, parentSweepRadians)

            if (background.isSpecified && background != Color.Transparent) {
                // TODO: upstream leaves the same note — this belongs in a CurvedModifier rather
                // than in the text. The arc itself is plain Compose geometry either way.
                val sweepDegrees = sweepRadians.toDegrees().coerceAtMost(360f)
                val startDegrees = startAngleRadians.toDegrees()
                backgroundPath.reset()
                backgroundPath.arcTo(
                    Rect(centerOffset, outerRadius),
                    startDegrees,
                    sweepDegrees,
                    forceMoveTo = false,
                )
                backgroundPath.arcTo(
                    Rect(centerOffset, innerRadius),
                    startDegrees + sweepDegrees,
                    -sweepDegrees,
                    forceMoveTo = false,
                )
                backgroundPath.close()
                drawPath(backgroundPath, background)
            }

            // How much of the run fits in the sweep the parent allowed. `Visible` overflows on
            // purpose; the others cut, and Ellipsis buys room for the ellipsis first.
            val available = parentSweepRadians * measureRadius
            val fits = this.sweepRadians <= parentSweepRadians + 0.001f
            val drawn =
                if (fits || overflow == TextOverflow.Visible) DrawnRun(glyphs, advances)
                else truncate(font, available, overflow == TextOverflow.Ellipsis)
            if (drawn.glyphs.isEmpty()) return

            // Clockwise text advances with the angle and sits with its ascent outwards;
            // anticlockwise text advances against it and is upside down at the same point, which
            // is why it starts at the far end of the sweep and why the tangent turns the other way.
            val direction = if (clockwise) 1f else -1f
            val quarterTurn = direction * (kotlin.math.PI.toFloat() / 2f)
            var angle = if (clockwise) startAngleRadians else startAngleRadians + sweepRadians

            val xforms =
                Array(drawn.glyphs.size) { index ->
                    val point = pointAt(centerOffset.x, centerOffset.y, measureRadius, angle)
                    val xform =
                        RSXform.makeFromRadians(
                            scale = 1f,
                            radians = angle + quarterTurn,
                            tx = point.first,
                            ty = point.second,
                            ax = 0f,
                            ay = 0f,
                        )
                    // Advance along the baseline circle: an arc length divided by its radius is
                    // the angle it subtends, which is the whole of the straight-to-curved mapping.
                    angle += direction * drawn.advances[index] / measureRadius
                    xform
                }

            val blob = TextBlobBuilder().appendRunRSXform(font, drawn.glyphs, xforms).build()
            if (blob != null) {
                paint.color = color.toArgb()
                drawIntoCanvas { canvas -> canvas.nativeCanvas.drawTextBlob(blob, 0f, 0f, paint) }
            }
        }
    }

    /** The glyphs that fit in [available] pixels of arc, with an ellipsis if one was asked for. */
    private fun truncate(font: Font, available: Float, ellipsis: Boolean): DrawnRun {
        val ellipsisGlyphs = if (ellipsis) font.getStringGlyphs("…") else ShortArray(0)
        val ellipsisAdvances =
            if (ellipsisGlyphs.isEmpty()) FloatArray(0) else font.getWidths(ellipsisGlyphs)
        val budget = available - ellipsisAdvances.sum()

        var used = 0f
        var count = 0
        while (count < glyphs.size && used + advances[count] <= budget) {
            used += advances[count]
            count++
        }
        return DrawnRun(
            glyphs.copyOf(count) + ellipsisGlyphs,
            advances.copyOf(count) + ellipsisAdvances,
        )
    }

    private class DrawnRun(val glyphs: ShortArray, val advances: FloatArray)

    private fun pointAt(cx: Float, cy: Float, radius: Float, angle: Float): Pair<Float, Float> =
        cx + radius * kotlin.math.cos(angle) to cy + radius * kotlin.math.sin(angle)

    private fun skiaStyle(weight: FontWeight?): org.jetbrains.skia.FontStyle =
        if ((weight?.weight ?: 400) >= 600) org.jetbrains.skia.FontStyle.BOLD
        else org.jetbrains.skia.FontStyle.NORMAL
}
