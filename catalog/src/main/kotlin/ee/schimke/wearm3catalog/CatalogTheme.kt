package ee.schimke.wearm3catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.timeTextCurvedText

/**
 * The Wear [MaterialTheme] every frame below installs — unless a named theme already installed one.
 *
 * A `@WearThemeCatalog` provider (see `CatalogThemes.kt`) wraps the sticker from the *outside*, so
 * a bare `MaterialTheme { … }` in here would shadow it and reset the palette and type scale it just
 * chose. The result is not an error and not obviously wrong on screen: every entry in the preview
 * server's theme select renders byte-identical pixels, and every generated specimen sheet reports
 * the stock Wear palette. Standing down when [LocalCatalogThemeOverride] is set is what makes a
 * theme choice reach the component stickers themselves and not only the specimen sheets.
 *
 * Absent a provider this is exactly the stock `MaterialTheme`, so an un-themed render is unchanged.
 */
@Composable
private fun CatalogMaterialTheme(content: @Composable () -> Unit) {
  if (LocalCatalogThemeOverride.current) content() else MaterialTheme(content = content)
}

/**
 * True while a `@WearThemeCatalog` provider owns the Wear Material theme wrapping this sticker. Set
 * only by the providers in `CatalogThemes.kt`; read only by [CatalogMaterialTheme].
 */
internal val LocalCatalogThemeOverride = staticCompositionLocalOf { false }

/**
 * The catalog's sticker frame: one component in the stock Wear [MaterialTheme] on a **transparent**
 * background, cropped tight to the component.
 *
 * Deliberately no `fillMaxSize()` or centring. Preview discovery hands a device-less Wear preview
 * the watch screen as a *measuring bound* rather than a fixed frame, so a fill-width component
 * sizes to the watch and everything else wraps — and the renderer crops the PNG to it. Filling here
 * would defeat that crop and put every sticker back on a full round canvas.
 *
 * A component the kit publishes as a *display* cell — the round watch face, whole — wants
 * [FullScreenSticker] instead, and the edge button wants [EdgeButtonSticker]: what decides is the
 * shape of the kit cell the render is compared against, not how the component feels.
 */
@Composable
fun Sticker(content: @Composable () -> Unit) {
  CatalogMaterialTheme { Box(Modifier.padding(8.dp)) { content() } }
}

/**
 * The width the kit draws a **row-shaped control** at: 172dp, the content column of the 192dp
 * screen every sticker cell is measured against.
 *
 * Wear's `Button` applies no `fillMaxWidth` of its own — unlike `Card`, `Slider` and `Stepper`,
 * which do — so a button handed no width hugs its label. `Button/Filled` published at 120dp against
 * a 172dp kit cell, and that is not a 52dp edge to trim: design-parity rasterises the reference at
 * the CANDIDATE's width, so a narrow candidate rescales the whole comparison. See #138.
 *
 * `fillMaxWidth()` is **not** the fix here, and the tempting reading — that a wrap sandbox leaves
 * it nothing to fill — is wrong. The sandbox is bounded at 227dp and it resolves perfectly well: to
 * 211dp, which is simply a different wrong answer. 172 is the kit's number, so state it.
 *
 * Reach for this on a control the kit draws across its content column. A component that is
 * *supposed to* size to its content — an icon button, a compact button, a text specimen — must not
 * take it: pinning those would publish a component wider than the thing it is.
 */
val KitRowWidth = 172.dp

/** [KitRowWidth] as a modifier, for the common case of a control that only needs the width. */
fun Modifier.kitRowWidth(): Modifier = width(KitRowWidth)

/**
 * Frame for a **full-screen** component — one that positions itself against the round display
 * rather than wrapping its content. The Wear dark [MaterialTheme] fills the screen black and the
 * component lays itself out against it; [Sticker]'s wrap-and-crop would leave it nothing to hug.
 *
 * Deliberately no `ScreenScaffold` here. A scaffold is structure a *screen* supplies, and these are
 * components: the rails position against the bezel on their own, and a dialog or picker is the
 * whole screen already.
 *
 * Deliberately not the default: a component that wraps is published cropped and transparent, so a
 * designer can drop it on any canvas. Reach for this when the kit draws the component on a
 * `192×192` display cell — a dialog, a picker, an indicator rail, a swipe-to-reveal — because a
 * cropped render pairs with that cell only by being squashed into a landscape strip.
 */
