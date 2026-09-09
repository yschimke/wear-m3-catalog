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

/**
 * The JVM actuals of the picker date seam.
 *
 * The accessors exist because the two platform libraries disagree about names and, in one case,
 * about base: `kotlinx-datetime` counts months through `month.number` where `java.time` has
 * `monthValue`, and getting that mapping wrong is an off-by-one that a compiling build would not
 * catch. `lengthOfMonth` is arithmetic on wasm and a member here, so it is worth pinning on both.
 */
class PortDateTimeTest {

    @Test
    fun theJvmActualIsJavaTime() {
        val date: Any = portLocalDate(2024, 2, 29)
        val time: Any = portLocalTime(18, 54, 7)
        assertTrue(date is java.time.LocalDate, "portLocalDate returned ${date::class}")
        assertTrue(time is java.time.LocalTime, "portLocalTime returned ${time::class}")
    }

    @Test
    fun dateFieldsAreOneBased() {
        val date = portLocalDate(2024, 2, 29)
        assertEquals(2024, date.portYear)
        assertEquals(2, date.portMonthNumber, "February is 2, not 1")
        assertEquals(29, date.portDayOfMonth)
    }

    @Test
    fun timeFieldsAreOnATwentyFourHourClock() {
        val time = portLocalTime(18, 54, 7)
        assertEquals(18, time.portHour)
        assertEquals(54, time.portMinute)
        assertEquals(7, time.portSecond)
    }

    @Test
    fun lengthOfMonthKnowsAboutLeapYears() {
        assertEquals(29, portLocalDate(2024, 2, 1).portLengthOfMonth())
        assertEquals(28, portLocalDate(2023, 2, 1).portLengthOfMonth())
        assertEquals(31, portLocalDate(2023, 12, 31).portLengthOfMonth())
        assertEquals(30, portLocalDate(2023, 4, 15).portLengthOfMonth())
        // 1900 is divisible by 4 but not a leap year; 2000 is.
        assertEquals(28, portLocalDate(1900, 2, 1).portLengthOfMonth())
        assertEquals(29, portLocalDate(2000, 2, 1).portLengthOfMonth())
    }

    @Test
    fun datesOrderChronologically() {
        val early = portLocalDate(2023, 12, 31)
        val late = portLocalDate(2024, 1, 1)
        assertTrue(early < late)
        assertTrue(late > early)
        assertTrue(early <= portLocalDate(2023, 12, 31))
        assertEquals(0, early.compareTo(portLocalDate(2023, 12, 31)))
    }
}
