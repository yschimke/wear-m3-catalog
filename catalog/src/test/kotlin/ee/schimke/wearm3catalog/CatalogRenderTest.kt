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
   * The screen sizes a full-screen sticker renders at, which is what puts a `_<n>dp` into its
   * render name and therefore into every key of the two maps below — the kit's own five
   * (`.WatchPuck`, see AGENTS.md). A component cell has no such suffix and its keys carry none.
   *
   * Declared before those maps on purpose: a Kotlin property initialiser reading one declared later
   * gets null, and the maps would silently come out keyed `null`.
   */
  private val screenSizes = listOf(192, 204, 216, 225, 240)

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

  /**
   * The cell pairs that are **expected** to render identically, each one a distinction the KIT
   * draws and the LIBRARY does not — the Wear-side twin of `RemoteRenderTest.knownDuplicate`, added
   * for the same reason ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
   *
   * Without it the only way past the test below was to withdraw the cell, and withdrawing is what
   * makes the collapse invisible: 30 published kit cells were absent from this sheet on that
   * ground, which reads exactly like nobody having drawn them. Publishing them puts the collapse
   * where a reader meets it, lets design-parity score it against the node the kit drew, and makes
   * the fix self-announcing through [every known duplicate still collapses].
   *
   * **An entry here is never a way to quiet a cell that is simply wrong.** A cell whose seed
   * nothing reads is what the test exists to catch, and that is a bug in the sources rather than in
   * the library. The test for the difference: if passing the knob through correctly would separate
   * the pictures, it is a bug here; if the library draws one picture for both states however it is
   * called, it belongs here.
   *
   * Keys are `<component>: <cell> == <cell>`, exactly as the failure below prints them — the cell
   * that repeats on the left, the plainer cell it repeats on the right.
   */
  private val knownDuplicate: Map<String, String> = buildMap {
    // Wear resolves all three filled styles' disabled colours to the same `onSurface` pair — 12%
    // container, 38% content — so `filledVariantButtonColors()` and `filledTonalButtonColors()`
    // hand a disabled button exactly what `buttonColors()` does. The kit publishes a disabled
    // cell for each style; the library draws one picture for the three. `Outline` DOES differ,
    // and its cells are ordinary comparisons.
    val filledStylesCollapse =
      "a disabled button takes the same onSurface pair whichever filled style's colours it was " +
        "given, so this is the disabled FILLED render under another name"
    for (cell in listOf("disabled", "icon_only_disabled", "text_only_disabled")) {
      put("CompactActionButton: filled_variant_$cell == $cell", filledStylesCollapse)
      put("CompactActionButton: tonal_$cell == $cell", filledStylesCollapse)
    }
    for (type in listOf("", "icon_")) {
      for (size in listOf("", "extra_small_", "medium_", "large_")) {
        val cell = "$type${size}disabled"
        put("ScreenEdgeButton: filled_variant_$cell == $cell", filledStylesCollapse)
        put("ScreenEdgeButton: tonal_$cell == $cell", filledStylesCollapse)
      }
    }
    // `TextButtonDefaults` resolves the same three the same way.
    for (cell in listOf("disabled", "small_disabled", "large_disabled")) {
      put("TextAction: filled_variant_$cell == $cell", filledStylesCollapse)
      put("TextAction: tonal_$cell == $cell", filledStylesCollapse)
    }
    // `IconButtonDefaults.ExtraSmallButtonSize` is a CONTAINER token, and the child style draws
    // no container: both sizes clamp to the same minimum touch target around an unchanged glyph.
    val childSizeCollapse =
      "IconButtonDefaults.ExtraSmallButtonSize sizes a container this style does not draw, so " +
        "it clamps to the same minimum touch target as SmallButtonSize"
    put("StandardIconAction: extra_small == small", childSizeCollapse)
    put("StandardIconAction: extra_small_disabled == small_disabled", childSizeCollapse)
    // A disabled stepper draws no button container at all, so `buttonFill=false` — which is
    // `StepperDefaults.colors(buttonContainerColor = Transparent)` — has nothing left to turn off.
    val stepperFillCollapse =
      "a disabled Stepper draws no button container, so buttonContainerColor = Transparent " +
        "removes nothing and this is the plain disabled render"
    for (screen in screenSizes) {
      put("ValueStepper_${screen}dp: no_button_fill_disabled == disabled", stepperFillCollapse)
      put(
        "ValueStepper_${screen}dp: icon_no_button_fill_disabled == icon_disabled",
        stepperFillCollapse,
      )
    }
    // `Fixed Width=False` at the DEFAULT size only: the label the kit draws these cells with is
    // as wide as the box the size names, so hugging lands on the same 52dp square. The kit's own
    // two nodes export identically for the same reason (`39083:776` and `39083:767` are both 52×52)
    // — and `hug_large` / `hug_extra_large` do differ from their fixed twins, so the knob works.
    // `Title Card 3` CROSSED WITH IMAGERY, which Compose cannot tell from `Title Card 2` crossed
    // with imagery. The kit's third layout is a title, a subtitle and the timestamp UNDER it, with
    // no body — and `TitleCard` draws `time` on the title's row however it is called, so the only
    // thing separating this sheet's `title-and-subtitle` cells from its `with-subtitle` ones is the
    // body text. Replace that body with an image or a gallery and there is nothing left to differ
    // by. The `Text` crossings of both layouts DO differ, and they are ordinary comparisons.
    //
    // Published rather than withheld, on this file's own rule: the collapse is the finding, and
    // `:remote-catalog` publishes all eight of these cells, so withdrawing them here is what left
    // its rows pairing against `background-image`
    // ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292)).
    val titleCardThreeCollapse =
      "TitleCard draws its time slot on the title's row whatever the layout, so Title Card 3 " +
        "differs from Title Card 2 only by its missing body — and an image or gallery cell has no " +
        "body to miss"
    for (content in listOf("content_image", "gallery_1", "gallery_2")) {
      put(
        "TitledCard: title_and_subtitle_$content == with_subtitle_$content",
        titleCardThreeCollapse,
      )
      put(
        "TitledCard: title_and_subtitle_outlined_$content == with_subtitle_outlined_$content",
        titleCardThreeCollapse,
      )
    }
    put(
      "TextToggle: hug == base",
      "the label is as wide as TextToggleButtonDefaults.Size, so hugging lands on the same box — " +
        "the kit's own Fixed Width cells export identically at this size for the same reason",
    )
  }

  /**
   * A cell that renders identically to the render it varies is a **wrong picture that renders
   * green**: the sheet publishes the same image twice under two names, and the second one claims to
   * show something it does not.
   *
   * It happens whenever a seed is declared but not read — an `@OverrideVariant` whose knob nothing
   * consumes — and whenever the thing being varied cannot show itself in a still, which is how a
   * scaffold's `edge-button` cell came out byte-identical to its default (the edge button is
   * revealed by scroll position, so at rest there is nothing to see).
   *
   * Deliberately compares only renders of the SAME component. Two different components may
   * legitimately look alike at this size; two renders of one component may not — unless
   * [knownDuplicate] says the library draws one picture for both.
   */
  @Test
  fun `no two renders of a component are identical`() {
    val unexplained = clashes().keys - knownDuplicate.keys
    assertTrue(
      "these renders of one component are byte-identical — a cell that varies nothing publishes " +
        "the same picture twice, under two names and against two kit nodes. If the LIBRARY draws " +
        "one picture for both states however it is called, publish the cell and record it in " +
        "`knownDuplicate` with the call that collapses it, rather than withdrawing it:\n" +
        unexplained.sorted().joinToString("\n") { "  $it" },
      unexplained.isEmpty(),
    )
  }

  /**
   * The other direction, and the reason a known collapse is published rather than withdrawn: when
   * the library learns to tell the two states apart, this fails, and the fix is to delete the entry
   * and let the cells stand as ordinary comparisons. Without it the gap would close in silence and
   * the sheet would keep an exemption it no longer needs.
   */
  @Test
  fun `every known duplicate still collapses`() {
    val separated = knownDuplicate.keys - clashes().keys
    assertTrue(
      "these pairs no longer render alike — the library collapse they record has been FIXED. Drop " +
        "the entry from `knownDuplicate` and let the cells stand as ordinary comparisons:\n" +
        separated.sorted().joinToString("\n") { "  $it (was: ${knownDuplicate[it]})" },
      separated.isEmpty(),
    )
  }

  /**
   * Every byte-identical pair among the renders, keyed `<component>: <cell> == <cell>`.
   *
   * Read the component's own render first and then the shortest cell name, so the pair is the same
   * on every machine AND the cell on the right is the plainer of the two — `filled_variant_disabled
   * == disabled`, never the other way round. Which the filesystem happened to list first decides
   * nothing. `base` leads on its own term rather than on length, because a cell can be spelled
   * shorter than it (`hug`) without being plainer than it.
   */
  private fun clashes(): Map<String, String> {
    val byComponent =
      renders
        .listFiles { f: File -> f.name.endsWith(".png") }
        .orEmpty()
        .sortedWith(
          compareBy({ cellOf(it.name) != "base" }, { cellOf(it.name).length }, { cellOf(it.name) })
        )
        .groupBy { componentOf(it.name) }
    return buildMap {
      for ((component, files) in byComponent) {
        val seen = mutableMapOf<Int, String>()
        for (file in files) {
          val digest = file.readBytes().toList().hashCode()
          // `putIfAbsent`, not `put`: three cells carrying one picture all name the FIRST of them,
          // so a three-way collapse reads as two pairs against the plain cell rather than a chain.
          val first = seen.putIfAbsent(digest, cellOf(file.name))
          if (first != null) put("$component: ${cellOf(file.name)} == $first", file.name)
        }
      }
    }
  }

  /** The composable a render belongs to: `ScreenEdgeButton_VARIANT_tonal-1a2b3c4d.png`. */
  private fun componentOf(name: String): String =
    name.substringBefore("_VARIANT_").substringBeforeLast("-")

  /** The `@OverrideVariant` a render belongs to, or `base` for the component's own render. */
  private fun cellOf(name: String): String =
    if ("_VARIANT_" in name) name.substringAfter("_VARIANT_").substringBeforeLast("-") else "base"

  @Test
  fun `no sticker publishes an empty frame`() {
    val pngs = renders.listFiles { f: File -> f.name.endsWith(".png") }.orEmpty()
    assertTrue(
      "no renders under ${renders.absolutePath} — is renderBeforeUnitTests still set?",
      pngs.isNotEmpty(),
    )
    val blank = blanks().map { (cell, fraction) -> "$cell ($fraction)" }
    assertTrue(
      "these renders are blank or all but blank — a component that draws nothing usually means a " +
        "missing Modifier.align, an unsettled animation, or a state the sticker forgot to seed:\n" +
        blank.sorted().joinToString("\n") { "  $it" },
      blank.isEmpty(),
    )
  }

  /** Every render that draws nothing, keyed `<component>: <cell>` with its visible fraction. */
  private fun blanks(): Map<String, String> =
    renders
      .listFiles { f: File -> f.name.endsWith(".png") }
      .orEmpty()
      .map { it to visibleFraction(it) }
      .filter { (_, fraction) -> fraction < minimumVisibleFraction }
      .associate { (file, fraction) ->
        "${componentOf(file.name)}: ${cellOf(file.name)}" to "%.5f".format(fraction)
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
