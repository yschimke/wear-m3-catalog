@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteBoolean
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteCheckboxButton
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Toggle+Selection-Buttons` SET, opened on the Remote column for the first time.
//
// This set is 32 published cells that `remote-m3` has drawn NONE of (docs/KIT_COVERAGE.md lists it
// `—`), for the plain reason that `remote-material3` published no selection row at all. The
// snapshot line adds `RemoteCheckboxButton`, so one of the set's four `Type`s becomes drawable —
// and only one. This file is that one, and the count below is what it honestly reaches.
//
// WHAT IS STILL MISSING, and why there is no sticker for it here:
//
//   * `Type = Switch` and `Type = Radio`. The snapshot ships `RemoteSelectionButtonImpl` and
//     `RemoteCheckboxControl` — the shared row and the checkbox glyph — but both are `internal`,
//     and no `RemoteSwitchButton` / `RemoteRadioButton` is published on top of them. The scaffolding
//     for all three types is there; two of the three public entry points are not. Drawing them
//     would mean reimplementing the control, which is a component invented under the kit's name.
//   * `Split (2 tap targets) = Yes`, on every type. There is no `RemoteSplitCheckboxButton`, and
//     the split row is not a `RemoteCheckboxButton` argument — it is a different arrangement with
//     two independent click targets. So the four split cells of this type are unreachable too.
//
// That leaves the `Selected` × `Disabled` quarter of one type: four cells of the set's 32. It is
// worth drawing anyway — those four are the ones every other selection cell is a variation of, and
// a set at 4/32 is a measured gap where `—` was an unmeasured one.
//
// TWO LINES, like the Wear sibling and for the same reason it records: every cell the kit publishes
// in this set has a secondary line, so a sticker drawing the primary label alone reports the
// missing line as a difference in all of its cells.

/**
 * The `Selected` × `Disabled` cells of one selection `Type` — the product this lane can reach.
 *
 * The Wear sibling's `SelectionCells` crosses three axes for eight cells; this is the same
 * annotation with the `Split (2 tap targets)` axis removed, because the composable that draws it
 * does not exist on the Remote column. Cell NAMES are deliberately the sibling's, so the compare
 * page sets `unselected` beside `unselected` rather than pairing two spellings of one cell.
 */
@OverrideVariant(
  name = "unselected",
  booleans = ["checked=false"],
  kitAxis = "Selected",
  kitValue = "No",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "unselected-disabled",
  booleans = ["checked=false", "enabled=false"],
  kitProps = ["Selected=No", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteSelectionCells

@CatalogComponent(
  id = "CheckboxButton",
  group = "Selection buttons",
  parallel = "CheckboxButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85642",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "RemoteCheckboxButton: a labelled row that toggles a checkbox, for a set where any number may be chosen.",
)
@CatalogRemoteModes
@RemoteSelectionCells
@Composable
fun CheckboxRowRemote() = RemoteSticker {
  // The row OWNS its checked state, as the Wear sibling's does: a selection control that cannot be
  // selected is not the component. `previewOverrideBoolean` picks the state the document is
  // RECORDED with — that is what makes `unselected` a cell — and `valueChange` binds the tap so the
  // recorded document still toggles when a player replays it.
  val checked = rememberMutableRemoteBoolean(previewOverrideBoolean("checked", true))
  RemoteCheckboxButton(
    checked = checked,
    onCheckedChange = valueChange(checked, !checked),
    modifier = RemoteModifier.width(KitRowWidth),
    enabled = previewOverrideBoolean("enabled", true).rb,
    label = { RemoteText(KitCopy.PRIMARY_LABEL.rs) },
    secondaryLabel = { RemoteText(KitCopy.SECONDARY_LABEL.rs) },
  )
}
