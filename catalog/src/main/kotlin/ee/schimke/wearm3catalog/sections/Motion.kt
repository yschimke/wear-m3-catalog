@file:CatalogGroup(name = "Motion", section = "Motion")

package ee.schimke.wearm3catalog.sections

import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AnimatedPage
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FadingExpandingLabel
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.VerticalPagerScaffold
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureClickIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureClickIndicatorState
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureDefaults
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureHorizontalPageIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePageIndicatorState
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePriority
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureScrollIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureScrollIndicatorState
import androidx.wear.compose.material3.onehandedgesture.oneHandedGesture
import androidx.wear.compose.material3.onehandedgesture.rememberOneHandedGestureConfiguration
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.wear.compose.material3.rememberRevealState
import com.google.android.horologist.media.ui.material3.components.PodcastControlButtons
import ee.schimke.composeai.preview.AnimatedPreview
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.InteractionGesture
import ee.schimke.composeai.preview.InteractionPreview
import ee.schimke.composeai.preview.MotionFormat
import ee.schimke.wearm3catalog.AnimatedSticker
import ee.schimke.wearm3catalog.EdgeButtonScreen
import ee.schimke.wearm3catalog.HorologistSamples
import ee.schimke.wearm3catalog.ScreenSticker
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
// and nothing anywhere says why. That is exactly what happened to the first five of these from the
// day they landed until compose-ai-tools 1.23.0: green runs, correct GIFs in the bundle, no Motion
// lane on the delivery branch.
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
//   MediaTransportMotion        -> Media/PodcastControlButtons (MediaControls.kt)
//   GestureHintMotion           -> OneHandedGestureClickIndicator          (OneHandedGestures.kt)
//   GestureScrollHintMotion     -> OneHandedGestureScrollIndicator         (OneHandedGestures.kt)
//   GesturePageHintMotion       -> OneHandedGesturePageIndicator/Horizontal (OneHandedGestures.kt)
//   FadingExpandingLabelMotion  -> FadingExpandingLabel       (TextComponents.kt)
//   AnimatedTextMotion          -> AnimatedText               (TextComponents.kt)
//   PagerTransitionMotion       -> Pager/Horizontal           (Pagers.kt)
//   VerticalPagerTransitionMotion -> Pager/Vertical           (Pagers.kt)
//
// ONE FUNCTION PER COMPONENT, which is a constraint on what a recording may cover rather than a
// detail of the wiring: `motionPreview` is a single name, so two things worth showing on the same
// component share one capture window. `MediaTransportMotion` is the worked example: the presses,
// the shape morph and the progress ring all belong to the transport row, so they run together.
//
// Claiming costs the recording nothing it had: it still carries no `@CatalogComponent`, still adds
// no card and no kit node. It only tells the export whose Motion lane the bytes belong in. ADDING A
// RECORDING HERE MEANS ADDING ITS CLAIM TOO — an unclaimed one is now warned about by the export
// ("N @Preview function(s) declare captures that no catalog component claims") rather than being
// silently dropped, but the warning does not publish it.
//
// `@InteractionPreview` WORKS HERE NOW, AND THIS NOTE USED TO SAY IT DID NOT
//
// It is the annotation for pointer-provoked motion — a switch only moves because someone flipped
// it — and it WAS implemented in the desktop renderer only. An Android catalog that reached for it
// got no capture, and failed confusingly rather than clearly: nothing wrote the animated file, and
// the still frame then failed to decode with `<id>.apng: file is missing on disk`, which also cost
// the component its ordinary PNG. That is compose-ai-tools issue #4215, and it is CLOSED —
// implemented on Robolectric in #4240, with the ripple's clock fixed in #4315, both shipped in
// **1.25.0**, which is the version this repo already pins.
//
// So the constraint this file was written under is gone, and the note is kept rather than deleted
// because it explains why the recordings below are split the way they are. **Where a press or a tap
// is what makes the pixels move, the annotation is now the tool** — the media transport row, the
// switch and the toggle button are all real dispatched pointers.
//
// One thing it buys that no `LaunchedEffect` could: the Android backend advances the **main
// looper** alongside `mainClock` on every frame. Material's ripple is a platform `RippleDrawable`
// and does not run on Compose's test clock at all, so a hand-driven press records a state layer
// frozen at frame 0 while the Compose-side animation plays. That is half of a press response, and
// it is the half a reader is usually looking for.
//
// WHAT IS STILL STATE-DRIVEN, AND THE ONE REASON LEFT
//
// Two kinds, and only one of them is a limitation.
//
//  - **Nothing to press.** A spinner and a shimmer run on their own; there is no gesture that
//    starts them, so `@AnimatedPreview` is not a workaround there, it is the correct annotation.
//  - **A gesture the annotation cannot script.** `@InteractionPreview` dispatches `Tap` and
//    `PressAndHold` — presses. Swipe-to-reveal, the edge button's scroll and the two pagers are all
//    **drags**, and a press that never travels does nothing to any of them. They drive the
//    component's own state object (`RevealState`, `TransformingLazyColumnState`, `PagerState`)
//    through the same animation a finger would, so the spring and the anchors are real and only the
//    cause is scripted. A drag gesture upstream would convert them all, and their KDoc says so at
//    the call site.
//
//    `SwipeToDismissBox` is the one drag that does NOT appear below, and the difference is the
//    state object rather than the gesture: it publishes only `snapTo(Default | Dismissed)`, which
//    teleports between two anchors, so there is no animation of the component's own to drive. Its
//    card publishes the two ends and says so — see `SwipeToDismiss.kt`.
//  - **A gesture that is not a pointer at all.** The three one-handed-gesture recordings at the
//    foot of this file are raised by a double pinch or a wrist turn — sensor events the watch's
//    `GestureInputManager` reports, which no pointer dispatcher can stand in for and which are
//    simply absent off a watch. The app-side callback that receives them (`onGestureAvailable`)
//    does one thing, and the recordings do that same public thing: call `showIndicator()`. Unlike
//    the two drags above, this is not waiting on an annotation — there is no gesture to script.
//
// That is the whole of it: no recording here is state-driven merely because nobody revisited it.
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
// WHAT NEVER DRIVES ANY OF THESE
//
// A seeded `MutableInteractionSource`. Emitting `PressInteraction.Press` paints a state layer that
// nothing is causing, so the capture shows a component that LOOKS pressed and documents this file's
// belief about it rather than the component. It does not even work: the state layer is a platform
// `RippleDrawable` running on the main looper, which a hand-driven press does not advance — see the
// measurement in the media note below. Where a press is the motion, dispatch a real one.

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
 * The switch thumb travelling, under a finger that actually flips it.
 *
 * A still of either end state says where the thumb is; it says nothing about the spring that
 * carries it, which is the part a designer is choosing. Both directions get recorded because the
 * target repeats — a spring is not symmetric, and choosing one is choosing how it settles each way.
 *
 * **The state is the component's own.** `onCheckedChange` writes to the `checked` this reads, so
 * the thumb moves because the row was pressed and `SwitchButton` reported the change — not because
 * a `LaunchedEffect` set a boolean and the component redrew. That distinction is invisible in the
 * pixels for a switch, which is exactly why it is worth wiring correctly: a recording that fakes
 * the cause documents this file's belief about the component rather than the component.
 *
 * `Tap` rather than `PressAndHold`: the thumb's travel begins on release, and the state layer under
 * a held finger is not what this recording is about.
 */
