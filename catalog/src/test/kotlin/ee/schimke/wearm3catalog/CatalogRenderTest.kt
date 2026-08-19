package ee.schimke.wearm3catalog

import java.io.File
import java.util.zip.InflaterInputStream
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fails the build for a sticker that publishes an **empty frame**.
 *
 * This is the failure mode nothing else here catches. The build is green, discovery finds the
 * preview, the render succeeds, the bundle publishes — and the card is blank. Review does not catch
 * it either, because on a dark-first catalog a sticker that drew nothing looks like a sticker that
 * drew something dark.
 *
 * It is not hypothetical. Both page indicators and the scroll indicator shipped as entirely black
 * 454x454 captures until `Modifier.align` was added: the alignment the Wear docs ask for is what
 * makes them lay out at all, and without it they collapse to nothing.
 *
 * The renders come from `composePreviewRender`, which `renderBeforeUnitTests` runs first (see
 * `build.gradle.kts`), so this reads the same PNGs CI publishes rather than a fixture.
 */
class CatalogRenderTest {

  private val renders = File("build/compose-previews/renders")

  /**
   * The threshold is deliberately near zero rather than tuned. This asserts a sticker drew
   * *something*, not that it drew the right thing — a real lower bound is a per-component judgement
   * and would need re-tuning on every legitimate change. The two-page indicator, the sparsest real
   * sticker, is around 0.001; a blank capture is exactly 0.
   */
  private val minimumVisibleFraction = 0.0002

  /**
   * A capture that failed writes `<id>.png.error.json` INSTEAD of the PNG, so the blank check below
   * cannot see it — there is no file to measure. That is how a broken `@InteractionPreview` cost
   * two components their ordinary stills while every other check stayed green.
   */
  @Test
  fun `no render failed`() {
    val errors =
      renders.listFiles { f: File -> f.name.endsWith(".error.json") }.orEmpty().map { it.name }
    assertTrue(
      "these captures failed and wrote no image:\n" + errors.joinToString("\n") { "  $it" },
      errors.isEmpty(),
    )
  }

  /**
   * A motion capture that records nothing is worse than none: it publishes a play button over a
   * still. `Motion.kt` states what each recording is supposed to show, and this is what holds it to
   * that — the frame payloads are compared, so a GIF of one repeated frame fails.
   *
   * The bar is deliberately low (a handful of distinct frames, not a tuned count per recording).
   * Real recordings here land at 15-46 distinct frames of 46; the ones this caught were at 3 and 4.
   */
  @Test
  fun `every motion capture actually moves`() {
    val gifs = renders.listFiles { f: File -> f.name.endsWith(".gif") }.orEmpty()
    val still =
      gifs
        .map { it to distinctFrames(it) }
        .filter { (_, distinct) -> distinct < 6 }
        .map { (file, distinct) -> "${file.name} ($distinct distinct frames)" }
    assertTrue(
      "these recordings barely move — either the component does not animate under this renderer, " +
        "or the capture window misses the part that does:\n" +
        still.joinToString("\n") { "  $it" },
      still.isEmpty(),
    )
  }

  /**
   * Distinct frame payloads in a GIF, split on the per-frame graphic control extension. Comparing
   * the compressed bytes is enough to answer "did the pixels change" without decoding LZW.
   */
  private fun distinctFrames(file: File): Int {
    val marker = byteArrayOf(0x21, 0xF9.toByte(), 0x04)
    val bytes = file.readBytes()
    val starts =
      bytes.indices.filter { i -> marker.indices.all { bytes.getOrNull(i + it) == marker[it] } }
    return starts
      .mapIndexed { index, start ->
        val end = starts.getOrNull(index + 1) ?: bytes.size
        bytes.copyOfRange(start, end).toList()
      }
      .toSet()
      .size
  }