@Composable
fun FullScreenSticker(content: @Composable BoxScope.() -> Unit) {
  CatalogMaterialTheme {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
  }
}

/**
 * Frame for a component that is **a screen with chrome** — one that needs the curved clock and the
 * scroll furniture around it to be the thing it is, rather than only the round display underneath.
 *
 * [FullScreenSticker] gives a component the display; this gives it the app. Two things want that: a
 * **list**, because a `TransformingLazyColumn` drawn with no `AppScaffold` over it is rows on a
 * black disc and the scroll indicator that tells a reader where they are never appears
 * (`ScreenScaffold` reads it from the scaffold above); and a component whose **kit cell draws the
 * clock**, which the media player's does — see `MediaControls.kt`.
 *
 * The clock is pinned to `10:10`, not the system clock, for the same reason every other capture
 * here is: a nightly render that differs from the last only in the time turns the delivery branch's
 * history into noise.
 */
@Composable
fun ScreenSticker(content: @Composable () -> Unit) {
  CatalogMaterialTheme {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) { content() }
  }
}

/**
 * Frame for a recording whose animation is driven by Wear's **app-level animation coordinator** —
 * today that is the placeholder, and only the placeholder.
 *
 * `Modifier.placeholder` and `Modifier.placeholderShimmer` do not drive themselves.
 * `PlaceholderState` reads its frame clock from the library's internal `AnimationCoordinator`, and
 * the one thing in Wear Compose that composes that coordinator's looper is **`AppScaffold`**. Draw
 * a placeholder without one — as [Sticker] does, and as every component capture should — and it is
 * a still: the shimmer never sweeps and the wipe-off never plays.
 *
 * Worth writing down, because it looks exactly like a renderer limitation and was recorded here as
 * one: `Motion.kt` carried "the placeholder does not animate under this renderer" (3 distinct
 * frames in 46) as a fact about Robolectric until the scaffold turned out to be what was missing. A
 * component sticker keeping its placeholder frozen is the right outcome — a baked capture of a
 * shimmer would differ on every publish — so this frame is for the recordings, not for them.
 *
 * No clock, unlike [ScreenSticker]. This is a component recording on a pinned canvas rather than a
 * screen, and a curved `TimeText` over it is chrome the recording is not about.
 */
@Composable
fun AnimatedSticker(content: @Composable () -> Unit) {
  CatalogMaterialTheme {
    AppScaffold(timeText = {}) {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
  }
}

/**
 * The width the kit lays an edge button out against: the 192dp base screen
 * (`CatalogFullScreenModes`).
 *
 * The button is not this wide — it is clipped to the display's bottom arc, so `Size=Default` draws
 * 114dp of shape inside a 192dp cell — but the arc it is clipped to is a fact about the *screen*,
 * so the canvas has to be the screen's width for the curve to come out the kit's shape.
 */
private val EdgeButtonCanvasWidth = 192.dp

/**
 * Frame for the **edge button** — the kit's `Edge-Button` cell, which is a *component* cell rather
 * than a display one.
 *
 * This used to be [EdgeButtonScreen]: a whole round watch face with a twelve-item list, captured
 * scrolled to the end so `ScreenScaffold` had revealed the button. It published a handsome picture
 * and an unusable comparison, because the cell it is diffed against (`36601:6587`) is 192×59 — the
 * button and nothing else. Squashing a 192×192 screen into that reported the list, the time text
 * and the scroll indicator as differences from a button (issue #31).
 *
 * So the frame is the kit's cell: the screen's width to clip the arc against, and nothing else. The
 * screen the button hugs is not lost — it moved to `Motion.kt`, where the scroll-driven reveal is a
 * recording rather than a still that has to stand in for one.
 *
 * NO PADDING, unlike [Sticker]. `EdgeButton` measures 3dp taller than its `EdgeButtonSize` on each
 * side (the library's own `VERTICAL_PADDING`), so the capture is `192 × size+6` against a kit cell
 * of `192 × size+3` — the kit keeps that 3dp under the shape and none above it. Those 3dp of extra
 * headroom are the whole of what still differs in the frame, and adding any of our own on top is
 * what would make it matter.
 */
@Composable
fun EdgeButtonSticker(content: @Composable () -> Unit) {
  CatalogMaterialTheme { Box(Modifier.width(EdgeButtonCanvasWidth)) { content() } }
}

/**
 * The screen an edge button hugs — an `AppScaffold`/`ScreenScaffold` pair over a list long enough
 * to scroll. Not a component frame: nothing in the inventory renders through it, because the kit
 * publishes no screen cell for the edge button. `Motion.kt` records the reveal through it.
 *
 * Two things have to be true for the reveal to be visible at all, and neither is optional:
 *
 * 1. **`ScreenScaffold`'s `edgeButton` slot places it.** Aligning it inside a content box instead
 *    puts it wherever that box happens to be — which is how the first render of this drew the
 *    button at the *top* of the watch.
 * 2. **The list must be scrolled.** The scaffold reveals the button *from the scroll state*: at the
 *    resting top it is collapsed, so a capture that never scrolls freezes that hidden first frame.
 */
@Composable
fun EdgeButtonScreen(
  state: TransformingLazyColumnState,
  edgeButton: @Composable BoxScope.() -> Unit,
) {
  CatalogMaterialTheme {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) {
      ScreenScaffold(scrollState = state, edgeButton = edgeButton) { padding ->
        TransformingLazyColumn(
          state = state,
          contentPadding = padding,
          modifier = Modifier.fillMaxSize(),
        ) {
          items(12) { index -> ListHeader { Text("Item ${index + 1}") } }
        }
      }
    }
  }
}