@MotionRowCanvas
@InteractionPreview(
  targets = [0, 0, 0],
  format = MotionFormat.Gif,
  caption =
    "Flipped on, off and on again: the thumb rides a spring that does not settle the same way " +
      "in both directions.",
)
@Composable
fun SwitchTransitionMotion() = Sticker {
  var checked by remember { mutableStateOf(false) }
  SwitchButton(
    checked = checked,
    onCheckedChange = { checked = it },
    label = { Text("Bluetooth") },
  )
}

/**
 * The toggle button's **shape morph**, which is the one animation you have to opt into:
 * `IconToggleButtonDefaults.animatedShapes()` rather than the static `shapes()` the component
 * sticker draws. Checked and unchecked are different corner shapes, and the morph between them is
 * what the kit's `Corner radius = Circular | Rounded (18)` axis is a still of.
 *
 * `PressAndHold` rather than `Tap`, because `animatedShapes()` declares **three** shapes and a
 * momentary tap only ever shows two of them. The pressed shape is a state of its own — held, the
 * button sits in it — and passing through it in one frame on the way to `checked` is how a reader
 * ends up believing the component has a morph it does not advertise. A hold records unchecked →
 * pressed → checked as three legible states, and neither `IconToggleButton` nor this preview sets
 * `onLongClick`, so the ordinary toggle still fires on release.
 */
@MotionCanvas
@InteractionPreview(
  gesture = InteractionGesture.PressAndHold,
  targets = [0, 0, 0],
  format = MotionFormat.Gif,
  caption =
    "Held, then released, three times: `animatedShapes()` morphs through a pressed shape on the " +
      "way between the unchecked and checked corner radii.",
)
@Composable
fun ToggleButtonShapeMotion() = Sticker {
  var checked by remember { mutableStateOf(false) }
  IconToggleButton(
    checked = checked,
    onCheckedChange = { checked = it },
    shapes = IconToggleButtonDefaults.animatedShapes(),
  ) {
    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
  }
}

