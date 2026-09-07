package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import javax.imageio.ImageIO
import org.junit.Test

/**
 * Guards that no sticker in this sheet bakes to a **fully transparent** PNG.
 *
 * Why it needs its own test rather than being caught downstream: every parity lane that scores this
 * catalog flattens the baked capture and the player's render onto the same mid-grey before diffing
 * (the stickers are light content on transparency, so compositing over white would let them read as
 * identical to an empty canvas). A blank *reference* therefore flattens to the same pixels as a
 * player that drew nothing — the comparison is vacuous, and it scores a perfect 0.00%. `rc-compare`
 * now refuses to score such a row (#2933), which stops the false green, but that is the differ's
 * end: it reports the row as unscored and moves on. Nothing fails, and the catalog keeps shipping a
 * sticker with no pixels in it.
 *
 * This is that missing end. It fails the build instead, at the point where the capture is produced
 * — which is also the only place that can tell "this preview draws nothing" from "this preview was
 * never rendered".
 *
 * Alpha > 8 is the same threshold the differ uses for "opaque" (see
 * `scripts/design-artifacts/rc-compare-pixels.mjs`), so the two agree on what blank means.
 *
 * Deliberately *not* a minimum-coverage assertion: several stickers are legitimately sparse — a
 * one-word outlined button, a hairline card border, a single icon on a 200dp canvas all land under
 * 1% — and a floor tight enough to catch anything real would fail them. "Something was drawn" is
 * the invariant that holds for every sticker in the sheet.
 */
class StickerBakeCoverageTest {

  private val rendersDir = File("build/compose-previews/renders")

  /** Alpha above which a pixel counts as drawn; matches `rc-compare-pixels.mjs`. */
  private val opaqueAlpha = 8

  @Test
  fun `the render produced captures to check`() {
    // Without this the coverage test below would pass vacuously if the render stopped emitting PNGs
    // (the chained `renderBeforeUnitTests` in build.gradle.kts is what puts them here).
    assertThat(rendersDir.listFiles { f -> f.name.endsWith(".png") }.orEmpty()).isNotEmpty()
  }

  /**
   * The captures that are **expected** to bake transparent, each one a gap outside this catalog
   * rather than a mistake in it — and each asserted in BOTH directions below.
   *
   * **This map is empty of component gaps now, and how it emptied is the point of the mechanism.**
   * It used to carry all twelve `Disabled=Yes` cells of the kit's `Text-Button` set on the reading
   * that `RemoteTextButton(enabled = false)` "draws nothing at all on the alpha surface" — tracked
   * as [#130](https://github.com/yschimke/wear-m3-catalog/issues/130), with
   * [#91](https://github.com/yschimke/wear-m3-catalog/issues/91) one component over as its sibling.
   * That reading was wrong in an instructive way: the gap was never in `remote-material3` at all.
   * It was in the **player the capture bakes through**. `RemoteOverridablePreview` defaults to the
   * embedded player, so a baked PNG is one interpreter's opinion, and the AOSP View player, the JS
   * player and the CMP player all drew these buttons correctly from the identical `.rc` bytes the
   * whole time. Three separate causes in the embedded player, all fixed in rc-players 1.59.2
   * ([rc-players#47](https://github.com/yschimke/rc-players/issues/47),
   * [#50](https://github.com/yschimke/rc-players/issues/50)): a store write that was never
   * mirrored, text drawing a colour it had resolved once, and a computed-op index that did not walk
   * a component's canvas stream — which left the labels being drawn, in a fully transparent colour.
   *
   * The SNAPSHOT lane's own entry went the same way on the same bump. `RemoteCheckboxButton(checked
   * = false, enabled = false)` was recorded here as drawing nothing at all — not the container, not
   * either label, not the checkbox — with the split row one function over offered as proof that the
   * plain row's disabled path was the gap. It was the same player, and the same three causes: on
   * 1.59.2 that cell bakes 33449 drawn pixels against the enabled row's 33540, which is the whole
   * row. The lane branch went with it, so this map is now unconditionally empty.
   *
   * So all thirteen entries are deleted rather than retargeted, and the cells stand as ordinary
   * comparisons. That is exactly what the second assertion below was built to force: it fails the
   * day a known-blank capture stops being blank, which is how this gap announced its own closure
   * instead of quietly persisting as an exemption nobody rechecked.
   *
   * **The lesson worth keeping when the next entry is added.** An entry here is never a way to
   * quiet a sticker that is simply broken, and it is not enough for it to name a tracked issue: it
   * has to name a cause that was actually *established*. A blank capture says the pipeline drew
   * nothing, not who failed to draw it, and the cheapest way to tell a library gap from a player
   * gap is to render the same document on a second player before blaming the first.
   * `remote-snapshot-probe.yml` re-checks these against the newest androidx.dev build weekly, which
   * catches a library that starts drawing — it could not have caught this, because the library was
   * never the one at fault.
   */
  private val knownBlank = emptyMap<String, String>()

