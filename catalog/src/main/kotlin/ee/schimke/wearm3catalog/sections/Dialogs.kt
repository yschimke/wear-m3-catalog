@file:CatalogGroup(name = "Dialogs", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
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
// THE EDGE-BUTTON CELL CARRIES A SCROLL, EVEN THOUGH NOTHING SCROLLS
//
// `AlertDialogContent(edgeButton = …)` is a `ScreenScaffold` with an `edgeButton` slot, and the
// scaffold sizes that button from the list's `lastItemOffset`: it seeds an `Animatable` with the
// value read on the *first* composition — 0, because the list has not laid out yet — and settles it
// from a `LaunchedEffect` afterwards. A static capture IS that first frame, so the cell published a
// title over an empty bottom half (issue #15).
//
// `@ScrollingPreview(ScrollMode.END)` is the fix, the same one `EdgeButtonScreen` carries. It reads
// oddly on a one-line dialog with nothing to scroll, and the scroll genuinely is a no-op here — but
// the driver advances the renderer's `MainTestClock` to step the scroll, and that is what lets the
// reveal settle before the capture. It sits on the component rather than the cell because a
// `@Preview` annotation is per-function; the confirm/dismiss and no-buttons cells lay out at rest
// and are unchanged by it.
//
// THE CONFIRMATION OVERLAY IS OUT, AND THIS IS WHY
//
// `ConfirmationDialogContent` reveals itself over time: the children start at `alpha = 0` and
// animate in from a `LaunchedEffect` after a delay, and `ConfirmationDialogDefaults.SuccessIcon()`
// is an `AnimatedImageVector` whose checkmark is drawn by a 267ms `trimPathEnd` tween that only
// starts after its own 100ms delay. Recorded as a GIF the whole reveal takes **~528ms** to reach
// its resting frame.
//
// The renderer never gets that far on a still. It does advance a little — enough that "the clock
// is paused, so the capture is frame zero" is too simple an account — but not nearly enough, and
// it fails in two different ways depending on where the capture lands:
//
// - **Plain sticker: the empty ring.** The still path proves quiescence by capturing twice and
//   comparing pixels, and returns as soon as two frames match. The first ~33ms of this component
//   are the reveal's own start delay, where nothing moves — so the check reads "settled", reports
//   no warning, and publishes the empty container: no icon, no curved text.
// - **`@ScrollingPreview(END)`: the chevron.** Enough virtual time passes to bring the container
//   and the curved text in, but the capture lands mid-tween and the checkmark reads as a chevron
//   rather than a check. The renderer says so on stderr ("did not become visually quiescent after
//   5 samples") and publishes it anyway. Note this is not scrolling: there is no scrollable in the
//   sticker, the drive logs "no scrollable composable found", and `maxScrollPx` / `frameIntervalMs`
//   change the output not at all — byte-identical PNGs. The advance is incidental, so tuning those
//   knobs is not a route to a settled capture.
//
// The renderer already owns a settle long enough to finish the job — a 1000ms frame-by-frame
// advance it runs after a scroll lands, for exactly this class of animation. Giving the probe a
// dummy sibling scrollable so that settle fires renders the component **perfectly**: full check,
// container at rest, curved text in. So nothing is missing from the renderer's capability, only a
// way to ask for it without a scroll — and a sticker that ships a fake scroller to buy clock time
// is the kind of workaround AGENTS.md sends upstream instead. Tracked as
// [yschimke/compose-ai-tools#4202](https://github.com/yschimke/compose-ai-tools/issues/4202); the
// set is recorded as excluded in kit-sets.json rather than dropped silently, so it comes back the
// day a preview can ask to be captured settled.
//
// `@AnimatedPreview` is not the answer either: it yields a motion artifact beside the same
// unsettled still, so the component's own card still shows the empty frame.
//
// `OpenOnPhoneDialog` is touched by the same gap but stays, and the distinction is worth writing
// down. Its icon is an animated vector too, so the published sticker catches the arrow mid-draw as
// a stub rather than a full arrow into the phone — the same 5-sample warning is logged for it. It
// stays because what it publishes is still recognisably the component, and because "settled" is
// not obviously the right frame for it anyway: the progress ring IS the wait, and a capture taken
// after the ring completes is a picture of the wait being over. The confirmation overlay has no
// such defence — unsettled, it publishes nothing at all.
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

@CatalogComponent(
  id = "AlertDialog",
  reference = "figma:B24oss2tTeXAFykyeyusz0/58475:87077",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/58475:87022",
  caption = "Interrupts to ask for a decision; the kit's button arrangements fold in as cells.",
)
@CatalogFullScreenModes
@ScrollingPreview(modes = [ScrollMode.END])
@OverrideVariant(
  name = "edge-button",
  strings = ["edge=single"],
  kitAxis = "Edge Option",
  kitValue = "Edge Button",
)
@OverrideVariant(
  name = "no-buttons",
  strings = ["edge=none"],
  kitAxis = "Edge Option",
  kitValue = "None",
)
@Composable
fun AlertDialogSticker() = FullScreenSticker {
  val icon: (@Composable () -> Unit)? =
    if (previewOverrideBoolean("icon", true)) {
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

  when (previewOverrideChoice("edge", "double", listOf("double", "single", "none"))) {
    "single" ->
      AlertDialogContent(
        edgeButton = { AlertDialogDefaults.EdgeButton(onClick = {}) },
        title = { Text(kitCopy("title", KitCopy.DIALOG_TITLE)) },
        icon = icon,
      )
    "none" ->
      AlertDialogContent(title = { Text(kitCopy("title", KitCopy.DIALOG_TITLE)) }, icon = icon)
    else ->
      AlertDialogContent(
        confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
        dismissButton = { AlertDialogDefaults.DismissButton(onClick = {}) },
        title = { Text(kitCopy("title", KitCopy.DIALOG_TITLE)) },
        icon = icon,
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
@Composable
fun OpenOnPhoneDialogSticker() = FullScreenSticker {
  val style = OpenOnPhoneDialogDefaults.curvedTextStyle
  // Read outside the slot: `curvedText` is a `CurvedScope` lambda, not a `@Composable` one, so a
  // composable call inside it does not compile.
  val text = kitCopy("curvedText", KitCopy.OPEN_ON_PHONE)
  OpenOnPhoneDialogContent(
    curvedText = { openOnPhoneDialogCurvedText(text, style) },
    durationMillis = OpenOnPhoneDialogDefaults.DurationMillis,
  ) {
    OpenOnPhoneDialogDefaults.Icon()
  }
}
