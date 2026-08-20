@file:CatalogGroup(name = "Icon buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import androidx.wear.compose.material3.touchTargetAwareSize
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Icon-Button` and `Text-Button` sets. Emphasis splits into a component per style, for
// the reason Buttons.kt states: each is its own Wear Compose function. The kit's `Size=` axis does
// not — it is a `Modifier.touchTargetAwareSize(IconButtonDefaults.<size>)` argument to whichever
// function you picked, so it folds as cells on every one of them.
//
// `Text-Button` is the exception that proves the rule. The kit gives it the same five styles, but
// Wear Compose ships ONE `TextButton` that takes its emphasis as `colors` — there is no second
// function to choose at the call site, so the styles fold as cells rather than splitting.
//
// `Filled Variant` is the kit style Compose spells as COLOURS rather than a function
// (`IconButtonDefaults.filledVariantIconButtonColors()` on `FilledIconButton`). It is a component
// here rather than a cell for the reason Buttons.kt gives for `Button/FilledVariant`: the emphasis
// is still the choice a reader is making, and the two repos' button pages should not disagree
// about where that choice lives.
//
// Its BASE cell is the filled style, not the child one. A folded component's base render is the
// card the sheet fronts, and `Child (No background)` is a bare letter on black — a picture of the
// absence of a container. The other styles, including child, ride as cells.

/** The kit's `Size=` values, as the Wear touch-target sizes each one names. */
@Composable
private fun iconButtonSize(): Dp =
  when (
    previewOverrideChoice("size", "default", listOf("default", "extra-small", "small", "large"))
  ) {
    "extra-small" -> IconButtonDefaults.ExtraSmallButtonSize
    "small" -> IconButtonDefaults.SmallButtonSize
    "large" -> IconButtonDefaults.LargeButtonSize
    else -> IconButtonDefaults.DefaultButtonSize
  }

// The kit draws a plus in every icon-button cell, so this does too — a heart is a different
// picture in the one slot these components have.
//
// The size is passed explicitly because Wear's `IconButton` does not size its content: the slot
// takes whatever the caller puts in it, and a bare `Icon` falls back to Material's 24dp default.
// The kit's `Size=Default` cell is a 52x52 frame around a 26x26 icon (34732:103015), so the
// default drew two dp small in every cell and the icon-to-button ratio was wrong at every size —
// `iconSizeFor` is the pairing Wear publishes for exactly this.
@Composable
private fun kitGlyph(size: Dp = iconButtonSize()) =
  Icon(
    Icons.Filled.Add,
    contentDescription = "Add",
    modifier = Modifier.size(IconButtonDefaults.iconSizeFor(size)),
  )

@CatalogComponent(
  id = "IconButton/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102976",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Highest emphasis; an icon-only action.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun FilledIconAction() = Sticker {
  val c = counted("filled")
  FilledIconButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(iconButtonSize()),
  ) {
    kitGlyph()
  }
}

@CatalogComponent(
  id = "IconButton/FilledVariant",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41409:52153",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "The kit's highlighted style — a filled icon button in the variant palette.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun FilledVariantIconAction() = Sticker {
  val c = counted("variant")
  FilledIconButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = IconButtonDefaults.filledVariantIconButtonColors(),
    modifier = Modifier.touchTargetAwareSize(iconButtonSize()),
  ) {
    kitGlyph()
  }
}

@CatalogComponent(
  id = "IconButton/Tonal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102989",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Medium emphasis, on a tonal container.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun TonalIconAction() = Sticker {
  val c = counted("tonal")
  FilledTonalIconButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(iconButtonSize()),
  ) {
    kitGlyph()
  }
}

@CatalogComponent(
  id = "IconButton/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103002",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Medium emphasis, drawn as an outline over the background.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun OutlinedIconAction() = Sticker {
  val c = counted("outlined")
  OutlinedIconButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(iconButtonSize()),
  ) {
    kitGlyph()
  }
}

@CatalogComponent(
  id = "IconButton/Standard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103015",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Lowest emphasis; the kit's child style, with no container at all.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
// NO `extra-small` cell, and this is the one style that cannot have one. The kit publishes
// `Size=Extra-Small` for all five, and `IconButtonDefaults.ExtraSmallButtonSize` exists — but the
// size is a CONTAINER token, and the child style draws no container. Both extra-small and small
// then clamp to the same minimum touch target around an unchanged glyph, so the cell published the
// small render a second time: byte-identical, and caught by `CatalogRenderTest.no two renders of a
// component are identical`. The other four styles carry the cell, because on them the container is
// the thing that changes.
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun StandardIconAction() = Sticker {
  val c = counted("standard")
  IconButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.touchTargetAwareSize(iconButtonSize()),
  ) {
    kitGlyph()
  }
}

@CatalogComponent(
  id = "TextButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103081",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:103080",
  caption = "A short label as a round action, with the kit's style and size axes folded in.",
)
@CatalogModes
// No `filled` cell: filled IS the base render (see below), so a cell for it publishes the base
// picture a second time under another name.
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitAxis = "Style",
  // The kit hyphenates it on THIS set and spaces it on `Icon-Button` — `Filled-Variant` here,
  // `Filled Variant` there. The seed keeps Compose's spelling either way; this names the kit's.
  kitValue = "Filled-Variant",
)
@OverrideVariant(name = "tonal", strings = ["style=tonal"], kitAxis = "Style", kitValue = "Tonal")
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@OverrideVariant(
  name = "child",
  strings = ["style=child"],
  kitAxis = "Style",
  kitValue = "Child (No background)",
)
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
// No `extra-small` cell: the kit gives `Text-Button` three sizes and `TextButtonDefaults` publishes
// the same three. Extra-small is an ICON-button size on both sides.
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun TextAction() = Sticker {
  val c = counted(kitCopy("label", KitCopy.GLYPHS))
  // Read once, used for both the colours and the border below.
  val style =
    previewOverrideChoice(
      "style",
      "filled",
      listOf("filled", "filled-variant", "tonal", "outlined", "child"),
    )
  val colors =
    when (style) {
      "child" -> TextButtonDefaults.textButtonColors()
      "filled-variant" -> TextButtonDefaults.filledVariantTextButtonColors()
      "tonal" -> TextButtonDefaults.filledTonalTextButtonColors()
      "outlined" -> TextButtonDefaults.outlinedTextButtonColors()
      else -> TextButtonDefaults.filledTextButtonColors()
    }
  // The border is its OWN parameter, not part of `colors` — an outlined text button built from
  // `outlinedTextButtonColors()` alone draws no outline and is pixel-identical to the child style,
  // which is how this cell published the wrong picture under the right name.
  TextButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
    border = if (style == "outlined") ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
    modifier = Modifier.touchTargetAwareSize(textButtonSize()),
  ) {
    Text(c.label)
  }
}

@Composable
private fun textButtonSize(): Dp =
  when (previewOverrideChoice("size", "default", listOf("default", "small", "large"))) {
    "small" -> TextButtonDefaults.SmallButtonSize
    "large" -> TextButtonDefaults.LargeButtonSize
    else -> TextButtonDefaults.DefaultButtonSize
  }
