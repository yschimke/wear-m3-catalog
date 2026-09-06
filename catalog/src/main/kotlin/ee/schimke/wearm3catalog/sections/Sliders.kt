@file:CatalogGroup(name = "Sliders and steppers", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.SliderDefaults
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.StepperDefaults
import androidx.wear.compose.material3.StepperLevelIndicator
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.catalogInteractive
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Slider` and `Stepper` sets. Both own their value: a control that cannot be moved is
// not the component, so a live session drags and steps for real while the baked capture stays at
// the value each cell names.
//
// The stepper fills the screen — its two buttons sit at the top and bottom of the display — so it
// publishes on the round frame; the slider is a band and crops.
//
// EVERY PARAMETER THAT CHANGES THE PICTURE IS A KNOB, SPELLED THE WAY COMPOSE SPELLS IT.
//
// A reader browsing the live sheet only has the controls panel to go on, and the panel is built
// from the `previewOverride*` calls a sticker makes: a parameter the sticker hardcodes is a
// parameter that reader cannot reach, and a knob named for something other than its parameter is
// one they cannot connect back to the API. Both were true here (issue #30) — `segmented`,
// `valueRange` and the two icon slots were pinned, and the value knob was called `level` after the
// kit's axis rather than `value` after Compose's parameter.
//
// The rule that came out of it, and that the rest of this catalog now follows: the KNOB carries
// Compose's name and the kit's word rides on the cell as `kitAxis` / `kitValue` (AGENTS.md already
// says this for the seed; it is the same argument one level up). What stays off the panel is what
// is not a scalar a reader can type — `colors`, `shape` and `modifier` are theme-level objects, and
// the theme switcher is where those are chosen.
//
// Knobs are additive: every default below is the value the sticker already rendered, so the baked
// captures and their kit comparisons are unchanged and only a live session sees the alternatives.

/**
 * A value the sticker owns in a live session and holds still for a baked capture.
 *
 * Keyed on [initial], so turning the `value` knob in a live session re-seeds the state. Remembered
 * unkeyed it captured the first composition's value forever, which made the one control the
 * component is *about* the one control that did nothing.
 */
@Composable
private fun heldValue(initial: Float): Pair<Float, (Float) -> Unit> {
  if (!catalogInteractive()) return initial to {}
  var value by remember(initial) { mutableFloatStateOf(initial) }
  return value to { it: Float -> value = it }
}

/**
 * The `valueRange` both components take, as the two floats a controls panel can offer.
 *
 * [defaultEnd] is the end the sticker uses when the knob is untouched. The slider passes its band
 * count, so its `value` knob is a count of filled bands and the kit's `Level` cells can be written
 * as the counts they are; the stepper keeps `0f..1f`, which is what its capture has always drawn.
 *
 * Coerced to a non-empty range on the way out: these are live knobs, and `value..value` (or a end
 * typed below its start) reaches `coerceIn` as an empty range and throws rather than rendering.
 */
@Composable
private fun valueRange(defaultEnd: Float = 1f): ClosedFloatingPointRange<Float> {
  val start = previewOverrideFloat("valueRangeStart", 0f)
  val end = previewOverrideFloat("valueRangeEnd", defaultEnd)
  return start..maxOf(end, start + 0.0001f)
}

/**
 * **Every `Slider` cell that is a still** — six `Increments` values by three `Level` values by
 * `Disabled`, 36 of the set's 54 nodes, against the six that were drawn
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * The other 18 are the `Changed=Yes` column: the kit's picture of a bar that has JUST been moved,
 * which is a moment rather than a state — Wear Compose has no parameter for it, and the sticker
 * that could show it is a recording. It is the same reason `Motion.kt` exists.
 *
 * `Level` is a band COUNT here, not a fraction, because the value range runs over the bands (see
 * the component). So `Full` is `steps + 1` and moves with `Increments`, which is why every crossing
 * seeds both knobs and declares the pair.
 *
 * `Increments=Percentage` is not a step count at all: it is the kit's bar with no separators in it,
 * which is what `segmented = false` draws, so those cells seed that instead of `steps`.
 */
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
annotation class SliderKitCells

/** The kit's `Icons` axis for the slider and stepper buttons. */
enum class StepIcons {
  @KnobValue("plus-minus") PlusMinus,
  @KnobValue("chevron") Chevron,
}

/** The kit's `Content` axis for the stepper's label slot. */
enum class StepperContent {
  @KnobValue("text") Text,
  @KnobValue("icon") Icon,
}

@CatalogComponent(
  id = "Slider",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43711:37256",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34828:79081",
  caption = "A value across a fixed number of steps, with the kit's levels folded in as cells.",
)
@CatalogModes
@SliderKitCells
@Composable
fun ValueSlider(
  steps: Int = 4,
  value: Float = 2f,
  enabled: Boolean = true,
  icons: StepIcons = StepIcons.PlusMinus,
) = Sticker {
  // THE KIT COUNTS BANDS; COMPOSE COUNTS THE STOPS BETWEEN THEM. The kit's axis is `Increments` —
  // how many bands the bar is cut into — and `steps` is the number of values between the ends, so
  // the bar draws `steps + 1` bands (`visibleSegments = steps + 1`, Slider.kt). The base cell is
  // `Increments=Five`, which is `steps = 4`: at five it drew six bands and five separators against
  // a five-band, four-separator reference (#34).
  // `value` COUNTS FILLED BANDS, because the range below runs over the bands rather than 0..1 —
  // and that is what the kit's `Level` cells are. `Mid` fills two bands whether the bar is cut
  // into three, four or five, and `Low` fills one, not none: `value = 0.0` published an empty bar
  // against a reference with one band lit. Counting is also what keeps the `Increments=Three` cell
  // on the kit's own Mid (two of three) while varying the one axis that cell names — two fifths of
  // a three-band bar is one band, and a cell that seeded both knobs could carry no `kitAxis`.
  val (value, onValueChange) = heldValue(value)
  // `plus-minus` is the pair `SliderDefaults` recommends and the kit draws. The chevrons are the
  // other pairing the API's two icon slots exist for, and a reader cannot discover a slot from a
  // still — so it is offered as a choice rather than described.
  Slider(
    value = value,
    onValueChange = onValueChange,
    steps = steps,
    // Over the bands, which is the library's own default range for a slider and what makes
    // `value` above a band count.
    valueRange = valueRange(defaultEnd = (steps + 1).toFloat()),
    // The library's own default is conditional — segmented up to `MaxSegmentSteps` and not beyond
    // — so the knob's default computes it rather than pinning `true`, and a step count past the
    // recommended maximum still reports what the component would actually do.
    segmented = previewOverrideBoolean("segmented", steps <= SliderDefaults.MaxSegmentSteps),
    enabled = enabled,
    decreaseIcon = {
      if (icons == StepIcons.Chevron)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Less")
      else SliderDefaults.DecreaseIcon()
    },
    increaseIcon = {
      if (icons == StepIcons.Chevron)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "More")
      else SliderDefaults.IncreaseIcon()
    },
    modifier = Modifier.width(180.dp),
  )
}

// REPOINTED, and the new `no-button-fill` cell below is how it was caught. `44993:61163` is the
// kit's `Button Fill=No` cell — buttons with no container — and this sticker draws Compose's
// default, which fills them. The base capture has therefore always been compared against a cell it
// is not a picture of; the projector said so out loud ("the reference already draws this") the
// moment a cell claimed `Button Fill=No` for itself. `45007:258717` is the same arrangement with
// the fill on, which is what the base render actually is.
/**
 * **Every `Stepper` cell** — `Button Fill` by `Icon` by `Disabled`, all eight of the set's nodes,
 * against the four that were drawn
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * The two disabled `Button Fill=No` cells are a collapse rather than a difference: a disabled
 * stepper draws no button container to begin with, so turning the fill off changes nothing and the
 * render repeats the plain disabled one. They are drawn anyway and recorded in
 * `CatalogRenderTest.knownDuplicate` — the kit draws two cells there and the library draws one
 * picture for both, which is a finding about the pair and belongs on the sheet rather than behind a
 * gap that reads as unfinished work
 * ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
 *
 * `Button Fill=No` is a colour rather than a flag in Compose — the buttons keep their icons and
 * lose their container, which is `StepperDefaults.colors(buttonContainerColor = Color.Transparent)`
 * on the real component. It is also the cell that caught this component's reference pointing at the
 * wrong node; the note above `ValueStepper` has that story.
 */
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "no-button-fill",
  booleans = ["buttonFill=false"],
  kitAxis = "Button Fill",
  kitValue = "No",
)
@OverrideVariant(
  name = "icon",
  strings = ["content=icon"],
  kitAxis = "Icon",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "icon-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon"],
  kitProps = ["Button Fill=Yes", "Icon=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-no-button-fill",
  booleans = ["buttonFill=false"],
  strings = ["content=icon"],
  kitProps = ["Button Fill=No", "Icon=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "no-button-fill-disabled",
  booleans = ["buttonFill=false", "enabled=false"],
  kitProps = ["Button Fill=No", "Icon=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-no-button-fill-disabled",
  booleans = ["buttonFill=false", "enabled=false"],
  strings = ["content=icon"],
  kitProps = ["Button Fill=No", "Icon=Yes", "Disabled=Yes"],
  secondary = true,
)
annotation class StepperKitCells

@CatalogComponent(
  id = "Stepper",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45007:258717",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/44993:61162",
  caption =
    "A value moved a step at a time, between buttons at the top and bottom of the screen, with " +
      "the level rail the kit draws beside them.",
)
@CatalogFullScreenModes
@StepperKitCells
@Composable
fun ValueStepper(
  value: Float = 0.5f,
  enabled: Boolean = true,
  steps: Int = 5,
  buttonFill: Boolean = true,
  icons: StepIcons = StepIcons.Chevron,
  content: StepperContent = StepperContent.Text,
  levelIndicator: Boolean = true,
) = FullScreenSticker {
  val (value, onValueChange) = heldValue(value)
  val range = valueRange()
  // THE RAIL IS PART OF THE KIT'S STEPPER, and this sticker used to leave it out.
  //
  // `StepperLevelIndicator` is a separate function from `LevelIndicator` — which by the call-site
  // test argues for a card of its own — but the kit is the one that decides what a component IS
  // here, and the kit does not publish it as a set: `Level-Indicator-RSB` is the rotating side
  // button's rail, and the stepper pairing is a BOOLEAN property on the `Stepper` set itself
  // (`Level Indicator`, default **Yes**). So every node this card is compared against draws the
  // rail, and the projector said so out loud — "`Stepper` — Stepper: `Level Indicator`" under the
  // references that draw optional content the render omits. A second card would have paired that
  // gap with a picture nobody was comparing; drawing it here closes it
  // ([#315](https://github.com/yschimke/wear-m3-catalog/issues/315)).
  //
  // It is a KNOB rather than a cell because the kit's `Level Indicator` is a boolean property and
  // not a variant axis: the set publishes eight nodes over `Button Fill` × `Icon` × `Disabled` and
  // none of them turn the rail off, so a cell for it would name a vector the kit never drew. The
  // default is the kit's default, which is what makes the baked capture the comparable one.
  //
  // Same `valueRange` and same `enabled` as the stepper below, because that is the whole point of
  // the pairing: the rail reads the stepper's value domain rather than a fraction of its own, and
  // a disabled stepper's rail takes the disabled colours with it.
  if (levelIndicator) {
    StepperLevelIndicator(
      value = { value },
      valueRange = range,
      enabled = enabled,
      // `CenterStart`, as `LevelRail` is aligned in `Indicators.kt`: the rail passes
      // `rsbSide = false` internally and draws on the left in Ltr, and it strikes its arc from the
      // centre of the box it is laid out in — so an unaligned one comes out as a diagonal stroke
      // half off the frame (#18).
      modifier = Modifier.align(Alignment.CenterStart),
    )
  }
  // Up/down, because the stepper's buttons are stacked: its decrease button is at the BOTTOM of
  // the display and its increase button at the top, which is the arrangement the kit draws too.
  Stepper(
    value = value,
    onValueChange = onValueChange,
    steps = steps,
    valueRange = range,
    enabled = enabled,
    colors =
      if (buttonFill) StepperDefaults.colors()
      else StepperDefaults.colors(buttonContainerColor = Color.Transparent),
    decreaseIcon = {
      if (icons == StepIcons.PlusMinus) SliderDefaults.DecreaseIcon()
      else Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Less")
    },
    increaseIcon = {
      if (icons == StepIcons.PlusMinus) SliderDefaults.IncreaseIcon()
      else Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "More")
    },
  ) {
    if (content == StepperContent.Icon) {
      Icon(Icons.Filled.Settings, contentDescription = "Volume")
    } else {
      Text(kitCopy("label", KitCopy.STEPPER_LABEL))
    }
  }
}
