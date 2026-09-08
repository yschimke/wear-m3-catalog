@file:Suppress("RestrictedApiAndroidX")
// `MaterialShapes` is expressive-experimental on the material3 1.5.0-alpha line this module already
// declares (for `androidx.compose.material3.ColorScheme`, see build.gradle.kts). Opted in at file
// scope rather than per-call: the whole file exists to draw that library.
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ee.schimke.wearm3catalog.remote

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.StandardRemotePaint
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.runtime.Composable
import androidx.graphics.shapes.RoundedPolygon
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// ---------------------------------------------------------------------------
// The kit's `Shapes` page, drawn as a RemoteDocument.
//
// The Wear column's `Shape/MaterialShapes` (`catalog/…/sections/Shapes.kt`) clips a 72dp box to
// `RoundedPolygon.toShape()`. This column cannot: a `RemoteShape` is resolved when the document is
// RECORDED and `remote-creation-compose` publishes only the corner-based family
// (`RemoteRoundedCornerShape`), so there is no shape object to hand `RemoteModifier.clip` for a
// 12-sided cookie. What it publishes instead is the polygon itself as a DRAW op —
// `RemoteDrawScope.drawRoundedPolygon`, which serialises the polygon's cubics into the document as
// path data — so the silhouette is painted rather than clipped out of a filled box. The picture is
// the same; the opcode is not, which is the whole point of the third column.
//
// THE POLYGON IS NORMALISED, AND THE CANVAS SCALE IS WHAT SIZES IT
//
// Every `MaterialShapes` constant is `.normalized()`: its bounds are exactly (0,0)-(1,1), so its
// cubics are a unit silhouette rather than anything in pixels. `drawRoundedPolygon` emits those
// cubics verbatim — it does no fitting of its own — so drawing one straight onto the canvas puts a
// one-pixel shape in the corner. Scaling the canvas by the drawing area is therefore not a
// convenience, it is the sizing step, and it is the same arithmetic `toShape()` does one column
// over (`Matrix().scale(size.width, size.height)`, then a centre-align that is a no-op on a
// normalised polygon). Doing it as a canvas transform rather than by baking pixel coordinates into
// the path keeps the document resolution-independent, which is what a RemoteDocument is for.
//
// THE FILL IS `primaryDim`, NOT `primary`
//
// The same finding as the Wear column
// ([#144](https://github.com/yschimke/wear-m3-catalog/issues/144)):
// every cell of the kit's set binds its fill to `primary/primary-dim`, `#D0BCFF`. A specimen sheet
// whose only content is one flat fill has to name the role the kit named, and
// `RemoteColorScheme.primaryDim` is that role on this side.
//
// ONE COMPONENT, ONE CELL PER SHAPE
//
// The kit models the whole page as ONE component set varying a single `Shape=` property, so this is
// one component with a shape axis rather than 35 components — the taxonomy rule in AGENTS.md, and
// the same fold the Wear column makes, under the same component id, so the two pair cell for cell
// on the compare page.
// ---------------------------------------------------------------------------

/**
 * Every shape in the kit's set, keyed by **the kit's own variant value**, lower-cased.
 *
 * The Wear column carries the identical table as a `@KnobValue`-annotated enum; this column reads
 * its axes through [previewOverrideChoice], which speaks strings, so the same two vocabularies live
 * side by side as a key and a constant instead of one having to win. `hexagon` is
 * `MaterialShapes.ClamShell`, `pantagon` is the kit's own spelling of Pentagon, and the cookies and
 * clovers put their count first — none of which is a legal Kotlin identifier.
 *
 * Ordered as the kit's `Shape=` axis is, so a reader can walk the two side by side.
 * [CATALOG_SHAPES] is what the knob offers and what the lookup reads, so a shape cannot be
 * reachable under no key or offered under a key that draws nothing.
 */
internal val CATALOG_SHAPES: Map<String, RoundedPolygon> =
  linkedMapOf(
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

/** The kit's base cell, and the shape a render with no seeded override draws. */
private const val BASE_SHAPE = "circle"

/**
 * The side of the drawn silhouette, and the same 72dp the Wear column's box is sized to.
 *
 * A number rather than a kit token because the kit's cells are a specimen grid: the set's frames
 * are editor geometry, and what the two columns have to agree on is that they draw the same
 * silhouette at the same size in the same frame. `72` is where that agreement is written down.
 */
private val SHAPE_SIZE = 72.rdp

@CatalogComponent(
  id = "Shape/MaterialShapes",
  group = "Shapes",
  parallel = "Shape/MaterialShapes",
  reference = "figma:B24oss2tTeXAFykyeyusz0/42284:176655",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/42284:176650",
  caption =
    "The expressive shape library painted as a RemoteDocument path, with each named shape folded " +
      "in as a variant.",
)
@CatalogRemoteModes
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
fun MaterialShapesRemote() = RemoteSticker {
  val choice = previewOverrideChoice("shape", BASE_SHAPE, CATALOG_SHAPES.keys.toList())
  // `?: Circle` is unreachable — the knob's option list IS this map's key set — and is here because
  // `previewOverrideChoice` returns a plain String, so the compiler cannot see that.
  val polygon = CATALOG_SHAPES[choice] ?: MaterialShapes.Circle
  // Read outside the canvas: the colour is a theme lookup, and the drawing lambda records opcodes.
  val fill = RemoteMaterialTheme.colorScheme.primaryDim
  RemoteCanvas(modifier = RemoteModifier.size(SHAPE_SIZE)) {
    val paint = StandardRemotePaint().apply { color = fill }
    // The unit polygon, scaled to the drawing area. See the note at the top of the file for why
    // this is the sizing step rather than a flourish.
    scale(size.width, size.height) { drawRoundedPolygon(polygon, paint) }
  }
}
