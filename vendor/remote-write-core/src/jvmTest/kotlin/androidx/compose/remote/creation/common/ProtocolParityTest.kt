package androidx.compose.remote.creation.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ProtocolParityTest {
  @Test
  fun identifiersMatchAndroidxCore() {
    assertEquals(
      androidx.compose.remote.core.operations.Utils.asNan(42).toRawBits(),
      Utils.asNan(42).toRawBits(),
    )
    assertEquals(
      androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression.ADD.toRawBits(),
      AnimatedFloatExpression.ADD.toRawBits(),
    )
    assertEquals(
      androidx.compose.remote.core.operations.utilities.MatrixOperations.ROT_PZ.toRawBits(),
      MatrixOperations.ROT_PZ.toRawBits(),
    )
    assertEquals(androidx.compose.remote.core.RemoteContext.ID_EPOCH_SECOND, RemoteContext.ID_EPOCH_SECOND)
  }

  @Test
  fun colorHelpersMatchAndroidxCore() {
    val from = 0xff336699.toInt()
    val to = 0xffeedd44.toInt()
    assertEquals(
      androidx.compose.remote.core.operations.Utils.interpolateColor(from, to, 0.37f),
      Utils.interpolateColor(from, to, 0.37f),
    )
    assertEquals(androidx.compose.remote.core.operations.Utils.getHue(from), Utils.getHue(from))
    assertEquals(
      androidx.compose.remote.core.operations.Utils.getSaturation(from),
      Utils.getSaturation(from),
    )
    assertEquals(
      androidx.compose.remote.core.operations.Utils.getBrightness(from),
      Utils.getBrightness(from),
    )
  }

  @Test
  fun formattingMatchesAndroidxCore() {
    val expected =
      androidx.compose.remote.core.operations.utilities.StringUtils.floatToString(
        -12345.678f,
        1,
        2,
        0.toChar(),
        '0',
        androidx.compose.remote.core.operations.utilities.StringUtils.SEPARATOR_COMMA_PERIOD,
        androidx.compose.remote.core.operations.utilities.StringUtils.GROUPING_BY3,
        androidx.compose.remote.core.operations.utilities.StringUtils.ROUNDING,
      )
    assertEquals(
      expected,
      StringUtils.floatToString(
        -12345.678f,
        1,
        2,
        0.toChar(),
        '0',
        StringUtils.SEPARATOR_COMMA_PERIOD,
        StringUtils.GROUPING_BY3,
        2,
      ),
    )
  }

  @Test
  fun canvasWriterOperationsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addMatrixSave()
    expected.addMatrixTranslate(1f, 2f)
    expected.addMatrixScale(3f, 4f, 5f, 6f)
    expected.addMatrixRotate(7f, 8f, 9f)
    expected.addMatrixSkew(10f, 11f)
    expected.addClipRect(12f, 13f, 14f, 15f)
    expected.addDrawRect(16f, 17f, 18f, 19f)
    expected.addDrawRoundRect(20f, 21f, 22f, 23f, 24f, 25f)
    expected.addDrawCircle(26f, 27f, 28f)
    expected.addDrawOval(29f, 30f, 31f, 32f)
    expected.addDrawArc(33f, 34f, 35f, 36f, 37f, 38f)
    expected.addDrawSector(39f, 40f, 41f, 42f, 43f, 44f)
    expected.addDrawLine(45f, 46f, 47f, 48f)
    expected.addDrawTextRun(49, 1, 2, 3, 4, 50f, 51f, true)
    expected.drawTextAnchored(52, 53f, 54f, 55f, 56f, 57)
    expected.addDrawTextOnCircle(
      58,
      59f,
      60f,
      61f,
      62f,
      63f,
      androidx.compose.remote.core.operations.DrawTextOnCircle.Alignment.END,
      androidx.compose.remote.core.operations.DrawTextOnCircle.Placement.INSIDE,
    )
    expected.addText(42, "bitmap")
    expected.drawScaledBitmap(64, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 4, 0.5f, 42)
    val path1 = floatArrayOf(Utils.asNan(10), 1f, 2f, Utils.asNan(15))
    val path2 = floatArrayOf(Utils.asNan(10), 3f, 4f, Utils.asNan(15))
    expected.addPathData(43, path1)
    expected.addDrawPath(43)
    expected.addClipPath(43)
    expected.addDrawTextOnPath(49, 43, 1.5f, 2.5f)
    expected.addPathData(44, path2)
    expected.addDrawTweenPath(43, 44, 0.25f, 0f, 1f)
    expected.addMatrixRestore()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.save()
    writer.translate(1f, 2f)
    writer.scale(3f, 4f, 5f, 6f)
    writer.rotate(7f, 8f, 9f)
    writer.skew(10f, 11f)
    writer.clipRect(12f, 13f, 14f, 15f)
    writer.drawRect(16f, 17f, 18f, 19f)
    writer.drawRoundRect(20f, 21f, 22f, 23f, 24f, 25f)
    writer.drawCircle(26f, 27f, 28f)
    writer.drawOval(29f, 30f, 31f, 32f)
    writer.drawArc(33f, 34f, 35f, 36f, 37f, 38f)
    writer.drawSector(39f, 40f, 41f, 42f, 43f, 44f)
    writer.drawLine(45f, 46f, 47f, 48f)
    writer.drawTextRun(49, 1, 2, 3, 4, 50f, 51f, true)
    writer.drawTextAnchored(52, 53f, 54f, 55f, 56f, 57)
    writer.drawTextOnCircle(
      58,
      59f,
      60f,
      61f,
      62f,
      63f,
      DrawTextOnCircle.Alignment.END,
      DrawTextOnCircle.Placement.INSIDE,
    )
    writer.drawScaledBitmap(64, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 4, 0.5f, "bitmap")
    val path1Id = writer.addPathData(path1)
    writer.drawPath(path1Id)
    writer.clipPath(path1Id)
    writer.drawTextOnPath(49, path1Id, 1.5f, 2.5f)
    val path2Id = writer.addPathData(path2)
    writer.drawTweenPath(path1Id, path2Id, 0.25f, 0f, 1f)
    writer.restore()

    assertContentEquals(
      expected.buffer.cloneBytes(),
      writer.encodeToByteArray().copyOfRange(headerSize, writer.encodeToByteArray().size),
    )
  }

  @Test
  fun evenOddPathMatchesAndroidxCore() {
    val data = floatArrayOf(Utils.asNan(10), 1f, 2f, Utils.asNan(15))
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addPathData(42, data, 1)
    expected.addDrawPath(42)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.drawPath(writer.addPathData(data, winding = 1))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun layoutContainersMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addRootStart()
    expected.addBoxStart(-1, -1, 1, 4)
    expected.addContentStart()
    expected.addRowStart(-1, -1, 2, 5, 6f)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addColumnStart(-1, -1, 3, 6, 7f)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addCollapsibleRowStart(-1, -1, 2, 5, 8f)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addCollapsibleColumnStart(-1, -1, 3, 6, 9f)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addFitBoxStart(-1, -1, 1, 4)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addFlowStart(-1, -1, 2, 5, 8f, 3, 4)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addStateLayout(-1, -1, 0, 0, 42)
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addCanvasStart(-1, -1)
    expected.addContentStart()
    expected.addCanvasOperationsStart()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addContainerEnd()
    expected.addContainerEnd()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.startRoot()
    writer.startBox(RemoteModifierData(), 1, 4)
    writer.startRow(RemoteModifierData(spacedBy = 6f), 2, 5)
    writer.endRow()
    writer.startColumn(RemoteModifierData(spacedBy = 7f), 3, 6)
    writer.endColumn()
    writer.startCollapsibleRow(RemoteModifierData(spacedBy = 8f), 2, 5)
    writer.endCollapsibleRow()
    writer.startCollapsibleColumn(RemoteModifierData(spacedBy = 9f), 3, 6)
    writer.endCollapsibleColumn()
    writer.startFitBox(RemoteModifierData(), 1, 4)
    writer.endFitBox()
    writer.startFlow(RemoteModifierData(spacedBy = 8f), 2, 5, 3, 4)
    writer.endFlow()
    writer.startStateLayout(RemoteModifierData(), 42)
    writer.endStateLayout()
    writer.startCanvas(RemoteModifierData())
    writer.startCanvasOperations()
    writer.endCanvasOperations()
    writer.endCanvas()
    writer.endBox()
    writer.endRoot()
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun basicModifiersMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addWidthModifierOperation(6, 10f)
    expected.addHeightModifierOperation(3, 0.5f)
    expected.addModifierPadding(1f, 2f, 3f, 4f)
    expected.addModifierBackground(0.1f, 0.2f, 0.3f, 0.4f, 1)
    expected.addDynamicModifierBackground(42, 0)
    expected.addClipRectModifier()
    expected.addModifierOffset(5f, 6f)
    expected.addModifierZIndex(7f)
    expected.addModifierRipple()
    expected.addDrawContentOperation()
    androidx.compose.remote.core.WriteOperations.patternInflation(expected.buffer, 9, intArrayOf(42, 43))
    expected.addContainerEnd()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.writeModifier(RemoteModifierOperation.Width(6, 10f))
    writer.writeModifier(RemoteModifierOperation.Height(3, 0.5f))
    writer.writeModifier(RemoteModifierOperation.Padding(1f, 2f, 3f, 4f))
    writer.writeModifier(
      RemoteModifierOperation.Background(0, 0, 0.1f, 0.2f, 0.3f, 0.4f, 1)
    )
    writer.writeModifier(RemoteModifierOperation.Background(2, 42, 0f, 0f, 0f, 0f, 0))
    writer.writeModifier(RemoteModifierOperation.ClipRect)
    writer.writeModifier(RemoteModifierOperation.Offset(5f, 6f))
    writer.writeModifier(RemoteModifierOperation.ZIndex(7f))
    writer.writeModifier(RemoteModifierOperation.Ripple)
    writer.writeModifier(RemoteModifierOperation.DrawContent)
    writer.writeModifier(RemoteModifierOperation.MacroCall(9, intArrayOf(42, 43)))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun clickActionsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addClickModifierOperation(2)
    val buffer = expected.buffer
    androidx.compose.remote.core.operations.layout.modifiers.HostActionOperation.apply(buffer, 7)
    androidx.compose.remote.core.operations.layout.modifiers.HostActionMetadataOperation.apply(
      buffer,
      8,
      42,
    )
    androidx.compose.remote.core.operations.layout.modifiers.HostNamedActionOperation.apply(
      buffer,
      43,
      1,
      44,
    )
    expected.addValueIntegerChangeActionOperation(45, 12)
    expected.addValueIntegerExpressionChangeActionOperation(46L, 47L)
    expected.addValueFloatChangeActionOperation(48, 1.5f)
    expected.addValueFloatExpressionChangeActionOperation(49, 50)
    expected.addValueStringChangeActionOperation(51, 52)
    expected.addContainerEnd()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.writeModifier(
      RemoteModifierOperation.Click(
        2,
        listOf(
          RemoteActionData.Host(7),
          RemoteActionData.HostMetadata(8, 42),
          RemoteActionData.HostNamed(43, 1, 44),
          RemoteActionData.IntegerChange(45, 12),
          RemoteActionData.IntegerExpressionChange(46L, 47L),
          RemoteActionData.FloatChange(48, 1.5f),
          RemoteActionData.FloatExpressionChange(49, 50),
          RemoteActionData.StringChange(51, 52),
        ),
      )
    )
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun touchActionsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addTouchDownModifierOperation()
    expected.addValueFloatChangeActionOperation(42, 1f)
    expected.addContainerEnd()
    expected.addTouchUpModifierOperation()
    expected.addValueIntegerChangeActionOperation(43, 2)
    expected.addContainerEnd()
    expected.addTouchCancelModifierOperation()
    expected.addValueStringChangeActionOperation(44, 45)
    expected.addContainerEnd()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.writeModifier(
      RemoteModifierOperation.Touch(0, listOf(RemoteActionData.FloatChange(42, 1f)))
    )
    writer.writeModifier(
      RemoteModifierOperation.Touch(1, listOf(RemoteActionData.IntegerChange(43, 2)))
    )
    writer.writeModifier(
      RemoteModifierOperation.Touch(2, listOf(RemoteActionData.StringChange(44, 45)))
    )
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun semanticsModifierMatchesAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    androidx.compose.remote.core.semantics.CoreSemantics.apply(
      expected.buffer,
      42,
      3,
      43,
      44,
      2,
      false,
      true,
    )

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.writeModifier(RemoteModifierOperation.Semantics(42, 3, 43, 44, 2, false, true))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun coreTextLayoutMatchesAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addTextComponentStart(
      -1,
      -1,
      42,
      -1,
      0xff123456.toInt(),
      43,
      18f,
      10f,
      30f,
      1,
      650f,
      44,
      2,
      0,
      3,
      1.5f,
      2f,
      1.2f,
      1,
      2,
      0,
      true,
      true,
      intArrayOf(45),
      floatArrayOf(0.5f),
      true,
      7,
    )
    expected.addContentStart()
    expected.addContainerEnd()
    expected.addContainerEnd()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.startText(
      RemoteTextData(
        RemoteModifierData(),
        textId = 42,
        color = 0xff123456.toInt(),
        colorId = 43,
        fontSize = 18f,
        minFontSize = 10f,
        maxFontSize = 30f,
        fontStyle = 1,
        fontWeight = 650f,
        fontFamilyId = 44,
        textAlign = 2,
        overflow = 0,
        maxLines = 3,
        letterSpacing = 1.5f,
        lineHeightAdd = 2f,
        lineHeightMultiplier = 1.2f,
        lineBreakStrategy = 1,
        hyphenationFrequency = 2,
        underline = true,
        strikethrough = true,
        fontAxisIds = intArrayOf(45),
        fontAxisValues = floatArrayOf(0.5f),
        autosize = true,
        flags = 7,
      )
    )
    writer.endText()
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun imageLayoutMatchesAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addImage(-1, -1, 42, 4, 0.75f)
    expected.addContainerEnd()

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.image(RemoteImageData(RemoteModifierData(), 42, 4, 0.75f))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun longStateOperationsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addLong(42, 0x1020304050607080L)
    expected.setNamedVariable(
      43,
      "USER:timestamp",
      androidx.compose.remote.core.operations.NamedVariable.LONG_TYPE,
    )
    expected.addLong(43, -123456789L)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(42, writer.addLong(0x1020304050607080L))
    assertEquals(43, writer.addNamedLong("USER:timestamp", -123456789L))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun colorStateOperationsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addColor(42, 0xff123456.toInt())
    expected.addColor(43, 0xffabcdef.toInt())
    expected.setNamedVariable(
      43,
      "USER:accent",
      androidx.compose.remote.core.operations.NamedVariable.COLOR_TYPE,
    )

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(42, writer.addColor(0xff123456.toInt()))
    assertEquals(43, writer.addNamedColor("USER:accent", 0xffabcdef.toInt()))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun stringStateOperationsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addText(42, "hello")
    expected.setNamedVariable(
      43,
      "USER:title",
      androidx.compose.remote.core.operations.NamedVariable.STRING_TYPE,
    )
    expected.addText(43, "world")

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(42, writer.addText("hello"))
    assertEquals(42, writer.addText("hello"))
    assertEquals(43, writer.addNamedString("USER:title", "world"))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun scalarStateOperationsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addInteger(42, 123)
    expected.setNamedVariable(
      43,
      "USER:count",
      androidx.compose.remote.core.operations.NamedVariable.INT_TYPE,
    )
    expected.addInteger(43, -4)
    expected.setNamedVariable(
      44,
      "USER:progress",
      androidx.compose.remote.core.operations.NamedVariable.FLOAT_TYPE,
    )
    expected.addFloat(44, 0.75f)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(42L + 0x100000000L, writer.addInteger(123))
    assertEquals(42L + 0x100000000L, writer.addInteger(123))
    assertEquals(43L + 0x100000000L, writer.addNamedInt("USER:count", -4))
    assertEquals(Utils.asNan(44).toRawBits(), writer.addNamedFloat("USER:progress", 0.75f).toRawBits())
    assertEquals(Utils.asNan(45).toRawBits(), writer.reserveFloatVariable().toRawBits())
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun listStateOperationsMatchAndroidxCore() {
    val floats = floatArrayOf(1f, Utils.asNan(7), 3f)
    val ids = intArrayOf(9, 10, 11)
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addFloatArray(42, floats)
    expected.addList(43, ids)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(Utils.asNan(42).toRawBits(), writer.addFloatArray(floats).toRawBits())
    assertEquals(Utils.asNan(42).toRawBits(), writer.addFloatArray(floats.copyOf()).toRawBits())
    assertEquals(Utils.asNan(43).toRawBits(), writer.addIdList(ids).toRawBits())
    assertEquals(Utils.asNan(43).toRawBits(), writer.addIdList(ids.copyOf()).toRawBits())
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun expressionStateOperationsMatchAndroidxCore() {
    val expression = floatArrayOf(2f, 3f, Utils.asNan(0x1001))
    val animation = floatArrayOf(300f, 1f)
    val integers = longArrayOf(7, 42L + 0x100000000L, 0x102)
    val matrix = floatArrayOf(1f, 0f, Utils.asNan(8))
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addAnimatedFloat(42, expression, animation)
    expected.addIntegerExpression(43, 2, intArrayOf(7, 42, 0x102))
    expected.timeAttribute(44, 9, 6, 1, 2)
    expected.addMatrixExpression(45, matrix)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(Utils.asNan(42).toRawBits(), writer.floatExpression(expression, animation).toRawBits())
    assertEquals(43L + 0x100000000L, writer.integerExpression(*integers))
    assertEquals(Utils.asNan(44).toRawBits(), writer.timeAttribute(9, 6, 1, 2).toRawBits())
    assertEquals(Utils.asNan(45).toRawBits(), writer.matrixExpression(*matrix).toRawBits())
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun textStateOperationsMatchAndroidxCore() {
    val arrayId = Utils.asNan(7)
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.idLookup(42, arrayId, 2.5f)
    expected.textLookup(43, arrayId, 3.5f)
    expected.textLookup(44, arrayId, 11)
    expected.textLength(45, 20)
    expected.textMerge(46, 20, 21)
    expected.textSubtext(47, 20, 1f, 4f)
    expected.textTransform(48, 20, 0f, -1f, 2)
    expected.createTextFromFloat(49, Utils.asNan(5), 3, 2, 1)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(42, writer.idLookup(arrayId, 2.5f))
    assertEquals(43, writer.textLookup(arrayId, 3.5f))
    assertEquals(44, writer.textLookup(arrayId, 11))
    assertEquals(Utils.asNan(45).toRawBits(), writer.textLength(20).toRawBits())
    assertEquals(46, writer.textMerge(20, 21))
    assertEquals(47, writer.textSubtext(20, 1f, 4f))
    assertEquals(48, writer.textTransform(20, 0f, -1f, 2))
    assertEquals(49, writer.createTextFromFloat(Utils.asNan(5), 3, 2, 1))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun attributeAndDynamicArrayOperationsMatchAndroidxCore() {
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addDynamicFloatArray(42, 6f)
    expected.setArrayValue(42, 2f, 9f)
    expected.getColorAttribute(43, 10, 3)
    expected.bitmapAttribute(44, 11, 1)
    expected.bitmapTextMeasure(45, 12, 13, 0, 0.5f)

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(Utils.asNan(42).toRawBits(), writer.addDynamicFloatArray(6f).toRawBits())
    writer.setArrayValue(42, 2f, 9f)
    assertEquals(Utils.asNan(43).toRawBits(), writer.colorAttribute(10, 3).toRawBits())
    assertEquals(Utils.asNan(44).toRawBits(), writer.bitmapAttribute(11, 1).toRawBits())
    assertEquals(Utils.asNan(45).toRawBits(), writer.bitmapTextMeasure(12, 13, 0, 0.5f).toRawBits())
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun bitmapDeclarationsMatchAndroidxCore() {
    val coreGlyph =
      androidx.compose.remote.core.operations.BitmapFontData.Glyph(
        "fi",
        9,
        1,
        2,
        3,
        4,
        10,
        12,
      )
    val glyph = BitmapFontGlyph("fi", 9, 1, 2, 3, 4, 10, 12)
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.storeBitmapUrl(42, "https://example.test/image.png", 1, 1)
    expected.setNamedVariable(
      42,
      "USER:image",
      androidx.compose.remote.core.operations.NamedVariable.IMAGE_TYPE,
    )
    expected.createBitmap(43, 20, 30)
    expected.addBitmapFont(44, arrayOf(coreGlyph), linkedMapOf("fifi" to 1.toShort()))

    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    assertEquals(42, writer.addBitmapUrl("https://example.test/image.png"))
    assertEquals(42, writer.addNamedBitmapUrl("USER:image", "https://example.test/image.png"))
    assertEquals(43, writer.createBitmap(20, 30))
    assertEquals(44, writer.addBitmapFont(listOf(glyph), linkedMapOf("fifi" to 1.toShort())))
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }

  @Test
  fun paintBundleMatchesAndroidxCore() {
    val corePaint = androidx.compose.remote.core.operations.paint.PaintBundle()
    corePaint.setColorId(11)
    corePaint.setStrokeWidth(Utils.asNan(7))
    corePaint.setStrokeCap(2)
    corePaint.setStyle(1)
    corePaint.setBlendMode(3)
    corePaint.setAntiAlias(true)
    corePaint.setTextStyle(2, 600, true)
    corePaint.setPathEffect(floatArrayOf(2f, Utils.asNan(8)))
    corePaint.setLinearGradient(
      intArrayOf(0xff000000.toInt(), 12),
      2,
      floatArrayOf(0f, 1f),
      1f,
      2f,
      3f,
      4f,
      0,
    )
    val expected = androidx.compose.remote.core.RemoteComposeBuffer()
    expected.addPaint(corePaint)

    val paint =
      PaintBundleData()
        .setColorId(11)
        .setStrokeWidth(Utils.asNan(7))
        .setStrokeCap(2)
        .setStyle(1)
        .setBlendMode(3)
        .setAntiAlias(true)
        .setTextStyle(2, 600, true)
        .setPathEffect(floatArrayOf(2f, Utils.asNan(8)))
        .setLinearGradient(
          intArrayOf(0xff000000.toInt(), 12),
          2,
          floatArrayOf(0f, 1f),
          1f,
          2f,
          3f,
          4f,
          0,
        )
    val writer = RemoteDocumentWriter(192, 192)
    val headerSize = writer.encodeToByteArray().size
    writer.applyPaint(paint)
    val bytes = writer.encodeToByteArray()

    assertContentEquals(expected.buffer.cloneBytes(), bytes.copyOfRange(headerSize, bytes.size))
  }
}