/**
 * The catalog's component multipreview.
 *
 * Wear is **dark-first** — the kit draws its components on a black watch face — so a sticker is a
 * single dark capture rather than the light/dark pair the phone catalog publishes. `showBackground
 * = false` keeps the capture transparent, so a designer can drop the sticker onto any canvas.
 */
@Preview(showBackground = false) annotation class CatalogModes

/**
 * The multipreview for a **full-screen** component, paired with [FullScreenSticker]: one capture at
 * every screen size the kit recognises.
 *
 * Names the watch device explicitly rather than relying on the measuring bound a device-less Wear
 * preview is retargeted to. The bound is enough for a component that wraps — it sizes and the
 * renderer crops — but a component that lays itself out against the *screen* reads the device
 * configuration, and without one a picker sizes its columns for a screen this is not and overflows
 * the frame.
 *
 * THE SIZES ARE THE KIT'S, NOT THE TOOLING'S. The kit enumerates them in its own `.WatchPuck` set
 * on the Meta Components page — `xSml 192 (Legacy)`, `Sml 204`, `Med 216`, `Lrg 225 (breakpoint)`,
 * `xLrg 240` — and those are the five drawn here. Wear tooling publishes device ids for only two of
 * them (`wearos_small_round` at 192 and `wearos_xl_round` at 240); the middle three are `spec:`
 * strings at the same 2.0 density, which the renderer handles fine. **`id:wearos_large_round` is
 * deliberately absent**: it is 227dp, which is not a size the kit draws — it sits between `Lrg 225`
 * and `xLrg 240`, and rendering there is what made every full-screen comparison carry a scale
 * difference underneath whatever else it found.
 *
 * 192 IS THE BASE, and that is the kit's call rather than a preference. The kit calls 192 "Legacy"
 * in the puck table, but it *draws* every one of its screen cells at 192×192 — so the base capture
 * has to be the 192 one for a base comparison to line up. The projector picks the narrowest by
 * default, which is that, so `scripts/design-map.sh` passes no `--base-breakpoint`.
 *
 * The other four fold under it as `<dp>dp` cells rather than becoming four more components: a size
 * is an argument to the same screen, not a different screen (AGENTS.md). Only the `Picker` set
 * publishes a second size in the kit — `Larger Screen (BP)=Yes` at 225 — so the rest are renders
 * with no kit counterpart, which the projector reports rather than pretends to have matched.
 */
@Preview(
  name = "192dp",
  device = "id:wearos_small_round",
  showBackground = true,
  backgroundColor = 0xFF000000,
)
@Preview(
  name = "204dp",
  device = "spec:width=204dp,height=204dp,dpi=320,isRound=true",
  showBackground = true,
  backgroundColor = 0xFF000000,
)
@Preview(
  name = "216dp",
  device = "spec:width=216dp,height=216dp,dpi=320,isRound=true",
  showBackground = true,
  backgroundColor = 0xFF000000,
)
@Preview(
  name = "225dp",
  device = "spec:width=225dp,height=225dp,dpi=320,isRound=true",
  showBackground = true,
  backgroundColor = 0xFF000000,
)
@Preview(
  name = "240dp",
  device = "id:wearos_xl_round",
  showBackground = true,
  backgroundColor = 0xFF000000,
)
annotation class CatalogFullScreenModes
