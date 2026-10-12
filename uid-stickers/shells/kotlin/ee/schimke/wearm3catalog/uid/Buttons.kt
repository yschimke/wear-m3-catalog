@file:CatalogGroup(name = "Buttons", section = "Actions")

package ee.schimke.wearm3catalog.uid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.sections.ButtonIconSize
import ee.schimke.wearm3catalog.sections.ButtonLayoutCells
import ee.schimke.wearm3catalog.sections.LabelAlignment
import generated.uibuilder.KitChildButton
import generated.uibuilder.KitChildButtonIcon
import generated.uibuilder.KitChildButtonIconExtraLarge
import generated.uibuilder.KitChildButtonIconLarge
import generated.uibuilder.KitChildButtonLeft
import generated.uibuilder.KitOutlinedButton
import generated.uibuilder.KitOutlinedButtonIcon
import generated.uibuilder.KitOutlinedButtonIconExtraLarge
import generated.uibuilder.KitOutlinedButtonIconLarge
import generated.uibuilder.KitOutlinedButtonLeft
import generated.uibuilder.KitTonalButton
import generated.uibuilder.KitTonalButtonIcon
import generated.uibuilder.KitTonalButtonIconExtraLarge
import generated.uibuilder.KitTonalButtonIconLarge
import generated.uibuilder.KitTonalButtonLeft

// Shells: the hand-written stickers' annotations and names, with bodies generated from
// `stickers/<name>.uid`. The knobs are read here, in the hand-written order, and choose the cell.

@CatalogComponent(
  id = "Button/Tonal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93104",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Medium emphasis, on a tonal container.",
)
@CatalogModes
@ButtonLayoutCells
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun TonalButton(
  enabled: Boolean = true,
  icon: Boolean = false,
  iconSize: ButtonIconSize = ButtonIconSize.Default,
  alignment: LabelAlignment = LabelAlignment.Center,
) = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  // The kit couples the icon to a secondary label and a start-aligned label: every `Icon=Yes`
  // cell is drawn that way, so the icon cells carry all three.
  if (icon) {
    val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)
    when (iconSize) {
      ButtonIconSize.Default -> KitTonalButtonIcon(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.Large ->
        KitTonalButtonIconLarge(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.ExtraLarge ->
        KitTonalButtonIconExtraLarge(Modifier, enabled, c.label, secondary, c.onClick)
    }
  } else if (alignment == LabelAlignment.Left) {
    KitTonalButtonLeft(Modifier, enabled, c.label, c.onClick)
  } else {
    KitTonalButton(Modifier, enabled, c.label, c.onClick)
  }
}

@CatalogComponent(
  id = "Button/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93116",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Medium emphasis, drawn as an outline over the background.",
)
@CatalogModes
@ButtonLayoutCells
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun OutlineButton(
  enabled: Boolean = true,
  icon: Boolean = false,
  iconSize: ButtonIconSize = ButtonIconSize.Default,
  alignment: LabelAlignment = LabelAlignment.Center,
) = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  // The kit couples the icon to a secondary label and a start-aligned label: every `Icon=Yes`
  // cell is drawn that way, so the icon cells carry all three.
  if (icon) {
    val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)
    when (iconSize) {
      ButtonIconSize.Default ->
        KitOutlinedButtonIcon(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.Large ->
        KitOutlinedButtonIconLarge(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.ExtraLarge ->
        KitOutlinedButtonIconExtraLarge(Modifier, enabled, c.label, secondary, c.onClick)
    }
  } else if (alignment == LabelAlignment.Left) {
    KitOutlinedButtonLeft(Modifier, enabled, c.label, c.onClick)
  } else {
    KitOutlinedButton(Modifier, enabled, c.label, c.onClick)
  }
}

@CatalogComponent(
  id = "Button/Child",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93128",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Lowest emphasis; no container at all, for a button inside another surface.",
)
@CatalogModes
@ButtonLayoutCells
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ChildLabelButton(
  enabled: Boolean = true,
  icon: Boolean = false,
  iconSize: ButtonIconSize = ButtonIconSize.Default,
  alignment: LabelAlignment = LabelAlignment.Center,
) = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  // The kit couples the icon to a secondary label and a start-aligned label: every `Icon=Yes`
  // cell is drawn that way, so the icon cells carry all three.
  if (icon) {
    val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)
    when (iconSize) {
      ButtonIconSize.Default -> KitChildButtonIcon(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.Large ->
        KitChildButtonIconLarge(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.ExtraLarge ->
        KitChildButtonIconExtraLarge(Modifier, enabled, c.label, secondary, c.onClick)
    }
  } else if (alignment == LabelAlignment.Left) {
    KitChildButtonLeft(Modifier, enabled, c.label, c.onClick)
  } else {
    KitChildButton(Modifier, enabled, c.label, c.onClick)
  }
}
