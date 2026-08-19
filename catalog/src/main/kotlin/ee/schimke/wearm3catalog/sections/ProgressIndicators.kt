@file:CatalogGroup(name = "Progress indicators", section = "Communication")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ArcProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.LinearProgressIndicator
import androidx.wear.compose.material3.LinearProgressIndicatorDefaults
import androidx.wear.compose.material3.SegmentedCircularProgressIndicator
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.overrides.previewOverrideString
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

@CatalogComponent(
  id = "CircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41424:58385",
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
    if (previewOverrideString("stroke", "medium") == "small")
      CircularProgressIndicatorDefaults.smallStrokeWidth
    else CircularProgressIndicatorDefaults.largeStrokeWidth
  if (previewOverrideString("mode", "determinate") == "indeterminate") {
    CircularProgressIndicator(modifier = Modifier.size(120.dp), strokeWidth = stroke)
  } else {
    CircularProgressIndicator(
      progress = { progress },
      modifier = Modifier.size(120.dp),
      enabled = previewOverrideBoolean("enabled", true),
      allowProgressOverflow = true,
      strokeWidth = stroke,
    )
  }
}

@CatalogComponent(
  id = "SegmentedCircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/52431:56915",
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
@OverrideVariant(name = "twelve", ints = ["segments=12"], kitAxis = "Segments", kitValue = "12")
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun SegmentedProgress() = Sticker {
  val progress = previewOverrideFloat("progress", 0.6f)
  SegmentedCircularProgressIndicator(
    segmentCount = previewOverrideInt("segments", 6),
    progress = { progress },
    modifier = Modifier.size(120.dp),
    enabled = previewOverrideBoolean("enabled", true),
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
fun ArcProgress() = Sticker { ArcProgressIndicator(modifier = Modifier.size(120.dp)) }

@CatalogComponent(
  id = "LinearProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45011:259051",
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
    strokeWidth =
      if (previewOverrideString("size", "large") == "small")
        LinearProgressIndicatorDefaults.StrokeWidthSmall
      else LinearProgressIndicatorDefaults.StrokeWidthLarge,
  )
}
