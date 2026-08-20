@file:CatalogGroup(name = "Media controls", section = "Horologist")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.horologist.media.ui.material3.components.MediaControlButtons
import com.google.android.horologist.media.ui.material3.components.MediaInfoDisplay
import com.google.android.horologist.media.ui.material3.components.PlayPauseProgressButton
import com.google.android.horologist.media.ui.material3.components.PodcastControlButtons
import com.google.android.horologist.media.ui.material3.components.actions.ShowPlaylistButton
import com.google.android.horologist.media.ui.material3.components.ambient.AmbientMediaControlButtons
import com.google.android.horologist.media.ui.material3.components.background.RadialBackground
import com.google.android.horologist.media.ui.material3.components.controls.SeekToNextButton
import com.google.android.horologist.media.ui.material3.components.controls.SeekToPreviousButton
import com.google.android.horologist.media.ui.material3.screens.player.PlayerScreen
import com.google.android.horologist.media.ui.state.model.MediaUiModel
import com.google.android.horologist.media.ui.state.model.TrackPositionUiModel
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.HorologistSamples
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.ScreenSticker
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's Media Controls page, rebuilt against HOROLOGIST rather than Wear Compose Material 3 —
// because Wear Compose does not publish a media player and Horologist does.
//
// WHY THIS PAGE IS DIFFERENT FROM EVERY OTHER ONE HERE
//
// Media Controls publishes exactly one component set — `Media-Player` (`71575:22328`), four cells
// across `Large Screen=` and `AOD=` — and that set is a whole 192×192 screen. Everything the screen
// is assembled from is private to the kit: `.Base / Media / Header`, `.Base / Media / Main
// Control`, `.Base / Media / Footer`, `.Base / Media / Album Artwork`. Private sets (names
// beginning `.`) are out of `kit-sets.json`'s scope by construction — the kit walk does not record
// them — so they are nodes this catalog can read but not join a coverage row to.
//
// EVERY COMPONENT HERE ENTERS THROUGH THE LIBRARY'S DOOR, and the player does so for a reason
// worth reading before mapping anything else on this page.
//
// The parts are the easy half: each says which private `.Base / Media / …` node draws it, since a
// private set carries no coverage row to join to.
//
// THE PLAYER IS THE INTERESTING HALF. It reproduces a set the kit really does publish, and it was
// mapped to the `Large Screen=No, AOD=No` cell (`71575:22329`) — which is a real, renderable node
// that looks exactly right on the Figma canvas. **The export is not the canvas.** That cell's last
// child is `.Base / Media / Album Artwork/Primary NEW`, a full-bleed 192×192 artwork that
// composites
// against the backdrop; exported on its own it stops compositing and simply covers the player. The
// published reference came out an opaque purple wash — no header, no transport row, no footer — and
// the comparison was a render diffed against a blurred background, which reports everything and
// means nothing.
//
// So the reference is WITHDRAWN rather than left pointing at a picture that is not the component,
// and `kit-sets.json` says so on the row. The lesson generalises, and AGENTS.md now carries it: a
// node being valid is not the test — the test is whether the node's EXPORT draws the component.
//
// The two `AOD=Yes` cells (`71575:22344`, `71575:22375`) carry no artwork instance at all and
// export faithfully, so this becomes mappable again the day the exporter composites that overlay.
// That is an upstream fix, not one to fork the pipeline for.
//
// AOD IS A CELL, NOT A COMPONENT. The kit varies its player on `AOD=`, and Horologist ships a
// parallel `ambient/` package (`AmbientMediaControlButtons`, `AmbientPlayPauseButton`, …) whose job
// is the same screen drawn for always-on: outlined rather than filled, no artwork behind it. That
// is a state axis, so it folds under its parent as an `@OverrideVariant` — one card, two renders —
// rather than doubling the sheet (AGENTS.md). It carries no `kitAxis`/`kitValue`: those resolve a
// cell against the kit THROUGH the component's base reference, and there is none to resolve
// through while the export is broken.
//
// LARGE SCREEN IS NOT EVEN A CELL. The kit's `Large Screen=Yes` cell is the same screen at the
// breakpoint, and `@CatalogFullScreenModes` already renders all five sizes the kit recognises.
// Horologist reads `LocalConfiguration` for it (`isLargeScreen`), so the 225dp and 240dp captures
// ARE the large-screen cell — with no seed, no knob and no extra card.
//
// THE PLAYER PUBLISHES THROUGH `ScreenSticker`, NOT `FullScreenSticker`. The kit's cell is not a
// bare component on a black disc: it carries a `TimeText`, a `Page-Indicator` and a
// `Level-Indicator-RSB` over the player (read the cell's children on `71575:22329`). The clock is
// the one of those three that comes from the frame rather than from the app, so the frame supplies
// it — pinned to `10:10`, like every other capture here. The two indicators are screen furniture an
// app positions around the player rather than parts of it, so the render does not draw them and the
// comparison reports them; saying so here is the rule for a difference Compose expresses elsewhere
// (AGENTS.md).
//
// A PARITY FINDING THE RENDERS SURFACED, WRITTEN DOWN RATHER THAN PAPERED OVER.
//
// The kit draws its main control with a progress arc AROUND the button — `Progress=80%` and
// `Progress=20%` on `.Base / Media / Main Control`. `PlayPauseProgressButton` hands ONE `modifier`
// to both halves of what it draws: the `Box` holding the progress ring, and the `FilledIconButton`
// inside it. Ring and button therefore come out the same diameter, and the opaque container covers
// the arc completely. There is no render on this sheet where the kit's `Progress=` reads.
//
// NOT EVEN THE AMBIENT ONE, which an earlier draft of this note claimed. The scalloped outline on
// the ambient cells is `AmbientPlayPauseButton`'s own SHAPE, not a progress arc — that composable
// takes no `trackPositionUiModel` and draws no progress at all. Raised upstream against Horologist
// as the double-applied `modifier`.
//
// This catalog is design-led, so the kit is right and the difference belongs in the caption.
// Rebuilding the ring from `CircularProgressIndicator` would make the picture match and stop
// testing the library, which is the one thing a published comparison must not do (AGENTS.md). The
// `progress` knob stays live: a reader can turn it and watch nothing move, which IS the finding.
//
// The Lottie-animated variants (`AnimatedMediaControlButtons`, `AnimatedPlayPauseButton`) are
// deliberately absent: they draw a Lottie composition rather than a Compose vector, which the
// Robolectric renderer does not resolve, and a still of an animation is what `Motion.kt` is for.

