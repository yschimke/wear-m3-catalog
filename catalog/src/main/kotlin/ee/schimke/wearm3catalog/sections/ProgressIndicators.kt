@file:CatalogGroup(name = "Progress indicators", section = "Communication")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.fillMaxSize
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
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.Sticker

// The kit's three progress sets. They are three components because Compose has three functions —
// the segmented ring is `SegmentedCircularProgressIndicator`, not a property of the round one — and
// the axes within each (progress, stroke width, segment count) are arguments, so they fold.
//
// The three sets are drawn on three DIFFERENT SHAPES of cell, and the frames here follow that
// rather than the family resemblance. `Progress-Indicator` is 192×192 displays with the ring 2dp
// inside the bezel, so `CircularProgressIndicator` takes the round frame;
// `Progress-Indicator-Small`
// is 80×80 component cells and `Progress-Indicator-Linear` is 172×12 ones, so those two are
// cropped stickers. All three used to be stickers, which quietly compared a 120dp ring against the
// whole watch face — the same defect as the edge button in issue #31 (AGENTS.md).
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

/**
 * **Every `Progress-Indicator` cell this function draws** — the kit's `Stroke Width` by `Progress`
 * by `Disabled` grid at `Segments=1`, 10 nodes of the set's 90, against the six that were drawn
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)). The grid is ragged and these
 * cells are generated from the kit's own cell list rather than from a product: the kit draws
 * `Disabled=Yes` only against `In progress` and `Zero`, so a product would mint two renders per
 * stroke that map to nothing.
 *
 * The two `Progress=Zero, Disabled=Yes` cells stay out as well, and that one IS a difference worth
 * writing down: Wear draws a disabled ring entirely in its disabled track colour, and at zero
 * progress on a transparent sticker that comes out as an empty frame — 0.00000 of it lit, which
 * `CatalogRenderTest.no sticker publishes an empty frame` rejects. The kit draws a visible track
 * there. Publishing a blank PNG against it would report the whole cell as a difference on every run
 * without saying anything the reader could act on.
 *
 * Two whole axes of that set stay out, and neither is a gap in this file:
 * * `Segments=6..14` is the SEGMENTED ring, which is `SegmentedCircularProgressIndicator` below — a
 *   different function, and the kit draws it here at display size and again in its own
 *   `Progress-Indicator-Small` set. The component that folds this axis is the one that calls the
 *   function taking it.
 * * `Type=Top Gap | Bottom Gap` is the pair of angles the component's own note explains: the kit
 *   does not publish the angles its gap cells draw, and an invented number under the kit's name is
 *   worse than an honest absence.
 */
@OverrideVariant(
  name = "small-stroke",
  strings = ["stroke=small"],
  kitAxis = "Stroke Width",
  kitValue = "Small",
)
@OverrideVariant(
  name = "small-stroke-complete",
  strings = ["stroke=small"],
  floats = ["progress=1.0"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Complete",
      "Dot value=No",
      "Disabled=No",
    ],
)
@OverrideVariant(
  name = "small-stroke-overflow",
  strings = ["stroke=small"],
  floats = ["progress=1.4"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Overflow",
      "Dot value=No",
      "Disabled=No",
    ],
)
@OverrideVariant(
  name = "small-stroke-zero",
  strings = ["stroke=small"],
  floats = ["progress=0.0"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Zero",
      "Dot value=No",
      "Disabled=No",
    ],
)
@OverrideVariant(
  name = "small-stroke-disabled",
  booleans = ["enabled=false"],
  strings = ["stroke=small"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=In progress",
      "Dot value=No",
      "Disabled=Yes",
    ],
)
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
  name = "zero",
  floats = ["progress=0.0"],
  kitAxis = "Progress",
  kitValue = "Zero",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
annotation class CircularProgressKitCells