  @Test
  fun `no sticker publishes an empty frame`() {
    val pngs = renders.listFiles { f: File -> f.name.endsWith(".png") }.orEmpty()
    assertTrue(
      "no renders under ${renders.absolutePath} — is renderBeforeUnitTests still set?",
      pngs.isNotEmpty(),
    )
    val blank =
      pngs
        .map { it to visibleFraction(it) }
        .filter { (_, fraction) -> fraction < minimumVisibleFraction }
        .map { (file, fraction) -> "${file.name} (${"%.5f".format(fraction)})" }
    assertTrue(
      "these renders are blank or all but blank — a component that draws nothing usually means a " +
        "missing Modifier.align, an unsettled animation, or a state the sticker forgot to seed:\n" +
        blank.joinToString("\n") { "  $it" },
      blank.isEmpty(),
    )
  }

  /** The fraction of pixels that are visible and not near-black. */
  private fun visibleFraction(file: File): Double {
    val png = Png.read(file)
    var visible = 0
    for (index in 0 until png.width * png.height) {
      val offset = index * png.channels
      val alpha = if (png.channels == 4) png.pixels[offset + 3].toInt() and 0xFF else 255
      if (alpha <= 8) continue
      val sum =
        (png.pixels[offset].toInt() and 0xFF) +
          (png.pixels[offset + 1].toInt() and 0xFF) +
          (png.pixels[offset + 2].toInt() and 0xFF)
      if (sum > 90) visible++
    }
    return visible.toDouble() / (png.width * png.height)
  }
}

/**
 * The smallest PNG reader that answers "did anything get drawn": IHDR for the geometry, the IDAT
 * stream inflated and un-filtered. No decoder dependency for a question this shallow, and Android's
 * `BitmapFactory` is a stub on the unit-test classpath.
 */
private class Png(val width: Int, val height: Int, val channels: Int, val pixels: ByteArray) {
  companion object {
    fun read(file: File): Png {
      val data = file.readBytes()
      var pos = 8
      var width = 0
      var height = 0
      var colorType = 0
      val idat = java.io.ByteArrayOutputStream()
      while (pos + 8 <= data.size) {
        val length = intAt(data, pos)
        val type = String(data, pos + 4, 4, Charsets.US_ASCII)
        when (type) {
          "IHDR" -> {
            width = intAt(data, pos + 8)
            height = intAt(data, pos + 12)
            colorType = data[pos + 8 + 9].toInt()
          }
          "IDAT" -> idat.write(data, pos + 8, length)
        }
        pos += 12 + length
      }
      val channels =
        when (colorType) {
          0,
          3 -> 1
          2 -> 3
          4 -> 2
          else -> 4
        }
      val raw = InflaterInputStream(idat.toByteArray().inputStream()).readBytes()
      return Png(width, height, channels, unfilter(raw, width, height, channels))
    }

    private fun intAt(data: ByteArray, at: Int): Int =
      ((data[at].toInt() and 0xFF) shl 24) or
        ((data[at + 1].toInt() and 0xFF) shl 16) or
        ((data[at + 2].toInt() and 0xFF) shl 8) or
        (data[at + 3].toInt() and 0xFF)

    private fun unfilter(raw: ByteArray, width: Int, height: Int, channels: Int): ByteArray {
      val stride = width * channels
      val out = ByteArray(stride * height)
      var read = 0
      val previous = ByteArray(stride)
      for (row in 0 until height) {
        val filter = raw[read].toInt() and 0xFF
        read++
        val line = raw.copyOfRange(read, read + stride)
        read += stride
        for (x in 0 until stride) {
          val a = if (x >= channels) line[x - channels].toInt() and 0xFF else 0
          val b = previous[x].toInt() and 0xFF
          val c = if (x >= channels) previous[x - channels].toInt() and 0xFF else 0
          val value = line[x].toInt() and 0xFF
          line[x] =
            when (filter) {
              1 -> (value + a)
              2 -> (value + b)
              3 -> (value + (a + b) / 2)
              4 -> value + paeth(a, b, c)
              else -> value
            }.toByte()
        }
        line.copyInto(out, row * stride)
        line.copyInto(previous)
      }
      return out
    }

    private fun paeth(a: Int, b: Int, c: Int): Int {
      val p = a + b - c
      val pa = Math.abs(p - a)
      val pb = Math.abs(p - b)
      val pc = Math.abs(p - c)
      return if (pa <= pb && pa <= pc) a else if (pb <= pc) b else c
    }
  }
}
