package ee.schimke.wearm3catalog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

/**
 * The placeholder the kit's reference carries, for the slots where the kit's own cell is empty.
 *
 * Thirty of the kit's mapped nodes carry a Figma `IMAGE` fill with no image behind it — every
 * `Content type=Image` / `Gallery` cell on `ApplicationCard` and `TitledCard`, and all four
 * `ImageBackgroundButton` cells. Figma renders that as its checkerboard, but the reference does not
 * keep it: `design-parity` normalises an empty image fill to a flat fill at import
 * (`--placeholder-fill`, default `flat`), and [Fill] is what it paints — the mean of Figma's own
 * two tile colours.
 *
 * **This is a contract with the reference, not a picture of Figma.** The two sides have to draw the
 * same thing for the comparison to mean anything, and nothing checks that automatically. If the
 * import's mode changes, this changes with it.
 *
 * ## Why flat, and not the checkerboard it replaces
 *
 * Because a checkerboard is the worst possible content to measure against. It converts a small
 * geometry error into a large pixel difference and then stops responding. Over a 236x132 region
 * against the kit's own grid, the share of pixels the diff reports:
 * ```
 *   error                                checkerboard   flat
 *   1dp shift                                    6.8%   0.8%
 *   3dp shift                                   20.3%   2.5%
 *   10dp shift                                  67.8%   8.5%
 *   a 42dp slot against the kit's 64dp          49.6%  55.9%
 * ```
 *
 * It amplifies about 8x, and by 10dp reads the same as a component that is entirely wrong. The last
 * row is the real problem: two checkerboards at different PITCHES differ in ~50% of pixels however
 * small the underlying error, because the grids decorrelate — a coin flip rather than a
 * measurement. This catalog proved it the expensive way: making the fill *more* faithful to Figma,
 * cover-fitting the tile exactly as `FILL` scale mode does, made the slot variants score WORSE,
 * because the square size then scaled with our own wrong slot height and the error was charged
 * twice.
 *
 * A flat fill is the only option that stays monotonic across the range. The residual over these
 * slots is then geometry and nothing else.
 *
 * ## Why drawn rather than shipped
 *
 * A committed photograph would be the only asset in the repository and would put a licence question
 * in front of every contributor. This weighs nothing and renders identically on every publish,
 * which a catalog whose delivery branch is diffed over time needs.
 *
 * **Use this only where the kit's cell is itself empty.** Where the kit draws real content — the
 * app avatar, media artwork — [CatalogArtwork] is the stand-in. Drawing a placeholder over a slot
 * the kit fills does not move the comparison closer; it disagrees differently.
 *
 * It is sample *data*, not a substitute for the component: the sticker still calls the real
 * `Button` / `Card` overload that takes a `Painter`.
 */
object CatalogImage : Painter() {
  /**
   * The mean of Figma's two tile colours, `#FFFFFF` and `#D9D9D9`.
   *
   * Derived rather than chosen, so it is the same value design-parity paints without either side
   * having to know a constant the other picked.
   */
  private val Fill = Color(0xFFECECEC)

  override val intrinsicSize: Size = Size.Unspecified

  override fun DrawScope.onDraw() {
    drawRect(Fill)
  }
}

/**
 * The kit's empty image fill with the flat 50% black overlay used by `Button-ImageBackground`.
 *
 * Wear's [androidx.wear.compose.material3.ButtonDefaults.containerPainter] intentionally adds a
 * directional scrim. The kit cell instead exports one uniform overlay across the full container, so
 * the catalog supplies that artwork through the real `Button` painter overload rather than
 * substituting a hand-built button.
 */
object CatalogImageWithFlatScrim : Painter() {
  override val intrinsicSize: Size = Size.Unspecified

  override fun DrawScope.onDraw() {
    drawRect(Color(0xFFECECEC))
    drawRect(Color.Black.copy(alpha = 0.5f))
  }
}

/**
 * Stand-in imagery for the slots where the kit draws real content rather than a placeholder.
 *
 * The app avatar is the clear case: the kit's `App Card` cells draw an `Avatar-AppEg` instance — a
 * vector app icon with a badge — and the base cell (`38437:5712`) carries no image fill at all.
 * Media artwork is the same: the kit's media cells ship a real illustration. Drawing
 * [CatalogImage]'s placeholder into either would be answering a filled slot with a blank.
 *
 * Drawn rather than shipped, for the reason on [CatalogImage]. A deterministic gradient is honest
 * about being sample data, weighs nothing, and renders identically on every publish. A solid colour
 * would not read as an image at all and would make the scrim these components apply invisible.
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
