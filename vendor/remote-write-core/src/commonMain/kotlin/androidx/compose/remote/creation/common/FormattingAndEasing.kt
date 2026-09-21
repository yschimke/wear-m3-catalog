package androidx.compose.remote.creation.common

import kotlin.math.roundToInt

public object StringUtils {
  public const val GROUPING_NONE: Byte = 0
  public const val GROUPING_BY3: Byte = 1
  public const val GROUPING_BY4: Byte = 2
  public const val GROUPING_BY32: Byte = 3
  public const val SEPARATOR_COMMA_PERIOD: Byte = 0
  public const val SEPARATOR_PERIOD_COMMA: Byte = 1
  public const val SEPARATOR_SPACE_COMMA: Byte = 2
  public const val SEPARATOR_UNDER_PERIOD: Byte = 3

  public fun floatToString(
    value: Float,
    beforeDecimalPoint: Int,
    afterDecimalPoint: Int,
    pre: Char,
    post: Char,
    separator: Byte,
    grouping: Byte,
    options: Int,
  ): String {
    val negative = value < 0f
    val absolute = kotlin.math.abs(value)
    val scale = pow10(afterDecimalPoint)
    val scaled = if ((options and 2) != 0) (absolute * scale).roundToInt() else (absolute * scale).toInt()
    var integer = (scaled / scale.coerceAtLeast(1)).toString()
    val groupSeparator = when (separator) {
      SEPARATOR_PERIOD_COMMA -> '.'
      SEPARATOR_SPACE_COMMA -> ' '
      SEPARATOR_UNDER_PERIOD -> '_'
      else -> ','
    }
    val groupSize = when (grouping) { GROUPING_BY3 -> 3; GROUPING_BY4 -> 4; else -> 0 }
    if (groupSize > 0) integer = integer.reversed().chunked(groupSize).joinToString(groupSeparator.toString()).reversed()
    if (integer.length < beforeDecimalPoint && pre.code != 0) integer = pre.toString().repeat(beforeDecimalPoint - integer.length) + integer
    else if (integer.length > beforeDecimalPoint) integer = integer.takeLast(beforeDecimalPoint)
    val decimalSeparator = if (separator == SEPARATOR_COMMA_PERIOD || separator == SEPARATOR_UNDER_PERIOD) '.' else ','
    var fraction = if (afterDecimalPoint == 0) "" else (scaled % scale).toString().padStart(afterDecimalPoint, '0')
    if (post.code == 0) fraction = fraction.trimEnd('0')
    else if (post != '0') fraction = fraction.trimEnd('0').padEnd(afterDecimalPoint, post)
    val sign = if (!negative) "" else if ((options and 1) != 0) "(" else "-"
    val suffix = if (negative && (options and 1) != 0) ")" else ""
    return sign + integer + (if (afterDecimalPoint == 0) "" else "$decimalSeparator$fraction") + suffix
  }

  private fun pow10(power: Int): Int {
    var result = 1
    repeat(power.coerceIn(0, 9)) { result *= 10 }
    return result
  }
}

public object GeneralEasing {
  public const val CUBIC_STANDARD: Int = 1
}

public class CubicEasing {
  private var x1 = 0.4f
  private var y1 = 0f
  private var x2 = 0.2f
  private var y2 = 1f

  public fun setup(x1: Float, y1: Float, x2: Float, y2: Float) {
    this.x1 = x1
    this.y1 = y1
    this.x2 = x2
    this.y2 = y2
  }

  public fun get(fraction: Float): Float {
    val target = fraction.coerceIn(0f, 1f)
    var low = 0f
    var high = 1f
    repeat(14) {
      val t = (low + high) / 2f
      if (bezier(t, x1, x2) < target) low = t else high = t
    }
    return bezier((low + high) / 2f, y1, y2)
  }

  private fun bezier(t: Float, first: Float, second: Float): Float {
    val inverse = 1f - t
    return 3f * inverse * inverse * t * first + 3f * inverse * t * t * second + t * t * t
  }
}

public class MonotonicSpline(unused: FloatArray?, private val values: FloatArray) {
  public fun getPos(position: Float): Float {
    if (values.isEmpty()) return Float.NaN
    if (values.size == 1) return values[0]
    val scaled = position.coerceIn(0f, 1f) * (values.size - 1)
    val index = scaled.toInt().coerceAtMost(values.lastIndex - 1)
    val fraction = scaled - index
    return values[index] * (1f - fraction) + values[index + 1] * fraction
  }
}
