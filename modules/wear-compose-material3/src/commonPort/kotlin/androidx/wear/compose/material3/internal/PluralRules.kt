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

/**
 * Which grammatical form a language uses for a given count — CLDR's plural categories.
 *
 * Android picks these through `Resources.getQuantityString`, reading the same CLDR data the
 * translations were written against. Off-Android there is no such call, and the port previously
 * asked only whether the quantity was one: English's rule, applied to all 85 languages. The text
 * was in the right language and could be in the wrong form for any count but one — Polish needs
 * `few` and `many`, Arabic uses all six.
 *
 * The translations were never the problem: `GeneratedLocalizedPlurals` already carries every
 * keyword each locale ships, because `tools/transform.py` extracts whatever is in the AAR. Only the
 * SELECTION was wrong, and that is what this file supplies.
 *
 * ## Integers only
 *
 * CLDR rules are written against six operands — `n i v w f t` — that distinguish 1 from 1.0 from
 * 1.00. Every quantity in this library is an `Int` (hours, minutes, seconds), so `v`, `w`, `f` and
 * `t` are all zero and `n` equals `i`. The rules below are those CLDR rules with the fractional
 * operands folded out, which is why they read more simply than the published source.
 *
 * ## Verified, not asserted
 *
 * `PluralRulesTest` checks every locale in `GeneratedLocalizedPlurals` for every count from 0 to
 * 200 against a fixture generated from ICU4J. The fixture is committed so the test needs no
 * dependency; regenerate it with the instructions in that test if the locale list ever changes.
 */
internal enum class PluralCategory(val keyword: String) {
    Zero("zero"),
    One("one"),
    Two("two"),
    Few("few"),
    Many("many"),
    Other("other"),
}

/**
 * The rule sets the shipped locales use. Eighty-six locale tags collapse to fifteen of these — the
 * grouping is CLDR's own, and languages share a set when their category function agrees exactly.
 */
private enum class PluralRuleSet {
    /** `one` for exactly 1. English, German, Spanish, Swedish and thirty-odd more. */
    OneForOne,
    /** `one` for 0 and 1. French, Portuguese (Brazil), Hindi, Persian, Zulu. */
    OneForZeroAndOne,
    /** No count distinction at all. Chinese, Japanese, Korean, Thai, Vietnamese, Malay. */
    OtherOnly,
    /** Bosnian, Croatian, Serbian. */
    SouthSlavic,
    /** Belarusian, Russian, Ukrainian. */
    EastSlavic,
    /** Czech, Slovak. */
    WestSlavic,
    /** Icelandic, Macedonian. */
    OneForEndingInOne,
    /** Arabic — the only locale here that uses all six categories. */
    Arabic,
    /** Hebrew. */
    Hebrew,
    /** Lithuanian. */
    Lithuanian,
    /** Latvian. */
    Latvian,
    /** Polish. */
    Polish,
    /** Romanian. */
    Romanian,
    /** Slovenian. */
    Slovenian,
    /** Tagalog. */
    Tagalog,
}

/**
 * The category [count] takes in [languageTag].
 *
 * The tag is matched the way [localeCandidates] matches strings — `pt-PT` before `pt` — because the
 * two genuinely differ here: European Portuguese takes `one` for 1 alone, Brazilian for 0 and 1.
 * An unknown language falls back to [PluralRuleSet.OneForOne], English's rule, which is what the
 * port did for everything before this.
 */
internal fun pluralCategory(languageTag: String, count: Int): PluralCategory =
    ruleSetFor(languageTag).categoryFor(count)

private fun ruleSetFor(languageTag: String): PluralRuleSet {
    RegionalPluralRuleSets[languageTag]?.let {
        return it
    }
    val language = languageTag.substringBefore('-')
    return PluralRuleSets[language] ?: PluralRuleSet.OneForOne
}

