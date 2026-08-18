package ee.schimke.wearm3catalog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

/**
 * Stand-in imagery for the kit's image-backed components — the background-image button and card.
 *
 * Drawn rather than shipped. A committed photograph would be the only asset in the repository and
 * would put a licence question in front of every contributor, while a solid colour would not read
 * as an image at all and would make the scrim these components apply invisible. A deterministic
 * gradient is honest about being sample data, weighs nothing, and renders identically on every
 * publish — which a catalog whose delivery branch is diffed over time needs.
 *
 * It is sample *data*, not a substitute for the component: the sticker still calls the real
 * `Button` / `Card` overload that takes a `Painter`.
 */
object CatalogImage : Painter() {
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
