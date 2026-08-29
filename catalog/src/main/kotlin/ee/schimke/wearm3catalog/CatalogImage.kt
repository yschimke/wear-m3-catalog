package ee.schimke.wearm3catalog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.max

/**
 * The kit's empty-image placeholder, for the slots where the kit's own cell is empty.
 *
 * Thirty of the kit's mapped nodes carry a Figma `IMAGE` fill with no image behind it, which Figma
 * renders as its empty-fill checkerboard: every `Content type=Image` / `Gallery` cell on
 * `ApplicationCard` and `TitledCard`, and all four `ImageBackgroundButton` cells. The kit is saying
 * "an image goes here", which is what this painter says, so the two placeholders should agree
 * rather than disagree over sample data.
 *
 * **Use this only where the kit's cell is itself empty.** Where the kit draws real content — the
 * app avatar, media artwork — it is the wrong picture, and [CatalogArtwork] is the stand-in.
 * Drawing a placeholder over a slot the kit fills does not move the comparison closer; it just
 * disagrees differently.
 *
 * Drawn rather than shipped. A committed photograph would be the only asset in the repository and
 * would put a licence question in front of every contributor. A checkerboard weighs nothing,
 * renders identically on every publish, and reads as placeholder rather than as content.
 *
 * The geometry is the kit's, and matching it is what makes this worth doing. Figma scales the tile
 * to **cover** the rect — `FILL` scale mode: aspect preserved, centred, cropped — not stretched to
 * fit. So the squares stay square whatever the slot's aspect ratio, sized off the longer edge, and
 * a wide slot shows a horizontal band through the tile's middle rather than eight squashed rows.
 * The transforms say so directly: the 172×52 button carries `matrix(0.0025 0 0 0.00826923 0
 * -1.15385)`, which is the 400px tile drawn 172 wide — 3.31× the rect's height — and offset by
 * `(52 - 172) / 2`.
 *
 * [PHASE] is the one number measured rather than derived: the tile's grid does not start at its own
 * edge, its bands breaking at 26px and every 50px after in a 400px image. Without it the squares
 * land half a step out and the pattern inverts, which costs about as much as not matching at all.
 *
 * Drawing inside our own frame rather than masking the region keeps the comparison honest: a frame
 * of the wrong size or the wrong count still diffs, because the squares move with it.
 *
 * It is sample *data*, not a substitute for the component: the sticker still calls the real
 * `Button` / `Card` overload that takes a `Painter`.
 */
object CatalogImage : Painter() {
  /** Squares across the tile, which covers the longer edge of whatever slot it is drawn into. */
  private const val CHECKS = 8

  /**
   * Where the grid starts, in squares, relative to the covering tile's leading edge.
   *
   * Measured off the kit's own tile: its bands break at 26px and every 50px after, in a 400px
   * image, so the grid origin sits 24/50 of a square outside the tile on both axes.
   */
  private const val PHASE = -0.48f

  private val Light = Color(0xFFFFFFFF)
  private val Dark = Color(0xFFD9D9D9)

  override val intrinsicSize: Size = Size.Unspecified

  override fun DrawScope.onDraw() {
    drawRect(Light)
    // Cover, not fit: the tile is square and spans the longer edge, so the shorter one is cropped
    // to the middle of it — which is what keeps a square square in a 172x52 slot.
    val cover = max(size.width, size.height)
    val square = cover / CHECKS
    val origin =
      Offset(
        (size.width - cover) / 2f + PHASE * square,
        (size.height - cover) / 2f + PHASE * square,
      )
    // One band past each edge, since the phase pushes a square across it; clip, because a painter
    // that ran outside its own bounds would paint over the component.
    clipRect {
      for (row in 0..CHECKS) {
        for (column in 0..CHECKS) {
          if ((row + column) % 2 == 0) continue
          drawRect(
            color = Dark,
            topLeft = Offset(origin.x + column * square, origin.y + row * square),
            size = Size(square, square),
          )
        }
      }
    }
  }
}

/**
 * Stand-in imagery for the slots where the kit draws real content rather than a placeholder.
 *
 * The app avatar is the clear case: the kit's `App Card` cells draw an `Avatar-AppEg` instance — a
 * vector app icon with a badge — and the base cell (`38437:5712`) carries no image fill at all.
 * Media artwork is the same: the kit's media cells ship a real illustration. Drawing
 * [CatalogImage]'s checkerboard into either would be answering a filled slot with a placeholder.
 *
 * Drawn rather than shipped, for the reason on [CatalogImage]. A deterministic gradient is honest
 * about being sample data, weighs nothing, and renders identically on every publish — which a
 * catalog whose delivery branch is diffed over time needs. A solid colour would not read as an
 * image at all and would make the scrim these components apply invisible.
 *
 * It is not a match for the kit's icon, and is not claimed as one — it is a stand-in that reads as
 * imagery. Closing that gap means drawing the avatar the kit actually draws, which is a separate
 * question from what an empty slot should hold.
 */
object CatalogArtwork : Painter() {
  override val intrinsicSize: Size = Size.Unspecified

  override fun DrawScope.onDraw() {
    drawRect(
      Brush.linearGradient(
        colors = listOf(Color(0xFF2B4C7E), Color(0xFF567EBB), Color(0xFF9BB7D4)),
        start = Offset.Zero,
        end = Offset(size.width, size.height),
      )
    )
  }
}