@CatalogComponent(
  id = "Media/PlayerScreen",
  noReference =
    "The kit's `Media-Player` set (`71575:22328`) is what this reproduces, but its published " +
      "cell does not EXPORT as the player: `.Base / Media / Album Artwork/Primary NEW` composites " +
      "over the whole 192×192 and the exported reference is an opaque purple wash with the " +
      "player nowhere in it. Mapping to it published a comparison against a blurred background, " +
      "so the reference is withdrawn until the export is faithful. See the note in this file.",
  caption =
    "The media player, whole: track and artist above, transport controls across the middle, the " +
      "app's own action below.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "ambient", strings = ["mode=ambient"])
@OverrideVariant(name = "loading", strings = ["state=loading"])
@OverrideVariant(name = "nothing-playing", strings = ["state=nothing-playing"])
@Composable
fun MediaPlayerScreen() = ScreenSticker {
  val ambient = previewOverrideChoice("mode", "interactive", listOf("interactive", "ambient"))
  val state =
    previewOverrideChoice("state", "playing", listOf("playing", "loading", "nothing-playing"))
  val playing = previewOverrideBoolean("playing", true)
  val progress = previewOverrideChoice("progress", "80", listOf("20", "80"))

  val media: MediaUiModel? =
    when (state) {
      "loading" -> MediaUiModel.Loading
      "nothing-playing" -> null
      else ->
        HorologistSamples.media(
          title = kitCopy("title", KitCopy.MEDIA_TITLE),
          artist = kitCopy("subtitle", KitCopy.MEDIA_ARTIST),
        )
    }
  val position =
    if (state == "playing") HorologistSamples.position(progress.toFloat() / 100f)
    else TrackPositionUiModel.Loading()

  PlayerScreen(
    mediaDisplay = {
      MediaInfoDisplay(media = media, loading = state == "loading", modifier = Modifier)
    },
    controlButtons = {
      if (ambient == "ambient") {
        AmbientMediaControlButtons(
          onPlayButtonClick = {},
          onPauseButtonClick = {},
          playPauseButtonEnabled = true,
          playing = playing,
          leftButton = { interactionSource ->
            SeekToPreviousButton(
              modifier = Modifier.weight(1f).fillMaxSize(),
              onClick = {},
              interactionSource = interactionSource,
            )
          },
          rightButton = { interactionSource ->
            SeekToNextButton(
              modifier = Modifier.weight(1f).fillMaxSize(),
              onClick = {},
              interactionSource = interactionSource,
            )
          },
        )
      } else {
        MediaControlButtons(
          onPlayButtonClick = {},
          onPauseButtonClick = {},
          playPauseButtonEnabled = true,
          playing = playing,
          onSeekToPreviousButtonClick = {},
          seekToPreviousButtonEnabled = true,
          onSeekToNextButtonClick = {},
          seekToNextButtonEnabled = true,
          trackPositionUiModel = position,
        )
      }
    },
    buttons = {
      ShowPlaylistButton(
        artworkPaintable = HorologistSamples.Artwork,
        name = kitCopy("playlist", KitCopy.MEDIA_PLAYLIST),
        onClick = {},
      )
    },
    // The kit draws the artwork behind the whole screen, tinted; ambient draws none. Horologist's
    // `RadialBackground` is that wash, seeded from the artwork's colour rather than the bitmap.
    background = {
      if (ambient != "ambient" && state == "playing") {
        RadialBackground(color = MediaBackgroundTint)
      }
    },
  )
}

@CatalogComponent(
  id = "Media/ControlButtons",
  noReference =
    "The kit draws the transport row only inside `.Base / Media / Footer` (`71575:22221`), a " +
      "PRIVATE set the kit walk does not publish and no coverage row can name. The published cell " +
      "it appears in is `Media-Player`, which `Media/PlayerScreen` reproduces whole.",
  caption =
    "Previous, play/pause, next — the transport row. The track's progress ring is under the " +
      "middle button's container rather than around it; see the note in this file.",
)
@CatalogModes
@OverrideVariant(
  name = "paused",
  booleans = ["playing=false"],
  kitAxis = "Playing",
  kitValue = "No",
)
@OverrideVariant(
  name = "progress-20",
  strings = ["progress=20"],
  kitAxis = "Progress",
  kitValue = "20%",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun MediaControlButtonsRow() = MediaRowSticker {
  val enabled = previewOverrideBoolean("enabled", true)
  val progress = previewOverrideChoice("progress", "80", listOf("20", "80"))
  MediaControlButtons(
    onPlayButtonClick = {},
    onPauseButtonClick = {},
    playPauseButtonEnabled = enabled,
    playing = previewOverrideBoolean("playing", true),
    onSeekToPreviousButtonClick = {},
    seekToPreviousButtonEnabled = enabled,
    onSeekToNextButtonClick = {},
    seekToNextButtonEnabled = enabled,
    trackPositionUiModel = HorologistSamples.position(progress.toFloat() / 100f),
  )
}

@CatalogComponent(
  id = "Media/PodcastControlButtons",
  noReference =
    "Horologist's second transport row, for spoken-word playback: the same middle button with " +
      "seek-back / seek-forward by a named increment either side of it. The kit publishes no " +
      "podcast cell — its `.Base / Media / Footer` draws the track-skip row only.",
  caption = "The spoken-word transport row: seek back and forward by a fixed increment.",
)
@CatalogModes
@OverrideVariant(name = "paused", booleans = ["playing=false"])
@Composable
fun MediaPodcastControlButtons() = MediaRowSticker {
  PodcastControlButtons(
    onPlayButtonClick = {},
    onPauseButtonClick = {},
    playPauseButtonEnabled = true,
    playing = previewOverrideBoolean("playing", true),
    onSeekBackButtonClick = {},
    seekBackButtonEnabled = true,
    onSeekForwardButtonClick = {},
    seekForwardButtonEnabled = true,
    trackPositionUiModel = HorologistSamples.position(0.8f),
  )
}

@CatalogComponent(
  id = "Media/PlayPauseProgressButton",
  noReference =
    "The kit's `.Base / Media / Main Control` (`71575:22140`) — a PRIVATE set, so it carries no " +
      "coverage row. Its `Progress=` and `Playing=` axes are the cells below; its `Shape=` axis is " +
      "the library's own, scalloped while playing and circular while paused.",
  caption =
    "The middle button: play or pause. Its progress ring is drawn UNDER the filled container " +
      "rather than around it, so the kit's `Progress=` does not read here — an upstream bug, not " +
      "a seed. See the note in this file.",
)
@CatalogModes
@OverrideVariant(
  name = "paused",
  booleans = ["playing=false"],
  kitAxis = "Playing",
  kitValue = "No",
)
@OverrideVariant(
  name = "progress-20",
  strings = ["progress=20"],
  kitAxis = "Progress",
  kitValue = "20%",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun MediaPlayPauseProgressButton() = Sticker {
  val c = counted(KitCopy.MEDIA_TITLE)
  val progress = previewOverrideChoice("progress", "80", listOf("20", "80"))
  PlayPauseProgressButton(
    onPlayClick = c.onClick,
    onPauseClick = c.onClick,
    playing = previewOverrideBoolean("playing", true),
    enabled = previewOverrideBoolean("enabled", true),
    trackPositionUiModel = HorologistSamples.position(progress.toFloat() / 100f),
    // The composable puts the progress ring in a `Box` sized by this modifier and draws it with
    // `fillMaxSize()`, so an unbounded frame gives the ring the whole measuring bound to fill.
    // 64dp is the size the kit's `Middle` row gives the main control on the 192dp base screen.
    modifier = Modifier.size(MainControlSize),
  )
}

@CatalogComponent(
  id = "Media/InfoDisplay",
  noReference =
    "The kit's `.Base / Media / Header` (`71575:21983`) — a PRIVATE set. Its text is the kit's " +
      "own placeholder copy, which `KitCopy.MEDIA_TITLE` / `MEDIA_ARTIST` carry.",
  caption = "Track and artist, marquee-scrolled when the title outruns the screen.",
)
@CatalogModes
@OverrideVariant(
  name = "loading",
  strings = ["state=loading"],
  kitAxis = "Placeholder/Loading",
  kitValue = "Yes",
)
@OverrideVariant(name = "nothing-playing", strings = ["state=nothing-playing"])
@Composable
fun MediaInfoDisplayHeader() =
  MediaRowSticker(height = HeaderHeight) {
    val state =
      previewOverrideChoice("state", "playing", listOf("playing", "loading", "nothing-playing"))
    MediaInfoDisplay(
      media =
        when (state) {
          "loading" -> MediaUiModel.Loading
          "nothing-playing" -> null
          else ->
            HorologistSamples.media(
              title = kitCopy("title", KitCopy.MEDIA_TITLE),
              artist = kitCopy("subtitle", KitCopy.MEDIA_ARTIST),
            )
        },
      loading = state == "loading",
    )
  }

@CatalogComponent(
  id = "Media/ShowPlaylistButton",
  noReference =
    "The app's own action in the kit's `.Base / Media / Footer` (`71575:22221`), a PRIVATE set. " +
      "Compose's `FilledTonalButton` underneath, but the composable a media app calls is this one.",
  caption = "The bottom action: jump to the playlist this track came from, with its artwork.",
)
@CatalogModes
@Composable
fun MediaShowPlaylistButton() = Sticker {
  val c = counted(kitCopy("name", KitCopy.MEDIA_PLAYLIST))
  Box(Modifier.width(PlayerScreenWidth)) {
    ShowPlaylistButton(
      artworkPaintable = HorologistSamples.Artwork,
      name = c.label,
      onClick = c.onClick,
    )
  }
}

/**
 * The frame the transport rows and the header render in.
 *
 * These are not components that wrap. `MediaControlButtons` lays a Wear `ButtonGroup` out with
 * `fillMaxSize()`, and `MediaInfoDisplay` sizes its marquee as a fraction of the screen width — so
 * handed the bare measuring bound a device-less Wear preview supplies, both take the whole watch
 * and the sticker is cropped to a screenful of mostly nothing.
 *
 * So the frame is the strip the kit draws them in: the base screen's 192dp of width, and the height
 * of the row on the kit's own `Media-Player` cell (`Middle` is 64dp, `Top` 68dp with the header
 * 38dp of it). It is [Sticker] underneath, so the capture is still transparent and still cropped —
 * cropped to the row rather than to the display.
 */
@Composable
private fun MediaRowSticker(
  height: Dp = MainControlSize,
  content: @Composable () -> Unit,
) = Sticker {
  Box(
    modifier = Modifier.width(PlayerScreenWidth).height(height),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}

/** The kit's base screen width — the one every cell on the Media Controls page is drawn at. */
private val PlayerScreenWidth = 192.dp

/** `Middle` on the kit's `Media-Player` cell: the row the main control sits in. */
private val MainControlSize = 64.dp

/** `.Base / Media / Header` on the kit's `Media-Player` cell: two lines, title over artist. */
private val HeaderHeight = 38.dp

/**
 * The tint under `RadialBackground`.
 *
 * Production seeds this from the artwork's own dominant colour, which Horologist extracts with
 * Coil. The catalog's artwork is a drawn gradient with no loader behind it, so the seed is stated
 * rather than extracted — and stating it is also what keeps the wash identical on every render.
 */
private val MediaBackgroundTint = Color(0xFF2B4C7E)
