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
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker

// The kit's `Shapes` page is a specimen sheet, not a component sheet: named shapes drawn as filled
// silhouettes. The point of the sticker is the outline, so anything inside it would only be
// something else to compare.
//
// THE FILL IS `primaryDim`, NOT `primary`
//
// Every cell of the kit's set binds its fill to the `primary/primary-dim` variable, `#D0BCFF` —
// which is Wear M3's `PrimaryDim` token (`PaletteTokens.Primary80`). `primary` is `Primary90`,
// `#E9DDFF`, a visibly lighter lavender: a silhouette drawn in it differs from the kit over the
// whole of its area, which is most of the sticker
// ([#144](https://github.com/yschimke/wear-m3-catalog/issues/144)). A specimen sheet whose only
// content is one flat fill has to name the role the kit named.
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
 * Every shape in the kit's set, as the closed value set the `shape` knob offers.
 *
 * `@KnobValue` carries **the kit's own variant value**, lower-cased, and the constant name carries
 * Compose's. The seed and the kit-index resolver both speak the annotation's spelling, so the two
 * vocabularies live side by side instead of one having to win: `hexagon` is `MaterialShapes
 * .ClamShell`, `pantagon` is the kit's own spelling of Pentagon, and the cookies and clovers put
 * their count first — none of which is a legal Kotlin identifier, which is why the value is
 * declared rather than the constant renamed to it.
 *
 * The polygon rides on the constant rather than in a parallel table. A `Pair` list could disagree
 * with itself — a key with no shape, a shape reachable under no key — and [catalogShape] had to
 * carry an `?: Circle` fallback for a lookup that could miss. An enum constant cannot be missing
 * its own property, so that whole failure mode is gone rather than guarded.
 */
enum class CatalogShape(val polygon: RoundedPolygon) {
  @KnobValue("circle") Circle(MaterialShapes.Circle),
  @KnobValue("square") Square(MaterialShapes.Square),
  @KnobValue("slanted") Slanted(MaterialShapes.Slanted),
  @KnobValue("arch") Arch(MaterialShapes.Arch),
  @KnobValue("fan") Fan(MaterialShapes.Fan),
  @KnobValue("arrow") Arrow(MaterialShapes.Arrow),
  @KnobValue("semicircle") Semicircle(MaterialShapes.SemiCircle),
  @KnobValue("oval") Oval(MaterialShapes.Oval),
  @KnobValue("pill") Pill(MaterialShapes.Pill),
  @KnobValue("triangle") Triangle(MaterialShapes.Triangle),
  @KnobValue("diamond") Diamond(MaterialShapes.Diamond),
  @KnobValue("hexagon") Hexagon(MaterialShapes.ClamShell),
  @KnobValue("pantagon") Pantagon(MaterialShapes.Pentagon),
  @KnobValue("gem") Gem(MaterialShapes.Gem),
  @KnobValue("very sunny") VerySunny(MaterialShapes.VerySunny),
  @KnobValue("sunny") Sunny(MaterialShapes.Sunny),
  @KnobValue("4-sided cookie") `4SidedCookie`(MaterialShapes.Cookie4Sided),
  @KnobValue("6-sided cookie") `6SidedCookie`(MaterialShapes.Cookie6Sided),
  @KnobValue("7-sided cookie") `7SidedCookie`(MaterialShapes.Cookie7Sided),
  @KnobValue("9-sided cookie") `9SidedCookie`(MaterialShapes.Cookie9Sided),
  @KnobValue("12-sided cookie") `12SidedCookie`(MaterialShapes.Cookie12Sided),
  @KnobValue("ghost-ish") GhostIsh(MaterialShapes.Ghostish),
  @KnobValue("4-leaf clover") `4LeafClover`(MaterialShapes.Clover4Leaf),
  @KnobValue("8-leaf clover") `8LeafClover`(MaterialShapes.Clover8Leaf),
  @KnobValue("burst") Burst(MaterialShapes.Burst),
  @KnobValue("soft burst") SoftBurst(MaterialShapes.SoftBurst),
  @KnobValue("boom") Boom(MaterialShapes.Boom),
  @KnobValue("soft boom") SoftBoom(MaterialShapes.SoftBoom),
  @KnobValue("flower") Flower(MaterialShapes.Flower),
  @KnobValue("puffy") Puffy(MaterialShapes.Puffy),
  @KnobValue("puffy diamond") PuffyDiamond(MaterialShapes.PuffyDiamond),
  @KnobValue("pixel circle") PixelCircle(MaterialShapes.PixelCircle),
  @KnobValue("pixel triangle") PixelTriangle(MaterialShapes.PixelTriangle),
  @KnobValue("bun") Bun(MaterialShapes.Bun),
  @KnobValue("heart") Heart(MaterialShapes.Heart),
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
fun MaterialShapesSticker(shape: CatalogShape = CatalogShape.Circle) = Sticker {
  Box(
    Modifier.size(72.dp)
      .clip(shape.polygon.toShape())
      .background(MaterialTheme.colorScheme.primaryDim)
  )
}
