package ee.schimke.wearm3catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
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

  /**
   * The declared themes stay declared, and stay **Wear** themes.
   *
   * `@WearThemeCatalog` is `BINARY` retention — discovery reads it off the class file with
   * ClassGraph, so it is deliberately not reflectable at runtime. The annotation's presence is
   * therefore checked in the source; what reflection *can* prove is the half that breaks the
   * render, since `PreviewWrapperProvider` is how the renderer invokes a theme at all.
   *
   * The mobile `@ThemeCatalog` on one of these would be the expensive mistake: the providers
   * install the Wear `MaterialTheme`, so the generated sheet would report the baseline mobile M3
   * palette instead of the theme it declares, with no error and a sheet that looks entirely
   * plausible.
   */
  @Test
  fun `declared themes are Wear wrapper providers`() {
    val providers: List<Any> =
      listOf(
        ConfettiDefaultTheme(),
        KotlinConfTheme(),
        AndroidMakersTheme(),
        DroidconTheme(),
        DevFestTheme(),
        GoogleSansFlexTheme(),
      )
    for (provider in providers) {
      assertTrue(
        "${provider::class.simpleName} must implement PreviewWrapperProvider, which is how the " +
          "renderer invokes a declared theme",
        provider is PreviewWrapperProvider,
      )
    }

    val themes = File("src/main/kotlin/ee/schimke/wearm3catalog/CatalogThemes.kt").readText()
    assertEquals(
      "every declared theme must keep its @WearThemeCatalog annotation, else it stops being " +
        "offered in the preview server's theme select and its specimen sheet stops being generated",
      providers.size,
      Regex("""@WearThemeCatalog\(""").findAll(themes).count(),
    )
    assertEquals(
      "a Wear theme annotated with the MOBILE @ThemeCatalog publishes a specimen sheet of the " +
        "baseline mobile M3 palette instead of the theme it declares",
      0,
      Regex("""(?<!Wear)ThemeCatalog\(""").findAll(themes).count(),
    )
  }

  /**
   * The four conference seeds are four colours. A copy-paste slip here publishes two themes that
   * render identical pixels under different names — which reads as a working theme switcher and is
   * the one defect a reviewer scrolling a sheet of round stickers will not see.
   */
  @Test
  fun `each conference identity has its own seed`() {
    val seeds = listOf(KotlinConfSeed, AndroidMakersSeed, DroidconSeed, DevFestSeed)
    assertEquals("conference seeds must be distinct", seeds.size, seeds.toSet().size)
  }

  /**
   * …and they are the same four the **`:remote-catalog` sibling** builds its palettes from.
   *
   * The two modules cannot share a constant: they are on different dependency lines, which is the
   * whole reason there are two of them (`AGENTS.md` → Two modules). So the seeds are duplicated in
   * `RemoteThemeCatalogs.kt`, and a duplicate that nothing pins is a duplicate that drifts — into
   * two catalogs of the same surface publishing a "Droidcon" apiece in two different greens, which
   * is exactly the split #99 closed. Each side pins the literals; editing a seed fails the other
   * side's build until both move.
   */
  @Test
  fun `the conference seeds are pinned, so the sibling's copy cannot drift`() {
    assertEquals(Color(0xFF7F52FF), KotlinConfSeed)
    assertEquals(Color(0xFFE59A4F), AndroidMakersSeed)
    assertEquals(Color(0xFF00D775), DroidconSeed)
    assertEquals(Color(0xFF4285F4), DevFestSeed)
  }

  /**
   * The cover sheet's `display.hero` is the ONE field in `catalog.spec.json` that names something
   * the annotations own, and nothing downstream complains when it names nothing: the preview server
   * resolves a declared hero by slug and, finding no preview, silently falls back to its own
   * representative pick ([ServeBundleHost.declaredHeroPreviewId] in compose-ai-tools). So a renamed
   * or mistyped component id costs this catalog its front-door picture and reports it nowhere — the
   * index simply shows a different sticker than the one that was chosen.
   */
  @Test
  fun `the declared front-door hero names a real component`() {
    val spec = File("../catalog.spec.json").readText()
    val hero =
      Regex(""""hero"\s*:\s*"([^"]+)"""").find(spec)?.groupValues?.get(1)
        ?: error("catalog.spec.json declares no display.hero")
    val ids = components.mapNotNull { arg(it.second, "id") }.toSet()
    assertTrue(
      "catalog.spec.json's display.hero is \"$hero\", which is not a @CatalogComponent id — the " +
        "front door would quietly feature whatever the server picks instead. Known ids: " +
        ids.sorted().joinToString(", "),
      hero in ids,
    )
  }

  private companion object {
    const val WEAR_KIT_FILE_KEY = "B24oss2tTeXAFykyeyusz0"
  }
}
