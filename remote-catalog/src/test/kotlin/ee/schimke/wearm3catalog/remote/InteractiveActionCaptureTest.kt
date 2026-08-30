package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Guards that the clickable stickers encode a **self-contained** click — one the player can act on
 * by itself — rather than a `hostAction` that leaves the document unchanged.
 *
 * Every button and card on this sheet used to carry a shared `hostAction(...)` as its `onClick`.
 * That posts a payload *out* of the document and mutates nothing inside it, so a tap in the preview
 * player repainted nothing: there was no state change to repaint. The stickers now use
 * `countedRemote(...)`, which pairs a `rememberMutableRemoteInt` with a `valueChange` action and a
 * label expression conditional on that counter — so the player updates itself, no host round-trip.
 *
 * This has to be a **sidecar** assertion, not a pixel one, and that is the whole point: at rest the
 * counter is 0, so the conditional resolves to the bare label and the baked PNG is byte-identical
 * to the one this catalog has always published (that parity is the feature, not a gap). The counter
 * branch is only reachable once a player dispatches a real touch, which a static render never does.
 * The evidence therefore lives in the encoded document — where the branch's own string literals are
 * stored as UTF-8, exactly as [WidgetContainerIrCaptureTest] reads them.
 *
 * A regression that reverted a sticker to `hostAction` would drop those literals and fail here,
 * while the render stayed green.
 */
class InteractiveActionCaptureTest {

  private val rendersDir = File("build/compose-previews/renders")

  /**
   * Sticker → the base label its `countedRemote(...)` was given.
   *
   * A key is a render, not a component: `<function stem>` for a base capture and `<function
   * stem>#<cell>` for an `@OverrideVariant` one. Most of this sheet's sizes and styles became cells
   * in #116, and a cell is a separate render of the same function — so the entries that used to
   * name `IconLabelRemoteButton` and `CompactTextOnlyRemoteButton` name `FilledRemoteButton#icon`
   * and `CompactRemoteButton#text-only` instead. Same captures, same assertions; what moved is the
   * name the renderer files them under.
   */
  private val countedStickers =
    mapOf(
      // The kit's label for every single-slot button — see `KitCopy`. Referenced rather than
      // spelled, so this test cannot drift back into asserting copy the catalog no longer draws.
      "FilledRemoteButton" to KitCopy.PRIMARY_LABEL,
      // The filled button's own cells. `disabled` is counted too, and that is not a contradiction:
      // the label expression is encoded whatever `enabled` resolves to — what a disabled button
      // will not do is dispatch the touch that advances it.
      "FilledRemoteButton#disabled" to KitCopy.PRIMARY_LABEL,
      "FilledRemoteButton#icon" to KitCopy.PRIMARY_LABEL,
      "FilledRemoteButton#icon-large" to KitCopy.PRIMARY_LABEL,
      "OutlinedRemoteButton" to KitCopy.PRIMARY_LABEL,
      "CustomShapeRemoteButton" to KitCopy.PRIMARY_LABEL,
      // Its label is an overridable named binding; the counter composes over it rather than
      // replacing it, so it takes the same default tally as everything else.
      "NamedLabelRemoteButton" to KitCopy.PRIMARY_LABEL,
      "TonalRemoteButton" to KitCopy.PRIMARY_LABEL,
      "CompactRemoteButton" to KitCopy.PRIMARY_LABEL,
      "CompactRemoteButton#text-only" to KitCopy.PRIMARY_LABEL,
      // Cards quote the kit's card slots.
      "CardRemote" to KitCopy.CARD_CONTENT,
      "OutlinedCardRemote" to KitCopy.CARD_CONTENT,
      "TitleCardRemote" to KitCopy.CARD_TITLE,
      "TitleCardRemote#with-subtitle" to KitCopy.CARD_TITLE,
      "AppCardRemote" to KitCopy.CARD_TITLE,
      // Both were components of their own until they folded into the card above, so they take the
      // `<stem>#<cell>` form this doc describes — the same move the buttons made in #116. Same
      // captures, same assertions; only the filed name moved.
      "TitleCardRemote#title-and-subtitle" to KitCopy.CARD_TITLE,
      "AppCardRemote#no-app-image" to KitCopy.CARD_TITLE,
    )

  /**
   * The renders that carry `toggledRemote` and NO label at all, so neither list above nor below
   * describes them: the icon-only compact cell and the icon button with its three size cells. They
   * are asserted by `every clickable sticker without a label still renders` below rather than left
   * unmentioned, so the absence reads as a decision.
   */
  private val unlabelledStickers =
    listOf(
      "CompactRemoteButton#icon-only",
      "IconRemoteButton",
      "IconRemoteButton#small",
      "IconRemoteButton#large",
    )

