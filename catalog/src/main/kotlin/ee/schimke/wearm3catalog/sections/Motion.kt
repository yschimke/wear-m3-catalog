@file:CatalogGroup(name = "Motion", section = "Motion")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.wear.compose.material3.rememberRevealState
import ee.schimke.composeai.preview.AnimatedPreview
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.wearm3catalog.AnimatedSticker
import ee.schimke.wearm3catalog.EdgeButtonScreen
import ee.schimke.wearm3catalog.Sticker
import kotlinx.coroutines.delay

// MOTION — what the component sheet cannot show, because a sticker is one frame.
//
// WHY THESE ARE THEIR OWN PREVIEWS AND NOT ANNOTATIONS ON THE COMPONENTS
//
// Two reasons, both found by trying the other way first.
//
// 1. An `@AnimatedPreview` on a `@CatalogComponent` rides every `@OverrideVariant` cell as well,
//    and the animated path does NOT apply a cell's knobs. Putting one on `Placeholder/Button`
//    published three byte-identical GIFs — the base recording, under the tonal and outlined names.
// 2. A motion capture needs a pinned canvas (`widthDp` + `heightDp`); the component stickers wrap
//    and are cropped, which is what makes them droppable onto any canvas.
//
// So motion is authored here, deliberately outside the component inventory: these carry no
// `@CatalogComponent`, so they add nothing to the kit taxonomy and answer to no kit node. They are
// recordings *of* components that are catalogued elsewhere.
//
// AND THAT IS ONLY HALF THE WIRING — THE HALF THAT WAS MISSING
//
// Authoring a recording here does NOT publish it. A design catalog collects motion PER COMPONENT:
// the export reads each component's own `@Preview`, or its `motionPreview`, and folds what it finds
// onto `components[].motion[]`. A recording no component names is resolved by nobody, so it renders
// into the bundle and is dropped at the join — the catalog then publishes with an empty `motion/`
// and nothing anywhere says why. That is exactly what happened to all five of these from the day
// they landed until compose-ai-tools 1.23.0: green runs, correct GIFs in the bundle, no Motion lane
// on the delivery branch.
//
// Each recording below is therefore CLAIMED by the component it is a recording of, by naming it as
// `motionPreview = "<function>"` on that component's own `@CatalogComponent` — never here. (Spelled
// without the opening bracket on purpose: `CatalogInventoryTest` finds components by scanning these
// files for the annotation's literal text, so writing it in full in a comment mints a phantom
// component with no id and fails the build.)
//
//   IndeterminateProgressMotion -> CircularProgressIndicator  (ProgressIndicators.kt)
//   SwitchTransitionMotion      -> SwitchButton               (SelectionButtons.kt)
//   ToggleButtonShapeMotion     -> IconToggleButton           (ToggleButtons.kt)
//   SwipeToRevealMotion         -> SwipeToReveal/Card         (SwipeToReveal.kt)
//   EdgeButtonRevealMotion      -> EdgeButton                 (EdgeButtons.kt)
//   PlaceholderButtonMotion     -> Placeholder/Button         (Placeholders.kt)
//   PlaceholderIconButtonMotion -> Placeholder/IconButton     (Placeholders.kt)
//   PlaceholderCardMotion       -> Placeholder/Card           (Placeholders.kt)
//
// Claiming costs the recording nothing it had: it still carries no `@CatalogComponent`, still adds
// no card and no kit node. It only tells the export whose Motion lane the bytes belong in. ADDING A
// RECORDING HERE MEANS ADDING ITS CLAIM TOO — an unclaimed one is now warned about by the export
// ("N @Preview function(s) declare captures that no catalog component claims") rather than being
// silently dropped, but the warning does not publish it.
//
// `@InteractionPreview` IS NOT USED, AND NOT BY CHOICE
//
// It is the annotation for pointer-provoked motion — a switch only moves because someone flipped
// it — and it is implemented in the DESKTOP renderer only: `DesktopRendererMain` builds an
// `InteractionSpec` from the gesture and targets, and the Robolectric path this Android module
// renders on has no equivalent. The failure is quiet and confusing rather than a clear "not
// supported": nothing writes the animated file, and the still frame then fails to decode with
// `<id>.apng: file is missing on disk` — which also costs the component its ordinary PNG.
// Tracked upstream; when it lands, the toggles below become real tap recordings instead of
// state-driven ones.
//
// THE PLACEHOLDER, WHICH WAS "NOT HERE" AND IS NOW
//
// This file used to record the placeholder as a thing the renderer could not do: held visible it
// came out with 3 distinct frames in 46, and toggling `isVisible` so the wipe plays made it 4.
// That reading was wrong, and the correction is worth keeping because it looks nothing like a
// missing wrapper: `PlaceholderState` reads its frame clock from the library's internal
// `AnimationCoordinator`, and the ONLY thing in Wear Compose that composes that coordinator's
// looper is `AppScaffold`. No scaffold, no frames — under any renderer, on a watch as much as
// here. The placeholder recordings below therefore go in `AnimatedSticker`, which is that scaffold
// and nothing else; the component stickers keep [Sticker] and keep their placeholder frozen, which
// is what a baked capture wants anyway.
//
// WHAT DRIVES THESE, THEN
//
// Either the component's own animation (a spinner, a shimmer — nothing to provoke), or a state
// change from a `LaunchedEffect`. The second is a real state change, not a forged interaction: no
// `MutableInteractionSource` is seeded and no state layer is painted that nothing is causing. What
// it does not claim is that a finger did it.

