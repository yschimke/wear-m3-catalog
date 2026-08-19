@file:CatalogGroup(name = "Shapes", section = "Styles")
// `MaterialShapes` and `RoundedPolygon.toShape()` are still expressive-experimental on the
// material3 1.5.0-alpha line this module pins for them. Opted in at file scope rather than
// per-call: the whole file exists to draw that library.
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.wear.compose.material3.MaterialTheme
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker

// The kit's `Shapes` page is a specimen sheet, not a component sheet: named shapes drawn as filled
// silhouettes. The point of the sticker is the outline, so anything inside it would only be
// something else to compare.
//
// ONE COMPONENT, ONE CELL PER SHAPE
//
// The kit models the whole page as ONE component set varying a single `Shape=` property, so by this
// repo's taxonomy rule — membership is the kit's call, see AGENTS.md — this is one component with a
// shape axis, not one component per shape. Each cell still names its own kit node: the kit-index
// resolver matches the `shape=` seed below against the set's `Shape=` values and writes a tagged
// entry per cell into design-map.json.
//
// The seed key is therefore THE KIT'S spelling, lower-cased, not Compose's. Where they disagree the
// kit wins on the left and the Compose name survives only on the right — the kit's `Pantagon`
// (its own typo) and `Hexagon` (Compose's `ClamShell`) are the two to watch when the rest of the
// set is authored.
//
// All 35 cells are authored. `CatalogInventoryTest` holds the table to the cells in both
// directions, so a shape cannot be added to one and forgotten in the other.

/**
 * Every shape in the kit's set, keyed by **the kit's own variant value**, lower-cased.
 *
 * The key is the seed the `shape` knob takes, and the kit-index resolver matches that seed against
 * the set's `Shape=` property to find each cell's node — so the key has to be the kit's spelling,
 * not Compose's. Mostly they agree. Where they do not, the kit wins on the left and the Compose
 * name survives only on the right: `hexagon` is `MaterialShapes.ClamShell`, `pantagon` is the kit's
 * own spelling of Pentagon, and the cookies and clovers put their count first.
 */
internal val SHAPE_SET: List<Pair<String, RoundedPolygon>> =
  listOf(
    "circle" to MaterialShapes.Circle,
    "square" to MaterialShapes.Square,
    "slanted" to MaterialShapes.Slanted,
    "arch" to MaterialShapes.Arch,
    "fan" to MaterialShapes.Fan,
    "arrow" to MaterialShapes.Arrow,
    "semicircle" to MaterialShapes.SemiCircle,
    "oval" to MaterialShapes.Oval,
    "pill" to MaterialShapes.Pill,
    "triangle" to MaterialShapes.Triangle,
    "diamond" to MaterialShapes.Diamond,
    "hexagon" to MaterialShapes.ClamShell,
    "pantagon" to MaterialShapes.Pentagon,
    "gem" to MaterialShapes.Gem,
    "very sunny" to MaterialShapes.VerySunny,
    "sunny" to MaterialShapes.Sunny,
    "4-sided cookie" to MaterialShapes.Cookie4Sided,
    "6-sided cookie" to MaterialShapes.Cookie6Sided,
    "7-sided cookie" to MaterialShapes.Cookie7Sided,
    "9-sided cookie" to MaterialShapes.Cookie9Sided,
    "12-sided cookie" to MaterialShapes.Cookie12Sided,
    "ghost-ish" to MaterialShapes.Ghostish,
    "4-leaf clover" to MaterialShapes.Clover4Leaf,
    "8-leaf clover" to MaterialShapes.Clover8Leaf,
    "burst" to MaterialShapes.Burst,
    "soft burst" to MaterialShapes.SoftBurst,
    "boom" to MaterialShapes.Boom,
    "soft boom" to MaterialShapes.SoftBoom,
    "flower" to MaterialShapes.Flower,
    "puffy" to MaterialShapes.Puffy,
    "puffy diamond" to MaterialShapes.PuffyDiamond,
    "pixel circle" to MaterialShapes.PixelCircle,
    "pixel triangle" to MaterialShapes.PixelTriangle,
    "bun" to MaterialShapes.Bun,
    "heart" to MaterialShapes.Heart,
  )

@Composable
private fun catalogShape(): RoundedPolygon {
  val key = previewOverrideString("shape", "circle")
  return SHAPE_SET.firstOrNull { it.first == key }?.second ?: MaterialShapes.Circle
}

@CatalogComponent(
  id = "Shape/MaterialShapes",
  reference = "figma:B24oss2tTeXAFykyeyusz0/42284:176655",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/42284:176650",
  caption = "The expressive shape library, with each named shape folded in as a variant.",
)
@CatalogModes
@OverrideVariant(name = "square", strings = ["shape=square"])
@OverrideVariant(name = "slanted", strings = ["shape=slanted"])
@OverrideVariant(name = "arch", strings = ["shape=arch"])
@OverrideVariant(name = "fan", strings = ["shape=fan"])
@OverrideVariant(name = "arrow", strings = ["shape=arrow"])
@OverrideVariant(name = "semicircle", strings = ["shape=semicircle"])
@OverrideVariant(name = "oval", strings = ["shape=oval"])
@OverrideVariant(name = "pill", strings = ["shape=pill"])
@OverrideVariant(name = "triangle", strings = ["shape=triangle"])
@OverrideVariant(name = "diamond", strings = ["shape=diamond"])
@OverrideVariant(name = "hexagon", strings = ["shape=hexagon"])
@OverrideVariant(name = "pantagon", strings = ["shape=pantagon"])
@OverrideVariant(name = "gem", strings = ["shape=gem"])
@OverrideVariant(name = "very-sunny", strings = ["shape=very sunny"])
@OverrideVariant(name = "sunny", strings = ["shape=sunny"])
@OverrideVariant(name = "4-sided-cookie", strings = ["shape=4-sided cookie"])
@OverrideVariant(name = "6-sided-cookie", strings = ["shape=6-sided cookie"])
@OverrideVariant(name = "7-sided-cookie", strings = ["shape=7-sided cookie"])
@OverrideVariant(name = "9-sided-cookie", strings = ["shape=9-sided cookie"])
@OverrideVariant(name = "12-sided-cookie", strings = ["shape=12-sided cookie"])
@OverrideVariant(name = "ghost-ish", strings = ["shape=ghost-ish"])
@OverrideVariant(name = "4-leaf-clover", strings = ["shape=4-leaf clover"])
@OverrideVariant(name = "8-leaf-clover", strings = ["shape=8-leaf clover"])
@OverrideVariant(name = "burst", strings = ["shape=burst"])
@OverrideVariant(name = "soft-burst", strings = ["shape=soft burst"])
@OverrideVariant(name = "boom", strings = ["shape=boom"])
@OverrideVariant(name = "soft-boom", strings = ["shape=soft boom"])
@OverrideVariant(name = "flower", strings = ["shape=flower"])
@OverrideVariant(name = "puffy", strings = ["shape=puffy"])
@OverrideVariant(name = "puffy-diamond", strings = ["shape=puffy diamond"])
@OverrideVariant(name = "pixel-circle", strings = ["shape=pixel circle"])
@OverrideVariant(name = "pixel-triangle", strings = ["shape=pixel triangle"])
@OverrideVariant(name = "bun", strings = ["shape=bun"])
@OverrideVariant(name = "heart", strings = ["shape=heart"])
@Composable
fun MaterialShapesSticker() = Sticker {
  Box(
    Modifier.size(72.dp)
      .clip(catalogShape().toShape())
      .background(MaterialTheme.colorScheme.primary)
  )
}
