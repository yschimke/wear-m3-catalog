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

import ee.schimke.wearcmp.port.TimePatternLocales
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The two CLDR-derived tables in `:port-runtime` must cover every locale the resources ship.
 *
 * Both are generated from ICU and committed, so nothing regenerates them when `tools/transform.py`
 * picks up a new locale from a new AAR. This is the test that notices — without it, a locale added
 * upstream would silently fall back to English's plural rule and an American clock, which is the
 * exact pair of bugs those tables were written to end.
 */
class LocaleCoverageTest {

    private val shipped = GeneratedLocalizedPlurals.keys

    @Test
    fun everyShippedLocaleHasATimePattern() {
        val missing = shipped - TimePatternLocales
        assertTrue(
            missing.isEmpty(),
            "no ICU time pattern for $missing — regenerate the table in TimePatterns.kt",
        )
    }

    /**
     * Every plural category a locale's rule can actually select must have a translated form.
     *
     * This is the direction that matters. The reverse — a locale shipping a form no count reaches —
     * is normal and not a fault: CLDR gives Spanish, French, Italian and Portuguese a `many` used
     * only for millions, and Android ships a redundant `one` for languages that make no count
     * distinction at all. What WOULD be a fault is the rule selecting `few` for a language whose
     * translators supplied none, silently falling back to `other`; a wrong rule set looks exactly
     * like that.
     *
     * It also pins a claim made in `PluralRules.kt`: `other` is unreachable for an integer count in
     * the East Slavic languages and Polish. Those four are precisely the locales that ship an
     * `other` form no count here selects.
     */
    @Test
    fun everyReachableCategoryHasATranslatedForm() {
        val gaps = mutableListOf<String>()
        for ((tag, resources) in GeneratedLocalizedPlurals) {
            val reachable = (0..200).map { pluralCategory(tag, it).keyword }.toSet()
            for ((resource, forms) in resources) {
                val missing = reachable - forms.keys
                if (missing.isNotEmpty()) gaps += "$tag/$resource lacks $missing"
            }
        }
        assertTrue(gaps.isEmpty(), gaps.joinToString("\n"))
    }
}
