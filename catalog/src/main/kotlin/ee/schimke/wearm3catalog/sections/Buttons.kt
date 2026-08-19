@file:CatalogGroup(name = "Buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.ChildButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogImage
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93092",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/39577:895",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93104",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93116",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93128",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/35276:87975",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:87971",
  caption = "A short button for a dense screen, with the kit's content and style axes folded in.",
)
@CatalogModes
@OverrideVariant(name = "icon-only", strings = ["content=icon"], kitAxis = "Text", kitValue = "No")
@OverrideVariant(name = "text-only", strings = ["content=text"], kitAxis = "Icon", kitValue = "No")
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitAxis = "Style",
  kitValue = "Filled Variant",
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
      "filled-variant" -> ButtonDefaults.filledVariantButtonColors()
      "tonal" -> ButtonDefaults.filledTonalButtonColors()
      "outlined" -> ButtonDefaults.outlinedButtonColors()
      "child" -> ButtonDefaults.childButtonColors()
      else -> ButtonDefaults.buttonColors()
    }
  // The border is its OWN parameter, not part of `colors`. An outlined button built from
  // `outlinedButtonColors()` alone draws no outline, which makes it pixel-identical to the child
  // style — caught by `CatalogRenderTest.no two renders of a component are identical` the moment
  // the child cell existed to collide with. Text-Button carries the same note for the same reason.
  val style = previewOverrideString("style", "filled")
  CompactButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
    border = if (style == "outlined") ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
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

// The kit's `Button-ImageBackground` sets and `Button-Loading`, the three remaining sets on its
// Buttons page.
//
// The image ones call the `Button` overload that takes a container `Painter`, with the scrim
// `ButtonDefaults.containerPainter` applies — the scrim is most of what the style IS, so a sticker
// that skipped it would publish a different component. The image itself is drawn
// (`CatalogImage`) rather than shipped: see its KDoc.
//
// The kit's `Button-ImageBackground-Round` set has no counterpart and stays out: Compose's image
// container painter is on `Button` and `Card`, and `IconButton` takes no painter — so there is no
// round image-backed button to invoke. Design-led means recording that the kit publishes something
// Compose cannot draw, not drawing something else under its name.
//
// `Button-Loading` is the kit publishing a PATTERN rather than a component — Wear Compose has no
// loading button, and an app builds one by putting a progress indicator in a button's icon slot.
// The sticker does exactly that, so both halves are the real named composables rather than a
// replica of either.

@CatalogComponent(
  id = "Button/ImageBackground",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38425:101029",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38425:101028",
  caption = "A button over an image, with the scrim that keeps its label legible.",
)
@CatalogModes
@OverrideVariant(
  name = "secondary-label",
  booleans = ["secondary=true"],
  kitAxis = "Secondary label",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ImageBackgroundButton() = Sticker {
  val c = counted("Playlist")
  Button(
    onClick = c.onClick,
    containerPainter = ButtonDefaults.containerPainter(image = CatalogImage),
    enabled = previewOverrideBoolean("enabled", true),
    colors = ButtonDefaults.buttonWithContainerPainterColors(),
    secondaryLabel =
      if (previewOverrideBoolean("secondary", false)) {
        { Text("12 tracks") }
      } else null,
    label = { Text(c.label) },
  )
}

@CatalogComponent(
  id = "Button/Loading",
  reference = "figma:B24oss2tTeXAFykyeyusz0/68333:155116",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/68333:155055",
  caption = "A button waiting on the work it started — the kit's loading pattern.",
)
@CatalogModes
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
@Composable
fun LoadingButton() = Sticker {
  // TONAL is the base, and that is the kit's call rather than Compose's. `Button-Loading` publishes
  // three styles — Tonal, Outline, Child — and no FILLED one: a filled container behind a progress
  // ring is the one arrangement the kit declined to draw. This sticker defaulted to
  // `buttonColors()` and so fronted the set with a style it does not contain, which is exactly the
  // silent divergence `design-led` exists to catch. The other two published styles ride as cells.
  val colors =
    when (previewOverrideString("style", "tonal")) {
      "outlined" -> ButtonDefaults.outlinedButtonColors()
      "child" -> ButtonDefaults.childButtonColors()
      else -> ButtonDefaults.filledTonalButtonColors()
    }
  Button(
    onClick = {},
    colors = colors,
    // As on the compact button: the outline is a `border`, not a colour, and without it the
    // outlined cell is the child cell's picture under another name.
    border =
      if (previewOverrideString("style", "tonal") == "outlined") {
        ButtonDefaults.outlinedButtonBorder(enabled = true)
      } else null,
    // Pinned, not indeterminate: an animated indicator renders a different frame on every
    // publish, and the delivery branch's history would be noise rather than change.
    icon = { CircularProgressIndicator(progress = { 0.35f }, modifier = Modifier.size(24.dp)) },
    label = { Text("Sending") },
  )
}

// `ButtonGroup` is a Wear Compose component with no kit set: the kit draws button rows as instances
// of its button sets side by side, which is a layout an app makes rather than a component it
// publishes. The library disagrees, and it is the library a reader of this sheet is calling — so it
// enters through the second door (AGENTS.md) with the reason stated.
@CatalogComponent(
  id = "ButtonGroup",
  noReference =
    "The kit publishes no button-group set — it draws rows as side-by-side instances of its " +
      "button sets. This is a Wear Compose component with no kit counterpart.",
  caption = "Buttons that share a row and expand away from whichever one is pressed.",
)
@CatalogModes
@OverrideVariant(name = "three", ints = ["count=3"])
@Composable
fun ButtonRowGroup() = Sticker {
  val count = previewOverrideInt("count", 2)
  ButtonGroup(modifier = Modifier.width(180.dp)) {
    repeat(count) { index ->
      Button(
        onClick = {},
        modifier = Modifier.weight(1f),
        label = { Text(('A' + index).toString()) },
      )
    }
  }
}
