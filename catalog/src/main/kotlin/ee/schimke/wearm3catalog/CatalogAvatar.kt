package ee.schimke.wearm3catalog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter

/**
 * A portrait stand-in for the slots that hold a person's photo — the account avatar.
 *
 * [CatalogArtwork] is the stand-in for *imagery*, and it is a gradient, which is honest for album
 * art and for the kit's app glyph. It is not honest for an avatar: an account row's leading slot
 * holds a FACE, and a reader looking at `Auth/SelectAccountScreen` has to be able to tell whether
 * the component sized, placed and shaped one. A rectangle of colour answers the first two questions
 * and not the third.
 *
 * ## Drawn, not shipped, and not a photograph
 *
 * This is a **representational drawing** — head, shoulders, hair, a collar — rather than a
 * photograph, and it does not pretend otherwise at any size. Three reasons it is generated here
 * instead of committed as an asset, in descending order of how much they would cost:
 * 1. A committed photograph of a person is a licence question AND a likeness question in front of
 *    every contributor, on a repository whose delivery branch republishes every render nightly.
 * 2. It weighs nothing and renders identically on every publish, which is the same determinism
 *    argument [CatalogImage] makes: a catalog whose history is diffed cannot afford a byte to move
 *    for a reason nobody intended.
 * 3. It scales. The account row draws it at `ButtonDefaults.LargeIconSize` and the confirmation
 *    dialog at 96dp; both come out of the same numbers because every coordinate below is a fraction
 *    of the frame, not a pixel.
 *
 * ## It clips itself, and that is the point
 *
 * Everything is drawn inside a circular clip, so the painter hands the component an image that is
 * **already masked** — which is exactly what a real app does, and exactly what Horologist's own
 * previews and screenshot tests do (their `horologist_avatar_small_*.png` fixtures are pre-rounded
 * PNGs; all four corners decode to `(0, 0, 0, 0)`).
 *
 * That matters because `SelectAccountScreen` applies no clip of its own
 * ([#435](https://github.com/yschimke/wear-m3-catalog/issues/435)), so an unmasked rectangle lands
 * square in the account row. Masking here is not covering for the library: it is the call site
 * doing what every call site has to do today, and the catalog publishing what an app would actually
 * see. The library's missing clip is reported upstream rather than drawn.
 *
 * @param background the two-stop ground behind the subject, top to bottom.
 * @param skin the lit side of the face.
 * @param skinShadow the neck and ears, which sit behind the plane of the face.
 * @param hair drawn twice — once behind the head, once as a fringe across the forehead.
 * @param shirt the shoulders, which the circle crops into a band along the bottom.
 * @param shirtShadow the collar's opening under the neck.
 * @param feature eyes and mouth. Deliberately low-contrast: at `LargeIconSize` these are about a
 *   pixel each, and a hard black reads as speckle rather than as a face.
 */
class CatalogAvatar(
  private val background: List<Color>,
  private val skin: Color,
  private val skinShadow: Color,
  private val hair: Color,
  private val shirt: Color,
  private val shirtShadow: Color,
  private val feature: Color,
) : Painter() {

  override val intrinsicSize: Size = Size.Unspecified

  override fun DrawScope.onDraw() {
    // Square, centred: the frame is whatever the component asks for, the portrait is not stretched
    // to it. `SignedInConfirmationDialog` scales its avatar with `ContentScale.FillBounds`, so a
    // portrait laid out against the frame's own aspect would skew inside a non-square pill.
    val side = minOf(size.width, size.height)
    val left = (size.width - side) / 2f
    val top = (size.height - side) / 2f

    fun at(fx: Float, fy: Float) = Offset(left + side * fx, top + side * fy)

    fun oval(color: Color, cx: Float, cy: Float, rx: Float, ry: Float) =
      drawOval(color, at(cx - rx, cy - ry), Size(side * rx * 2f, side * ry * 2f))

    clipPath(Path().apply { addOval(Rect(Offset(left, top), Size(side, side))) }) {
      drawRect(
        Brush.verticalGradient(background, startY = top, endY = top + side),
        Offset(left, top),
        Size(side, side),
      )

      // Shoulders, wider than the frame so the circle crops them rather than showing their ends.
      oval(shirt, cx = 0.5f, cy = 1.12f, rx = 0.52f, ry = 0.40f)
      // The collar's opening: a half-disc under the neck, flat side up.
      drawArc(
        shirtShadow,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = at(0.34f, 0.70f),
        size = Size(side * 0.32f, side * 0.22f),
      )

      oval(skinShadow, cx = 0.5f, cy = 0.66f, rx = 0.10f, ry = 0.14f) // neck
      oval(hair, cx = 0.5f, cy = 0.38f, rx = 0.215f, ry = 0.235f) // hair, behind the head
      oval(skinShadow, cx = 0.315f, cy = 0.45f, rx = 0.030f, ry = 0.042f) // ears
      oval(skinShadow, cx = 0.685f, cy = 0.45f, rx = 0.030f, ry = 0.042f)
      oval(skin, cx = 0.5f, cy = 0.43f, rx = 0.185f, ry = 0.225f) // face

      // Fringe: the top half of an oval, filled to its centre, sitting across the forehead.
      drawArc(
        hair,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = at(0.30f, 0.20f),
        size = Size(side * 0.40f, side * 0.30f),
      )

      oval(feature, cx = 0.437f, cy = 0.455f, rx = 0.024f, ry = 0.018f) // eyes
      oval(feature, cx = 0.563f, cy = 0.455f, rx = 0.024f, ry = 0.018f)
      drawArc(
        feature,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = at(0.445f, 0.520f),
        size = Size(side * 0.110f, side * 0.055f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = side * 0.014f),
      )

      // A vignette, which is what stops the whole thing reading as a flat sticker.
      drawRect(
        Brush.radialGradient(
          colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
          center = at(0.5f, 0.5f),
          radius = side * 0.52f,
        ),
        Offset(left, top),
        Size(side, side),
      )
    }
  }

  companion object {
    /**
     * The two accounts the sign-in surfaces seed from, drawn as two different people.
     *
     * One portrait twice would read as a rendering bug in a two-row account list — the row's whole
     * job is telling accounts apart — so the palettes differ in every band, not only in hue.
     */
    val First =
      CatalogAvatar(
        background = listOf(Color(0xFF4A3A52), Color(0xFF211A28)),
        skin = Color(0xFFC98B63),
        skinShadow = Color(0xFFA96F4C),
        hair = Color(0xFF2B1C14),
        shirt = Color(0xFF4F6D8C),
        shirtShadow = Color(0xFF3A5570),
        feature = Color(0xFF4A3026),
      )

    val Second =
      CatalogAvatar(
        background = listOf(Color(0xFF2E4046), Color(0xFF152125)),
        skin = Color(0xFFE0B48D),
        skinShadow = Color(0xFFC08F6A),
        hair = Color(0xFF6B4A2F),
        shirt = Color(0xFF7A5A6E),
        shirtShadow = Color(0xFF5C4253),
        feature = Color(0xFF5A4232),
      )
  }
}
