package ee.schimke.wearm3catalog

import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Holds [kit-cells.json] — how much of each kit set each sheet draws — against the kit walk it
 * counts and the reasons `kit-sets.json` states.
 *
 * `CatalogKitCoverageTest` is the same idea one level up, and its level is the SET: every published
 * set is reproduced or excluded, and a set counts as reproduced when a component exists for it.
 * Nothing there asks how much of it is drawn, which is how `:remote-catalog` came to draw 15 of the
 * `Card` set's 45 cells with the whole suite green
 * ([#158](https://github.com/yschimke/wear-m3-catalog/issues/158)). The `Content type` axis was
 * absent entirely and no file said so.
 *
 * The numbers themselves are not asserted here and could not be: they are an OUTPUT, regenerated
 * from each module's resolved design map by `scripts/kit-cells.sh` and reconciled by CI, so a cell
 * that stops being drawn moves a number in a reviewable diff. What this test adds is everything
 * that cannot be regenerated:
 * - the record and the kit walk agree on what the kit publishes,
 * - the record's own arithmetic holds, and no sheet claims a node the set no longer publishes, and
 * - a reason stated for a gap cannot outlive it — the same both-directions rule
 *   `CatalogKitCoverageTest` applies to an exclusion.
 *
 * WHY THE REASONS LIVE IN `kit-sets.json` and the counts here: one is prose a person writes, the
 * other is a number a script derives, and putting a hand-written sentence in a generated file makes
 * it a merge conflict every time a cell is added. `kit-sets.json` is already the file that carries
 * this catalog's written answers about a set, so a gap's reason goes on the row that already says
 * what the set is.
 */
class KitCellCoverageTest {

  /** The sheets whose gaps must carry a written reason — both of them, since #160. */
  private val reasoned = setOf("catalog", "remote-catalog")

  private val root = File("..")

  private val record = JSONObject(File(root, "kit-cells.json").readText())

  private val index = JSONObject(File(root, "figma-kit-index.json").readText())

  private val rows =
    record.getJSONArray("sets").let { array ->
      (0 until array.length()).map { array.getJSONObject(it) }
    }

  private val setRows =
    JSONObject(File(root, "kit-sets.json").readText()).getJSONArray("sets").let { array ->
      (0 until array.length()).map { array.getJSONObject(it) }
    }

  private fun sheetsOf(row: JSONObject): List<Pair<String, JSONObject>> =
    row.getJSONObject("sheets").let { sheets ->
      sheets.keys().asSequence().map { it to sheets.getJSONObject(it) }.toList()
    }

  /**
   * A record that generated nothing would pass every assertion below by having nothing to check.
   */
  @Test
  fun `the record covers both sheets`() {
    assertTrue(
      "kit-cells.json names no sets — regenerate with scripts/kit-cells.sh",
      rows.isNotEmpty(),
    )
    val sheets = rows.flatMap { sheetsOf(it) }.map { it.first }.toSet()
    assertEquals(setOf("catalog", "remote-catalog"), sheets)
  }

  /**
   * The denominator is the committed kit walk, so the two files move together or the record is
   * counting against a kit that has changed underneath it.
   */
  @Test
  fun `every row counts the cells the kit walk saw`() {
    val sets = index.getJSONObject("sets")
    for (row in rows) {
      val node = row.getString("node")
      assertTrue(
        "${row.getString("set")} is a row of kit-cells.json but figma-kit-index.json publishes no " +
          "set $node — regenerate both, in that order",
        sets.has(node),
      )
      assertEquals(
        "${row.getString("set")} counts against ${row.getInt("published")} cells, the kit walk " +
          "saw a different number — regenerate with scripts/kit-cells.sh",
        sets.getJSONObject(node).getJSONArray("variants").length(),
        row.getInt("published"),
      )
    }
  }

  /**
   * Every published cell is either drawn or named as uncovered — a record that dropped cells off
   * both sides would report a smaller gap than the set actually has.
   *
   * `stray` is the other half: a sheet claiming a node this set does not publish. It means a
   * reference has rotted, and it is worth failing on rather than recording, because the cell is not
   * being compared to anything and the count would still look healthy.
   */
  @Test
  fun `drawn and uncovered account for every published cell`() {
    for (row in rows) {
      for ((sheet, counts) in sheetsOf(row)) {
        val where = "${row.getString("set")} / $sheet"
        assertEquals(
          "$where draws ${counts.getInt("drawn")} and names " +
            "${counts.getJSONArray("uncovered").length()} uncovered, which is not the " +
            "${row.getInt("published")} cells the set publishes",
          row.getInt("published"),
          counts.getInt("drawn") + counts.getJSONArray("uncovered").length(),
        )
        assertTrue(
          "$where names ${counts.optJSONArray("stray")} as cells of this set, which the kit does " +
            "not publish under it — the reference has rotted and is being compared to nothing",
          !counts.has("stray"),
        )
      }
    }
  }

  /**
   * A stated reason cannot outlive the gap that earned it.
   *
   * The reason is optional — most of these gaps are not yet written down, which is the half of #158
   * this record makes visible rather than closes — but a reason that IS stated has to still be
   * true. Left alone, a sentence explaining why a sheet cannot draw the `Background Image` column
   * would sit there unchanged on the day someone drew it, and the file would be documenting a
   * limitation nobody has any more.
   */
  @Test
  fun `no stated reason outlives its gap`() {
    val byNode = rows.associateBy { it.getString("node") }
    for (setRow in setRows) {
      val cells = setRow.optJSONObject("cells") ?: continue
      val set = setRow.getString("set")
      val row = byNode[setRow.getString("node")]
      assertTrue(
        "$set states why a sheet falls short of it, but kit-cells.json has no row for it at all — " +
          "no sheet reproduces this set, so the reason belongs in `excluded`",
        row != null,
      )
      for (sheet in cells.keys()) {
        val counts = row!!.getJSONObject("sheets").optJSONObject(sheet)
        assertTrue(
          "$set states why $sheet falls short of it, and $sheet reproduces none of it — that is " +
            "an absence at the level of the set, not of a cell",
          counts != null,
        )
        assertTrue(
          "$set / $sheet states why it falls short and now draws every cell the kit publishes — " +
            "delete the reason",
          counts!!.getJSONArray("uncovered").length() > 0,
        )
        assertTrue(
          "$set / $sheet has an empty reason, which says nothing a missing key would not",
          cells.getString(sheet).isNotBlank(),
        )
      }
    }
  }

  /**
   * A gap on a sheet held to the rule has to say why — the direction that makes silence fail.
   *
   * This is the other half of `no stated reason outlives its gap`, and it could not be added until
   * the reasons existed: when the record landed, all 30 of its gaps were unexplained
   * ([#158](https://github.com/yschimke/wear-m3-catalog/issues/158)). It is the same shape as
   * `CatalogInventoryTest.every component is either mapped to the kit or says why not`, one level
   * down: an uncovered cell is fine when something says why, and only silence fails.
   *
   * **Both sheets are held to it now.** `remote-catalog` was carved out when the record landed,
   * because its rows were mostly cells nobody had drawn rather than cells nothing could draw — the
   * honest answer to those is a component, not a sentence, and writing "not drawn yet" against each
   * would have satisfied this test while telling a reader nothing. That work is done
   * ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)): the sheet went from 47 of 327
   * cells to 163, and what is left is written down.
   */
  @Test
  fun `every gap on a reasoned sheet says why`() {
    val reasons = setRows.associate { row ->
      row.getString("node") to (row.optJSONObject("cells") ?: JSONObject())
    }
    for (row in rows) {
      val stated = reasons[row.getString("node")] ?: JSONObject()
      for ((sheet, counts) in sheetsOf(row)) {
        if (sheet !in reasoned) continue
        if (counts.getJSONArray("uncovered").length() == 0) continue
        assertTrue(
          "${row.getString("set")} / $sheet draws ${counts.getInt("drawn")} of " +
            "${row.getInt("published")} cells and says nothing about the rest — add a `cells." +
            "$sheet` reason to its kit-sets.json row, or draw them",
          stated.optString(sheet).isNotBlank(),
        )
      }
    }
  }

  /** A sheet cannot draw a set the coverage record says this catalog leaves out. */
  @Test
  fun `no set the record counts is excluded in kit-sets json`() {
    val excluded =
      setRows.filter { it.optString("excluded").isNotBlank() }.associateBy { it.getString("node") }
    for (row in rows) {
      val node = row.getString("node")
      assertTrue(
        "${row.getString("set")} is excluded in kit-sets.json but " +
          "${sheetsOf(row).map { it.first }} draw cells of it — move it to `components` and delete " +
          "the reason",
        node !in excluded,
      )
    }
  }
}
