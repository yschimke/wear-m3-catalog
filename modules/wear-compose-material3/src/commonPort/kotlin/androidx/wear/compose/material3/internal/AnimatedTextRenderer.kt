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

package androidx.wear.compose.material3.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight

/**
 * The platform half of `AnimatedText`.
 *
 * Upstream animates text by shaping the same string twice — once with the font at the start of the
 * animation, once with the font at the end — and then, every frame, drawing each glyph at the
 * position interpolated between the two, in a font whose variation axes are interpolated to match.
 * That needs three things Compose Multiplatform does not expose: a font built from named variation
 * axes, a shaping pass that hands back per-glyph positions, and a draw call that takes glyph ids.
 * All three are ordinary Skia, so the one implementation lives in `skikoMain`, the way
 * `CurvedTextDelegate` does in `:wear-compose-foundation`.
 *
 * The split is deliberate: everything measurable in common code — sizing, alignment, the axis
 * interpolation, the cache — stays in `AnimatedText.kt`, and this seam is only what genuinely
 * cannot be written twice.
 */
internal expect class AnimatedTextFont

/** One shaping pass: the glyphs of a string and where they sit, in a platform representation. */
internal expect class ShapedRun {
    /** Total advance of the run, in pixels — its width. */
    val advance: Float
}

/** A single variation axis at a single value, e.g. `wght` at 612.5. */
internal data class AnimatedTextAxis(val axisName: String, val value: Float)

/** Vertical metrics of a font at its current size, in pixels, sign-compatible with Android's. */
internal data class AnimatedTextMetrics(val top: Float, val ascent: Float, val descent: Float)

/**
 * The typeface [textStyle] names, at [sizePx], with no axes applied yet.
 *
 * Resolution follows `CurvedTextDelegate`: a family this port built hands its typeface straight
 * back, anything else goes through Compose's own resolver, and a host that has neither falls back
 * to the platform's default face at the requested weight.
 */
internal expect fun resolveAnimatedTextFont(
    fontFamily: FontFamily?,
    fontWeight: FontWeight?,
    fontStyle: FontStyle?,
    fontSynthesis: FontSynthesis?,
    resolver: FontFamily.Resolver,
    sizePx: Float,
): AnimatedTextFont

/** The same typeface with [axes] applied. An axis the font does not declare is ignored. */
internal expect fun AnimatedTextFont.withAxes(axes: List<AnimatedTextAxis>): AnimatedTextFont

/** The same font at [sizePx]. Returns the receiver when it is already that size. */
internal expect fun AnimatedTextFont.atSize(sizePx: Float): AnimatedTextFont

/** Vertical metrics at this font's size. */
internal expect fun AnimatedTextFont.metrics(): AnimatedTextMetrics

/** Shapes [text] with this font. [rtl] sets the base direction of the run. */
internal expect fun AnimatedTextFont.shape(text: String, rtl: Boolean): ShapedRun

/**
 * Draws the run with every glyph at the position [fraction] of the way from [start] to [end], in
 * [font].
 *
 * The glyph ids come from [start]: the two runs are the same string shaped in the same typeface, so
 * they differ in where the glyphs sit, not in which glyphs they are. A run that disagrees anyway —
 * a variation axis that switches in an alternate glyph, say — draws the glyphs the two have in
 * common, which is what upstream does with its own `minOf(startGlyphs, endGlyphs)`.
 */
internal expect fun DrawScope.drawAnimatedText(
    font: AnimatedTextFont,
    start: ShapedRun,
    end: ShapedRun,
    fraction: Float,
    color: Color,
)
