package androidx.compose.remote.creation.common

import kotlin.math.pow

/** Protocol-level ID and colour helpers shared by every creation target. */
public object Utils {
  public fun asNan(value: Int): Float = Float.fromBits(value or -0x800000)

  public fun idFromNan(value: Float): Int = value.toRawBits() and 0x3fffff

  public fun longIdFromNan(value: Float): Long = idFromNan(value).toLong() + 0x100000000L

  public fun idFromLong(value: Long): Long = value - 0x100000000L

  public fun idStringFromNan(value: Float): String = idString(idFromNan(value))

  public fun idString(value: Int): String =
    if (value > 0xfffff) "A_${value and 0xfffff}" else value.toString()

  public fun isVariable(value: Float): Boolean {
    if (!value.isNaN()) return false
    val id = idFromNan(value)
    return id != 0 && (id > 40 || id < 10)
  }

  public fun interpolateColor(from: Int, to: Int, fraction: Float): Int {
    if (fraction.isNaN() || fraction == 0f) return from
    if (fraction == 1f) return to
    fun channel(color: Int, shift: Int): Float = ((color ushr shift) and 0xff) / 255f
    fun linear(value: Float): Float = value.pow(2.2f)
    fun encoded(value: Float): Int = (value.coerceIn(0f, 1f).pow(1f / 2.2f) * 255f).toInt()
    val alpha = ((channel(from, 24) * (1f - fraction) + channel(to, 24) * fraction) * 255f).toInt()
    val red = encoded(linear(channel(from, 16)) * (1f - fraction) + linear(channel(to, 16)) * fraction)
    val green = encoded(linear(channel(from, 8)) * (1f - fraction) + linear(channel(to, 8)) * fraction)
    val blue = encoded(linear(channel(from, 0)) * (1f - fraction) + linear(channel(to, 0)) * fraction)
    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
  }

  public fun getHue(color: Int): Float = rgbToHsv(color)[0]

  public fun getSaturation(color: Int): Float = rgbToHsv(color)[1]

  public fun getBrightness(color: Int): Float = rgbToHsv(color)[2]

  public fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
    val h = ((hue % 1f) + 1f) % 1f * 6f
    val sector = h.toInt()
    val f = h - sector
    val p = value * (1f - saturation)
    val q = value * (1f - saturation * f)
    val t = value * (1f - saturation * (1f - f))
    val (r, g, b) =
      when (sector) {
        0 -> Triple(value, t, p)
        1 -> Triple(q, value, p)
        2 -> Triple(p, value, t)
        3 -> Triple(p, q, value)
        4 -> Triple(t, p, value)
        else -> Triple(value, p, q)
      }
    return ((r * 255f).toInt() shl 16) or ((g * 255f).toInt() shl 8) or (b * 255f).toInt()
  }

  private fun rgbToHsv(color: Int): FloatArray {
    val r = ((color ushr 16) and 0xff) / 255f
    val g = ((color ushr 8) and 0xff) / 255f
    val b = (color and 0xff) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val hue =
      when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * ((b - r) / delta + 2f)
        else -> 60f * ((r - g) / delta + 4f)
      }
    return floatArrayOf(
      (if (hue < 0f) hue + 360f else hue) / 360f,
      if (max == 0f) 0f else delta / max,
      max,
    )
  }
}

public object ColumnLayout {
  public const val START: Int = 1
  public const val CENTER: Int = 2
  public const val END: Int = 3
  public const val TOP: Int = 4
  public const val BOTTOM: Int = 5
  public const val SPACE_BETWEEN: Int = 6
  public const val SPACE_EVENLY: Int = 7
  public const val SPACE_AROUND: Int = 8
}

