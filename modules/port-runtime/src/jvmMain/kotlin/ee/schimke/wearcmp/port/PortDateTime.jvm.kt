/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ee.schimke.wearcmp.port

/**
 * The JVM half of the picker date seam: upstream's own types, unaliased.
 *
 * A caller that compiles against the real `androidx.wear.compose.material3` compiles against this
 * one unchanged — `DatePicker(initialDate = LocalDate.now(), …)` with `java.time.LocalDate` in
 * scope resolves here, and the compiled parameter type is `java.time.LocalDate` too.
 */
public actual typealias LocalDate = java.time.LocalDate

/** See [LocalDate]. */
public actual typealias LocalTime = java.time.LocalTime

public actual fun portLocalDate(year: Int, month: Int, day: Int): LocalDate =
    java.time.LocalDate.of(year, month, day)

public actual fun portLocalTime(hour: Int, minute: Int, second: Int): LocalTime =
    java.time.LocalTime.of(hour, minute, second)

public actual val LocalDate.portYear: Int
    get() = year

public actual val LocalDate.portMonthNumber: Int
    get() = monthValue

public actual val LocalDate.portDayOfMonth: Int
    get() = dayOfMonth

public actual fun LocalDate.portLengthOfMonth(): Int = lengthOfMonth()

// Resolves to `java.time.LocalDate.compareTo(ChronoLocalDate)`, the member — a member always wins
// over an extension, so this is a delegation and not a recursion.
public actual operator fun LocalDate.compareTo(other: LocalDate): Int = compareTo(other)

public actual val LocalTime.portHour: Int
    get() = hour

public actual val LocalTime.portMinute: Int
    get() = minute

public actual val LocalTime.portSecond: Int
    get() = second
