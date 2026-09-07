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
import androidx.wear.compose.remote.material3.RemoteRadioButton
import androidx.wear.compose.remote.material3.RemoteSplitCheckboxButton
import androidx.wear.compose.remote.material3.RemoteSplitRadioButton
import androidx.wear.compose.remote.material3.RemoteSplitSwitchButton
import androidx.wear.compose.remote.material3.RemoteSwitchButton
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Toggle+Selection-Buttons` SET, on the Remote column.
//
// 32 published cells that `remote-m3` had drawn NONE of (`docs/KIT_COVERAGE.md` lists it `—`), for
// the plain reason that `remote-material3` published no selection row at all. The snapshot line
// adds them, and this file draws every cell that line can currently be asked for.
//
// WHAT IS REACHABLE, and it is now THREE whole `Type`s rather than one. `RemoteCheckboxButton`
// arrived first and this file opened with `Type=Checkbox` alone; `RemoteSwitchButtonKt` and
// `RemoteRadioButtonKt` appeared in androidx.dev build 16280882, which is the week the two
// `AWAITED_API` entries in `scripts/remote-snapshot-probe.py` were watching for and the week they
// were retired. Each `Type` is its own pair — plain plus split — so `Selected` x
// `Split (2 tap targets)` x `Disabled` is a clean eight-cell product per type and all 24 are drawn
// here, under the Wear sibling's cell names so the compare page pairs them.
//
// WHY THE PLAIN ROW WAS THE ONE BEING WAITED ON. All three SPLIT forms shipped together; the plain
// `RemoteSwitchButton` and `RemoteRadioButton` did not, and a component drawn only as a split row
// is the wrong shape to publish — its BASE render would resolve to the kit's `Split=No` node and be
// compared against a picture it does not draw, which AGENTS.md is explicit is worse than no mapping
// at all. That is why the watch named `RemoteSwitchButtonKt` and `RemoteRadioButtonKt` and not the
// split classes that were already there.
//
// WHAT IS STILL NOT REACHABLE, and why there is no sticker for it:
//
//   * `Custom - Task`. The kit showing that the control slot is swappable rather than a fourth
//     component, and none of the three plain rows exposes a `toggleControl` slot to swap. Same
//     stance the Wear sibling takes for the same cell.
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
 * Hoisted onto one annotation class and applied to all three types rather than written out three
 * times, exactly as the Wear sibling does it: the cells are identical for `Checkbox`, `Switch` and
 * `Radio`, and `Type` is carried by each component's own `reference`.
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
      toggleContentDescription = KitCopy.PRIMARY.rs,
      // The second tap target, and deliberately a `lambdaAction {}` rather than the toggle: the
      // point of the split row is that the container half does something ELSE, and wiring it to
      // `checked` too would draw a two-target row that behaves like a one-target one. This is the
      // Remote spelling of the Wear sibling's `onContainerClick = {}` — a real, distinct target
      // whose handler is empty, because the catalog has no screen to navigate to.
      onContainerClick = lambdaAction {},
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY.rs) },
    )
  } else {
    RemoteCheckboxButton(
      checked = checked,
      onCheckedChange = toggle,
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY.rs) },
    )
  }
}

@CatalogComponent(
  id = "SwitchButton",
  group = "Selection buttons",
  parallel = "SwitchButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85629",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row that flips a switch; for a setting that takes effect immediately.",
)
@CatalogRemoteModes
@RemoteSelectionCells
@Composable
fun SwitchRowRemote() = RemoteSticker {
  // Line for line the checkbox row above, with the pair of functions swapped. The Wear sibling
  // carries a `motionPreview` on its `SwitchButton` for the thumb's travel; there is no Remote
  // equivalent to point at, and a motion reference naming a preview this module does not publish
  // would be a dangling id rather than a captured spring.
  val checked = rememberMutableRemoteBoolean(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true).rb
  val toggle = valueChange(checked, !checked)
  if (previewOverrideBoolean("split", false)) {
    RemoteSplitSwitchButton(
      checked = checked,
      onCheckedChange = toggle,
      toggleContentDescription = KitCopy.PRIMARY.rs,
      onContainerClick = lambdaAction {},
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY.rs) },
    )
  } else {
    RemoteSwitchButton(
      checked = checked,
      onCheckedChange = toggle,
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY.rs) },
    )
  }
}

@CatalogComponent(
  id = "RadioButton",
  group = "Selection buttons",
  parallel = "RadioButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35326:85655",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:84869",
  caption = "A labelled row in a set where exactly one may be chosen.",
)
@CatalogRemoteModes
@RemoteSelectionCells
@Composable
fun RadioRowRemote() = RemoteSticker {
  // The one place the three types are NOT the same call. A radio row selects rather than toggles —
  // its `onSelect` is one-way, and the Wear sibling spells that `onSelectedChange(true)`. The
  // Remote spelling is `valueChange(selected, true.rb)`: a recorded document whose tap sets the
  // value rather than flipping it, which is what a set where exactly one may be chosen does.
  // The `checked` override keeps its name so the cell spellings stay identical across the three.
  val selected = rememberMutableRemoteBoolean(previewOverrideBoolean("checked", true))
  val enabled = previewOverrideBoolean("enabled", true).rb
  val select = valueChange(selected, true.rb)
  if (previewOverrideBoolean("split", false)) {
    RemoteSplitRadioButton(
      selected = selected,
      onSelectionClick = select,
      selectionContentDescription = KitCopy.PRIMARY.rs,
      onContainerClick = lambdaAction {},
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY.rs) },
    )
  } else {
    RemoteRadioButton(
      selected = selected,
      onSelect = select,
      modifier = RemoteModifier.width(KitRowWidth),
      enabled = enabled,
      label = { RemoteText(KitCopy.PRIMARY.rs) },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY.rs) },
    )
  }
}
