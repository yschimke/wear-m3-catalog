@file:CatalogGroup(name = "Dialogs", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.FullScreenSticker

// The kit's `Dialogs` and `Confirmation Overlays` pages.
//
// THE `*DialogContent` CARVE-OUT
//
// Every sticker in this catalog invokes the real named Wear Material 3 composable — a replica built
// from `Surface` and `Column` can be made to line up, but it cannot test the library and so cannot
// be defended as a comparison. Dialogs are the case where that rule meets a renderer limit: the
// `AlertDialog` / `ConfirmationDialog` / `OpenOnPhoneDialog` entry points open a platform dialog
// window, and a capture of the composition does not include that window's surface.
//
// Wear Compose publishes the answer itself. `AlertDialogContent`, `ConfirmationDialogContent`,
// `OpenOnPhoneDialogContent` and friends are public API carrying the entire Material layout — the
// same code the windowed entry point composes inside its window. So the sticker draws the real
// component and only the platform window is missing, which is the same trade the phone catalog
// makes with its inline dialog host.
//
// The kit's `Edge Option=` axis is the dialog's button arrangement, and it is a slot in Compose
// (`edgeButton`, or the confirm/dismiss pair) rather than a property, so it folds as content cells.
//
// THE CONFIRMATION OVERLAY IS OUT, AND THIS IS WHY
//
// `ConfirmationDialogContent` reveals itself over time: the children start at `alpha = 0` and
// animate in from a `LaunchedEffect` after a delay, and `ConfirmationDialogDefaults.SuccessIcon()`
// is an `AnimatedImageVector` whose checkmark is drawn by a 267ms `trimPathEnd` tween that only
// starts after its own 100ms delay. Recorded as a GIF the whole reveal takes **~528ms** to reach
// its resting frame.
//
// The renderer never gets that far on a still. It does advance a little — enough that "the clock
// is paused, so the capture is frame zero" is too simple an account — but not nearly enough, and
// it fails in two different ways depending on where the capture lands:
//
// - **Plain sticker: the empty ring.** The still path proves quiescence by capturing twice and
//   comparing pixels, and returns as soon as two frames match. The first ~33ms of this component
//   are the reveal's own start delay, where nothing moves — so the check reads "settled", reports
//   no warning, and publishes the empty container: no icon, no curved text.
// - **`@ScrollingPreview(END)`: the chevron.** Enough virtual time passes to bring the container
//   and the curved text in, but the capture lands mid-tween and the checkmark reads as a chevron
//   rather than a check. The renderer says so on stderr ("did not become visually quiescent after
//   5 samples") and publishes it anyway. Note this is not scrolling: there is no scrollable in the
//   sticker, the drive logs "no scrollable composable found", and `maxScrollPx` / `frameIntervalMs`
//   change the output not at all — byte-identical PNGs. The advance is incidental, so tuning those
//   knobs is not a route to a settled capture.
//
// The renderer already owns a settle long enough to finish the job — a 1000ms frame-by-frame
// advance it runs after a scroll lands, for exactly this class of animation. Giving the probe a
// dummy sibling scrollable so that settle fires renders the component **perfectly**: full check,
// container at rest, curved text in. So nothing is missing from the renderer's capability, only a
// way to ask for it without a scroll — and a sticker that ships a fake scroller to buy clock time
// is the kind of workaround AGENTS.md sends upstream instead. Tracked as
// [yschimke/compose-ai-tools#4202](https://github.com/yschimke/compose-ai-tools/issues/4202); the
// set is recorded as excluded in kit-sets.json rather than dropped silently, so it comes back the
// day a preview can ask to be captured settled.
//
// `@AnimatedPreview` is not the answer either: it yields a motion artifact beside the same
// unsettled still, so the component's own card still shows the empty frame.
//
// `OpenOnPhoneDialog` is touched by the same gap but stays, and the distinction is worth writing
// down. Its icon is an animated vector too, so the published sticker catches the arrow mid-draw as
// a stub rather than a full arrow into the phone — the same 5-sample warning is logged for it. It
// stays because what it publishes is still recognisably the component, and because "settled" is
// not obviously the right frame for it anyway: the progress ring IS the wait, and a capture taken
// after the ring completes is a picture of the wait being over. The confirmation overlay has no
// such defence — unsettled, it publishes nothing at all.

@CatalogComponent(
  id = "AlertDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/58475:87022",
  caption = "Interrupts to ask for a decision; the kit's button arrangements fold in as cells.",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "edge-button",
  strings = ["edge=single"],
  kitAxis = "Edge Option",
  kitValue = "Edge Button",
)
@OverrideVariant(
  name = "no-buttons",
  strings = ["edge=none"],
  kitAxis = "Edge Option",
  kitValue = "None",
)
@Composable
fun AlertDialogSticker() = FullScreenSticker {
  when (previewOverrideString("edge", "double")) {
    "single" ->
      AlertDialogContent(
        edgeButton = { AlertDialogDefaults.EdgeButton(onClick = {}) },
        title = { Text("Delete this run?") },
      )
    "none" -> AlertDialogContent(title = { Text("Syncing with your phone") })
    else ->
      AlertDialogContent(
        confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
        dismissButton = { AlertDialogDefaults.DismissButton(onClick = {}) },
        title = { Text("Delete this run?") },
      )
  }
}

@CatalogComponent(
  id = "OpenOnPhoneDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/46964:90920",
  caption = "Hands the task to the paired phone, with the progress the wait needs.",
)
@CatalogFullScreenModes
@Composable
fun OpenOnPhoneDialogSticker() = FullScreenSticker {
  val style = OpenOnPhoneDialogDefaults.curvedTextStyle
  OpenOnPhoneDialogContent(
    curvedText = { openOnPhoneDialogCurvedText("Open on phone", style) },
    durationMillis = OpenOnPhoneDialogDefaults.DurationMillis,
  ) {
    OpenOnPhoneDialogDefaults.Icon()
  }
}