/**
 * The button group yielding: one button swells under a finger and its neighbours give up the width.
 *
 * A still cannot carry this at all. Every cell on the `ButtonGroup` card is the row at rest, where
 * the buttons are equal and the component is indistinguishable from a `Row` that shares its
 * spacing. The expansion IS the component, and it only exists while something is pressed.
 *
 * **The press is a real dispatched pointer, and the widths that move are the layout's own.**
 * `ButtonGroupScope.animateWidth` hands each button's `MutableInteractionSource` to the layout, so
 * `ButtonGroup` grows the pressed one by `ButtonGroupDefaults.ExpansionWidth` and takes the
 * difference off its neighbours. Nothing here animates a width.
 *
 * The first and the last are pressed rather than one twice: the middle button yields on both sides,
 * and a recording that only ever presses one end shows half of what the row does.
 *
 * `PressAndHold` rather than `Tap`: the swell lasts exactly as long as the press, so a momentary
 * tap records the grow and the shrink colliding in a frame or two.
 */
@MotionRowCanvas
@InteractionPreview(
  gesture = InteractionGesture.PressAndHold,
  targets = [0, 2],
  format = MotionFormat.Gif,
  caption =
    "The first button held, then the last: the pressed one swells by the group's expansion width " +
      "and its neighbours yield it back.",
)
@Composable
fun ButtonGroupExpansionMotion() = Sticker {
  ButtonGroup(modifier = Modifier.width(180.dp)) {
    repeat(3) { index ->
      // The component sticker's own wiring (`Buttons.kt`'s `ButtonRowGroup`), because the wiring is
      // what is being recorded: the source `animateWidth` watches has to be the source the button
      // emits its press into.
      val interactionSource = remember { MutableInteractionSource() }
      Button(
        onClick = {},
        modifier = Modifier.weight(1f).animateWidth(interactionSource),
        interactionSource = interactionSource,
        label = { Text(('A' + index).toString()) },
      )
    }
  }
}

/**
 * Swipe to reveal, revealing. The component sheet publishes it already revealed, because at rest it
 * is indistinguishable from the card underneath — this is the part in between.
 *
 * **State-driven, and this is the one where that is a limitation rather than a choice.** The motion
 * is a *drag*, and `@InteractionPreview` dispatches `Tap` and `PressAndHold` only — a press that
 * never travels reveals nothing, so there is no gesture to script here. `animateTo` drives the
 * component's own `RevealState` through the same animation a finger would, which is the closest
 * honest thing: the spring and the anchors are the component's, only the cause is not. If the
 * annotation gains a drag gesture this becomes a real swipe recording, and it should.
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
 * The scroll is driven from a `LaunchedEffect` rather than a gesture, and — like swipe-to-reveal
 * above — that is now a limitation with a name rather than the stale "desktop-only" one this line
 * used to carry. A scroll is a drag, `@InteractionPreview` scripts taps and holds, and a tap on a
 * list row scrolls nothing. `animateScrollToItem` moves the component's own
 * `TransformingLazyColumnState`, so the scaffold reveals the button off the real scroll position;
 * only the finger is missing.
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
 * How long the placeholder is held before its content lands: one full shimmer sweep.
 *
 * `PLACEHOLDER_SHIMMER_DURATION_MS` in Wear Compose — `MotionTokens.DurationExtraLong2`. The
 * library's own number, not a taste: the sweep runs for 800ms and then the shimmer **stops** for
 * the rest of a 2000ms loop (`PLACEHOLDER_SHIMMER_GAP_BETWEEN_ANIMATION_LOOPS_MS`), so a window
 * that holds the placeholder any longer than this spends the extra time on a motionless
 * placeholder.
 */
private const val SHIMMER_SWEEP_MS = 800

/**
 * The wipe-off, once the content has landed — the part of it that is visible on a component.
 *
 * `PLACEHOLDER_WIPE_OFF_PROGRESSION_ALPHA_DURATION_MS`, and **not** the 300ms
 * `PLACEHOLDER_WIPE_OFF_PROGRESSION_DURATION_MS` the state machine runs for. The placeholder's
 * alpha reaches 1 at 80/300 of that progression — Wear Compose's own KDoc on
 * `placeholderWipeOffAlpha` says "the time taken for this progression is 80ms" — and the remaining
 * 220ms changes no pixel on the component. Recording them would be 220ms of held still.
 */
private const val WIPE_OFF_MS = 80