@CatalogComponent(
  id = "CircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41424:58637",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/41424:58385",
  caption = "A ring around the display edge, with the kit's progress and stroke axes folded in.",
  // The indeterminate sweep, recorded in Motion.kt. It cannot be annotated here: a motion
  // annotation rides every `@OverrideVariant` cell below and would publish one recording under
  // four progress names, and a GIF needs the pinned canvas a cropped sticker does not have.
  motionPreview = "IndeterminateProgressMotion",
)
@CatalogFullScreenModes
@CircularProgressKitCells
// The kit publishes four determinate `Progress=` values and no indeterminate one, but the library
// ships both on the same name — so it folds in here as a cell rather than standing up a second
// card for the same component. The cell is a still of something that only reads as itself in
// motion; `Motion.kt` carries the recording.
@OverrideVariant(name = "indeterminate", strings = ["mode=indeterminate"])
@Composable
fun CircularProgress() = FullScreenSticker {
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
  // `fillMaxSize` throughout, because the kit's cell draws the ring 2dp inside the bezel of the
  // whole display — a fixed `size(120.dp)` is a ring around nothing in particular.
  if (
    previewOverrideChoice("mode", "determinate", listOf("determinate", "indeterminate")) ==
      "indeterminate"
  ) {
    // The indeterminate overload takes neither progress nor angles — it is a different function on
    // the same name, and the knobs above simply do not reach it.
    CircularProgressIndicator(modifier = Modifier.fillMaxSize(), strokeWidth = stroke)
  } else {
    CircularProgressIndicator(
      progress = { progress },
      modifier = Modifier.fillMaxSize(),
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

/**
 * **Every `Progress-Indicator-Small` cell this function draws** — 90 of the set's 174 nodes,
 * against the five that were drawn
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)). The kit crosses `Segments`
 * (1..14) with `Stroke Width`, four `Progress` values and `Disabled`, and it does so **raggedly** —
 * not every segment count carries every stroke, and `Disabled=Yes` is drawn only against `In
 * progress` and `Zero`. So these cells are generated from the kit's own published cell list rather
 * than from a product of its axis values, which is the difference between 90 cells that each land
 * on a node and 168 that mostly do not.
 *
 * `Type=Top Gap | Bottom Gap` stays out for the reason [CircularProgressKitCells] gives: the kit
 * does not publish the angles those cells draw.
 *
 * `Overflow` seeds `allowProgressOverflow` with it. The parameter defaults to false, which coerces
 * 1.4 into 1.0 — the overflow cell would have published the complete cell's picture.
 */
@OverrideVariant(
  name = "segments-1",
  ints = ["segmentCount=1"],
  kitAxis = "Segments",
  kitValue = "1",
)
@OverrideVariant(
  name = "segments-1-complete",
  ints = ["segmentCount=1"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=1"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-zero",
  ints = ["segmentCount=1"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=1"],
  kitProps =
    ["Type=Full", "Segments=1", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-1-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=1"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-2",
  ints = ["segmentCount=2"],
  kitAxis = "Segments",
  kitValue = "2",
)
@OverrideVariant(
  name = "segments-2-complete",
  ints = ["segmentCount=2"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=2", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-2-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=2"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=2", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-2-zero",
  ints = ["segmentCount=2"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=2", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-2-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=2"],
  kitProps =
    ["Type=Full", "Segments=2", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-2-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=2"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=2", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-3",
  ints = ["segmentCount=3"],
  kitAxis = "Segments",
  kitValue = "3",
)
@OverrideVariant(
  name = "segments-3-complete",
  ints = ["segmentCount=3"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=3", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-3-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=3"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=3", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-3-zero",
  ints = ["segmentCount=3"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=3", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-3-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=3"],
  kitProps =
    ["Type=Full", "Segments=3", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-3-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=3"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=3", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-4",
  ints = ["segmentCount=4"],
  kitAxis = "Segments",
  kitValue = "4",
)
@OverrideVariant(
  name = "segments-4-complete",
  ints = ["segmentCount=4"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=4", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-4-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=4"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=4", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-4-zero",
  ints = ["segmentCount=4"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=4", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-4-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=4"],
  kitProps =
    ["Type=Full", "Segments=4", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-4-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=4"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=4", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-5",
  ints = ["segmentCount=5"],
  kitAxis = "Segments",
  kitValue = "5",
)
@OverrideVariant(
  name = "segments-5-complete",
  ints = ["segmentCount=5"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=5", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-5-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=5"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=5", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-5-zero",
  ints = ["segmentCount=5"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=5", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-5-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=5"],
  kitProps =
    ["Type=Full", "Segments=5", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-5-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=5"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=5", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(
  name = "overflow",
  booleans = ["allowProgressOverflow=true"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=6", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "zero",
  floats = ["progress=0.0"],
  kitAxis = "Progress",
  kitValue = "Zero",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "zero-disabled",
  booleans = ["enabled=false"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=6", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-7",
  ints = ["segmentCount=7"],
  kitAxis = "Segments",
  kitValue = "7",
)
@OverrideVariant(
  name = "segments-7-complete",
  ints = ["segmentCount=7"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=7", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-7-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=7"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=7", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-7-zero",
  ints = ["segmentCount=7"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=7", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-7-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=7"],
  kitProps =
    ["Type=Full", "Segments=7", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-7-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=7"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=7", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-8",
  ints = ["segmentCount=8"],
  kitAxis = "Segments",
  kitValue = "8",
)
@OverrideVariant(
  name = "segments-8-complete",
  ints = ["segmentCount=8"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=8", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-8-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=8"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=8", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-8-zero",
  ints = ["segmentCount=8"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=8", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-8-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=8"],
  kitProps =
    ["Type=Full", "Segments=8", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-8-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=8"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=8", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-9",
  ints = ["segmentCount=9"],
  kitAxis = "Segments",
  kitValue = "9",
)
@OverrideVariant(
  name = "segments-9-complete",
  ints = ["segmentCount=9"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=9", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-9-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=9"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=9", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-9-zero",
  ints = ["segmentCount=9"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=9", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-9-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=9"],
  kitProps =
    ["Type=Full", "Segments=9", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-9-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=9"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=9", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-10",
  ints = ["segmentCount=10"],
  kitAxis = "Segments",
  kitValue = "10",
)
@OverrideVariant(
  name = "segments-10-complete",
  ints = ["segmentCount=10"],
  floats = ["progress=1.0"],
  kitProps =
    ["Type=Full", "Segments=10", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-10-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=10"],
  floats = ["progress=1.4"],
  kitProps =
    ["Type=Full", "Segments=10", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-10-zero",
  ints = ["segmentCount=10"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=10", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-10-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=10"],
  kitProps =
    ["Type=Full", "Segments=10", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-10-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=10"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=10", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-11",
  ints = ["segmentCount=11"],
  kitAxis = "Segments",
  kitValue = "11",
)
@OverrideVariant(
  name = "segments-11-complete",
  ints = ["segmentCount=11"],
  floats = ["progress=1.0"],
  kitProps =
    ["Type=Full", "Segments=11", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-11-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=11"],
  floats = ["progress=1.4"],
  kitProps =
    ["Type=Full", "Segments=11", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-11-zero",
  ints = ["segmentCount=11"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=11", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-11-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=11"],
  kitProps =
    ["Type=Full", "Segments=11", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-11-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=11"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=11", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-12",
  ints = ["segmentCount=12"],
  kitAxis = "Segments",
  kitValue = "12",
)
@OverrideVariant(
  name = "segments-12-complete",
  ints = ["segmentCount=12"],
  floats = ["progress=1.0"],
  kitProps =
    ["Type=Full", "Segments=12", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-12-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=12"],
  floats = ["progress=1.4"],
  kitProps =
    ["Type=Full", "Segments=12", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-12-zero",
  ints = ["segmentCount=12"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=12", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-12-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=12"],
  kitProps =
    ["Type=Full", "Segments=12", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-12-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=12"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=12", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-13",
  ints = ["segmentCount=13"],
  kitAxis = "Segments",
  kitValue = "13",
)
@OverrideVariant(
  name = "segments-13-complete",
  ints = ["segmentCount=13"],
  floats = ["progress=1.0"],
  kitProps =
    ["Type=Full", "Segments=13", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-13-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=13"],
  floats = ["progress=1.4"],
  kitProps =
    ["Type=Full", "Segments=13", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-13-zero",
  ints = ["segmentCount=13"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=13", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-13-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=13"],
  kitProps =
    ["Type=Full", "Segments=13", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-13-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=13"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=13", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-14",
  ints = ["segmentCount=14"],
  kitAxis = "Segments",
  kitValue = "14",
)
@OverrideVariant(
  name = "segments-14-complete",
  ints = ["segmentCount=14"],
  floats = ["progress=1.0"],
  kitProps =
    ["Type=Full", "Segments=14", "Stroke Width=Medium", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-14-overflow",
  booleans = ["allowProgressOverflow=true"],
  ints = ["segmentCount=14"],
  floats = ["progress=1.4"],
  kitProps =
    ["Type=Full", "Segments=14", "Stroke Width=Medium", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-14-zero",
  ints = ["segmentCount=14"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=14", "Stroke Width=Medium", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-14-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=14"],
  kitProps =
    ["Type=Full", "Segments=14", "Stroke Width=Medium", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-14-zero-disabled",
  booleans = ["enabled=false"],
  ints = ["segmentCount=14"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=14", "Stroke Width=Medium", "Progress=Zero", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-1-small-stroke",
  strings = ["stroke=small"],
  ints = ["segmentCount=1"],
  kitProps =
    ["Type=Full", "Segments=1", "Stroke Width=Small", "Progress=In progress", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-small-stroke-complete",
  strings = ["stroke=small"],
  ints = ["segmentCount=1"],
  floats = ["progress=1.0"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Small", "Progress=Complete", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-small-stroke-overflow",
  booleans = ["allowProgressOverflow=true"],
  strings = ["stroke=small"],
  ints = ["segmentCount=1"],
  floats = ["progress=1.4"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Small", "Progress=Overflow", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-small-stroke-zero",
  strings = ["stroke=small"],
  ints = ["segmentCount=1"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Small", "Progress=Zero", "Disabled=No"],
)
@OverrideVariant(
  name = "segments-1-small-stroke-disabled",
  booleans = ["enabled=false"],
  strings = ["stroke=small"],
  ints = ["segmentCount=1"],
  kitProps =
    ["Type=Full", "Segments=1", "Stroke Width=Small", "Progress=In progress", "Disabled=Yes"],
)
@OverrideVariant(
  name = "segments-1-small-stroke-zero-disabled",
  booleans = ["enabled=false"],
  strings = ["stroke=small"],
  ints = ["segmentCount=1"],
  floats = ["progress=0.0"],
  kitProps = ["Type=Full", "Segments=1", "Stroke Width=Small", "Progress=Zero", "Disabled=Yes"],
)
annotation class SegmentedProgressKitCells

@CatalogComponent(
  id = "SegmentedCircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/52431:57063",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/52431:56915",
  caption = "The same ring cut into segments, for progress through a countable set of steps.",
)
@CatalogModes
@SegmentedProgressKitCells
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
    // A CHOICE, not a `previewOverrideDp`, and it is spelled exactly as the round indicator above
    // spells it. The parameter is a `Dp` and the knob used to be a free one, but both of the kit's
    // `Stroke Width` values are library constants that resolve against the screen — they are not
    // numbers a cell can seed — so a cell for either had no way to name it.
    strokeWidth =
      if (previewOverrideChoice("stroke", "medium", listOf("medium", "small")) == "small")
        CircularProgressIndicatorDefaults.smallStrokeWidth
      else CircularProgressIndicatorDefaults.largeStrokeWidth,
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

/**
 * **Every `Progress-Indicator-Linear` cell this function draws** — four `Progress` values by two
 * `Size` values, 8 of the set's 16 nodes, against the five that were drawn
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * The other eight are the same eight again under `Context=In List`. Context is not a parameter: it
 * is where the caller puts the indicator, and the kit draws both because the surrounding row
 * changes what you see around it. A sticker of the component alone is the `In Component` half.
 */
@OverrideVariant(
  name = "min",
  floats = ["progress=0.0"],
  kitAxis = "Progress",
  kitValue = "Min",
)
@OverrideVariant(
  name = "20",
  floats = ["progress=0.2"],
  kitAxis = "Progress",
  kitValue = "20%",
)
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(
  name = "min-small",
  strings = ["size=small"],
  floats = ["progress=0.0"],
  kitProps = ["Progress=Min", "Context=In Component", "Size=Small"],
)
@OverrideVariant(
  name = "20-small",
  strings = ["size=small"],
  floats = ["progress=0.2"],
  kitProps = ["Progress=20%", "Context=In Component", "Size=Small"],
)
@OverrideVariant(
  name = "small",
  strings = ["size=small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(
  name = "complete-small",
  strings = ["size=small"],
  floats = ["progress=1.0"],
  kitProps = ["Progress=Complete", "Context=In Component", "Size=Small"],
)
annotation class LinearProgressKitCells

@CatalogComponent(
  id = "LinearProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45011:259221",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/45011:259051",
  caption = "A straight track, for progress inside a component or a list row.",
)
@CatalogModes
@LinearProgressKitCells
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
