@file:CatalogGroup(name = "One-handed gestures", section = "Communication")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.onehandedgesture.LocalOneHandedGestureEnabled
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureClickIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureClickIndicatorState
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureConfiguration
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureDefaults
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureHorizontalPageIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureIndicatorSize
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePageIndicatorState
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePriority
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureScrollIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureScrollIndicatorState
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureVerticalPageIndicator
import androidx.wear.compose.material3.onehandedgesture.oneHandedGesture
import androidx.wear.compose.material3.onehandedgesture.rememberOneHandedGestureConfiguration
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.CatalogTransparentScreenModes
import ee.schimke.wearm3catalog.GestureActivation
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.TransparentScreenSticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.kitRowWidth

// ONE-HANDED GESTURES — `androidx.wear.compose.material3.onehandedgesture`, new in wear-compose
// 1.7.0-beta.
//
// A Pixel Watch 3 can be driven without the other hand: a double pinch is the *primary* action, a
// wrist turn is *dismiss*. `Modifier.oneHandedGesture` is how a composable claims one, and the
// three indicators here are how the system tells a wearer that the claim exists — an animated
// glyph that takes the button's label over for a beat, or grows out of the scroll rail or the page
// dots.
//
// THE CARDS ARE THE INDICATORS, AND THE MODIFIER RIDES ALONG
//
// `Modifier.oneHandedGesture` is the other half of the API and it draws nothing, so there is no
// card for it: a modifier's sticker would be a picture of whatever it was attached to. Every
// sticker below applies the real modifier anyway, because the indicator is meaningless without the
// registration it indicates — the pairing is the component.
//
// DOOR 2, and the kit really has nothing here. The gesture pattern is published as a *design
// guide* (developer.android.com/design/ui/wear/guides/patterns/gestures), not as a component set:
// there is no `Gesture-Indicator` frame in the kit walk, so `kit-sets.json` grows no row and these
// enter with `noReference` (AGENTS.md).
//
// FILED UNDER COMMUNICATION, a judgement rather than a kit page. These are not controls —
// a wearer cannot press one — they are the system saying "this is reachable without your other
// hand", which is the same job the placeholder and the progress indicators in that section do. The
// alternative was splitting them across Actions (the button) and Navigation (the rail and the
// dots), and a reader looking for one-handed gestures would then have to know the taxonomy before
// they could find them.
//
// HOW THE HINT IS MADE TO SHOW, AND WHY IT IS NOT A FAKE
//
// On a watch the framework raises the hint: `Modifier.oneHandedGesture` registers with the
// on-device `GestureInputManager`, which decides when a wearer needs reminding and calls back
// `onGestureAvailable`, and the app's job is to call `showIndicator()` from there. Off a watch
// that manager is absent, the registration is inert, and a preview of a gesture-aware screen shows
// nothing at all.
//
// So these stickers call the same **public** `showIndicator()` the callback would, from a
// `LaunchedEffect`. Nothing else is stubbed: the configuration, the modifier, the indicator and
// its state are the library's, and what the render shows is the library's own animation, not a
// drawing of it. Only the *cause* is scripted — which is the same bargain `Motion.kt` strikes for
// swipe-to-reveal, and it is forced by the same thing: the cause is not a pointer, so
// `@InteractionPreview` cannot dispatch it. There is a zero-source-change alternative upstream
// (`@GestureHintPreview` shadows the SDK bridge so the framework itself raises the hint), and it is
// deliberately not used here: it needs `:data-gestures-connector` on the render classpath and it
// is one flag per FUNCTION, so the `gestures-off` cell below — which is a fact about
// `LocalOneHandedGestureEnabled`, not about the renderer — could not be a cell at all.
//
// `@SettledPreview(afterMs = 800)` is what makes the still deterministic. The sequence is a 450ms
// lead-in, then the animated vector, then a spring back to the content, so it BEGINS and ENDS on
// the resting picture: an auto settle would walk past the whole thing and publish the plain button
// every time. 800ms is a phase pin — 350ms into the glyph — and the renderer records it as one.

/**
 * The gesture whose glyph the sticker draws. Primary is the double pinch, Dismiss the wrist turn.
 */
@Composable
private fun gestureAction(): OneHandedGestureAction =
  when (previewOverrideChoice("action", "primary", listOf("primary", "dismiss"))) {
    "dismiss" -> OneHandedGestureAction.Dismiss
    else -> OneHandedGestureAction.Primary
  }

