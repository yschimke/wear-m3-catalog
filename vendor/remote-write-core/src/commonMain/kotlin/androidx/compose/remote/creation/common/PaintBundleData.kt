package androidx.compose.remote.creation.common

/** Platform-neutral encoding of a Remote Compose paint delta. */
public class PaintBundleData {
  private val values = mutableListOf<Int>()

  public fun setTextSize(value: Float): PaintBundleData = value(TextSize, value)
  public fun setColor(argb: Int): PaintBundleData = value(Color, argb)
  public fun setColorId(id: Int): PaintBundleData = value(ColorId, id)
  public fun setStrokeWidth(value: Float): PaintBundleData = value(StrokeWidth, value)
  public fun setStrokeMiter(value: Float): PaintBundleData = value(StrokeMiter, value)
  public fun setStrokeCap(value: Int): PaintBundleData = command(StrokeCap, value)
  public fun setStrokeJoin(value: Int): PaintBundleData = command(StrokeJoin, value)
  public fun setStyle(value: Int): PaintBundleData = command(Style, value)
  public fun setImageFilterQuality(value: Int): PaintBundleData = command(ImageFilterQuality, value)
  public fun setBlendMode(value: Int): PaintBundleData = command(BlendMode, value)
  public fun setAntiAlias(value: Boolean): PaintBundleData = command(AntiAlias, if (value) 1 else 0)
  public fun setFilterBitmap(value: Boolean): PaintBundleData =
    command(FilterBitmap, if (value) 1 else 0)
  public fun setAlpha(value: Float): PaintBundleData = value(Alpha, value)
  public fun setShader(shaderId: Int): PaintBundleData = value(Shader, shaderId)
  public fun setShaderMatrix(matrixId: Float): PaintBundleData = value(ShaderMatrix, matrixId)
  public fun setColorFilter(color: Int, mode: Int): PaintBundleData =
    value(ColorFilter or (mode shl 16), color)
  public fun setColorFilterId(colorId: Int, mode: Int): PaintBundleData =
    value(ColorFilterId or (mode shl 16), colorId)
  public fun clearColorFilter(): PaintBundleData = appendCommand(ClearColorFilter)

  public fun setTextStyle(
    fontType: Int,
    weight: Int,
    italic: Boolean,
    fontData: Boolean = false,
  ): PaintBundleData {
    val style = (weight and 0x3ff) or (if (italic) 2048 else 0) or (if (fontData) 1024 else 0)
    return value(Typeface or (style shl 16), fontType)
  }

  public fun setFallbackTypeface(fontType: Int, weight: Int, italic: Boolean): PaintBundleData {
    val style = (weight and 0x3ff) or (if (italic) 2048 else 0)
    return value(FallbackTypeface or (style shl 16), fontType)
  }

  public fun setFontAxes(tags: IntArray, axisValues: FloatArray): PaintBundleData {
    require(tags.size == axisValues.size)
    require(tags.size <= 8)
    append(FontAxis or (tags.size shl 16))
    tags.indices.forEach { index ->
      append(tags[index])
      append(axisValues[index].toRawBits())
    }
    return this
  }

  public fun setPathEffect(pathEffect: FloatArray?): PaintBundleData {
    require((pathEffect?.size ?: 0) <= 2028)
    append(PathEffect or ((pathEffect?.size ?: 0) shl 16))
    pathEffect?.forEach { append(it.toRawBits()) }
    return this
  }

  public fun setLinearGradient(
    colors: IntArray,
    idMask: Int,
    stops: FloatArray?,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    tileMode: Int,
  ): PaintBundleData =
    gradient(LinearGradient, colors, idMask, stops) {
      append(startX.toRawBits())
      append(startY.toRawBits())
      append(endX.toRawBits())
      append(endY.toRawBits())
      append(tileMode)
    }

  public fun setRadialGradient(
    colors: IntArray,
    idMask: Int,
    stops: FloatArray?,
    centerX: Float,
    centerY: Float,
    radius: Float,
    tileMode: Int,
  ): PaintBundleData =
    gradient(RadialGradient, colors, idMask, stops) {
      append(centerX.toRawBits())
      append(centerY.toRawBits())
      append(radius.toRawBits())
      append(tileMode)
    }

  public fun setSweepGradient(
    colors: IntArray,
    idMask: Int,
    stops: FloatArray?,
    centerX: Float,
    centerY: Float,
  ): PaintBundleData =
    gradient(SweepGradient, colors, idMask, stops) {
      append(centerX.toRawBits())
      append(centerY.toRawBits())
    }

  public fun setTextureShader(
    bitmapId: Int,
    tileModeX: Short,
    tileModeY: Short,
    filterMode: Short,
    maxAnisotropy: Short,
  ): PaintBundleData {
    append(Texture)
    append(bitmapId)
    append((tileModeX.toInt() and 0xffff) or (tileModeY.toInt() shl 16))
    append((filterMode.toInt() and 0xffff) or (maxAnisotropy.toInt() shl 16))
    return this
  }

  public fun toIntArray(): IntArray = values.toIntArray()

  private fun command(type: Int, value: Int): PaintBundleData = appendCommand(type or (value shl 16))
  private fun value(type: Int, value: Int): PaintBundleData {
    append(type)
    append(value)
    return this
  }
  private fun value(type: Int, value: Float): PaintBundleData = value(type, value.toRawBits())
  private fun appendCommand(command: Int): PaintBundleData {
    append(command)
    return this
  }

  private fun gradient(
    type: Int,
    colors: IntArray,
    idMask: Int,
    stops: FloatArray?,
    tail: PaintBundleData.() -> Unit,
  ): PaintBundleData {
    require(colors.size <= 255)
    append(Gradient or (type shl 16))
    append((idMask shl 16) or colors.size)
    colors.forEach(::append)
    append(stops?.size ?: 0)
    stops?.forEach { append(it.toRawBits()) }
    tail()
    return this
  }

  private fun append(value: Int) { values += value }

  public companion object {
    public const val FONT_TYPE_DEFAULT: Int = 0
    public const val FONT_TYPE_SANS_SERIF: Int = 1
    public const val FONT_TYPE_SERIF: Int = 2
    public const val FONT_TYPE_MONOSPACE: Int = 3

    const val TextSize = 1
    const val Color = 4
    const val StrokeWidth = 5
    const val StrokeMiter = 6
    const val StrokeCap = 7
    const val Style = 8
    const val Shader = 9
    const val ImageFilterQuality = 10
    const val Gradient = 11
    const val Alpha = 12
    const val ColorFilter = 13
    const val AntiAlias = 14
    const val StrokeJoin = 15
    const val Typeface = 16
    const val FilterBitmap = 17
    const val BlendMode = 18
    const val ColorId = 19
    const val ColorFilterId = 20
    const val ClearColorFilter = 21
    const val ShaderMatrix = 22
    const val FontAxis = 23
    const val Texture = 24
    const val PathEffect = 25
    const val FallbackTypeface = 26
    const val LinearGradient = 0
    const val RadialGradient = 1
    const val SweepGradient = 2
  }
}
