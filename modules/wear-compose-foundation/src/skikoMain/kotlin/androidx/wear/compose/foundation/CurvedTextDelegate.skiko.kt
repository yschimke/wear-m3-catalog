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
import ee.schimke.wearcmp.port.skiaTypefaceForFamily
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
 * Upstream's *warping* renderer is here too, in [drawWarped]: it additionally bends each glyph's
 * outline so its verticals converge on the centre of the circle, which needs the outlines
 * themselves. Skia hands those over — `Font.getPath(glyph)`, whose verbs are the same set Android's
 * `PathIterator` produces — so the mapping in [CircleWarper] is upstream's, point for point.
 */
internal actual class CurvedTextDelegate {
    private var text: String = ""
    private var clockwise: Boolean = true
    private var fontSizePx: Float = 0f
    private var letterSpacingPx: Float = 0f
    private var lastLineHeightPx: Float = -1f
    private var warpOffset: CurvedTextStyle.WarpOffset = CurvedTextStyle.WarpOffset.Unspecified

    /**
     * How far from the baseline the warping line sits — the line of the text that keeps its size
     * when the run is bent. Zero means no warping.
     */
    private var warpRadiusOffset: Float = 0f

    actual var textWidth: Float by mutableFloatStateOf(0f)
    actual var textHeight: Float by mutableFloatStateOf(0f)
    actual var baseLinePosition: Float = 0f

    private var typeface: Typeface? by mutableStateOf(null)
    private var font: Font? = null

    /** Per-glyph advances at the current size, including letter spacing. */
    private var glyphs: ShortArray = ShortArray(0)
    private var advances: FloatArray = FloatArray(0)

    private val backgroundPath = Path()
    private val warpedPath = Path()
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
                // A family this port built — the Wear type scale's `roboto-flex`, or anything else
                // registered with WearFonts — hands its typeface straight back. That is the case
                // that matters, and it is checked first because the general one below cannot cover
                // it: Compose wraps a loaded typeface in a platform type it will not unwrap.
                skiaTypefaceForFamily(fontFamily)
                    ?: runCatching {
                            resolver
                                .resolve(
                                    fontFamily,
                                    fontWeight ?: FontWeight.Normal,
                                    fontStyle ?: FontStyle.Normal,
                                    fontSynthesis ?: FontSynthesis.All,
                                )
                                .value as? Typeface
                        }
                        .getOrNull()
                    ?: FontMgr.default.legacyMakeTypeface("", skiaStyle(fontWeight))
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
                lineHeightPx != this.lastLineHeightPx ||
                warpOffset != this.warpOffset
        ) {
            this.text = text
            this.clockwise = clockwise
            this.fontSizePx = fontSizePx
            this.letterSpacingPx = spacingPx
            this.lastLineHeightPx = lineHeightPx
            this.warpOffset = warpOffset
            remeasure()
        }
    }

    // Upstream returns the same thing: the layout has to know where the warping line is, because
    // that is the radius the run was measured against.
    actual fun getMeasureOffset(): Float = warpRadiusOffset

    // The flag is upstream's own experimental switch; reading it here is exactly what upstream's
    // renderer does, and the opt-in is not this port's decision to make.
    @OptIn(ExperimentalWearFoundationApi::class)
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

        // Upstream's default when warping is enabled and the caller said nothing: half the optical
        // height, which puts the warping line through the middle of a lowercase letter rather than
        // at the baseline, where the distortion would be lopsided.
        val effective =
            if (!WearComposeFoundationFlags.isWarpingCurvedTextEnabled) {
                CurvedTextStyle.WarpOffset.None
            } else {
                warpOffset.takeOrElse { CurvedTextStyle.WarpOffset.HalfOpticalHeight }
            }
        warpRadiusOffset =
            if (effective == CurvedTextStyle.WarpOffset.None) 0f
            else effective.determineWarpRadiusOffset(metrics.ascent, metrics.descent)
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
            //
            // Compared in PIXELS, with a pixel of tolerance, rather than in radians: a run that
            // exactly fills the sweep it was given has a width and an allowance that agree only to
            // the last bits of a float, and a strict comparison there would cut its final glyph.
            //
            // A run that overflows by more than that really is too long — `curvedText` caps its
            // sweep at `CurvedTextDefaults.MaxSweepAngle` (70°) by default, and anything past it is
            // clipped or ellipsized here exactly as it is on Android.
            val available = parentSweepRadians * measureRadius
            val drawn =
                if (textWidth <= available + 1f || overflow == TextOverflow.Visible)
                    DrawnRun(glyphs, advances)
                else truncate(font, available, overflow == TextOverflow.Ellipsis)
            if (drawn.glyphs.isEmpty()) return

            // Clockwise text advances with the angle and sits with its ascent outwards;
            // anticlockwise text advances against it and is upside down at the same point, which
            // is why it starts at the far end of the sweep and why the tangent turns the other way.
            val direction = if (clockwise) 1f else -1f
            val quarterTurn = direction * (kotlin.math.PI.toFloat() / 2f)
            var angle = if (clockwise) startAngleRadians else startAngleRadians + sweepRadians

            if (warpRadiusOffset != 0f) {
                drawWarped(this@with, font, drawn, angle, color)
                return
            }

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

    /**
     * The run with each glyph's OUTLINE bent around the arc, rather than stamped on it rotated.
     *
     * This is upstream's `WarpedCurvedTextRenderer`. The difference from the `RSXform` path is what
     * happens between glyphs: a rigid stamp keeps each glyph rectangular and lets the gap between
     * two of them open on the outside of the curve, which is visible on a cursive face and at large
     * sizes on a tight radius. Warping maps the whole run's rectangle onto the annulus, so adjacent
     * glyphs share the line between them.
     *
     * Skia gives the outlines that Compose will not: `Font.getPath(glyph)` per glyph, and a path
     * whose verbs are the same set Android's `PathIterator` produces.
     */
    private fun DrawScope.drawWarped(
        layout: CurvedLayoutInfo,
        font: Font,
        drawn: DrawnRun,
        startAngle: Float,
        color: Color,
    ) = with(layout) {
        // Upstream measures against the warping line rather than the baseline, so the radius the
        // run was laid out for is the one the warper has to use.
        val warpRadius =
            if (clockwise) measureRadius + warpRadiusOffset else measureRadius - warpRadiusOffset
        val warper =
            CircleWarper(centerOffset.x, centerOffset.y, warpRadius, startAngle, clockwise)

        warpedPath.reset()
        var offsetX = 0f
        for (index in drawn.glyphs.indices) {
            val outline = font.getPath(drawn.glyphs[index])
            if (outline != null) {
                // The glyph sits on the baseline; the warper works from the warping line, so the
                // outline is shifted by the distance between them before being bent.
                appendWarpedGlyph(warpedPath, outline, offsetX, warpRadiusOffset, warper)
            }
            offsetX += drawn.advances[index]
        }
        drawPath(warpedPath, color)
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
