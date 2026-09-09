package ee.schimke.wearcmp.port

import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * `java.time` has the same CLDR data Android's own formatter reads, reached through a `Locale`.
 */
public actual object PlatformDateTimeFormat {
    public actual fun yearHasLinguisticMarker(languageTag: String): Boolean {
        // Format a year on its own and look at what came out beside the digits. A locale that
        // marks its year puts a letter there; one that does not writes the digits alone.
        val formatted =
            runCatching {
                    DateTimeFormatter.ofPattern("y", Locale.forLanguageTag(languageTag))
                        .format(java.time.LocalDate.of(2022, 1, 1))
                }
                .getOrDefault("2022")
        return formatted.any { it.isLetter() }
    }

    public actual fun monthNames(languageTag: String, style: MonthNameStyle): List<String> {
        val locale = Locale.forLanguageTag(languageTag)
        return (1..12).map { month ->
            when (style) {
                MonthNameStyle.Numeric -> month.toString().padStart(2, '0')
                MonthNameStyle.Abbreviated ->
                    java.time.Month.of(month).getDisplayName(TextStyle.SHORT, locale)
                MonthNameStyle.Full ->
                    java.time.Month.of(month).getDisplayName(TextStyle.FULL, locale)
            }
        }
    }

    public actual fun dateFieldOrder(languageTag: String): String {
        val pattern =
            runCatching {
                    java.time.format.DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                        java.time.format.FormatStyle.SHORT,
                        null,
                        java.time.chrono.IsoChronology.INSTANCE,
                        Locale.forLanguageTag(languageTag),
                    )
                }
                .getOrDefault("M/d/y")
        // The pattern spells each field with a repeated letter (`dd.MM.y`); the ORDER is the
        // sequence of distinct field letters in it, which is what the picker needs.
        return pattern.filter { it in "yMd" }.fold(StringBuilder()) { order, letter ->
            if (order.isEmpty() || order.last() != letter) order.append(letter) else order
        }.toString()
    }

    public actual fun formatNumber(languageTag: String, value: Int, minimumDigits: Int): String =
        String.format(Locale.forLanguageTag(languageTag), "%0${minimumDigits}d", value)

    public actual fun amPmNames(languageTag: String): Pair<String, String> {
        val formatter = DateTimeFormatter.ofPattern("a", Locale.forLanguageTag(languageTag))
        return runCatching {
                formatter.format(java.time.LocalTime.of(0, 0)) to
                    formatter.format(java.time.LocalTime.of(12, 0))
            }
            .getOrDefault("AM" to "PM")
    }
}
