package ee.schimke.wearm3catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.google.android.horologist.images.base.paintable.Paintable

/**
 * The account photographs the sign-in surfaces seed from.
 *
 * [CatalogArtwork] is the stand-in for *imagery*, and it is a gradient, which is honest for album
 * art and for the kit's app glyph. It is not honest here: an account row's leading slot holds a
 * FACE, and a reader looking at `Auth/SelectAccountScreen` has to be able to tell whether the
 * component sized, placed and shaped one. A rectangle of colour answers the first two questions and
 * not the third, and a drawn figure answers the third only approximately — at
 * `ButtonDefaults.LargeIconSize` the question is whether a *photograph* survives the slot, which
 * only a photograph can answer.
 *
 * ## Why these two, and what licence they carry
 *
 * Both are real photographs from [inter-faces](https://github.com/cjdowner/interfaces), a pack of
 * 100 avatar crops assembled from community stock photography and released under
 * [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) — free for commercial use,
 * modifiable, redistributable, **attribution not required**. Credited anyway, because a contributor
 * who finds two photographs of strangers in a design repository deserves to be told where they came
 * from without having to ask: see
 * [`docs/THIRD_PARTY_IMAGES.md`](../../../../../../../docs/THIRD_PARTY_IMAGES.md).
 *
 * This deliberately reverses the "drawn rather than shipped" reasoning on [CatalogImage], which
 * argued that a committed photograph puts a licence question in front of every contributor. That
 * argument holds for a *placeholder*, where a gradient loses nothing. It does not hold for the one
 * slot whose whole subject is a human face — and a CC0 pack answers the licence question in the
 * file that records it rather than leaving it open. The two are 256x256 JPEGs, ~45 KB each.
 *
 * They are chosen to be **distinguishable at 32dp**: a light, high-key frame against a dark,
 * low-key one. Telling two accounts apart is the account row's entire job, and two portraits that
 * resolve to the same grey blur at `LargeIconSize` would fail it while looking fine in review.
 *
 * ## The circular crop, and why it belongs here
 *
 * `SelectAccountScreen` draws the account's `avatar` through a bare `Image` with no `clip`, so an
 * unmasked rectangle — which is exactly what a photograph is, and what a `CoilPaintable` hands it
 * in production — lands SQUARE in the account row. That is a library gap, reported at
 * [#435](https://github.com/yschimke/wear-m3-catalog/issues/435).
 *
 * What the API gives a caller is the PIXELS: `Paintable` returns any `Painter`, so the mask goes on
 * here, before the component ever sees the image. That is not covering for the library — it is what
 * every call site has to do today, and what Horologist's own fixtures do
 * (`horologist_avatar_small_1` and `_2`, the two the `SelectAccountScreen` preview and screenshot
 * test pass, are pre-rounded PNGs whose corners decode to `(0, 0, 0, 0)`; `_3`, used only by the
 * confirmation dialog, is not, because that component clips to its own pill).
 *
 * It survives the fix, too: a circle clipped to a circle is unchanged the day #435 lands.
 */
object CatalogAvatar {

  /** Maya's photograph — light frame, so it reads against a dark row. */
  val First: Paintable = ResourceAvatar(R.drawable.catalog_avatar_1)

  /** Sam's photograph — dark frame, so the two do not blur into one another at 32dp. */
  val Second: Paintable = ResourceAvatar(R.drawable.catalog_avatar_2)

  private class ResourceAvatar(private val resId: Int) : Paintable {
    @Composable
    override fun rememberPainter(): Painter {
      val source = painterResource(resId)
      return remember(source) { CircleCropPainter(source) }
    }
  }

  /**
   * Draws [source] into the largest centred circle the frame allows.
   *
   * Centred and square rather than stretched to the frame: `SignedInConfirmationDialog` scales its
   * avatar with `ContentScale.FillBounds`, so a portrait laid out against a non-square frame would
   * come out skewed — a face is the one subject where that is unmissable.
   */
  private class CircleCropPainter(private val source: Painter) : Painter() {

    // Unspecified, so the component's own sizing wins. An intrinsic size here would fight
    // `Modifier.size(LargeIconSize)` in the row and `fillMaxSize()` in the dialog.
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
      val side = minOf(size.width, size.height)
      val left = (size.width - side) / 2f
      val top = (size.height - side) / 2f
      clipPath(Path().apply { addOval(Rect(Offset(left, top), Size(side, side))) }) {
        translate(left, top) { with(source) { draw(Size(side, side)) } }
      }
    }
  }
}
