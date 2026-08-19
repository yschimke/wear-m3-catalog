package ee.schimke.wearm3catalog

import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Holds [kit-sets.json] and the annotations together, in **both** directions.
 *
 * The file is the catalog's answer to "what does the kit publish, and what did you do about it" —
 * one row per published component set, carrying either the catalog components that reproduce it or
 * a reason it is absent. That answer is only worth having if it cannot drift:
 *
 * - a set that loses its components silently becomes an unstated gap, and
 * - an exclusion outlives the limitation that earned it the moment someone implements the thing
 *   anyway, at which point the file is documenting a decision nobody is making any more.
 *
 * So this fails on both. The kit walk that produced the rows is `.github/workflows/figma-refs.yml`;
 * re-run it when the kit itself moves.
 */
class CatalogKitCoverageTest {

  private val root = File("..")

  private val sources: List<String> =
    File("src/main/kotlin/ee/schimke/wearm3catalog/sections")
      .listFiles { f: File -> f.name.endsWith(".kt") }!!
      .map { it.readText() }

  private val coverage = JSONObject(File(root, "kit-sets.json").readText())

  private val rows =
    coverage.getJSONArray("sets").let { array ->
      (0 until array.length()).map { array.getJSONObject(it) }
    }

  /**
   * Every `id` paired with the kit **SET** node it belongs to.
   *
   * The join key is `referenceSet`, not `reference`. `kit-sets.json` has one row per published
   * component set, while `reference` names the exact VARIANT within that set which the sticker
   * reproduces — `Style=Filled, Icon=No, Alignment=Center, Disabled=No` rather than the 50-cell
   * `Button` grid. Joining on the variant would leave every row of this file unmatched.
   */
  private val declared: List<Pair<String, String>> = buildList {
    val block = Regex("""@CatalogComponent\((.*?)\n\)""", RegexOption.DOT_MATCHES_ALL)
    val idOf = Regex("""id = "([^"]+)"""")
    val setOf = Regex("""referenceSet = "figma:[^/]+/([^"]+)"""")
    for (text in sources) {
      for (match in block.findAll(text)) {
        val body = match.groupValues[1]
        val id = idOf.find(body)?.groupValues?.get(1) ?: continue
        val node = setOf.find(body)?.groupValues?.get(1) ?: continue
        add(id to node)
      }
    }
  }

  @Test
  fun `every published kit set is either reproduced or excluded for a stated reason`() {
    for (row in rows) {
      val set = row.getString("set")
      val mapped = row.has("components")
      val excluded = row.optString("excluded").isNotBlank()
      assertTrue(
        "${row.getString("page")} / $set is neither reproduced nor excluded — a set the catalog " +
          "silently ignores is an unstated gap",
        mapped != excluded,
      )
    }
  }

  @Test
  fun `every component a row names exists, and names that row's node`() {
    val byId = declared.toMap()
    for (row in rows) {
      if (!row.has("components")) continue
      val node = row.getString("node")
      val components = row.getJSONArray("components")
      for (i in 0 until components.length()) {
        val id = components.getString(i)
        assertEquals(
          "$id is listed against ${row.getString("set")} but its referenceSet names another set",
          node,
          byId[id],
        )
      }
    }
  }

  /** An exclusion cannot outlive the limitation that earned it. */
  @Test
  fun `no excluded set is quietly implemented anyway`() {
    val referenced = declared.map { it.second }.toSet()
    for (row in rows) {
      if (row.optString("excluded").isBlank()) continue
      assertTrue(
        "${row.getString("set")} is excluded but something now names it as a referenceSet — move " +
          "it to `components` and delete the reason",
        row.getString("node") !in referenced,
      )
    }
  }

  /** Nothing may reference a node the kit walk never saw. */
  @Test
  fun `every reference in the sources belongs to a known kit set`() {
    val known = rows.map { it.getString("node") }.toSet()
    for ((id, node) in declared) {
      assertTrue(
        "$id names the set $node, which is not published in kit-sets.json — re-run " +
          "figma-refs.yml if the kit gained it",
        node in known,
      )
    }
  }
}
