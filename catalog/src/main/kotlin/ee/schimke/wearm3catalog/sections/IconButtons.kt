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

/**
 * **Every `Icon-Button` cell one style publishes** — the kit's four sizes crossed with `Disabled`,
 * eight nodes, seven of them variants of the base. Hoisted onto one annotation class and applied to
 * each style component rather than written out five times: the size is a `touchTargetAwareSize`
 * argument to whichever function you picked, so the cells are identical.
 *
 * The size run alone was already here; what was missing was the **crossing** — a disabled button at
 * anything but the default size, which is half the set
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)). A crossing declares both values
 * with `kitProps`, since `kitAxis` describes a cell that turns exactly one knob.
 */
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
@OverrideVariant(
  name = "small",
  strings = ["size=small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(
  name = "large",
  strings = ["size=large"],
  kitAxis = "Size",
  kitValue = "Large",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=extra-small"],
  kitProps = ["Size=Extra-Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=small"],
  kitProps = ["Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "large-disabled",
  booleans = ["enabled=false"],
  strings = ["size=large"],
  kitProps = ["Size=Large", "Disabled=Yes"],
  secondary = true,
)
annotation class IconButtonKitCells

@CatalogComponent(
  id = "IconButton/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102976",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Highest emphasis; an icon-only action.",
)
@CatalogModes
@IconButtonKitCells
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
@IconButtonKitCells
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
@IconButtonKitCells
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
@IconButtonKitCells
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
@IconButtonKitCells
// The `extra-small` cells here are the kit's distinction that the library does not draw. The size
// is a CONTAINER token and the child style draws no container, so extra-small and small clamp to
// the same minimum touch target around an unchanged glyph: `extra-small` is byte-identical to
// `small`, and `extra-small-disabled` to `small-disabled`. They used to be withheld for that, which
// left this style reading as six of the kit's eight cells with the collapse recorded nowhere a
// reader of the sheet would meet it. Both are published now and named in
// `CatalogRenderTest.knownDuplicate`, which fails from the other side when Wear learns to tell the
// two sizes apart ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
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

/**
 * **Every cell of the kit's `Text-Button` set** — all 30 nodes. The set is five styles by three
 * sizes by `Disabled`; eight cells were drawn, so every crossing (a small tonal button, a disabled
 * outlined one) was compared against nothing
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * The six disabled `Filled-Variant` and `Tonal` cells used to stay out, for the reason
 * `EdgeButtonKitCells` states at length: Wear resolves all three filled styles' disabled colours to
 * the same `onSurface` pair, so those cells ARE the disabled filled render under two more names.
 * They are drawn now, and recorded in `CatalogRenderTest.knownDuplicate` — a collapse the library
 * makes belongs on the sheet where a reader meets it, not behind a gap that reads as work nobody
 * got to ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
 *
 * No `filled` and no `default` cell — filled at the default size IS the base render, and a cell for
 * it publishes the base picture a second time under another name. No `extra-small` either: the kit
 * gives this set three sizes and `TextButtonDefaults` publishes the same three, extra-small being
 * an ICON-button size on both sides.
 *
 * The kit hyphenates `Filled-Variant` on THIS set and spaces it on `Icon-Button`. The seeds keep
 * Compose's spelling either way; each cell declares the kit's.
 */
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "small",
  strings = ["size=small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(
  name = "small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=small"],
  kitProps = ["Style=Filled", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "large",
  strings = ["size=large"],
  kitAxis = "Size",
  kitValue = "Large",
)
@OverrideVariant(
  name = "large-disabled",
  booleans = ["enabled=false"],
  strings = ["size=large"],
  kitProps = ["Style=Filled", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitAxis = "Style",
  kitValue = "Filled-Variant",
)
@OverrideVariant(
  name = "filled-variant-small",
  strings = ["style=filled-variant", "size=small"],
  kitProps = ["Style=Filled-Variant", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large",
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled-Variant", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled-Variant", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=small"],
  kitProps = ["Style=Filled-Variant", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled-Variant", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal",
  strings = ["style=tonal"],
  kitAxis = "Style",
  kitValue = "Tonal",
)
@OverrideVariant(
  name = "tonal-small",
  strings = ["style=tonal", "size=small"],
  kitProps = ["Style=Tonal", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large",
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=small"],
  kitProps = ["Style=Tonal", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@OverrideVariant(
  name = "outlined-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-small",
  strings = ["style=outlined", "size=small"],
  kitProps = ["Style=Outline", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=small"],
  kitProps = ["Style=Outline", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-large",
  strings = ["style=outlined", "size=large"],
  kitProps = ["Style=Outline", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=large"],
  kitProps = ["Style=Outline", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child",
  strings = ["style=child"],
  kitAxis = "Style",
  kitValue = "Child (No background)",
)
@OverrideVariant(
  name = "child-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child"],
  kitProps = ["Style=Child (No background)", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-small",
  strings = ["style=child", "size=small"],
  kitProps = ["Style=Child (No background)", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "size=small"],
  kitProps = ["Style=Child (No background)", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-large",
  strings = ["style=child", "size=large"],
  kitProps = ["Style=Child (No background)", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "size=large"],
  kitProps = ["Style=Child (No background)", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
annotation class TextButtonKitCells

@CatalogComponent(
  id = "TextButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103081",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:103080",
  caption = "A short label as a round action, with the kit's style and size axes folded in.",
)
@CatalogModes
@TextButtonKitCells
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
