package androidx.compose.remote.creation.common

/**
 * Common Kotlin write side for Remote Compose documents.
 *
 * This starts with the layout/text surface used by the cross-platform smoke client. Additional
 * operations are added here as Creation Compose moves off the legacy Java writer.
 */
public class RemoteDocumentWriter(
  width: Int,
  height: Int,
  densityBehavior: Int = DensityBehaviorDp,
  profiles: Int = 0,
) : RemoteWriter {
  private val buffer = RemoteWireBuffer()
  private var nextComponentId = -1
  private var lastComponentId = -1
  private var nextDataId = 42
  private val textIds = mutableMapOf<String, Int>()
  private val pathIds = mutableMapOf<PathKey, Int>()
  private val integerIds = mutableMapOf<Int, Int>()
  private val floatArrayIds = mutableMapOf<IntArrayKey, Int>()
  private val idListIds = mutableMapOf<IntArrayKey, Int>()
  private val floatExpressionIds = mutableMapOf<IntArrayKey, Int>()
  private val integerExpressionIds = mutableMapOf<LongArrayKey, Int>()
  private val textLengthIds = mutableMapOf<Int, Int>()
  private val textLookupIds = mutableMapOf<Long, Int>()
  private val textLookupIntIds = mutableMapOf<Long, Int>()
  private val textFromFloatIds = mutableMapOf<TextFromFloatKey, Int>()
  private val bitmapUrlIds = mutableMapOf<String, Int>()
  private val componentValueIds = mutableMapOf<Long, Int>()

  init {
    writeHeader(width, height, profiles, densityBehavior)
  }

  public fun root(content: RemoteDocumentWriter.() -> Unit) {
    operation(LayoutRoot)
    buffer.writeInt(componentId())
    content()
    containerEnd()
  }

  public fun column(
    horizontal: Int = HorizontalStart,
    vertical: Int = VerticalTop,
    spacedBy: Float = 0f,
    content: RemoteDocumentWriter.() -> Unit,
  ) {
    operation(LayoutColumn)
    buffer.writeInt(componentId())
    buffer.writeInt(-1)
    buffer.writeInt(horizontal)
    buffer.writeInt(vertical)
    buffer.writeFloat(spacedBy)
    contentSection()
    content()
    containerEnd()
    containerEnd()
  }

  public fun text(value: String, fontSize: Float = 12f) {
    val textId = textId(value)

    operation(CoreText)
    buffer.writeInt(textId)
    buffer.writeShort(2)
    buffer.writeByte(TextParameterId)
    buffer.writeInt(componentId())
    buffer.writeByte(TextParameterFontSize)
    buffer.writeFloat(fontSize)
    contentSection()
    containerEnd()
    containerEnd()
  }

  public fun encodeToByteArray(): ByteArray = buffer.toByteArray()

  override val componentIdForCache: Int
    get() = lastComponentId

  override fun addComponentWidthValue(): Float = componentValue(ComponentWidth)

  override fun addComponentHeightValue(): Float = componentValue(ComponentHeight)

  override fun setNamedVariable(id: Int, name: String, type: Int) =
    writeNamedVariable(id, type, name)

  override fun addInteger(value: Int): Long {
    val id =
      integerIds.getOrPut(value) {
        val allocated = nextDataId++
        writeInteger(allocated, value)
        allocated
      }
    return id.toLong() + IntegerIdOffset
  }

  override fun addNamedInt(name: String, initialValue: Int): Long {
    val id = nextDataId++
    writeNamedVariable(id, NamedVariableType.INT, name)
    writeInteger(id, initialValue)
    return id.toLong() + IntegerIdOffset
  }

  override fun addNamedFloat(name: String, initialValue: Float): Float {
    val id = nextDataId++
    writeNamedVariable(id, NamedVariableType.FLOAT, name)
    operation(DataFloat)
    buffer.writeInt(id)
    buffer.writeFloat(initialValue)
    return Utils.asNan(id)
  }

  override fun reserveFloatVariable(): Float = Utils.asNan(nextDataId++)

  override fun addFloatArray(values: FloatArray): Float {
    val key = IntArrayKey(IntArray(values.size) { values[it].toRawBits() })
    val id =
      floatArrayIds.getOrPut(key) {
        val allocated = nextDataId++
        operation(FloatList)
        buffer.writeInt(allocated)
        buffer.writeInt(values.size)
        values.forEach(buffer::writeFloat)
        allocated
      }
    return Utils.asNan(id)
  }

  override fun addIdList(ids: IntArray): Float {
    val id =
      idListIds.getOrPut(IntArrayKey(ids)) {
        val allocated = nextDataId++
        operation(IdList)
        buffer.writeInt(allocated)
        buffer.writeInt(ids.size)
        ids.forEach(buffer::writeInt)
        allocated
      }
    return Utils.asNan(id)
  }

  override fun addDynamicFloatArray(size: Float): Float {
    val id = nextDataId++
    operation(DynamicFloatList)
    buffer.writeInt(id)
    buffer.writeFloat(size)
    return Utils.asNan(id)
  }

  override fun setArrayValue(id: Int, index: Float, value: Float) {
    operation(UpdateDynamicFloatList)
    buffer.writeInt(id)
    buffer.writeFloat(index)
    buffer.writeFloat(value)
  }

  override fun floatExpression(vararg values: Float): Float = floatExpression(values, null)

  override fun floatExpression(values: FloatArray, animation: FloatArray?): Float {
    val key = IntArrayKey(values.toRawBits())
    val id =
      floatExpressionIds.getOrPut(key) {
        val allocated = nextDataId++
        operation(AnimatedFloat)
        buffer.writeInt(allocated)
        buffer.writeInt(values.size or ((animation?.size ?: 0) shl 16))
        values.forEach(buffer::writeFloat)
        animation?.forEach(buffer::writeFloat)
        allocated
      }
    return Utils.asNan(id)
  }

  override fun integerExpression(vararg values: Long): Long {
    var mask = 0
    val encoded =
      IntArray(values.size) { index ->
        val value = values[index]
        if (value >= IntegerIdOffset) {
          mask = mask or (1 shl index)
          (value - IntegerIdOffset).toInt()
        } else {
          value.toInt()
        }
      }
    val key = LongArrayKey(longArrayOf(mask.toLong(), *values))
    val id =
      integerExpressionIds.getOrPut(key) {
        val allocated = nextDataId++
        operation(IntegerExpression)
        buffer.writeInt(allocated)
        buffer.writeInt(mask)
        buffer.writeInt(encoded.size)
        encoded.forEach(buffer::writeInt)
        allocated
      }
    return id.toLong() + IntegerIdOffset
  }

  override fun timeAttribute(longId: Int, type: Short, vararg args: Int): Float {
    val id = nextDataId++
    operation(AttributeTime)
    buffer.writeInt(id)
    buffer.writeInt(longId)
    buffer.writeShort(type.toInt())
    buffer.writeShort(args.size)
    args.forEach(buffer::writeInt)
    return Utils.asNan(id)
  }

  override fun idLookup(arrayId: Float, index: Float): Int {
    val id = nextDataId++
    operation(IdLookup)
    buffer.writeInt(id)
    buffer.writeInt(Utils.idFromNan(arrayId))
    buffer.writeFloat(index)
    return id
  }

  override fun textLookup(arrayId: Float, index: Float): Int {
    val key = pairKey(arrayId.toRawBits(), index.toRawBits())
    return textLookupIds.getOrPut(key) {
      val id = nextDataId++
      operation(TextLookup)
      buffer.writeInt(id)
      buffer.writeInt(Utils.idFromNan(arrayId))
      buffer.writeFloat(index)
      id
    }
  }

  override fun textLookup(arrayId: Float, indexId: Int): Int {
    val key = pairKey(arrayId.toRawBits(), indexId)
    return textLookupIntIds.getOrPut(key) {
      val id = nextDataId++
      operation(TextLookupInt)
      buffer.writeInt(id)
      buffer.writeInt(Utils.idFromNan(arrayId))
      buffer.writeInt(indexId)
      id
    }
  }

  override fun textLength(textId: Int): Float {
    val id =
      textLengthIds.getOrPut(textId) {
        val allocated = nextDataId++
        operation(TextLength)
        buffer.writeInt(allocated)
        buffer.writeInt(textId)
        allocated
      }
    return Utils.asNan(id)
  }

  override fun textMerge(leftId: Int, rightId: Int): Int {
    val id = nextDataId++
    operation(TextMerge)
    buffer.writeInt(id)
    buffer.writeInt(leftId)
    buffer.writeInt(rightId)
    return id
  }

  override fun textSubtext(textId: Int, start: Float, length: Float): Int {
    val id = nextDataId++
    operation(TextSubtext)
    buffer.writeInt(id)
    buffer.writeInt(textId)
    buffer.writeFloat(start)
    buffer.writeFloat(length)
    return id
  }

  override fun textTransform(textId: Int, start: Float, length: Float, operation: Int): Int {
    val id = nextDataId++
    this.operation(TextTransform)
    buffer.writeInt(id)
    buffer.writeInt(textId)
    buffer.writeFloat(start)
    buffer.writeFloat(length)
    buffer.writeInt(operation)
    return id
  }

  override fun createTextFromFloat(value: Float, before: Int, after: Int, flags: Int): Int {
    val key = TextFromFloatKey(value.toRawBits(), before, after, flags)
    return textFromFloatIds.getOrPut(key) {
      val id = nextDataId++
      operation(TextFromFloat)
      buffer.writeInt(id)
      buffer.writeFloat(value)
      buffer.writeInt((before shl 16) or (after and 0xffff))
      buffer.writeInt(flags)
      id
    }
  }

  override fun colorAttribute(colorId: Int, type: Short): Float {
    val id = nextDataId++
    operation(AttributeColor)
    buffer.writeInt(id)
    buffer.writeInt(colorId)
    buffer.writeShort(type.toInt())
    return Utils.asNan(id)
  }

  override fun bitmapAttribute(bitmapId: Int, type: Short): Float {
    val id = nextDataId++
    operation(AttributeImage)
    buffer.writeInt(id)
    buffer.writeInt(bitmapId)
    buffer.writeShort(type.toInt())
    buffer.writeShort(0)
    return Utils.asNan(id)
  }

  override fun bitmapTextMeasure(
    textId: Int,
    bitmapFontId: Int,
    type: Int,
    glyphSpacing: Float,
  ): Float {
    val id = nextDataId++
    operation(BitmapTextMeasure)
    if (glyphSpacing == 0f) {
      buffer.writeInt(id)
    } else {
      buffer.writeInt(id or Int.MIN_VALUE)
      buffer.writeFloat(glyphSpacing)
    }
    buffer.writeInt(textId)
    buffer.writeInt(bitmapFontId)
    buffer.writeInt(type)
    return Utils.asNan(id)
  }

  override fun addBitmapUrl(url: String, width: Int, height: Int): Int =
    bitmapUrlIds.getOrPut(url) {
      val id = nextDataId++
      writeBitmapData(
        id = id,
        type = BitmapTypePng,
        width = width,
        encoding = BitmapEncodingUrl,
        height = height,
        data = url.encodeToByteArray(),
      )
      id
    }

  override fun addNamedBitmapUrl(name: String, url: String): Int {
    val id = addBitmapUrl(url)
    writeNamedVariable(id, NamedVariableType.IMAGE, name)
    return id
  }

  override fun createBitmap(width: Int, height: Int): Int {
    val id = nextDataId++
    writeBitmapData(
      id = id,
      type = BitmapTypeRaw8888,
      width = width,
      encoding = BitmapEncodingEmpty,
      height = height,
      data = byteArrayOf(),
    )
    return id
  }

  override fun addBitmapFont(glyphs: List<BitmapFontGlyph>, kerningTable: Map<String, Short>): Int {
    require(glyphs.size < 0xffff) { "Too many glyphs, the maximum is 65535" }
    require(kerningTable.size < 0xffff) { "Kerning table too big, the maximum size is 65535" }
    val id = nextDataId++
    operation(DataBitmapFont)
    buffer.writeInt(id)
    buffer.writeInt(glyphs.size or (if (kerningTable.isEmpty()) 0 else 1 shl 16))
    glyphs.sortedByDescending { it.chars.length }.forEach { glyph ->
      buffer.writeUtf8(glyph.chars)
      buffer.writeInt(glyph.bitmapId)
      buffer.writeShort(glyph.marginLeft.toInt())
      buffer.writeShort(glyph.marginTop.toInt())
      buffer.writeShort(glyph.marginRight.toInt())
      buffer.writeShort(glyph.marginBottom.toInt())
      buffer.writeShort(glyph.width.toInt())
      buffer.writeShort(glyph.height.toInt())
    }
    if (kerningTable.isNotEmpty()) {
      buffer.writeShort(kerningTable.size)
      kerningTable.forEach { (pair, adjustment) ->
        buffer.writeUtf8(pair)
        buffer.writeShort(adjustment.toInt())
      }
    }
    return id
  }

  override fun matrixExpression(vararg expression: Float): Float {
    val id = nextDataId++
    operation(MatrixExpression)
    buffer.writeInt(id)
    buffer.writeInt(0)
    buffer.writeInt(expression.size)
    expression.forEach(buffer::writeFloat)
    return Utils.asNan(id)
  }

  override fun addColor(argb: Int): Int {
    val id = nextDataId++
    operation(ColorConstant)
    buffer.writeInt(id)
    buffer.writeInt(argb)
    return id
  }

  override fun addNamedColor(name: String, argb: Int): Int {
    val id = addColor(argb)
    writeNamedVariable(id, NamedVariableType.COLOR, name)
    return id
  }

  override fun addColorExpression(color1: Int, color2: Int, tween: Float): Short =
    writeColorExpression(mode = 0, color1 = color1, color2 = color2, fourth = tween.toRawBits())

  override fun addColorExpression(color1: Short, color2: Short, tween: Float): Short =
    writeColorExpression(
      mode = 3,
      color1 = color1.toInt(),
      color2 = color2.toInt(),
      fourth = tween.toRawBits(),
    )

  override fun addColorExpression(
    alpha: Int,
    hue: Float,
    saturation: Float,
    value: Float,
  ): Short =
    writeColorExpression(
      mode = ColorHsvMode or (alpha shl 16),
      color1 = hue.toRawBits(),
      color2 = saturation.toRawBits(),
      fourth = value.toRawBits(),
    )

  override fun addColorExpression(alpha: Float, red: Float, green: Float, blue: Float): Short {
    val mode =
      if (alpha.isNaN()) {
        ColorIdArgbMode or (Utils.idFromNan(alpha) shl 16)
      } else {
        ColorArgbMode or ((alpha * 1024).toInt() shl 16)
      }
    return writeColorExpression(mode, red.toRawBits(), green.toRawBits(), blue.toRawBits())
  }

  override fun addText(value: String): Int = textId(value)

  override fun addNamedString(name: String, initialValue: String): Int {
    val id = nextDataId++
    writeNamedVariable(id, NamedVariableType.STRING, name)
    writeText(id, initialValue)
    return id
  }

  override fun addLong(value: Long): Int {
    val id = nextDataId++
    writeLong(id, value)
    return id
  }

  override fun addNamedLong(name: String, initialValue: Long): Int {
    val id = nextDataId++
    writeNamedVariable(id, NamedVariableType.LONG, name)
    writeLong(id, initialValue)
    return id
  }

  override fun addPathData(pathData: FloatArray, winding: Int): Int =
    pathIds.getOrPut(PathKey(pathData, winding)) {
      val id = nextDataId++
      operation(DataPath)
      buffer.writeInt(id or (winding shl 24))
      buffer.writeInt(pathData.size)
      pathData.forEach(buffer::writeFloat)
      id
    }

  override fun save(): Unit = operation(MatrixSave)

  override fun restore(): Unit = operation(MatrixRestore)

  override fun translate(dx: Float, dy: Float) = floats(MatrixTranslate, dx, dy)

  override fun scale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) =
    floats(MatrixScale, scaleX, scaleY, centerX, centerY)

  override fun rotate(angle: Float, centerX: Float, centerY: Float) =
    floats(MatrixRotate, angle, centerX, centerY)

  override fun skew(skewX: Float, skewY: Float) = floats(MatrixSkew, skewX, skewY)

  override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) =
    floats(ClipRect, left, top, right, bottom)

  override fun drawRect(left: Float, top: Float, right: Float, bottom: Float) =
    floats(DrawRect, left, top, right, bottom)

  override fun drawRoundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radiusX: Float,
    radiusY: Float,
  ) = floats(DrawRoundRect, left, top, right, bottom, radiusX, radiusY)

  override fun drawCircle(centerX: Float, centerY: Float, radius: Float) =
    floats(DrawCircle, centerX, centerY, radius)

  override fun drawOval(left: Float, top: Float, right: Float, bottom: Float) =
    floats(DrawOval, left, top, right, bottom)

  override fun drawArc(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
  ) = floats(DrawArc, left, top, right, bottom, startAngle, sweepAngle)

  override fun drawSector(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
  ) = floats(DrawSector, left, top, right, bottom, startAngle, sweepAngle)

  override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) =
    floats(DrawLine, x1, y1, x2, y2)

  override fun drawPath(pathId: Int) = intOperation(DrawPath, pathId)

  override fun clipPath(pathId: Int) = intOperation(ClipPath, pathId)

  override fun drawTweenPath(
    path1Id: Int,
    path2Id: Int,
    tween: Float,
    start: Float,
    stop: Float,
  ) {
    operation(DrawTweenPath)
    buffer.writeInt(path1Id)
    buffer.writeInt(path2Id)
    floatsWithoutOpcode(tween, start, stop)
  }

  override fun drawTextOnPath(
    textId: Int,
    pathId: Int,
    horizontalOffset: Float,
    verticalOffset: Float,
  ) {
    operation(DrawTextOnPath)
    buffer.writeInt(textId)
    buffer.writeInt(pathId)
    // This is the protocol order, despite the public API naming horizontal first.
    buffer.writeFloat(verticalOffset)
    buffer.writeFloat(horizontalOffset)
  }

  override fun drawTextRun(
    textId: Int,
    start: Int,
    end: Int,
    contextStart: Int,
    contextEnd: Int,
    x: Float,
    y: Float,
    isRtl: Boolean,
  ) {
    operation(DrawTextRun)
    buffer.writeInt(textId)
    buffer.writeInt(start)
    buffer.writeInt(end)
    buffer.writeInt(contextStart)
    buffer.writeInt(contextEnd)
    buffer.writeFloat(x)
    buffer.writeFloat(y)
    buffer.writeByte(if (isRtl) 1 else 0)
  }

  override fun drawTextAnchored(
    textId: Int,
    anchorX: Float,
    anchorY: Float,
    panX: Float,
    panY: Float,
    flags: Int,
  ) {
    operation(DrawTextAnchored)
    buffer.writeInt(textId)
    buffer.writeFloat(anchorX)
    buffer.writeFloat(anchorY)
    buffer.writeFloat(panX)
    buffer.writeFloat(panY)
    buffer.writeInt(flags)
  }

  override fun drawTextOnCircle(
    textId: Int,
    centerX: Float,
    centerY: Float,
    radius: Float,
    startAngle: Float,
    warpRadiusOffset: Float,
    alignment: DrawTextOnCircle.Alignment,
    placement: DrawTextOnCircle.Placement,
  ) {
    operation(DrawTextOnCircleOp)
    buffer.writeInt(textId)
    floatsWithoutOpcode(centerX, centerY, radius, startAngle, warpRadiusOffset)
    buffer.writeByte(alignment.ordinal)
    buffer.writeByte(placement.ordinal)
  }

  override fun drawScaledBitmap(
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
  ) {
    val descriptionId = textId(contentDescription)
    operation(DrawBitmapScaled)
    buffer.writeInt(imageId)
    listOf(srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom).forEach(
      buffer::writeFloat
    )
    buffer.writeInt(scaleType)
    buffer.writeFloat(scaleFactor)
    buffer.writeInt(descriptionId)
  }

  private fun writeHeader(width: Int, height: Int, profiles: Int, densityBehavior: Int) {
    operation(Header)
    buffer.writeInt(MagicNumber or MajorVersion)
    buffer.writeInt(MinorVersion)
    buffer.writeInt(PatchVersion)
    buffer.writeInt(5)
    headerInt(HeaderWidth, width)
    headerInt(HeaderHeight, height)
    headerString(HeaderContentDescription, "")
    headerInt(HeaderProfiles, profiles)
    headerInt(HeaderDensityBehavior, densityBehavior)
  }

  private fun headerInt(tag: Int, value: Int) {
    buffer.writeShort(tag)
    buffer.writeShort(IntSize)
    buffer.writeInt(value)
  }

  private fun headerString(tag: Int, value: String) {
    val encoded = value.encodeToByteArray()
    buffer.writeShort(tag or HeaderStringType)
    buffer.writeShort(encoded.size + IntSize)
    buffer.writeInt(encoded.size)
    buffer.writeBytes(encoded)
  }

  private fun contentSection() {
    operation(LayoutContent)
    buffer.writeInt(componentId())
  }

  private fun containerEnd(): Unit = operation(ContainerEnd)

  private fun componentId(): Int {
    lastComponentId = --nextComponentId
    return lastComponentId
  }

  private fun componentValue(type: Int): Float {
    val key = pairKey(lastComponentId, type)
    val id =
      componentValueIds.getOrPut(key) {
        val allocated = nextDataId++
        operation(ComponentValue)
        buffer.writeInt(allocated)
        buffer.writeInt(type)
        allocated
      }
    return Utils.asNan(id)
  }

  private fun operation(opcode: Int): Unit = buffer.writeByte(opcode)

  private fun intOperation(opcode: Int, value: Int) {
    operation(opcode)
    buffer.writeInt(value)
  }

  private fun floats(opcode: Int, vararg values: Float) {
    operation(opcode)
    floatsWithoutOpcode(*values)
  }

  private fun floatsWithoutOpcode(vararg values: Float) = values.forEach(buffer::writeFloat)

  private fun FloatArray.toRawBits(): IntArray = IntArray(size) { this[it].toRawBits() }

  private fun pairKey(first: Int, second: Int): Long =
    (first.toLong() shl 32) or (second.toLong() and 0xffffffffL)

  private fun textId(value: String): Int =
    textIds.getOrPut(value) {
      val id = nextDataId++
      writeText(id, value)
      id
    }

  private fun writeText(id: Int, value: String) {
    operation(DataText)
    buffer.writeInt(id)
    buffer.writeUtf8(value)
  }

  private fun writeLong(id: Int, value: Long) {
    operation(DataLong)
    buffer.writeInt(id)
    buffer.writeLong(value)
  }

  private fun writeInteger(id: Int, value: Int) {
    operation(DataInt)
    buffer.writeInt(id)
    buffer.writeInt(value)
  }

  private fun writeNamedVariable(id: Int, type: Int, name: String) {
    operation(NamedVariable)
    buffer.writeInt(id)
    buffer.writeInt(type)
    buffer.writeUtf8(name)
  }

  private fun writeBitmapData(
    id: Int,
    type: Int,
    width: Int,
    encoding: Int,
    height: Int,
    data: ByteArray,
  ) {
    operation(DataBitmap)
    buffer.writeInt(id)
    buffer.writeInt((type shl 16) or (width and 0xffff))
    buffer.writeInt((encoding shl 16) or (height and 0xffff))
    buffer.writeInt(data.size)
    buffer.writeBytes(data)
  }

  private fun writeColorExpression(mode: Int, color1: Int, color2: Int, fourth: Int): Short {
    val id = nextDataId++
    operation(ColorExpression)
    buffer.writeInt(id)
    buffer.writeInt(mode)
    buffer.writeInt(color1)
    buffer.writeInt(color2)
    buffer.writeInt(fourth)
    return id.toShort()
  }

  public companion object {
    public const val DensityBehaviorDp: Int = 2
    public const val HorizontalStart: Int = 1
    public const val VerticalTop: Int = 4

    private const val Header = 0
    private const val DataBitmap = 101
    private const val DataText = 102
    private const val DataFloat = 80
    private const val AnimatedFloat = 81
    private const val LayoutRoot = 200
    private const val LayoutContent = 201
    private const val LayoutColumn = 204
    private const val ContainerEnd = 214
    private const val CoreText = 239
    private const val ClipRect = 39
    private const val ClipPath = 38
    private const val DrawRect = 42
    private const val DrawTextRun = 43
    private const val DrawCircle = 46
    private const val DrawLine = 47
    private const val DrawRoundRect = 51
    private const val DrawSector = 52
    private const val DrawTextOnPath = 53
    private const val DrawOval = 56
    private const val DrawTextOnCircleOp = 57
    private const val DataPath = 123
    private const val DrawPath = 124
    private const val DrawTweenPath = 125
    private const val MatrixScale = 126
    private const val MatrixTranslate = 127
    private const val MatrixSkew = 128
    private const val MatrixRotate = 129
    private const val MatrixSave = 130
    private const val MatrixRestore = 131
    private const val DrawTextAnchored = 133
    private const val ColorExpression = 134
    private const val TextFromFloat = 135
    private const val TextMerge = 136
    private const val NamedVariable = 137
    private const val ColorConstant = 138
    private const val DataInt = 140
    private const val IntegerExpression = 144
    private const val IdList = 146
    private const val FloatList = 147
    private const val DataLong = 148
    private const val DrawBitmapScaled = 149
    private const val ComponentValue = 150
    private const val DrawArc = 152
    private const val TextLookup = 151
    private const val TextLookupInt = 153
    private const val TextLength = 156
    private const val AttributeImage = 171
    private const val AttributeTime = 172
    private const val AttributeColor = 180
    private const val TextSubtext = 182
    private const val BitmapTextMeasure = 183
    private const val MatrixExpression = 187
    private const val IdLookup = 192
    private const val DynamicFloatList = 197
    private const val UpdateDynamicFloatList = 198
    private const val TextTransform = 199
    private const val DataBitmapFont = 167

    private const val BitmapTypePng = 1
    private const val BitmapTypeRaw8888 = 3
    private const val BitmapEncodingUrl = 1
    private const val BitmapEncodingEmpty = 3
    private const val ColorHsvMode = 4
    private const val ColorArgbMode = 5
    private const val ColorIdArgbMode = 6
    private const val ComponentWidth = 0
    private const val ComponentHeight = 1

    private const val MagicNumber = 0x048C0000
    private const val MajorVersion = 1
    private const val MinorVersion = 1
    private const val PatchVersion = 0
    private const val IntSize = 4
    private const val HeaderWidth = 5
    private const val HeaderHeight = 6
    private const val HeaderContentDescription = 9
    private const val HeaderProfiles = 14
    private const val HeaderDensityBehavior = 27
    private const val HeaderStringType = 3 shl 10
    private const val TextParameterId = 1
    private const val TextParameterFontSize = 5
    private const val IntegerIdOffset = 0x100000000L
  }
}

private class PathKey(pathData: FloatArray, private val winding: Int) {
  private val bits = IntArray(pathData.size) { pathData[it].toRawBits() }

  override fun equals(other: Any?): Boolean =
    other is PathKey && winding == other.winding && bits.contentEquals(other.bits)

  override fun hashCode(): Int = 31 * bits.contentHashCode() + winding
}

private class IntArrayKey(values: IntArray) {
  private val values = values.copyOf()

  override fun equals(other: Any?): Boolean = other is IntArrayKey && values.contentEquals(other.values)

  override fun hashCode(): Int = values.contentHashCode()
}

private class LongArrayKey(values: LongArray) {
  private val values = values.copyOf()

  override fun equals(other: Any?): Boolean =
    other is LongArrayKey && values.contentEquals(other.values)

  override fun hashCode(): Int = values.contentHashCode()
}

private data class TextFromFloatKey(
  val valueBits: Int,
  val before: Int,
  val after: Int,
  val flags: Int,
)
