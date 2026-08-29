package ee.schimke.wearm3catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.google.android.horologist.auth.composables.material3.models.AccountUiModel
import com.google.android.horologist.images.base.paintable.Paintable
import com.google.android.horologist.media.ui.state.model.MediaUiModel
import com.google.android.horologist.media.ui.state.model.TrackPositionUiModel
import kotlin.time.Duration.Companion.seconds

/**
 * The seed data the Horologist stickers render.
 *
 * Horologist's components take **state**, not slots: `PlayerScreen` renders a `PlayerUiState`,
 * `MediaInfoDisplay` a `MediaUiModel`, `SelectAccountScreen` a list of `AccountUiModel`. A Wear
 * Compose sticker can pass a `Text` and be done; these cannot, so the state has to live somewhere,
 * and it lives here rather than being re-typed in each sticker — three stickers seeded three
 * slightly different ways is three renders that differ for a reason nobody intended.
 *
 * Everything below is **deterministic and offline**. No clock, no network, no `ImageLoader`: the
 * artwork is [CatalogArtwork] wrapped as a [Paintable] rather than a `CoilPaintable`, which is the
 * type Horologist's own samples use and the one thing in these APIs that would otherwise reach out
 * of the render. A nightly capture has to come out byte-identical to the last one or the delivery
 * branch's history turns into noise (AGENTS.md).
 *
 * The copy is the kit's — see [KitCopy.MEDIA_TITLE] and [KitCopy.MEDIA_ARTIST] — so a media sticker
 * and the kit cell it is compared against say the same words.
 */
object HorologistSamples {

  /**
   * The stand-in album art, as the type Horologist takes.
   *
   * [Paintable] is Horologist's image abstraction and every artwork parameter in these APIs is one.
   * The production implementation is `CoilPaintable`, which resolves a URI through an
   * `ImageLoader`; a catalog render must not, so this returns the repo's drawn placeholder
   * directly.
   */
  object Artwork : Paintable {
    @Composable override fun rememberPainter(): Painter = CatalogArtwork
  }

  /** A track that is playing: what the kit's `Media-Player` cell draws. */
  fun media(title: String, artist: String): MediaUiModel.Ready =
    MediaUiModel.Ready(id = "catalog-track", title = title, subtitle = artist, artwork = Artwork)

  /**
   * A track position part-way through, at the kit's own `Progress=` values.
   *
   * The kit publishes its main control at `Progress=80%` and `Progress=20%`, so the progress ring
   * around the play/pause button is seeded from a percentage rather than from a running clock. The
   * duration and position are only what the percentage implies; nothing in these components shows
   * them as text, and a real elapsed time would make the render non-deterministic.
   */
  fun position(percent: Float): TrackPositionUiModel.Actual =
    TrackPositionUiModel.Actual(
      percent = percent,
      duration = TRACK_DURATION_SECONDS.seconds,
      position = (TRACK_DURATION_SECONDS * percent).toInt().seconds,
    )

  /** The accounts `SelectAccountScreen` lists. Example.com, which is reserved for exactly this. */
  val accounts: List<AccountUiModel> =
    listOf(
      AccountUiModel(email = "maya@example.com", name = "Maya"),
      AccountUiModel(email = "sam@example.com", name = "Sam"),
    )

  /** The single account the signed-in confirmation greets. */
  val account: AccountUiModel = accounts.first()

  private const val TRACK_DURATION_SECONDS = 210
}
