@file:CatalogGroup(name = "Selection buttons", section = "Selection")

package ee.schimke.wearm3catalog.uid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.sections.SelectionCells
import ee.schimke.wearm3catalog.toggleable
import generated.uibuilder.KitCheckboxButton
import generated.uibuilder.KitCheckboxButtonSplit
import generated.uibuilder.KitRadioButton
import generated.uibuilder.KitRadioButtonSplit
import generated.uibuilder.KitSwitchButton
import generated.uibuilder.KitSwitchButtonSplit

// Shells: the hand-written stickers' annotations and names, with bodies generated from
// `stickers/<name>.uid`. The toggle's state lives here (`toggleable`); the generated control
// reports a change as an intent, and the shell flips its own state. A split control's container
// tap does nothing, as in the hand-written sticker.

@CatalogComponent(
  id = "CheckboxButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85642",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that toggles a checkbox; for a set where any number may be chosen.",
)
@CatalogModes
@SelectionCells
@Composable
fun CheckboxRow(
  checked: Boolean = true,
  enabled: Boolean = true,
  split: Boolean = false,
) = Sticker {
  val (checked, onCheckedChange) = toggleable(checked)
  val label = kitCopy("label", KitCopy.PRIMARY)
  val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY)
  if (split) {
    KitCheckboxButtonSplit(
      Modifier,
      checked,
      enabled,
      label,
      secondary,
      { onCheckedChange(!checked) },
      {},
    )
  } else {
    KitCheckboxButton(Modifier, checked, enabled, label, secondary) { onCheckedChange(!checked) }
  }
}

@CatalogComponent(
  id = "SwitchButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85629",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that flips a switch; for a setting that takes effect immediately.",
  // The thumb's travel, recorded in Motion.kt — the spring a still of either end state cannot show.
  motionPreview = "SwitchTransitionMotion",
)
@CatalogModes
@SelectionCells
@Composable
fun SwitchRow(
  checked: Boolean = true,
  enabled: Boolean = true,
  split: Boolean = false,
) = Sticker {
  val (checked, onCheckedChange) = toggleable(checked)
  val label = kitCopy("label", KitCopy.PRIMARY)
  val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY)
  if (split) {
    KitSwitchButtonSplit(
      Modifier,
      checked,
      enabled,
      label,
      secondary,
      { onCheckedChange(!checked) },
      {},
    )
  } else {
    KitSwitchButton(Modifier, checked, enabled, label, secondary) { onCheckedChange(!checked) }
  }
}

@CatalogComponent(
  id = "RadioButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85655",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row in a set where exactly one may be chosen.",
)
@CatalogModes
@SelectionCells
@Composable
fun RadioRow(
  checked: Boolean = true,
  enabled: Boolean = true,
  split: Boolean = false,
) = Sticker {
  val (selected, onSelectedChange) = toggleable(checked)
  val label = kitCopy("label", KitCopy.PRIMARY)
  val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY)
  if (split) {
    KitRadioButtonSplit(
      Modifier,
      selected,
      enabled,
      label,
      secondary,
      { onSelectedChange(true) },
      {},
    )
  } else {
    KitRadioButton(Modifier, selected, enabled, label, secondary) { onSelectedChange(true) }
  }
}