  /**
   * The round text buttons, which deliberately do NOT appear in [countedStickers].
   *
   * They carry the kit's `MMM` — a run of its widest glyph, which is how the kit sizes a round
   * container — so the label is already the width of the circle and `countedRemote` would grow it
   * to `MMM (1)` on the first tap, drawing the tally through the edge. They use `toggledRemote`
   * instead: a colour tween that says the tap landed without touching the metrics. Asserted so the
   * absence reads as a decision rather than an oversight.
   */
  private val toggledStickers =
    listOf(
      "TextRemoteButton",
      // The four style and size cells the text button folded in (#116) — `Button/Text-Small` and
      // friends until then. Each is still its own render, so each is still asserted.
      "TextRemoteButton#small",
      "TextRemoteButton#large",
      "TextRemoteButton#child",
      "TextRemoteButton#outlined",
    )

  @Test
  fun `no round text button grows its label on tap`() {
    for (stem in toggledStickers) {
      val text = documentText(stem)
      assertWithMessage("$stem still encodes a click counter into its label")
        .that(text)
        .doesNotContain("${KitCopy.GLYPHS} (")
      assertWithMessage("$stem lost its kit glyph run").that(text).contains(KitCopy.GLYPHS)
    }
  }

  /**
   * The fragments `countedRemote` concatenates around the counter (`"<base> (" + n + ")"`). They
   * are in the document only because the counter branch was encoded — the resting label never draws
   * them.
   */
  private val counterFragments = listOf(" (", ")")

  /**
   * Captures are named `<stem>_width_…_height_…_dpi_…[_VARIANT_<cell>]-<hash>.<ext>`, so match on
   * the stem prefix rather than pinning the size suffix — a breakpoint change shouldn't rename this
   * test's inputs.
   *
   * The cell suffix is matched EXPLICITLY rather than left to sort order. A base capture and its
   * cells all start with the same stem, so a prefix match alone returns the whole family and any
   * pick among them is an accident of naming: `-<hash>` happens to sort before `_VARIANT_` today,
   * which would have let a base assertion quietly read a cell's document the day that changed. The
   * renderer spells a cell name with underscores (`icon-large` → `icon_large`), so the key's
   * hyphens are translated here and the map above stays readable as the cell names the annotations
   * declare.
   */
  private fun capture(key: String, ext: String): File? {
    val stem = key.substringBefore('#')
    val cell = key.substringAfter('#', "").replace('-', '_')
    val marker = if (cell.isEmpty()) null else "_VARIANT_$cell-"
    return rendersDir
      .listFiles { f ->
        f.name.startsWith("${stem}_") &&
          f.name.endsWith(".$ext") &&
          if (marker == null) !f.name.contains("_VARIANT_") else f.name.contains(marker)
      }
      .orEmpty()
      .minByOrNull { it.name }
  }

  private fun documentText(key: String): String {
    val rc = capture(key, "rc")
    assertWithMessage("missing encoded document for $key").that(rc).isNotNull()
    return rc!!.readBytes().toString(Charsets.UTF_8)
  }

  @Test
  fun `every counted sticker still carries its resting label`() {
    for ((stem, label) in countedStickers) {
      assertWithMessage("$stem lost its label").that(documentText(stem)).contains(label)
    }
  }

  @Test
  fun `every counted sticker encodes the click-counter branch of its label`() {
    for (stem in countedStickers.keys) {
      val text = documentText(stem)
      for (fragment in counterFragments) {
        assertWithMessage("$stem does not encode the counter fragment '$fragment'")
          .that(text)
          .contains(fragment)
      }
    }
  }

  @Test
  fun `the sheet still renders every counted sticker`() {
    // Keeps the assertions above from passing vacuously if the render stopped emitting captures.
    for (stem in countedStickers.keys) {
      assertWithMessage("no baked capture for $stem").that(capture(stem, "png")).isNotNull()
    }
  }

  @Test
  fun `every clickable sticker without a label still renders`() {
    // No label means no counter fragment and no glyph run to assert, so the only thing left to
    // guard is that the cell is still being rendered at all — which is what would break if a fold
    // dropped one.
    for (stem in unlabelledStickers) {
      assertWithMessage("no baked capture for $stem").that(capture(stem, "png")).isNotNull()
    }
  }
}
