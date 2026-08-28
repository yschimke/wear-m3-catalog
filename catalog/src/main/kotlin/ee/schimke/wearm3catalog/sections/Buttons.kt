@file:CatalogGroup(name = "Buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogImage
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

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

/**
 * The leading icon the kit's `Icon=Yes` cells draw, and nothing when they draw `Icon=No`.
 *
 * `iconSize` is the kit's own third axis and only means anything when there IS an icon — the kit
 * spells that dependency as `Icon size=n/a` on every `Icon=No` cell, which is why the sizes are not
 * a matrix against `icon` but a refinement of one of its values.
 */
@Composable
private fun leadingIcon(): (@Composable BoxScope.() -> Unit)? =
  if (!previewOverrideBoolean("icon", false)) null
  else {
    val size =
      when (
        previewOverrideChoice("iconSize", "default", listOf("default", "large", "extra-large"))
      ) {
        "large" -> ButtonDefaults.LargeIconSize
        "extra-large" -> ButtonDefaults.ExtraLargeIconSize
        else -> ButtonDefaults.IconSize
      }
    { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(size)) }
  }

/**
 * The label the kit's `Alignment` axis turns, as a `Button` label slot.
 *
 * The kit's `Alignment=Left | Center` is not a parameter on the Wear Compose function — it is what
 * the label does with the width it is given, so this is where it has to live. `Center` is the base
 * cell and what a label-only button does on its own; `Left` fills the row and starts the text,
 * which is the arrangement every `Icon=Yes` cell is drawn in (an icon and a centred label would
 * leave the text floating between the icon and nothing).
 *
 * Only the five `Button` styles read it, because `Alignment` is only on that set. A component whose
 * kit set does not publish the axis has no business turning it.
 */
@Composable
private fun alignedLabel(text: String) {
  if (previewOverrideChoice("alignment", "center", listOf("center", "left")) == "left") {
    Text(text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
  } else {
    Text(text)
  }
}

/**
 * The eight `Button` cells this catalog did not draw — every combination of the kit's `Icon`, `Icon
 * size` and `Alignment` axes that its set actually publishes, crossed with `Disabled`.
 *
 * Hoisted onto one annotation class and applied to all five styles rather than written out per
 * function: five styles by eight cells is forty near-identical annotations, which is the shape that
 * drifted last time somebody wrote it by hand (see `@PreviewAxis`'s own docs).
 *
 * **Not a `@PreviewAxis` cross product**, deliberately. The product of these axes is 24 cells per
 * style and the kit publishes 10: there is no `Icon=No, Icon size=Lrg 32` node, because `Icon size`
 * only exists once there is an icon. A blind product would mint fourteen renders per style that map
 * to nothing, which is worse than the gap it closed. A ragged matrix is what hand-written cells are
 * for.
 *
 * Every cell declares its WHOLE kit assignment through `kitProps`, because the kit's axes are
 * coupled: there is no `Icon=Yes, Icon size=n/a, Alignment=Center` node either, so a cell naming
 * only `Icon=Yes` asks for a node between the ones the kit drew — which is exactly why the existing
 * single-axis `icon` cell resolves to nothing today and is replaced here.
 */
@OverrideVariant(
  name = "left",
  strings = ["alignment=left"],
  kitProps = ["Icon=No", "Icon size=n/a", "Alignment=Left"],
)
@OverrideVariant(
  name = "icon",
  booleans = ["icon=true"],
  strings = ["alignment=left"],
  kitProps = ["Icon=Yes", "Icon size=26 (Default)", "Alignment=Left"],
)
@OverrideVariant(
  name = "icon-large",
  booleans = ["icon=true"],
  strings = ["iconSize=large", "alignment=left"],
  kitProps = ["Icon=Yes", "Icon size=Lrg 32", "Alignment=Left"],
)
@OverrideVariant(
  name = "icon-extra-large",
  booleans = ["icon=true"],
  strings = ["iconSize=extra-large", "alignment=left"],
  kitProps = ["Icon=Yes", "Icon size=xLg 36", "Alignment=Left"],
)
@OverrideVariant(
  name = "left-disabled",
  booleans = ["enabled=false"],
  strings = ["alignment=left"],
  kitProps = ["Icon=No", "Icon size=n/a", "Alignment=Left", "Disabled=Yes"],
)
@OverrideVariant(
  name = "icon-disabled",
  booleans = ["icon=true", "enabled=false"],
  strings = ["alignment=left"],
  kitProps = ["Icon=Yes", "Icon size=26 (Default)", "Alignment=Left", "Disabled=Yes"],
)
@OverrideVariant(
  name = "icon-large-disabled",
  booleans = ["icon=true", "enabled=false"],
  strings = ["iconSize=large", "alignment=left"],
  kitProps = ["Icon=Yes", "Icon size=Lrg 32", "Alignment=Left", "Disabled=Yes"],
)
@OverrideVariant(
  name = "icon-extra-large-disabled",
  booleans = ["icon=true", "enabled=false"],
  strings = ["iconSize=extra-large", "alignment=left"],
  kitProps = ["Icon=Yes", "Icon size=xLg 36", "Alignment=Left", "Disabled=Yes"],
)
annotation class ButtonLayoutCells

@CatalogComponent(
  id = "Button/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93092",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Highest emphasis; the screen's primary action.",
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
fun FilledButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  Button(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { alignedLabel(c.label) },
  )
}

@CatalogComponent(
  id = "Button/FilledVariant",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39577:895",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "The kit's highlighted style — a filled button in the variant palette.",
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
fun FilledVariantButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  Button(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = ButtonDefaults.filledVariantButtonColors(),
    icon = leadingIcon(),
    label = { alignedLabel(c.label) },
  )
}

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
fun TonalButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  FilledTonalButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { alignedLabel(c.label) },
  )
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
fun OutlineButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  OutlinedButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { alignedLabel(c.label) },
  )
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
fun ChildLabelButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  ChildButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    icon = leadingIcon(),
    label = { alignedLabel(c.label) },
  )
}

