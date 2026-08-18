package ee.schimke.wearm3catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold

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
  MaterialTheme { Box(Modifier.padding(8.dp)) { content() } }
}

/**
 * Frame for a **full-screen** component — one that positions itself against the round display
 * rather than wrapping its content. The Wear dark [MaterialTheme] fills the screen black and
 * [ScreenScaffold] supplies the structure such a component lays itself out against; [Sticker]'s
 * wrap-and-crop would leave it nothing to hug.
 *
 * Deliberately not the default: a component that wraps is published cropped and transparent, so a
 * designer can drop it on any canvas. Reach for this only when the round frame is part of what the
 * component *is* — the edge button is the live example.
 */
@Composable
fun FullScreenSticker(content: @Composable BoxScope.() -> Unit) {
  MaterialTheme {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
      ScreenScaffold { _ -> content() }
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
