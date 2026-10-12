@file:CatalogGroup(name = "Text", section = "Text")

package ee.schimke.wearm3catalog.uid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.sections.SubHeadingAlign
import ee.schimke.wearm3catalog.sections.TextAlignment
import generated.uibuilder.KitBodyText
import generated.uibuilder.KitBodyTextLeft
import generated.uibuilder.KitBodyTextRight
import generated.uibuilder.KitCaptionText
import generated.uibuilder.KitCaptionTextLeft
import generated.uibuilder.KitCaptionTextRight
import generated.uibuilder.KitListHeader
import generated.uibuilder.KitListHeaderLeft
import generated.uibuilder.KitListHeaderRight
import generated.uibuilder.KitListSubHeader
import generated.uibuilder.KitListSubHeaderCentred
import generated.uibuilder.KitListSubHeaderIcon
import generated.uibuilder.KitListSubHeaderIconCentred

// Shells: the hand-written stickers' annotations and names, with bodies generated from
// `stickers/<name>.uid`. The knobs are read here, in the hand-written order, and choose the cell.
// Every combination a knob can reach has a cell, not only the combinations a capture seeds.

@CatalogComponent(
  id = "ListHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66978",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66977",
  caption = "Titles the screen a list belongs to; the first thing above the list.",
)
@CatalogModes
// The kit's other axis, `Type = Page-Top | Page-Mid`, is NOT a cell and cannot be one:
// `ListHeaderDefaults` publishes a single `contentPadding` and `ListHeader` takes no argument that
// distinguishes the first header on a screen from one further down a list. The two kit cells
// differ in the space above them, which is a property of where the header sits rather than of the
// component — so the set stays at two of its four cells, with the reason here rather than in a
// silence ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun ListHeading(align: TextAlignment = TextAlignment.Centre) = Sticker {
  val label = kitCopy("label", KitCopy.TITLE)
  when (align) {
    TextAlignment.Left -> KitListHeaderLeft(Modifier, label)
    TextAlignment.Right -> KitListHeaderRight(Modifier, label)
    TextAlignment.Centre -> KitListHeader(Modifier, label)
  }
}

@CatalogComponent(
  id = "ListSubHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66983",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66982",
  caption = "Divides a long list into named runs, without leaving the screen's title behind.",
)
@CatalogModes
// The kit publishes `Icon=Yes` at `Alignment=Left` only — the icon leads the row, so there is
// nothing for a centred variant of it to be — which is why the icon cell names one axis and the
// centred one names the pair.
@OverrideVariant(
  name = "icon",
  booleans = ["icon=true"],
  kitProps = ["Icon=Yes", "Alignment=Left"],
)
@OverrideVariant(
  name = "centred",
  strings = ["align=centre"],
  kitProps = ["Icon=No", "Alignment=Centre"],
)
@Composable
fun ListSubHeading(
  icon: Boolean = false,
  align: SubHeadingAlign = SubHeadingAlign.Left,
) = Sticker {
  val label = kitCopy("label", KitCopy.SUBTITLE)
  val centred = align == SubHeadingAlign.Centre
  when {
    icon && centred -> KitListSubHeaderIconCentred(Modifier, label)
    icon -> KitListSubHeaderIcon(Modifier, label)
    centred -> KitListSubHeaderCentred(Modifier, label)
    else -> KitListSubHeader(Modifier, label)
  }
}

@CatalogComponent(
  id = "Text/Body",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66993",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66990",
  caption = "Running text at the `bodyMedium` role of the Wear type scale.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun BodyText(align: TextAlignment = TextAlignment.Centre) = Sticker {
  val text = kitCopy("text", KitCopy.BODY)
  when (align) {
    TextAlignment.Left -> KitBodyTextLeft(Modifier, text)
    TextAlignment.Right -> KitBodyTextRight(Modifier, text)
    TextAlignment.Centre -> KitBodyText(Modifier, text)
  }
}

@CatalogComponent(
  id = "Text/Caption",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66998",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66995",
  caption = "Secondary text at the `labelSmall` role — a timestamp, a unit, a footnote.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun CaptionText(align: TextAlignment = TextAlignment.Centre) = Sticker {
  val text = kitCopy("text", KitCopy.CAPTION)
  when (align) {
    TextAlignment.Left -> KitCaptionTextLeft(Modifier, text)
    TextAlignment.Right -> KitCaptionTextRight(Modifier, text)
    TextAlignment.Centre -> KitCaptionText(Modifier, text)
  }
}
