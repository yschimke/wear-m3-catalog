@file:CatalogGroup(name = "Media controls", section = "Horologist")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.horologist.audio.AudioOutput
import com.google.android.horologist.audio.ui.material3.components.actions.SettingsButton
import com.google.android.horologist.audio.ui.material3.components.actions.SettingsButtonDefaults
import com.google.android.horologist.audio.ui.material3.components.actions.VolumeButtonWithBadge
import com.google.android.horologist.audio.ui.material3.components.toAudioOutputUi
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
import ee.schimke.composeai.preview.CaptureGutter
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
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
// THE FOOTER IS TWO COMPACT BUTTONS, AND THIS PAGE ONCE DREW A PLAYLIST CHIP INSTEAD (issue #67).
//
// `PlayerScreen`'s third slot is typed `SettingsButtons` and ships EMPTY — Horologist supplies no
// default for it, so what goes there is entirely the app's call. The first draft of this file put
// `ShowPlaylistButton` in it, because that is the one component in `media-ui-material3`'s
// `components/actions/` that pairs with the `MediaUiModel` seed already on hand. The kit draws
// something else: `.Base / Media / Footer` (`71575:22221`) is two `Section`s of 68×60, each a 48dp
// tap target around a 44×32 `Button-Compact` — the output-device button carrying a volume badge,
// and an overflow. Horologist publishes exactly those, but in `horologist-audio-ui-material3`
// (`VolumeButtonWithBadge` / `SettingsButton`), which was not on this module's classpath — so the
// slot got the available component rather than the drawn one.
//
// The sizes are not a coincidence to be re-derived here: `SettingsButton`'s own `BUTTON_WIDTH` /
// `BUTTON_HEIGHT` ARE 44×32, and it fills whatever box it is handed and centres itself in it. So
// the frame below is the kit's two sections and nothing more, and the library draws the buttons.
//
// WHY NOTHING CAUGHT IT: the `Media-Player` reference is withdrawn (see above) because the cell
// does not export. That is the right call about the reference — and it also switched off the one
// check that would have compared this render against the cell, so a wrong footer read as intended
// for as long as the caption described what was built. Two lessons, both already in AGENTS.md's
// spirit: a withdrawn reference is a gap to state, not a licence; and a caption is written from the
// KIT, not from the render.
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
// deliberately absent as CARDS: they draw a Lottie composition rather than a Compose vector, which
// the Robolectric renderer does not resolve, and a still of an animation is what `Motion.kt` is
// for.
//
// WHERE THIS PAGE'S MOTION LIVES, AND WHY ALL OF IT IS ON THE PODCAST ROW
//
// `Media/PodcastControlButtons` claims `MediaTransportMotion` — a scripted pointer pressing seek
// back, play/pause and seek forward in turn, over a progress ring that never stops. It is the only
// one of these three cards that can carry a recording at all, and that is a fact about the library
// rather than a preference. The two rows are assembled from different middle buttons:
//
//   `PodcastControlButtons` -> `AnimatedMediaControlButtons` -> `AnimatedPlayPauseProgressButton`,
//     which morphs a 10-vertex scallop, draws a WAVY indicator OUTSIDE the container (so the kit's
//     `Progress=` finally reads somewhere on this sheet), and opts its side buttons into
//     `ButtonGroupScope.animateWidth` so a press swells them;
//   `MediaControlButtons`   -> the plain `PlayPauseProgressButton` described above, whose ring is
//     under its own container, whose play/pause is a bare icon swap, and whose side buttons never
//     call `animateWidth`.
//
// Both halves were measured, not assumed. Flipping `playing` on the plain button for a whole
// capture window: **4 distinct frames of 46** — the placeholder's number, a still with extra bytes.
// And a real dispatched pointer pressing `MediaControlButtons`' previous and next, which is the
// strongest test available: **1 pixel-distinct frame of 178**. The row simply does not respond
// visibly. The same pointer on `PodcastControlButtons` gives 111 of 178.
//
// So `Media/ControlButtons` and `Media/PlayPauseProgressButton` publish no recording rather than
// one that implies motion nobody would see. A reader browsing those two cards should look at the
// podcast card's Motion lane for what a transport row does, and read the captions here for why
// these two draw a different picture.

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
      "output-device and overflow buttons below.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "ambient", strings = ["mode=ambient"])
