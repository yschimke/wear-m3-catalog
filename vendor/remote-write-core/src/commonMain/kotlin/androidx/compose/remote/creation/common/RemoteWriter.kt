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
  public fun setNamedVariable(id: Int, name: String, type: Int)

  public fun addInteger(value: Int): Long

  public fun addNamedInt(name: String, initialValue: Int): Long

  public fun addNamedFloat(name: String, initialValue: Float): Float

  public fun reserveFloatVariable(): Float

  public fun addFloatArray(values: FloatArray): Float

  public fun addIdList(ids: IntArray): Float

  public fun addColor(argb: Int): Int

  public fun addNamedColor(name: String, argb: Int): Int

  public fun addText(value: String): Int

  public fun addNamedString(name: String, initialValue: String): Int

  public fun addLong(value: Long): Int

  public fun addNamedLong(name: String, initialValue: Long): Int

  public fun addPathData(pathData: FloatArray, winding: Int = 0): Int

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

public object NamedVariableType {
  public const val STRING: Int = 0
  public const val FLOAT: Int = 1
  public const val COLOR: Int = 2
  public const val IMAGE: Int = 3
  public const val INT: Int = 4
  public const val LONG: Int = 5
  public const val FLOAT_ARRAY: Int = 6
}