public class AnimatedFloatExpression {
  public companion object {
    public const val OFFSET: Int = 0x310000
    public val ADD: Float = op(1)
    public val SUB: Float = op(2)
    public val MUL: Float = op(3)
    public val DIV: Float = op(4)
    public val MOD: Float = op(5)
    public val MIN: Float = op(6)
    public val MAX: Float = op(7)
    public val POW: Float = op(8)
    public val SQRT: Float = op(9)
    public val ABS: Float = op(10)
    public val SIGN: Float = op(11)
    public val COPY_SIGN: Float = op(12)
    public val EXP: Float = op(13)
    public val FLOOR: Float = op(14)
    public val LOG: Float = op(15)
    public val LN: Float = op(16)
    public val ROUND: Float = op(17)
    public val SIN: Float = op(18)
    public val COS: Float = op(19)
    public val TAN: Float = op(20)
    public val ASIN: Float = op(21)
    public val ACOS: Float = op(22)
    public val ATAN: Float = op(23)
    public val ATAN2: Float = op(24)
    public val MAD: Float = op(25)
    public val IFELSE: Float = op(26)
    public val CLAMP: Float = op(27)
    public val CBRT: Float = op(28)
    public val DEG: Float = op(29)
    public val RAD: Float = op(30)
    public val CEIL: Float = op(31)
    public val A_DEREF: Float = op(32)
    public val A_MAX: Float = op(33)
    public val A_MIN: Float = op(34)
    public val A_SUM: Float = op(35)
    public val A_AVG: Float = op(36)
    public val A_LEN: Float = op(37)
    public val A_SPLINE: Float = op(38)
    public val RAND: Float = op(39)
    public val RAND_IN_RANGE: Float = op(42)
    public val LERP: Float = op(49)
    public val VAR1: Float = op(70)
    public val VAR2: Float = op(71)
    public val VAR3: Float = op(72)
    public val CHANGE_SIGN: Float = op(73)
    public val CUBIC: Float = op(74)
    public val A_SPLINE_LOOP: Float = op(75)
    public val A_SUM_TILL: Float = op(76)
    public val A_SUM_XY: Float = op(77)
    public val A_SUM_SQR: Float = op(78)
    public val A_LERP: Float = op(79)

    private fun op(offset: Int): Float = Utils.asNan(OFFSET + offset)

    public fun isMathOperator(value: Float): Boolean =
      value.isNaN() && Utils.idFromNan(value) in (OFFSET + 1)..(OFFSET + 79)

    public fun toString(values: FloatArray, labels: Array<String?>?): String =
      values.mapIndexed { index, value -> labels?.getOrNull(index) ?: Utils.idStringFromNan(value) }
        .joinToString(prefix = "[", postfix = "]")
  }
}

public object IntegerExpressionEvaluator {
  public const val OFFSET: Int = 0x10000
  public const val I_ADD: Int = OFFSET + 1
  public const val I_SUB: Int = OFFSET + 2
  public const val I_MUL: Int = OFFSET + 3
  public const val I_DIV: Int = OFFSET + 4
  public const val I_MOD: Int = OFFSET + 5
  public const val I_SHL: Int = OFFSET + 6
  public const val I_SHR: Int = OFFSET + 7
  public const val I_USHR: Int = OFFSET + 8
  public const val I_OR: Int = OFFSET + 9
  public const val I_AND: Int = OFFSET + 10
  public const val I_XOR: Int = OFFSET + 11
  public const val I_COPY_SIGN: Int = OFFSET + 12
  public const val I_MIN: Int = OFFSET + 13
  public const val I_MAX: Int = OFFSET + 14
  public const val I_NEG: Int = OFFSET + 15
  public const val I_ABS: Int = OFFSET + 16
  public const val I_INCR: Int = OFFSET + 17
  public const val I_DECR: Int = OFFSET + 18
  public const val I_NOT: Int = OFFSET + 19
  public const val I_SIGN: Int = OFFSET + 20
  public const val I_CLAMP: Int = OFFSET + 21
  public const val I_IFELSE: Int = OFFSET + 22
  public const val I_MAD: Int = OFFSET + 23
  public const val I_VAR1: Int = OFFSET + 24
  public const val I_VAR2: Int = OFFSET + 25
}
