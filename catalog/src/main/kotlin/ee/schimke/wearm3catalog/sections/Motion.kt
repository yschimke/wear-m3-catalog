@file:CatalogGroup(name = "Motion", section = "Motion")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberRevealState
import ee.schimke.composeai.preview.AnimatedPreview
import ee.schimke.composeai.preview.CatalogGroup
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
// WHAT IS NOT HERE: THE PLACEHOLDER
//
// The obvious fifth recording — the shimmer, and the wipe-off when content lands — does not run
// under this renderer. Held visible it came out with 3 distinct frames in 46; toggling `isVisible`
// so the wipe plays made it 4. Forcing `LocalReduceMotion` off (the sandbox reports the system
// animator scale as zero, which Wear reads as a wearer asking for less motion) changed nothing, so
// that is not the cause either — `PlaceholderState` drives itself from a coordinator the paused
// clock does not advance. Four near-identical frames is a still with extra bytes, so there is no
// placeholder GIF rather than one that implies motion nobody would see.
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
/**
 * The canvas every motion capture is pinned to. Frames must share one size or no GIF is written.
 */
@Preview(showBackground = false, widthDp = 200, heightDp = 120) annotation class MotionCanvas

/** A wide, short canvas for the row-shaped components. */
@Preview(showBackground = false, widthDp = 220, heightDp = 80) annotation class MotionRowCanvas

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
