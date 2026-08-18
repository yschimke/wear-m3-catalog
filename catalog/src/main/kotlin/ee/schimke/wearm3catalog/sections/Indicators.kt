@file:CatalogGroup(name = "Position indicators", section = "Navigation")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.LevelIndicator
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.VerticalPageIndicator
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.FullScreenSticker

// The kit's `Position Indicators` and `Page Indicators` pages. Every one of these is a curved rail
// positioned against the bezel — it has no size of its own to be cropped to — so they publish on
// the
// round frame rather than as wrap-and-crop stickers.
//
// The scroll indicator is driven by a real `ScrollState` seeded to the position each cell names,
// rather than by drawing a rail at a fraction: the component decides where the thumb goes from the
// state, and a sticker that bypassed that would be a picture of a rail rather than of the
// component.

@CatalogComponent(
  id = "ScrollIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/44998:18342",
  caption = "Where the screen sits in a scroll, drawn against the right bezel.",
)
@CatalogModes
@OverrideVariant(
  name = "middle",
  floats = ["position=0.5"],
  kitAxis = "Position",
  kitValue = "Middle",
)
@OverrideVariant(
  name = "bottom",
  floats = ["position=1.0"],
  kitAxis = "Position",
  kitValue = "Bottom",
)
@Composable
fun ScrollRail() = FullScreenSticker {
  val extent = 1000
  val state = rememberScrollState(initial = (previewOverrideFloat("position", 0f) * extent).toInt())
  ScrollIndicator(state = state)
}

@CatalogComponent(
  id = "LevelIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/46619:47146",
  caption = "The value a rotating side button is setting, while it is being turned.",
)
@CatalogModes
@OverrideVariant(name = "low", floats = ["level=0.15"])
@OverrideVariant(name = "full", floats = ["level=1.0"])
@Composable
fun LevelRail() = FullScreenSticker {
  val level = previewOverrideFloat("level", 0.6f)
  LevelIndicator(value = { level })
}

@CatalogComponent(
  id = "PageIndicator/Horizontal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Which page of a horizontal pager is showing, along the bottom of the display.",
)
@CatalogModes
@OverrideVariant(name = "six-pages", ints = ["pages=6"], kitAxis = "Number", kitValue = "6 - Start")
@OverrideVariant(name = "two-pages", ints = ["pages=2"], kitAxis = "Number", kitValue = "2")
@Composable
fun HorizontalPages() = FullScreenSticker {
  val pages = previewOverrideInt("pages", 4)
  HorizontalPageIndicator(pagerState = rememberPagerState(initialPage = 0) { pages })
}

@CatalogComponent(
  id = "PageIndicator/Vertical",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "The same indicator for a vertical pager, drawn against the left bezel.",
)
@CatalogModes
@OverrideVariant(name = "six-pages", ints = ["pages=6"], kitAxis = "Number", kitValue = "6 - Start")
@Composable
fun VerticalPages() = FullScreenSticker {
  val pages = previewOverrideInt("pages", 4)
  VerticalPageIndicator(pagerState = rememberPagerState(initialPage = 0) { pages })
}
