@file:CatalogGroup(name = "Text", section = "Text")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.timeTextCurvedText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Text` page: the two list headers, the two body roles, and the curved clock.
//
// `Text-Body` and `Text-Caption` are `Text` at a named role from the Wear type scale rather than
// two components — but they are two kit sets, and membership is the kit's call, so they are two
// stickers. What each names on the Compose side is a `MaterialTheme.typography` role, which is why
// the caption says which one: the sticker is otherwise indistinguishable from any other text.

/**
 * The kit's `Alignment=` axis, as the `textAlign` its three text sets take.
 *
 * A choice rather than a text box: `TextAlign` is a closed set, and a control that only shows
 * `centre` leaves the other values reachable only by someone who has read this file. `right` is
 * offered too — the kit publishes Left and Centre, but the parameter takes it and a live session is
 * where a reader tries the thing the kit did not draw.
 */
@Composable
private fun textAlign(): TextAlign =
  when (previewOverrideChoice("align", "centre", listOf("centre", "left", "right"))) {
    "left" -> TextAlign.Start
    "right" -> TextAlign.End
    else -> TextAlign.Center
  }

@CatalogComponent(
  id = "ListHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66978",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66977",
  caption = "Titles the screen a list belongs to; the first thing above the list.",
)
@CatalogModes
// The kit's other axis, `Type = Page-Top | Page-Mid`, is NOT a cell and cannot be one:
// `ListHeaderDefaults` publishes a single `contentPadding` and `ListHeader` takes no argument that
// distinguishes the first header on a screen from one further down a list. The two kit cells
// differ in the space above them, which is a property of where the header sits rather than of the
// component — so the set stays at two of its four cells, with the reason here rather than in a
// silence ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun ListHeading() = Sticker {
  ListHeader(modifier = Modifier.width(180.dp)) {
    Text(kitCopy("label", KitCopy.TITLE), textAlign = textAlign())
  }
}

@CatalogComponent(
  id = "ListSubHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66983",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66982",
  caption = "Divides a long list into named runs, without leaving the screen's title behind.",
)
@CatalogModes
// The kit publishes `Icon=Yes` at `Alignment=Left` only — the icon leads the row, so there is
// nothing for a centred variant of it to be — which is why the icon cell names one axis and the
// centred one names the pair.
@OverrideVariant(
  name = "icon",
  booleans = ["icon=true"],
  kitProps = ["Icon=Yes", "Alignment=Left"],
)
@OverrideVariant(
  name = "centred",
  strings = ["align=centre"],
  kitProps = ["Icon=No", "Alignment=Centre"],
)
@Composable
fun ListSubHeading() = Sticker {
  ListSubHeader(
    modifier = Modifier.width(180.dp),
    icon =
      if (previewOverrideBoolean("icon", false)) {
        { Icon(Icons.Filled.Add, contentDescription = null) }
      } else null,
    // The kit's `Alignment` axis, which is what the label does with the width it is given rather
    // than a parameter of the sub-header. `left` is the base cell here (a run of list rows starts
    // its text at the same place), so this knob's default differs from the shared `textAlign`
    // helper's — the header above it is centred and this one is not.
    label = {
      Text(
        kitCopy("label", KitCopy.SUBTITLE),
        modifier = Modifier.fillMaxWidth(),
        textAlign =
          if (previewOverrideChoice("align", "left", listOf("left", "centre")) == "centre")
            TextAlign.Center
          else TextAlign.Start,
      )
    },
  )
}

@CatalogComponent(
  id = "Text/Body",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66993",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66990",
  caption = "Running text at the `bodyMedium` role of the Wear type scale.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun BodyText() = Sticker {
  Text(
    kitCopy("text", KitCopy.BODY),
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier.width(160.dp),
    textAlign = textAlign(),
  )
}

@CatalogComponent(
  id = "Text/Caption",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66998",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66995",
  caption = "Secondary text at the `labelSmall` role — a timestamp, a unit, a footnote.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun CaptionText() = Sticker {
  Text(
    kitCopy("text", KitCopy.CAPTION),
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.width(160.dp),
    textAlign = textAlign(),
  )
}

// Pinned to 10:10, never the system clock. An unpinned clock would make every nightly render differ
// from the last, which turns the delivery branch's history into noise — and the strip is curved to
// the bezel, so it publishes on the round frame.
@CatalogComponent(
  id = "TimeText",
  reference = "figma:B24oss2tTeXAFykyeyusz0/48151:45209",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38973:10025",
  caption = "The curved status strip every Wear screen carries, pinned to a fixed time.",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "24-hour",
  strings = ["time=09:30"],
  kitAxis = "Type",
  kitValue = "24hr",
)
@Composable
fun FixedTimeText() = FullScreenSticker {
  val time = kitCopy("time", KitCopy.TIME_12H)
  TimeText { timeTextCurvedText(time) }
}
