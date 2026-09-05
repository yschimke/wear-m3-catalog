package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.state.RemoteImageBitmap
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint

/**
 * The placeholder the kit's reference carries, as the type Remote Compose can draw.
 *
 * Thirty of the kit's forty-five `Card` cells put imagery in the content slot — ten `Image`, ten
 * `Gallery 1`, ten `Gallery 2` — and Figma publishes every one of them with an `IMAGE` fill that
 * has no image behind it. `design-parity` normalises that to a flat fill at import, so the
 * reference draws [Fill] and so does `:catalog`'s `CatalogImage`. This is the same contract, in the
 * one type Remote can carry.
 *
 * ## Why this is a bitmap where `:catalog` uses a Painter
 *
 * A `RemoteDocument` is serialised and replayed by a player that never runs our code, so there is
 * no draw callback to hand it: `RemoteImage` and `remoteContainerPainter` both take a
 * `RemoteImageBitmap`, which means real pixels baked into the document. `:catalog` can draw its
 * placeholder procedurally; Remote has to ship one.
 *
 * That asymmetry is most of why these cells went undrawn here. It was a real obstacle while the
 * placeholder was Figma's checkerboard — a tile to encode, phase-aligned, at every slot's aspect
 * ratio. Against a flat fill it costs [SizePx] squared pixels of one colour, which is why these
 * variants became worth adding now.
 *
 * ## Size
 *
 * A solid colour carries no detail to lose, so the bitmap only has to be big enough that no scaler
 * treats it as degenerate — it is stretched to whatever slot draws it, and every sample is [Fill].
 * Small keeps the document small, which for a format that is transmitted to a watch is the point.
 */
object CatalogRemoteImage {
  /**
   * The mean of Figma's two tile colours, `#FFFFFF` and `#D9D9D9`.
   *
   * The same value `:catalog`'s `CatalogImage` draws and design-parity's `flat` mode paints. All
   * three have to agree for the comparison to mean anything, and nothing checks that automatically.
   */
  private val Fill = Color(0xFFECECEC)

  private const val SizePx = 8

  /**
   * The stand-in for a slot the kit draws real content into, rather than an empty fill.
   *
   * The kit's `App Card` cell puts an `Avatar-AppEg` instance in its leading slot — a coloured app
   * icon, not a placeholder — so answering it with [Fill] drew a white square where the kit draws
   * artwork, and the compare page reported that against the Wear sibling's own stand-in on
   * seventeen AppCard rows ([#294](https://github.com/yschimke/wear-m3-catalog/issues/294)).
   *
   * The gradient and its three stops are `:catalog`'s `CatalogArtwork`, transcribed here on the
   * same terms [KitCopy] is: the sibling lives in another module and publishes no artifact carrying
   * them, and two stand-ins for one slot have to be the same picture or the row compares the
   * catalogs rather than the libraries. It is baked at [ArtworkSizePx] because a gradient does
   * carry detail — unlike [Fill] it cannot be one pixel stretched.
   */
  @Composable
  fun artwork(): RemoteImageBitmap {
    val image = remember {
      ImageBitmap(ArtworkSizePx, ArtworkSizePx).also { target ->
        val side = ArtworkSizePx.toFloat()
        Canvas(target)
          .drawRect(
            left = 0f,
            top = 0f,
            right = side,
            bottom = side,
            paint =
              Paint().apply {
                shader =
                  LinearGradientShader(
                    from = Offset.Zero,
                    to = Offset(side, side),
                    colors = ArtworkStops,
                  )
              },
          )
      }
    }
    return image.rb
  }

  /** `:catalog`'s `CatalogArtwork` stops, in its order. */
  private val ArtworkStops = listOf(Color(0xFF2B4C7E), Color(0xFF567EBB), Color(0xFF9BB7D4))

  private const val ArtworkSizePx = 32

  /** The placeholder, ready for `RemoteImage` or `containerPainter`. */
  @Composable
  fun bitmap(): RemoteImageBitmap {
    val image = remember {
      ImageBitmap(SizePx, SizePx).also { target ->
        Canvas(target)
          .drawRect(
            left = 0f,
            top = 0f,
            right = SizePx.toFloat(),
            bottom = SizePx.toFloat(),
            paint = Paint().apply { color = Fill },
          )
      }
    }
    return image.rb
  }
}
