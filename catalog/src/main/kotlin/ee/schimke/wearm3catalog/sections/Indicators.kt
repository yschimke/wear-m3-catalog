@file:CatalogGroup(name = "Position indicators", section = "Navigation")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.FullScreenSticker

// The kit's `Position Indicators` and `Page Indicators` pages. Every one of these is a curved rail
// positioned against the bezel — it has no size of its own to be cropped to — so they publish on
// the
// round frame rather than as wrap-and-crop stickers.
//
// **Every rail here must carry its own `Modifier.align(…)`.** These components lay out as a small
// box — just the arc's bounding box — but draw their arc on a circle the size of the *screen*,
// centred on that box. So the box has to be sitting where the component assumes it is, or the arc
// is struck from the wrong centre and comes out as a diagonal stroke half of which is off the
// frame. `ScreenScaffold` and the pager scaffolds supply the alignment when these are used as a
// screen's furniture; here there is no scaffold, so the sticker supplies it:
//
//  - `ScrollIndicator` and `VerticalPageIndicator` → `Alignment.CenterEnd` (right bezel in Ltr).
//  - `LevelIndicator` → `Alignment.CenterStart`. It is the odd one out: it passes `rsbSide = false`
//    internally, so it draws on the *left* in Ltr, and unlike the page indicators its KDoc names no
//    alignment at all — which is how it shipped here unaligned and rendered as issue #18.
//  - `HorizontalPageIndicator` → `Alignment.BottomCenter`.
//
// Use `CenterStart`/`CenterEnd` rather than a hardcoded side: they flip under Rtl exactly as the
// components' own `indicatorOnTheRight` does.
//
// The scroll indicator is driven by a real `ScrollState` seeded to the position each cell names,
// rather than by drawing a rail at a fraction: the component decides where the thumb goes from the
// state, and a sticker that bypassed that would be a picture of a rail rather than of the
// component.

@CatalogComponent(
  id = "ScrollIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/44998:18343",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/44998:18342",
  caption = "Where the screen sits in a scroll, drawn against the right bezel.",
)
@CatalogFullScreenModes
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
  // SCROLLABLE, BUT EMPTY — and it has to be both.
  //
  // Scrollable, because a `ScrollState` seeded to an offset it cannot reach reports position zero:
  // with nothing scrollable its maximum is 0, so every cell drew the thumb at the top and published
  // three identical pictures under three names. The column overflows several screens, so the
  // seeded offsets land where they claim to.
  //
  // Empty, because the kit's `Scroll-Indicator` cells are a bare black display with the rail on the
  // bezel and nothing else in the frame. This column used to carry twenty numbered rows, and they
  // were the loudest thing in the comparison: a screenful of text diffed against a blank one, with
  // the rail — the only thing either side is a picture OF — a few pixels wide at the edge. Spacers
  // scroll exactly as well as labels do and leave the component alone in the frame.
  val state = rememberScrollState()
  val position = previewOverrideFloat("position", 0f)
  Column(Modifier.fillMaxSize().verticalScroll(state)) {
    repeat(20) { Spacer(Modifier.height(40.dp)) }
  }
  LaunchedEffect(position) { state.scrollTo((state.maxValue * position).toInt()) }
  ScrollIndicator(state = state, modifier = Modifier.align(Alignment.CenterEnd))
}

@CatalogComponent(
  id = "LevelIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/46640:262811",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/46619:47146",
  caption = "The value a rotating side button is setting, while it is being turned.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "low", floats = ["level=0.15"])
@OverrideVariant(name = "full", floats = ["level=1.0"])
@Composable
fun LevelRail() = FullScreenSticker {
  val level = previewOverrideFloat("level", 0.6f)
  LevelIndicator(value = { level }, modifier = Modifier.align(Alignment.CenterStart))
}

@CatalogComponent(
  id = "PageIndicator/Horizontal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:138301",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Which page of a horizontal pager is showing, along the bottom of the display.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "six-pages", ints = ["pages=6"], kitAxis = "Number", kitValue = "6 - Start")
@OverrideVariant(name = "two-pages", ints = ["pages=2"], kitAxis = "Number", kitValue = "2")
@Composable
fun HorizontalPages() = FullScreenSticker {
  val pages = previewOverrideInt("pages", 4)
  HorizontalPageIndicator(
    pagerState = rememberPagerState(initialPage = 0) { pages },
    modifier = Modifier.align(Alignment.BottomCenter),
  )
}

@CatalogComponent(
  id = "PageIndicator/Vertical",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38966:402",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "The same indicator for a vertical pager, drawn against the right bezel.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "six-pages", ints = ["pages=6"], kitAxis = "Number", kitValue = "6 - Start")
@Composable
fun VerticalPages() = FullScreenSticker {
  val pages = previewOverrideInt("pages", 4)
  VerticalPageIndicator(
    pagerState = rememberPagerState(initialPage = 0) { pages },
    modifier = Modifier.align(Alignment.CenterEnd),
  )
}
