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

  /**
   * Every `@CatalogComponent(...)` block, sliced by balancing parentheses rather than by a regex
   * that stops at the first `)`. A caption or a reason routinely contains parentheses, and a
   * greedy-or-lazy match either swallows the next annotation or truncates mid-argument.
   */
  private val components: List<Pair<File, String>> = buildList {
    for ((file, text) in sources) {
      var at = text.indexOf("@CatalogComponent(")
      while (at >= 0) {
        var depth = 0
        var i = at + "@CatalogComponent".length
        var end = -1
        while (i < text.length) {
          when (text[i]) {
            '(' -> depth++
            ')' -> {
              depth--
              if (depth == 0) {
                end = i
                i = text.length
              }
            }
          }
          i++
        }
        if (end < 0) break
        add(file to text.substring(at, end))
        at = text.indexOf("@CatalogComponent(", end)
      }
    }
  }

  /**
   * The value of a named argument, joining a **concatenated** string literal back together. A
   * reason long enough to be worth stating is long enough to be wrapped as `"…" + "…"`, and reading
   * only the first fragment made the arg look absent — which failed the build for components that
   * had said exactly what the rule asks.
   */
  private fun arg(body: String, name: String): String? {
    // Built by concatenation on purpose: a raw string ending in `= """` sits one quote away from
    // meaning something else entirely.
    // `\\s*` around the `=` because ktfmt wraps a long value onto the next line: an argument whose
    // reason is worth stating is exactly the one that wraps, and demanding `name = ` read those as
    // absent — failing the build for components that had said precisely what the rule asks.
    val start = Regex("\\b" + name + "\\s*=\\s*").find(body)?.range?.last ?: return null
    val builder = StringBuilder()
    var i = start
    var sawLiteral = false
    while (i < body.length) {
      when {
        body[i].isWhitespace() || body[i] == '+' -> i++
        body[i] == '"' -> {
          val close = body.indexOf('"', i + 1)
          if (close < 0) return if (sawLiteral) builder.toString() else null
          builder.append(body, i + 1, close)
          sawLiteral = true
          i = close + 1
        }
        else -> return if (sawLiteral) builder.toString() else null
      }
    }
    return if (sawLiteral) builder.toString() else null
  }

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
   * Membership has two doors — a kit node, or a stated reason the kit has none — and what this
   * forbids is neither. A component that simply never said is indistinguishable from one nobody
   * checked, and that is the state the rule exists to keep out. `--strict` in
   * `scripts/design-map.sh` fails the same way before a render is attempted; this fails first, and
   * without a Figma token.
   */
  @Test
  fun `every component is either mapped to the kit or says why not`() {
    for ((file, body) in components) {
      val id = arg(body, "id")
      val reference = arg(body, "reference")
      val noReference = arg(body, "noReference")
      assertTrue(
        "$id (${file.name}) names neither a kit node nor a reason the kit has none — say which",
        !reference.isNullOrBlank() || !noReference.isNullOrBlank(),
      )
      if (!reference.isNullOrBlank()) {
        assertTrue(
          "$id points at $reference, which is not a node in the M3 Wear OS Apps Design Kit",
          reference.startsWith("figma:$WEAR_KIT_FILE_KEY/"),
        )
      }
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