/**
 * Whether this render's wearer would be shown the hint at all.
 *
 * `LocalOneHandedGestureEnabled` is the app's own opt-out — a screen where a stray pinch would do
 * something unrecoverable turns it off — and it stops the *registration*, not the indicator. Since
 * the stickers raise the hint themselves (see the note at the top of this file), the driver has to
 * read the same flag the framework would, or the `gestures-off` cell would publish a hint the
 * library would never have raised.
 */
@Composable private fun gesturesEnabled(): Boolean = previewOverrideBoolean("gesturesEnabled", true)

/**
 * The gesture configuration every sticker registers under, keyed on the axes a cell can turn.
 *
 * [rememberOneHandedGestureConfiguration] derives its `gestureId` from the composite key when it is
 * given none, and a stale one would leave a cell registered under the previous cell's identity.
 */
@Composable
private fun rememberGestureConfiguration(
  action: OneHandedGestureAction,
  priority: OneHandedGesturePriority = OneHandedGesturePriority.Unspecified,
): OneHandedGestureConfiguration =
  rememberOneHandedGestureConfiguration(action = action, priority = priority)

/** The kit's indicator-size axis for the one-handed gesture glyph. */
enum class GestureIndicatorSize {
  @KnobValue("medium") Medium,
  @KnobValue("small") Small,
}

@CatalogComponent(
  id = "OneHandedGestureClickIndicator",
  noReference =
    "The kit publishes no gesture set — one-handed gestures are a design GUIDE " +
      "(design/ui/wear/guides/patterns/gestures) rather than a component sheet, so there is no " +
      "frame to compare against. The component is real Wear Compose API, new in 1.7.0-beta.",
  caption =
    "A button announcing that a double pinch will press it: the label steps aside for the glyph.",
  motionPreview = "GestureHintMotion",
)
@CatalogModes
// Each cell turns one knob, so all three are primary: which gesture is claimed, how big the glyph
// is drawn, and whether the app has switched gestures off for this screen at all.
@OverrideVariant(name = "dismiss", strings = ["action=dismiss"])
@OverrideVariant(name = "small-indicator", strings = ["indicatorSize=small"])
@OverrideVariant(name = "gestures-off", booleans = ["gesturesEnabled=false"])
@SettledPreview(afterMs = 800)
@Composable
fun GestureClickIndicator(indicatorSize: GestureIndicatorSize = GestureIndicatorSize.Medium) =
  Sticker {
    val enabled = gesturesEnabled()
    // A COLUMN, not the frame's bare `Box`, because the activation button below the component has
    // to
    // sit under it rather than over it. In the baked lane the button is absent and the column wraps
    // to exactly the button's bounds, so the published capture is unchanged — verified by hash.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CompositionLocalProvider(LocalOneHandedGestureEnabled provides enabled) {
        val action = gestureAction()
        val configuration = rememberGestureConfiguration(action, OneHandedGesturePriority.Clickable)
        val interactionSource = remember { MutableInteractionSource() }
        // Keyed on the configuration: a cell that changes the action registers a new indicator, and
        // a
        // state left over from the old one would animate under the wrong glyph.
        val indicatorState = remember(configuration) { OneHandedGestureClickIndicatorState() }
        val size =
          when (indicatorSize) {
            GestureIndicatorSize.Small -> OneHandedGestureIndicatorSize.Small
            GestureIndicatorSize.Medium -> OneHandedGestureIndicatorSize.Medium
          }
        LaunchedEffect(indicatorState, enabled) { if (enabled) indicatorState.showIndicator() }
        // The gesture PRESSES THE BUTTON, as the AndroidX sample's does. `onGesture = {}` was a
        // component that announced an action and then had none: the glyph plays, the wearer
        // pinches,
        // and nothing anywhere changes. Sharing one lambda with `onClick` is also the point — the
        // two
        // routes to the same button must not be able to drift.
        val press = counted(kitCopy("label", KitCopy.PRIMARY_LABEL))
        Button(
          onClick = press.onClick,
          interactionSource = interactionSource,
          modifier =
            Modifier.kitRowWidth()
              .oneHandedGesture(
                gestureConfiguration = configuration,
                interactionSource = interactionSource,
                onGestureLabel = "press the button",
                onGesture = { press.onClick() },
              ),
        ) {
          // `fillMaxWidth()` on the LABEL, as the AndroidX sample has it. The indicator is a
          // `Layout`
          // that measures the content and centres the glyph over it, so a label that hugs its text
          // pulls the glyph off-centre with it — visible on the recording in `Motion.kt` before
          // this
          // was here, where the hand sat over the first two words rather than in the button.
          OneHandedGestureClickIndicator(
            configuration,
            indicatorState,
            gestureIndicatorSize = size,
          ) {
            Text(press.label, modifier = Modifier.fillMaxWidth())
          }
        }
        // Live lane only, and only where there is no wrist to do it — see `CatalogGestures.kt`.
        GestureActivation(action, onGesture = { press.onClick() })
      }
    }
  }

