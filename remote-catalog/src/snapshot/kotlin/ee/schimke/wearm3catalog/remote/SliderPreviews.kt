@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteSlider
import androidx.wear.compose.remote.material3.RemoteSliderDefaults
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Slider` SET, opened on the Remote column for the first time.
//
// 54 published cells that `remote-m3` had drawn NONE of (`docs/KIT_COVERAGE.md` lists it `—`), for
// the plain reason that `remote-material3` published no slider. `RemoteSlider` appeared in
// androidx.dev build 16280882 — the same build that brought the switch and radio rows this sheet
// drew in #346 — and this file draws every cell of the set that is a STILL.
//
// A TRANSCRIPTION, DELIBERATELY. The 35 `@OverrideVariant`s below are the Wear sibling's
// `SliderKitCells` copied cell for cell (`catalog/src/main/kotlin/…/sections/Sliders.kt`), and that
// is not laziness: the four knobs they seed — `steps`, `value`, `enabled`, `segmented` — are named
// identically on both columns because `RemoteSlider`'s signature is `Slider`'s with the state types
// swapped. Keeping the spellings identical is what lets the compare page set `four-increments-full`
// beside `four-increments-full` rather than pairing two names for one cell. A divergence here would
// be a reported difference invented by this file rather than one either library draws.
//
// WHAT IS REACHABLE, and it is the whole still half of the set. `Increments` x `Level` x `Disabled`
// is 6 x 3 x 2 = 36 cells and all 36 are drawn, exactly as the Wear rendition draws them.
//
// WHAT IS NOT, and why there is no sticker for it:
//
//   * The 18 `Changed=Yes` cells. The kit's picture of a bar that has JUST been moved, which is a
//     moment rather than a state. The Wear sibling withholds them for the same reason and says so
//     in the same words: there is no parameter for it on either column, and the sticker that could
//     show it is a recording.
//   * The `Icons` knob. The Wear sticker offers `chevron` beside the kit's `plus-minus` pair,
//     because a reader cannot discover from a still that the two icon slots are slots. It is a KNOB
//     there and not a CELL — no `@OverrideVariant` seeds it — so withholding it here costs the
//     sheet no kit cell. It is withheld rather than transcribed because the Remote slots take
//     `@RemoteComposable` content and the chevron pair would have to be sourced as remote drawing
//     rather than as `Icons.AutoMirrored.Filled.KeyboardArrowLeft`; that is a component this file
//     would be inventing, and the kit publishes no cell to check it against.
//
// THE RANGE IS THE LIBRARY'S OWN, and that is what makes `value` a band count on both columns.
// `RemoteSlider`'s default `valueRange` is `0f..(steps + 1).toFloat()` — byte for byte the
// computation the Wear sticker spells out by hand — so it is left unset here rather than restated.
// `Level=Full` is therefore `steps + 1` and moves with `Increments`, which is why every crossing
// seeds both knobs and declares the pair.
//
// `segmented` IS A PLAIN `Boolean`, not a `RemoteBoolean`: the separators are a capture-time
// decision baked into the document rather than a value the player re-reads, so the knob feeds it
// directly. `enabled` is the opposite and takes `.rb` — read the two lines below together, because
// the asymmetry is the library's and not a slip.

@OverrideVariant(
  name = "three-increments-low",
  ints = ["steps=2"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Three", "Level=Low", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "three-increments-low-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=2"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Three", "Level=Low", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "three-increments",
  ints = ["steps=2"],
  kitAxis = "Increments",
  kitValue = "Three",
)
@OverrideVariant(
  name = "three-increments-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=2"],
  kitProps = ["Increments=Three", "Level=Mid", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "three-increments-full",
  ints = ["steps=2"],
  floats = ["value=3.0"],
  kitProps = ["Increments=Three", "Level=Full", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "three-increments-full-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=2"],
  floats = ["value=3.0"],
  kitProps = ["Increments=Three", "Level=Full", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "four-increments-low",
  ints = ["steps=3"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Four", "Level=Low", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "four-increments-low-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=3"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Four", "Level=Low", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "four-increments",
  ints = ["steps=3"],
  kitAxis = "Increments",
  kitValue = "Four",
)
@OverrideVariant(
  name = "four-increments-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=3"],
  kitProps = ["Increments=Four", "Level=Mid", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "four-increments-full",
  ints = ["steps=3"],
  floats = ["value=4.0"],
  kitProps = ["Increments=Four", "Level=Full", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "four-increments-full-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=3"],
  floats = ["value=4.0"],
  kitProps = ["Increments=Four", "Level=Full", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "low",
  floats = ["value=1.0"],
  kitAxis = "Level",
  kitValue = "Low",
)
@OverrideVariant(
  name = "low-disabled",
  booleans = ["enabled=false"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Five", "Level=Low", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "full",
  floats = ["value=5.0"],
  kitAxis = "Level",
  kitValue = "Full",
)
@OverrideVariant(
  name = "full-disabled",
  booleans = ["enabled=false"],
  floats = ["value=5.0"],
  kitProps = ["Increments=Five", "Level=Full", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "six-increments-low",
  ints = ["steps=5"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Six", "Level=Low", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "six-increments-low-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=5"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Six", "Level=Low", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "six-increments",
  ints = ["steps=5"],
  kitAxis = "Increments",
  kitValue = "Six",
)
@OverrideVariant(
  name = "six-increments-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=5"],
  kitProps = ["Increments=Six", "Level=Mid", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "six-increments-full",
  ints = ["steps=5"],
  floats = ["value=6.0"],
  kitProps = ["Increments=Six", "Level=Full", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "six-increments-full-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=5"],
  floats = ["value=6.0"],
  kitProps = ["Increments=Six", "Level=Full", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "seven-increments-low",
  ints = ["steps=6"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Seven", "Level=Low", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "seven-increments-low-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=6"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Seven", "Level=Low", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "seven-increments",
  ints = ["steps=6"],
  kitAxis = "Increments",
  kitValue = "Seven",
)
@OverrideVariant(
  name = "seven-increments-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=6"],
  kitProps = ["Increments=Seven", "Level=Mid", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "seven-increments-full",
  ints = ["steps=6"],
  floats = ["value=7.0"],
  kitProps = ["Increments=Seven", "Level=Full", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "seven-increments-full-disabled",
  booleans = ["enabled=false"],
  ints = ["steps=6"],
  floats = ["value=7.0"],
  kitProps = ["Increments=Seven", "Level=Full", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "continuous-low",
  booleans = ["segmented=false"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Percentage", "Level=Low", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "continuous-low-disabled",
  booleans = ["segmented=false", "enabled=false"],
  floats = ["value=1.0"],
  kitProps = ["Increments=Percentage", "Level=Low", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "continuous",
  booleans = ["segmented=false"],
  kitAxis = "Increments",
  kitValue = "Percentage",
)
@OverrideVariant(
  name = "continuous-disabled",
  booleans = ["segmented=false", "enabled=false"],
  kitProps = ["Increments=Percentage", "Level=Mid", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "continuous-full",
  booleans = ["segmented=false"],
  floats = ["value=5.0"],
  kitProps = ["Increments=Percentage", "Level=Full", "Changed=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "continuous-full-disabled",
  booleans = ["segmented=false", "enabled=false"],
  floats = ["value=5.0"],
  kitProps = ["Increments=Percentage", "Level=Full", "Changed=No", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteSliderKitCells
@CatalogComponent(
  id = "Slider",
  group = "Sliders",
  parallel = "Slider",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43711:37256",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34828:79081",
  caption = "A value across a fixed number of steps, with the kit's levels folded in as cells.",
)
@CatalogRemoteModes
@RemoteSliderKitCells
@Composable
fun ValueSliderRemote() = RemoteSticker {
  val steps = previewOverrideInt("steps", 4)
  // The bar OWNS its value, as the Wear sibling's does: a slider that cannot move is not the
  // component. `previewOverrideFloat` picks the value the document is RECORDED with — that is what
  // makes `four-increments-full` a cell — and the two actions below bind the buttons so the
  // recorded document still steps when a player replays it, with no host round-trip.
  val value = rememberMutableRemoteFloat(previewOverrideFloat("value", 2f))
  RemoteSlider(
    value = value,
    steps = steps,
    // The Wear sibling's number, and it has to be: a slider has no width of its own, so a
    // Remote-only width would trade a reported difference for an unreported one. Same reasoning
    // `LinearProgressRemote` records next door for its track.
    modifier = RemoteModifier.width(180.rdp),
    // `Action`s rather than a `(Float) -> Unit`, which is the one place this signature is not the
    // Wear one. Each button writes the stepped value straight into the document's state; the
    // library clamps to `valueRange`, so stepping past either end is the library's decision to
    // report rather than this call site's to pre-empt.
    decreaseAction = valueChange(value, value - 1f.rf),
    increaseAction = valueChange(value, value + 1f.rf),
    enabled = previewOverrideBoolean("enabled", true).rb,
    // The library's own default is conditional — segmented up to `MaxSegmentSteps` and not beyond —
    // so the knob's default computes it rather than pinning `true`, and a step count past the
    // recommended maximum still reports what the component would actually do. The Wear sibling
    // computes the same default from the same constant.
    segmented =
      previewOverrideBoolean("segmented", steps <= RemoteSliderDefaults.MaxSegmentSteps),
  )
}
