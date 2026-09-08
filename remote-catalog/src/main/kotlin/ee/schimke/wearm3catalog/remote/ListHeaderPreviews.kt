@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.heightIn
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S TWO LIST HEADERS, on the Remote column for the first time — `Text-ListHeader` and
// `Text-ListHeader-Sub`, seven cells between them, both `—` on this sheet until now.
//
// `remote-material3` publishes NO `RemoteListHeader` and no `RemoteListSubHeader`: its text surface
// is `RemoteText` and `RemoteTypography`, and there is no header container to call. A header is
// what the Wear pair actually IS underneath, though — a row of text at a named type role, a named
// content colour, a minimum height and a content padding — so this draws it from `RemoteText`
// exactly as `androidx.wear.compose.material3.ListHeader` draws it from `Text`, with all four
// numbers taken from the Wear tokens rather than chosen here:
//
//   * `ListHeaderTokens`  — `titleMedium` over `onBackground`, minimum height 48dp.
//   * `ListSubHeaderTokens` — `titleSmall` over `onSurface`, minimum height 48dp.
//   * `ListHeaderDefaults.ContentPadding` — 14dp horizontal, 16dp top, 12dp bottom.
//   * `ListHeaderDefaults.SubHeaderContentPadding` — the same but 8dp at the bottom.
//
// Those four are `internal` upstream (`ListHeaderDefaults` publishes the two `PaddingValues` as
// objects, and the tokens not at all), which is why they are transcribed as literals here and cited
// one by one at the call site. Everything a reader can reach through public API is read rather than
// copied: the type roles come off `RemoteMaterialTheme.typography` and the colours off its
// `colorScheme`, so a theme in the switcher still moves them.
//
// THIS IS THE `Text-Caption` CASE, not the `Level-Indicator` one. Nothing was missing from the
// library: `RemoteText`, the type scale and the layout primitives have all been here since the
// sheet existed, and the set was short because nobody had written the component. Drawing it is the
// honest answer to a blank row; a sentence in `kit-sets.json` would not have been.

/**
 * `ListHeaderTokens.Height` / `ListSubHeaderTokens.Height` — 48dp on both.
 *
 * Applied with `heightIn(min = …)` rather than the `defaultMinSize` the Wear pair uses, because
 * `defaultMinSize` reached `remote-creation-compose` after the released alpha this module's other
 * lane is on and the two lanes have to compile the same file. Nothing else here sets a height, so
 * the two resolve to the same measurement.
 */
private val ListHeaderMinHeight = 48.rdp

/** `ListHeaderDefaults.HorizontalPadding`. */
private val ListHeaderHorizontalPadding = 14.rdp

/** `ListHeaderDefaults.TopPadding`. */
private val ListHeaderTopPadding = 16.rdp

/** `ListHeaderDefaults.HeaderBottomPadding`. */
private val ListHeaderBottomPadding = 12.rdp

/** `ListHeaderDefaults.SubHeaderBottomPadding` — the one number the two headers differ in. */
private val ListSubHeaderBottomPadding = 8.rdp

/**
 * The width both stickers are drawn at, matching the Wear siblings' `Modifier.width(180.dp)`.
 *
 * It is what makes `Alignment` an axis at all — unconstrained text sizes itself to its own glyphs,
 * and `Left` and `Centre` are then the same picture — and handing the two columns the same measure
 * is what keeps the comparison about the header rather than about the frame each catalog chose.
 */
private val ListHeaderWidth = 180.rdp

@CatalogComponent(
  id = "ListHeader",
  group = "Text",
  parallel = "ListHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66978",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66977",
  caption = "Titles the screen a list belongs to; the first thing above the list.",
)
@CatalogRemoteModes
// The kit's other axis, `Type = Page-Top | Page-Mid`, is NOT a cell on either column and cannot be
// one: the two cells differ in the space ABOVE them, which is a property of where the header sits
// rather than of the component — and a component sticker is cropped to what it draws, so that space
// is not in the picture either way. Same absence, same reason, as the `catalog` row states.
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun ListHeaderRemote() = RemoteSticker {
  RemoteBox(
    modifier =
      RemoteModifier.width(ListHeaderWidth)
        .heightIn(min = ListHeaderMinHeight)
        .padding(
          ListHeaderHorizontalPadding,
          ListHeaderTopPadding,
          ListHeaderHorizontalPadding,
          ListHeaderBottomPadding,
        ),
    contentAlignment = RemoteAlignment.Center,
  ) {
    RemoteText(
      KitCopy.TITLE.rs,
      modifier = RemoteModifier.fillMaxWidth(),
      style = RemoteMaterialTheme.typography.titleMedium,
      color = RemoteMaterialTheme.colorScheme.onBackground,
      // The kit's `Alignment` axis, which is not a parameter of the header on either column: it is
      // what the label does with the width it is given. `Centre` is the base cell.
      textAlign = kitTextAlign(),
    )
  }
}

@CatalogComponent(
  id = "ListSubHeader",
  group = "Text",
  parallel = "ListSubHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66983",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66982",
  caption = "Divides a long list into named runs, without leaving the screen's title behind.",
)
@CatalogRemoteModes
// The kit publishes `Icon=Yes` at `Alignment=Left` only — the icon leads the row, so there is
// nothing for a centred variant of it to be — which is why the icon cell names one axis and the
// centred one names the pair. Cell names are the Wear sibling's, so the two columns pair.
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
fun ListSubHeaderRemote() = RemoteSticker {
  val icon = previewOverrideBoolean("icon", false)
  RemoteRow(
    modifier =
      RemoteModifier.width(ListHeaderWidth)
        .heightIn(min = ListHeaderMinHeight)
        .padding(
          ListHeaderHorizontalPadding,
          ListHeaderTopPadding,
          ListHeaderHorizontalPadding,
          ListSubHeaderBottomPadding,
        ),
    verticalAlignment = RemoteAlignment.CenterVertically,
  ) {
    if (icon) {
      // 24dp is what Wear's `Icon` resolves with no size given, which is what the sibling passes
      // here; 6dp is the gap `ListSubHeader` puts between its icon slot and its label. Stated
      // because `RemoteRow` has no arrangement of its own to inherit one from.
      RemoteIcon(
        Icons.Filled.Add,
        contentDescription = null,
        modifier = RemoteModifier.size(24.rdp).padding(0.rdp, 0.rdp, 6.rdp, 0.rdp),
        tint = RemoteMaterialTheme.colorScheme.onSurface,
      )
    }
    RemoteText(
      KitCopy.SUBTITLE.rs,
      modifier = RemoteModifier.fillMaxWidth(),
      style = RemoteMaterialTheme.typography.titleSmall,
      color = RemoteMaterialTheme.colorScheme.onSurface,
      // `left` is the base cell here — a run of list rows starts its text where the rows do — so
      // this knob's default differs from the header's above it.
      textAlign = kitTextAlign(default = "left"),
    )
  }
}

/**
 * The kit's `Alignment` axis as the `textAlign` both header sets take.
 *
 * A `previewOverrideChoice` rather than a text box: the value set is closed, and a control that
 * only shows its current value leaves the alternative reachable only by someone who has read this
 * file. The spelling is the Wear sibling's (`centre`, `left`), because the cells pair by the seed
 * they name as well as by their own.
 */
@Composable
private fun kitTextAlign(default: String = "centre"): TextAlign =
  if (previewOverrideChoice("align", default, listOf("centre", "left")) == "left") TextAlign.Start
  else TextAlign.Center
