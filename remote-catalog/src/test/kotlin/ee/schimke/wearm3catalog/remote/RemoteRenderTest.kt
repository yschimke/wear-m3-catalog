package ee.schimke.wearm3catalog.remote

import java.io.File
import java.util.zip.InflaterInputStream
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The three render guards `:catalog` has carried since its own cells arrived, ported to the Remote
 * sheet — a **blank** capture, a capture that is byte-identical to another render of the same
 * component, and a capture that failed and wrote no image at all.
 *
 * `:remote-catalog` had none of them, and #116 is the record of what that cost. Three defects
 * reached `main` or came within a push of it, each one a cell scored against a kit node while
 * drawing something else, and each one caught by hand rather than by the build:
 *
 * * `Button/Icon-ExtraSmall` published the SMALL render under the extra-small name — the child
 *   style draws no container, so both sizes clamp to the same glyph. Byte-identical, and pointed at
 *   two different kit nodes (#125).
 * * `Progress/Circular`'s `disabled` cell declared its seed and nothing read it, so the disabled
 *   render was the enabled picture scored against the kit's `Disabled=Yes` node.
 * * Six `Button/Text` disabled cells came out fully transparent: `RemoteTextButton(enabled=false)`
 *   draws nothing at all on the alpha surface.
 *
 * The Wear sibling's `CatalogRenderTest` states the general case: a cell that renders identically
 * to the render it varies is a **wrong picture that renders green**, and the sheet then publishes
 * one image twice under two names. The same is true of a blank one, and worse on a dark-first
 * catalog — a sticker that drew nothing looks like a sticker that drew something dark.
 *
 * The renders come from `composePreviewRender`, which `renderBeforeUnitTests` runs first (see
 * `build.gradle.kts`), so this reads the same PNGs CI publishes rather than a fixture.
 *
 * The `Png` reader below is copied from `:catalog`'s test rather than shared: the two modules are
 * on different dependency lines on purpose (AGENTS.md → "the alpha line stays in
 * `:remote-catalog`"), so there is no common test source set to put it in, and a reader this small
 * is cheaper to duplicate than a module to bridge them.
 */
class RemoteRenderTest {

  private val renders = File("build/compose-previews/renders")

  /**
   * Near zero rather than tuned, exactly as `:catalog` sets it: this asserts a sticker drew
   * *something*, not that it drew the right thing. It has to be low here — these stickers rasterise
   * onto transparency and several are a single thin ring or a line of body copy — so it is set from
   * the sparsest real capture on this sheet rather than borrowed from the Wear one.
   */
  private val minimumVisibleFraction = 0.0002

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
   * Deliberately compares only renders of the SAME component. Two different components may
   * legitimately look alike at this size; two renders of one component may not — that is a cell
   * varying nothing.
   */
  @Test
  fun `no two renders of a component are identical`() {
    val byComponent =
      renders
        .listFiles { f: File -> f.name.endsWith(".png") }
        .orEmpty()
        .groupBy { it.name.substringBefore("_VARIANT_").substringBeforeLast("-") }
    val duplicates = byComponent.mapNotNull { (component, files) ->
      val seen = mutableMapOf<String, String>()
      val clashes = files.mapNotNull { file ->
        val digest = file.readBytes().toList().hashCode().toString()
        val first = seen.put(digest, file.name)
        if (first == null) null else "${file.name} == $first"
      }
      if (clashes.isEmpty()) null else "$component: ${clashes.joinToString(", ")}"
    }
    assertTrue(
      "these renders of one component are byte-identical — a cell that varies nothing publishes " +
        "the same picture twice, under two names and against two kit nodes:\n" +
        duplicates.joinToString("\n") { "  $it" },
      duplicates.isEmpty(),
    )
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
      "these renders are blank or all but blank — on this sheet that usually means the library " +
        "draws nothing for the state the cell seeds, which is a gap to state rather than a cell " +
        "to publish:\n" +
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