/**
 * The loading flag the three placeholder recordings share: content is missing for one shimmer
 * sweep, then it arrives. **One direction only**, unlike [flipping].
 *
 * This used to flip both ways every 700ms, on the reasoning that a recording which only ever wiped
 * off would spend most of itself as a held still of a loaded card. Measuring the GIF said the
 * opposite: of 2.8s of playback, roughly 2.2s was a frozen frame. Two whole load/unload cycles were
 * being crammed into a 1.5s window — so neither reveal had room — and the second placeholder period
 * landed entirely inside the shimmer's 1200ms dead gap, which is a *motionless* placeholder held
 * for a third of a second. A GIF loops, so the shimmer comes back either way; what it does not do
 * is come back sooner for having been recorded twice.
 *
 * So the window is one cycle, phase-locked to the library's own timings: sweep, reveal, and the
 * renderer's own end-of-capture hold to read the loaded state by. See [SHIMMER_SWEEP_MS].
 */
@Composable
private fun loading(): Boolean {
  var value by remember { mutableStateOf(true) }
  LaunchedEffect(Unit) {
    delay(SHIMMER_SWEEP_MS.toLong())
    value = false
  }
  return value
}

/**
 * The capture window the three placeholder recordings share: one shimmer sweep, then the wipe-off
 * that reveals the content, and nothing after it.
 *
 * Stated rather than auto-detected, which is what `durationMs = 0` would do. `PlaceholderState`
 * drives itself from Wear Compose's internal `AnimationCoordinator` rather than from a Compose
 * `Animatable`, so `PreviewAnimationClock` finds nothing to measure and auto-detection falls back
 * to a flat 1500ms — a number with no relationship to what a placeholder actually does, and long
 * enough to record a second cycle nobody asked for.
 */
private const val PLACEHOLDER_CAPTURE_MS = SHIMMER_SWEEP_MS + WIPE_OFF_MS

/**
 * Per-frame step for the placeholder recordings, in place of `@AnimatedPreview`'s 33ms default.
 *
 * **A multiple of 16 on purpose, and the difference is 1.5× of playback speed.** The renderer steps
 * the capture with `mainClock.advanceTimeBy(frameIntervalMs)`, which rounds *up* to a whole number
 * of 16ms Compose frames: a nominal 33ms step advances the clock 48ms, while the GIF still plays
 * that frame for 33ms. Every recording taken at the default therefore plays at ~1.6× the speed the
 * animation actually runs. 32ms is two whole frames, so a frame of capture and a frame of playback
 * are the same 32ms and the sweep plays at the 800ms Wear Compose spends on it. (GIF delays
 * quantise to 10ms, so 32ms is encoded as 30ms — 7% fast, against 60% before.)
 */
