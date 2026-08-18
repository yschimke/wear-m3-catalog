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
// Two cells today, by design: this catalog is being stood up end to end — build, discover, render,
// publish, compare — before the sweep fans the remaining shapes out.

internal val SHAPE_SET: List<Pair<String, RoundedPolygon>> =
  listOf("circle" to MaterialShapes.Circle, "square" to MaterialShapes.Square)

@Composable
private fun catalogShape(): RoundedPolygon {
  val key = previewOverrideString("shape", "circle")
  return SHAPE_SET.firstOrNull { it.first == key }?.second ?: MaterialShapes.Circle
}

@CatalogComponent(
  id = "Shape/MaterialShapes",
  reference = "figma:B24oss2tTeXAFykyeyusz0/42284:176650",
  caption = "The expressive shape library, with each named shape folded in as a variant.",
)
@CatalogModes
@OverrideVariant(name = "square", strings = ["shape=square"])
@Composable
fun MaterialShapesSticker() = Sticker {
  Box(
    Modifier.size(72.dp)
      .clip(catalogShape().toShape())
      .background(MaterialTheme.colorScheme.primary)
  )
}
