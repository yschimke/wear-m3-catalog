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
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.EdgeButtonSticker
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Edge-Button` set.
//
// IT IS A COMPONENT CELL, NOT A SCREEN. Every one of the set's cells is 192 wide and 49/59/73/99
// tall: the button alone, laid out against the 192dp screen so its arc is the display's, with the
// kit's 3dp of floor under it. It is *not* one of the kit's 192×192 display cells, so this renders
// through `EdgeButtonSticker` rather than a round watch face. Publishing the screen instead is
// issue #31: the comparison squashed a whole watch — list, time text, scroll indicator — into a
// 192×59 cell and reported all of it as a difference from a button. The screen is still shown; it
// is a recording in `Motion.kt`, which is where a scroll-driven reveal belongs.
//
// THE FOUR SIZES LINE UP ONE-TO-ONE, which is not what this file used to say. The kit's cells are
// exactly `EdgeButtonSize` plus the 3dp floor — 49=46+3, 59=56+3, 73=70+3, 99=96+3 — so the kit's
// `Small` is Compose's `ExtraSmall`, its `Default` is `Small`, its `Large` is `Medium` and its
// `Extra-Large` is `Large`. Reading the two lists off in parallel instead put every size one step
// too big and invented two gaps that are not there (a kit `Extra-Large` with no counterpart, and a
// Compose `ExtraSmall` the kit never published). The cells below keep Compose's names and name the
// kit's spelling with `kitValue`.
//
// Style folds rather than splitting — Compose ships one `EdgeButton` taking its emphasis as
// `colors`, so there is no second function to choose at the call site (AGENTS.md).

@CatalogComponent(
  id = "EdgeButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/36601:6587",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/36601:6586",
  caption = "The screen-hugging confirm action, curved to the bottom edge of the display.",
)
@CatalogModes
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(name = "medium", strings = ["size=medium"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "large",
  strings = ["size=large"],
  kitAxis = "Size",
  kitValue = "Extra-Large",
)
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
fun ScreenEdgeButton() = EdgeButtonSticker {
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
  // Four values, one per kit cell, and the default is Compose's `Small` because that is what the
  // kit calls `Size=Default` — see the note above.
  val size =
    when (
      previewOverrideChoice("size", "small", listOf("extra-small", "small", "medium", "large"))
    ) {
      "extra-small" -> EdgeButtonSize.ExtraSmall
      "medium" -> EdgeButtonSize.Medium
      "large" -> EdgeButtonSize.Large
      else -> EdgeButtonSize.Small
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
