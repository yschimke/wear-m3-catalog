@file:CatalogGroup(name = "Dialogs", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.ConfirmationDialogContent
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.FailureConfirmationDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.SuccessConfirmationDialogContent
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.confirmationDialogCurvedText
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
  id = "ConfirmationDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/47251:36765",
  caption = "Reports that an action landed, then dismisses itself.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "success", strings = ["kind=success"])
@OverrideVariant(name = "failure", strings = ["kind=failure"])
@Composable
fun ConfirmationDialogSticker() = FullScreenSticker {
  // Hoisted: `curvedTextStyle` is @Composable and a CurvedScope lambda is not a composition.
  val style = ConfirmationDialogDefaults.curvedTextStyle
  when (previewOverrideString("kind", "plain")) {
    "success" ->
      SuccessConfirmationDialogContent(
        curvedText = { confirmationDialogCurvedText("Saved", style) }
      )
    "failure" ->
      FailureConfirmationDialogContent(
        curvedText = { confirmationDialogCurvedText("Didn't save", style) }
      )
    else ->
      ConfirmationDialogContent(
        curvedText = { confirmationDialogCurvedText("Sent", style) },
        content = { ConfirmationDialogDefaults.SuccessIcon() },
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
