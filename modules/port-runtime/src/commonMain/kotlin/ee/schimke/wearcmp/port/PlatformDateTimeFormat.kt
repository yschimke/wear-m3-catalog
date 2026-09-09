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

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** How a month is written: `MM`, `MMM` and `MMMM` in an Android date pattern. */
public enum class MonthNameStyle {
    Numeric,
    Abbreviated,
    Full,
}

/**
 * The two locale questions the date picker asks, which no multiplatform date library answers.
 *
 * `kotlinx-datetime` deliberately carries no locale data — it does arithmetic, not presentation —
 * so month names and locale conventions have to come from the platform: `java.time` on the JVM,
 * `Intl` in the browser. Both have the data; only the way in differs.
 *
 * Stated as the QUESTIONS the picker asks rather than as Android's
 * `DateFormat.getBestDateTimePattern`, which is what it asks them through. A pattern is Android's
 * way of encoding an answer, and reproducing that encoding on two platforms that do not use it
 * would be work in service of nothing.
 */
public expect object PlatformDateTimeFormat {
    /**
     * Whether this locale writes a year with a linguistic marker after it — `2022年`, `2022년`,
     * `2022 г.`.
     *
     * The date picker uses it to decide between a named and a numeric month: a locale that marks
     * its year is one whose months read better as numbers.
     */
    public fun yearHasLinguisticMarker(languageTag: String): Boolean

    /** The twelve month names of this locale, January first. */
    public fun monthNames(languageTag: String, style: MonthNameStyle): List<String>

    /**
     * The order this locale writes a date in, as the letters `y`, `M` and `d` — Android's
     * `DateFormat.getDateFormatOrder`. `Mdy` in the United States, `dMy` in most of Europe, `yMd`
     * in Japan. The picker orders its three columns by it.
     */
    public fun dateFieldOrder(languageTag: String): String

    /**
     * [value] written in this locale's own digits, padded to at least [minimumDigits].
     *
     * Not cosmetic: several locales do not write numbers with the Arabic numerals this file is
     * typed in, and a picker showing `05` to someone whose clock reads `٠٥` has switched writing
     * system mid-screen.
     */
    public fun formatNumber(languageTag: String, value: Int, minimumDigits: Int = 1): String

    /**
     * What this locale writes for the two halves of the day — `AM`/`PM`, `午前`/`午後`, `ص`/`م`.
     * The time picker shows them as the options of its period column.
     */
    public fun amPmNames(languageTag: String): Pair<String, String>
}

/**
 * `java.time.LocalDate.lengthOfMonth()`, which `kotlinx-datetime` has no direct equivalent of.
 *
 * Adding a month and stepping back a day lands on the last day of the original month, whatever its
 * length and whether the year is a leap year — so this is arithmetic rather than a table.
 */
public fun LocalDate.lengthOfMonth(): Int =
    LocalDate(year, month, 1).plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day
