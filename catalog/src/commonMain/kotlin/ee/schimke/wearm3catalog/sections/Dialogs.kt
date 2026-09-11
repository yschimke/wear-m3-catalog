@file:CatalogGroup(name = "Dialogs", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.CurvedScope
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.ConfirmationDialogContent
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.FailureConfirmationDialogContent
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.SuccessConfirmationDialogContent
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.confirmationDialogCurvedText
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Dialogs` and `Confirmation Overlays` pages.
//
// THE `*DialogContent` CARVE-OUT
//
// Every sticker in this catalog invokes the real named Wear Material 3 composable — a replica built
// from `Surface` and `Column` can be made to line up, but it cannot test the library and so cannot
// be defended as a comparison. Dialogs are the case where that rule meets a renderer limit: the
// `AlertDialog` / `ConfirmationDialog` / `OpenOnPhoneDialog` entry points open a platform dialog
// window, and a capture of the composition does not include that window's surface.
//
// Wear Compose publishes the answer itself. `AlertDialogContent`, `ConfirmationDialogContent`,
// `OpenOnPhoneDialogContent` and friends are public API carrying the entire Material layout — the
// same code the windowed entry point composes inside its window. So the sticker draws the real
// component and only the platform window is missing, which is the same trade the phone catalog
// makes with its inline dialog host.
//
// The kit's `Edge Option=` axis is the dialog's button arrangement, and it is a slot in Compose
// (`edgeButton`, or the confirm/dismiss pair) rather than a property, so it folds as content cells.
//
// THREE OF THE KIT'S TEN ARRANGEMENTS ARE THE LIBRARY'S; THE REST ARE AN APP'S
//
// `AlertDialogContent` publishes exactly three bottom arrangements: an `edgeButton`, the
// confirm/dismiss pair (`AlertDialogDefaults.ConfirmButton` / `DismissButton`, the rotated -45
// degree pair the kit calls `Double Angled Button`), and neither. Those are the three cells below.
//
// `Double Button Fixed`, `Double Button Flex`, `Double Button Sml` and `Single Button Sml` are
// rows of ordinary buttons under the title — a layout an app writes into the content slot, not an
// arrangement the component offers, and a sticker that hand-built one would be a picture of this
// file rather than of the library. The remaining three (`Double Button Stack`, and the two
// `... + Edge Button Stack`) are published only as `Scrolling=Yes` long-scroll cells, which
// nothing here can be diffed against for the reason the note at the bottom of this comment gives.
//
// THE EDGE-BUTTON CELL NEEDS THE CLOCK ADVANCED, AND USED TO BUY IT WITH A SCROLL
//
// `AlertDialogContent(edgeButton = …)` is a `ScreenScaffold` with an `edgeButton` slot, and the
// scaffold sizes that button from the list's `lastItemOffset`: it seeds an `Animatable` with the
// value read on the *first* composition — 0, because the list has not laid out yet — and settles it
// from a `LaunchedEffect` afterwards. A static capture IS that first frame, so the cell published a
// title over an empty bottom half (issue #15).
//
// `@ScrollingPreview(ScrollMode.END)` was the fix, and it read oddly for the reason it worked: the
// scroll is a genuine no-op on a one-line dialog, and what the sticker actually wanted was the
// clock advance the scroll driver happens to perform. `@SettledPreview` asks for that directly, so
// it is what this carries now — the same picture, without a scroll standing in for a settle
// ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
//
// THE CONFIRMATION OVERLAY WAS OUT, AND WHAT BROUGHT IT BACK
//
// `ConfirmationDialogContent` reveals itself over time: the children start at `alpha = 0` and
// animate in from a `LaunchedEffect` after a delay, and `ConfirmationDialogDefaults.SuccessIcon()`
// is an `AnimatedImageVector` whose checkmark is drawn by a 267ms `trimPathEnd` tween that only
// starts after its own 100ms delay. Recorded as a GIF the whole reveal takes **~528ms** to reach
// its resting frame.
//
// A still never got that far, and it failed in two different ways depending on where the capture
// landed. The plain sticker published the **empty ring**: the still path proves quiescence by
// capturing twice and comparing, and the reveal's own start delay reads as "settled" before
// anything has moved. `@ScrollingPreview(END)` bought enough virtual time for the container and
// the curved text but landed mid-tween, publishing the check as a **chevron**. Neither is the
// component, so the whole set was excluded rather than drawn wrong.
//
// The measurement in that account also named the fix: the renderer already owned a 1000ms
// frame-by-frame settle, and what was missing was a way to ask for it without a scroll. That is
// `@SettledPreview`
// ([compose-ai-tools#4202](https://github.com/yschimke/compose-ai-tools/issues/4202),
// shipped), and it lands the reveal exactly where the note predicted: full check, container at
// rest, curved text in. The three overlays at the bottom of this file are the set, and
// `kit-sets.json` carries it as implemented rather than excluded
// ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
//
// `@AnimatedPreview` was never the answer: it yields a motion artifact BESIDE the still, so the
// component's own card would still have shown the empty frame.
//
// `OpenOnPhoneDialog` carries the same annotation for the same reason, and it is the one place
// where settling changed a published picture rather than rescuing it. Its icon is an animated
// vector too, so the sticker used to catch the arrow mid-draw as a stub; more to the point its
// `Text=No` cell was byte-identical to the base, because the curved text was missing from both
// frames whether or not it was passed. That collapse was recorded in
// `CatalogRenderTest.knownDuplicate` and is gone: the two cells are a real comparison now, which
// is what the entry said would happen the day a preview could ask to be captured settled.
//
// THE ICON IS PART OF THE LAYOUT, NOT DECORATION ON TOP OF IT
//
// Every `Dialog` cell the kit publishes draws an icon above the title — it lives in the shared
// `.Base / Dialog / Top` (`58475:87080`), so `Double Angled Button`, `Edge Button` and `None` all
// carry it and there is no axis that turns it off. The sticker passed no `icon`, and the cost was
// not only a missing glyph: the icon is a row of the layout, so without it the title rose to fill
// the space — it sat at 42–71dp against the kit's 62–91dp, 20dp high, with the buttons already
// landing on the kit's own row ([#74](https://github.com/yschimke/wear-m3-catalog/issues/74)).
//
// Three numbers come off the kit rather than off a Compose default, because the library supplies
// none — `AlertDialogContent` hands its `icon` slot to the caller unstyled:
//
//  - **32dp**, not `Icon`'s 24dp default. The kit's `Icon` instance is a 32×32 box, and the disc
//    inside it measures 26.67 — the 20-of-24 Material grid scaled to 32.
//  - **`secondaryDim`**. The kit fills the glyph `#BAC3FF`, which is `Secondary80`, which is what
//    `ColorTokens.SecondaryDim` resolves to. It is deliberately not the `primary` (`#E9DDFF`) the
//    confirm button beside it is filled with.
//  - **`Icons.Filled.Info`** stands in for the kit's `error` glyph — the same disc on the same
//    grid, with a different shape knocked out of it. This catalog depends on `material-icons-core`
//    only, which publishes `Info` and `Warning` but no `Error`, and one glyph is not a reason to
//    pull the ~1400-icon extended artifact in.
//
// It is a knob (`icon`, default on) rather than a cell: a cell resolves against a kit axis, and the
// kit has no axis here for it to resolve against.
//
// **~9dp of the gap is Wear Compose's, and it stays.** With the icon in, the block lands 8–9dp
// above the kit's row (icon disc 13.5–39dp against 23–48.5dp, title 53.5–83dp against 62–91.5dp;
// the buttons match to half a dp). The cause is `AlertDialogDefaults`: adding an icon swaps the
// top padding from `verticalContentPadding()` (10% of the screen — 20dp here) to
// `screenHeightFraction(iconTopPaddingFraction)`, 1.2%, or 3dp. That trade reads as top-anchored,
// but the fixed layout gives its content column `weight(1f)` and centres the block inside it, so
// half of the 17dp it gave up comes straight off the block's centre. Feed the same layout the
// kit's 20dp and it lands on 19.3dp, i.e. on the kit.
//
// It is not papered over here. Passing a hand-built `contentPadding` would line the picture up
// while no longer drawing what a caller of `AlertDialogContent` gets, which is the one thing a
// comparison in this catalog is for — so the sticker keeps the library's default and the residual
// is written down instead.
//
// TWO THINGS THE INSPECTION OVERLAYS SHOW ON THIS STICKER THAT NOTHING HERE ASKED FOR
//
// Both are `AlertDialogContent`'s own structure rather than anything this file does, and both were
// asked about from the preview page rather than read off the picture — so they are written down
// here, where the next reader of those overlays will look.
//
// **The unlabelled clickable around the confirm button is a SECOND button, and it is the
// library's** ([#76](https://github.com/yschimke/wear-m3-catalog/issues/76)). Tick
// **Accessibility** and the confirm button carries two boxes: an outer `(unlabelled)` stop at
// 83×83dp and an inner `Confirm` stop at 68×68dp inside it. The dismiss button carries one.
//
// `AlertDialogDefaults.ConfirmButton` is a `FilledIconButton` sized 63×54dp and rotated **-45°**
// — which is why the outer box is a square 83dp, the bounding box of that rotated rect, and why
// the shape reads as a leaning cookie rather than a circle. Its content is a `Row` counter-rotated
// +45° so the check stands upright, and that `Row` carries
// `Modifier.semantics(mergeDescendants = true) { onClick(…); role = Role.Button }` of its own. So
// the tree really does hold two nested merged, clickable `Button` nodes: the outer one is the
// `FilledIconButton`, and the copy — the check `Icon`'s `contentDescription`, "Confirm" — sits
// in the inner one. A label does not roll up across a nested stop (it belongs to that stop's own
// announcement), so the outer stop has nothing to announce and the overlay says so.
//
// That is a real finding rather than an artifact of the overlay: TalkBack presents the same two
// nodes, the first of them unlabelled. It is upstream's to fix — the sticker calls
// `AlertDialogDefaults.ConfirmButton`, which is the confirm slot's own default, and swapping in a
// hand-built button to tidy the tree would stop the comparison testing the library (the
// `*DialogContent` carve-out above). Recorded here, not worked around.
//
// **The second typography element was a copy of the dialog the layout only measured**
// ([#77](https://github.com/yschimke/wear-m3-catalog/issues/77)). `AlertDialogContent` picks
// between a scrolling and a fixed layout by subcomposing a whole trial copy of itself and
// measuring its unconstrained height (`DynamicScrollableOrFixedLayout`), then placing only the
// arrangement it chose. The trial copy is `clearAndSetSemantics {}`-wrapped but still in the
// semantics tree, and an unplaced node has no position — so it reported `0,0` and the Typography
// layer drew a second title in the frame's top-left corner. Nothing to fix here: the tree gained a
// `placed` flag upstream and the overlays now skip what was never placed
// ([compose-ai-tools#4432](https://github.com/yschimke/compose-ai-tools/pull/4432)). Worth knowing
// anyway, because it is why this component appears twice in any dump of its semantics.
//
// THE REFERENCE IS A `Scrolling=No` CELL, AND THAT IS THE WHOLE POINT
//
// The `Dialog` set varies `Scrolling=` as well as `Edge Option=`, and the two values are not two
// states of one picture — they are two different KINDS of artwork:
//
//  - `Scrolling=Yes` cells are **192×402** (and up to 192×500). They are long-scroll captures: the
//    dialog's entire scrollable content unrolled down the page, past the bottom of any watch.
//  - `Scrolling=No` cells are **192×192** — the round display, the frame a watch actually has.
//
// This component publishes one 454×454 capture of a round screen, so only the second kind is
// comparable. Pointed at `58475:87041` (`Scrolling=Yes`) it was diffing a screen against a
// three-screens-tall strip: the comparison squashes the reference to the render's aspect to line
// them up, so every element in it was ~2.1× too short and NOTHING matched — a finding that says
// only "these are different shapes". `58475:87077` is the same `Edge Option=Double Angled Button`
// arrangement drawn on the display, which is what this sticker is a picture of.
//
// The kit's own scroll captures are not wrong and are not being ignored — they are simply not what
// a device-framed render can be diffed against. Publishing them would need a preview that captures
// its own full scroll extent rather than the display; see `docs/DESIGN_MAP.md`.

/** The kit's `Edge Option` axis: which button treatment the alert closes on. */
enum class AlertEdge {
  @KnobValue("double") Double,
  @KnobValue("single") Single,
  @KnobValue("none") None,
}

@CatalogComponent(
  id = "AlertDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/58475:87077",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/58475:87022",
  caption = "Interrupts to ask for a decision; the kit's button arrangements fold in as cells.",
)
@CatalogFullScreenModes
@SettledPreview
// Both cells declare `Scrolling=No` with the arrangement, because the axes are coupled: the
// comparable half of this set is its 192x192 column (see the note above), and `Edge Option=None`
// is drawn there with `Bottom=No` — the only value of that axis it is published under. Naming the
// arrangement alone asked for a node between the ones the kit drew, and `no-buttons` resolved to
// nothing.
@OverrideVariant(
  name = "edge-button",
  strings = ["edge=single"],
  kitProps = ["Edge Option=Edge Button", "Scrolling=No", "Bottom=Yes"],
)
@OverrideVariant(
  name = "no-buttons",
  strings = ["edge=none"],
  kitProps = ["Edge Option=None", "Scrolling=No", "Bottom=No"],
)
@Composable
fun AlertDialogSticker(
  icon: Boolean = true,
  edge: AlertEdge = AlertEdge.Double,
) = FullScreenSticker {
  val iconSlot: (@Composable () -> Unit)? =
    if (icon) {
      {
        Icon(
          Icons.Filled.Info,
          contentDescription = null,
          modifier = Modifier.size(AlertIconSize),
          tint = MaterialTheme.colorScheme.secondaryDim,
        )
      }
    } else {
      null
    }

  when (edge) {
    AlertEdge.Single ->
      AlertDialogContent(
        edgeButton = { AlertDialogDefaults.EdgeButton(onClick = {}) },
        title = { Text(kitCopy("title", KitCopy.DIALOG_TITLE)) },
        icon = iconSlot,
      )
    AlertEdge.None ->
      AlertDialogContent(title = { Text(kitCopy("title", KitCopy.DIALOG_TITLE)) }, icon = iconSlot)
    AlertEdge.Double ->
      AlertDialogContent(
        confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
        dismissButton = { AlertDialogDefaults.DismissButton(onClick = {}) },
        title = { Text(kitCopy("title", KitCopy.DIALOG_TITLE)) },
        icon = iconSlot,
      )
  }
}

/** The kit's alert icon box (`.Base / Dialog / Top` → `Icon`), not `Icon`'s own 24dp default. */
private val AlertIconSize = 32.dp

@CatalogComponent(
  id = "OpenOnPhoneDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/46964:91112",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/46964:90920",
  caption = "Hands the task to the paired phone, with the progress the wait needs.",
)
@CatalogFullScreenModes
// The `Text=No` cell is `curvedText = null`, which the `text` knob below turns. It used to be a
// COLLAPSE rather than a difference — the still was taken before the reveal ran, so the curved
// text was missing either way — and `@SettledPreview` is what separated the two pictures.
@OverrideVariant(
  name = "no-text",
  booleans = ["text=false"],
  kitAxis = "Text",
  kitValue = "No",
)
@SettledPreview
@Composable
fun OpenOnPhoneDialogSticker(text: Boolean = true) = FullScreenSticker {
  val style = OpenOnPhoneDialogDefaults.curvedTextStyle
  // Read outside the slot: `curvedText` is a `CurvedScope` lambda, not a `@Composable` one, so a
  // composable call inside it does not compile.
  val copy = kitCopy("curvedText", KitCopy.OPEN_ON_PHONE)
  OpenOnPhoneDialogContent(
    curvedText = if (text) ({ openOnPhoneDialogCurvedText(copy, style) }) else null,
    durationMillis = OpenOnPhoneDialogDefaults.DurationMillis,
  ) {
    OpenOnPhoneDialogDefaults.Icon()
  }
}

// The kit's `Confirmation-Overlay` set, back after being excluded outright.
//
// The exclusion was never about the components: `ConfirmationDialogContent` and its two typed
// siblings reveal themselves over ~528ms, and nothing could ask a still to wait that long, so the
// sticker published an empty container ring. The note at the top of this file has the whole
// account. `@SettledPreview` is the thing it was waiting for — it advances the paused clock until
// the composition stops changing, which is exactly the 1000ms settle the note measured the reveal
// against.
//
// THREE COMPONENTS, NOT ONE, and the kit's own axis is why the split is the taxonomy's rather than
// this file's: `Type=` here is three separate Wear Compose functions —
// `SuccessConfirmationDialogContent`, `FailureConfirmationDialogContent` and the generic
// `ConfirmationDialogContent` that takes the icon as a slot. Which one you call is the choice a
// reader is making, which is the carve-out AGENTS.md states for `Style=` on `Button`.
//
// THE `Text=No` CELLS ARE DRAWN NOW, and what unblocked them was the kit INDEX rather than the
// library. `curvedText = null` was always right there as an argument and the `text` knob below
// always turned it, but `figma-kit-index.json` is built from the sets this catalog maps and had
// last been walked while this one was excluded — so it carried no `Confirmation-Overlay` entry,
// and a cell naming `Text=No` had no set to vary that axis within. It resolved to nothing, which
// AGENTS.md rates worse than an absence, so the cells waited. Re-running `figma-refs.yml` with
// three components naming the set added it: one set, nine cells, 888 kit cells to 897, and
// nothing else in the index moved.
//
// The kit's other two types stay out and say why on the `kit-sets.json` row: `Latency` is the wait
// before an outcome is known, which no `*ConfirmationDialogContent` draws, and `Long text` is the
// same overlay carrying more copy than fits — a property of the string rather than of the call.

@CatalogComponent(
  id = "ConfirmationDialog/Success",
  reference = "figma:B24oss2tTeXAFykyeyusz0/47251:36766",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/47251:36765",
  caption = "It worked: the check drawn on the theme's success container, over curved copy.",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "no-text",
  booleans = ["text=false"],
  kitAxis = "Text",
  kitValue = "No",
)
@SettledPreview
@Composable
fun SuccessConfirmation() = FullScreenSticker {
  SuccessConfirmationDialogContent(curvedText = confirmationText(KitCopy.CONFIRMATION_SUCCESS))
}

@CatalogComponent(
  id = "ConfirmationDialog/Failure",
  reference = "figma:B24oss2tTeXAFykyeyusz0/47251:36863",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/47251:36765",
  caption = "It did not: the same overlay in the failure palette, with the library's own glyph.",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "no-text",
  booleans = ["text=false"],
  kitAxis = "Text",
  kitValue = "No",
)
@SettledPreview
@Composable
fun FailureConfirmation() = FullScreenSticker {
  FailureConfirmationDialogContent(curvedText = confirmationText(KitCopy.CONFIRMATION_FAILED))
}

@CatalogComponent(
  id = "ConfirmationDialog/Generic",
  reference = "figma:B24oss2tTeXAFykyeyusz0/47251:36801",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/47251:36765",
  caption = "The overlay an app brings its own glyph to, which is the icon slot this one takes.",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "no-text",
  booleans = ["text=false"],
  kitAxis = "Text",
  kitValue = "No",
)
@SettledPreview
@Composable
fun GenericConfirmation() = FullScreenSticker {
  ConfirmationDialogContent(curvedText = confirmationText(KitCopy.CONFIRMATION_GENERIC)) {
    // The kit draws a plus here — the same stand-in glyph its `Generic` cell carries, and the one
    // this catalog uses wherever the content is the caller's rather than the component's.
    Icon(
      Icons.Filled.Add,
      contentDescription = null,
      modifier = Modifier.size(ConfirmationDialogDefaults.IconSize),
    )
  }
}

/**
 * The curved text slot the three overlays share, or `null` when the `text` knob turns it off.
 *
 * Read outside the slot for the reason `OpenOnPhoneDialogSticker` states: `curvedText` is a
 * `CurvedScope` lambda rather than a `@Composable` one, so a composable call inside it does not
 * compile.
 */
@Composable
private fun confirmationText(kit: String): (CurvedScope.() -> Unit)? {
  val style = ConfirmationDialogDefaults.curvedTextStyle
  val text = kitCopy("curvedText", kit)
  return if (previewOverrideBoolean("text", true)) {
    { confirmationDialogCurvedText(text, style) }
  } else null
}
