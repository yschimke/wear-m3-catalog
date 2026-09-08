@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteCurvedProgressIndicator
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteProgressIndicatorDefaults
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
// THE LEVEL RAIL, DRAWN WITH `RemoteCurvedProgressIndicator`.
//
// The kit's `Stepper` set carries `Level Indicator` as a boolean property defaulting to **Yes**, so
// every node this card is compared against draws a rail beside the buttons. The Wear sibling draws
// it with `StepperLevelIndicator` and added it for exactly that reason
// ([#315](https://github.com/yschimke/wear-m3-catalog/issues/315)).
//
// `remote-material3` publishes NO level indicator: the artifact carries
// `RemoteCircularProgressIndicator`, `RemoteLinearProgressIndicator`,
// `RemoteCurvedProgressIndicator` and `RemotePageIndicator`, and no `RemoteStepperLevelIndicator`
// or `RemoteLevelIndicator` ([#356](https://github.com/yschimke/wear-m3-catalog/issues/356), still
// the upstream ask). The rail below is `RemoteCurvedProgressIndicator` standing in for it, and this
// file used to refuse that substitution as a component invented under the kit's name. It is drawn
// now, on the reading that the two are the same picture struck by the same primitive — an arc of
// track with a filled leading segment — and every number the stand-in needs is PUBLISHED by one
// library or the other rather than chosen here:
//
//   * `sweepAngle = 72f` and `strokeWidth = 6.dp` are `LevelIndicatorDefaults.SweepAngle` and
//     `LevelIndicatorDefaults.StrokeWidth`, the Wear sibling's own tokens (the Remote arc's
//     defaults are 90f and 8dp, which are the progress arc's numbers and not the rail's).
//   * `startAngle = 144f` is `180f - sweepAngle / 2f` — the arc centred on the LEFT bezel, which is
//     where `LevelIndicator` puts it by passing `rsbSide = false`. Derived from the sweep, so it
//     moves with it rather than being a second literal.
//   * The fill direction needs nothing passed. `RemoteCurvedProgressIndicator` runs its active
//     segment CLOCKWISE from `startAngle`, and on the left bezel clockwise is upward — the same
//     bottom-anchored growth `FractionPositionStateAdapter` gets from `positionFraction = 1f`.
//   * `secondaryDim` over `surfaceContainer` are `LevelIndicatorColors`' indicator and track
//     tokens. The Remote arc's default indicator is `primary`, which is the progress colour; the
//     track already agrees. The DISABLED pair is left alone: both libraries resolve it to
//     `onSurface` at 0.38 / 0.12, so overriding it would restate the default.
//   * `padding` is left at `RemoteProgressIndicatorDefaults.CurvedIndicatorPadding`, 2dp, which is
//     already `PaddingDefaults.edgePadding` — the same number the Wear rail insets by.
//
// WHERE THE TWO STILL DIFFER, and it is stated rather than tuned away: the gap between the filled
// segment and the track is `CurvedIndicatorGapAngleDegrees` (a flat 2 degrees) where Wear derives
// it from the stroke width and a gap height in pixels, so the two rails break at slightly different
// places; and the Remote arc's dot behaviour is a progress-indicator affordance a rail has none of,
// which is why `dotCollapsible = false` is passed. Neither is a number invented here — one is the
// library's default and the other is its own switch.

/**
 * `LevelIndicatorDefaults.SweepAngle` — 72 degrees, a fifth of the circumference. Held here rather
 * than read from `androidx.wear.compose.material3` because this module does not depend on it, and
 * spelled once because the rail's start angle is derived from it.
 */
private const val LevelRailSweepAngle = 72f

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
    "A value moved a step at a time, between buttons at the top and bottom of the screen, with " +
      "the level rail the kit draws beside them — a curved progress arc standing in for the " +
      "level indicator remote-material3 does not publish.",
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
  // See the file note for every number here and where it is published. A KNOB rather than a cell,
  // exactly as on the Wear sibling: the kit's `Level Indicator` is a boolean property and not a
  // variant axis, so a cell for it would name a vector the kit never drew, and the default is the
  // kit's default — which is what makes the baked capture the comparable one.
  if (previewOverrideBoolean("levelIndicator", true)) {
    RemoteCurvedProgressIndicator(
      // The stepper's own value, which IS the fraction here: `StepperLevelIndicator` normalises
      // `value` over `valueRange`, and this card's range is `0f..1f`, so the two are the same
      // number. The rail reads the stepper's domain rather than a fraction of its own, and takes
      // the same `enabled` with it — the whole point of the pairing.
      progress = value,
      modifier = RemoteModifier.fillMaxSize(),
      enabled = enabled,
      startAngle = (180f - LevelRailSweepAngle / 2f).rf,
      sweepAngle = LevelRailSweepAngle.rf,
      colors =
        RemoteProgressIndicatorDefaults.colors(
          indicatorColor = RemoteMaterialTheme.colorScheme.secondaryDim,
          trackColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
        ),
      strokeWidth = 6.rdp,
      // A rail does not collapse to a dot; a progress arc does. See the file note.
      dotCollapsible = false.rb,
    )
  }
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
