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
        "RemoteButtonDefaults.buttonColors leaves the disabled pair at its defaults, so a style " +
          "written out by passing containerColor/contentColor is the disabled FILLED button",
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
    ) + if (onSnapshotLane) emptyMap() else CONTAINED_ICON_BUTTONS_COLLAPSE_WHEN_DISABLED

  /**
   * Deliberately compares only renders of the SAME component. Two different components may
   * legitimately look alike at this size; two renders of one component may not — that is a cell
   * varying nothing, unless [knownDuplicate] says the library draws one picture for both.
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
      if (clashes.isEmpty() || componentOf(component) in knownDuplicate) null
      else "$component: ${clashes.joinToString(", ")}"
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
  }
}
