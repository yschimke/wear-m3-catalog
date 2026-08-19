package ee.schimke.wearm3catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
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
 * Full-screen components (scaffolds, lists, the edge button) will need their own frame that keeps
 * the round device shape; they are not in the inventory yet.
 */
@Composable
fun Sticker(content: @Composable () -> Unit) {
  CatalogMaterialTheme { Box(Modifier.padding(8.dp)) { content() } }
}

/**
 * Frame for a **full-screen** component — one that positions itself against the round display
 * rather than wrapping its content. The Wear dark [MaterialTheme] fills the screen black and the
 * component lays itself out against it; [Sticker]'s wrap-and-crop would leave it nothing to hug.
 *
 * Deliberately no `ScreenScaffold` here. A scaffold is structure a *screen* supplies, and these are
 * components: the rails position against the bezel on their own, and a dialog or picker is the
 * whole screen already. The edge button is the one that genuinely needs a scaffold, and it has its
 * own frame ([EdgeButtonScreen]) that supplies one.
 *
 * Deliberately not the default: a component that wraps is published cropped and transparent, so a
 * designer can drop it on any canvas. Reach for this only when the round frame is part of what the
 * component *is* — the edge button is the live example.
 */
@Composable
fun FullScreenSticker(content: @Composable BoxScope.() -> Unit) {
  CatalogMaterialTheme {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
  }
}

/**
 * Frame for the **edge button**, which is not merely full-screen but bottom-anchored and
 * scroll-revealed.
 *
 * Two things have to be true for this to publish the component rather than a frame of it, and
 * neither is optional:
 *
 * 1. **`ScreenScaffold`'s `edgeButton` slot places it.** Aligning it inside a content box instead
 *    puts it wherever that box happens to be — which is how the first render of this sticker drew
 *    the button at the *top* of the watch.
 * 2. **The list must be scrolled to the end.** The scaffold reveals the button *from the scroll
 *    state*: at the resting top it is collapsed, and a static capture would freeze that hidden
 *    first frame. So the list overflows the screen by a few times and the sticker carries
 *    `@ScrollingPreview(END)`, which scrolls and lets the reveal settle before capturing.
 */
@Composable
fun EdgeButtonScreen(edgeButton: @Composable BoxScope.() -> Unit) {
  CatalogMaterialTheme {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) {
      val state = rememberTransformingLazyColumnState()
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
 * The multipreview for a **full-screen** component, paired with [FullScreenSticker].
 *
 * Names the watch device explicitly rather than relying on the measuring bound a device-less Wear
 * preview is retargeted to. The bound is enough for a component that wraps — it sizes and the
 * renderer crops — but a component that lays itself out against the *screen* reads the device
 * configuration, and without one a picker sizes its columns for a screen this is not and overflows
 * the frame.
 */
@Preview(device = "id:wearos_large_round", showBackground = true, backgroundColor = 0xFF000000)
annotation class CatalogFullScreenModes
