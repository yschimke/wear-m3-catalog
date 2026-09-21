package androidx.compose.remote.creation.compose.state

/** Deterministic, platform-neutral number formatting options for remote text. */
public class DecimalFormat {
    public var decimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols()
    public var isGroupingUsed: Boolean = true
    public var groupingSize: Int = 3
    public var minimumIntegerDigits: Int = 1
    public var maximumIntegerDigits: Int = 309
    public var minimumFractionDigits: Int = 0
    public var maximumFractionDigits: Int = 3
    public var negativePrefix: String = "-"
    public var roundingMode: RoundingMode = RoundingMode.HALF_EVEN
}

public data class DecimalFormatSymbols(
    public val decimalSeparator: Char = '.',
    public val groupingSeparator: Char = ',',
)

public enum class RoundingMode {
    UNNECESSARY,
    HALF_EVEN,
}