@OverrideVariant(name = "loading", strings = ["state=loading"])
@OverrideVariant(name = "nothing-playing", strings = ["state=nothing-playing"])
@SettledPreview
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
    buttons = { MediaFooterButtons(ambient = ambient == "ambient") },
    // The kit draws the artwork behind the whole screen, tinted; ambient draws none. Horologist's
    // `RadialBackground` is that wash, seeded from the artwork's colour rather than the bitmap.
    background = {
      if (ambient != "ambient" && state == "playing") {
        RadialBackground(color = MediaBackgroundTint)
      }
    },
  )
}

/**
 * The width of one `Section` in the kit's `.Base / Media / Footer` (`71575:22228`, `71575:22232`).
 */
private val FooterSectionWidth = 68.dp

/** The height of the kit's `Bottom` frame (`71575:22339`) — the row the two sections sit in. */
private val FooterSectionHeight = 60.dp

/**
 * The audio output the footer's first button reports.
 *
 * A `BluetoothHeadset` rather than a hand-picked icon, because `toAudioOutputUi()` is what maps an
 * output to the glyph the kit draws — headphones for `TYPE_HEADPHONES`, and `isConnected`, which is
 * what puts the volume badge on the button at all. Naming the icon here instead would be this repo
 * transcribing a decision the library already makes (AGENTS.md).
 */
private val FooterAudioOutput =
  AudioOutput.BluetoothHeadset(id = "catalog-headset", name = "Headphones")

/**
 * The kit's `.Base / Media / Footer` (`71575:22221`): the output-device button with its volume
 * badge, and the overflow, in two 68×60 sections centred across the 192dp screen.
 *
 * NO SIZE IS INVENTED HERE. `SettingsButton` measures 44×32 — its own `BUTTON_WIDTH` /
 * `BUTTON_HEIGHT`, which are the kit's `Button-Compact` — and it `fillMaxSize()`s into whatever box
 * it is handed, centring itself. So the sections below are the only geometry this file states, and
 * they are read off the kit's frames rather than chosen: 28 + 68 + 68 + 28 = 192.
 *
 * The volume state is deliberately **null**. The badge's glyph is the library's own function of it
 * (mute / down / up), and a catalog render has no volume to report; null takes Horologist's own
 * no-state default rather than seeding a number nobody chose. `isConnected` — which is what decides
 * whether there is a badge at all — comes from [FooterAudioOutput].
 *
 * [ambient] outlines both buttons instead of filling them, which is the kit's `AOD=Yes` cell
 * (`71575:22344`): same two buttons, drawn as outlines, badge still filled. Horologist publishes
 * that pair as `ambientButtonColors()` + `ambientButtonBorder()`, so it is a colour choice on the
 * same composable rather than a second one.
 */
@Composable
private fun MediaFooterButtons(ambient: Boolean) {
  val colors =
    if (ambient) SettingsButtonDefaults.ambientButtonColors()
    else SettingsButtonDefaults.buttonColors()
  val border = if (ambient) SettingsButtonDefaults.ambientButtonBorder(enabled = true) else null
  Row {
    Box(Modifier.size(FooterSectionWidth, FooterSectionHeight)) {
      VolumeButtonWithBadge(
        onOutputClick = {},
        audioOutputUi = FooterAudioOutput.toAudioOutputUi(),
        volumeUiState = null,
        buttonColors = colors,
        border = border,
      )
    }
    Box(Modifier.size(FooterSectionWidth, FooterSectionHeight)) {
      SettingsButton(
        onClick = {},
        imageVector = Icons.Filled.MoreVert,
        // Not `kitCopy`: that is for words the kit DRAWS, and this is never on screen.
        contentDescription = "More",
        buttonColors = colors,
        border = border,
      )
    }
  }
}