  /**
   * `<stem>_VARIANT_<cell>`, the identity a [knownBlank] entry names, or null for a base render.
   *
   * **The name can arrive TRUNCATED**, which is why [matches] compares by prefix rather than by
   * equality. The renderer caps a capture's filename, so `…_VARIANT_filled_variant_large_disabled`
   * reaches disk as `…_VARIANT_filled_variant_large_d`. Keying the map on what survived would put
   * `filled-variant-large-d` in the source, which reads as a typo and breaks the moment the cap
   * moves; keying it on the cell's real name and matching forwards keeps the map readable.
   */
  private fun blankKey(file: File): String? {
    val stem = file.name.substringBefore("_width")
    val cell = file.name.substringAfter("_VARIANT_", "").substringBeforeLast("-")
    return if (cell.isEmpty()) null else "${stem}_VARIANT_${cell.replace('_', '-')}"
  }

  private fun File.hasDrawnPixel(): Boolean {
    val image = ImageIO.read(this)
    return (0 until image.height).any { y ->
      (0 until image.width).any { x -> (image.getRGB(x, y) ushr 24) > opaqueAlpha }
    }
  }

  @Test
  fun `every baked sticker carries at least one drawn pixel`() {
    val blank =
      rendersDir
        .listFiles { f -> f.name.endsWith(".png") }
        .orEmpty()
        .sorted()
        .filterNot { it.hasDrawnPixel() }
        .filter { blankKey(it) !in knownBlank }
        .map { it.name }
    assertWithMessage(
        "these stickers baked to a fully transparent PNG — nothing was drawn. If the LIBRARY " +
          "draws nothing for the state the cell seeds, publish the cell and record it in " +
          "`knownBlank` with the call that does it, rather than withdrawing it silently"
      )
      .that(blank)
      .isEmpty()
  }

  /**
   * The other direction, and the reason a known gap is published rather than withdrawn: when the
   * alpha line learns to draw a disabled text button, this fails, and the fix is to delete the
   * entry and let the cell stand as an ordinary comparison. Without it the gap would close in
   * silence and the sheet would keep an exemption it no longer needs.
   */
  @Test
  fun `every known-blank sticker is still blank`() {
    val pngs = rendersDir.listFiles { f -> f.name.endsWith(".png") }.orEmpty()
    val byKey = pngs.mapNotNull { png -> blankKey(png)?.let { it to png } }.toMap()

    val stale = knownBlank.keys.filterNot { it in byKey }
    assertWithMessage("these `knownBlank` entries name no render — a stale exemption hides a blank")
      .that(stale)
      .isEmpty()

    val fixed = knownBlank.keys.filter { byKey.getValue(it).hasDrawnPixel() }
    assertWithMessage(
        "these captures are no longer blank — the library gap they record has been FIXED. Drop " +
          "the entry from `knownBlank` and let the cell stand as an ordinary comparison: " +
          fixed.joinToString { "$it (was: ${knownBlank[it]})" }
      )
      .that(fixed)
      .isEmpty()
  }
}
