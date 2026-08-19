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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.SliderDefaults
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.StepperDefaults
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
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

@CatalogComponent(
  id = "Slider",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43711:37256",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34828:79081",
  caption = "A value across a fixed number of steps, with the kit's levels folded in as cells.",
)
@CatalogModes
@OverrideVariant(name = "low", floats = ["value=1.0"], kitAxis = "Level", kitValue = "Low")
@OverrideVariant(name = "full", floats = ["value=5.0"], kitAxis = "Level", kitValue = "Full")
@OverrideVariant(
  name = "three-increments",
  ints = ["steps=2"],
  kitAxis = "Increments",
  kitValue = "Three",
)
// The kit's `Increments=Percentage` is a bar with no separators in it — which is what `segmented =
// false` draws, so it is that cell rather than a step count.
@OverrideVariant(
  name = "continuous",
  booleans = ["segmented=false"],
  kitAxis = "Increments",
  kitValue = "Percentage",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ValueSlider() = Sticker {
  // THE KIT COUNTS BANDS; COMPOSE COUNTS THE STOPS BETWEEN THEM. The kit's axis is `Increments` —
  // how many bands the bar is cut into — and `steps` is the number of values between the ends, so
  // the bar draws `steps + 1` bands (`visibleSegments = steps + 1`, Slider.kt). The base cell is
  // `Increments=Five`, which is `steps = 4`: at five it drew six bands and five separators against
  // a five-band, four-separator reference (#34).
  val steps = previewOverrideInt("steps", 4)
  // `value` COUNTS FILLED BANDS, because the range below runs over the bands rather than 0..1 —
  // and that is what the kit's `Level` cells are. `Mid` fills two bands whether the bar is cut
  // into three, four or five, and `Low` fills one, not none: `value = 0.0` published an empty bar
  // against a reference with one band lit. Counting is also what keeps the `Increments=Three` cell
  // on the kit's own Mid (two of three) while varying the one axis that cell names — two fifths of
  // a three-band bar is one band, and a cell that seeded both knobs could carry no `kitAxis`.
  val (value, onValueChange) = heldValue(previewOverrideFloat("value", 2f))
  // `plus-minus` is the pair `SliderDefaults` recommends and the kit draws. The chevrons are the
  // other pairing the API's two icon slots exist for, and a reader cannot discover a slot from a
  // still — so it is offered as a choice rather than described.
  val icons = previewOverrideChoice("icons", "plus-minus", listOf("plus-minus", "chevron"))
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
    enabled = previewOverrideBoolean("enabled", true),
    decreaseIcon = {
      if (icons == "chevron")
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Less")
      else SliderDefaults.DecreaseIcon()
    },
    increaseIcon = {
      if (icons == "chevron")
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
@CatalogComponent(
  id = "Stepper",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45007:258717",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/44993:61162",
  caption = "A value moved a step at a time, between buttons at the top and bottom of the screen.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "icon", strings = ["content=icon"], kitAxis = "Icon", kitValue = "Yes")
// `Button Fill=No` is the kit's third axis and it was not reachable at all. Compose spells it as a
// colour rather than a flag — the buttons keep their icons and lose their container — so it is
// `StepperDefaults.colors(buttonContainerColor = Color.Transparent)` on the real component.
@OverrideVariant(
  name = "no-button-fill",
  booleans = ["buttonFill=false"],
  kitAxis = "Button Fill",
  kitValue = "No",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ValueStepper() = FullScreenSticker {
  val (value, onValueChange) = heldValue(previewOverrideFloat("value", 0.5f))
  val enabled = previewOverrideBoolean("enabled", true)
  // Up/down, because the stepper's buttons are stacked: its decrease button is at the BOTTOM of
  // the display and its increase button at the top, which is the arrangement the kit draws too.
  val icons = previewOverrideChoice("icons", "chevron", listOf("chevron", "plus-minus"))
  Stepper(
    value = value,
    onValueChange = onValueChange,
    steps = previewOverrideInt("steps", 5),
    valueRange = valueRange(),
    enabled = enabled,
    colors =
      if (previewOverrideBoolean("buttonFill", true)) StepperDefaults.colors()
      else StepperDefaults.colors(buttonContainerColor = Color.Transparent),
    decreaseIcon = {
      if (icons == "plus-minus") SliderDefaults.DecreaseIcon()
      else Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Less")
    },
    increaseIcon = {
      if (icons == "plus-minus") SliderDefaults.IncreaseIcon()
      else Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "More")
    },
  ) {
    if (previewOverrideChoice("content", "text", listOf("text", "icon")) == "icon") {
      Icon(Icons.Filled.Settings, contentDescription = "Volume")
    } else {
      Text(kitCopy("label", KitCopy.STEPPER_LABEL))
    }
  }
}
