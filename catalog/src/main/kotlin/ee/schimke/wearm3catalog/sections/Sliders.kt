@file:CatalogGroup(name = "Sliders and steppers", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.catalogInteractive

// The kit's `Slider` and `Stepper` sets. Both own their value: a control that cannot be moved is
// not the component, so a live session drags and steps for real while the baked capture stays at
// the value each cell names.
//
// The stepper fills the screen — its two buttons sit at the top and bottom of the display — so it
// publishes on the round frame; the slider is a band and crops.

/** A value the sticker owns in a live session and holds still for a baked capture. */
@Composable
private fun heldValue(initial: Float): Pair<Float, (Float) -> Unit> {
  if (!catalogInteractive()) return initial to {}
  var value by remember { mutableFloatStateOf(initial) }
  return value to { it: Float -> value = it }
}

@CatalogComponent(
  id = "Slider",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43711:37256",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34828:79081",
  caption = "A value across a fixed number of steps, with the kit's levels folded in as cells.",
)
@CatalogModes
@OverrideVariant(name = "low", floats = ["level=0.0"], kitAxis = "Level", kitValue = "Low")
@OverrideVariant(name = "full", floats = ["level=1.0"], kitAxis = "Level", kitValue = "Full")
@OverrideVariant(
  name = "three-steps",
  ints = ["steps=3"],
  kitAxis = "Increments",
  kitValue = "Three",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ValueSlider() = Sticker {
  val (value, onValueChange) = heldValue(previewOverrideFloat("level", 0.5f))
  Slider(
    value = value,
    onValueChange = onValueChange,
    steps = previewOverrideInt("steps", 5),
    valueRange = 0f..1f,
    enabled = previewOverrideBoolean("enabled", true),
    modifier = Modifier.width(180.dp),
  )
}

@CatalogComponent(
  id = "Stepper",
  reference = "figma:B24oss2tTeXAFykyeyusz0/44993:61163",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/44993:61162",
  caption = "A value moved a step at a time, between buttons at the top and bottom of the screen.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun ValueStepper() = FullScreenSticker {
  val (value, onValueChange) = heldValue(previewOverrideFloat("level", 0.5f))
  val enabled = previewOverrideBoolean("enabled", true)
  Stepper(
    value = value,
    onValueChange = onValueChange,
    steps = 5,
    valueRange = 0f..1f,
    enabled = enabled,
    decreaseIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Less") },
    increaseIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "More") },
  ) {
    if (previewOverrideBoolean("icon", false)) {
      Icon(Icons.Filled.Settings, contentDescription = "Volume")
    } else {
      Text("Volume")
    }
  }
}
