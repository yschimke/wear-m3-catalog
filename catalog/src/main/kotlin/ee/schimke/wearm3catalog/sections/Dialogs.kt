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
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
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
// THE EDGE-BUTTON CELL CARRIES A SCROLL, EVEN THOUGH NOTHING SCROLLS
//
// `AlertDialogContent(edgeButton = …)` is a `ScreenScaffold` with an `edgeButton` slot, and the
// scaffold sizes that button from the list's `lastItemOffset`: it seeds an `Animatable` with the
// value read on the *first* composition — 0, because the list has not laid out yet — and settles it
// from a `LaunchedEffect` afterwards. A static capture IS that first frame, so the cell published a
// title over an empty bottom half (issue #15).
//
// `@ScrollingPreview(ScrollMode.END)` is the fix, the same one `EdgeButtonScreen` carries. It reads
// oddly on a one-line dialog with nothing to scroll, and the scroll genuinely is a no-op here — but
// the driver advances the renderer's `MainTestClock` to step the scroll, and that is what lets the
// reveal settle before the capture. It sits on the component rather than the cell because a
// `@Preview` annotation is per-function; the confirm/dismiss and no-buttons cells lay out at rest
// and are unchanged by it.
//
// THE CONFIRMATION OVERLAY IS OUT, AND THIS IS WHY
//
// `ConfirmationDialogContent` starts its children at `alpha = 0` and animates them in from a
// `LaunchedEffect` after a delay (`alphaAnimatable.animateTo(1f, TextOpacityAnimationSpec)`). The
// renderer pauses the clock, so a static capture IS that first frame: an empty container ring with
// no icon and no curved text. Publishing it would put a picture of nothing under the component's
// name, and there is no annotation today that says "settle, then capture" — `@AnimatedPreview`
// yields a motion artifact beside the same unsettled still. Tracked upstream; the set is recorded
// as excluded in kit-sets.json rather than dropped silently, so it comes back the day the renderer
// can advance the clock.
//
// Note that borrowing `@ScrollingPreview` — as the edge-button cell above does — is not that day.
// Its clock advance does bring the container and the curved text in, but it is bounded by the
// scroll it was asked to drive rather than by the animation, and the success tick still captures
// half drawn. Settling on purpose is the annotation that is missing, not settling by side effect.
//
// `OpenOnPhoneDialog` is unaffected — its icon and progress ring draw at rest — so it stays.

@CatalogComponent(
  id = "AlertDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/58475:87022",
  caption = "Interrupts to ask for a decision; the kit's button arrangements fold in as cells.",
)
@CatalogFullScreenModes
@ScrollingPreview(modes = [ScrollMode.END])
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
