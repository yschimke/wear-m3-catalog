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
}
