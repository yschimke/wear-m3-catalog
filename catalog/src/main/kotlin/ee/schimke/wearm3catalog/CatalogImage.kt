package ee.schimke.wearm3catalog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.Painter

/**
 * Stand-in imagery for the kit's image-backed components — the background-image button and card.
 *
 * This draws the placeholder the kit draws. Thirty of the kit's mapped nodes — every `Content
 * type=Image` / `Gallery` cell on `ApplicationCard` and `TitledCard`, and all four
 * `ImageBackgroundButton` cells — carry a Figma `IMAGE` fill with no image behind it, which Figma
 * renders as its empty-fill checkerboard. The kit is saying "an image goes here", which is exactly
 * what this painter says, so the two placeholders should agree rather than disagree over sample
 * data. Before this they didn't, and the disagreement was worth 29% of an app card's cell and the
 * whole of a background-image one — noise the eye has to discount before it can reach the real
 * divergence underneath.
 *
 * Drawn rather than shipped, as it was before. A committed photograph would be the only asset in
 * the repository and would put a licence question in front of every contributor, while a solid
 * colour would not read as an image at all and would make the scrim these components apply
 * invisible. A checkerboard keeps every property the gradient was chosen for — no licence, no
 * bytes, identical on every publish — and still reads as placeholder rather than as content.
 *
 * The geometry is the kit's, and matching it is what makes this worth doing. Figma's pattern is
 * `patternContentUnits="objectBoundingBox"`, so its 400×400 tile stretches to whatever rect it
 * fills: always [CHECKS] squares across each axis, at any size and any aspect ratio. Deriving the
 * square from `size` reproduces that at every frame the catalog draws, with nothing to tune per
 * cell. [PHASE] is the one number that has to be measured rather than assumed — the tile's grid
 * does not start at its own edge — and without it the squares land half a step out and invert,
 * which would cost about as much as not matching at all. With it the fill is pixel-identical to the
 * kit's tile.
 *
 * Drawing inside our own frame rather than masking the region keeps the comparison honest: a frame
 * of the wrong size or the wrong count still diffs, because the squares move with it. Matching the
 * fill must not buy agreement by giving up the geometry around it.
 *
 * It is sample *data*, not a substitute for the component: the sticker still calls the real
 * `Button` / `Card` overload that takes a `Painter`.
 */
object CatalogImage : Painter() {
  /** Squares across each axis, matching the tile Figma stretches over an empty image fill. */
  private const val CHECKS = 8

  /**
   * Where the grid starts, in squares, relative to the frame's leading edge.
   *
   * Measured off the kit's own tile: its bands break at 26px and every 50px after, in a 400px
   * image, so the grid origin sits 24/50 of a square outside the frame on both axes.
   */
  private const val PHASE = -0.48f

  private val Light = Color(0xFFFFFFFF)
  private val Dark = Color(0xFFD9D9D9)

  override val intrinsicSize: Size = Size.Unspecified

  override fun DrawScope.onDraw() {
    drawRect(Light)
    val square = Size(size.width / CHECKS, size.height / CHECKS)
    val origin = Offset(PHASE * square.width, PHASE * square.height)
    // The phase shift pushes a square past each trailing edge, so draw one band over on each axis
    // and clip: a painter that ran outside its own bounds would paint over the component.
    clipRect {
      for (row in 0..CHECKS) {
        for (column in 0..CHECKS) {
          if ((row + column) % 2 == 0) continue
          drawRect(
            color = Dark,
            topLeft = Offset(origin.x + column * square.width, origin.y + row * square.height),
            size = square,
          )
        }
      }
    }
  }
}