private fun PluralRuleSet.categoryFor(count: Int): PluralCategory {
    // CLDR's rules are written for non-negative counts. A negative one has the same shape as its
    // magnitude in every language here, and none of the three call sites can produce one anyway.
    val n = if (count < 0) -count else count
    return when (this) {
        PluralRuleSet.OneForOne -> if (n == 1) PluralCategory.One else PluralCategory.Other
        PluralRuleSet.OneForZeroAndOne ->
            if (n == 0 || n == 1) PluralCategory.One else PluralCategory.Other
        PluralRuleSet.OtherOnly -> PluralCategory.Other
        PluralRuleSet.SouthSlavic ->
            when {
                n % 10 == 1 && n % 100 != 11 -> PluralCategory.One
                n % 10 in 2..4 && n % 100 !in 12..14 -> PluralCategory.Few
                else -> PluralCategory.Other
            }
        PluralRuleSet.EastSlavic ->
            when {
                n % 10 == 1 && n % 100 != 11 -> PluralCategory.One
                n % 10 in 2..4 && n % 100 !in 12..14 -> PluralCategory.Few
                // Every remaining integer: 0, 5..9, and the 11..14 teens the two rules above
                // excluded. `other` is unreachable for an integer count in these languages.
                else -> PluralCategory.Many
            }
        PluralRuleSet.WestSlavic ->
            when {
                n == 1 -> PluralCategory.One
                n in 2..4 -> PluralCategory.Few
                else -> PluralCategory.Other
            }
        PluralRuleSet.OneForEndingInOne ->
            if (n % 10 == 1 && n % 100 != 11) PluralCategory.One else PluralCategory.Other
        PluralRuleSet.Arabic ->
            when {
                n == 0 -> PluralCategory.Zero
                n == 1 -> PluralCategory.One
                n == 2 -> PluralCategory.Two
                n % 100 in 3..10 -> PluralCategory.Few
                n % 100 in 11..99 -> PluralCategory.Many
                else -> PluralCategory.Other
            }
        PluralRuleSet.Hebrew ->
            when (n) {
                1 -> PluralCategory.One
                2 -> PluralCategory.Two
                else -> PluralCategory.Other
            }
        PluralRuleSet.Lithuanian ->
            when {
                n % 10 == 1 && n % 100 !in 11..19 -> PluralCategory.One
                n % 10 in 2..9 && n % 100 !in 11..19 -> PluralCategory.Few
                else -> PluralCategory.Other
            }
        PluralRuleSet.Latvian ->
            when {
                n % 10 == 0 || n % 100 in 11..19 -> PluralCategory.Zero
                n % 10 == 1 && n % 100 != 11 -> PluralCategory.One
                else -> PluralCategory.Other
            }
        PluralRuleSet.Polish ->
            when {
                n == 1 -> PluralCategory.One
                n % 10 in 2..4 && n % 100 !in 12..14 -> PluralCategory.Few
                else -> PluralCategory.Many
            }
        PluralRuleSet.Romanian ->
            when {
                n == 1 -> PluralCategory.One
                n == 0 || n % 100 in 1..19 -> PluralCategory.Few
                else -> PluralCategory.Other
            }
        PluralRuleSet.Slovenian ->
            when {
                n % 100 == 1 -> PluralCategory.One
                n % 100 == 2 -> PluralCategory.Two
                n % 100 in 3..4 -> PluralCategory.Few
                else -> PluralCategory.Other
            }
        // Tagalog counts by the LAST DIGIT, and the odd one out: 4, 6 and 9 take `other`, every
        // other digit takes `one` — including 0. 1, 2 and 3 are `one` whatever they end in.
        PluralRuleSet.Tagalog ->
            if (n in 1..3 || n % 10 !in setOf(4, 6, 9)) PluralCategory.One else PluralCategory.Other
    }
}

/**
 * Language to rule set. Keyed by language subtag; see [RegionalPluralRuleSets] for the one locale
 * whose region changes the answer.
 */
private val PluralRuleSets: Map<String, PluralRuleSet> =
    buildMap {
        listOf(
                "af", "az", "bg", "ca", "da", "de", "el", "en", "es", "et", "eu", "fi", "gl", "hu",
                "it", "ka", "kk", "ky", "ml", "mn", "mr", "nb", "ne", "nl", "or", "sq", "sv", "sw",
                "ta", "te", "tr", "ur", "uz",
            )
            .forEach { put(it, PluralRuleSet.OneForOne) }
        listOf("am", "as", "bn", "fa", "fr", "gu", "hi", "hy", "kn", "pa", "pt", "si", "zu")
            .forEach { put(it, PluralRuleSet.OneForZeroAndOne) }
        // `in` and `iw` are the historical codes for Indonesian and Hebrew, and are what Android
        // resource directories use; the AAR ships `values-in` and `values-iw`, so the port's tags
        // are those and not `id` / `he`. Both spellings are mapped.
        listOf("in", "id", "ja", "km", "ko", "lo", "ms", "my", "th", "vi", "zh").forEach {
            put(it, PluralRuleSet.OtherOnly)
        }
        listOf("bs", "hr", "sr").forEach { put(it, PluralRuleSet.SouthSlavic) }
        listOf("be", "ru", "uk").forEach { put(it, PluralRuleSet.EastSlavic) }
        listOf("cs", "sk").forEach { put(it, PluralRuleSet.WestSlavic) }
        listOf("is", "mk").forEach { put(it, PluralRuleSet.OneForEndingInOne) }
        put("ar", PluralRuleSet.Arabic)
        listOf("iw", "he").forEach { put(it, PluralRuleSet.Hebrew) }
        put("lt", PluralRuleSet.Lithuanian)
        put("lv", PluralRuleSet.Latvian)
        put("pl", PluralRuleSet.Polish)
        put("ro", PluralRuleSet.Romanian)
        put("sl", PluralRuleSet.Slovenian)
        put("tl", PluralRuleSet.Tagalog)
        put("fil", PluralRuleSet.Tagalog)
    }

/**
 * Locales whose rule set is not their language's.
 *
 * There is exactly one: European Portuguese takes `one` for 1 alone, where Portuguese generally —
 * and Brazilian Portuguese, the AAR's `values-pt` and `values-pt-rBR` — takes `one` for 0 and 1.
 */
private val RegionalPluralRuleSets: Map<String, PluralRuleSet> =
    mapOf("pt-PT" to PluralRuleSet.OneForOne)
