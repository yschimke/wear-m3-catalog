@file:CatalogGroup(name = "Selection buttons", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.kitRowWidth
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
//
// TWO LINES, because every cell of the kit's set has two. The stickers drew the primary label
// alone, so the comparison reported the missing `Secondary` line as a difference in all 24 cells —
// and a selection row is a visibly shorter shape with one line than with two, so it was not only
// the words. `KitCopy.SECONDARY` was already transcribed for this set and unused, which is the
// tell.

/**
 * **Every cell the kit publishes for one selection `Type`** — `Selected` by `Split (2 tap targets)`
 * by `Disabled`, a clean eight-cell product, all eight of them drawn. The catalog used to draw the
 * base plus one cell per axis, so the four crossings (an unselected split row, a disabled split
 * row, …) were compared against nothing
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * Hoisted onto one annotation class and applied to all three types rather than written out three
 * times: the axes are arguments to whichever function you picked, so the cells are identical for
 * `Checkbox`, `Radio` and `Switch`, and `Type` is carried by each component's own `reference`.
 *
 * A crossing declares its whole assignment with `kitProps`, not `kitAxis` — a cell that turns two
 * knobs has no single axis to name, and the pair would be dropped rather than guessed at.
 */
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
@OverrideVariant(
  name = "unselected-split",
  booleans = ["checked=false", "split=true"],
  kitProps = ["Selected=No", "Split (2 tap targets)=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "unselected-disabled",
  booleans = ["checked=false", "enabled=false"],
  kitProps = ["Selected=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "split-disabled",
  booleans = ["split=true", "enabled=false"],
  kitProps = ["Split (2 tap targets)=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "unselected-split-disabled",
  booleans = ["checked=false", "split=true", "enabled=false"],
  kitProps = ["Selected=No", "Split (2 tap targets)=Yes", "Disabled=Yes"],
  secondary = true,
)
annotation class SelectionCells

@CatalogComponent(
  id = "CheckboxButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85642",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that toggles a checkbox; for a set where any number may be chosen.",
)
@CatalogModes
@SelectionCells
@Composable
fun CheckboxRow() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true)
  if (previewOverrideBoolean("split", false)) {
    SplitCheckboxButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.kitRowWidth(),
      toggleContentDescription = kitCopy("label", KitCopy.PRIMARY),
      onContainerClick = {},
      enabled = enabled,
      label = { Text(kitCopy("label", KitCopy.PRIMARY)) },
      secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY)) },
    )
  } else {
    CheckboxButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.kitRowWidth(),
      enabled = enabled,
      label = { Text(kitCopy("label", KitCopy.PRIMARY)) },
      secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY)) },
    )
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
fun SwitchRow() = Sticker {
  val (checked, onCheckedChange) = toggleable(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true)
  if (previewOverrideBoolean("split", false)) {
    SplitSwitchButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.kitRowWidth(),
      toggleContentDescription = kitCopy("label", KitCopy.PRIMARY),
      onContainerClick = {},
      enabled = enabled,
      label = { Text(kitCopy("label", KitCopy.PRIMARY)) },
      secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY)) },
    )
  } else {
    SwitchButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.kitRowWidth(),
      enabled = enabled,
      label = { Text(kitCopy("label", KitCopy.PRIMARY)) },
      secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY)) },
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
@SelectionCells
@Composable
fun RadioRow() = Sticker {
  val (selected, onSelectedChange) = toggleable(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true)
  if (previewOverrideBoolean("split", false)) {
    SplitRadioButton(
      selected = selected,
      onSelectionClick = { onSelectedChange(true) },
      modifier = Modifier.kitRowWidth(),
      selectionContentDescription = kitCopy("label", KitCopy.PRIMARY),
      onContainerClick = {},
      enabled = enabled,
      label = { Text(kitCopy("label", KitCopy.PRIMARY)) },
      secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY)) },
    )
  } else {
    RadioButton(
      selected = selected,
      onSelect = { onSelectedChange(true) },
      modifier = Modifier.kitRowWidth(),
      enabled = enabled,
      label = { Text(kitCopy("label", KitCopy.PRIMARY)) },
      secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY)) },
    )
  }
}
