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
 * * `Button/Icon`'s base was left unpinned in the same PR, which renders the same 28dp glyph
 *   `SmallButtonSize` resolves to, so the `small` cell became a copy of the base.
 *
 * A third, `Progress/Circular`'s `disabled` cell, declared a seed that nothing read: the disabled
 * render was the enabled picture, scored against the kit's `Disabled=Yes` node. That one is a
 * duplicate too, which is how this test catches it.
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
}
