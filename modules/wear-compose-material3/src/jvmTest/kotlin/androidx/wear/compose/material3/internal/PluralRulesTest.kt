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

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [pluralCategory] against ICU, for every shipped locale and every count from 0 to 200.
 *
 * The comparison is with a committed fixture rather than a live ICU dependency: this module's tests
 * should not pull 14 MB of CLDR data to answer a question whose answer does not change between
 * runs. `cldr-plural-categories.tsv` is one row per locale — the tag, a tab, then 201 characters,
 * one per count, coded `z 1 2 f m o` for zero/one/two/few/many/other.
 *
 * To regenerate it after a locale is added or a CLDR release moves a rule:
 * ```
 * curl -sSLO https://repo1.maven.org/maven2/com/ibm/icu/icu4j/77.1/icu4j-77.1.jar
 * // for each tag: PluralRules.forLocale(ULocale(tag)).select(n.toDouble()) for n in 0..200
 * ```
 * and map the keywords onto those six letters. The locale list is the keys of
 * `GeneratedLocalizedPlurals`, plus `en` for the default resources.
 */
class PluralRulesTest {

    private val expected: Map<String, String> =
        checkNotNull(javaClass.getResourceAsStream("/cldr-plural-categories.tsv")) {
                "the ICU fixture is missing"
            }
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
            .associate { line ->
                val (tag, categories) = line.split("\t")
                tag to categories
            }

    @Test
    fun everyShippedLocaleAgreesWithIcuForEveryCount() {
        val mismatches = mutableListOf<String>()
        for ((tag, categories) in expected) {
            for (count in categories.indices) {
                val actual = pluralCategory(tag, count).code
                if (actual != categories[count]) {
                    mismatches += "$tag n=$count: expected ${categories[count]}, was $actual"
                }
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches:\n" + mismatches.joinToString("\n"))
    }

    @Test
    fun theFixtureCoversEveryLocaleTheResourcesShip() {
        val shipped = GeneratedLocalizedPlurals.keys
        val missing = shipped - expected.keys
        assertTrue(missing.isEmpty(), "no ICU fixture for $missing — regenerate it")
        // Plus `en`, which the default (unqualified) resources are written in.
        assertTrue("en" in expected)
    }

    /**
     * The port's own resolution, not ICU's: the tag chooses the rule, so a fallback from `pt-PT` to
     * `pt` must not carry European Portuguese's rule onto Brazilian text. This is the one locale
     * pair in the set where the two disagree.
     */
    @Test
    fun europeanAndBrazilianPortugueseDisagreeAboutZero() {
        assertEquals(PluralCategory.Other, pluralCategory("pt-PT", 0))
        assertEquals(PluralCategory.One, pluralCategory("pt", 0))
        assertEquals(PluralCategory.One, pluralCategory("pt-BR", 0))
        assertEquals(PluralCategory.One, pluralCategory("pt-PT", 1))
    }

    /** An unknown language falls back to English's rule rather than throwing. */
    @Test
    fun anUnknownLanguageFallsBackToOneForOne() {
        assertEquals(PluralCategory.One, pluralCategory("qqq", 1))
        assertEquals(PluralCategory.Other, pluralCategory("qqq", 2))
    }

    /**
     * The end-to-end path: a Polish count of five reads the `many` form, which is a different word
     * from both `one` and `few`. This is the bug the rule table fixes, seen from the call site.
     */
    @Test
    fun polishReadsAllThreeOfItsForms() {
        val hours = Plurals.TimePickerHoursContentDescription
        val polish = Locale("pl")
        val one = hours.textIn(polish, 1)
        val few = hours.textIn(polish, 2)
        val many = hours.textIn(polish, 5)
        assertEquals(3, setOf(one, few, many).size, "expected three distinct forms, got $one / $few / $many")
    }

    private val PluralCategory.code: Char
        get() =
            when (this) {
                PluralCategory.Zero -> 'z'
                PluralCategory.One -> '1'
                PluralCategory.Two -> '2'
                PluralCategory.Few -> 'f'
                PluralCategory.Many -> 'm'
                PluralCategory.Other -> 'o'
            }
}
