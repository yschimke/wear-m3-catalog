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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Replaces the `CurvedTextDelegate` that upstream declares at the bottom of `BasicCurvedText.kt`,
 * which the patch on that file removes. Everything above it there — the public `basicCurvedText`,
 * `CurvedTextChild`, and all the angular layout arithmetic — is upstream's, unchanged, and calls
 * into this through exactly the members below.
 *
 * ┌──────────────────────────────────────────────────────────────────────────────────────────┐
 * │ TODO: THE TEXT IS NOT CURVED. It is laid out as one straight run, rotated to the tangent  │
 * │ of the arc at the middle of its sweep and centred there, so its position, orientation and │
 * │ size are right and each glyph sits on the baseline circle only at the centre of the run.  │
 * │ Short labels — a time, a title, a button caption, which is what Wear curves — read         │
 * │ correctly. A long run visibly departs from the arc towards its ends, and at a sweep of     │
 * │ more than roughly a quarter turn it will leave the band it was allotted.                   │
 * └──────────────────────────────────────────────────────────────────────────────────────────┘
 *
 * Why it is not curved: upstream shapes the run to glyphs with `android.text.TextRunShaper`,
 * reads their positions out of a `PositionedGlyphs`, and either draws the run along a `Path` with
 * `Canvas.drawTextOnPath` or warps each glyph's outline around one with a `PathIterator`. Compose
 * Multiplatform exposes none of that: `TextMeasurer` will measure and draw a run, but it will not
 * hand back positioned glyphs, and there is no path-drawing text API.
 *
 * The next step, when this matters, is per-character placement: measure each character, walk them
 * around the arc at their own angles, and rotate each to its own tangent. That is a real
 * improvement rather than a workaround for the Latin text the catalog draws, and it is still wrong
 * for scripts whose glyphs change shape in context — the reason upstream shapes the whole run
 * first. `docs/PIPELINE.md` -> Curved text has the detail.
 *
 * Measurement is faithful either way: the width, height and baseline this reports are a real
 * measurement of the real font, so the surrounding curved layout allots the right sweep.
 */
internal class CurvedTextDelegate {
    private var text: String = ""
    private var clockwise: Boolean = true
    private var fontSizePx: Float = 0f
    private var letterSpacing: TextUnit = TextUnit.Unspecified
    private var density: Float = 0f
    private var lastLineHeightPx: Float = 0f

    var textWidth by mutableFloatStateOf(0f)
    var textHeight by mutableFloatStateOf(0f)
    var baseLinePosition = 0f

    // Both are captured in composition by UpdateFontIfNeeded, because measuring needs a
    // TextMeasurer and a TextMeasurer can only be obtained from a composition — while the
    // measure and draw passes that use them cannot.
    private var measurer: TextMeasurer? = null
    private var fontStyle: TextStyle = TextStyle.Default

    private var layout: TextLayoutResult? = null
    private val backgroundPath = Path()

