@file:CatalogGroup(name = "Text", section = "Text")

package ee.schimke.wearm3catalog.sections

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
import ee.schimke.composeai.overrides.previewOverrideString
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

@CatalogComponent(
  id = "ListHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66978",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66977",
  caption = "Titles the screen a list belongs to; the first thing above the list.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun ListHeading() = Sticker {
  ListHeader(modifier = Modifier.width(180.dp)) {
    Text(
      kitCopy("label", KitCopy.TITLE),
      textAlign =
        if (previewOverrideString("align", "centre") == "left") TextAlign.Start
        else TextAlign.Center,
    )
  }
}

@CatalogComponent(
  id = "ListSubHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66983",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66982",
  caption = "Divides a long list into named runs, without leaving the screen's title behind.",
)
@CatalogModes
@OverrideVariant(name = "icon", booleans = ["icon=true"], kitAxis = "Icon", kitValue = "Yes")
@Composable
fun ListSubHeading() = Sticker {
  ListSubHeader(
    modifier = Modifier.width(180.dp),
    icon =
      if (previewOverrideBoolean("icon", false)) {
        { Icon(Icons.Filled.Add, contentDescription = null) }
      } else null,
    label = { Text(kitCopy("label", KitCopy.SUBTITLE)) },
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
    textAlign =
      if (previewOverrideString("align", "centre") == "left") TextAlign.Start else TextAlign.Center,
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
    textAlign =
      if (previewOverrideString("align", "centre") == "left") TextAlign.Start else TextAlign.Center,
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