/**
 * The canvas every motion capture is pinned to. Frames must share one size or no GIF is written.
 */
@Preview(showBackground = false, widthDp = 200, heightDp = 120) annotation class MotionCanvas

/** A wide, short canvas for the row-shaped components. */
@Preview(showBackground = false, widthDp = 220, heightDp = 80) annotation class MotionRowCanvas

/**
 * The round 192dp screen, for a recording of something that positions itself against the display
 * rather than wrapping. Black rather than transparent, because a screen is not a sticker.
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 192, heightDp = 192)
annotation class MotionScreenCanvas

// ---------------------------------------------------------------------------
// Infinite — motion the component runs by itself, with nothing to provoke it.
// ---------------------------------------------------------------------------

/**
 * The indeterminate ring: `CircularProgressIndicator`'s overload with no `progress`, which runs an
 * `InfiniteTransition` forever.
 *
 * It is a recording and NOT a catalogued component on purpose. The kit's `Progress-Indicator` set
 * publishes `Progress = Zero | In progress | Complete | Overflow` — four determinate values and no
 * indeterminate one — and membership is the kit's call. What the kit has no cell for can still be
 * worth showing; it just does not get a card.
 */
@MotionCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun IndeterminateProgressMotion() = Sticker {
  CircularProgressIndicator(modifier = Modifier.size(90.dp))
}

// ---------------------------------------------------------------------------
// State transitions — the component moving between two states it publishes.
// ---------------------------------------------------------------------------

/**
 * Flips back and forth for the whole capture window, so the recording is motion rather than a
 * transition followed by a held still.
 *
 * Both directions on purpose: a spring is not symmetric, and a designer choosing one is choosing
 * how it settles each way. A single flip left two thirds of the window identical — 12 distinct
 * frames out of 46 — which is a still with a preamble.
 */
@Composable
private fun flipping(initial: Boolean, everyMs: Long = 500): Boolean {
  var value by remember { mutableStateOf(initial) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(everyMs)
      value = !value
    }
  }
  return value
}

/**
 * The switch thumb travelling. A still of either end state says where the thumb is; it says nothing
 * about the spring that carries it, which is the part a designer is choosing.
 */
@MotionRowCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun SwitchTransitionMotion() = Sticker {
  val checked = flipping(false)
  SwitchButton(checked = checked, onCheckedChange = {}, label = { Text("Bluetooth") })
}

/**
 * The toggle button's **shape morph**, which is the one animation you have to opt into:
 * `IconToggleButtonDefaults.animatedShapes()` rather than the static `shapes()` the component
 * sticker draws. Checked and unchecked are different corner shapes, and the morph between them is
 * what the kit's `Corner radius = Circular | Rounded (18)` axis is a still of.
 */
@MotionCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun ToggleButtonShapeMotion() = Sticker {
  val checked = flipping(false)
  IconToggleButton(
    checked = checked,
    onCheckedChange = {},
    shapes = IconToggleButtonDefaults.animatedShapes(),
  ) {
    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
  }
}

/**
 * Swipe to reveal, revealing. The component sheet publishes it already revealed, because at rest it
 * is indistinguishable from the card underneath — this is the part in between.
 */
@MotionRowCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun SwipeToRevealMotion() = Sticker {
  val state = rememberRevealState(initialValue = RevealValue.Covered)
  LaunchedEffect(Unit) {
    while (true) {
      delay(500)
      state.animateTo(RevealValue.RightRevealing)
      delay(500)
      state.animateTo(RevealValue.Covered)
    }
  }
  SwipeToReveal(
    primaryAction = {
      PrimaryActionButton(
        onClick = {},
        icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
        text = { Text("Delete") },
      )
    },
    onSwipePrimaryAction = {},
    revealState = state,
    modifier = Modifier.width(180.dp),
  ) {
    Card(onClick = {}) { Text("Morning run") }
  }
}