    @Composable
    fun UpdateFontIfNeeded(
        fontFamily: FontFamily?,
        fontWeight: FontWeight?,
        fontStyle: FontStyle?,
        fontSynthesis: FontSynthesis?,
    ) {
        measurer = rememberTextMeasurer()
        this.fontStyle =
            remember(fontFamily, fontWeight, fontStyle, fontSynthesis) {
                TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    fontSynthesis = fontSynthesis,
                )
            }
        remeasure()
    }

    fun updateIfNeeded(
        text: String,
        clockwise: Boolean,
        fontSizePx: Float,
        letterSpacing: TextUnit,
        density: Float,
        lineHeightPx: Float,
        warpOffset: CurvedTextStyle.WarpOffset,
    ) {
        if (
            text != this.text ||
                clockwise != this.clockwise ||
                fontSizePx != this.fontSizePx ||
                letterSpacing != this.letterSpacing ||
                density != this.density ||
                lineHeightPx != this.lastLineHeightPx
        ) {
            this.text = text
            this.clockwise = clockwise
            this.fontSizePx = fontSizePx
            this.letterSpacing = letterSpacing
            this.density = density
            this.lastLineHeightPx = lineHeightPx
            remeasure()
        }
    }

    /**
     * Upstream returns the radius offset the warping is measured at; with no warping it returns
     * zero, which is the case this port is always in.
     */
    fun getMeasureOffset(): Float = 0f

    private fun remeasure() {
        val measurer = measurer ?: return
        if (fontSizePx <= 0f || density <= 0f) return

        // The delegate works in pixels, because that is what the curved layout works in; the text
        // stack works in sp. Dividing by the density and back is the whole conversion — a Density
        // with fontScale 1 keeps the two exactly reciprocal, so the caller's px survives round trip.
        val densityForText = Density(density = density, fontScale = 1f)
        val style =
            fontStyle.copy(
                fontSize = (fontSizePx / density).sp,
                letterSpacing = letterSpacing,
                lineHeight =
                    if (lastLineHeightPx >= 0f) (lastLineHeightPx / density).sp
                    else TextUnit.Unspecified,
            )

        val result =
            measurer.measure(
                text = text,
                style = style,
                maxLines = 1,
                softWrap = false,
                density = densityForText,
            )
        layout = result
        textWidth = result.size.width.toFloat()
        textHeight = result.size.height.toFloat()
        // Upstream measures the baseline from whichever edge of the band faces outwards: the
        // ascent for clockwise text, whose glyph tops point away from the centre, and the descent
        // for anticlockwise text, whose tops point towards it.
        val ascent = result.firstBaseline
        baseLinePosition = if (clockwise) ascent else textHeight - ascent
    }

    fun DrawScope.doDraw(
        layoutInfo: CurvedLayoutInfo,
        parentSweepRadians: Float,
        overflow: TextOverflow,
        color: Color,
        background: Color,
    ) {
        val measurer = measurer ?: return
        val naturalLayout = layout ?: return

        with(layoutInfo) {
            val sweepRadians = min(sweepRadians, parentSweepRadians)

            if (background.isSpecified && background != Color.Transparent) {
                // TODO: upstream leaves the same note — this belongs in a CurvedModifier rather
                // than in the text. The arc itself ports exactly: Compose's Path.arcTo takes the
                // same oval, start angle and sweep that android.graphics.Path.arcTo does.
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

            // The band the parent allotted, along the baseline circle. Wider than that and the run
            // has to be cut or ellipsized, which is what overflow decides.
            val availableWidth = parentSweepRadians * measureRadius
            val fits =
                // Float arithmetic can make the parentSweepRadians slightly smaller.
                this.sweepRadians <= parentSweepRadians + 0.001f || overflow == TextOverflow.Visible
            val drawnLayout =
                if (fits) naturalLayout
                else
                    measurer.measure(
                        text = text,
                        style = naturalLayout.layoutInput.style,
                        maxLines = 1,
                        softWrap = false,
                        overflow = overflow,
                        constraints = Constraints(maxWidth = availableWidth.roundToInt()),
                        density = Density(density = density, fontScale = 1f),
                    )

            // The middle of the sweep, on the baseline circle: the one point where straight text
            // and curved text agree, so it is where the run is anchored.
            val middleAngle = startAngleRadians + sweepRadians / 2
            val anchor =
                Offset(
                    centerOffset.x + measureRadius * cos(middleAngle),
                    centerOffset.y + measureRadius * sin(middleAngle),
                )

            // Rotate to the tangent at that point. The quarter turn differs by direction because
            // anticlockwise text is upside down relative to clockwise text at the same angle.
            val tangentDegrees = middleAngle.toDegrees() + if (clockwise) 90f else 270f

            rotate(tangentDegrees, anchor) {
                drawText(
                    textLayoutResult = drawnLayout,
                    color = color,
                    topLeft =
                        Offset(
                            anchor.x - drawnLayout.size.width / 2f,
                            anchor.y - drawnLayout.firstBaseline,
                        ),
                )
            }
        }
    }
}
