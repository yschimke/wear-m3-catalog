@file:CatalogGroup(name = "Swipe to dismiss", section = "Navigation")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.SwipeToDismissValue
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwipeToDismissBox
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.FullScreenSticker

// `SwipeToDismissBox` — the Wear back gesture, and the component this sheet was missing while
// carrying the superficially similar `SwipeToReveal` twice
// ([#314](https://github.com/yschimke/wear-m3-catalog/issues/314)).
//
// IT IS NOT A VARIANT OF SWIPE-TO-REVEAL, and the adjacency on the sheet is exactly why the caption
// says so. Both are a horizontal drag over a full-width thing, and there the resemblance ends:
// swipe-to-reveal uncovers ACTIONS behind an item and springs back, while this one takes the whole
// SCREEN off to the right and hands the app an `onDismissed` — it is how a user leaves a screen on
// this platform, which is navigation rather than containment. Hence its own group under
// `Navigation` rather than a cell of `SwipeToReveal`.
//
// ONE CARD FOR TWO OVERLOADS. The library publishes a state-taking form and a convenience form that
// takes `onDismissed` and remembers its own state; they are the same component under the call-site
// test (AGENTS.md), so one card. The state-taking one is what this draws, because it is the only
// one whose position a capture can seed — see below.
//
// THE KIT'S DOOR IS SHUT HERE, and it was checked rather than assumed: `kit-sets.json` lists every
// set the kit publishes and there is no swipe-to-dismiss among them. The kit draws the STR sets
// because those are an item's own two states; a back gesture is a transition between two screens,
// which is not a component the kit could draw a cell of.
//
// WHY THERE IS NO RECORDING, and this one is a real limitation with a name rather than a
// preference.
// `Motion.kt` records swipe-to-reveal by driving the component's own `RevealState.animateTo`, so
// the spring and the anchors are the component's and only the finger is scripted.
// `SwipeToDismissBoxState`
// publishes no such door: its only mutator is `snapTo(Default | Dismissed)`, which teleports
// between the two anchors with no animation at all, and the offset the box actually draws from
// lives on an internal `SwipeableV2State`. A recording built out of `snapTo` would be a two-frame
// jump cut asserting a transition the component never played. So the card publishes the two ends as
// stills and says here what is between them; a drag gesture in `@InteractionPreview` would convert
// this the same day it converts the two drags `Motion.kt` already names.

/** Which of the box's two anchors the capture is seeded at. */
enum class DismissPosition {
  @KnobValue("at-rest") AtRest,
  @KnobValue("dismissed") Dismissed,
}

@CatalogComponent(
  id = "SwipeToDismissBox",
  noReference =
    "The kit publishes no swipe-to-dismiss set. Its `STR-card` and `STR-button` sets draw an " +
      "item's own revealed state; the back gesture is a transition between two screens, which " +
      "the kit has no cell for. Wear Compose publishes it as the component that implements the " +
      "platform's primary navigation affordance.",
  caption =
    "Wear's back gesture: the screen slides off to the right and uncovers what is behind it — " +
      "not `SwipeToReveal`, which uncovers an item's actions and springs back.",
)
@CatalogFullScreenModes
// The far anchor, which is the only other position the public state can be put in. It is worth a
// cell rather than a knob alone: it is where the background slot — the half of the component a
// still at rest cannot show at all, because the box does not even compose it until the swipe starts
// — becomes visible.
@OverrideVariant(name = "dismissed", strings = ["position=dismissed"])
// The gesture is dispatched by the box, and the seed above is applied by a `LaunchedEffect` that
// has to wait for `BasicSwipeToDismissBox`'s own `SideEffect` to publish its anchors. Settled, the
// capture is taken after that has landed; unsettled, `dismissed` renders the resting frame.
@SettledPreview
@Composable
fun DismissableScreen(position: DismissPosition = DismissPosition.AtRest) = FullScreenSticker {
  val state = rememberSwipeToDismissBoxState()
  LaunchedEffect(position) {
    state.snapTo(
      when (position) {
        DismissPosition.Dismissed -> SwipeToDismissValue.Dismissed
        DismissPosition.AtRest -> SwipeToDismissValue.Default
      }
    )
  }
  SwipeToDismissBox(state = state, modifier = Modifier.fillMaxSize()) { isBackground ->
    // ONE lambda for both layers, which is the component's shape rather than this sticker's: the
    // box calls its content twice — once as the background, once as the foreground — so that any
    // state remembered inside it survives the screen becoming the other one. Two visibly different
    // screens is what makes the capture legible: at rest only the foreground exists, and the
    // `dismissed` cell is the same frame with the foreground gone.
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        if (isBackground) "Activity" else "Today's run",
        style =
          if (isBackground) MaterialTheme.typography.titleMedium
          else MaterialTheme.typography.titleLarge,
      )
      Text(
        if (isBackground) "The screen behind" else "Swipe right to go back",
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}
