/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.wear.compose.material3

import androidx.collection.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.util.lerp
import androidx.wear.compose.foundation.LocalReduceMotion
import androidx.wear.compose.material3.internal.AnimatedTextAxis
import androidx.wear.compose.material3.internal.AnimatedTextFont
import androidx.wear.compose.material3.internal.ShapedRun
import androidx.wear.compose.material3.internal.atSize
import androidx.wear.compose.material3.internal.drawAnimatedText
import androidx.wear.compose.material3.internal.metrics
import androidx.wear.compose.material3.internal.resolveAnimatedTextFont
import androidx.wear.compose.material3.internal.shape
import androidx.wear.compose.material3.internal.withAxes
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A composable that displays an animated text.
 *
 * AnimatedText can be used to animate a text along font variation axes and size. It requires an
 * [AnimatedTextFontRegistry] to improve performance.
 *
 * [AnimatedTextFontRegistry] can be generated using [rememberAnimatedTextFontRegistry] method,
 * which requires start and end font variation axes, and start and end font sizes for the animation.
 *
 * Start of the animation is when the animatable is at 0f and end of the animation is when the
 * animatable is at 1f. Current animation progress is provided by the [progressFraction] function.
 * This should be between 0f and 1f, but might go beyond in some cases such as overshooting spring
 * animations.
 *
 * @param text The text to be displayed.
 * @param fontRegistry The font registry to be used to animate the text.
 * @param progressFraction A provider for the current state of the animation. Provided value should
 *   be between 0f and 1f, but might go beyond in some cases such as overshooting spring animations.
 * @param modifier Modifier to be applied to the composable.
 * @param contentAlignment Alignment within the bounds of the Canvas.
 */
@Composable
public fun AnimatedText(
    text: String,
    fontRegistry: AnimatedTextFontRegistry,
    progressFraction: () -> Float,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val isReduceMotionEnabled = LocalReduceMotion.current
    val animatedTextState =
        remember(fontRegistry, layoutDirection, density) {
            AnimatedTextState(fontRegistry, layoutDirection, density)
        }

    // Update before composing Canvas to make sure size gets updated
    animatedTextState.updateText(text)
    Canvas(
        modifier.size(animatedTextState.size).semantics {
            apply { this.text = AnnotatedString(text) }
        }
    ) {
        // Update text if ReduceMotion is enabled to make sure the new text value is used
        if (isReduceMotionEnabled) {
            animatedTextState.updateText(text)
        }

        // If ReduceMotion is enabled, show static text with the end font configuration.
        val fraction = if (isReduceMotionEnabled) 1f else progressFraction()

        with(animatedTextState) { draw(contentAlignment, fraction) }
    }
}

/**
 * Generates an [AnimatedTextFontRegistry] to use within composition.
 *
 * Start and end of the animation is when the animatable is at 0f and 1f, respectively. This API
 * supports overshooting, so a generated font can be extrapolated outside
 * [startFontVariationSettings] and [endFontVariationSettings] range, developers need to make sure
 * the given font supports possible font variation settings throughout the animation.
 *
 * @param startFontVariationSettings Font variation settings at the start of the animation
 * @param endFontVariationSettings Font variation settings at the end of the animation
 * @param textStyle Text style to be used for the animation
 * @param startFontSize Font size at the start of the animation
 * @param endFontSize Font size at the end of the animation
 */
@Composable
public fun rememberAnimatedTextFontRegistry(
    startFontVariationSettings: FontVariation.Settings,
    endFontVariationSettings: FontVariation.Settings,
    textStyle: TextStyle = LocalTextStyle.current,
    startFontSize: TextUnit = textStyle.fontSize,
    endFontSize: TextUnit = textStyle.fontSize,
): AnimatedTextFontRegistry {
    // Convert current compose values to types we can use on Canvas
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val contentColor = textStyle.color.takeOrElse { LocalContentColor.current }
    return remember(
        textStyle,
        startFontVariationSettings.toString(),
        endFontVariationSettings.toString(),
        startFontSize,
        endFontSize,
        contentColor,
        fontFamilyResolver,
    ) {
        AnimatedTextFontRegistry(
            textStyle,
            startFontVariationSettings,
            endFontVariationSettings,
            startFontSize,
            endFontSize,
            density,
            fontFamilyResolver,
            contentColor,
        )
    }
}