/**
 * The edge button revealing itself as the list scrolls.
 *
 * This is the picture the component sheet used to publish as a still, and it belongs here instead.
 * `ScreenScaffold` reveals the button *from the scroll state* — at the resting top it is collapsed
 * — so the whole of what an edge button does is a thing one frame cannot show, while the sticker it
 * is compared against is the kit's 192×59 component cell and nothing else (issue #31).
 *
 * The scroll is driven from a `LaunchedEffect` rather than a gesture, for the reason at the top of
 * this file: `@InteractionPreview` is desktop-only, and there is no finger to record here.
 */
@MotionScreenCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun EdgeButtonRevealMotion() {
  val state = rememberTransformingLazyColumnState()
  // To the LAST item and back, not by a pixel count: the scaffold only reveals the button near the
  // end of the list, so a partial scroll records a list moving and no edge button at all.
  LaunchedEffect(Unit) {
    while (true) {
      delay(200)
      state.animateScrollToItem(11)
      delay(400)
      state.animateScrollToItem(0)
    }
  }
  EdgeButtonScreen(state) {
    EdgeButton(onClick = {}, buttonSize = EdgeButtonSize.Small) { Text("Done") }
  }
}

// ---------------------------------------------------------------------------
// Loading — a placeholder resolving into the content it was standing in for.
// ---------------------------------------------------------------------------

/**
 * The loading flag the three placeholder recordings share: content is missing, then it arrives,
 * then it is missing again so the recording loops.
 *
 * Both directions on purpose, as with [flipping]. The wipe-off is the animation a designer is
 * choosing here, but a window that only ever wiped off would spend most of itself as a held still
 * of a loaded button, and the shimmer — the other half of what a placeholder does — would show for
 * a moment and never come back.
 */
@Composable private fun loading(): Boolean = flipping(initial = true, everyMs = 700)

/**
 * The placeholder resolving: the label and icon are covered while they are in flight, the shimmer
 * sweeps across them, and when the content lands the cover wipes off to reveal it.
 *
 * `Modifier.placeholder` sits on the CONTENT here — on the `Text` and on the icon — rather than on
 * a chip standing in for it, which is what makes the reveal a reveal: the real label is drawn
 * underneath the whole time and the placeholder is what is taken away. The component sticker in
 * `Placeholders.kt` cannot do that, because it has no content to cover; it draws the kit's chips at
 * the kit's sizes, and this is the same component with the words filled in.
 *
 * Copy that reads like an app, not [KitCopy]: a recording answers to no kit node, and a wipe-off
 * revealing the words `Primary label` reveals nothing.
 */
@MotionCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun PlaceholderButtonMotion() = AnimatedSticker {
  val state = rememberPlaceholderState(isVisible = loading())
  Button(
    onClick = {},
    modifier = Modifier.width(172.dp).placeholderShimmer(state),
    icon = {
      Icon(
        Icons.Filled.Place,
        contentDescription = null,
        modifier = Modifier.placeholder(state, CircleShape),
      )
    },
    secondaryLabel = { Text("6.2 km", modifier = Modifier.placeholder(state, CircleShape)) },
    label = { Text("Morning run", modifier = Modifier.placeholder(state, CircleShape)) },
  )
}

/** The same reveal with nothing but an icon to reveal — the icon button's whole content. */
@MotionCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun PlaceholderIconButtonMotion() = AnimatedSticker {
  val state = rememberPlaceholderState(isVisible = loading())
  FilledIconButton(onClick = {}, modifier = Modifier.placeholderShimmer(state, CircleShape)) {
    Icon(
      Icons.Filled.Place,
      contentDescription = "Activity",
      modifier = Modifier.placeholder(state, CircleShape),
    )
  }
}

/**
 * Three lines and an icon arriving at once, which is the case a placeholder is really for: a card
 * of loaded content appearing all together rather than line by line as each string lands.
 */
@MotionCanvas
@AnimatedPreview(showCurves = false)
@Composable
fun PlaceholderCardMotion() = AnimatedSticker {
  val state = rememberPlaceholderState(isVisible = loading())
  Card(onClick = {}, modifier = Modifier.width(172.dp).placeholderShimmer(state)) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Icon(
        Icons.Filled.Place,
        contentDescription = null,
        modifier = Modifier.placeholder(state, CircleShape),
      )
      // Spaced, unlike the loaded text: two placeholder pills on adjacent baselines touch and read
      // as one blob, which is a shape no line of text has.
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Morning run", modifier = Modifier.placeholder(state, CircleShape))
        Text("6.2 km in 31:04", modifier = Modifier.placeholder(state, CircleShape))
      }
    }
  }
}
