@file:CatalogGroup(name = "Selection buttons", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SplitCheckboxButton
import androidx.wear.compose.material3.SplitRadioButton
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.toggleable

// The kit's `Toggle+Selection-Buttons` set — one set carrying `Type = Checkbox | Radio | Switch |
// Custom - Task`. Three components, not one, because each Type is its own Wear Compose function and
// picking one is the call-site choice; the `Split (2 tap targets)`, `Selected` and `Disabled` axes
// are arguments to whichever you picked, so they fold. See AGENTS.md.
//
// `Custom - Task` is the kit showing that the control slot is swappable rather than a fourth
// component, and Compose expresses it as the `toggleControl` slot on the same functions. It stays
// out of the inventory until a sticker can draw it without inventing a control the kit does not
// publish.
//
// These own their state: a selection control that cannot be selected is not the component.

@CatalogComponent(
  id = "CheckboxButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85642",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that toggles a checkbox; for a set where any number may be chosen.",
)
@CatalogModes
@OverrideVariant(
  name = "unselected",
  booleans = ["checked=false"],
  kitAxis = "Selected",
  kitValue = "No",
)
@OverrideVariant(
  name = "split",
  booleans = ["split=true"],
  kitAxis = "Split (2 tap targets)",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun CheckboxRow() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true)
  if (previewOverrideBoolean("split", false)) {
    SplitCheckboxButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      toggleContentDescription = "Alarm",
      onContainerClick = {},
      enabled = enabled,
      label = { Text("Alarm") },
    )
  } else {
    CheckboxButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      label = { Text("Alarm") },
    )
  }
}

@CatalogComponent(
  id = "SwitchButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85629",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that flips a switch; for a setting that takes effect immediately.",
)
@CatalogModes
@OverrideVariant(
  name = "unselected",
  booleans = ["checked=false"],
  kitAxis = "Selected",
  kitValue = "No",
)
@OverrideVariant(
  name = "split",
  booleans = ["split=true"],
  kitAxis = "Split (2 tap targets)",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun SwitchRow() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true)
  if (previewOverrideBoolean("split", false)) {
    SplitSwitchButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      toggleContentDescription = "Bluetooth",
      onContainerClick = {},
      enabled = enabled,
      label = { Text("Bluetooth") },
    )
  } else {
    SwitchButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      label = { Text("Bluetooth") },
    )
  }
}

@CatalogComponent(
  id = "RadioButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85655",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row in a set where exactly one may be chosen.",
)
@CatalogModes
@OverrideVariant(
  name = "unselected",
  booleans = ["checked=false"],
  kitAxis = "Selected",
  kitValue = "No",
)
@OverrideVariant(
  name = "split",
  booleans = ["split=true"],
  kitAxis = "Split (2 tap targets)",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun RadioRow() = Sticker {
  val (selected, onSelectedChange) = toggleable(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true)
  if (previewOverrideBoolean("split", false)) {
    SplitRadioButton(
      selected = selected,
      onSelectionClick = { onSelectedChange(true) },
      selectionContentDescription = "Vibrate",
      onContainerClick = {},
      enabled = enabled,
      label = { Text("Vibrate") },
    )
  } else {
    RadioButton(
      selected = selected,
      onSelect = { onSelectedChange(true) },
      enabled = enabled,
      label = { Text("Vibrate") },
    )
  }
}