/**
 * Generates fonts to be used by [AnimatedText] throughout the animation.
 *
 * Start and end of the animation is when the animatable is at 0f and 1f, respectively. This API
 * supports overshooting, so a generated font can be extrapolated outside
 * [startFontVariationSettings] and [endFontVariationSettings] range, developers need to make sure
 * the given font supports possible font variation settings throughout the animation.
 *
 * [AnimatedTextFontRegistry] can be re-used between multiple [AnimatedText] composables to save
 * memory and improve performance where needed and feasible.
 *
 * @param startFontVariationSettings Font variation settings at the start of the animation
 * @param endFontVariationSettings Font variation settings at the end of the animation
 * @param textStyle Text style to be used for the animation
 * @param startFontSize Font size at the start of the animation
 * @param endFontSize Font size at the end of the animation
 * @param density Current density, used to to convert font sizes
 * @param contentColor Content color of the animated text
 * @param fontFamilyResolver Current Resolver to use to resolve font families
 * @param cacheSize Size of the cache used to store animated variable fonts, this can be increased
 *   to improve animation performance if needed, but it also increases the memory usage.
 */
public class AnimatedTextFontRegistry(
    private val textStyle: TextStyle,
    private val startFontVariationSettings: FontVariation.Settings,
    private val endFontVariationSettings: FontVariation.Settings,
    private val startFontSize: TextUnit,
    private val endFontSize: TextUnit,
    private val density: Density,
    fontFamilyResolver: FontFamily.Resolver,
    private val contentColor: Color = textStyle.color,
    cacheSize: Int = AnimatedTextDefaults.CacheSize,
) {
    private val startFontSizePx = with(density) { startFontSize.toPx() }
    private val endFontSizePx = with(density) { endFontSize.toPx() }

    /** The text direction specified in [textStyle]. */
    internal val textDirection = textStyle.textDirection

    /** The colour the run is drawn in. */
    internal val color = contentColor

    /**
     * The typeface with no axes applied, at the start size.
     *
     * Upstream reaches the same object the long way round — it shapes the text once with a
     * `TextPaint` purely to pull `PositionedGlyphs.getFont(0)` back out of the result, with a
     * comment wondering whether there is another way. There is, on Skia: the typeface is what the
     * resolver returns, so nothing has to be shaped to find it.
     */
    private val baseFont: AnimatedTextFont =
        resolveAnimatedTextFont(
            textStyle.fontFamily,
            textStyle.fontWeight,
            textStyle.fontStyle,
            textStyle.fontSynthesis,
            fontFamilyResolver,
            startFontSizePx,
        )

    /** Font at the start of the animation. */
    internal val startFont: AnimatedTextFont = baseFont.withAxes(axesAt(0f)).atSize(startFontSizePx)

    /** Font at the end of the animation. */
    internal val endFont: AnimatedTextFont = baseFont.withAxes(axesAt(1f)).atSize(endFontSizePx)

    /**
     * Font cache for animation steps, between start font at 0f and end font at 1f.
     *
     * Keyed by the SNAPPED fraction, and holding the axes only: the size is applied on top at draw
     * time, so a cache hit is a hit for every size the same axes are drawn at.
     */
    private val fontCache = LruCache<Float, AnimatedTextFont>(cacheSize)

    /** Returns the font at a certain [fraction] of the animation. */
    internal fun getFont(fraction: Float): AnimatedTextFont =
        when (fraction) {
            0f -> startFont
            1f -> endFont
            else -> {
                val snapped =
                    floor(fraction / AnimatedTextDefaults.FractionStep) *
                        AnimatedTextDefaults.FractionStep
                fontCache[snapped]
                    ?: baseFont.withAxes(axesAt(snapped)).also { fontCache.put(snapped, it) }
            }
        }

    /** Returns the font size at a certain point in the animation. */
    internal fun getFontSize(fraction: Float): Float =
        lerp(startFontSizePx, endFontSizePx, fraction)

    /**
     * The variation axes at [fraction] of the way through the animation.
     *
     * Upstream's arithmetic exactly: the START settings decide which axes exist and in what order,
     * each is matched to the end setting of the same name, and an axis the end does not mention
     * holds its start value. Overshooting is deliberate — `lerp` is not clamped, so a spring that
     * goes past 1f keeps driving the axis, which is what the doc on this class promises.
     */
    private fun axesAt(fraction: Float): List<AnimatedTextAxis> =
        startFontVariationSettings.settings.map { startSetting ->
            val endSetting =
                endFontVariationSettings.settings.firstOrNull {
                    it.axisName == startSetting.axisName
                } ?: startSetting
            AnimatedTextAxis(
                startSetting.axisName,
                lerp(
                    startSetting.toVariationValue(density),
                    endSetting.toVariationValue(density),
                    fraction,
                ),
            )
        }
}

