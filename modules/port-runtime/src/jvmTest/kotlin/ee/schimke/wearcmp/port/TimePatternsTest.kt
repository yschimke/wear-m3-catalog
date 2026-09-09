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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [timeFieldPattern] — the table, and the invariants `TimePicker` reads it under. */
class TimePatternsTest {

    /**
     * The cases that motivated the table, each one something a heuristic would have got wrong.
     *
     * The ` ` is a NARROW NO-BREAK SPACE, which is what CLDR puts before a day-period marker.
     */
    @Test
    fun thePatternsAreTheLocalesOwn() {
        // Marker BEFORE the hour — and not only in the languages one would guess. Hungarian does
        // it too, which is the reason this is a table and not a rule about CJK.
        assertEquals("ah:mm", timeFieldPattern("zh-CN", "h:mm a"))
        assertEquals("a h:mm", timeFieldPattern("hu", "h:mm a"))
        assertEquals("a h:mm", timeFieldPattern("ko", "h:mm a"))
        // Japanese counts a 12-hour clock with K (0..11), not h (1..12).
        assertEquals("aK:mm", timeFieldPattern("ja", "h:mm a"))
        // Finnish separates with a full stop.
        assertEquals("H.mm", timeFieldPattern("fi", "H:mm"))
        // French Canadian writes the hour marker as a quoted literal.
        assertEquals("HH 'h' mm", timeFieldPattern("fr-CA", "H:mm"))
        // ... and English still looks like English.
        assertEquals("h:mm a", timeFieldPattern("en-GB", "h:mm a"))
        assertEquals("HH:mm", timeFieldPattern("en-GB", "H:mm"))
    }

    @Test
    fun anUnknownLocaleKeepsTheSkeleton() {
        // Exactly what the port produced for every locale before the table existed, so a locale
        // outside it is no worse off than before.
        assertEquals("h:mm a", timeFieldPattern("qqq", "h:mm a"))
        assertEquals("mm:ss", timeFieldPattern("qqq-ZZ", "mm:ss"))
    }

    @Test
    fun regionFallsBackToLanguage() {
        // de-AT is not in the table; de is.
        assertEquals(timeFieldPattern("de", "H:mm"), timeFieldPattern("de-AT", "H:mm"))
        // pt-BR IS in the table in its own right, and differs from nothing here — but the lookup
        // must prefer it rather than silently reaching pt.
        assertEquals("HH:mm", timeFieldPattern("pt-BR", "H:mm"))
    }

    /**
     * The invariants `PickerLocaleConfig` reads these patterns under, checked across every entry.
     *
     * `hourValueOffset` decides 0- vs 1-based hours with `pattern.contains('H') || contains('K')`
     * against the RAW pattern, while `parsePattern` strips quoted literals first. A quoted literal
     * containing an hour letter would make those two disagree — fr-CA's `HH 'h' mm` is exactly the
     * shape that could — so this asserts they never do.
     */
    @Test
    fun everyPatternHoldsThePickersInvariants() {
        val problems = mutableListOf<String>()
        for ((tag, patterns) in GeneratedTimePatterns) {
            for (skeleton in patterns.keys) {
                val pattern = timeFieldPattern(tag, skeleton)
                val stripped = pattern.replace(Regex("\\s*'.*?'\\s*"), "")
                val is12Hour = skeleton.startswith12Hour()

                if (is12Hour && 'a' !in stripped) {
                    problems += "$tag/$skeleton: 12-hour pattern '$pattern' has no period field"
                }
                if (!is12Hour && skeleton != "mm:ss" && 'a' in stripped) {
                    problems += "$tag/$skeleton: 24-hour pattern '$pattern' has a period field"
                }
                val zeroBasedRaw = 'H' in pattern || 'K' in pattern
                val zeroBasedStripped = 'H' in stripped || 'K' in stripped
                if (zeroBasedRaw != zeroBasedStripped) {
                    problems += "$tag/$skeleton: a quoted literal in '$pattern' changes hourValueOffset"
                }
                if (skeleton != "mm:ss" && stripped.none { it in "hHkK" }) {
                    problems += "$tag/$skeleton: '$pattern' has no hour field"
                }
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    /** The 12-hour skeletons are the ones spelled with a lower-case `h`. */
    private fun String.startswith12Hour() = startsWith("h")
}
