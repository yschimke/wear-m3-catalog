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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.LevelIndicator
import androidx.wear.compose.material3.LevelIndicatorDefaults
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.VerticalPageIndicator
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideDp
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogTransparentScreenModes
import ee.schimke.wearm3catalog.TransparentScreenSticker

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
@CatalogTransparentScreenModes
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
fun ScrollRail() = TransparentScreenSticker {
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
  ScrollIndicator(
    state = state,
    reverseDirection = previewOverrideBoolean("reverseDirection", false),
    modifier = Modifier.align(Alignment.CenterEnd),
  )
}

@CatalogComponent(
  id = "LevelIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/46640:262811",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/46619:47146",
  caption = "The value a rotating side button is setting, while it is being turned.",
)
@CatalogTransparentScreenModes
@OverrideVariant(name = "low", floats = ["value=0.15"])
@OverrideVariant(name = "full", floats = ["value=1.0"])
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@Composable
fun LevelRail() = TransparentScreenSticker {
  // `value`, not `level`: the knob carries the name of the parameter it sets, so a reader of the
  // controls panel can find it in `LevelIndicator`'s signature. The kit calls the axis something
  // else on some sets and that word rides on the cell, not on the knob (see Sliders.kt).
  val value = previewOverrideFloat("value", 0.6f)
  LevelIndicator(
    value = { value },
    enabled = previewOverrideBoolean("enabled", true),
    // The kit's `Size = Default | Long | Short` axis is this angle in Compose — the arc is 72
    // degrees, a fifth of the circumference, by default. No cells for the other two: the kit does
    // not publish the angles they draw, and guessing one would put an invented number under the
    // kit's name.
    sweepAngle = previewOverrideFloat("sweepAngle", LevelIndicatorDefaults.SweepAngle),
    strokeWidth = previewOverrideDp("strokeWidth", LevelIndicatorDefaults.StrokeWidth),
    reverseDirection = previewOverrideBoolean("reverseDirection", false),
    modifier = Modifier.align(Alignment.CenterStart),
  )
}

/**
 * **Every `Page-Indicator` cell along the bottom** — the kit's `Number` axis at
 * `Position=Horizontal-Bottom`, ten nodes, against the two that were drawn
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * `Number` is two knobs, not one, past five pages: the kit draws `6` and `7+` three times each, for
 * a window sitting at the start, in the middle and at the end of the run, and which of those you
 * see is `initialPage` rather than the count. `7+` is eight pages here — the indicator collapses
 * identically past its maximum, and eight is far enough past it to show that.
 */
