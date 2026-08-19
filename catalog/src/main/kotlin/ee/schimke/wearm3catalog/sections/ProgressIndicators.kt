@file:CatalogGroup(name = "Progress indicators", section = "Communication")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AngularDirection
import androidx.wear.compose.material3.ArcProgressIndicator
import androidx.wear.compose.material3.ArcProgressIndicatorDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.LinearProgressIndicator
import androidx.wear.compose.material3.LinearProgressIndicatorDefaults
import androidx.wear.compose.material3.SegmentedCircularProgressIndicator
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.overrides.previewOverrideDp
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker

// The kit's three progress sets. They are three components because Compose has three functions —
// the segmented ring is `SegmentedCircularProgressIndicator`, not a property of the round one — and
// the axes within each (progress, stroke width, segment count) are arguments, so they fold.
//
// Progress is pinned rather than animated: an indeterminate or mid-animation indicator would render
// differently on every nightly publish, and the delivery branch's history would be noise. The kit
// pins the same way, by drawing a `Progress=` value per cell.
//
// Every parameter that changes the picture is a knob spelled the way Compose spells it, and every
// knob with a closed set of values is a `previewOverrideChoice` so the controls panel offers the
// alternatives instead of a text box only someone who has read this file can fill in. See the note
// at the top of Sliders.kt (issue #30) — the arc indicator was the extreme case, published with no
// controls at all despite taking four.

