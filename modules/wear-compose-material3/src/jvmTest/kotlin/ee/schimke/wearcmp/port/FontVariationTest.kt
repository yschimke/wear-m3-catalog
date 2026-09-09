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

package ee.schimke.wearcmp.port

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.fail
import kotlin.test.assertTrue
import org.jetbrains.skia.Font

/**
 * [withFontVariation] against real glyph metrics.
 *
 * "It did not throw" would not be evidence here: a clone that ignored its axes would return a
 * perfectly good typeface drawing exactly the wrong shapes. So the assertions are about advance
 * widths — a heavier weight is a wider glyph, and that is measurable.
 *
 * Everything below needs the bundled Roboto Flex actually installed; on a host where
 * `installBundledWearFonts` cannot (wasm), these do not run at all.
 *
 * They live in `:wear-compose-material3` rather than beside the code they test because measuring a
 * glyph needs Skia's NATIVE library, which arrives with `compose.desktop.currentOs` — a dependency
 * this module's tests already have and `:port-runtime`'s do not.
 */
class FontVariationTest {

    private companion object {
        const val FAMILY = "roboto-flex"

        /**
         * Installed once, before anything in this class resolves a family — and before any other
         * test in the module gets the chance to, since a class initialiser runs on first touch.
         */
        val installed: Boolean = ee.schimke.wearcmp.fonts.installBundledWearFonts()
    }

    /**
     * The bundled Roboto Flex, as a family whose Skia typeface can be cloned.
     *
     * Fails rather than skips when it cannot be had. An earlier version returned null and every
     * test returned early, which passed just as happily with the axis cloning disabled — a
     * vacuous green. The failure mode it was hiding is real and is documented on
     * `deviceFontFamily`: resolution is CACHED BY NAME, so if anything in this JVM resolved
     * `roboto-flex` before `installBundledWearFonts` ran, the cache holds a SansSerif fallback
     * with no typeface to vary. Installing the fonts in a companion initialiser is what keeps
     * this ahead of any other test in the module.
     */
    private fun robotoFlex(): FontFamily {
        val family = deviceFontFamily(FAMILY)
        val typeface =
            skiaTypefaceForFamily(family)
                ?: fail(
                    "no Skia typeface for '$FAMILY' — installBundledWearFonts() returned " +
                        "$installed, and deviceFontFamily caches by name, so something is likely " +
                        "resolving this family before the fonts are installed"
                )
        assertTrue(
            typeface.familyName.contains("Roboto", ignoreCase = true),
            "expected the bundled Roboto Flex, got '${typeface.familyName}'",
        )
        return family
    }

    private fun widthOf(family: FontFamily, text: String = "Weight"): Float {
        val typeface = requireNotNull(skiaTypefaceForFamily(family)) { "no typeface for $family" }
        val font = Font(typeface, 40f)
        return font.measureTextWidth(text)
    }

    @Test
    fun aHeavierWeightDrawsWiderGlyphs() {
        val base = robotoFlex()
        val thin = base.withFontVariation(FontVariation.Settings(FontVariation.weight(100)))
        val black = base.withFontVariation(FontVariation.Settings(FontVariation.weight(1000)))

        assertNotEquals(thin, black, "two weights must not be the same family")
        assertTrue(
            widthOf(black) > widthOf(thin),
            "wght 1000 should measure wider than wght 100, got ${widthOf(black)} vs ${widthOf(thin)}",
        )
    }

    @Test
    fun theWidthAxisIsAppliedToo() {
        val base = robotoFlex()
        // `wdth` is the axis the port used to discard entirely — the type scale sets it on every
        // arc style, and Roboto Flex declares it 25..151.
        val narrow =
            base.withFontVariation(FontVariation.Settings(FontVariation.Setting("wdth", 25f)))
        val wide =
            base.withFontVariation(FontVariation.Settings(FontVariation.Setting("wdth", 151f)))
        assertTrue(
            widthOf(wide) > widthOf(narrow),
            "wdth 151 should measure wider than wdth 25, got ${widthOf(wide)} vs ${widthOf(narrow)}",
        )
    }

    @Test
    fun theSameAxesResolveToTheSameFamily() {
        val base = robotoFlex()
        val once = base.withFontVariation(FontVariation.Settings(FontVariation.weight(700)))
        val twice = base.withFontVariation(FontVariation.Settings(FontVariation.weight(700)))
        assertSame(once, twice, "the cache should be keyed by the axes, not merely by the family")
    }

    @Test
    fun noAxesIsTheFamilyItself() {
        val base = robotoFlex()
        assertSame(base, base.withFontVariation(FontVariation.Settings()))
    }

    /** A family this module never resolved has no typeface to clone, so it comes back unchanged. */
    @Test
    fun aForeignFamilyIsReturnedUnchanged() {
        val serif = FontFamily.Serif
        assertEquals(
            serif,
            serif.withFontVariation(FontVariation.Settings(FontVariation.weight(900))),
        )
    }
}
