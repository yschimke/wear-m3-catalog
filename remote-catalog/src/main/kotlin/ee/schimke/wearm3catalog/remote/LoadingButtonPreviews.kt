@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Button-Loading` SET, on the Remote column for the first time — 18 cells against the
// nothing this sheet drew: `docs/KIT_COVERAGE.md` had the whole set as `—` here while the
// Wear column drew all eighteen.
//
// `Button-Loading` IS THE KIT PUBLISHING A PATTERN, NOT A COMPONENT. Neither library has a loading
// button: `androidx.wear.compose.material3` has none and neither does `remote-material3` — the
// artifact carries `RemoteButton`, `RemoteCompactButton`, `RemoteIconButton`, `RemoteTextButton`
// and `RemoteEdgeButton` and no loading variant of any of them. An app builds one by putting a
// progress indicator in a button's icon slot, and that is exactly what this sticker does: both
// halves are real named `remote-material3` composables — `RemoteButton` and
// `RemoteCircularProgressIndicator` — rather than a replica of either. The Wear sibling's
// `LoadingButton` is the same two calls one column over, which is what makes the row comparable.
//
// EVERY NUMBER HERE IS THE WEAR SIBLING'S, and that is deliberate: this row is read beside
// `Button/Loading` on the compare page, so a slot size or a stroke chosen here rather than
// transcribed would be reported as a difference between the two libraries when it is a difference
// between two catalogs. The Wear file states where each comes from; repeated in short:
//
//   * 26dp is the icon SLOT (`ButtonDefaults.IconSize`, and `RemoteButtonDefaults.IconSize` is the
//     same 26), and the kit's `Icon size` axis sizes that slot rather than the glyph in it.
//   * 18dp of that 26 is the glyph, centred in the ring — the kit's `Icon` instance is a
//     `Progress-Indicator-Small` filling the slot with an icon inside it, not a ring where the
//     icon was. Written as a RATIO of the slot so the proportion survives the other two sizes.
//   * 3dp of the remaining 4dp is the ring's stroke. `CircularProgressIndicatorDefaults`'
//     `smallStrokeWidth` is 6dp — sized for an indicator that is the only thing in its box — and
//     spent here it closes over the icon the kit put inside, which is why the number is written
//     out on both columns rather than taken from the defaults.
//
// TONAL IS THE BASE, and that is the kit's call rather than either library's. `Button-Loading`
// publishes three styles — `Tonal`, `Outline`, `Child (No background)` — and no FILLED one: a
// filled container behind a progress ring is the one arrangement the kit declined to draw. The
// other two ride as cells.
//
// `xLg 36` IS A LITERAL, and the only one in this file. `RemoteButtonDefaults` publishes
// `ExtraSmallIconSize`, `SmallIconSize`, `IconSize` and `LargeIconSize` and stops there — the alpha
// `remote-material3` surface has no extra-large token — so the kit's third size is reached by
// naming the kit's own 36dp rather than left undrawn. `RemoteKitButton` in CatalogPreviews.kt makes
// the same call for the same column of the `Button` set; if the library gains the token, both
// become `ExtraLargeIconSize` and nothing else moves.
//
// PINNED, NOT INDETERMINATE. An animated ring renders a different frame on every publish and turns
// the delivery branch's history into noise. 0.75 is the Wear sibling's value and the kit's picture:
// its cell draws an almost-closed ring, and at a 26dp diameter a short arc reads as a blob stuck to
// one side of the icon rather than as progress around it.

/**
 * **Every cell of the kit's `Button-Loading` set** — three styles by three icon sizes by
 * `Disabled`, 18 nodes.
 *
 * The names are the Wear sibling's, cell for cell, because the compare page reads the two columns
 * cell by cell as well as card by card: the three knobs they seed — `style`, `iconSize`, `enabled`
 * — are spelled identically on both, so identical cell names pair rather than translate.
 *
 * No `filled` cell, because the kit publishes no filled loading button — see the note above.
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
  secondary = true,
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
  kitProps = ["Style=Outline", "Icon size=26 (Default)", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-large",
  strings = ["style=outlined", "iconSize=large"],
  kitProps = ["Style=Outline", "Icon size=Lrg 32", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "iconSize=large"],
  kitProps = ["Style=Outline", "Icon size=Lrg 32", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-extra-large",
  strings = ["style=outlined", "iconSize=extra-large"],
  kitProps = ["Style=Outline", "Icon size=xLg 36", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-extra-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "iconSize=extra-large"],
  kitProps = ["Style=Outline", "Icon size=xLg 36", "Disabled=Yes"],
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
  kitProps = ["Style=Child (No background)", "Icon size=26 (Default)", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-icon-large",
  strings = ["style=child", "iconSize=large"],
  kitProps = ["Style=Child (No background)", "Icon size=Lrg 32", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-icon-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "iconSize=large"],
  kitProps = ["Style=Child (No background)", "Icon size=Lrg 32", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-icon-extra-large",
  strings = ["style=child", "iconSize=extra-large"],
  kitProps = ["Style=Child (No background)", "Icon size=xLg 36", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-icon-extra-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "iconSize=extra-large"],
  kitProps = ["Style=Child (No background)", "Icon size=xLg 36", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteLoadingButtonKitCells

@CatalogComponent(
  id = "Button/Loading",
  group = "Buttons",
  parallel = "Button/Loading",
  reference = "figma:B24oss2tTeXAFykyeyusz0/68333:155116",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/68333:155055",
  caption = "A button waiting on the work it started — the kit's loading pattern.",
)
@CatalogRemoteModes
@RemoteLoadingButtonKitCells
@Composable
fun LoadingRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  val enabled = previewOverrideBoolean("enabled", true).rb
  // The kit's `Style` axis. `remote-material3` publishes one `RemoteButton` taking its emphasis as
  // colours, so all three values are arguments to one call — which is why they are cells here
  // rather than three components, and why the outline is a `border` rather than a colour.
  val style = previewOverrideChoice("style", "tonal", listOf("tonal", "outlined", "child"))
  // The kit's `Icon size` axis is the SLOT the ring and the glyph share: at `Lrg 32` the kit draws
  // a bigger ring around the same proportioned glyph, not the same ring around a bigger one.
  val slot =
    when (previewOverrideChoice("iconSize", "default", listOf("default", "large", "extra-large"))) {
      "large" -> RemoteButtonDefaults.LargeIconSize
      "extra-large" -> 36.rdp
      else -> RemoteButtonDefaults.IconSize
    }
  RemoteButton(
    onClick = onClick,
    // The kit draws this button across its content column, and the Wear sibling pins the same
    // 172dp with `Modifier.kitRowWidth()` — handed different widths the two columns would be
    // compared at two scales, because design-parity rasterises the reference to the candidate's
    // width. No `buttonSizeModifier()` with it: that pins the one-LINE height, and this cell has
    // two lines plus an icon, so it sizes from its slots (the same branch `RemoteKitButton` takes
    // for the `Button` set's `Icon=Yes` column).
    modifier = RemoteModifier.width(KitRowWidth),
    enabled = enabled,
    colors =
      when (style) {
        "outlined" -> remoteOutlinedButtonColors()
        "child" -> remoteChildButtonColors()
        else -> remoteTonalButtonColors()
      },
    // As on every other outlined cell of this sheet: the outline is a `border`, not a colour
    // (`RemoteButtonColors` has no stroke field), and without it the outlined cell is the child
    // cell's picture under another name. [KitOutlinedBorderWidth] is the 1dp Wear resolves.
    border = if (style == "outlined") KitOutlinedBorderWidth else 0.rdp,
    borderColor =
      if (style == "outlined") RemoteMaterialTheme.colorScheme.outline
      else RemoteColor(Color.Transparent),
    // THE ICON SLOT IS A STACK, because the kit's is — see the file note for all three numbers.
    icon = {
      RemoteBox(modifier = RemoteModifier.size(slot), contentAlignment = RemoteAlignment.Center) {
        RemoteCircularProgressIndicator(
          // A `progress` knob so a reader can move the ring, seeded at the kit's own
          // almost-closed arc. Read at composition rather than published as a named value: the
          // ring is one slot of a button rather than the whole sticker, and the cells above turn
          // the style and the size, not this.
          progress = previewOverrideFloat("progress", 0.75f).rf,
          modifier = RemoteModifier.fillMaxSize(),
          enabled = enabled,
          strokeWidth = slot * (3f / 26f),
        )
        RemoteIcon(
          Icons.Filled.Add,
          contentDescription = null,
          modifier = RemoteModifier.size(slot * (18f / 26f)),
        )
      }
    },
    label = { RemoteText(label) },
    // TWO LINES, because the kit's cell has two. `Button-Loading` is the only button set whose base
    // cell fills the secondary slot — the others leave it empty and put it behind a cell — and a
    // one-line button is a visibly shorter shape, not just shorter words.
    secondaryLabel = { RemoteText(KitCopy.SECONDARY_LABEL.rs) },
  )
}
