@file:CatalogGroup(name = "Placeholders", section = "Communication")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker

// The kit's three `*-Placeholder` sets: what a button, an icon button and a card look like while
// their content is still loading.
//
// These are not separate components in Compose — a placeholder is `Modifier.placeholder` over
// whatever has not arrived yet — but they ARE separate kit sets, and membership is the kit's call.
// So each is a sticker that calls the real `Button` / `IconButton` / `Card` and dresses it in the
// placeholder modifier, which is exactly what an app does.
//
// A PLACEHOLDER IS DRAWN OVER THE CONTENT, NOT OVER THE COMPONENT
//
// The kit draws the container in its real style and fills it with the *shape of the content that is
// coming*: a round icon chip, a wide primary line, a shorter secondary line
// ([#55](https://github.com/yschimke/wear-m3-catalog/issues/55)). These stickers used to put
// `Modifier.placeholder` on the component itself, which paints over the whole container — a
// featureless pill, with no icon, no lines and nothing left of the style underneath.
//
// So `Modifier.placeholder` goes on the boxes standing in for the icon and the labels, and the
// component keeps its own colours. That is also what an app writes: the button is already on screen
// and is already the button it will be, and it is the label and icon that are still in flight.
//
// The chips are sized from the kit's own cells — `172×52` for the button, `172×68` for the card —
// because a placeholder has no text to size itself from, so those numbers ARE the design. Wear
// Compose's own padding puts them where the kit puts them.
//
// The shimmer is deliberately left STILL on these stickers, and it costs nothing to ask for: a
// placeholder animates only under an `AppScaffold`, because that is what composes the frame clock
// `PlaceholderState` reads (see `AnimatedSticker` in `CatalogTheme.kt`). `Sticker` has no scaffold,
// so `Modifier.placeholderShimmer` here is a declaration of what the component does rather than a
// sweep — which is what a baked capture wants, since a frame of a shimmer would differ on every
// publish. The kit's own `Placeholder-gradient` overlay is that same sweep, drawn frozen.
//
// The moving version is not lost: `Motion.kt` records all three resolving into real content, and
// each component claims its recording with `motionPreview` above.
//
// STYLE CELLS, WHICH THE KIT HAS AND THIS FILE USED TO OWE IT
//
// The kit's placeholder sets carry `Style = Filled | Variant (Highlighted) | Tonal | Outline`, and
// while the placeholder covered the container all four rendered identically — authored as cells
// they published one picture under four names, caught by `CatalogRenderTest`. With the placeholder
// on the content the container is visible again and the styles differ, so each is a cell that names
// the kit's own value for the axis.
//
// They are cells rather than four components, unlike `Button` itself: what a reader is choosing
// here is not which function to call — it is what a component looks like mid-load, and the
// emphasis is already published, with its own card, in `Buttons.kt` and `Cards.kt`.
//
// The `@AnimatedPreview` is NOT on these functions, deliberately — only the `motionPreview` claim
// is. An `@AnimatedPreview` here would ride every `@OverrideVariant` cell too, and the animated
// path does not apply the cells' knobs: all four placeholder styles would come out byte-identical,
// four copies of the base GIF published under four different names.

/** The icon that has not arrived: the kit draws it as a plain circle the icon's own size. */
@Composable
private fun PlaceholderIcon(state: PlaceholderState, size: Dp) {
  Box(Modifier.size(size).placeholder(state, CircleShape))
}

/**
 * One line of text that has not arrived, as a pill of [width] × [height] centred in the [slot] the
 * text itself would occupy.
 *
 * Two sizes, not one: the kit's line is shorter than the line box around it — 12dp of pill in an
 * 18dp primary line, 10dp in a 16dp secondary — and a pill that filled the slot would read as a
 * solid block rather than as a line of text.
 */
@Composable
private fun PlaceholderLine(state: PlaceholderState, width: Dp, height: Dp, slot: Dp) {
  Box(Modifier.height(slot), contentAlignment = Alignment.Center) {
    Box(Modifier.width(width).height(height).placeholder(state, CircleShape))
  }
}