@CatalogComponent(
  id = "CircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41424:58637",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/41424:58385",
  caption = "A ring around the display edge, with the kit's progress and stroke axes folded in.",
)
@CatalogModes
@OverrideVariant(name = "zero", floats = ["progress=0.0"], kitAxis = "Progress", kitValue = "Zero")
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(
  name = "overflow",
  floats = ["progress=1.4"],
  kitAxis = "Progress",
  kitValue = "Overflow",
)
@OverrideVariant(
  name = "small-stroke",
  strings = ["stroke=small"],
  kitAxis = "Stroke Width",
  kitValue = "Small",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
// The kit publishes four determinate `Progress=` values and no indeterminate one, but the library
// ships both on the same name — so it folds in here as a cell rather than standing up a second
// card for the same component. The cell is a still of something that only reads as itself in
// motion; `Motion.kt` carries the recording.
@OverrideVariant(name = "indeterminate", strings = ["mode=indeterminate"])
@Composable
fun CircularProgress() = Sticker {
  val progress = previewOverrideFloat("progress", 0.6f)
  val stroke =
    if (previewOverrideChoice("stroke", "medium", listOf("medium", "small")) == "small")
      CircularProgressIndicatorDefaults.smallStrokeWidth
    else CircularProgressIndicatorDefaults.largeStrokeWidth
  // The kit's `Type = Full | Top Gap | Bottom Gap` axis is these two angles in Compose: equal
  // angles close the ring, and separating them opens a gap wherever the pair points. No cells —
  // the kit does not publish the angles its two gap cells draw, and an invented number under the
  // kit's name is worse than an honest absence.
  val startAngle = previewOverrideFloat("startAngle", CircularProgressIndicatorDefaults.StartAngle)
  val endAngle = previewOverrideFloat("endAngle", startAngle)
  if (
    previewOverrideChoice("mode", "determinate", listOf("determinate", "indeterminate")) ==
      "indeterminate"
  ) {
    // The indeterminate overload takes neither progress nor angles — it is a different function on
    // the same name, and the knobs above simply do not reach it.
    CircularProgressIndicator(modifier = Modifier.size(120.dp), strokeWidth = stroke)
  } else {
    CircularProgressIndicator(
      progress = { progress },
      modifier = Modifier.size(120.dp),
      enabled = previewOverrideBoolean("enabled", true),
      // The API defaults this to `false`; the sticker defaults it to `true` because the kit
      // publishes a `Progress=Overflow` cell, and coerced into 0..1 that cell is the complete one.
      allowProgressOverflow = previewOverrideBoolean("allowProgressOverflow", true),
      startAngle = startAngle,
      endAngle = endAngle,
      strokeWidth = stroke,
    )
  }
}

@CatalogComponent(
  id = "SegmentedCircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/52431:57063",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/52431:56915",
  caption = "The same ring cut into segments, for progress through a countable set of steps.",
)
@CatalogModes
@OverrideVariant(name = "zero", floats = ["progress=0.0"], kitAxis = "Progress", kitValue = "Zero")
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(
  name = "twelve",
  ints = ["segmentCount=12"],
  kitAxis = "Segments",
  kitValue = "12",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun SegmentedProgress() = Sticker {
  val progress = previewOverrideFloat("progress", 0.6f)
  val startAngle = previewOverrideFloat("startAngle", CircularProgressIndicatorDefaults.StartAngle)
  SegmentedCircularProgressIndicator(
    // `segmentCount` is the parameter's own name — the knob used to be `segments`, which is the
    // kit's word for the axis and rides on the cell instead.
    segmentCount = previewOverrideInt("segmentCount", 6),
    progress = { progress },
    modifier = Modifier.size(120.dp),
    enabled = previewOverrideBoolean("enabled", true),
    allowProgressOverflow = previewOverrideBoolean("allowProgressOverflow", false),
    startAngle = startAngle,
    endAngle = previewOverrideFloat("endAngle", startAngle),
    strokeWidth =
      previewOverrideDp("strokeWidth", CircularProgressIndicatorDefaults.largeStrokeWidth),
  )
}

@CatalogComponent(
  id = "ArcProgressIndicator",
  noReference =
    "The kit publishes no arc indicator: its progress sets are the full ring, the segmented ring " +
      "and the linear track. This is a Wear Compose component with no kit counterpart.",
  caption = "An indeterminate arc along the bezel, for a wait with no measurable progress.",
)
@CatalogModes
@Composable
fun ArcProgress() = Sticker {
  val strokeWidth =
    previewOverrideDp("strokeWidth", ArcProgressIndicatorDefaults.IndeterminateStrokeWidth)
  ArcProgressIndicator(
    modifier = Modifier.size(120.dp),
    startAngle =
      previewOverrideFloat("startAngle", ArcProgressIndicatorDefaults.IndeterminateStartAngle),
    endAngle = previewOverrideFloat("endAngle", ArcProgressIndicatorDefaults.IndeterminateEndAngle),
    angularDirection =
      if (
        previewOverrideChoice(
          "angularDirection",
          "counter-clockwise",
          listOf("counter-clockwise", "clockwise"),
        ) == "clockwise"
      ) {
        AngularDirection.Clockwise
      } else {
        AngularDirection.CounterClockwise
      },
    strokeWidth = strokeWidth,
  )
}

@CatalogComponent(
  id = "LinearProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45011:259221",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/45011:259051",
  caption = "A straight track, for progress inside a component or a list row.",
)
@CatalogModes
@OverrideVariant(name = "min", floats = ["progress=0.0"], kitAxis = "Progress", kitValue = "Min")
@OverrideVariant(name = "20", floats = ["progress=0.2"], kitAxis = "Progress", kitValue = "20%")
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(name = "small", strings = ["size=small"], kitAxis = "Size", kitValue = "Small")
@Composable
fun LinearProgress() = Sticker {
  val progress = previewOverrideFloat("progress", 0.5f)
  LinearProgressIndicator(
    progress = { progress },
    modifier = Modifier.width(150.dp),
    enabled = previewOverrideBoolean("enabled", true),
    strokeWidth =
      if (previewOverrideChoice("size", "large", listOf("large", "small")) == "small")
        LinearProgressIndicatorDefaults.StrokeWidthSmall
      else LinearProgressIndicatorDefaults.StrokeWidthLarge,
  )
}
