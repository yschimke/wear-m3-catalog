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

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * The wasm half of the picker date seam.
 *
 * `java.time` does not exist here and there is no prior API to stay compatible with, so the port
 * takes `kotlinx-datetime` — the multiplatform date library, and the one a Wasm consumer is already
 * likely to be using.
 */
public actual typealias LocalDate = kotlinx.datetime.LocalDate

/** See [LocalDate]. */
public actual typealias LocalTime = kotlinx.datetime.LocalTime

public actual fun portLocalDate(year: Int, month: Int, day: Int): LocalDate =
    kotlinx.datetime.LocalDate(year, month, day)

public actual fun portLocalTime(hour: Int, minute: Int, second: Int): LocalTime =
    kotlinx.datetime.LocalTime(hour, minute, second)

public actual val LocalDate.portYear: Int
    get() = year

public actual val LocalDate.portMonthNumber: Int
    get() = month.number

public actual val LocalDate.portDayOfMonth: Int
    get() = day

public actual fun LocalDate.portLengthOfMonth(): Int =
    kotlinx.datetime.LocalDate(year, month, 1)
        .plus(DatePeriod(months = 1))
        .minus(DatePeriod(days = 1))
        .day

public actual operator fun LocalDate.compareTo(other: LocalDate): Int = compareTo(other)

public actual val LocalTime.portHour: Int
    get() = hour

public actual val LocalTime.portMinute: Int
    get() = minute

public actual val LocalTime.portSecond: Int
    get() = second
