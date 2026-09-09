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
 * The wall-clock time, in the host's own time zone, split into the fields a clock face needs.
 *
 * This is the whole of what `TimeText` asked `java.util.Calendar` for. Deliberately not a date:
 * nothing in the ported surface formats one, and the components that would — the two pickers —
 * need a real date library rather than this.
 */
public class LocalTimeParts(
    /** 0..23. */
    public val hour: Int,
    /** 0..59. */
    public val minute: Int,
    /** 0..59. */
    public val second: Int,
) {
    /** 1..12, for the `h` patterns. */
    public val hour12: Int
        get() = if (hour % 12 == 0) 12 else hour % 12

    public val isAfternoon: Boolean
        get() = hour >= 12
}

/** Split [epochMillis] into local clock fields, in whatever time zone the host is in. */
public expect fun platformLocalTime(epochMillis: Long): LocalTimeParts

/**
 * The device's preferred time-of-day pattern — Android's
 * `DateFormat.getBestDateTimePattern(locale, "Hm" | "hm")`.
 *
 * TODO: the separator is assumed to be a colon. Android derives the whole pattern from the
 * locale's CLDR data, and a few locales do not use one (`H时mm分`). `Intl.DateTimeFormat`'s
 * `formatToParts` exposes the real literal on the web and could answer this properly; every
 * locale the catalog is rendered in today uses a colon, so it has not been worth the interop.
 */
public fun platformTimePattern(is24Hour: Boolean): String = if (is24Hour) "HH:mm" else "h:mm"

/**
 * Format [parts] with a `SimpleDateFormat`-style pattern, which is what `TimeText` documents its
 * `timeFormat` parameter as taking.
 *
 * Supports the time-of-day fields — `H HH h hh m mm s ss a` — and passes anything else through as
 * a literal, with `'` quoting a literal run as `SimpleDateFormat` does. Date fields are not
 * supported and are passed through unchanged rather than silently formatted wrongly; nothing in
 * the ported surface asks for one.
 */
public fun formatTime(parts: LocalTimeParts, pattern: String): String {
    val out = StringBuilder(pattern.length)
    var index = 0
    while (index < pattern.length) {
        val char = pattern[index]
        if (char == '\'') {
            // A quoted literal run. '' is an escaped apostrophe, quoted or not.
            index++
            while (index < pattern.length && pattern[index] != '\'') {
                out.append(pattern[index])
                index++
            }
            index++
            continue
        }
        if (char !in "HhmsaKk") {
            out.append(char)
            index++
            continue
        }
        var run = 0
        while (index + run < pattern.length && pattern[index + run] == char) run++
        when (char) {
            'H',
            'k' -> out.append(parts.hour.pad(run))
            'h',
            'K' -> out.append(parts.hour12.pad(run))
            'm' -> out.append(parts.minute.pad(run))
            's' -> out.append(parts.second.pad(run))
            'a' -> out.append(if (parts.isAfternoon) "PM" else "AM")
        }
        index += run
    }
    return out.toString()
}

private fun Int.pad(width: Int): String = toString().padStart(width, '0')
