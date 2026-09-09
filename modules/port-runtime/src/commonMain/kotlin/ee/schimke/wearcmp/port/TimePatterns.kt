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

/**
 * What each locale's clock actually looks like: `TimePicker`'s field order, separators and
 * day-period position.
 *
 * Upstream passes a skeleton — `h:mm a`, `H:mm:ss` — through Android's
 * `DateFormat.getBestDateTimePattern(locale, skeleton)`, which reorders the fields and swaps the
 * separators for the locales that need it. The port had no such call and used the skeleton
 * verbatim, so every locale got American field order with a colon.
 *
 * That is more wrong than it sounds. Hungarian and Chinese write the day-period marker BEFORE the
 * hour; Finnish separates with a full stop, not a colon; Japanese counts its 12-hour clock with
 * `K` (0..11) rather than `h` (1..12); French Canadian writes `HH 'h' mm`. None of that is
 * guessable, which is the argument for a table over a heuristic.
 *
 * ## Where this comes from
 *
 * `getBestDateTimePattern` is ICU's `DateTimePatternGenerator`, so these ARE Android's answers,
 * generated once from ICU4J 77.1 rather than approximated. The port already ships 86 locales of
 * CLDR string data; 4 KB of CLDR pattern data alongside it is the same trade, and it makes both
 * targets agree exactly instead of leaving the JVM to reconstruct what `Intl` can answer directly.
 *
 * To regenerate — after a locale is added, or a CLDR release moves a pattern:
 * ```
 * curl -sSLO https://repo1.maven.org/maven2/com/ibm/icu/icu4j/77.1/icu4j-77.1.jar
 * // for each tag, for each skeleton below:
 * //   DateTimePatternGenerator.getInstance(ULocale(tag)).getBestPattern(icuSkeleton)
 * // where h:mm a -> hmm, H:mm -> Hmm, H:mm:ss -> Hmmss, mm:ss -> mmss
 * ```
 * Run the JVM with `-Dstdout.encoding=UTF-8`: 109 of these patterns contain a NARROW NO-BREAK
 * SPACE (U+202F) before the marker, and a default POSIX locale silently writes it as `?`.
 *
 * The locale list is the one `GeneratedLocalizedPlurals` ships, plus `en` for the default.
 */

/**
 * The pattern [skeleton] takes in [languageTag], or [skeleton] itself for a locale not in the
 * table.
 *
 * The fallback is deliberate rather than a gap: an unknown locale gets exactly what the port
 * produced for every locale before this, so nothing regresses. Region is tried before language —
 * `pt-BR` before `pt` — the same two steps the string tables resolve with.
 */
public fun timeFieldPattern(languageTag: String, skeleton: String): String {
    GeneratedTimePatterns[languageTag]?.get(skeleton)?.let {
        return it
    }
    val language = languageTag.substringBefore('-')
    return GeneratedTimePatterns[language]?.get(skeleton) ?: skeleton
}

/**
 * The locale tags this table covers.
 *
 * Public because "is this locale covered?" cannot be answered from [timeFieldPattern] alone: a
 * locale whose pattern happens to equal the skeleton is indistinguishable from one that fell
 * through, and twenty of the shipped locales write `h:mm a` exactly.
 */
public val TimePatternLocales: Set<String>
    get() = GeneratedTimePatterns.keys

/**
 * Locale tag to skeleton to pattern. Generated; see the note above before editing by hand.
 *
 * `internal` rather than private so `TimePatternsTest` can sweep every entry for the invariants
 * `PickerLocaleConfig` reads them under.
 */
internal val GeneratedTimePatterns: Map<String, Map<String, String>> =
    mapOf(
        "af" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "am" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ar" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "as" to
            mapOf(
                "h:mm a" to "a h.mm",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "az" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "be" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm.ss",
            ),
        "bg" to
            mapOf(
                "h:mm a" to "h:mm '\u0447'. a",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "m:ss",
            ),
        "bn" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "bs" to
            mapOf(
                "h:mm a" to "hh:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ca" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "cs" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "da" to
            mapOf(
                "h:mm a" to "h.mm\u202Fa",
                "H:mm" to "HH.mm",
                "H:mm:ss" to "HH.mm.ss",
                "mm:ss" to "mm.ss",
            ),
        "de" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "el" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "en-AU" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "en-CA" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "en-GB" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "en-IN" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "en-XC" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "es-US" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "es" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "et" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "eu" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "fa" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "m:ss",
            ),
        "fi" to
            mapOf(
                "h:mm a" to "h.mm\u202Fa",
                "H:mm" to "H.mm",
                "H:mm:ss" to "H.mm.ss",
                "mm:ss" to "m.ss",
            ),
        "fr-CA" to
            mapOf(
                "h:mm a" to "h 'h' mm\u202Fa",
                "H:mm" to "HH 'h' mm",
                "H:mm:ss" to "HH 'h' mm 'min' ss 's'",
                "mm:ss" to "mm 'min' ss 's'",
            ),
        "fr" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "gl" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "gu" to
            mapOf(
                "h:mm a" to "hh:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "hi" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "hr" to
            mapOf(
                "h:mm a" to "hh:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "hu" to
            mapOf(
                "h:mm a" to "a\u202Fh:mm",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "hy" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "in" to
            mapOf(
                "h:mm a" to "h.mm\u202Fa",
                "H:mm" to "HH.mm",
                "H:mm:ss" to "HH.mm.ss",
                "mm:ss" to "mm.ss",
            ),
        "is" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "it" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "iw" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ja" to
            mapOf(
                "h:mm a" to "aK:mm",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ka" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "kk" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "km" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "kn" to
            mapOf(
                "h:mm a" to "hh:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ko" to
            mapOf(
                "h:mm a" to "a h:mm",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ky" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "lo" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "lt" to
            mapOf(
                "h:mm a" to "hh:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "lv" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "mk" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ml" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "mn" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "mr" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ms" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "my" to
            mapOf(
                "h:mm a" to "a h:mm",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "nb" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ne" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "nl" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "or" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "pa" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "pl" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "pt-BR" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "pt-PT" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "pt" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ro" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ru" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "si" to
            mapOf(
                "h:mm a" to "a h.mm",
                "H:mm" to "HH.mm",
                "H:mm:ss" to "HH.mm.ss",
                "mm:ss" to "mm.ss",
            ),
        "sk" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "H:mm",
                "H:mm:ss" to "H:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "sl" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "sq" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "sr-Latn" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "sr" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "sv" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "sw" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ta" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "te" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "th" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "tl" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "tr" to
            mapOf(
                "h:mm a" to "a\u202Fh:mm",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "uk" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "ur" to
            mapOf(
                "h:mm a" to "h:mm a",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "uz" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "vi" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "zh-CN" to
            mapOf(
                "h:mm a" to "ah:mm",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "zh-HK" to
            mapOf(
                "h:mm a" to "ah:mm",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "zh-TW" to
            mapOf(
                "h:mm a" to "ah:mm",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "zu" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
        "en" to
            mapOf(
                "h:mm a" to "h:mm\u202Fa",
                "H:mm" to "HH:mm",
                "H:mm:ss" to "HH:mm:ss",
                "mm:ss" to "mm:ss",
            ),
    )