@OverrideVariant(
  name = "two-pages",
  ints = ["pages=2"],
  kitAxis = "Number",
  kitValue = "2",
)
@OverrideVariant(
  name = "three-pages",
  ints = ["pages=3"],
  kitAxis = "Number",
  kitValue = "3",
)
@OverrideVariant(
  name = "five-pages",
  ints = ["pages=5"],
  kitAxis = "Number",
  kitValue = "5",
)
@OverrideVariant(
  name = "six-pages",
  ints = ["pages=6"],
  kitAxis = "Number",
  kitValue = "6 - Start",
)
@OverrideVariant(
  name = "six-pages-middle",
  ints = ["pages=6", "initialPage=3"],
  kitProps = ["Number=6  - MiddleEnd", "Position=Horizontal-Bottom"],
  secondary = true,
)
@OverrideVariant(
  name = "six-pages-end",
  ints = ["pages=6", "initialPage=5"],
  kitProps = ["Number=6 - End", "Position=Horizontal-Bottom"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages",
  ints = ["pages=8"],
  kitProps = ["Number=7+ - Start", "Position=Horizontal-Bottom"],
)
@OverrideVariant(
  name = "many-pages-middle",
  ints = ["pages=8", "initialPage=4"],
  kitProps = ["Number=7+  - MiddleEnd", "Position=Horizontal-Bottom"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages-end",
  ints = ["pages=8", "initialPage=7"],
  kitProps = ["Number=7+ - End", "Position=Horizontal-Bottom"],
  secondary = true,
)
annotation class HorizontalPageKitCells

@CatalogComponent(
  id = "PageIndicator/Horizontal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:138301",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Which page of a horizontal pager is showing, along the bottom of the display.",
)
@CatalogTransparentScreenModes
@HorizontalPageKitCells
@Composable
fun HorizontalPages() = TransparentScreenSticker {
  val pages = previewOverrideInt("pages", 4)
  // Which page is showing is the other half of what this component draws, and it was pinned to the
  // first one — so the kit's `6 - Start` / `6 - End` / `6 - MiddleEnd` positions were unreachable
  // from the controls. Coerced into the page count: a live knob can outrun it, and the pager
  // throws rather than rendering when it does.
  val initialPage = previewOverrideInt("initialPage", 0).coerceIn(0, (pages - 1).coerceAtLeast(0))
  HorizontalPageIndicator(
    // `key(…)`, because `rememberPagerState` saves its state unkeyed: it reads `initialPage` once
    // and ignores every later value, so the knob would move nothing in a live session. A baked
    // render never noticed — each capture is a fresh composition.
    pagerState =
      key(pages, initialPage) { rememberPagerState(initialPage = initialPage) { pages } },
    modifier = Modifier.align(Alignment.BottomCenter),
  )
}

/**
 * The same ten cells at `Position=Vertical-Right`, for the vertical pager.
 *
 * The kit's third column, `Vertical-Left`, is not drawn: which bezel the rail sits against is the
 * caller's `Alignment` (or the layout direction), not a parameter of `VerticalPageIndicator` — see
 * the placement note at the top of this file. A cell for it would be a picture of the sticker's own
 * layout under the kit's name.
 */
@OverrideVariant(
  name = "two-pages",
  ints = ["pages=2"],
  kitAxis = "Number",
  kitValue = "2",
)
@OverrideVariant(
  name = "three-pages",
  ints = ["pages=3"],
  kitAxis = "Number",
  kitValue = "3",
)
@OverrideVariant(
  name = "five-pages",
  ints = ["pages=5"],
  kitAxis = "Number",
  kitValue = "5",
)
@OverrideVariant(
  name = "six-pages",
  ints = ["pages=6"],
  kitAxis = "Number",
  kitValue = "6 - Start",
)
@OverrideVariant(
  name = "six-pages-middle",
  ints = ["pages=6", "initialPage=3"],
  kitProps = ["Number=6  - MiddleEnd", "Position=Vertical-Right"],
  secondary = true,
)
@OverrideVariant(
  name = "six-pages-end",
  ints = ["pages=6", "initialPage=5"],
  kitProps = ["Number=6 - End", "Position=Vertical-Right"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages",
  ints = ["pages=8"],
  kitProps = ["Number=7+ - Start", "Position=Vertical-Right"],
)
@OverrideVariant(
  name = "many-pages-middle",
  ints = ["pages=8", "initialPage=4"],
  kitProps = ["Number=7+  - MiddleEnd", "Position=Vertical-Right"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages-end",
  ints = ["pages=8", "initialPage=7"],
  kitProps = ["Number=7+ - End", "Position=Vertical-Right"],
  secondary = true,
)
annotation class VerticalPageKitCells

@CatalogComponent(
  id = "PageIndicator/Vertical",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38966:402",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "The same indicator for a vertical pager, drawn against the right bezel.",
)
@CatalogTransparentScreenModes
@VerticalPageKitCells
@Composable
fun VerticalPages() = TransparentScreenSticker {
  val pages = previewOverrideInt("pages", 4)
  val initialPage = previewOverrideInt("initialPage", 0).coerceIn(0, (pages - 1).coerceAtLeast(0))
  VerticalPageIndicator(
    pagerState =
      key(pages, initialPage) { rememberPagerState(initialPage = initialPage) { pages } },
    modifier = Modifier.align(Alignment.CenterEnd),
  )
}
