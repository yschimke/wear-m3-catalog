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

  /**
   * Blanks that only exist on the SNAPSHOT lane, because the component that bakes them only exists
   * there — see `src/snapshot/kotlin/…/SelectionPreviews.kt`.
   *
   * `RemoteCheckboxButton` arrived after 1.0.0-alpha10, so `CheckboxRowRemote` is the first
   * selection row this sheet has ever drawn. Seven of its eight cells bake something; the eighth —
   * unchecked AND disabled AND not split — bakes fully transparent. Both state halves are needed to
   * reach it: `unselected` (unchecked, enabled) is a whole row, and `disabled` (checked, disabled)
   * still draws its checkmark, so it is the crossing rather than either knob.
   *
   * THE SPLIT CELLS ARE WHAT MAKE THIS A LIBRARY BUG RATHER THAN A SUSPICION.
   * `RemoteSplitCheckboxButton(checked = false, enabled = false)` — the same two knobs, the same
   * kit set, one function over — draws its containers and its checkbox perfectly well. So the blank
   * is specific to the plain row's disabled path rather than to anything this sticker asks for, and
   * there is no argument from this call site that would change it.
   *
   * `disabled` is worth looking at beside it: it keeps the checkmark and loses the container and
   * both labels, which is the same shape of gap one component over
   * ([#91](https://github.com/yschimke/wear-m3-catalog/issues/91)) and not something this sheet can
   * reach from the call site. It is not listed here because it is not blank, and this test only
   * owns the blanks.
   *
   * Published rather than withdrawn, for the reason the KDoc above gives: the kit publishes this
   * cell, and a sheet that quietly skips it reads as a sheet nobody got to. The entry is scoped to
   * the lane because on the released alphas there is no render here at all, and `every known-blank
   * sticker is still blank` fails a `knownBlank` key that names no render — correctly, since a
   * stale exemption hides a blank.
   */
  private val SNAPSHOT_LANE_BLANKS =
    mapOf(
      "CheckboxRowRemote_VARIANT_unselected-disabled" to
        "RemoteCheckboxButton(checked = false, enabled = false) draws nothing at all — not the " +
          "container, not either label, not the checkbox. Either knob alone renders; only the " +
          "crossing is blank"
    )

  /**
   * One reason, twelve cells: the whole `Disabled=Yes` column of the kit's `Text-Button` set, none
   * of which `remote-material3` draws anything for.
   */
  private val REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED =
    "#130 — RemoteTextButton(enabled = false) draws nothing at all: no container, no label, and " +
      "passing disabledContainerColor / disabledContentColor explicitly does not change it"

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
   * The captures that are **expected** to bake transparent, each one a published gap in the library
   * rather than a mistake in this catalog — and each asserted in BOTH directions below.
   *
   * `RemoteTextButton(enabled = false)` draws nothing at all on the alpha surface: no container, no
   * label. The kit publishes fifteen `Disabled=Yes` cells for the `Text-Button` set and this
   * rendition draws none of them. Tracked as
   * [#130](https://github.com/yschimke/wear-m3-catalog/issues/130), the sibling of
   * [#91](https://github.com/yschimke/wear-m3-catalog/issues/91) one component over: there
   * `RemoteButton` resolves its disabled container and loses the label; here neither resolves.
   * `RemoteIconButton` resolves both, which is what makes it the library rather than this sheet.
   *
   * The alternative was to withdraw the cell and explain the gap in a KDoc, which is what the first
   * draft of #116 phase 3 did. A comment is invisible: nobody reads it again, and nothing announces
   * the day the library starts drawing. Publishing the cell puts the gap on the sheet where a
   * reader meets it, lets design-parity report it as the real divergence it is — this rendition
   * genuinely does not draw what the kit specifies, which is what a design-led scan is for — and
   * makes the fix self-announcing through the second assertion below.
   *
   * ALL TWELVE of them, not one. This used to carry `disabled` alone, on the reasoning that the
   * others bake byte-identically and publishing them would be the same empty picture under a dozen
   * names. That reasoning traded one invisibility for another: the kit publishes fifteen
   * `Disabled=Yes` cells for this set, and a sheet drawing one of them reads as a sheet that has
   * nearly finished. Drawing all twelve the library can be asked for says the size of the hole, and
   * design-parity scores twelve divergences rather than one. The duplication is recorded next door
   * in `RemoteRenderTest.knownDuplicate`, which is the test that owns that question.
   *
   * The three `outlined-*-disabled` cells are not listed and are not blank: the border survives,
   * because this sticker draws it itself through `border` rather than through the component, so
   * each is a real picture of a button that lost only its label.
   *
   * PASSING THE COLOURS DOES NOT HELP, which is worth knowing before anyone tries.
   * `textButtonColors` takes `disabledContainerColor` and `disabledContentColor`; handing it both,
   * at the tokens Wear resolves, still bakes transparent. The failure is in what the disabled path
   * draws rather than in what it looks up, so a caller cannot reach it from the call site.
   *
   * **An entry here is never a way to quiet a sticker that is simply broken.** It says the LIBRARY
   * draws nothing for this state; the entry names the tracked issue and the call that does it, and
   * `remote-snapshot-probe.yml` re-checks it against the newest androidx.dev build weekly.
   */
  private val knownBlank =
    mapOf(
      "TextRemoteButton_VARIANT_disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_small-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_large-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_filled-variant-disable" to
        REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_filled-variant-small-d" to
        REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_filled-variant-large-d" to
        REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_tonal-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_tonal-small-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_tonal-large-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_child-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_child-small-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
      "TextRemoteButton_VARIANT_child-large-disabled" to REMOTE_TEXT_BUTTON_DRAWS_NOTHING_DISABLED,
    ) + if (onSnapshotLane) SNAPSHOT_LANE_BLANKS else emptyMap()

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
