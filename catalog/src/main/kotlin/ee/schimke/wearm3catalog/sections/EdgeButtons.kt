@file:CatalogGroup(name = "Edge-hugging buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.EdgeButtonScreen
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Edge-Button` set, and the one place where "is this a component or a screen" had to be
// answered explicitly rather than assumed.
//
// THE KIT NEVER DRAWS THIS ON A SCREEN. All 64 cells of `Edge-Button` are the button ALONE, cropped
// to itself: 192 wide (the small-round display's width, which is what the shape spans) by 49 / 59 /
// 73 / 99 tall for its four sizes. There is no display-shaped cell anywhere in the set.
//
// This catalog published only the screen — an `EdgeButtonScreen` with a twelve-row list above the
// button, because the scaffold reveals it from scroll state. That meant a screenful of list rows
// was being diffed against a bare button, and the comparison squashed one into the other's frame:
// a finding about the framing wearing the costume of a finding about the component.
//
// So there are two entries now, and only one of them is mapped:
//
//  - `EdgeButton` is the component, cropped at the kit's own 192dp width, and it carries the
//    reference and the whole `Style / Type / Size / Disabled` matrix.
//  - `EdgeButton/Screen` is the button in the place it actually lives, and it is **unmapped** —
//    there is no kit cell of that shape for it to answer to, which is a fact about the kit rather
//    than a gap here (AGENTS.md's second door).
//
// SIZES. The kit publishes four and Compose has three of them: `EdgeButtonSize` is
// ExtraSmall/Small/Medium/Large, so the kit's `Extra-Large` has no counterpart to render — and this
// catalog is design-led, so the honest answer is to say so rather than to publish `Large` under the
// larger name. Compose's `ExtraSmall` is the mirror gap and stays out for the opposite reason:
// membership is the kit's call, and the kit does not publish it.
//
// STYLE folds rather than splitting — Compose ships one `EdgeButton` taking its emphasis as
// `colors`, so there is no second function to choose at the call site (AGENTS.md).

/** The four knobs both entries below read, so the pair cannot drift in what they draw. */
@Composable
private fun edgeButtonColors() =
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

// Three values, not four: `EdgeButtonSize.ExtraSmall` exists and the kit does not publish it, so it
// is not offered here for the reason the file header gives.
@Composable
private fun edgeButtonSize() =
  when (previewOverrideChoice("size", "default", listOf("default", "small", "large"))) {
    "small" -> EdgeButtonSize.Small
    "large" -> EdgeButtonSize.Large
    else -> EdgeButtonSize.Medium
  }

@CatalogComponent(
  id = "EdgeButton",
  // Style=Filled, Type=Text, Size=Default, Disabled=No — 192×59, the button and nothing else.
  reference = "figma:B24oss2tTeXAFykyeyusz0/36601:6587",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/36601:6586",
  caption = "The screen-hugging confirm action, curved to the bottom edge of the display.",
)
@CatalogModes
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
fun EdgeAction() = Sticker {
  val c = counted(kitCopy("label", KitCopy.EDGE_BUTTON_LABEL))
  // The 192dp box is for the CROP, and it is the only part of the width this sticker controls.
  //
  // `EdgeButton` measures to the width it is offered but DRAWS to its content: given 192dp it lays
  // out 192dp and paints a 131dp pill inside it, and neither `Modifier.width(192.dp)` on the button
  // nor `fillMaxWidth()` changes that — both were tried and both rendered identically. Its span
  // comes from `ScreenScaffold`, which stretches it across the display; in isolation there is
  // nothing doing the stretching.
  //
  // So the box pins what it can. Without it the sticker crops to the renderer's measuring bound —
  // 227dp, a size the kit does not draw — and the reference gets squashed into that. With it the
  // crop is 208dp (192 plus the sticker's own 8dp padding either side), against the kit's 192.
  //
  // What is left is a difference the framing does not explain, and it is left visible on purpose:
  // the kit's cell is 192×59 and this draws 131×70. The width gap is the scaffold's doing; the 11dp
  // of height is `EdgeButtonSize.Medium` against the kit's `Size=Default`, which may well be a
  // break between the kit and the library rather than anything a sticker can fix by drawing
  // something else. Now that the shapes are comparable, the live comparison is what settles it.
  // See docs/DESIGN_MAP.md.
  Box(Modifier.width(192.dp)) {
    EdgeButton(
      onClick = c.onClick,
      buttonSize = edgeButtonSize(),
      enabled = previewOverrideBoolean("enabled", true),
      colors = edgeButtonColors(),
    ) {
      if (previewOverrideChoice("content", "text", listOf("text", "icon")) == "icon") {
        Icon(Icons.Filled.Check, contentDescription = "Done")
      } else {
        Text(c.label)
      }
    }
  }
}

// UNMAPPED ON PURPOSE, and the reason is the kit's rather than this catalog's: `Edge-Button` has no
// display-shaped cell, so there is nothing of this shape to compare against. It stays in the
// inventory because the edge button is the one component whose *placement* is most of what it is —
// bottom-anchored, revealed by the scroll — and a reader looking at the cropped sticker above has
// no way to see that from it.
@CatalogComponent(
  id = "EdgeButton/Screen",
  noReference =
    "The kit draws the edge button only as a component — all 64 cells of `Edge-Button` are the " +
      "button cropped to itself, 192dp wide, with no display-shaped cell anywhere in the set. " +
      "This entry is the button in place on the round frame, which the kit does not publish.",
  caption = "The same button where it lives: anchored to the bottom of a scrolling screen.",
)
@CatalogFullScreenModes
@ScrollingPreview(modes = [ScrollMode.END])
// No cells. The matrix belongs to the mapped component above; repeating it here would publish the
// same seven knobs a second time, at five breakpoints each, against nothing that can check them.
@Composable
fun ScreenEdgeButton() = EdgeButtonScreen {
  val c = counted(kitCopy("label", KitCopy.EDGE_BUTTON_LABEL))
  EdgeButton(onClick = c.onClick, buttonSize = EdgeButtonSize.Medium) { Text(c.label) }
}
