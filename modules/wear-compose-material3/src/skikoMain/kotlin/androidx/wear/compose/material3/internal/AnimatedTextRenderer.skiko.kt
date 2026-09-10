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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.lerp
import ee.schimke.wearcmp.port.resolveSkiaTypeface
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextBlobBuilder
import org.jetbrains.skia.FontVariation as SkiaFontVariation
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.shaper.ShapingOptions

/**
 * A Skia [Font]: a typeface, its variation axes, and a size. Android splits the same three across a
 * `Font` and a `TextPaint`; Skia carries them together, which is why [atSize] exists — the size
 * moves independently of the axes, and only the axes are worth caching.
 */
internal actual class AnimatedTextFont(internal val font: Font)

/**
 * The glyphs of one shaped line and their positions, as x/y pairs — Skia's own layout of the run,
 * complete with the kerning and ligatures a naive character-by-character mapping would miss.
 */
internal actual class ShapedRun(
    internal val glyphs: ShortArray,
    internal val positions: FloatArray,
    actual val advance: Float,
)

internal actual fun resolveAnimatedTextFont(
    fontFamily: FontFamily?,
    fontWeight: FontWeight?,
    fontStyle: FontStyle?,
    fontSynthesis: FontSynthesis?,
    resolver: FontFamily.Resolver,
    sizePx: Float,
): AnimatedTextFont =
    AnimatedTextFont(
        Font(
            resolveSkiaTypeface(fontFamily, fontWeight, fontStyle, fontSynthesis, resolver),
            sizePx,
        )
    )

internal actual fun AnimatedTextFont.withAxes(axes: List<AnimatedTextAxis>): AnimatedTextFont {
    val typeface = font.typeface ?: return this
    if (axes.isEmpty()) return this
    val varied =
        typeface.makeClone(axes.map { SkiaFontVariation(it.axisName, it.value) }.toTypedArray())
    return AnimatedTextFont(Font(varied, font.size))
}

internal actual fun AnimatedTextFont.atSize(sizePx: Float): AnimatedTextFont =
    if (font.size == sizePx) this else AnimatedTextFont(font.makeWithSize(sizePx))

internal actual fun AnimatedTextFont.metrics(): AnimatedTextMetrics =
    font.metrics.let { AnimatedTextMetrics(top = it.top, ascent = it.ascent, descent = it.descent) }

internal actual fun AnimatedTextFont.shape(text: String, rtl: Boolean): ShapedRun {
    // `shapeLine` is Skia's own single-line shaper: HarfBuzz over the whole string, with the base
    // direction the caller asks for. Upstream reaches the same place through `TextShaper`, which is
    // HarfBuzz too — the shaping is the platform's in both cases, not this port's arithmetic.
    val line = shaper.shapeLine(text, font, ShapingOptions.DEFAULT.withLeftToRight(!rtl))
    return ShapedRun(line.glyphs, line.positions, line.width)
}

internal actual fun DrawScope.drawAnimatedText(
    font: AnimatedTextFont,
    start: ShapedRun,
    end: ShapedRun,
    fraction: Float,
    color: Color,
) {
    val count = minOf(start.glyphs.size, end.glyphs.size)
    if (count == 0) return
    val positions =
        Array(count) { index ->
            Point(
                lerp(start.positions[index * 2], end.positions[index * 2], fraction),
                lerp(start.positions[index * 2 + 1], end.positions[index * 2 + 1], fraction),
            )
        }
    val blob =
        TextBlobBuilder()
            .appendRunPos(font.font, start.glyphs.copyOf(count), positions)
            .build() ?: return
    paint.color = color.toArgb()
    drawIntoCanvas { canvas -> canvas.nativeCanvas.drawTextBlob(blob, 0f, 0f, paint) }
}

private val shaper = Shaper.make()

private val paint = Paint().apply { isAntiAlias = true }
