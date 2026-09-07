@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteStepper
import androidx.wear.compose.remote.material3.RemoteStepperDefaults
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Stepper` SET, on the Remote column for the first time.
//
// `RemoteStepper` landed in androidx.dev build 16280882, alongside the slider, switch and radio
// this sheet has already drawn. The set publishes eight nodes over `Button Fill` x `Icon` x
// `Disabled` and all eight are drawn here, under the Wear sibling's cell names.
//
// THE CELLS ARE THE WEAR SIBLING'S, transcribed rather than retyped, on the same reasoning
// `SliderPreviews.kt` records: the three knobs they seed — `enabled`, `buttonFill`, `content` —
// are named identically on both columns, so identical spellings are what let the compare page pair
// `icon-no-button-fill-disabled` with itself rather than pairing two names for one cell.
//
// THREE THINGS THIS FILE PASSES EXPLICITLY, each because the Remote DEFAULT differs from the Wear
// sibling's and a default taken here would be a difference invented by this file:
//
//   * CHEVRONS, not plus/minus. `RemoteStepperDefaults.DecreaseIcon()` delegates to
//     `RemoteSliderDefaults`, which draws `RemoteIcons.Add` / `Remove` — a `+` and a `-`. The Wear
//     `ValueStepper`'s default is `StepIcons.Chevron`, drawing `Icons.Filled.KeyboardArrowUp` and
//     `KeyboardArrowDown`, and that is what the kit's node shows. So the chevrons are passed in.
//     They are the SAME `ImageVector`s the Wear sibling passes, from the same artifact — which is
//     only true since this module gained an icon dependency at all.
//   * `0f..1f` AS THE RANGE, with `steps = 5`. `RemoteStepper` defaults its range to
//     `0f..(steps + 1)`, which for a stepper is a different domain from the Wear card's
//     `valueRange()` default of `0f..1f`. The slider's default range is left alone next door
//     because there it AGREES with the Wear computation; here it does not, so it is stated.
//   * A TRANSPARENT BUTTON CONTAINER for `Button Fill=No`, through
//     `stepperColors(buttonContainerColor = …)` — the same knob name, and the same colour, the
//     Wear sibling passes to `StepperDefaults.colors`.
//
// `RemoteStepperColors` is `@RestrictTo(LIBRARY_GROUP)` and so is `RemoteStepperDefaults.IconSize`,
// which is why this file carries the suppression at the top. Using them is the deliberate house
// position: a restricted API that draws a kit cell is worth suppressing a lint for, and withholding
// the cell instead would report the library as unable to draw something it draws perfectly well.
//
// WHAT IS MISSING, AND IT IS THE LIBRARY'S: THE LEVEL RAIL.
//
// The kit's `Stepper` set carries `Level Indicator` as a boolean property defaulting to **Yes**, so
// every node this card is compared against draws a rail beside the buttons. The Wear sibling draws
// it with `StepperLevelIndicator` and added it for exactly that reason
// ([#315](https://github.com/yschimke/wear-m3-catalog/issues/315)).
//
// `remote-material3` publishes NO level indicator of any kind — not restricted, not internal,
// absent: the 84 classes in the artifact carry `RemoteCircularProgressIndicator`,
// `RemoteLinearProgressIndicator`, `RemoteCurvedProgressIndicator` and `RemotePageIndicator`, and
// nothing that draws a stepper's rail. So every cell here will report the missing rail as a
// difference against its kit node, on all eight. That is the correct outcome rather than a gap to
// paper over: it is a real thing this rendition cannot draw, it is tracked upstream in
// [#356](https://github.com/yschimke/wear-m3-catalog/issues/356), and drawing a curved progress arc
// in its place would be inventing a component under the kit's name.

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
annotation class RemoteStepperKitCells
@CatalogComponent(
  id = "Stepper",
  group = "Steppers",
  parallel = "Stepper",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45007:258717",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/44993:61162",
  caption =
    "A value moved a step at a time, between buttons at the top and bottom of the screen. The " +
      "kit draws a level rail beside them; remote-material3 publishes no level indicator, so " +
      "this rendition cannot.",
)
@CatalogRemoteScreen
@RemoteStepperKitCells
@Composable
fun ValueStepperRemote() = RemoteSticker {
  // The stepper OWNS its value, as the Wear sibling's does, and the two actions bind the buttons so
  // the recorded document still steps when a player replays it with no host round-trip. A step is
  // 1/6 of the range: `steps = 5` cuts `0f..1f` into six.
  val value = rememberMutableRemoteFloat(0.5f)
  val step = (1f / 6f).rf
  val enabled = previewOverrideBoolean("enabled", true).rb
  val buttonFill = previewOverrideBoolean("buttonFill", true)
  RemoteStepper(
    value = value,
    steps = 5,
    // Up at the top and down at the bottom, which is how the buttons are stacked and how the kit
    // draws them: the DECREASE button sits at the bottom of the display.
    decreaseIcon = { RemoteIcon(Icons.Filled.KeyboardArrowDown, contentDescription = "Less".rs) },
    increaseIcon = { RemoteIcon(Icons.Filled.KeyboardArrowUp, contentDescription = "More".rs) },
    decreaseAction = valueChange(value, value - step),
    increaseAction = valueChange(value, value + step),
    enabled = enabled,
    // See the file note: stated rather than defaulted, because the library's default range for a
    // stepper is not the Wear card's.
    valueRange = 0f..1f,
    colors =
      if (buttonFill) RemoteStepperDefaults.stepperColors()
      else RemoteStepperDefaults.stepperColors(buttonContainerColor = RemoteColor(Color.Transparent)),
  ) {
    // The kit's `Icon` axis, as what the content slot holds. `Icons.Filled.Settings` is the Wear
    // sibling's glyph for this cell, and now literally the same `ImageVector` rather than a
    // transcription of it.
    if (previewOverrideChoice("content", "text", listOf("text", "icon")) == "icon") {
      RemoteIcon(Icons.Filled.Settings, contentDescription = "Volume".rs)
    } else {
      RemoteText(KitCopy.STEPPER_LABEL.rs)
    }
  }
}