/**
 * **Every `Button-Compact` cell Compose can reach and tell apart** — 24 of the set's 40 nodes,
 * against the seven that were drawn: the base plus one cell per axis
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * Six of the sixteen absences are the disabled `Filled Variant` and `Tonal` cells, which Wear draws
 * with the same `onSurface` colours as the disabled filled one — see `EdgeButtonKitCells`. The
 * other ten are the `Alignment=Icon right` column. `CompactButton` has ONE icon slot and it leads;
 * a trailing icon is not something the component takes, and putting a glyph in the label to fake
 * one would publish a picture of this file rather than of the library.
 *
 * Every cell declares its whole kit vector, because this set's axes are coupled three deep:
 * `Alignment`, `Icon` and `Text` are one choice the kit spells as three properties, so
 * `content=icon` alone asks for a node between the ones the kit drew. Which is exactly what the
 * `icon-only` and `text-only` cells did — the projector reported `no counterpart for content=icon`
 * on both, and they were compared against nothing.
 */
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitProps = ["Style=Filled", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
)
@OverrideVariant(
  name = "icon-only",
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
)
@OverrideVariant(
  name = "icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
)
@OverrideVariant(
  name = "text-only",
  strings = ["content=text"],
  kitProps = ["Style=Filled", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["content=text"],
  kitProps = ["Style=Filled", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
)
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled Variant", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "filled-variant-icon-only",
  strings = ["style=filled-variant", "content=icon"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
)
@OverrideVariant(
  name = "filled-variant-text-only",
  strings = ["style=filled-variant", "content=text"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "tonal",
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "tonal-icon-only",
  strings = ["style=tonal", "content=icon"],
  kitProps = ["Style=Tonal", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
)
@OverrideVariant(
  name = "tonal-text-only",
  strings = ["style=tonal", "content=text"],
  kitProps = ["Style=Tonal", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
)
@OverrideVariant(
  name = "outlined-icon-only",
  strings = ["style=outlined", "content=icon"],
  kitProps = ["Style=Outline", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined-icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=icon"],
  kitProps = ["Style=Outline", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
)
@OverrideVariant(
  name = "outlined-text-only",
  strings = ["style=outlined", "content=text"],
  kitProps = ["Style=Outline", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined-text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=text"],
  kitProps = ["Style=Outline", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
)
@OverrideVariant(
  name = "child",
  strings = ["style=child"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "child-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
)
@OverrideVariant(
  name = "child-icon-only",
  strings = ["style=child", "content=icon"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
)
@OverrideVariant(
  name = "child-icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "content=icon"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
)
@OverrideVariant(
  name = "child-text-only",
  strings = ["style=child", "content=text"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
)
@OverrideVariant(
  name = "child-text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "content=text"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
)
annotation class CompactButtonKitCells

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
@CompactButtonKitCells
@Composable
fun CompactActionButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  val content = previewOverrideChoice("content", "icon+text", listOf("icon+text", "icon", "text"))
  // Read ONCE. Declared twice — as it was, for the colours and again for the border — the panel
  // still shows one control, but the duplication is how the two reads drift apart.
  val style =
    previewOverrideChoice(
      "style",
      "filled",
      listOf("filled", "filled-variant", "tonal", "outlined", "child"),
    )
  val colors =
    when (style) {
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
  CompactButton(
    onClick = c.onClick,
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
    border = if (style == "outlined") ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
    icon =
      if (content == "text") null
      else {
        { Icon(Icons.Filled.Add, contentDescription = null) }
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
@OverrideVariant(
  name = "secondary-label-disabled",
  booleans = ["secondary=true", "enabled=false"],
  kitProps = ["Secondary label=Yes", "Disabled=Yes"],
)
@Composable
fun ImageBackgroundButton() = Sticker {
  val c = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
  Button(
    onClick = c.onClick,
    containerPainter = ButtonDefaults.containerPainter(image = CatalogImage),
    enabled = previewOverrideBoolean("enabled", true),
    colors = ButtonDefaults.buttonWithContainerPainterColors(),
    secondaryLabel =
      if (previewOverrideBoolean("secondary", false)) {
        { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)) }
      } else null,
    label = { Text(c.label) },
  )
}

/**
 * **Every cell of the kit's `Button-Loading` set** — three styles by three icon sizes by
 * `Disabled`, 18 nodes, against the three this drew
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * `Icon size` is the kit's axis for the SLOT the ring and the glyph share, so the knob sizes the
 * stack rather than the icon inside it: at `Lrg 32` the kit draws a bigger ring around the same
 * proportioned glyph, not the same ring around a bigger one. The 4dp the ring lives in scales with
 * the slot, which is why the stroke stays where the note below puts it.
 *
 * No `filled` cell, because the kit publishes no filled loading button — see the note in the
 * component.
 */
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "icon-large",
  strings = ["iconSize=large"],
  kitAxis = "Icon size",
  kitValue = "Lrg 32",
)
@OverrideVariant(
  name = "icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["iconSize=large"],
  kitProps = ["Style=Tonal", "Icon size=Lrg 32", "Disabled=Yes"],
)
@OverrideVariant(
  name = "icon-extra-large",
  strings = ["iconSize=extra-large"],
  kitAxis = "Icon size",
  kitValue = "xLg 36",
)
@OverrideVariant(
  name = "icon-extra-large-disabled",
  booleans = ["enabled=false"],
  strings = ["iconSize=extra-large"],
  kitProps = ["Style=Tonal", "Icon size=xLg 36", "Disabled=Yes"],
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
  kitProps = ["Style=Outline", "Icon size=26 (Default)", "Disabled=Yes"],
)
@OverrideVariant(
  name = "outlined-icon-large",
  strings = ["style=outlined", "iconSize=large"],
  kitProps = ["Style=Outline", "Icon size=Lrg 32", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "iconSize=large"],
  kitProps = ["Style=Outline", "Icon size=Lrg 32", "Disabled=Yes"],
)
@OverrideVariant(
  name = "outlined-icon-extra-large",
  strings = ["style=outlined", "iconSize=extra-large"],
  kitProps = ["Style=Outline", "Icon size=xLg 36", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined-icon-extra-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "iconSize=extra-large"],
  kitProps = ["Style=Outline", "Icon size=xLg 36", "Disabled=Yes"],
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
  kitProps = ["Style=Child (No background)", "Icon size=26 (Default)", "Disabled=Yes"],
)
@OverrideVariant(
  name = "child-icon-large",
  strings = ["style=child", "iconSize=large"],
  kitProps = ["Style=Child (No background)", "Icon size=Lrg 32", "Disabled=No"],
)
@OverrideVariant(
  name = "child-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "iconSize=large"],
  kitProps = ["Style=Child (No background)", "Icon size=Lrg 32", "Disabled=Yes"],
)
@OverrideVariant(
  name = "child-icon-extra-large",
  strings = ["style=child", "iconSize=extra-large"],
  kitProps = ["Style=Child (No background)", "Icon size=xLg 36", "Disabled=No"],
)
@OverrideVariant(
  name = "child-icon-extra-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "iconSize=extra-large"],
  kitProps = ["Style=Child (No background)", "Icon size=xLg 36", "Disabled=Yes"],
)
annotation class LoadingButtonKitCells

@CatalogComponent(
  id = "Button/Loading",
  reference = "figma:B24oss2tTeXAFykyeyusz0/68333:155116",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/68333:155055",
  caption = "A button waiting on the work it started — the kit's loading pattern.",
)
@CatalogModes
@LoadingButtonKitCells
@Composable
fun LoadingButton() = Sticker {
  // TONAL is the base, and that is the kit's call rather than Compose's. `Button-Loading` publishes
  // three styles — Tonal, Outline, Child — and no FILLED one: a filled container behind a progress
  // ring is the one arrangement the kit declined to draw. This sticker defaulted to
  // `buttonColors()` and so fronted the set with a style it does not contain, which is exactly the
  // silent divergence `design-led` exists to catch. The other two published styles ride as cells.
  val style = previewOverrideChoice("style", "tonal", listOf("tonal", "outlined", "child"))
  val colors =
    when (style) {
      "outlined" -> ButtonDefaults.outlinedButtonColors()
      "child" -> ButtonDefaults.childButtonColors()
      else -> ButtonDefaults.filledTonalButtonColors()
    }
  Button(
    onClick = {},
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
    // As on the compact button: the outline is a `border`, not a colour, and without it the
    // outlined cell is the child cell's picture under another name.
    border = if (style == "outlined") ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
    // The icon slot is a STACK, because the kit's is: `Button-Loading`'s `Icon` instance holds a
    // `Progress-Indicator-Small` filling all 26dp of the slot and an 18dp icon centred inside the
    // ring, and the pattern is "the thing you asked for, with a ring around it" rather than "a ring
    // where the icon was". This used to be the indicator alone at 24dp, which published a button
    // with no icon AND — since a bare `CircularProgressIndicator` sizes its ring off the slot it is
    // given and drew nothing at that size — no visible ring either: an icon-sized hole in the
    // button, which is what issue #45 is a picture of.
    //
    // All three numbers are the kit's, not guesses: 26dp is the slot ([ButtonDefaults.IconSize] is
    // the same 26), 18dp is the icon centred in it, and the 4dp that separates them is the whole
    // budget the ring has — which is why the stroke is written out rather than taken from
    // `CircularProgressIndicatorDefaults`. Its `smallStrokeWidth` is 6dp, sized for an indicator
    // that is the only thing in its box; spent here it eats 12 of the 26 and closes over the icon
    // the kit put inside. So the ring gets 3dp, inside the kit's own 4dp gap, and the library's
    // constants stay where they fit.
    //
    // Pinned, not indeterminate: an animated indicator renders a different frame on every
    // publish, and the delivery branch's history would be noise rather than change. The value is
    // the kit's picture rather than a round number — its cell draws an almost-closed ring, and at
    // a 26dp diameter a short arc reads as a blob stuck to one side of the icon rather than as
    // progress around it.
    icon = {
      // The kit's `Icon size` axis is the SLOT, and the glyph keeps its proportion inside it: 18
      // of the base cell's 26, so the ring's 4dp gap and its 3dp stroke scale with the slot rather
      // than eating a bigger share of a bigger one.
      val slot =
        when (
          previewOverrideChoice("iconSize", "default", listOf("default", "large", "extra-large"))
        ) {
          "large" -> ButtonDefaults.LargeIconSize
          "extra-large" -> ButtonDefaults.ExtraLargeIconSize
          else -> ButtonDefaults.IconSize
        }
      Box(contentAlignment = Alignment.Center, modifier = Modifier.size(slot)) {
        CircularProgressIndicator(
          progress = { 0.75f },
          modifier = Modifier.fillMaxSize(),
          strokeWidth = slot * (3f / 26f),
        )
        Icon(
          Icons.Filled.Add,
          contentDescription = null,
          modifier = Modifier.size(slot * (18f / 26f)),
        )
      }
    },
    label = { Text(kitCopy("label", KitCopy.PRIMARY_LABEL)) },
    // TWO LINES, because the kit's cell has two. `Button-Loading` is the only button set whose
    // base cell fills the secondary slot — the others leave it empty and put it behind a cell —
    // and a one-line button is a visibly shorter shape, not just shorter words.
    secondaryLabel = { Text(kitCopy("secondaryLabel", KitCopy.SECONDARY_LABEL)) },
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
  // The expansion is the component, and it only exists under a finger — see
  // `Motion.ButtonGroupExpansionMotion` for why no still on this card can carry it.
  motionPreview = "ButtonGroupExpansionMotion",
)
@CatalogModes
@OverrideVariant(name = "three", ints = ["count=3"])
@Composable
fun ButtonRowGroup() = Sticker {
  val count = previewOverrideInt("count", 2)
  ButtonGroup(modifier = Modifier.width(180.dp)) {
    repeat(count) { index ->
      // The expansion the caption promises is opt-in, and this is the opt-in:
      // `ButtonGroupScope.animateWidth` is what makes the pressed button swell by
      // `ButtonGroupDefaults.ExpansionWidth` while its neighbours yield the width. Without it a
      // `ButtonGroup` is only a row that shares its spacing, and the card documented a behaviour
      // the component never had.
      //
      // The interaction source has to be the button's OWN — passed to `Button` and read by
      // `animateWidth` — or the layout is watching a source nothing ever emits a press into.
      val interactionSource = remember { MutableInteractionSource() }
      Button(
        onClick = {},
        modifier = Modifier.weight(1f).animateWidth(interactionSource),
        interactionSource = interactionSource,
        label = { Text(('A' + index).toString()) },
      )
    }
  }
}