private const val PLACEHOLDER_FRAME_MS = 32

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
@AnimatedPreview(
  durationMs = PLACEHOLDER_CAPTURE_MS,
  frameIntervalMs = PLACEHOLDER_FRAME_MS,
  showCurves = false,
)
@Composable
fun PlaceholderButtonMotion() = AnimatedSticker {
  val state = rememberPlaceholderState(isVisible = loading())
  Button(
    onClick = {},
    modifier = Modifier.width(172.dp).placeholderShimmer(state, ButtonDefaults.shape),
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
@AnimatedPreview(
  durationMs = PLACEHOLDER_CAPTURE_MS,
  frameIntervalMs = PLACEHOLDER_FRAME_MS,
  showCurves = false,
)
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
@AnimatedPreview(
  durationMs = PLACEHOLDER_CAPTURE_MS,
  frameIntervalMs = PLACEHOLDER_FRAME_MS,
  showCurves = false,
)
@Composable
fun PlaceholderCardMotion() = AnimatedSticker {
  val state = rememberPlaceholderState(isVisible = loading())
  Card(
    onClick = {},
    modifier = Modifier.width(172.dp).placeholderShimmer(state, CardDefaults.shape),
  ) {
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

// ---------------------------------------------------------------------------
// The media transport row — playback progress, and play/pause.
// ---------------------------------------------------------------------------
//
// A transport row is almost all motion: the ring fills as the track runs, and the middle button
// morphs shape as it is stopped and started. The Media controls cards publish four stills of that
// and none of it moves, which is what issue #60 asked for.
//
// WHICH ROW THE RECORDING USES IS NOT A FREE CHOICE. Horologist ships two transport rows and they
// are assembled from different middle buttons:
//
//   `MediaControlButtons`   -> `PlayPauseProgressButton`         — a `FilledIconButton` with a
//                                                                  `CircularProgressIndicator`
//                                                                  BEHIND it;
//   `PodcastControlButtons` -> `AnimatedPlayPauseProgressButton` — a 10-vertex scallop that morphs
//                                                                  against a circle, with a wavy
//                                                                  indicator drawn around it.
//
// The first hands ONE `modifier` to both the progress `Box` and the button inside it, so ring and
// container come out the same diameter and the opaque container covers the arc — the parity finding
// `MediaControls.kt` writes down. Its play/pause is a bare icon swap with nothing in between. Both
// halves were measured rather than assumed: a `PlayPauseProgressButton` flipped for a whole capture
// window came out at 4 distinct frames of 46, which is the placeholder's number and fails
// `CatalogRenderTest` for the same reason. So the recording is of the PODCAST row, which is where
// the library actually animates — and which is the preview the issue was filed from.
//
// WHY ONE RECORDING AND NOT TWO. `motionPreview` names ONE function per component, and both things
// worth showing belong to the same component, so they share a window: the ring sweeps 0→100% while
// the middle button is stopped and started under it. Splitting them would need a second component
// to claim the second GIF, and the only candidates are the two that cannot move.
//
// NEXT/PREVIOUS IS A PRESS, AND A PRESS NEEDS A FINGER
//
// The side buttons have no state to change: their whole motion is what happens under a pointer.
// That is `@InteractionPreview`'s job, and `MediaTransportMotion` below is this file's first use
// of it — see the note at the top of the file for why the annotation was unavailable here until
// recently, and what it replaced.
//
// It is worth recording what the workarounds cost before the annotation arrived, because both
// dead ends look reasonable from the code:
//
//  - Emitting `PressInteraction.Press` onto a `MutableInteractionSource` reaches the state layer
//    and NOT the animation. `MediaControlButtons`'s ripple is a platform `RippleDrawable`, which
//    runs on the main looper rather than on Compose's test clock, so a hand-driven press records a
//    component that changes colour once and then sits still: measured at 3 distinct frames of 46.
//    The interaction renderer advances the looper per frame, which is exactly the step that was
//    missing.
//  - Reaching the sources at all meant reassembling the row. `ButtonGroupLayout` publishes an
//    `interactionSources` parameter; the two rows one layer above it `remember` their own and take
//    no parameter, so a hand-driven press has to rebuild `MediaControlButtons` out of its parts —
//    and then the recording is of a replica rather than of the component.
//
// A real dispatched pointer needs neither: the renderer resolves the row's clickable nodes from the
// live semantics tree and presses them, so the composable under the recording is the one the card
// publishes.

/** How long a full 0→100% progress sweep takes in the recording below. */
private const val ProgressSweepMs = 2000

/**
 * The transport row under a finger: seek back, play/pause, seek forward — pressed in turn, with the
 * progress ring sweeping underneath the whole time.
 *
 * This is one recording covering everything the Media controls cards cannot show as stills, because
 * `motionPreview` gives a component one function and all of it belongs to this one component.
 *
 * **The press is a real dispatched pointer, and that is what makes the recording worth having.**
 * The renderer resolves the row's three clickable nodes from the live semantics tree and presses
 * each in turn, so what responds is `PodcastControlButtons` itself rather than a state this file
 * set on its behalf. Three separate things move as a result, none of which a still can carry:
 *
 * - the pressed button **swells** by `ButtonGroupLayoutDefaults.ExpansionWidth` and its neighbours
 *   yield the width — `AnimatedMediaControlButtons` opts into that with `ButtonGroupScope
 *   .animateWidth`, and it is the whole of what a side button does;
 * - the middle button **re-morphs**, because `AnimatedPlayPauseProgressButton` shapes itself on
 *   `isAnyButtonPressed` — pressing *either* neighbour changes it;
 * - pressing the middle one **actually pauses**, through the component's own `onPauseButtonClick`.
 *   The scallop resolves to a circle and the glyph swaps because playback stopped, not because a
 *   knob was turned.
 *
 * Progress runs on an `InfiniteTransition` rather than a clock. Horologist's
 * `TrackPositionUiModel.Predictive` would advance the ring from a `TimestampProvider`, which is the
 * non-deterministic input a nightly capture must not have; `ProgressStateHolder` writes whatever
 * percent it is handed straight through, so driving the model is the same picture and repeats
 * byte-for-byte.
 *
 * `PressAndHold` rather than `Tap`: these buttons draw their state layer through
 * `UnboundedRippleIconButton` — a ripple that grows past the button's own bounds — and a momentary
 * tap dispatches down and up in the same breath, so the grow and the fade collide into a flicker.
 * Neither button sets `onLongClick`, so a hold still fires the ordinary click on release.
 *
 * GIF rather than the annotation's default APNG, so the recording lands in the same lane as every
 * other one here and `CatalogRenderTest`'s "every motion capture actually moves" check can see it —
 * that test reads `*.gif`.
 */
@MotionRowCanvas
@InteractionPreview(
  gesture = InteractionGesture.PressAndHold,
  targets = [0, 1, 2],
  format = MotionFormat.Gif,
  caption =
    "Seek back, play/pause, seek forward pressed in turn: the pressed button swells and its " +
      "neighbours yield, the middle button re-morphs on any press, and pausing resolves its " +
      "scallop to a circle — all over a progress ring that never stops moving.",
)
@Composable
fun MediaTransportMotion() = MediaRowSticker {
  val percent by
    rememberInfiniteTransition(label = "playback")
      .animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
          infiniteRepeatable(tween(durationMillis = ProgressSweepMs, easing = LinearEasing)),
        label = "percent",
      )
  var playing by remember { mutableStateOf(true) }
  PodcastControlButtons(
    onPlayButtonClick = { playing = true },
    onPauseButtonClick = { playing = false },
    playPauseButtonEnabled = true,
    playing = playing,
    onSeekBackButtonClick = {},
    seekBackButtonEnabled = true,
    onSeekForwardButtonClick = {},
    seekForwardButtonEnabled = true,
    trackPositionUiModel = HorologistSamples.position(percent),
  )
}

// ---------------------------------------------------------------------------
// One-handed gestures — the affordance that only exists while it is playing.
// ---------------------------------------------------------------------------
//
// `OneHandedGestures.kt` publishes stills of all three indicators, pinned with
// `@SettledPreview(afterMs = 800)` to a frame in the middle of the animation. That is a phase, and
// a phase is the wrong shape for what these components are: each one BEGINS and ENDS on the
// picture it is not — a button showing its label, a plain scroll rail, plain page dots — and takes
// the screen over for about two seconds in between. The still says what the glyph looks like; only
// a recording says that the component gives the space back.
//
// All three are state-driven and none of them is waiting on a drag gesture: see the third bullet
// at the top of this file. `showIndicator()` is the public API the framework's own callback calls.

/**
 * How long one hint runs, end to end: the 450ms lead-in, the animated vector, the 200ms after it,
 * and the springs either side. Two of these fit in the capture window below, so a reader sees the
 * component hand the space back and take it again rather than a glyph that appears once and stops.
 */
private const val GESTURE_HINT_CAPTURE_MS = 5000

/**
 * Frame step for the gesture recordings, a multiple of 16 for the reason [PLACEHOLDER_FRAME_MS]
 * gives: the renderer rounds a nominal step up to a whole 16ms Compose frame while the GIF still
 * plays it for the nominal time, so anything else plays the hint faster than a wearer sees it.
 */
private const val GESTURE_HINT_FRAME_MS = 32

/**
 * The click indicator taking a button over and giving it back: the label fades, the double-pinch
 * glyph springs in and plays, and the label comes back.
 *
 * The pill does not move, and that is the design rather than a limitation of the capture — the
 * indicator is a `Layout` that measures the *content* and centres the glyph over it, so the button
 * keeps its shape and size while its label steps aside.
 *
 * **Copy that reads like an app, not [KitCopy]**, for the reason the placeholder recordings give: a
 * recording answers to no kit node, and a label that reappears reading `Primary label` says nothing
 * about what came back.
 */
@MotionCanvas
@AnimatedPreview(
  durationMs = GESTURE_HINT_CAPTURE_MS,
  frameIntervalMs = GESTURE_HINT_FRAME_MS,
  showCurves = false,
  caption =
    "The label fades out, the double-pinch glyph springs in and plays, and the label comes back — " +
      "the button keeps its shape throughout.",
)
@Composable
fun GestureHintMotion() = Sticker {
  val configuration =
    rememberOneHandedGestureConfiguration(
      action = OneHandedGestureAction.Primary,
      priority = OneHandedGesturePriority.Clickable,
    )
  val interactionSource = remember { MutableInteractionSource() }
  val state = remember { OneHandedGestureClickIndicatorState() }
  LaunchedEffect(Unit) {
    while (true) {
      delay(300)
      state.showIndicator()
    }
  }
  Button(
    onClick = {},
    interactionSource = interactionSource,
    modifier =
      Modifier.width(172.dp)
        .oneHandedGesture(
          gestureConfiguration = configuration,
          interactionSource = interactionSource,
          onGestureLabel = "start the run",
          onGesture = {},
        ),
  ) {
    OneHandedGestureClickIndicator(configuration, state) {
      // `fillMaxWidth()` so the glyph centres in the BUTTON rather than over the label: the
      // indicator measures its content and centres the icon on that. See `OneHandedGestures.kt`.
      Text("Start run", modifier = Modifier.fillMaxWidth())
    }
  }
}

/**
 * The scroll rail announcing that a pinch will scroll: the thumb picks up its gesture colours, the
 * glyph grows out of the bezel, and the rail jiggles down and back before everything recedes.
 *
 * The jiggle is the part worth the recording. `OneHandedGestureScrollIndicatorState` runs it as two
 * spring animations against the indicator's own `jiggleAmount` — down by half a thumb, then back —
 * so the rail demonstrates the direction the gesture would take the list. A still catches it at one
 * offset, which reads as a rail drawn in the wrong place.
 *
 * The list is empty rows, as the sticker's is and for the same reason (`Indicators.kt`): the
 * indicator sizes its thumb from how much of the content fits on screen, so it needs a list that
 * really overflows, and labels would be the loudest thing in a frame whose subject is the bezel.
 */
@MotionScreenCanvas
@AnimatedPreview(
  durationMs = GESTURE_HINT_CAPTURE_MS,
  frameIntervalMs = GESTURE_HINT_FRAME_MS,
  showCurves = false,
  caption =
    "The scroll rail takes its gesture colours, grows a glyph, and jiggles down and back to show " +
      "which way a pinch would move the list.",
)
@Composable
fun GestureScrollHintMotion() {
  val configuration =
    rememberOneHandedGestureConfiguration(
      action = OneHandedGestureAction.Primary,
      priority = OneHandedGesturePriority.Scrollable,
    )
  val scrollState = rememberTransformingLazyColumnState()
  val state = remember { OneHandedGestureScrollIndicatorState() }
  LaunchedEffect(Unit) {
    while (true) {
      delay(300)
      state.showIndicator()
    }
  }
  Box(Modifier.fillMaxSize()) {
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
      items(12) { Spacer(Modifier.height(48.dp)) }
    }
    // Aligned from OUTSIDE the component: it forwards its `modifier` to the rail rather than to
    // its own root, so a `BoxScope.align` handed to it lands on a child of its internal `Row` and
    // is ignored — see the longer note in `OneHandedGestures.kt`.
    Box(Modifier.align(Alignment.CenterEnd)) {
      OneHandedGestureScrollIndicator(
        gestureConfiguration = configuration,
        indicatorState = state,
        scrollState = scrollState,
      )
    }
  }
}

/**
 * The page dots doing the same thing along the bottom of the display: a pill grows above them,
 * plays the glyph, and collapses back into the row of dots.
 *
 * Its own recording rather than a cell of the scroll one, because the two components place the
 * affordance differently — `OneHandedGestureHorizontalPageIndicator` stacks the pill *above* the
 * dots in a `Column` and scales it from their top edge, where the scroll indicator grows out of the
 * rail itself. That is the thing a reader of a page indicator is asking about.
 */
@MotionScreenCanvas
@AnimatedPreview(
  durationMs = GESTURE_HINT_CAPTURE_MS,
  frameIntervalMs = GESTURE_HINT_FRAME_MS,
  showCurves = false,
  caption =
    "A pill grows above the page dots, plays the pinch glyph, and collapses back into the row.",
)
@Composable
fun GesturePageHintMotion() {
  val configuration =
    rememberOneHandedGestureConfiguration(
      action = OneHandedGestureAction.Primary,
      priority = OneHandedGesturePriority.Scrollable,
    )
  val pagerState = rememberPagerState(initialPage = 1) { 4 }
  val state = remember { OneHandedGesturePageIndicatorState() }
  LaunchedEffect(Unit) {
    while (true) {
      delay(300)
      state.showIndicator()
    }
  }
  Box(Modifier.fillMaxSize()) {
    Box(Modifier.align(Alignment.BottomCenter)) {
      OneHandedGestureHorizontalPageIndicator(
        gestureConfiguration = configuration,
        indicatorState = state,
        pagerState = pagerState,
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Components whose subject IS the animation — a still of one is a frame of it, and the card says
// so. Four recordings, all state-driven for the reason each one names.
// ---------------------------------------------------------------------------

/**
 * The label growing a line at a time, with each new line fading in as the button expands to hold
 * it.
 *
 * `FadingExpandingLabel` animates on a text CHANGE — it measures the new string, springs the height
 * to it and wipes the arriving line in — so the recording changes the text rather than a knob. Both
 * directions, because a button that only ever grows records half of what an app does with it: a
 * label that shrinks back plays the same animation in reverse and settles differently.
 *
 * `@AnimatedPreview` rather than `@InteractionPreview`: nothing is pressed here. The text changes
 * because the app got a new value, which is the situation the component is published for, and there
 * is no gesture that would stand in for it.
 */
@MotionRowCanvas
@AnimatedPreview(
  showCurves = false,
  caption =
    "One line, then three, then back: the button's height springs to the new text while each " +
      "arriving line fades in under the wipe.",
)
@Composable
fun FadingExpandingLabelMotion() = Sticker {
  var lines by remember { mutableStateOf(1) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(700)
      lines = if (lines == 1) 3 else 1
    }
  }
  // The component sticker's own wiring (`TextComponents.kt`'s `FadingLabel`), down to the width:
  // the container growing is half of what is being recorded, and a button that wraps its label
  // would grow sideways instead.
  Button(
    onClick = {},
    modifier = Modifier.width(172.dp),
    label = { FadingExpandingLabel(text = fadingLabelText(lines)) },
  )
}

/**
 * The numeral travelling: `AnimatedText` interpolating its variable font's weight axis and its size
 * between the two ends the card publishes as `start` and the base cell.
 *
 * **The component is the interpolation**, which is why the card's two stills are cells of a
 * recording rather than states: `AnimatedText` takes a progress lambda instead of a text style
 * precisely because there is no "state" to draw — an app hands it an `Animatable` and the digit
 * counts. This drives the same lambda from an `InfiniteTransition`, which is the honest stand-in:
 * the font registry, the shaping and the size lerp are all the component's, and only the clock is
 * ours.
 *
 * Reversing rather than restarting. The ends are a light 30sp and a heavy 48sp, and a sawtooth
 * would spend a frame of every cycle teleporting between them — which reads as a glitch rather than
 * as the travel a designer is choosing.
 */
@RequiresApi(31)
@MotionCanvas
@AnimatedPreview(
  showCurves = false,
  caption =
    "The numeral thickens along the font's weight axis and grows from 30sp to 48sp, then plays " +
      "back down again.",
)
@Composable
fun AnimatedTextMotion() = Sticker {
  val transition = rememberInfiniteTransition(label = "animated-text")
  val progress by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 700, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "progress",
    )
  AnimatedNumeralText { progress }
}

/**
 * A horizontal pager paging: the outgoing page scales down and takes a scrim while the incoming one
 * grows into place, and the scaffold's dots follow.
 *
 * A still of a pager is a still of one page — which is a picture of whatever that page contains and
 * says nothing about the component. The transition is the whole of what `HorizontalPagerScaffold`
 * and `AnimatedPage` add over a `Row` of screens, so it belongs here and the card claims it.
 *
 * **State-driven, and it is the drag limitation `Motion.kt` already names twice.**
 * `@InteractionPreview` dispatches taps and holds; paging is a horizontal drag, and a press that
 * never travels pages nothing. `animateScrollToPage` moves the component's own `PagerState` through
 * the same animation a fling would, so the spring, the scrim and the indicator's own show/hide are
 * real and only the finger is scripted.
 *
 * Forward and back, because the scaffold's furniture is not symmetric: the page indicator fades in
 * as paging starts and out as it settles, and one pass records the fade-in once.
 */
@MotionScreenCanvas
@AnimatedPreview(
  showCurves = false,
  caption =
    "Paged forward and back: the leaving page scales down under a scrim, the arriving one grows " +
      "in, and the dots move with them.",
)
@Composable
fun PagerTransitionMotion() = ScreenSticker {
  val state = pagerState(pages = 4, initialPage = 0)
  LaunchedEffect(Unit) {
    while (true) {
      delay(400)
      state.animateScrollToPage(2)
      delay(400)
      state.animateScrollToPage(0)
    }
  }
  HorizontalPagerScaffold(pagerState = state) {
    HorizontalPager(state = state) { page ->
      AnimatedPage(pageIndex = page, pagerState = state) { PagerPage(page, pages = 4) }
    }
  }
}

/**
 * The same transition up and down, for the vertical pager.
 *
 * Its own recording rather than a cell of the horizontal one, for the reason the two cards are two
 * cards: `VerticalPagerScaffold` places its indicator against the right bezel and travels its pages
 * on the other axis, and which of those a reader is looking at is the question they came with.
 */
@MotionScreenCanvas
@AnimatedPreview(
  showCurves = false,
  caption =
    "The same paging vertically, with the dots riding the right bezel as the pages scale past " +
      "each other.",
)
@Composable
fun VerticalPagerTransitionMotion() = ScreenSticker {
  val state = pagerState(pages = 4, initialPage = 0)
  LaunchedEffect(Unit) {
    while (true) {
      delay(400)
      state.animateScrollToPage(2)
      delay(400)
      state.animateScrollToPage(0)
    }
  }
  VerticalPagerScaffold(pagerState = state) {
    VerticalPager(state = state) { page ->
      AnimatedPage(pageIndex = page, pagerState = state) { PagerPage(page, pages = 4) }
    }
  }
}
