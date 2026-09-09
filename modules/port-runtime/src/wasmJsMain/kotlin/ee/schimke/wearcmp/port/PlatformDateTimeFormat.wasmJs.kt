package ee.schimke.wearcmp.port

/**
 * `Intl.DateTimeFormat` is the browser's CLDR data — the same source Android's formatter reads
 * from, reached through a different door. Nothing here is a table this port maintains.
 */
public actual object PlatformDateTimeFormat {
    public actual fun yearHasLinguisticMarker(languageTag: String): Boolean =
        // `2022年` in Japanese, `2022` in English. Asking the platform to write a year on its own
        // and looking at what it wrote is the whole test.
        jsFormattedYear(languageTag).any { it.isLetter() }

    public actual fun monthNames(languageTag: String, style: MonthNameStyle): List<String> {
        val intlStyle =
            when (style) {
                MonthNameStyle.Numeric -> "2-digit"
                MonthNameStyle.Abbreviated -> "short"
                MonthNameStyle.Full -> "long"
            }
        return (1..12).map { month -> jsMonthName(languageTag, intlStyle, month) }
    }

    public actual fun dateFieldOrder(languageTag: String): String =
        // `formatToParts` reports the fields in the order the locale writes them, which is the
        // question, with no pattern string to parse in between.
        jsDateFieldOrder(languageTag).ifEmpty { "Mdy" }

    public actual fun formatNumber(languageTag: String, value: Int, minimumDigits: Int): String =
        jsFormatNumber(languageTag, value, minimumDigits)

    public actual fun amPmNames(languageTag: String): Pair<String, String> =
        jsDayPeriod(languageTag, 0) to jsDayPeriod(languageTag, 13)
}

private fun jsDayPeriod(tag: String, hour: Int): String =
    js(
        """(function () {
        var parts = new Intl.DateTimeFormat(tag || undefined, { hour: 'numeric', hour12: true })
            .formatToParts(new Date(2022, 0, 1, hour, 0));
        var period = parts.find(function (p) { return p.type === 'dayPeriod'; });
        return period ? period.value : (hour < 12 ? 'AM' : 'PM');
    })()"""
    )

private fun jsDateFieldOrder(tag: String): String =
    js(
        """(function () {
        var parts = new Intl.DateTimeFormat(tag || undefined).formatToParts(new Date(2022, 0, 2));
        var letters = { year: 'y', month: 'M', day: 'd' };
        return parts.map(function (p) { return letters[p.type] || ''; }).join('');
    })()"""
    )

private fun jsFormatNumber(tag: String, value: Int, minimumDigits: Int): String =
    js(
        "new Intl.NumberFormat(tag || undefined, { minimumIntegerDigits: minimumDigits, useGrouping: false }).format(value)"
    )

private fun jsFormattedYear(tag: String): String =
    js(
        "new Intl.DateTimeFormat(tag || undefined, { year: 'numeric' }).format(new Date(2022, 0, 1))"
    )

private fun jsMonthName(tag: String, style: String, month: Int): String =
    js(
        "new Intl.DateTimeFormat(tag || undefined, { month: style }).format(new Date(2022, month - 1, 1))"
    )