/** Defaults for AnimatedText. */
public object AnimatedTextDefaults {
    /** Default font cache size to be used in AnimatedTextFontRegistry. */
    public val CacheSize: Int = 5

    /**
     * Default step size used to snap progress fractions. Progress fractions will be rounded down to
     * a multiple of this to increase cache efficiency.
     *
     * 0.016f is chosen to divide a 1 second animation into 60 animation steps.
     */
    internal val FractionStep = 0.016f
}

/**
 * Animated text state.
 *
 * Generates the fonts required using the given [animatedFontRegistry] and draws the animation
 * inside an [AnimatedText] controlled Canvas.
 *
 * This is generated by [AnimatedText].
 */
internal class AnimatedTextState
internal constructor(
    private val animatedFontRegistry: AnimatedTextFontRegistry,
    private val layoutDirection: LayoutDirection,
    private val density: Density,
) {
    /**
     * Required size for the canvas.
     *
     * It's a mutable state to make sure it triggers recomposition when the text changes.
     */
    internal var size: DpSize by mutableStateOf(DpSize.Zero)

    /** Updates the text to draw. */
    internal fun updateText(text: String) {
        if (currentText == text) {
            return
        }
        currentText = text
        recalculateSizeAndPositions()
    }

    /** Draws the text. */
    internal fun DrawScope.draw(contentAlignment: Alignment, fraction: Float) {
        // Nothing to draw, return early. `size` is this state's, not the DrawScope's.
        if (this@AnimatedTextState.size == DpSize.Zero) {
            return
        }
        val start = startRun ?: return
        val end = endRun ?: return

        val widthPx = lerp(startWidthPx, endWidthPx, fraction)
        val heightPx = lerp(startHeightPx, endHeightPx, fraction)
        val offset =
            contentAlignment.align(
                IntSize(widthPx.roundToInt(), heightPx.roundToInt()),
                intSize,
                if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            )
        translate(
            if (isRtl) widthPx + offset.x.toFloat() else offset.x.toFloat(),
            offset.y.toFloat() +
                heightPx / 2 +
                lerp(startBaselineOffset, endBaselineOffset, fraction) / 2 +
                lerp(startAscentPx, endAscentPx, fraction) / 4,
        ) {
            val font =
                animatedFontRegistry
                    .getFont(fraction)
                    .atSize(animatedFontRegistry.getFontSize(fraction))
            drawAnimatedText(font, start, end, fraction, animatedFontRegistry.color)
        }
    }

    /**
     * Same as [size], but in pixels, used to calculate content offset according to content
     * alignment.
     */
    private var intSize = IntSize(0, 0)

    /** Content height at the start of the animation, in px */
    private var startHeightPx = 0f

    /** Content height at the end of the animation, in px */
    private var endHeightPx = 0f

    /** Baseline offset at the start, used for content alignment. */
    private var startBaselineOffset: Int = 0

    /** Baseline offset at the end, used for content alignment. */
    private var endBaselineOffset: Int = 0

    /** Font ascent at the start of the animation, used for content alignment. */
    private var startAscentPx = 0

    /** Font ascent at the end of the animation, used for content alignment. */
    private var endAscentPx = 0

    /** Current text. */
    private var currentText: String = ""

    /** If text direction is right-to-left or left-to-right. */
    private var isRtl = false

    /** Content width at the start of the animation, in px */
    private var startWidthPx = 0f

    /** Content width at the end of the animation, in px */
    private var endWidthPx = 0f

    /** The run shaped with the font at the start of the animation. */
    private var startRun: ShapedRun? = null

    /** The run shaped with the font at the end of the animation. */
    private var endRun: ShapedRun? = null

    /**
     * Calculates required canvas size to draw the text, font ascent and baseline offset for the
     * font, positions for the glyphs at the start and the end of the animation.
     */
    private fun recalculateSizeAndPositions() {
        with(density) {
            intSize = IntSize(calculateMaxWidth().roundToInt(), calculateMaxHeight().roundToInt())
            size = DpSize(intSize.width.toDp(), intSize.height.toDp())
        }
    }

    private fun calculateMaxWidth(): Float {
        startWidthPx = 0f
        endWidthPx = 0f
        startRun = null
        endRun = null
        if (currentText.isEmpty()) {
            return 0f
        }

        isRtl = resolveRtl()
        val start = animatedFontRegistry.startFont.shape(currentText, isRtl)
        val end = animatedFontRegistry.endFont.shape(currentText, isRtl)
        startRun = start
        endRun = end
        startWidthPx = start.advance
        endWidthPx = end.advance

        return max(startWidthPx, endWidthPx)
    }

    /**
     * Resolves the base direction of the run from the registry's text style, falling back on the
     * layout direction when the style does not say.
     *
     * Upstream hands the same decision to `TextDirectionHeuristics`, which is Android's. The
     * content-driven cases are first-strong: the first character with a direction of its own
     * decides, and a run of digits or punctuation with none takes the fallback. That is a rule
     * about Unicode rather than about Android, so it is written out here.
     */
    private fun resolveRtl(): Boolean {
        val layoutIsRtl = layoutDirection == LayoutDirection.Rtl
        return when (animatedFontRegistry.textDirection) {
            TextDirection.Rtl -> true
            TextDirection.Ltr -> false
            TextDirection.ContentOrRtl -> firstStrongIsRtl(currentText) ?: true
            TextDirection.ContentOrLtr -> firstStrongIsRtl(currentText) ?: false
            else -> firstStrongIsRtl(currentText) ?: layoutIsRtl
        }
    }

    private fun calculateMaxHeight(): Float {
        if (currentText.isEmpty()) {
            startHeightPx = 0f
            startAscentPx = 0
            startBaselineOffset = 0
            endHeightPx = 0f
            endAscentPx = 0
            endBaselineOffset = 0
            return 0f
        }
        val startMetrics = animatedFontRegistry.startFont.metrics()
        startHeightPx = startMetrics.descent - startMetrics.ascent
        startAscentPx = startMetrics.ascent.roundToInt()
        startBaselineOffset = -startMetrics.top.roundToInt()
        val endMetrics = animatedFontRegistry.endFont.metrics()
        endHeightPx = endMetrics.descent - endMetrics.ascent
        endAscentPx = endMetrics.ascent.roundToInt()
        endBaselineOffset = -endMetrics.top.roundToInt()
        return max(startHeightPx, endHeightPx)
    }
}

