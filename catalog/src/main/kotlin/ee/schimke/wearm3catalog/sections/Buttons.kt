@file:CatalogGroup(name = "Buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ChildButton
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted

// The kit's `Button` page. One kit set per file section, and one catalog component per EMPHASIS —
// the documented carve-out to "one kit set is one catalog component".
//
// The kit models emphasis as a `Style=` property on a single `Button` set, so folding it would be
// the literal reading of the rule. It is the wrong one here: `Button`, `FilledTonalButton`,
// `OutlinedButton` and `ChildButton` are four separate Wear Compose functions, and which one you
// call is the choice a reader of this catalog is making. The axes that stay folded are the ones
// that are arguments to whichever function you picked — `enabled`, whether there is an icon, how
// the content is aligned. All five components therefore name the same kit set node, and each cell
// names the kit's own value for the axis it turns.
//
// `Variant (Highlighted)` is the kit's fifth style and Compose spells it as colours rather than a
// function — `ButtonDefaults.filledVariantButtonColors()` on the ordinary `Button` — so it is a
// component here for the same call-site reason, not a cell.
//
// Naming follows Compose: the kit's `Style=Outline` is `Button/Outlined`, because `OutlinedButton`
// is what a reader greps for.

// The kit node each component names is written out in full on the annotation rather than through a
// constant. It repeats, and that is the trade: the reference is the one field that decides what a
// sticker is compared against, and a source scan — CatalogInventoryTest, and the map projector —
// reads the annotation, not a resolved constant.

/** The leading icon the kit's `Icon=Yes` cells draw, and nothing when they draw `Icon=No`. */
@Composable
private fun leadingIcon(): (@Composable BoxScope.() -> Unit)? =
  if (!previewOverrideBoolean("icon", false)) null
  else {
    { Icon(Icons.Filled.Check, contentDescription = null) }
  }

@CatalogComponent(
  id = "Button/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Highest emphasis; the screen's primary action.",
)
@CatalogModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun FilledButton() = Sticker {
  val c = counted("Filled")
  Button(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { Text(c.label) },
  )
}

@CatalogComponent(
  id = "Button/FilledVariant",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "The kit's highlighted style — a filled button in the variant palette.",
)
@CatalogModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun FilledVariantButton() = Sticker {
  val c = counted("Variant")
  Button(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = ButtonDefaults.filledVariantButtonColors(),
    icon = leadingIcon(),
    label = { Text(c.label) },
  )
}

@CatalogComponent(
  id = "Button/Tonal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Medium emphasis, on a tonal container.",
)
@CatalogModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun TonalButton() = Sticker {
  val c = counted("Tonal")
  FilledTonalButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { Text(c.label) },
  )
}

@CatalogComponent(
  id = "Button/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Medium emphasis, drawn as an outline over the background.",
)
@CatalogModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun OutlineButton() = Sticker {
  val c = counted("Outlined")
  OutlinedButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { Text(c.label) },
  )
}

@CatalogComponent(
  id = "Button/Child",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Lowest emphasis; no container at all, for a button inside another surface.",
)
@CatalogModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ChildLabelButton() = Sticker {
  val c = counted("Child")
  ChildButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { Text(c.label) },
  )
}

// The compact set is its own kit set and its own Compose function, so it is one component with the
// kit's content axis folded in. `Style=` is NOT split here the way it is above: `CompactButton`
// takes its emphasis as `colors`, so there is no second function to choose at the call site — the
// distinction the carve-out exists to preserve isn't there.
@CatalogComponent(
  id = "Button/Compact",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35276:87971",
  caption = "A short button for a dense screen, with the kit's content and style axes folded in.",
)
@CatalogModes
@OverrideVariant(name = "icon-only", strings = ["content=icon"], kitAxis = "Text", kitValue = "No")
@OverrideVariant(name = "text-only", strings = ["content=text"], kitAxis = "Icon", kitValue = "No")
@OverrideVariant(name = "tonal", strings = ["style=tonal"], kitAxis = "Style", kitValue = "Tonal")
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun CompactActionButton() = Sticker {
  val c = counted("Compact")
  val content = previewOverrideString("content", "icon+text")
  val colors =
    when (previewOverrideString("style", "filled")) {
      "tonal" -> ButtonDefaults.filledTonalButtonColors()
      "outlined" -> ButtonDefaults.outlinedButtonColors()
      else -> ButtonDefaults.buttonColors()
    }
  CompactButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
    icon =
      if (content == "text") null
      else {
        { Icon(Icons.Filled.Check, contentDescription = null) }
      },
    label =
      if (content == "icon") null
      else {
        { Text(c.label) }
      },
  )
}
