@file:CatalogGroup(name = "Edge-hugging buttons", section = "Actions")

package ee.schimke.wearm3catalog.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.EdgeButtonSticker
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Edge-Button` set.
//
// IT IS A COMPONENT CELL, NOT A SCREEN. Every one of the set's cells is 192 wide and 49/59/73/99
// tall: the button alone, laid out against the 192dp screen so its arc is the display's, with the
// kit's 3dp of floor under it. It is *not* one of the kit's 192×192 display cells, so this renders
// through `EdgeButtonSticker` rather than a round watch face. Publishing the screen instead is
// issue #31: the comparison squashed a whole watch — list, time text, scroll indicator — into a
// 192×59 cell and reported all of it as a difference from a button. The screen is still shown; it
// is a recording in `Motion.kt`, which is where a scroll-driven reveal belongs.
//
// THE FOUR SIZES LINE UP ONE-TO-ONE, which is not what this file used to say. The kit's cells are
// exactly `EdgeButtonSize` plus the 3dp floor — 49=46+3, 59=56+3, 73=70+3, 99=96+3 — so the kit's
// `Small` is Compose's `ExtraSmall`, its `Default` is `Small`, its `Large` is `Medium` and its
// `Extra-Large` is `Large`. Reading the two lists off in parallel instead put every size one step
// too big and invented two gaps that are not there (a kit `Extra-Large` with no counterpart, and a
// Compose `ExtraSmall` the kit never published). The cells below keep Compose's names and name the
// kit's spelling with `kitProps`.
//
// Style folds rather than splitting — Compose ships one `EdgeButton` taking its emphasis as
// `colors`, so there is no second function to choose at the call site (AGENTS.md).

/**
 * **Every cell of the kit's `Edge-Button` set Compose can tell apart** — 48 of its 64 nodes. The
 * set is a clean product (four `Style` values by two `Type` values by four `Size` values by
 * `Disabled`) and the catalog drew nine of it, the base plus one cell per axis, so 55 published
 * nodes were compared against nothing
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * The 16 that stay out are the DISABLED cells of `Filled Variant` and `Tonal`. Wear Compose
 * resolves all three filled styles' disabled colours to the same `onSurface` pair — 12% container,
 * 38% content — so a disabled variant, tonal and filled button are one picture, and the three cells
 * the kit draws for them cannot be told apart at the call site. Publishing them anyway is three
 * names for one render, which `CatalogRenderTest.no two renders of a component are identical`
 * rejects, and rightly: a comparison that cannot fail is not a comparison. The kit drawing a
 * distinction the library does not is a finding about the pair, recorded here (AGENTS.md), and the
 * `Outline` disabled cells DO differ, so they are drawn.
 *
 * Hoisted onto an annotation class rather than stacked on the composable so the component's own
 * declaration still reads as one screen of code.
 *
 * **Written out rather than declared as a `@PreviewAxis` product**, even though the product is
 * exactly what this is, because three of the four axes are spelled differently on the two sides and
 * an axis cell carries no kit handle: `outlined` is the kit's `Outline`, `enabled=false` is its
 * `Disabled=Yes`, and — the trap this file already carries a note about — Compose's `Small` is the
 * kit's `Default`, one step off the whole way down. A product resolved through the alias tables
 * would pair `size=medium` with `Size=Large`'s neighbour and report the miss as a design
 * divergence. `kitProps` states each cell's WHOLE kit assignment instead, the same way
 * `ButtonLayoutCells` does, so every cell either lands on the node the kit drew or resolves to
 * nothing loudly.
 *
 * Names are the cell's non-default values joined by `-`, in `Style`, `Type`, `Size`, `Disabled`
 * order, so the nine renders that already existed keep the ids they published (`icon`, `tonal`,
 * `extra-small`, `disabled`, …) and the new ones read as their crossings (`tonal-icon-large`).
 */
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Small", "Disabled=No"],
)
@OverrideVariant(
  name = "extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=extra-small"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Default", "Disabled=Yes"],
)
@OverrideVariant(
  name = "medium",
  strings = ["size=medium"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Large", "Disabled=No"],
)
@OverrideVariant(
  name = "medium-disabled",
  booleans = ["enabled=false"],
  strings = ["size=medium"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "large",
  strings = ["size=large"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Extra-Large", "Disabled=No"],
)
@OverrideVariant(
  name = "large-disabled",
  booleans = ["enabled=false"],
  strings = ["size=large"],
  kitProps = ["Style=Filled", "Type=Text", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-extra-small",
  strings = ["content=icon", "size=extra-small"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon", "size=extra-small"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon",
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Default", "Disabled=No"],
)
@OverrideVariant(
  name = "icon-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-medium",
  strings = ["content=icon", "size=medium"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon", "size=medium"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-large",
  strings = ["content=icon", "size=large"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon", "size=large"],
  kitProps = ["Style=Filled", "Type=Icon", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-extra-small",
  strings = ["style=filled-variant", "size=extra-small"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Default", "Disabled=No"],
)
@OverrideVariant(
  name = "filled-variant-medium",
  strings = ["style=filled-variant", "size=medium"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large",
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-extra-small",
  strings = ["style=filled-variant", "content=icon", "size=extra-small"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon",
  strings = ["style=filled-variant", "content=icon"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Default", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-medium",
  strings = ["style=filled-variant", "content=icon", "size=medium"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-large",
  strings = ["style=filled-variant", "content=icon", "size=large"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-extra-small",
  strings = ["style=tonal", "size=extra-small"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal",
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Default", "Disabled=No"],
)
@OverrideVariant(
  name = "tonal-medium",
  strings = ["style=tonal", "size=medium"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large",
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-extra-small",
  strings = ["style=tonal", "content=icon", "size=extra-small"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon",
  strings = ["style=tonal", "content=icon"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Default", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-medium",
  strings = ["style=tonal", "content=icon", "size=medium"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-large",
  strings = ["style=tonal", "content=icon", "size=large"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-extra-small",
  strings = ["style=outlined", "size=extra-small"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=extra-small"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Default", "Disabled=No"],
)
@OverrideVariant(
  name = "outlined-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-medium",
  strings = ["style=outlined", "size=medium"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=medium"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-large",
  strings = ["style=outlined", "size=large"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=large"],
  kitProps = ["Style=Outline", "Type=Text", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-extra-small",
  strings = ["style=outlined", "content=icon", "size=extra-small"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=icon", "size=extra-small"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon",
  strings = ["style=outlined", "content=icon"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Default", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=icon"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-medium",
  strings = ["style=outlined", "content=icon", "size=medium"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=icon", "size=medium"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-large",
  strings = ["style=outlined", "content=icon", "size=large"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=icon", "size=large"],
  kitProps = ["Style=Outline", "Type=Icon", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
annotation class EdgeButtonKitCells

@CatalogComponent(
  id = "EdgeButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/36601:6587",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/36601:6586",
  caption = "The screen-hugging confirm action, curved to the bottom edge of the display.",
  // The reveal-on-scroll, recorded in Motion.kt on the full 192dp screen. `ScreenScaffold` reveals
  // the button from the scroll state, so what an edge button *does* is not a thing one frame shows
  // — while this sticker stays the kit's 192×59 component cell (#31).
  motionPreview = "EdgeButtonRevealMotion",
)
@CatalogModes
@EdgeButtonKitCells
@Composable
fun ScreenEdgeButton() = EdgeButtonSticker {
  val c = counted(kitCopy("label", KitCopy.EDGE_BUTTON_LABEL))
  val colors =
    when (
      previewOverrideChoice(
        "style",
        "filled",
        listOf("filled", "filled-variant", "tonal", "outlined"),
      )
    ) {
      "filled-variant" -> ButtonDefaults.filledVariantButtonColors()
      "tonal" -> ButtonDefaults.filledTonalButtonColors()
      "outlined" -> ButtonDefaults.outlinedButtonColors()
      else -> ButtonDefaults.buttonColors()
    }
  // Four values, one per kit cell, and the default is Compose's `Small` because that is what the
  // kit calls `Size=Default` — see the note above.
  val size =
    when (
      previewOverrideChoice("size", "small", listOf("extra-small", "small", "medium", "large"))
    ) {
      "extra-small" -> EdgeButtonSize.ExtraSmall
      "medium" -> EdgeButtonSize.Medium
      "large" -> EdgeButtonSize.Large
      else -> EdgeButtonSize.Small
    }
  EdgeButton(
    onClick = c.onClick,
    buttonSize = size,
    enabled = previewOverrideBoolean("enabled", true),
    colors = colors,
  ) {
    if (previewOverrideChoice("content", "text", listOf("text", "icon")) == "icon") {
      Icon(Icons.Filled.Check, contentDescription = "Done")
    } else {
      Text(c.label)
    }
  }
}
