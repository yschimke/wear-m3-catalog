@file:CatalogGroup(name = "Edge-hugging buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.EdgeButtonScreen
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Edge-Button` set. This is the one component so far that is published on the round
// frame rather than cropped: its whole shape is the bottom curve of the display, so a wrap-and-crop
// sticker would show an arc with nothing to be an arc of.
//
// The kit publishes four sizes and Compose has three of them. `EdgeButtonSize` is
// ExtraSmall/Small/Medium/Large, so the kit's `Extra-Large` has no counterpart to render — and this
// catalog is design-led, so the honest answer is to say so rather than to publish `Large` under the
// larger name. Compose's `ExtraSmall` is the mirror gap and stays out for the opposite reason:
// membership is the kit's call, and the kit does not publish it.
//
// Style folds rather than splitting — Compose ships one `EdgeButton` taking its emphasis as
// `colors`, so there is no second function to choose at the call site (AGENTS.md).

@CatalogComponent(
  id = "EdgeButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/36601:6587",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/36601:6586",
  caption = "The screen-hugging confirm action, curved to the bottom edge of the display.",
)
@CatalogFullScreenModes
@ScrollingPreview(modes = [ScrollMode.END])
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(name = "icon", strings = ["content=icon"], kitAxis = "Type", kitValue = "Icon")
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitAxis = "Style",
  kitValue = "Filled Variant",
)
@OverrideVariant(name = "tonal", strings = ["style=tonal"], kitAxis = "Style", kitValue = "Tonal")
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ScreenEdgeButton() = EdgeButtonScreen {
  val c = counted(kitCopy("label", KitCopy.EDGE_BUTTON_LABEL))
  val colors =
    when (
      previewOverrideChoice(
        "style",
        "filled",
        listOf("filled", "filled-variant", "tonal", "outlined"),
      )
    ) {
      "filled-variant" -> ButtonDefaults.filledVariantButtonColors()
      "tonal" -> ButtonDefaults.filledTonalButtonColors()
      "outlined" -> ButtonDefaults.outlinedButtonColors()
      else -> ButtonDefaults.buttonColors()
    }
  // Three values, not four: `EdgeButtonSize.ExtraSmall` exists and the kit does not publish it, so
  // it is not offered here for the reason the file header gives.
  val size =
    when (previewOverrideChoice("size", "default", listOf("default", "small", "large"))) {
      "small" -> EdgeButtonSize.Small
      "large" -> EdgeButtonSize.Large
      else -> EdgeButtonSize.Medium
    }
  EdgeButton(
    onClick = c.onClick,
    buttonSize = size,
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
  ) {
    if (previewOverrideChoice("content", "text", listOf("text", "icon")) == "icon") {
      Icon(Icons.Filled.Check, contentDescription = "Done")
    } else {
      Text(c.label)
    }
  }
}
