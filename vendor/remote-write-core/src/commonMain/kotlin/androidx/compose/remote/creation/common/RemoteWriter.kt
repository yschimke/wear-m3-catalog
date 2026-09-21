package androidx.compose.remote.creation.common

/**
 * Platform-neutral write surface consumed by the typed Creation Compose document program.
 *
 * This interface intentionally describes protocol operations rather than a canvas. Creation
 * Compose retains remote values in its IR until optimization is complete, then resolves them and
 * calls this writer. The JVM/Android migration currently adapts these calls to the legacy Java
 * writer; the common encoder will implement the same surface directly.
 */
public interface RemoteWriter {
  public val componentIdForCache: Int

  public fun addComponentWidthValue(): Float

  public fun addComponentHeightValue(): Float

  public fun setNamedVariable(id: Int, name: String, type: Int)

  public fun addInteger(value: Int): Long

  public fun addNamedInt(name: String, initialValue: Int): Long

  public fun addNamedFloat(name: String, initialValue: Float): Float

  public fun reserveFloatVariable(): Float

  public fun addFloatArray(values: FloatArray): Float

  public fun addIdList(ids: IntArray): Float

  public fun addDynamicFloatArray(size: Float): Float

  public fun setArrayValue(id: Int, index: Float, value: Float)

  public fun floatExpression(vararg values: Float): Float

  public fun floatExpression(values: FloatArray, animation: FloatArray?): Float

  public fun integerExpression(vararg values: Long): Long

  public fun timeAttribute(longId: Int, type: Short, vararg args: Int): Float

  public fun idLookup(arrayId: Float, index: Float): Int

  public fun textLookup(arrayId: Float, index: Float): Int

  public fun textLookup(arrayId: Float, indexId: Int): Int

  public fun textLength(textId: Int): Float

  public fun textMerge(leftId: Int, rightId: Int): Int

  public fun textSubtext(textId: Int, start: Float, length: Float): Int

  public fun textTransform(textId: Int, start: Float, length: Float, operation: Int): Int

  public fun createTextFromFloat(value: Float, before: Int, after: Int, flags: Int): Int

  public fun colorAttribute(colorId: Int, type: Short): Float

  public fun bitmapAttribute(bitmapId: Int, type: Short): Float

  public fun bitmapTextMeasure(
    textId: Int,
    bitmapFontId: Int,
    type: Int,
    glyphSpacing: Float,
  ): Float

  public fun addBitmapUrl(url: String, width: Int = 1, height: Int = 1): Int

  public fun addNamedBitmapUrl(name: String, url: String): Int

  public fun createBitmap(width: Int, height: Int): Int

  public fun addBitmapFont(
    glyphs: List<BitmapFontGlyph>,
    kerningTable: Map<String, Short> = emptyMap(),
  ): Int

  public fun matrixExpression(vararg expression: Float): Float

  public fun addColor(argb: Int): Int

  public fun addNamedColor(name: String, argb: Int): Int

  public fun addColorExpression(color1: Int, color2: Int, tween: Float): Short

  public fun addColorExpression(color1: Short, color2: Short, tween: Float): Short

  public fun addColorExpression(alpha: Int, hue: Float, saturation: Float, value: Float): Short

  public fun addColorExpression(alpha: Float, red: Float, green: Float, blue: Float): Short

  public fun addText(value: String): Int

  public fun addNamedString(name: String, initialValue: String): Int

  public fun addLong(value: Long): Int

  public fun addNamedLong(name: String, initialValue: Long): Int

  public fun addPathData(pathData: FloatArray, winding: Int = 0): Int

  public fun applyPaint(paint: PaintBundleData)

  /** Returns and clears whether the next paint must be emitted as a complete state. */
  public fun consumePaintReset(): Boolean

  public fun save()

  public fun restore()

  public fun translate(dx: Float, dy: Float)

  public fun scale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float)

  public fun rotate(angle: Float, centerX: Float, centerY: Float)

  public fun skew(skewX: Float, skewY: Float)

  public fun clipRect(left: Float, top: Float, right: Float, bottom: Float)

  public fun drawRect(left: Float, top: Float, right: Float, bottom: Float)

  public fun drawRoundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radiusX: Float,
    radiusY: Float,
  )

  public fun drawCircle(centerX: Float, centerY: Float, radius: Float)

  public fun drawOval(left: Float, top: Float, right: Float, bottom: Float)

  public fun drawArc(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
  )

  public fun drawSector(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
  )

  public fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float)

  public fun drawPath(pathId: Int)

  public fun clipPath(pathId: Int)

  public fun drawTweenPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, stop: Float)

  public fun drawTextOnPath(textId: Int, pathId: Int, horizontalOffset: Float, verticalOffset: Float)

  public fun drawTextRun(
    textId: Int,
    start: Int,
    end: Int,
    contextStart: Int,
    contextEnd: Int,
    x: Float,
    y: Float,
    isRtl: Boolean,
  )

  public fun drawTextAnchored(
    textId: Int,
    anchorX: Float,
    anchorY: Float,
    panX: Float,
    panY: Float,
    flags: Int,
  )

  public fun drawTextOnCircle(
    textId: Int,
    centerX: Float,
    centerY: Float,
    radius: Float,
    startAngle: Float,
    warpRadiusOffset: Float,
    alignment: DrawTextOnCircle.Alignment,
    placement: DrawTextOnCircle.Placement,
  )

  public fun drawScaledBitmap(
    imageId: Int,
    srcLeft: Float,
    srcTop: Float,
    srcRight: Float,
    srcBottom: Float,
    dstLeft: Float,
    dstTop: Float,
    dstRight: Float,
    dstBottom: Float,
    scaleType: Int,
    scaleFactor: Float,
    contentDescription: String,
  )
}

public data class BitmapFontGlyph(
  val chars: String,
  val bitmapId: Int,
  val marginLeft: Short,
  val marginTop: Short,
  val marginRight: Short,
  val marginBottom: Short,
  val width: Short,
  val height: Short,
)

public object NamedVariableType {
  public const val STRING: Int = 0
  public const val FLOAT: Int = 1
  public const val COLOR: Int = 2
  public const val IMAGE: Int = 3
  public const val INT: Int = 4
  public const val LONG: Int = 5
  public const val FLOAT_ARRAY: Int = 6
}
