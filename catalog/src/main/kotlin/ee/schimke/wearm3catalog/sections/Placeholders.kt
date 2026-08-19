@file:CatalogGroup(name = "Placeholders", section = "Communication")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker

// The kit's three `*-Placeholder` sets: what a button, an icon button and a card look like while
// their content is still loading.
//
// These are not separate components in Compose — a placeholder is `Modifier.placeholder` over the
// real component — but they ARE separate kit sets, and membership is the kit's call. So each is a
// sticker that calls the real `Button` / `IconButton` / `Card` and dresses it in the placeholder
// modifier, which is exactly what an app does.
//
// The shimmer is deliberately left OFF the baked capture: it is an animation, and a capture of a
// frame of it would differ on every publish. `Modifier.placeholderShimmer` is applied in a live
// session, where it can actually shimmer — and `Motion.kt` records it as a GIF, which is where a
// reader should go to see what these stickers are the frozen first frame of.
//
// The motion capture is NOT on these functions, deliberately. An `@AnimatedPreview` here would ride
// every `@OverrideVariant` cell too, and the animated path does not apply the cells' knobs: all
// three placeholder styles came out byte-identical, three copies of the base GIF published under
// three different names.

@CatalogComponent(
  id = "Placeholder/Button",
  reference = "figma:B24oss2tTeXAFykyeyusz0/71571:44772",
  caption = "A button whose label has not arrived yet.",
)
@CatalogModes
@OverrideVariant(name = "tonal", strings = ["style=tonal"], kitAxis = "Style", kitValue = "Tonal")
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@Composable
fun ButtonPlaceholder() = Sticker {
  val state = rememberPlaceholderState(isVisible = true)
  val label: @Composable RowScope.() -> Unit = { Text("", modifier = Modifier.width(80.dp)) }
  val modifier = Modifier.width(150.dp).placeholder(state).placeholderShimmer(state)
  when (previewOverrideString("style", "filled")) {
    "tonal" -> FilledTonalButton(onClick = {}, modifier = modifier, label = label)
    "outlined" -> OutlinedButton(onClick = {}, modifier = modifier, label = label)
    else -> Button(onClick = {}, modifier = modifier, label = label)
  }
}

@CatalogComponent(
  id = "Placeholder/IconButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/71571:44843",
  caption = "An icon button whose icon has not arrived yet.",
)
@CatalogModes
@Composable
fun IconButtonPlaceholder() = Sticker {
  val state = rememberPlaceholderState(isVisible = true)
  IconButton(
    onClick = {},
    modifier = Modifier.placeholder(state, ButtonDefaults.shape).placeholderShimmer(state),
  ) {}
}

@CatalogComponent(
  id = "Placeholder/Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/71571:45109",
  caption = "A card whose content has not arrived yet.",
)
@CatalogModes
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@Composable
fun CardPlaceholder() = Sticker {
  val state = rememberPlaceholderState(isVisible = true)
  val modifier = Modifier.width(180.dp).placeholder(state).placeholderShimmer(state)
  if (previewOverrideString("style", "tonal") == "outlined") {
    OutlinedCard(onClick = {}, modifier = modifier) { Text("") }
  } else {
    Card(onClick = {}, modifier = modifier) { Text("") }
  }
}
