package ee.schimke.wearm3catalog.remote

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The render guard `:catalog` has carried since its own cells arrived and this sheet did not: a
 * capture that is **byte-identical to another render of the same component**. Plus the cheap
 * companion that stops the whole family passing vacuously — a capture that failed and wrote no
 * image at all.
 *
 * The blank-capture invariant already lives next door in [StickerBakeCoverageTest] and is not
 * repeated here.
 *
 * The Wear sibling's `CatalogRenderTest` states the general case: a cell that renders identically
 * to the render it varies is a **wrong picture that renders green**. The sheet publishes one image
 * twice under two names, and the second one claims to show something it does not — while every
 * other check stays happy, because both files exist, both decode, and neither is blank.
 *
 * #116 is the record of what its absence cost here. Two cells reached `main` or came within a push
 * of it, each scored against a kit node while drawing a different cell's picture:
 *
 * * `Button/Icon-ExtraSmall` published the SMALL render under the extra-small name — the child
 *   style draws no container, so both sizes clamp to the same glyph. Byte-identical, and pointed at
 *   two different kit nodes (#125).
 * * `IconButton/Standard`'s base was left unpinned in the same PR, which renders the same 28dp
 *   glyph `SmallButtonSize` resolves to, so the `small` cell became a copy of the base.
 *
 * A third, `CircularProgressIndicator`'s `disabled` cell, declared a seed that nothing read: the
 * disabled render was the enabled picture, scored against the kit's `Disabled=Yes` node. That one
 * is a duplicate too, which is how this test catches it.
 *
 * The renders come from `composePreviewRender`, which `renderBeforeUnitTests` runs first (see
 * `build.gradle.kts`), so this reads the same PNGs CI publishes rather than a fixture.
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
   * The three contained icon-button styles, and the one entry the SNAPSHOT lane does not need.
   *
   * On the released alphas a disabled contained icon button draws no container: `iconButtonColors`
   * hard-defaults `disabledContainerColor` to transparent rather than deriving it, so the whole
   * disabled cell is a 38%-alpha glyph and nothing else — and `iconSizeFor` resolves
   * `ExtraSmallButtonSize` and `SmallButtonSize` to the same glyph size, leaving one picture where
   * the kit publishes two.
   *
   * The snapshot line publishes `filledIconButtonColors()`, `filledVariantIconButtonColors()` and
   * `filledTonalIconButtonColors()`, which derive that container as `onSurface` at 12% — see
   * `src/snapshot/kotlin/…/RemoteIconButtonPalette.kt`. The container is then drawn at the button's
   * real size, so the two cells are two pictures again and the collapse is gone. Hence the branch:
   * this is not an exemption being quieted, it is a library gap that one of the two lanes has.
   *
   * `outlined` is absent from both lanes and always was — that sticker draws its own border through
   * `border`, so its disabled cells never collapsed.
   */
  /**
   * The SNAPSHOT lane's own collapse, and the only component that has one: `EdgeButton`, which is
   * published on that lane alone (`src/snapshot/kotlin/…/EdgeButtonPreviews.kt`) and so cannot be
   * named unconditionally — a `knownDuplicate` key that matches no component fails the staleness
   * assertion below, correctly.
   *
   * `RemoteEdgeButtonDefaults` resolves all THREE filled styles' disabled colours to the same pair,
   * so `filled`, `filled-variant` and `tonal` are one picture once `enabled = false` however they
   * are called: 24 renders across the eight `Size`×`Type` crossings collapse to eight. The style
   * knob is demonstrably wired — the four ENABLED styles are four distinct pictures, and
   * `outlined-disabled` is a fifth because that sticker draws its own border through `border`
   * rather than through the colours. It is the disabled pair alone that the library collapses.
   *
   * This is the same finding the Wear sibling records for the same set (`CatalogRenderTest`, whose
   * note explains that Wear resolves all three to `onSurface` at 12%/38%), which is what makes it a
   * property of the design system rather than of either rendition.
   */
  private val EDGE_BUTTON_FILLED_STYLES_COLLAPSE_WHEN_DISABLED: Map<String, String> =
    mapOf(
      "EdgeButtonRemote" to
        "RemoteEdgeButtonDefaults resolves the filled, filled-variant and tonal disabled colours " +
          "to one pair, so all three styles draw one picture per Size x Type crossing once " +
          "enabled = false"
    )

  private val CONTAINED_ICON_BUTTONS_COLLAPSE_WHEN_DISABLED: Map<String, String> =
    mapOf(
      "FilledRemoteIconButton" to
        "a disabled RemoteIconButton loses its glyph, so ExtraSmallButtonSize and SmallButtonSize " +
          "leave one frame",
      "FilledVariantRemoteIconButton" to
        "a disabled RemoteIconButton loses its glyph, so ExtraSmallButtonSize and SmallButtonSize " +
          "leave one frame",
      "TonalRemoteIconButton" to
        "a disabled RemoteIconButton loses its glyph, so ExtraSmallButtonSize and SmallButtonSize " +
          "leave one frame",
    )

  /**
   * The pairs that are **expected** to render identically, each one a state the LIBRARY collapses
   * rather than a cell that varies nothing — the duplicate twin of
   * [StickerBakeCoverageTest.knownBlank], and added for the same reason.
   *
   * Without it the only way past this test is to withdraw the cell, and withdrawing is what makes a
   * defect invisible: the kit set then reads as unreproduced, which is indistinguishable from
   * nobody having got to it. The sheet looks fine and the collapse is nowhere. Publishing the cell
   * puts it where a reader meets it, lets design-parity score it as the divergence it is, and makes
   * the fix self-announcing through the second assertion below.
   *
   * Every entry names the call that collapses the pair. **An entry here is never a way to quiet a
   * cell that is simply wrong** — a cell whose seed nothing reads is exactly what this test exists
   * to catch (`CircularProgressIndicator`'s `disabled` cell, #125's two icon-button cells), and
   * those are bugs in this file rather than in the library. The test for the difference: if passing
   * the knob through correctly would separate the pictures, it is a bug here; if the library draws
   * one picture for both states however it is called, it belongs here.
   *
   * The keys are `<cell> == <cell>` within one component, ordered as the failure prints them.
   */
  private val knownDuplicate: Map<String, String> =
    mapOf(
      "TextRemoteButton" to
        "#130 — RemoteTextButton(enabled = false) draws nothing at all, so every Disabled=Yes " +
          "cell is the same empty frame; passing the disabled colours explicitly does not " +
          "change it",
      "CompactRemoteButton" to
        "RemoteButtonDefaults resolves the filled, filled-variant, tonal and child disabled " +
          "colours to one pair, so all four styles draw one picture per cell once enabled = " +
          "false — the same collapse EDGE_BUTTON_FILLED_STYLES_COLLAPSE_WHEN_DISABLED records " +
          "for RemoteEdgeButtonDefaults. This reason used to blame the call site for passing " +
          "containerColor/contentColor through the generic factory, which was true of the code " +
          "and not of the collapse: the styles now come from the library's own named factories " +
          "(RemoteButtonPalette) and the four disabled cells are still byte-identical",
      "FilledRemoteButton" to "a disabled RemoteButton draws its container and not its label",
      "FilledVariantRemoteButton" to
        "a disabled RemoteButton draws its container and not its label",
      "TonalRemoteButton" to "a disabled RemoteButton draws its container and not its label",
      "OutlinedRemoteButton" to "a disabled RemoteButton draws its container and not its label",
      "ChildRemoteButton" to "a disabled RemoteButton draws its container and not its label",
      "ImageBackgroundRemoteButton" to
        "a disabled RemoteButton draws its container and not its labels, so the secondary one " +
          "cannot show",
      "IconRemoteButton" to
        "the CHILD style draws no container, so iconSizeFor resolves ExtraSmallButtonSize and " +
          "SmallButtonSize to the same glyph and there is nothing else in the frame — enabled as " +
          "well as disabled, unlike the four contained styles below",
      "TitleCardRemote" to
        "RemoteTitleCard cannot put the timestamp under the subtitle, so Title Card 3 differs " +
          "from Title Card 2 only by its missing body — and an image or gallery cell has no body " +
          "to miss. Exactly six pairs, held by expectedCollapses",
    ) +
      if (onSnapshotLane) EDGE_BUTTON_FILLED_STYLES_COLLAPSE_WHEN_DISABLED
      else CONTAINED_ICON_BUTTONS_COLLAPSE_WHEN_DISABLED

  /**
   * How many pairs a [knownDuplicate] component is allowed to collapse into.
   *
   * A blanket exemption suits the entries above: a disabled `RemoteButton` draws no label however
   * it is called, so every cell of that component that turns `enabled` off collapses and the
   * component IS the finding. `TitleCardRemote` is the other shape — six of its twenty-eight cells
   * collapse and the other twenty-two must go on being checked — so exempting the component would
   * buy the six by giving up the test on the rest.
   *
   * A COUNT rather than the sibling's pairs, and the reason is the filenames. `CatalogRenderTest`
   * keys `<cell> == <cell>` because a Wear render spells its cell out
   * (`TitledCard_VARIANT_title_and_subtitle_outlined_content_image-<digest>.png`); a Remote render
   * spends its name budget on the frame first and truncates what is left, so all three outlined
   * crossings land on `title_and_subtitle_outl == with_subtitle_outlined` and no key could tell
   * them apart. The digest disambiguates but is `sha256(preview.id)` — stable, and unreadable as an
   * exemption. The count is what remains checkable: a seventh collapse fails, and so does a sixth
   * that appears because one of these was fixed and something else broke.
   *
   * The collapse itself: `Title Card 3` is a title, a subtitle and the timestamp UNDER them, with
   * no body, and `RemoteTitleCard` has no argument that puts the time there — so once both layouts
   * are handed the same slots, the only thing separating this sheet's `title-and-subtitle` cells
   * from its `with-subtitle` ones is the body text. Replace that body with an image or a gallery
   * and there is nothing left to differ by: three content crossings, tonal and outlined, is six.
   * The `Text` crossings of both layouts DO differ — the body is still there on one of them — and
   * they are ordinary comparisons.
   *
   * The Wear sibling records exactly these six for exactly this reason, which is what makes it a
   * property of the design system rather than of either rendition: neither library has an argument
   * that moves the timestamp off the title's row.
   */
  private val expectedCollapses: Map<String, Int> = mapOf("TitleCardRemote" to 6)

  /**
   * Deliberately compares only renders of the SAME component. Two different components may
   * legitimately look alike at this size; two renders of one component may not — that is a cell
   * varying nothing, unless [knownDuplicate] says the library draws one picture for both, in the
   * number [expectedCollapses] records where it names one.
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
      val name = componentOf(component)
      val allowed = expectedCollapses[name]
      when {
        clashes.isEmpty() -> null
        // A counted component is exempt for as many collapses as it records and reports any
        // beyond. What it cannot see is a SWAP — one recorded collapse fixed while an unrelated
        // cell starts duplicating — which keeps the number at six. That is the price of counting
        // instead of naming, and the reason the count is paired with a floor check below rather
        // than left to stand on its own.
        allowed != null ->
          if (clashes.size == allowed) null
          else
            "$component: ${clashes.size} pairs collapse where $allowed are recorded — " +
              clashes.joinToString(", ")
        name in knownDuplicate -> null
        else -> "$component: ${clashes.joinToString(", ")}"
      }
    }
    assertTrue(
      "these renders of one component are byte-identical — a cell that varies nothing publishes " +
        "the same picture twice, under two names and against two kit nodes. If the LIBRARY draws " +
        "one picture for both states however it is called, publish the cell and record it in " +
        "`knownDuplicate` with the call that collapses it, rather than withdrawing it:\n" +
        duplicates.joinToString("\n") { "  $it" },
      duplicates.isEmpty(),
    )
  }

  /** The component name a [knownDuplicate] key uses, from the grouped render stem. */
  private fun componentOf(stem: String): String = stem.substringBefore("_width")

  /**
   * The other direction, and the reason a known collapse is published rather than withdrawn: when
   * the library learns to tell the two states apart, this fails, and the fix is to delete the entry
   * and let the cells stand as ordinary comparisons. Without it the gap would close in silence and
   * the sheet would keep an exemption it no longer needs.
   */
  @Test
  fun `every known duplicate still collapses`() {
    val byComponent =
      renders
        .listFiles { f: File -> f.name.endsWith(".png") }
        .orEmpty()
        .groupBy { componentOf(it.name.substringBefore("_VARIANT_").substringBeforeLast("-")) }

    val missing = knownDuplicate.keys.filterNot { it in byComponent }
    assertTrue(
      "these `knownDuplicate` entries name no component — a stale exemption hides a real " +
        "duplicate:\n" +
        missing.joinToString("\n") { "  $it" },
      missing.isEmpty(),
    )

    val separated =
      knownDuplicate.keys.filter { component ->
        val digests = byComponent.getValue(component).map { it.readBytes().toList().hashCode() }
        digests.size == digests.toSet().size
      }
    assertTrue(
      "these components no longer render any duplicate — the library collapse they record has " +
        "been FIXED. Drop the entry from `knownDuplicate` and let the cells stand as ordinary " +
        "comparisons:\n" +
        separated.joinToString("\n") { "  $it (was: ${knownDuplicate[it]})" },
      separated.isEmpty(),
    )

    // The same both-directions rule for the counted components: a count that is too HIGH is a
    // collapse the library has since fixed, and nothing above would say so — the assertion there
    // only fires when the number grows.
    val overCounted = expectedCollapses.mapNotNull { (component, allowed) ->
      val files = byComponent[component].orEmpty()
      val seen = mutableSetOf<Int>()
      val clashes = files.count { !seen.add(it.readBytes().toList().hashCode()) }
      if (clashes < allowed) "$component: $clashes pairs collapse, $allowed recorded" else null
    }
    assertTrue(
      "these components collapse FEWER pairs than `expectedCollapses` records — the library has " +
        "learned to tell some of them apart. Lower the count, or drop the entry and let the cells " +
        "stand as ordinary comparisons:\n" +
        overCounted.joinToString("\n") { "  $it" },
      overCounted.isEmpty(),
    )
  }
}
