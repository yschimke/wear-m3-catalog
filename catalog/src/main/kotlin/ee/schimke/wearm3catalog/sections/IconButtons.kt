@file:CatalogGroup(name = "Icon buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted

// The kit's `Icon-Button` and `Text-Button` sets. Emphasis splits into a component per style, for
// the reason Buttons.kt states: each is its own Wear Compose function. The kit's `Size=` axis does
// not — it is a `Modifier.touchTargetAwareSize(IconButtonDefaults.<size>)` argument to whichever
// function you picked, so it folds as cells on every one of them.
//
// `Text-Button` is the exception that proves the rule. The kit gives it the same five styles, but
// Wear Compose ships ONE `TextButton` that takes its emphasis as `colors` — there is no second
// function to choose at the call site, so the styles fold as cells rather than splitting.
//
// Its BASE cell is the filled style, not the child one. A folded component's base render is the
// card the sheet fronts, and `Child (No background)` is a bare letter on black — a picture of the
// absence of a container. The other styles, including child, ride as cells.

/** The kit's `Size=` values, as the Wear touch-target sizes each one names. */
@Composable
private fun iconButtonSize(): Dp =
  when (previewOverrideString("size", "default")) {
    "extra-small" -> IconButtonDefaults.ExtraSmallButtonSize
    "small" -> IconButtonDefaults.SmallButtonSize
    "large" -> IconButtonDefaults.LargeButtonSize
    else -> IconButtonDefaults.DefaultButtonSize
  }

@Composable private fun favourite() = Icon(Icons.Filled.Favorite, contentDescription = "Favourite")

@CatalogComponent(
  id = "IconButton/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
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
    favourite()
  }
}

@CatalogComponent(
  id = "IconButton/Tonal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Medium emphasis, on a tonal container.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
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
    favourite()
  }
}

@CatalogComponent(
  id = "IconButton/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Medium emphasis, drawn as an outline over the background.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
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
    favourite()
  }
}

@CatalogComponent(
  id = "IconButton/Standard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Lowest emphasis; the kit's child style, with no container at all.",
)
@CatalogModes
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@OverrideVariant(name = "large", strings = ["size=large"], kitAxis = "Size", kitValue = "Large")
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
    favourite()
  }
}

@CatalogComponent(
  id = "TextButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103080",
  caption = "A short label as a round action, with the kit's style and size axes folded in.",
)
@CatalogModes
// No `filled` cell: filled IS the base render (see below), so a cell for it publishes the base
// picture a second time under another name.
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
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun TextAction() = Sticker {
  val c = counted("A")
  val colors =
    when (previewOverrideString("style", "filled")) {
      "child" -> TextButtonDefaults.textButtonColors()
      "tonal" -> TextButtonDefaults.filledTonalTextButtonColors()
      "outlined" -> TextButtonDefaults.outlinedTextButtonColors()
      else -> TextButtonDefaults.filledTextButtonColors()
    }
  // The border is its OWN parameter, not part of `colors` — an outlined text button built from
  // `outlinedTextButtonColors()` alone draws no outline and is pixel-identical to the child style,
  // which is how this cell published the wrong picture under the right name.
  val style = previewOverrideString("style", "filled")
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
  when (previewOverrideString("size", "default")) {
    "small" -> TextButtonDefaults.SmallButtonSize
    "large" -> TextButtonDefaults.LargeButtonSize
    else -> TextButtonDefaults.DefaultButtonSize
  }
