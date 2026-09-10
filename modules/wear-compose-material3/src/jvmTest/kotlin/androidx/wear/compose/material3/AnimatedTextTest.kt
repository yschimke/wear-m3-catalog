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

package androidx.wear.compose.material3

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.internal.atSize
import androidx.wear.compose.material3.internal.shape
import androidx.wear.compose.material3.internal.metrics
import ee.schimke.wearcmp.port.deviceFontFamily
import ee.schimke.wearcmp.port.skiaTypefaceForFamily
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * `AnimatedText`'s shaping and font interpolation, measured rather than asserted to have run.
 *
 * The thing that could silently be wrong here is the same one `FontVariationTest` guards for the
 * type scale: a font built from variation axes that quietly ignores them still shapes, still draws,
 * and still passes any test that only checks for the absence of an exception. So every assertion
 * below is about a MEASURED advance — a heavier weight is a wider run, and an interpolated weight
 * is a run between the two.
 *
 * Needs the bundled Roboto Flex: it is the variable font the animation is designed around, and a
 * font with no `wght` axis would make all three widths equal.
 */
class AnimatedTextTest {

    private companion object {
        const val FAMILY = "roboto-flex"

        /** Ahead of any other test in the module — `deviceFontFamily` caches by name. */
        val installed: Boolean = ee.schimke.wearcmp.fonts.installBundledWearFonts()

        val Thin = FontVariation.Settings(FontVariation.weight(100))
        val Black = FontVariation.Settings(FontVariation.weight(1000))
    }

    private fun robotoFlex(): FontFamily {
        val family = deviceFontFamily(FAMILY)
        skiaTypefaceForFamily(family)
            ?: fail(
                "no Skia typeface for '$FAMILY' — installBundledWearFonts() returned $installed"
            )
        return family
    }

    private fun registry(
        start: FontVariation.Settings = Thin,
        end: FontVariation.Settings = Black,
        startSize: Int = 40,
        endSize: Int = 40,
    ) =
        AnimatedTextFontRegistry(
            textStyle = TextStyle(fontFamily = robotoFlex(), fontSize = startSize.sp),
            startFontVariationSettings = start,
            endFontVariationSettings = end,
            startFontSize = startSize.sp,
            endFontSize = endSize.sp,
            density = Density(1f),
            fontFamilyResolver = createFontFamilyResolver(),
        )

    private fun widthAt(registry: AnimatedTextFontRegistry, fraction: Float): Float =
        registry
            .getFont(fraction)
            .atSize(registry.getFontSize(fraction))
            .shape("12:34", rtl = false)
            .advance

    /** The axes reach the shaper at all: the end of the animation is wider than the start. */
    @Test
    fun aHeavierEndShapesWider() {
        val registry = registry()
        assertTrue(
            widthAt(registry, 1f) > widthAt(registry, 0f),
            "wght 1000 should shape wider than wght 100, got " +
                "${widthAt(registry, 1f)} vs ${widthAt(registry, 0f)}",
        )
    }

    /** And they are INTERPOLATED, not switched: the middle of the animation is in the middle. */
    @Test
    fun theMiddleOfTheAnimationIsBetweenTheEnds() {
        val registry = registry()
        val start = widthAt(registry, 0f)
        val middle = widthAt(registry, 0.5f)
        val end = widthAt(registry, 1f)
        assertTrue(
            middle > start && middle < end,
            "expected $start < $middle < $end at half way through the animation",
        )
    }

    /** An axis the end does not mention holds its start value rather than falling to zero. */
    @Test
    fun anUnmatchedAxisHoldsItsStartValue() {
        val registry =
            registry(
                start = FontVariation.Settings(FontVariation.weight(1000)),
                end = FontVariation.Settings(FontVariation.Setting("wdth", 100f)),
            )
        val onlyWeightRegistry =
            registry(
                start = FontVariation.Settings(FontVariation.weight(1000)),
                end = FontVariation.Settings(FontVariation.weight(1000)),
            )
        assertEquals(
            widthAt(onlyWeightRegistry, 1f),
            widthAt(registry, 1f),
            absoluteTolerance = 0.01f,
            message = "`wght` is not in the end settings, so it should stay at 1000 throughout",
        )
    }

    /** The size animates independently of the axes, and scales the run with it. */
    @Test
    fun theSizeAnimatesToo() {
        val registry = registry(start = Thin, end = Thin, startSize = 20, endSize = 40)
        assertEquals(30f, registry.getFontSize(0.5f), absoluteTolerance = 0.01f)
        assertTrue(
            widthAt(registry, 1f) > widthAt(registry, 0f) * 1.5f,
            "doubling the size should roughly double the run",
        )
    }

    /**
     * Fractions inside one step share a cached font, and fractions in different steps do not.
     *
     * This is what the snapping is for: 60 fonts for a one-second animation instead of one per
     * frame at whatever rate the display runs.
     */
    @Test
    fun theFractionIsSnappedForTheCache() {
        val registry = registry()
        assertSame(
            registry.getFont(0.5f),
            registry.getFont(0.505f),
            "0.5 and 0.505 are the same 0.016 step",
        )
        assertNotSame(registry.getFont(0.5f), registry.getFont(0.6f))
    }

    /** The ends are the fonts the registry was built with, not another cache entry. */
    @Test
    fun theEndsAreNotCacheEntries() {
        val registry = registry()
        assertSame(registry.startFont, registry.getFont(0f))
        assertSame(registry.endFont, registry.getFont(1f))
    }

    /** Shaping gives one x/y pair per glyph — what the frame-by-frame interpolation walks. */
    @Test
    fun everyGlyphHasAPosition() {
        val run = registry().startFont.shape("12:34", rtl = false)
        assertEquals(5, run.glyphs.size, "five characters, five glyphs in a font with no ligatures")
        assertEquals(run.glyphs.size * 2, run.positions.size)
        assertTrue(run.advance > 0f)
    }

    /** Metrics come from the sized font: a bigger font is a taller line. */
    @Test
    fun metricsScaleWithTheFont() {
        val registry = registry(start = Thin, end = Thin, startSize = 20, endSize = 40)
        val small = registry.startFont.metrics()
        val large = registry.endFont.metrics()
        assertTrue(small.ascent < 0f, "ascent is negative above the baseline")
        assertTrue(
            large.descent - large.ascent > small.descent - small.ascent,
            "40sp should be taller than 20sp",
        )
    }

    @Test
    fun theFirstStrongCharacterDecidesTheDirection() {
        assertNull(firstStrongIsRtl("12:34"), "digits have no direction of their own")
        assertNull(firstStrongIsRtl(""))
        assertEquals(false, firstStrongIsRtl("12 hours"))
        assertEquals(true, firstStrongIsRtl("שלום"))
        assertEquals(true, firstStrongIsRtl("12 שלום"))
        assertEquals(false, firstStrongIsRtl("hello שלום"))
    }
}