@CatalogComponent(
  id = "OneHandedGestureScrollIndicator",
  noReference =
    "The kit's `Scroll-Indicator` set draws the rail alone; the gesture layer over it — the " +
      "pill, the glyph and the jiggle that says a pinch will scroll — has no cell in the kit, " +
      "which publishes one-handed gestures as a design guide rather than as a component sheet.",
  caption = "The scroll rail growing a glyph: a pinch scrolls the list without the other hand.",
  motionPreview = "GestureScrollHintMotion",
)
@CatalogTransparentScreenModes
@OverrideVariant(name = "gestures-off", booleans = ["gesturesEnabled=false"])
@SettledPreview(afterMs = 800)
@Composable
fun GestureScrollIndicator(rows: Int = 12) = TransparentScreenSticker {
  val enabled = gesturesEnabled()
  CompositionLocalProvider(LocalOneHandedGestureEnabled provides enabled) {
    val configuration =
      rememberGestureConfiguration(
        OneHandedGestureAction.Primary,
        OneHandedGesturePriority.Scrollable,
      )
    val scrollState = rememberTransformingLazyColumnState()
    val indicatorState = remember(configuration) { OneHandedGestureScrollIndicatorState() }
    LaunchedEffect(indicatorState, enabled) { if (enabled) indicatorState.showIndicator() }
    // Empty rows, for the reason `Indicators.kt` gives for the plain rail: the indicator sizes its
    // thumb from how much of the content fits on screen, so it needs a list that overflows —
    // and a screenful of labels would be the loudest thing in a frame whose subject is a few
    // pixels of bezel.
    TransformingLazyColumn(
      state = scrollState,
      modifier =
        Modifier.fillMaxSize()
          .oneHandedGesture(
            gestureConfiguration = configuration,
            onGestureLabel = "scroll down",
            onGesture = { OneHandedGestureDefaults.scrollDown(scrollState) },
          ),
    ) {
      items(rows) { Spacer(Modifier.height(48.dp)) }
    }
    // ALIGNED FROM OUTSIDE, and the obvious way round does not work.
    //
    // These three components take a `modifier` and forward it to the RAIL (or, on the page
    // indicators, to the dots) rather than to their own root: the gesture pill is a sibling in a
    // `Row`/`Column` above it, and a `BoxScope.align` handed to the inner child is silently
    // ignored by that row. The whole component therefore lands at the top-left corner — which is
    // exactly what the first render of this was, a hand glyph in the top-left of an empty display.
    //
    // On a screen nothing notices, because `ScreenScaffold` and the pager scaffolds place the slot
    // themselves; this is the same "no scaffold here, so the sticker supplies the alignment" note
    // `Indicators.kt` opens with, one layer further out.
    Box(Modifier.align(Alignment.CenterEnd)) {
      OneHandedGestureScrollIndicator(
        gestureConfiguration = configuration,
        indicatorState = indicatorState,
        scrollState = scrollState,
      )
    }
    GestureActivation(
      OneHandedGestureAction.Primary,
      onGesture = { OneHandedGestureDefaults.scrollDown(scrollState) },
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@CatalogComponent(
  id = "OneHandedGesturePageIndicator/Horizontal",
  noReference =
    "The kit's `Page-Indicator` set draws the dots alone; the gesture pill that grows above them " +
      "has no cell, one-handed gestures being a design guide rather than a kit component sheet.",
  caption = "The page dots with the pinch that advances them, along the bottom of the display.",
  motionPreview = "GesturePageHintMotion",
)
@CatalogTransparentScreenModes
@OverrideVariant(name = "gestures-off", booleans = ["gesturesEnabled=false"])
@SettledPreview(afterMs = 800)
@Composable
fun GestureHorizontalPages(
  pages: Int = 4,
  initialPage: Int = 0,
) = TransparentScreenSticker {
  val enabled = gesturesEnabled()
  CompositionLocalProvider(LocalOneHandedGestureEnabled provides enabled) {
    val initialPage = initialPage.coerceIn(0, (pages - 1).coerceAtLeast(0))
    val configuration =
      rememberGestureConfiguration(
        OneHandedGestureAction.Primary,
        OneHandedGesturePriority.Scrollable,
      )
    val indicatorState = remember(configuration) { OneHandedGesturePageIndicatorState() }
    LaunchedEffect(indicatorState, enabled) { if (enabled) indicatorState.showIndicator() }
    // `key(…)` for the same reason `Indicators.kt` needs it: `rememberPagerState` reads
    // `initialPage` once, so an unkeyed state leaves the knob dead in a live session.
    val pagerState =
      key(pages, initialPage) { rememberPagerState(initialPage = initialPage) { pages } }
    // THE GESTURE HAS TO BE REGISTERED SOMEWHERE, and it was registered nowhere.
    //
    // This drew the indicator and stopped: no `Modifier.oneHandedGesture` anywhere in the sticker,
    // so the configuration named a gesture nothing had claimed and there was no `onGesture` to
    // run. The dots sat at `initialPage` for good — the component announced "a pinch advances the
    // pager" and could not advance one. On a screen the modifier goes on the pager itself; there
    // is no pager here (the kit's cell is the dots alone, as `Indicators.kt` draws them), so it
    // goes on the frame, which is what a pager would have filled.
    Box(
      Modifier.fillMaxSize()
        .oneHandedGesture(
          gestureConfiguration = configuration,
          onGestureLabel = "scroll to the next page",
          onGesture = { OneHandedGestureDefaults.scrollToNextPage(pagerState) },
        )
    )
    // Aligned from the outside — see the note in `GestureScrollIndicator` above.
    Box(Modifier.align(Alignment.BottomCenter)) {
      OneHandedGestureHorizontalPageIndicator(
        gestureConfiguration = configuration,
        indicatorState = indicatorState,
        pagerState = pagerState,
      )
    }
    GestureActivation(
      OneHandedGestureAction.Primary,
      onGesture = { OneHandedGestureDefaults.scrollToNextPage(pagerState) },
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

/**
 * The same indicator for a vertical pager, drawn against the right bezel.
 *
 * A separate component rather than a cell, exactly as `PageIndicator/Vertical` is: the orientation
 * is a different Wear Compose function ([OneHandedGestureVerticalPageIndicator]), not an argument,
 * and which one you call is the choice a reader of this catalog is making (AGENTS.md).
 */
@CatalogComponent(
  id = "OneHandedGesturePageIndicator/Vertical",
  noReference =
    "The kit's `Page-Indicator` set draws the dots alone; the gesture pill beside them " +
      "has no cell, one-handed gestures being a design guide rather than a kit component sheet.",
  caption = "The same pinch-to-advance affordance for a vertical pager, against the right bezel.",
)
@CatalogTransparentScreenModes
@OverrideVariant(name = "gestures-off", booleans = ["gesturesEnabled=false"])
@SettledPreview(afterMs = 800)
@Composable
fun GestureVerticalPages(
  pages: Int = 4,
  initialPage: Int = 0,
) = TransparentScreenSticker {
  val enabled = gesturesEnabled()
  CompositionLocalProvider(LocalOneHandedGestureEnabled provides enabled) {
    val initialPage = initialPage.coerceIn(0, (pages - 1).coerceAtLeast(0))
    val configuration =
      rememberGestureConfiguration(
        OneHandedGestureAction.Primary,
        OneHandedGesturePriority.Scrollable,
      )
    val indicatorState = remember(configuration) { OneHandedGesturePageIndicatorState() }
    LaunchedEffect(indicatorState, enabled) { if (enabled) indicatorState.showIndicator() }
    val pagerState =
      key(pages, initialPage) { rememberPagerState(initialPage = initialPage) { pages } }
    // THE GESTURE HAS TO BE REGISTERED SOMEWHERE, and it was registered nowhere.
    //
    // This drew the indicator and stopped: no `Modifier.oneHandedGesture` anywhere in the sticker,
    // so the configuration named a gesture nothing had claimed and there was no `onGesture` to
    // run. The dots sat at `initialPage` for good — the component announced "a pinch advances the
    // pager" and could not advance one. On a screen the modifier goes on the pager itself; there
    // is no pager here (the kit's cell is the dots alone, as `Indicators.kt` draws them), so it
    // goes on the frame, which is what a pager would have filled.
    Box(
      Modifier.fillMaxSize()
        .oneHandedGesture(
          gestureConfiguration = configuration,
          onGestureLabel = "scroll to the next page",
          onGesture = { OneHandedGestureDefaults.scrollToNextPage(pagerState) },
        )
    )
    // Aligned from the outside — see the note in `GestureScrollIndicator` above.
    Box(Modifier.align(Alignment.CenterEnd)) {
      OneHandedGestureVerticalPageIndicator(
        gestureConfiguration = configuration,
        indicatorState = indicatorState,
        pagerState = pagerState,
      )
    }
    GestureActivation(
      OneHandedGestureAction.Primary,
      onGesture = { OneHandedGestureDefaults.scrollToNextPage(pagerState) },
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}
