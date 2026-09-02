@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.lambdaAction
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteBoolean
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteCheckboxButton
import androidx.wear.compose.remote.material3.RemoteSplitCheckboxButton
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Toggle+Selection-Buttons` SET, opened on the Remote column for the first time.
//
// 32 published cells that `remote-m3` had drawn NONE of (`docs/KIT_COVERAGE.md` lists it `—`), for
// the plain reason that `remote-material3` published no selection row at all. The snapshot line
// adds them, and this file draws every cell that line can currently be asked for.
//
// WHAT IS REACHABLE, and it is one whole `Type` rather than a slice of three. The library publishes
// `RemoteCheckboxButton` and `RemoteSplitCheckboxButton`, which is exactly the pair `Type=Checkbox`
// needs: `Selected` x `Split (2 tap targets)` x `Disabled` is a clean eight-cell product and all
// eight are drawn here, under the Wear sibling's cell names so the compare page pairs them.
//
// WHAT IS NOT, and why there is no sticker for it:
//
//   * `Type = Switch` and `Type = Radio`. Their SPLIT forms exist — `RemoteSplitSwitchButton` and
//     `RemoteSplitRadioButton` landed alongside `RemoteSplitCheckboxButton` — but the plain
//     `RemoteSwitchButton` and `RemoteRadioButton` do not, and a component drawn only as a split
//     row is the wrong shape to publish: its BASE render would resolve to the kit's `Split=No`
//     node and be compared against a picture it does not draw, which AGENTS.md is explicit is
//     worse than no mapping at all. They are tracked instead, by symbol, in
//     `scripts/remote-snapshot-probe.py` — the probe now names the class each one is waiting on and
//     says the week it appears, which is the mechanism this file's earlier draft did not have.
//   * `Custom - Task`. The kit showing that the control slot is swappable rather than a fourth
//     component, and `RemoteCheckboxButton` exposes no `toggleControl` slot to swap. Same stance
//     the Wear sibling takes for the same cell.
//
// TWO LINES, like the Wear sibling and for the reason it records: every cell the kit publishes in
// this set has a secondary line, so a sticker drawing the primary label alone reports the missing
// line as a difference in all of its cells.
//
// WHAT THE EIGHT CELLS SHOWED, all of it upstream and none of it reachable from this call site:
//
//   * THE TWO FUNCTIONS DISAGREE ABOUT `enabled = false`, which is the sharpest thing on this
//     sheet because it is one knob, one set, two functions. `RemoteSplitCheckboxButton` draws both
//     of its disabled cells — containers, checkbox, the lot. `RemoteCheckboxButton` draws almost
//     nothing: `disabled` is a bare checkmark with no container and no labels, and
//     `unselected-disabled` is fully transparent (recorded in
//     `StickerBakeCoverageTest.knownBlank`). Same argument, same file, opposite behaviour.
//   * BOTH SPLIT DISABLED CELLS LOSE THEIR LABELS while keeping their containers — the same shape
//     as [#91](https://github.com/yschimke/wear-m3-catalog/issues/91) on `RemoteButton`, which is
//     the family this belongs to rather than a new defect.
//   * THE SPLIT ROW TRUNCATES the kit's copy at [KitRowWidth]. The split form spends part of the
//     172dp on its second tap target, so `Primary label` arrives as `Primary la…`. The width is
//     NOT adjusted to hide it: 172dp is what the kit draws a row-shaped control at, both columns
//     use it, and a Remote-only widening would trade a reported difference for an unreported one.

/**
 * **Every cell the kit publishes for one selection `Type`** — `Selected` by
 * `Split (2 tap targets)` by `Disabled`, all eight drawn.
 *
 * A transcription of the Wear sibling's `SelectionCells` (`catalog/src/main/kotlin/…/sections/
 * SelectionButtons.kt`), cell name for cell name, because the axes are arguments to whichever
 * function you picked and the crossings are therefore identical on both columns. Keeping the
 * spellings identical is what lets the compare page set `unselected-split` beside
 * `unselected-split` rather than pairing two names for one cell.
 *
 * A crossing declares its whole assignment with `kitProps` rather than `kitAxis`: a cell that turns
 * two knobs has no single axis to name, and the pair would be dropped rather than guessed at.
 */
@OverrideVariant(
  name = "unselected",
  booleans = ["checked=false"],
  kitAxis = "Selected",
  kitValue = "No",
)
@OverrideVariant(
  name = "split",
  booleans = ["split=true"],
  kitAxis = "Split (2 tap targets)",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "unselected-split",
  booleans = ["checked=false", "split=true"],
  kitProps = ["Selected=No", "Split (2 tap targets)=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "unselected-disabled",
  booleans = ["checked=false", "enabled=false"],
  kitProps = ["Selected=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "split-disabled",
  booleans = ["split=true", "enabled=false"],
  kitProps = ["Split (2 tap targets)=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "unselected-split-disabled",
  booleans = ["checked=false", "split=true", "enabled=false"],
  kitProps = ["Selected=No", "Split (2 tap targets)=Yes", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteSelectionCells

@CatalogComponent(
  id = "CheckboxButton",
  group = "Selection buttons",
  parallel = "CheckboxButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85642",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that toggles a checkbox; for a set where any number may be chosen.",
)
@CatalogRemoteModes
@RemoteSelectionCells
@Composable
fun CheckboxRowRemote() = RemoteSticker {
  // The row OWNS its checked state, as the Wear sibling's does: a selection control that cannot be
  // selected is not the component. `previewOverrideBoolean` picks the state the document is
  // RECORDED with — that is what makes `unselected` a cell — and `valueChange` binds the tap so the
  // recorded document still toggles when a player replays it, with no host round-trip
  // (`InteractiveActionCaptureTest` is why that distinction matters on this sheet).
  val checked = rememberMutableRemoteBoolean(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true).rb
  val toggle = valueChange(checked, !checked)
  if (previewOverrideBoolean("split", false)) {
    RemoteSplitCheckboxButton(
      checked = checked,
      onCheckedChange = toggle,
      toggleContentDescription = KitCopy.PRIMARY_LABEL.rs,
      // The second tap target, and deliberately a `lambdaAction {}` rather than the toggle: the
      // point of the split row is that the container half does something ELSE, and wiring it to
      // `checked` too would draw a two-target row that behaves like a one-target one. This is the
      // Remote spelling of the Wear sibling's `onContainerClick = {}` — a real, distinct target
      // whose handler is empty, because the catalog has no screen to navigate to.
      onContainerClick = lambdaAction {},
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY_LABEL.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY_LABEL.rs) },
    )
  } else {
    RemoteCheckboxButton(
      checked = checked,
      onCheckedChange = toggle,
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY_LABEL.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY_LABEL.rs) },
    )
  }
}