@CatalogComponent(
  id = "Placeholder/Button",
  reference = "figma:B24oss2tTeXAFykyeyusz0/71571:44771",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/71571:44772",
  caption = "A button whose icon and labels have not arrived yet.",
  motionPreview = "PlaceholderButtonMotion",
)
@CatalogModes
@OverrideVariant(
  name = "variant",
  strings = ["style=variant"],
  kitAxis = "Style",
  kitValue = "Variant (Highlighted)",
)
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
  val modifier = Modifier.width(172.dp).placeholderShimmer(state)
  val icon: @Composable BoxScope.() -> Unit = { PlaceholderIcon(state, ButtonDefaults.IconSize) }
  val label: @Composable RowScope.() -> Unit = { PlaceholderLine(state, 94.dp, 12.dp, 18.dp) }
  val secondaryLabel: @Composable RowScope.() -> Unit = {
    PlaceholderLine(state, 60.dp, 10.dp, 16.dp)
  }
  when (
    previewOverrideChoice("style", "filled", listOf("filled", "variant", "tonal", "outlined"))
  ) {
    "variant" ->
      Button(
        onClick = {},
        modifier = modifier,
        colors = ButtonDefaults.filledVariantButtonColors(),
        secondaryLabel = secondaryLabel,
        icon = icon,
        label = label,
      )
    "tonal" ->
      FilledTonalButton(
        onClick = {},
        modifier = modifier,
        secondaryLabel = secondaryLabel,
        icon = icon,
        label = label,
      )
    "outlined" ->
      OutlinedButton(
        onClick = {},
        modifier = modifier,
        secondaryLabel = secondaryLabel,
        icon = icon,
        label = label,
      )
    else ->
      Button(
        onClick = {},
        modifier = modifier,
        secondaryLabel = secondaryLabel,
        icon = icon,
        label = label,
      )
  }
}

@CatalogComponent(
  id = "Placeholder/IconButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/71571:44842",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/71571:44843",
  caption = "An icon button whose icon has not arrived yet.",
  motionPreview = "PlaceholderIconButtonMotion",
)
@CatalogModes
@OverrideVariant(
  name = "variant",
  strings = ["style=variant"],
  kitAxis = "Style",
  kitValue = "Variant (Highlighted)",
)
@OverrideVariant(name = "tonal", strings = ["style=tonal"], kitAxis = "Style", kitValue = "Tonal")
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
@Composable
fun IconButtonPlaceholder() = Sticker {
  val state = rememberPlaceholderState(isVisible = true)
  val modifier = Modifier.placeholderShimmer(state, CircleShape)
  val icon: @Composable BoxScope.() -> Unit = {
    PlaceholderIcon(state, IconButtonDefaults.DefaultIconSize)
  }
  when (
    previewOverrideChoice("style", "filled", listOf("filled", "variant", "tonal", "outlined"))
  ) {
    "variant" ->
      FilledIconButton(
        onClick = {},
        modifier = modifier,
        colors = IconButtonDefaults.filledVariantIconButtonColors(),
        content = icon,
      )
    "tonal" -> FilledTonalIconButton(onClick = {}, modifier = modifier, content = icon)
    "outlined" -> OutlinedIconButton(onClick = {}, modifier = modifier, content = icon)
    else -> FilledIconButton(onClick = {}, modifier = modifier, content = icon)
  }
}

@CatalogComponent(
  id = "Placeholder/Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/71571:45108",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/71571:45109",
  caption = "A card whose icon and text have not arrived yet.",
  motionPreview = "PlaceholderCardMotion",
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
  val modifier = Modifier.width(172.dp).placeholderShimmer(state)
  // The kit's card cell is the same row an `AppCard` draws — an icon beside a title and two lines
  // of body — with every slot still empty, so the content is laid out here rather than borrowed
  // from `AppCard`, which requires an app name and a time it does not have yet.
  val content: @Composable () -> Unit = {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      PlaceholderIcon(state, 24.dp)
      Column {
        PlaceholderLine(state, 93.dp, 12.dp, 18.dp)
        PlaceholderLine(state, 100.dp, 10.dp, 16.dp)
        PlaceholderLine(state, 60.dp, 10.dp, 16.dp)
      }
    }
  }
  if (previewOverrideChoice("style", "tonal", listOf("tonal", "outlined")) == "outlined") {
    OutlinedCard(onClick = {}, modifier = modifier) { content() }
  } else {
    Card(onClick = {}, modifier = modifier) { content() }
  }
}
