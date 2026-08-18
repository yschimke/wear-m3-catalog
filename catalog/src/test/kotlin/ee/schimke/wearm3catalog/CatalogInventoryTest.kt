package ee.schimke.wearm3catalog

import ee.schimke.wearm3catalog.sections.SHAPE_SET
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the catalog's **inventory invariants** — the ones a compile can't catch and that would
 * otherwise only surface as a wrong or missing sticker at the end of a long CI render.
 *
 * The annotations are the source of truth for the published sheet (`catalog.spec.json` carries only
 * cover-sheet fields), so a duplicated id, a component with no caption, or a component with no kit
 * node is a real defect in the deliverable rather than a style nit.
 *
 * The source tree is read directly rather than through reflection: the annotations are `BINARY`
 * retention and discovery reads them with ClassGraph, so a source scan is both sufficient here and
 * independent of the render pipeline being available.
 */
class CatalogInventoryTest {

  private val sections: List<File> =
    File("src/main/kotlin/ee/schimke/wearm3catalog/sections")
      .listFiles { f: File -> f.name.endsWith(".kt") }!!
      .sortedBy { it.name }

  private val sources: List<Pair<File, String>> = sections.map { it to it.readText() }

  private val components =
    Regex("""@CatalogComponent\((.*?)\)\n@""", RegexOption.DOT_MATCHES_ALL).let { block ->
      sources.flatMap { (file, text) -> block.findAll(text).map { file to it.groupValues[1] } }
    }

  private fun arg(body: String, name: String): String? =
    Regex("""$name = "([^"]*)"""").find(body)?.groupValues?.get(1)

  @Test
  fun `every section file declares its group and section`() {
    for ((file, text) in sources) {
      assertTrue(
        "${file.name} must open with @file:CatalogGroup(name = …, section = …) so its stickers " +
          "land in a named group under a named tab",
        text.startsWith("@file:CatalogGroup(name = ") && text.contains(", section = \""),
      )
    }
  }

  @Test
  fun `the catalog declares at least one component`() {
    assertTrue("no @CatalogComponent found under sections/", components.isNotEmpty())
  }

  @Test
  fun `component ids are unique`() {
    val ids = components.mapNotNull { arg(it.second, "id") }
    assertEquals(ids.size, ids.toSet().size)
  }

  @Test
  fun `every component carries a caption`() {
    for ((file, body) in components) {
      val id = arg(body, "id")
      assertTrue(
        "$id (${file.name}) publishes as a bare picture: give it a caption",
        !arg(body, "caption").isNullOrBlank(),
      )
    }
  }

  /**
   * Membership is the kit's call: a component with no exact, renderable kit node does not enter the
   * inventory at all, so there is no "published but unmapped" state to fall into. `--strict` in
   * `scripts/design-map.sh` fails the same way before a render is attempted; this fails first, and
   * without a Figma token.
   */
  @Test
  fun `every component maps to the Wear kit`() {
    for ((file, body) in components) {
      val id = arg(body, "id")
      val reference = arg(body, "reference")
      assertTrue("$id (${file.name}) has no reference to a kit node", !reference.isNullOrBlank())
      assertTrue(
        "$id points at $reference, which is not a node in the M3 Wear OS Apps Design Kit",
        reference!!.startsWith("figma:$WEAR_KIT_FILE_KEY/"),
      )
    }
  }

  /**
   * Every shape the `shape` knob can select has a baked cell, and every baked cell selects a shape
   * that exists. A cell whose seed does not match a table key renders the default silhouette under
   * another shape's name — silently, because nothing else compares the two lists.
   */
  @Test
  fun `every shape in the set has a cell, and every cell a shape`() {
    val keys = SHAPE_SET.map { it.first }
    assertEquals("shape keys must be unique", keys.size, keys.toSet().size)

    val shapesSource = sections.single { it.name == "Shapes.kt" }.readText()
    val seeded =
      Regex("""@OverrideVariant\([^)]*strings = \["shape=([^"]+)"\]""")
        .findAll(shapesSource)
        .map { it.groupValues[1] }
        .toSet()

    // The base render draws the first entry with no knob turned, so it is the one key with no cell.
    assertEquals(keys.drop(1).toSet(), seeded)
  }

  private companion object {
    const val WEAR_KIT_FILE_KEY = "B24oss2tTeXAFykyeyusz0"
  }
}