/**
 * Whether the first character with a strong direction is right-to-left, or `null` when the text has
 * none.
 *
 * The strong right-to-left blocks: Hebrew, Arabic, Syriac, Thaana, N'Ko, Samaritan and Mandaic
 * (0590–08FF), the Hebrew and Arabic presentation forms (FB1D–FDFF, FE70–FEFF), and, past the BMP,
 * Kharoshthi through Arabic Mathematical Alphabetic Symbols (10800–10FFF, 1E800–1EFFF) — which are
 * surrogate pairs in a Kotlin string, so they are read as code points rather than as chars.
 */
internal fun firstStrongIsRtl(text: String): Boolean? {
    var index = 0
    while (index < text.length) {
        val code = text.codePointAtCompat(index)
        index += if (code > 0xFFFF) 2 else 1
        when {
            code in 0x0590..0x08FF ||
                code in 0xFB1D..0xFDFF ||
                code in 0xFE70..0xFEFF ||
                code in 0x10800..0x10FFF ||
                code in 0x1E800..0x1EFFF -> return true
            code in 0x0041..0x005A ||
                code in 0x0061..0x007A ||
                code in 0x00C0..0x058F ||
                code in 0x0900..0x1FFF ||
                code in 0x2C00..0xD7FF -> return false
        }
    }
    return null
}

private fun String.codePointAtCompat(index: Int): Int {
    val high = this[index]
    if (high.isHighSurrogate() && index + 1 < length) {
        val low = this[index + 1]
        if (low.isLowSurrogate()) {
            return 0x10000 + ((high.code - 0xD800) shl 10) + (low.code - 0xDC00)
        }
    }
    return high.code
}
