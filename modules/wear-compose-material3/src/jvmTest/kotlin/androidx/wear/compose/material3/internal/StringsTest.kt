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

/**
 * The port's string resolution — the one piece of it that a rendered PNG cannot check, because
 * every string here is a content description that only a screen reader ever reads.
 */
class StringsTest {
    @Test
    fun `default locale falls back to the AAR's own values`() {
        assertEquals("Confirm", Strings.AlertDialogContentDescriptionConfirmButton.textIn(english))
    }

    @Test
    fun `a translated locale is used`() {
        assertEquals("Jour", Strings.DatePickerDay.textIn(Locale("fr")))
    }

    @Test
    fun `a region falls back to its language`() {
        // There is no values-fr-rCA in the AAR; Android resolves it to values-fr, and so does this.
        assertEquals(
            Strings.DatePickerDay.textIn(Locale("fr")),
            Strings.DatePickerDay.textIn(Locale("fr-CA")),
        )
    }

    @Test
    fun `a region-specific translation wins over its language`() {
        // pt and pt-BR are both shipped, and they differ — the region must not be dropped.
        assertEquals("Dia", Strings.DatePickerDay.textIn(Locale("pt-BR")))
    }

    @Test
    fun `an unknown locale falls back to the default`() {
        assertEquals(
            Strings.DatePickerDay.textIn(english),
            Strings.DatePickerDay.textIn(Locale("zz")),
        )
    }

    @Test
    fun `a missing resource yields its own name rather than throwing`() {
        assertEquals("no_such_string", Strings("no_such_string").textIn(english))
    }

    @Test
    fun `plurals pick one and other`() {
        val hours = Plurals.TimePickerHoursContentDescription
        assertEquals("%d Hour", hours.textIn(english, 1))
        assertEquals("%d Hours", hours.textIn(english, 2))
    }

    @Test
    fun `plurals are translated`() {
        // A NO-BREAK SPACE, not a space: French typography does not break between a number and its
        // unit, and the translation says so. Asserting the exact character is the point — a
        // pipeline that normalised whitespace would silently change how the text renders.
        assertEquals(
            "%d\u00A0heure",
            Plurals.TimePickerHoursContentDescription.textIn(Locale("fr"), 1),
        )
    }

    @Test
    fun `a plural category the translation lacks falls back to other`() {
        // French has no `two`; asking for 2 must not yield the resource name.
        assertEquals(
            "%d\u00A0heures",
            Plurals.TimePickerHoursContentDescription.textIn(Locale("fr"), 2),
        )
    }

    private val english = Locale("en-US")
}