@CatalogComponent(
  id = "Media/FooterButtons",
  noReference =
    "The kit's `.Base / Media / Footer` (`71575:22221`) — a PRIVATE set, so it carries no coverage " +
      "row. The published cell it appears in is `Media-Player`, which `Media/PlayerScreen` " +
      "reproduces whole; this is the same row on its own, so a change to it is diffed without " +
      "having to read it out of a 192×192 screen.",
  caption =
    "The player's footer: the output-device button with its volume badge, and the overflow. Two " +
      "44×32 compact buttons in the kit's two 68dp sections.",
)
@CatalogModes
@OverrideVariant(name = "ambient", booleans = ["ambient=true"])
@Composable
fun MediaFooterButtonsRow() =
  MediaRowSticker(height = FooterSectionHeight) {
    MediaFooterButtons(ambient = previewOverrideBoolean("ambient", false))
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
@CaptureGutter(top = 8, bottom = 8)
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
  motionPreview = "MediaTransportMotion",
)
@CatalogModes
@CaptureGutter(top = 8, bottom = 8)
@OverrideVariant(name = "paused", booleans = ["playing=false"])
@SettledPreview
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
    "NOT IN THE KIT AT ALL, and this row used to claim it was. The kit's `.Base / Media / Footer` " +
      "(`71575:22221`) draws two 44×32 compact buttons — `Media/FooterButtons` — not a labelled " +
      "chip, so there is no cell this reproduces and none to withdraw. It stays on the sheet " +
      "because Horologist publishes it and a media app that jumps to its source playlist calls " +
      "exactly this; a reader should just not expect to find it on the kit's player. See the note " +
      "in this file (issue #67).",
  caption = "Jump to the playlist this track came from, with its artwork.",
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
 *
 * Horologist's two transport rows deliberately draw their 80dp middle control 8dp above and below
 * that 64dp layout strip. Their component previews declare those painted overhangs with
 * `@CaptureGutter(top = 8, bottom = 8)`: the capture grows without changing the 192×64 constraints
 * the rows measure against. Do not make this frame 80dp high instead — that changes the component's
 * layout rather than preserving what it draws outside its bounds.
 *
 * THE LIVE SHEET STILL CLIPS THEM, AND THAT IS UPSTREAM. The gutter reaches the baked capture and
 * the override-free live render — both 384×160 at density 2 — but any request that wakes the live
 * Android daemon comes back 384×128, the bare 192×64 frame with the middle button's scallop cut off
 * top and bottom. A `?themeProvider=` from the Theme select does it, and so does a `?knob.` edit;
 * the theme is not the cause, it is just one of the overrides that forces the request off the baked
 * lane. `RobolectricHost.reshapeRenderPayload` re-serialises the resolved spec without a
 * `captureGutter=` token and every edge then defaults to zero, silently
 * ([compose-ai-tools#4822](https://github.com/yschimke/compose-ai-tools/issues/4822), reported as
 * [#179](https://github.com/yschimke/wear-m3-catalog/issues/179)). Nothing in this file fixes it:
 * padding the frame back would put undeclared transparent margin in the baked capture, which is
 * [#138](https://github.com/yschimke/wear-m3-catalog/issues/138)'s mistake in a new place.
 *
 * `internal` rather than file-private because `Motion.kt`'s media recordings render in it too: a
 * motion capture needs a **pinned** canvas and this is the pin, so a recording that framed itself
 * some other way would move the row relative to every still it is published beside.
 */
@Composable
internal fun MediaRowSticker(
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
 * Coil. The catalog's artwork is a drawn placeholder with no loader behind it, so the seed is
 * stated rather than extracted — and stating it is also what keeps the wash identical on every
 * render.
 */
private val MediaBackgroundTint = Color(0xFF2B4C7E)
