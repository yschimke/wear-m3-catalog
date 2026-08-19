@file:CatalogGroup(name = "Lists and screens", section = "Layout")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.timeTextCurvedText
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes

// The screen-shaped half of Wear Material 3, which the kit does not publish and a reader of the
// component set still has to call. The kit's pages are *component* sheets — its screens live on the
// Stickersheet page as a poster rather than as sets — so these enter through the library's door
// with the reason stated (AGENTS.md).
//
// TWO CARDS, NOT NINE
//
// The obvious way to publish these is one card per screen size, which is what the code-led
// `wear-m3`
// catalog does: `Template/TimeText/smallRound`, `/largeRound`, `/xlRound`, and again for the page
// indicator and the edge button — fifteen cards for five components. This catalog folds instead. A
// screen size is an argument to the same composable, not a different one, so it is a cell; and the
// scaffold's optional furniture is a cell too, which is what turns three "templates" into one
// `Scaffold` with variants.

@CatalogComponent(
  id = "TransformingLazyColumn",
  noReference =
    "The kit publishes no list set — its list pages are the row components (list header, cards) " +
      "rather than the scrolling container. This is the Wear Compose list idiom, and the " +
      "transformation that scales and fades rows against the bezel is only visible on the real one.",
  caption = "The Wear list: rows scale and fade toward the edges of the round display.",
)
@CatalogFullScreenModes
@ScrollingPreview(modes = [ScrollMode.END])
@Composable
fun WearList() = WearScreen {
  val state = rememberTransformingLazyColumnState()
  val spec = rememberTransformationSpec()
  ScreenScaffold(scrollState = state) { padding ->
    TransformingLazyColumn(
      state = state,
      contentPadding = padding,
      modifier = Modifier.fillMaxSize(),
    ) {
      item {
        ListHeader(
          modifier = Modifier.transformedHeight(this, spec),
          transformation = SurfaceTransformation(spec),
        ) {
          Text("Activity")
        }
      }
      items(6) { index ->
        TitleCard(
          onClick = {},
          title = { Text("Session ${index + 1}") },
          subtitle = { Text("${(index + 1) * 4} min") },
          modifier = Modifier.transformedHeight(this, spec),
          transformation = SurfaceTransformation(spec),
        )
      }
    }
  }
}

@CatalogComponent(
  id = "Scaffold",
  noReference =
    "The kit publishes no scaffold: the chrome it arranges — the curved clock, the scroll " +
      "indicator, the edge button — are separate sets, and the thing that places them is Wear " +
      "Compose's, not the kit's.",
  caption =
    "The screen frame: curved clock, scroll position, and the bottom action, in their places.",
)
@CatalogFullScreenModes
// One cell, and it is a real difference: `scrollIndicator = null` is a screen that does not show
// where it sits. An `edge-button` cell was authored here first and dropped — the edge button is
// revealed by the scroll state, so at the resting top it is collapsed and the cell rendered
// byte-identically to the default. The edge button is published as its own component, which
// carries the scroll-to-end capture that actually shows it.
@OverrideVariant(name = "no-scroll-indicator", strings = ["chrome=bare"])
@Composable
fun WearScaffold() = WearScreen {
  val state = rememberTransformingLazyColumnState()
  val bare = previewOverrideChoice("chrome", "scroll", listOf("scroll", "bare")) == "bare"
  val body: @Composable BoxScope.(PaddingValues) -> Unit = { padding ->
    TransformingLazyColumn(
      state = state,
      contentPadding = padding,
      modifier = Modifier.fillMaxSize(),
    ) {
      items(4) { index -> ListHeader { Text("Row ${index + 1}") } }
    }
  }
  if (bare) {
    ScreenScaffold(scrollState = state, scrollIndicator = null, content = body)
  } else {
    ScreenScaffold(scrollState = state, content = body)
  }
}

/** The screen frame these publish inside: the dark theme plus the curved clock every screen has. */
@Composable
private fun WearScreen(content: @Composable () -> Unit) {
  MaterialTheme {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) { content() }
  }
}
