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
 * The date and time types `DatePicker` and `TimePicker` take, as an `expect class` each so that
 * **the JVM keeps upstream's exact signature**.
 *
 * `DatePicker(initialDate: java.time.LocalDate, …)` is the published Wear Compose API. `java.time`
 * does not exist on wasm, so the port cannot write that type in common code — but it must not
 * change it either, or a JVM/Android caller of the real AndroidX library could no longer compile
 * against this one. An `expect class` actualised by a `typealias` per target resolves that: the
 * declaration is abstract in common, and on the JVM it *is* `java.time.LocalDate`, both in source
 * and in the compiled signature. `kotlinx-datetime` supplies the wasm actual, where nothing has a
 * prior API to be compatible with.
 *
 * The cost of a typealias actual is that common code sees a type with no members — a typealias
 * cannot introduce them, and an `expect class` that declared `val year: Int` would not match
 * `java.time.LocalDate`'s Java getter. So every field the pickers read is an `expect` extension
 * here, prefixed `port` to keep it clear of the members both platform types already have (which
 * would otherwise shadow it, since a member always wins over an extension). `transform-rules.json`
 * rewrites upstream's `.year` / `.monthValue` / `.dayOfMonth` / `.hour` / `.minute` / `.second`
 * onto them.
 */
public expect class LocalDate

/** The time half of the seam; see [LocalDate]. On the JVM this is `java.time.LocalTime`. */
public expect class LocalTime

/** `java.time.LocalDate.of(year, month, day)`; [month] and [day] are 1-based. */
public expect fun portLocalDate(year: Int, month: Int, day: Int): LocalDate

/** `java.time.LocalTime.of(hour, minute, second)`, on a 24-hour clock. */
public expect fun portLocalTime(hour: Int, minute: Int, second: Int): LocalTime

/** The proleptic year. */
public expect val LocalDate.portYear: Int

/** The month, 1..12. */
public expect val LocalDate.portMonthNumber: Int

/** The day of the month, 1..31. */
public expect val LocalDate.portDayOfMonth: Int

/**
 * The number of days in this date's month, leap years included.
 *
 * `java.time.LocalDate.lengthOfMonth()` on the JVM. `kotlinx-datetime` has no equivalent, so the
 * wasm actual does the arithmetic: adding a month and stepping back a day lands on the last day of
 * the original month, whatever its length.
 */
public expect fun LocalDate.portLengthOfMonth(): Int

/**
 * Chronological order.
 *
 * Both platform types have a `compareTo` member, but neither is visible through the memberless
 * `expect class`, and `java.time.LocalDate` implements `Comparable<ChronoLocalDate>` rather than
 * `Comparable<LocalDate>` — so this is an operator extension rather than a supertype on the `expect
 * class`, which the JVM actual could not satisfy.
 */
public expect operator fun LocalDate.compareTo(other: LocalDate): Int

/** The hour of the day, 0..23. */
public expect val LocalTime.portHour: Int

/** The minute of the hour, 0..59. */
public expect val LocalTime.portMinute: Int

/** The second of the minute, 0..59. */
public expect val LocalTime.portSecond: Int
