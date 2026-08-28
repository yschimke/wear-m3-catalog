@file:CatalogGroup(name = "Toggle buttons", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.IconToggleButtonShapes
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextToggleButton
import androidx.wear.compose.material3.TextToggleButtonDefaults
import androidx.wear.compose.material3.touchTargetAwareSize
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.toggleable

// The kit's `Icon-ToggleButton` and `Text-ToggleButton` sets. A toggle button holds a binary state
// on the button itself rather than in a row, which is what separates these from the selection
// buttons next door.
//
// Both are one component with the kit's axes folded: Compose ships a single function each and takes
// emphasis as `colors`, so there is no second function to choose at the call site — the rule that
// split the Buttons page does not reach here.
//
// The kit's `Corner radius` axis is `shapes`. Compose animates between resting and checked shapes
// (`animatedShapes()`); the sticker takes the static `shapes()` so a baked capture is a shape
// rather
// than a frame of an animation, and the live session still animates the press. The `shape` knob
// picks which end of that morph a capture holds: `circular` is `IconToggleButtonDefaults.shape`
// (`CornerFull`, what an unseeded render already drew) and `rounded` is its `checkedShape`, which
// resolves to `MaterialTheme.shapes.medium` — `RoundedCornerShape(18.dp)`, the kit's
// `Corner radius = Rounded (18)` to the dp.
//
// WHAT NEITHER SET CAN DRAW, and why it is the kit rather than this file: both kit sets carry a
// `Style` axis whose values Compose does not publish as a choice. `Icon-ToggleButton` draws its
// selected cells in `Filled` and `Secondary`, `Text-ToggleButton` in `Filled` and `Tonal`, but
// Wear Compose ships ONE `colors()` per component — emphasis is not an argument here the way it is
// on `Button` — so the eight `Secondary` cells and the three selected `Tonal` ones have no call
// site to render them. `Text-ToggleButton`'s `Fixed Width=False` cells are out for a second
// reason: the sticker sizes the button with `touchTargetAwareSize`, and a label-hugging width is
// not something the component takes as a parameter. The remaining 16 of 24 and 9 of 15 cells are
// drawn ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).

@Composable
private fun iconToggleSize(): Dp =
  when (
    previewOverrideChoice("size", "default", listOf("default", "small", "large", "extra-large"))
  ) {
    "small" -> IconToggleButtonDefaults.SmallSize
    "large" -> IconToggleButtonDefaults.LargeSize
    "extra-large" -> IconToggleButtonDefaults.ExtraLargeSize
    else -> IconToggleButtonDefaults.Size
  }

/**
 * **Every `Icon-ToggleButton` cell Compose can draw** — the `Size` run at both `Corner radius`
 * values while selected, and the unselected `Tonal` run enabled and disabled: 16 of the set's 24
 * nodes. The eight the kit draws in `Style=Secondary` have no call site (see the note above).
 *
 * The cells that stay on one axis keep `kitAxis`/`kitValue`; the rest declare their whole kit
 * vector with `kitProps`, because the set's axes are coupled and a single-axis cell asks for a node
 * between the ones the kit drew. That is not hypothetical here: `off` and `disabled` named
 * `Selected=Off` and `Disabled=Yes` on their own and BOTH resolved to nothing, because every
 * unselected cell in this set is also `Style=Tonal` and every disabled one is also unselected.
 * Which is why `disabled` now seeds `checked=false` as well — the kit publishes no disabled
 * selected cell, so the render it was being compared against did not exist.
 */
@OverrideVariant(
  name = "off",
  booleans = ["checked=false"],
  kitProps = ["Selected=Off", "Style=Tonal"],
)
@OverrideVariant(
  name = "off-small",
  booleans = ["checked=false"],
  strings = ["size=small"],
  kitProps = ["Selected=Off", "Style=Tonal", "Size=Small"],
  secondary = true,
)
@OverrideVariant(
  name = "off-large",
  booleans = ["checked=false"],
  strings = ["size=large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Size=Large"],
  secondary = true,
)
@OverrideVariant(
  name = "off-extra-large",
  booleans = ["checked=false"],
  strings = ["size=extra-large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Size=Extra-Large"],
  secondary = true,
)
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-large",
  strings = ["size=extra-large"],
  kitAxis = "Size",
  kitValue = "Extra-Large",
)
@OverrideVariant(
  name = "rounded",
  strings = ["shape=rounded"],
  kitAxis = "Corner radius",
  kitValue = "Rounded (18)",
)
@OverrideVariant(
  name = "rounded-small",
  strings = ["shape=rounded", "size=small"],
  kitProps = ["Corner radius=Rounded (18)", "Size=Small"],
  secondary = true,
)
@OverrideVariant(
  name = "rounded-large",
  strings = ["shape=rounded", "size=large"],
  kitProps = ["Corner radius=Rounded (18)", "Size=Large"],
  secondary = true,
)
@OverrideVariant(
  name = "rounded-extra-large",
  strings = ["shape=rounded", "size=extra-large"],
  kitProps = ["Corner radius=Rounded (18)", "Size=Extra-Large"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled",
  booleans = ["checked=false", "enabled=false"],
  kitProps = ["Selected=Off", "Style=Tonal", "Disabled=Yes"],
)
@OverrideVariant(
  name = "disabled-small",
  booleans = ["checked=false", "enabled=false"],
  strings = ["size=small"],
  kitProps = ["Selected=Off", "Style=Tonal", "Disabled=Yes", "Size=Small"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled-large",
  booleans = ["checked=false", "enabled=false"],
  strings = ["size=large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Disabled=Yes", "Size=Large"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled-extra-large",
  booleans = ["checked=false", "enabled=false"],
  strings = ["size=extra-large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Disabled=Yes", "Size=Extra-Large"],
  secondary = true,
)
annotation class IconToggleKitCells

// The two ends of the corner morph as a static pair, which is what the kit's `Corner radius` axis
// publishes a still of each of.
//
// NOT `shapes(shape)`, which is the overload that looks right: it copies the default pair with the
// argument as the UNCHECKED shape only, so a checked sticker — every cell on this axis is
// `Selected=On` — came out byte-identical to the circular one under a name claiming otherwise.
// Both shapes are set explicitly instead, so what the capture holds is a shape rather than a frame
// of the animation, and the live session still animates the press.
@Composable
private fun iconToggleShapes(): IconToggleButtonShapes {
  val shapes = IconToggleButtonDefaults.shapes()
  if (previewOverrideChoice("shape", "circular", listOf("circular", "rounded")) != "rounded") {
    return shapes
  }
  val rounded = IconToggleButtonDefaults.checkedShape
  return shapes.copy(uncheckedShape = rounded, checkedShape = rounded)
}

@CatalogComponent(
  id = "IconToggleButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39083:684",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/39083:679",
  caption = "An icon that holds an on/off state, with the kit's size and state axes folded in.",
  // The corner-shape morph, recorded in Motion.kt against `animatedShapes()` — the thing the kit's
  // `Corner radius = Circular | Rounded (18)` axis publishes two stills of.
  motionPreview = "ToggleButtonShapeMotion",
)
@CatalogModes
@IconToggleKitCells
@Composable
fun IconToggle() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  // Read once: the container size and the glyph size are the same choice, and `iconSizeFor` is
  // what pairs them.
  val size = iconToggleSize()
  IconToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(size),
    shapes = iconToggleShapes(),
  ) {
    // Sized explicitly for the reason IconButtons.kt states: Wear's toggle button does not size
    // its content either, and a bare `Icon` falls back to Material's 24dp default. That is the
    // kit's icon at `Size=Small` only — the other three cells draw 26, 32 and 36 (39083:684,
    // :682, :688), which is exactly the pairing `iconSizeFor` publishes.
    Icon(
      Icons.Filled.Add,
      contentDescription = "Add",
      modifier = Modifier.size(IconToggleButtonDefaults.iconSizeFor(size)),
    )
  }
}

@Composable
private fun textToggleSize(): Dp =
  when (previewOverrideChoice("size", "default", listOf("default", "large", "extra-large"))) {
    "large" -> TextToggleButtonDefaults.LargeSize
    "extra-large" -> TextToggleButtonDefaults.ExtraLargeSize
    else -> TextToggleButtonDefaults.Size
  }

/**
 * **Every `Text-ToggleButton` cell Compose can draw** — the selected `Filled` size run, and the
 * unselected `Tonal` run enabled and disabled at all three sizes: 9 of the set's 15 nodes. The
 * other six are the three selected `Tonal` cells and the three `Fixed Width=False` ones, neither of
 * which this component takes as an argument (see the note at the top of the file).
 *
 * `off` and `disabled` used to name `Selected=Off` and `Disabled=Yes` alone and resolved to
 * nothing: the kit couples them to `Style=Tonal` and `Radius=Circular`, and it publishes no
 * disabled SELECTED cell, so `disabled` seeds `checked=false` too.
 */
@OverrideVariant(
  name = "off",
  booleans = ["checked=false"],
  kitProps = ["Selected=Off", "Style=Tonal", "Radius=Circular"],
)
@OverrideVariant(
  name = "off-large",
  booleans = ["checked=false"],
  strings = ["size=large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Radius=Circular", "Size=Large"],
  secondary = true,
)
@OverrideVariant(
  name = "off-extra-large",
  booleans = ["checked=false"],
  strings = ["size=extra-large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Radius=Circular", "Size=Extra-Large"],
  secondary = true,
)
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-large",
  strings = ["size=extra-large"],
  kitAxis = "Size",
  kitValue = "Extra-Large",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["checked=false", "enabled=false"],
  kitProps = ["Selected=Off", "Style=Tonal", "Radius=Circular", "Disabled=Yes"],
)
@OverrideVariant(
  name = "disabled-large",
  booleans = ["checked=false", "enabled=false"],
  strings = ["size=large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Radius=Circular", "Disabled=Yes", "Size=Large"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled-extra-large",
  booleans = ["checked=false", "enabled=false"],
  strings = ["size=extra-large"],
  kitProps = ["Selected=Off", "Style=Tonal", "Radius=Circular", "Disabled=Yes", "Size=Extra-Large"],
  secondary = true,
)
annotation class TextToggleKitCells

@CatalogComponent(
  id = "TextToggleButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39083:767",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/39083:760",
  caption = "A short label that holds an on/off state.",
)
@CatalogModes
@TextToggleKitCells
@Composable
fun TextToggle() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  TextToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(textToggleSize()),
  ) {
    Text(kitCopy("label", KitCopy.GLYPHS))
  }
}
