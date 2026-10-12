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
import generated.uibuilder.KitChildButtonIconCentre
import generated.uibuilder.KitChildButtonIconExtraLargeCentre
import generated.uibuilder.KitChildButtonIconExtraLargeLeft
import generated.uibuilder.KitChildButtonIconLargeCentre
import generated.uibuilder.KitChildButtonIconLargeLeft
import generated.uibuilder.KitChildButtonIconLeft
import generated.uibuilder.KitChildButtonLeft
import generated.uibuilder.KitOutlinedButton
import generated.uibuilder.KitOutlinedButtonIconCentre
import generated.uibuilder.KitOutlinedButtonIconExtraLargeCentre
import generated.uibuilder.KitOutlinedButtonIconExtraLargeLeft
import generated.uibuilder.KitOutlinedButtonIconLargeCentre
import generated.uibuilder.KitOutlinedButtonIconLargeLeft
import generated.uibuilder.KitOutlinedButtonIconLeft
import generated.uibuilder.KitOutlinedButtonLeft
import generated.uibuilder.KitTonalButton
import generated.uibuilder.KitTonalButtonIconCentre
import generated.uibuilder.KitTonalButtonIconExtraLargeCentre
import generated.uibuilder.KitTonalButtonIconExtraLargeLeft
import generated.uibuilder.KitTonalButtonIconLargeCentre
import generated.uibuilder.KitTonalButtonIconLargeLeft
import generated.uibuilder.KitTonalButtonIconLeft
import generated.uibuilder.KitTonalButtonLeft

// Shells: the hand-written stickers' annotations and names, with bodies generated from
// `stickers/<name>.uid`. The knobs are read here, in the hand-written order, and choose the cell:
// every combination a knob can reach has one, not only the combinations a capture seeds.

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
  // The kit couples the icon to a secondary label; the label keeps the alignment its knob asks for.
  if (icon) {
    val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)
    val left = alignment == LabelAlignment.Left
    when (iconSize) {
      ButtonIconSize.Default ->
        if (left) KitTonalButtonIconLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitTonalButtonIconCentre(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.Large ->
        if (left) KitTonalButtonIconLargeLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitTonalButtonIconLargeCentre(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.ExtraLarge ->
        if (left) KitTonalButtonIconExtraLargeLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitTonalButtonIconExtraLargeCentre(Modifier, enabled, c.label, secondary, c.onClick)
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
  // The kit couples the icon to a secondary label; the label keeps the alignment its knob asks for.
  if (icon) {
    val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)
    val left = alignment == LabelAlignment.Left
    when (iconSize) {
      ButtonIconSize.Default ->
        if (left) KitOutlinedButtonIconLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitOutlinedButtonIconCentre(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.Large ->
        if (left) KitOutlinedButtonIconLargeLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitOutlinedButtonIconLargeCentre(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.ExtraLarge ->
        if (left)
          KitOutlinedButtonIconExtraLargeLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitOutlinedButtonIconExtraLargeCentre(Modifier, enabled, c.label, secondary, c.onClick)
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
  // The kit couples the icon to a secondary label; the label keeps the alignment its knob asks for.
  if (icon) {
    val secondary = kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)
    val left = alignment == LabelAlignment.Left
    when (iconSize) {
      ButtonIconSize.Default ->
        if (left) KitChildButtonIconLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitChildButtonIconCentre(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.Large ->
        if (left) KitChildButtonIconLargeLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitChildButtonIconLargeCentre(Modifier, enabled, c.label, secondary, c.onClick)
      ButtonIconSize.ExtraLarge ->
        if (left) KitChildButtonIconExtraLargeLeft(Modifier, enabled, c.label, secondary, c.onClick)
        else KitChildButtonIconExtraLargeCentre(Modifier, enabled, c.label, secondary, c.onClick)
    }
  } else if (alignment == LabelAlignment.Left) {
    KitChildButtonLeft(Modifier, enabled, c.label, c.onClick)
  } else {
    KitChildButton(Modifier, enabled, c.label, c.onClick)
  }
}
