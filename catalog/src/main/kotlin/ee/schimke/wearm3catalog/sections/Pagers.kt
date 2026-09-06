@file:CatalogGroup(name = "Pagers", section = "Navigation")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.PagerState
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AnimatedPage
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.VerticalPagerScaffold
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.ScreenSticker

// The pagers, and the hole they close: this sheet drew `PageIndicator/Horizontal` and
// `PageIndicator/Vertical` — two components whose whole job is to sit on the edge of a pager — and
// never the pager ([#310](https://github.com/yschimke/wear-m3-catalog/issues/310)). Three files
// imported `rememberPagerState` and every one of them used it to feed an indicator; nothing
// composed a pager. Two of this catalog's own source comments named the pager scaffolds as the
// thing that would normally place the furniture the indicator stickers hand-roll, which is a sheet
// documenting a workaround by pointing at a component it does not contain.
//
// THE SCAFFOLD IS THE CALL SITE, not the bare foundation pager. `HorizontalPager` and
// `VerticalPager` are `androidx.wear.compose.foundation`; what Material 3 publishes on top is
// `HorizontalPagerScaffold` / `VerticalPagerScaffold`, and those are what SUPPLY the indicator
// placement and coordinate it with `TimeText` — which is precisely the alignment `Indicators.kt`
// and `OneHandedGestures.kt` have to do by hand for want of one. A card that drew the foundation
// pager alone would document the layer this sheet's other components do not come from.
//
// SO IT DRAWS THE DOTS, and that is not an overlap with `PageIndicator/*`. The scaffold's
// `pageIndicator` parameter DEFAULTS to a `HorizontalPageIndicator` over this pager's state: the
// dots are part of what the component does, and a card that passed `pageIndicator = null` to keep
// them off somebody else's card would publish a picture of the scaffold nobody calls. What the two
// indicator cards own is the indicator's own axes — the kit's `Number` and `Position` cells, ten
// apiece — and neither of those is reachable from here.
//
// BOTH DIRECTIONS, because the two indicators are already two components for the same reason: the
// Wear library publishes two scaffolds and two pagers, and which bezel the furniture sits against
// follows from which one is called.
//
// DOOR 2, checked rather than assumed: `kit-sets.json`, `figma-kit-index.json`, `design-map.json`
// and `design-pages.json` contain no pager set, case-insensitively. `ButtonGroup` is the precedent
// and the reasoning transfers — a real library component the kit draws as instances of other sets
// rather than publishing as a set of its own.

/** The pages both pagers show, as the words each one carries. */
private val PAGER_PAGES = listOf("Today", "Week", "Month", "Year")

/**
 * A pager state that a live knob can actually move.
 *
 * `rememberPagerState` saves its state unkeyed — it reads `initialPage` once and ignores every
 * later value — so the knob would move nothing in a held Live composition. Keyed, it re-seeds. A
 * baked render never noticed, because each capture is a fresh composition; this is the same
 * `key(…)` the two page indicators take, and for the same reason (`Indicators.kt`).
 */
@Composable
internal fun pagerState(pages: Int, initialPage: Int): PagerState =
  key(pages, initialPage) {
    rememberPagerState(initialPage = initialPage.coerceIn(0, (pages - 1).coerceAtLeast(0))) {
      pages
    }
  }

/** One page: a word and its position in the run, big enough to read at a glance while paging. */
@Composable
internal fun PagerPage(page: Int, pages: Int) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(PAGER_PAGES[page % PAGER_PAGES.size], style = MaterialTheme.typography.titleLarge)
    Text("${page + 1} of $pages", style = MaterialTheme.typography.bodySmall)
  }
}

@CatalogComponent(
  id = "Pager/Horizontal",
  noReference =
    "The kit publishes no pager set — it draws paged screens as instances of its other sets with " +
      "the `Page-Indicator` cell on the edge, which is a layout an app makes rather than a " +
      "component the kit publishes. Wear Compose publishes the pager and its scaffold, and this " +
      "sheet already draws the indicator that only exists to sit on one.",
  caption = "Screens paged side to side, with the page dots the scaffold places along the bottom.",
  // The page transition — the scale and the scrim `AnimatedPage` runs as a page travels — is the
  // component, and every still is one page of it sitting still. See `Motion.PagerTransitionMotion`.
  motionPreview = "PagerTransitionMotion",
)
@CatalogFullScreenModes
@OverrideVariant(name = "first-page", ints = ["initialPage=0"])
@OverrideVariant(name = "last-page", ints = ["initialPage=3"])
@OverrideVariant(name = "two-pages", ints = ["pages=2", "initialPage=0"])
@SettledPreview
@Composable
fun HorizontalPagerScreen(pages: Int = 4, initialPage: Int = 1) = ScreenSticker {
  // `ScreenSticker`, so the pager gets the `AppScaffold` above it that the scaffold coordinates
  // with: `HorizontalPagerScaffold` shows and hides `TimeText` alongside the page indicator as the
  // pager is paged, and with no app scaffold there is no clock for it to coordinate.
  val state = pagerState(pages, initialPage)
  HorizontalPagerScaffold(pagerState = state) {
    HorizontalPager(state = state) { page ->
      // `AnimatedPage` is the Material 3 page wrapper, and it is what makes the transition the
      // recording is of: it scales the page down and scrims it as it travels, reading the position
      // off the same `PagerState`. Left out, the pages simply slide.
      AnimatedPage(pageIndex = page, pagerState = state) { PagerPage(page, pages) }
    }
  }
}

@CatalogComponent(
  id = "Pager/Vertical",
  noReference =
    "The same absence as `Pager/Horizontal`: the kit publishes no pager set at all, in either " +
      "orientation. Wear Compose publishes a second scaffold and a second pager, which is why " +
      "this is a second card rather than a cell.",
  caption = "The same paging up and down, with the dots on the right bezel instead.",
  motionPreview = "VerticalPagerTransitionMotion",
)
@CatalogFullScreenModes
@OverrideVariant(name = "first-page", ints = ["initialPage=0"])
@OverrideVariant(name = "last-page", ints = ["initialPage=3"])
@SettledPreview
@Composable
fun VerticalPagerScreen(pages: Int = 4, initialPage: Int = 1) = ScreenSticker {
  val state = pagerState(pages, initialPage)
  VerticalPagerScaffold(pagerState = state) {
    VerticalPager(state = state) { page ->
      AnimatedPage(pageIndex = page, pagerState = state) { PagerPage(page, pages) }
    }
  }
}
