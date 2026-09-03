@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.wear.compose.remote.material3.RemoteEdgeButton
import androidx.wear.compose.remote.material3.RemoteEdgeButtonDefaults
import androidx.wear.compose.remote.material3.RemoteEdgeButtonSize
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Edge-Button` SET, opened on the Remote column for the first time.
//
// 64 published cells that `remote-m3` had drawn NONE of, for the plain reason that
// `remote-material3` published no edge button at all. The snapshot line adds `RemoteEdgeButton`,
// `RemoteEdgeButtonSize` and `RemoteEdgeButtonDefaults`, and that is the whole set's worth of API:
// every one of the four axes the kit crosses is an argument here, so all 64 cells are drawn and
// none is withheld.
//
// IT IS A STATIC COMPONENT ON THIS COLUMN, and that is the one place the two sheets legitimately
// differ. The Wear sibling's note is about a REVEAL: `ScreenScaffold` grows the button out of the
// scroll state, so what an edge button *does* there is not a thing one frame shows, and its
// sticker carries a `motionPreview` pointing at a recording. A RemoteDocument has no scroll host
// and `RemoteEdgeButton` has no reveal to drive — it draws its arc and stops — so this sticker is
// the component, drawn normally, with no motion counterpart to point at. That is a smaller claim
// than the Wear side's, not a gap in this one.
//
// THE FOUR SIZES LINE UP ONE-TO-ONE with the kit, and the mapping is the Wear sibling's because
// `RemoteEdgeButtonSize` publishes the same four names as `EdgeButtonSize`: the kit's `Small` is
// `ExtraSmall`, its `Default` is `Small`, its `Large` is `Medium` and its `Extra-Large` is `Large`.
// Reading the two lists off in parallel instead puts every size one step too big — the trap the
// Wear file records — so the cells below keep the library's names and name the kit's spelling with
// `kitProps`, exactly as that file does.
//
// STYLE FOLDS rather than splitting, on the same call-site test AGENTS.md sets: `remote-material3`
// publishes one `RemoteEdgeButton` taking its emphasis as `colors`, so there is no second function
// to choose and no second card to stand up. It folds on the Wear column for the identical reason.
//
// THE OUTLINED STYLE IS THE ONE PLACE THIS CALL SITE ADDS ANYTHING, and it is the same treatment
// `RemoteKitButton` gives `Button/Outlined`: `RemoteEdgeButtonDefaults.outlinedButtonColors()`
// resolves the transparent container and the `onSurface` content, but the border is a separate
// `border`/`borderColor` pair on the component rather than part of its colours, so it is passed
// here at the theme's `outline` token. The three filled styles pass a zero-width border, which is
// a no-op whatever colour rides with it.

// WHAT THE 64 CELLS SHOWED, and it is one finding rather than a list: **`RemoteEdgeButton` does not
// clip its content to its arc.** The kit draws this component with a truncated label — that is why
// [KitCopy.EDGE_BUTTON_LABEL] is an M-run long enough to truncate, on both columns — and Wear's
// `EdgeButton` reproduces it, constraining the label to the arc and ellipsizing what will not fit.
// The Remote one lays its content out to the width the button is GIVEN and draws it wherever it
// lands, so the label spills past the pill on both sides in every `Type=Text` cell.
//
// IT IS NOT REACHABLE FROM HERE, which is the part worth being exact about, because there is an
// obvious-looking fix that would be wrong twice over. `RemoteText` takes `maxLines`, and
// `RemoteEdgeButtonSize` computes the right number — but `maxLines$remote_material3()` is INTERNAL,
// so a call site can only hardcode it, which is the invented-number-under-the-kit's-name that
// `CircularProgressRemote`'s stroke note refuses on this very sheet. And it would not work anyway:
// the overflow is HORIZONTAL as well as vertical (the `extra-small` cell spills on one line), and
// the arc's inner width is published nowhere at all. So the divergence is stated rather than
// papered over, per AGENTS.md.
//
// THE WIDTH IS NOT ADJUSTED TO HIDE IT, and the measurement says why. Dropping the width modifier
// does not help, it hurts: with no bound the label lays out across the whole 227dp frame — the
// `extra-small` cell measured 193dp of text against a 117dp pill, ellipsis and all, entirely
// outside the container. Narrowing below the kit's 192dp would shrink the spill by drawing an arc
// the kit never published, which is the trade `SelectionPreviews.kt` names for the split row: a
// reported difference swapped for an unreported one. So the button is handed the display width the
// kit measures it against and the spill is left visible for design-parity to score.

/**
 * **Every cell of the kit's `Edge-Button` set** — all 64 nodes, drawn on the Remote column.
 *
 * A transcription of the Wear sibling's `EdgeButtonKitCells`
 * (`catalog/src/main/kotlin/…/sections/EdgeButtons.kt`), cell name for cell name and `kitProps` for
 * `kitProps`, because all four axes are arguments to whichever function you picked and the
 * crossings are therefore identical on both columns. Keeping the spellings identical is what lets
 * the compare page set `tonal-icon-large` beside `tonal-icon-large` rather than pairing two names
 * for one cell.
 *
 * **Written out rather than declared as a `@PreviewAxis` product**, for the reason the Wear file
 * gives at length: three of the four axes are spelled differently on the two sides — `outlined` is
 * the kit's `Outline`, `enabled=false` is its `Disabled=Yes`, and the library's `Small` is the
 * kit's `Default`, one step off the whole way down. A product resolved through the alias tables
 * would pair `size=medium` with `Size=Large`'s neighbour and report the miss as a design
 * divergence. `kitProps` states each cell's WHOLE kit assignment instead, so every cell either
 * lands on the node the kit drew or resolves to nothing loudly.
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
  name = "filled-variant-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=extra-small"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Default", "Disabled=No"],
)
@OverrideVariant(
  name = "filled-variant-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-medium",
  strings = ["style=filled-variant", "size=medium"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=medium"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large",
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled Variant", "Type=Text", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-extra-small",
  strings = ["style=filled-variant", "content=icon", "size=extra-small"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "content=icon", "size=extra-small"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon",
  strings = ["style=filled-variant", "content=icon"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Default", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "content=icon"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-medium",
  strings = ["style=filled-variant", "content=icon", "size=medium"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "content=icon", "size=medium"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-large",
  strings = ["style=filled-variant", "content=icon", "size=large"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "content=icon", "size=large"],
  kitProps = ["Style=Filled Variant", "Type=Icon", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-extra-small",
  strings = ["style=tonal", "size=extra-small"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=extra-small"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal",
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Default", "Disabled=No"],
)
@OverrideVariant(
  name = "tonal-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-medium",
  strings = ["style=tonal", "size=medium"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=medium"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large",
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Type=Text", "Size=Extra-Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-extra-small",
  strings = ["style=tonal", "content=icon", "size=extra-small"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "content=icon", "size=extra-small"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon",
  strings = ["style=tonal", "content=icon"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Default", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "content=icon"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-medium",
  strings = ["style=tonal", "content=icon", "size=medium"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-medium-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "content=icon", "size=medium"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-large",
  strings = ["style=tonal", "content=icon", "size=large"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Extra-Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "content=icon", "size=large"],
  kitProps = ["Style=Tonal", "Type=Icon", "Size=Extra-Large", "Disabled=Yes"],
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
annotation class RemoteEdgeButtonKitCells

@CatalogComponent(
  id = "EdgeButton",
  group = "Edge-hugging buttons",
  parallel = "EdgeButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/36601:6587",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/36601:6586",
  caption =
    "The screen-hugging confirm action, curved to the bottom edge of the display. Remote draws " +
      "the label unclipped, so it spills past the arc where the kit truncates it — see the file note.",
)
@CatalogRemoteModes
@RemoteEdgeButtonKitCells
@Composable
fun EdgeButtonRemote() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.EDGE_BUTTON_LABEL)
  // The kit's `Style` axis. Four calls into `RemoteEdgeButtonDefaults`, which publishes one
  // factory per style — so unlike `Button/Outlined` on this sheet, the outlined colours are the
  // library's own rather than a transparent container this catalog writes out. Only the BORDER is
  // the call site's, because it is a separate parameter rather than part of the colours.
  val style =
    previewOverrideChoice(
      "style",
      "filled",
      listOf("filled", "filled-variant", "tonal", "outlined"),
    )
  val colors =
    when (style) {
      "filled-variant" -> RemoteEdgeButtonDefaults.filledVariantButtonColors()
      "tonal" -> RemoteEdgeButtonDefaults.filledTonalButtonColors()
      "outlined" -> RemoteEdgeButtonDefaults.outlinedButtonColors()
      else -> RemoteEdgeButtonDefaults.buttonColors()
    }
  // Four values, one per kit cell, and the default is the library's `Small` because that is what
  // the kit calls `Size=Default` — see the note above.
  val size =
    when (
      previewOverrideChoice("size", "small", listOf("extra-small", "small", "medium", "large"))
    ) {
      "extra-small" -> RemoteEdgeButtonSize.ExtraSmall
      "medium" -> RemoteEdgeButtonSize.Medium
      "large" -> RemoteEdgeButtonSize.Large
      else -> RemoteEdgeButtonSize.Small
    }
  RemoteEdgeButton(
    onClick = onClick,
    // The kit measures every cell of this set against the 192dp display, because an edge button IS
    // an arc struck at the width it is given: hand it the frame's 227dp and the curve flattens,
    // and the row then compares a shallower button against the kit's. [KitDisplayWidth] is that
    // number, and it is the counterpart of [KitRowWidth] one step out — the display rather than
    // the content column.
    modifier = RemoteModifier.width(KitDisplayWidth),
    buttonSize = size,
    enabled = previewOverrideBoolean("enabled", true).rb,
    colors = colors,
    // Zero on the three filled styles, and a no-op there whatever colour rides with it.
    border = if (style == "outlined") 2.rdp else 0.rdp,
    borderColor =
      if (style == "outlined") RemoteMaterialTheme.colorScheme.outline
      else RemoteColor(Color.Transparent),
  ) {
    // The kit's `Type` axis. The glyph is the Wear sibling's — a check, the confirm action this
    // component exists for — hand-built rather than taken from `material-icons-core`, which this
    // module does not depend on (see `addIcon`, the same story one file over).
    if (previewOverrideChoice("content", "text", listOf("text", "icon")) == "icon") {
      RemoteIcon(checkIcon, contentDescription = "Done".rs)
    } else {
      RemoteText(label)
    }
  }
}

/**
 * Material's `Check`, hand-built.
 *
 * The Wear sibling draws `Icons.Filled.Check` in this set's `Type=Icon` cells; this module has no
 * `material-icons-core` on its classpath, so the one path is transcribed rather than depended on —
 * the same bargain [addIcon] strikes for the button set's leading icon.
 */
internal val checkIcon: ImageVector =
  ImageVector.Builder(
      name = "Check",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .apply {
      path(fill = SolidColor(Color.White)) {
        moveTo(9f, 16.17f)
        lineTo(4.83f, 12f)
        lineTo(3.41f, 13.41f)
        lineTo(9f, 19f)
        lineTo(21f, 7f)
        lineTo(19.59f, 5.59f)
        close()
      }
    }
    .build()
