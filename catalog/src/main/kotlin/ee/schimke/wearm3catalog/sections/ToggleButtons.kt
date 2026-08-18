@file:CatalogGroup(name = "Toggle buttons", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextToggleButton
import androidx.wear.compose.material3.TextToggleButtonDefaults
import androidx.wear.compose.material3.touchTargetAwareSize
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker
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
// than a frame of an animation, and the live session still animates the press.

@Composable
private fun iconToggleSize(): Dp =
  when (previewOverrideString("size", "default")) {
    "small" -> IconToggleButtonDefaults.SmallSize
    "large" -> IconToggleButtonDefaults.LargeSize
    "extra-large" -> IconToggleButtonDefaults.ExtraLargeSize
    else -> IconToggleButtonDefaults.Size
  }

@CatalogComponent(
  id = "IconToggleButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39083:679",
  caption = "An icon that holds an on/off state, with the kit's size and state axes folded in.",
)
@CatalogModes
@OverrideVariant(name = "off", booleans = ["checked=false"], kitAxis = "Selected", kitValue = "Off")
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-large",
  strings = ["size=extra-large"],
  kitAxis = "Size",
  kitValue = "Extra-Large",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun IconToggle() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  IconToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(iconToggleSize()),
  ) {
    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
  }
}

@Composable
private fun textToggleSize(): Dp =
  when (previewOverrideString("size", "default")) {
    "large" -> TextToggleButtonDefaults.LargeSize
    "extra-large" -> TextToggleButtonDefaults.ExtraLargeSize
    else -> TextToggleButtonDefaults.Size
  }

@CatalogComponent(
  id = "TextToggleButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39083:760",
  caption = "A short label that holds an on/off state.",
)
@CatalogModes
@OverrideVariant(name = "off", booleans = ["checked=false"], kitAxis = "Selected", kitValue = "Off")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-large",
  strings = ["size=extra-large"],
  kitAxis = "Size",
  kitValue = "Extra-Large",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun TextToggle() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  TextToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(textToggleSize()),
  ) {
    Text("Mon")
  }
}
