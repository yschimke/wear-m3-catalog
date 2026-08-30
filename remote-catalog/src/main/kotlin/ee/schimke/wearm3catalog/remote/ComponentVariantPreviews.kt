@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.animateRemoteFloat
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteHorizontalPageIndicator
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemotePageIndicatorState
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTextButton
import androidx.wear.compose.remote.material3.RemoteTimeText
import androidx.wear.compose.remote.material3.RemoteVerticalPageIndicator
import androidx.wear.compose.remote.material3.buttonSizeModifier
import androidx.wear.compose.remote.material3.rememberRemotePageIndicatorState
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideInt
import ee.schimke.composeai.preview.AnimatedPreview
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// What is LEFT of the variant matrix, after #116 folded most of it away.
//
// This file used to carry every public size, emphasis and slot of the `remote-material3` surface as
// its own top-level `@CatalogComponent` — the sheet had 51 components and zero `@OverrideVariant`
// cells, where `:catalog` folds the same axes into cells of 49. The compare page reads the two
// columns component by component through `parallel`, so the taxonomies now match: an axis that is
// an ARGUMENT to a function folded onto that function's component in CatalogPreviews.kt, under the
// cell name the Wear sibling gives it.
//
// Three things stay here, and each is one of the reasons a cell is the wrong shape:
//
//   * A style whose Wear counterpart is a SEPARATE FUNCTION. `Button/Tonal`, `IconButton/Filled`
//     and `IconButton/Outlined` fold by the call-site test — `remote-material3` publishes one
//     `RemoteButton` and one `RemoteIconButton`, taking emphasis as `colors` — but each pairs with
//     a separate function on the Wear column, and therefore a separate card. Folding them here
//     would leave those cards facing nothing.
//   * A LAYOUT of its own in the kit, that nothing else names. `TitleCard/Subtitle` is
//     `Title Card 3` — nine cells the kit spells as its own `Layout type` rather than as a slot
//     toggled off, and which no render claimed until #194. It spent a long time under
//     `noReference` arguing that our arrangement does not match the kit's, which is backwards:
//     that mismatch is the FINDING, and pointing at the node is what lets parity report it.
//
//     "That nothing else names" is the other half of the rule, and #194 got it wrong for
//     `AppCard/NoAppImage`: `Title Card + Icon` looks equally unclaimed until you notice
//     `AppCard`'s `icon` cells already draw it. Two renders on one node is not coverage — one of
//     them wins by manifest order and the other is scored against nothing while reading as mapped.
//     Check that a node is FREE before naming it, not just that it exists.
//   * A render the kit publishes NO CELL FOR. A cell resolves against the kit set and a cell that
//     resolves to nothing is compared against nothing, with no diagnostic anywhere; a component
//     carries `noReference` and says why instead. That is `AppCard/NoAppImage`,
//     `CircularProgressIndicator-Indeterminate`, and the two Remote-only capability rows in
//     CatalogPreviews.kt (`Button/CustomShape`, `Button/NamedLabel`).
//   * A component of its own. The page indicators are two functions on both columns.

@CatalogComponent(
  id = "Button/Tonal",
  group = "Buttons",
  parallel = "Button/Tonal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93104",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Tonal button on the theme's surface-container emphasis.",
)
@CatalogRemoteModes
@RemoteButtonKitCells
// THE THREE `Disabled=Yes` CELLS ARE DRAWN, and they are the disabled FILLED button to the byte.
// `RemoteButtonDefaults.buttonColors` takes the disabled pair as its own arguments, so a style
// written out by passing `containerColor` and `contentColor` leaves them at their defaults:
//
//   c58fbb6c…  FilledRemoteButton_VARIANT_disabled   == TonalRemoteButton_VARIANT_disabled
//   e12368d1…  …_VARIANT_icon_disabled               == …_VARIANT_icon_disabled
//   4676a294…  …_VARIANT_icon_large_disabled         == …_VARIANT_icon_large_disabled
//
// The Wear column withdraws the equivalent cells for that reason (`EdgeButtonKitCells`). This
// column draws them, because the sheet's job here is to cover what the kit publishes: a reader
// looking for the disabled tonal button finds it, and what they find is the truth about the alpha
// surface — the emphasis does not survive being disabled. Recorded rather than hidden.
@Composable
fun TonalRemoteButton() = RemoteSticker {
  // The kit's `Icon` / `Icon size` / `Disabled` cells come from `RemoteKitButton`, which is the
  // filled style's body with the emphasis lifted out — those axes are arguments to `RemoteButton`
  // rather than a choice of function, so drawing them a second time here would be a copy that can
  // drift.
  RemoteKitButton(
    // `surfaceContainer`, NOT `secondaryContainer`. `RemoteButtonDefaults` publishes no tonal
    // colours, so this style is written out here — and written out against the wrong token it drew
    // a blue button beside a kit cell (and a `wear-m3-catalog` parallel) that is neutral grey.
    // Wear Material 3 is where the token comes from: `ButtonDefaults.filledTonalButtonColors()` is
    // `surfaceContainer` / `onSurface` on this platform, unlike phone M3's secondary-container
    // tonal, and `Button/Tonal` in the sibling catalog is that function.
    RemoteButtonDefaults.buttonColors(
      containerColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
      contentColor = RemoteMaterialTheme.colorScheme.onSurface,
      secondaryContentColor = RemoteMaterialTheme.colorScheme.onSurfaceVariant,
      iconColor = RemoteMaterialTheme.colorScheme.primary,
    )
  )
}

@CatalogComponent(
  id = "Button/FilledVariant",
  group = "Buttons",
  parallel = "Button/FilledVariant",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39577:895",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "The kit's highlighted style — a filled button in the variant palette.",
)
@CatalogRemoteModes
@RemoteButtonKitCells
// The kit's fifth `Button` style, and the last one this column did not reach: ten of its fifty
// cells had no component at all here, against a Wear column that draws all five styles
// ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)).
//
// Written out, like `Button/Tonal` beside it, because `RemoteButtonDefaults` publishes exactly one
// `buttonColors()` and no emphasis variants. The tokens come from the Wear function this pairs
// with: `ButtonDefaults.filledVariantButtonColors()` is `primaryContainer` / `onPrimaryContainer`.
@Composable
fun FilledVariantRemoteButton() = RemoteSticker {
  RemoteKitButton(
    RemoteButtonDefaults.buttonColors(
      containerColor = RemoteMaterialTheme.colorScheme.primaryContainer,
      contentColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
      secondaryContentColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
      iconColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
    )
  )
}

@CatalogComponent(
  id = "Button/Child",
  group = "Buttons",
  parallel = "Button/Child",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93128",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Lowest emphasis; no container at all, for a button inside another surface.",
)
@CatalogRemoteModes
@RemoteButtonKitCells
// The kit's `Child (No background)` column — a label on whatever is behind it. Transparent
// container plus `onSurface` content is how Wear's own `childButtonColors()` is built, and with no
// container to draw the whole style IS those two colours.
@Composable
fun ChildRemoteButton() = RemoteSticker {
  RemoteKitButton(
    RemoteButtonDefaults.buttonColors(
      containerColor = RemoteColor(Color.Transparent),
      contentColor = RemoteMaterialTheme.colorScheme.onSurface,
      secondaryContentColor = RemoteMaterialTheme.colorScheme.onSurfaceVariant,
      iconColor = RemoteMaterialTheme.colorScheme.primary,
    )
  )
}

@CatalogComponent(
  id = "Button/ImageBackground",
  group = "Buttons",
  parallel = "Button/ImageBackground",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38425:101029",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38425:101028",
  caption = "A button over an image, with the scrim that keeps its label legible.",
)
@CatalogRemoteModes
@OverrideVariant(
  name = "secondary-label",
  booleans = ["secondary=true"],
  kitAxis = "Secondary label",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
// THIS ROW IS BROKEN ON PURPOSE, and that is the whole reason it is here.
//
// `Button-ImageBackground` publishes four cells and this column reproduced none of them, while the
// Wear column draws all four ([#157](https://github.com/yschimke/wear-m3-catalog/issues/157)). The
// library looks ready for it: `remote-material3-1.0.0-alpha10` ships `containerPainter`, its
// disabled companion, `buttonWithContainerPainterColors()` and a `RemoteButton` overload taking
// both painters. Call them and the button renders an OPAQUE BLACK PILL with no image in it —
// under the default scrim, a transparent one, `ContentScale.FillBounds`, and a bitmap 64x larger.
// `RemoteImage` draws that same bitmap in the card content slots, so it is the container painter
// specifically rather than the bitmap or the player.
//
// The first pass withdrew the component over that, on the grounds that a black pill scored against
// an image-backed node claims a comparison it is not making. That was the wrong call, and it is
// worth writing down why: withdrawing leaves the SET reading as unreproduced, which is
// indistinguishable from nobody having got to it — the sheet looks fine and the defect is
// invisible.
// Publishing it puts the break where a reader meets it and lets design-parity score it as the real
// divergence it is, which on a design-led scan is exactly what the number is for. It is the same
// bargain `StickerBakeCoverageTest.knownBlank` already strikes for the blank text button, and it
// makes the fix self-announcing: the day the painter draws, this row moves on its own.
//
// The image is the flat placeholder every other slot here draws — the kit publishes this cell's
// background as an empty `IMAGE` fill, which design-parity normalises to a flat colour at import.
//
// All four cells are drawn. `secondary-label-disabled` is the same picture as `disabled`, because
// a disabled `RemoteButton` draws its container and not its labels — the same loss the `Button`
// row's five `Alignment=Left, Disabled` cells record, and recorded here the same way.
@OverrideVariant(
  name = "secondary-label-disabled",
  booleans = ["secondary=true", "enabled=false"],
  kitProps = ["Secondary label=Yes", "Disabled=Yes"],
)
@Composable
fun ImageBackgroundRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  val container = RemoteButtonDefaults.containerPainter(CatalogRemoteImage.bitmap())
  RemoteButton(
    onClick = onClick,
    modifier = RemoteModifier.buttonSizeModifier().width(KitRowWidth),
    enabled = previewOverrideBoolean("enabled", true).rb,
    containerPainter = container,
    // The library's own dimming of the same painter, not a second image: the kit's `Disabled=Yes`
    // cell is this picture at a lower opacity, and `disabledContainerPainter` is the pair published
    // for exactly that.
    disabledContainerPainter = RemoteButtonDefaults.disabledContainerPainter(container),
    colors = RemoteButtonDefaults.buttonWithContainerPainterColors(),
    secondaryLabel =
      if (previewOverrideBoolean("secondary", false)) ({ RemoteText(KitCopy.SECONDARY_LABEL.rs) })
      else null,
    label = { RemoteText(label) },
  )
}

@CatalogComponent(
  id = "IconButton/Filled",
  group = "Buttons",
  parallel = "IconButton/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102976",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "RemoteIconButton with a filled primary container.",
)
@CatalogRemoteModes
@RemoteContainedIconButtonKitCells
@Composable
fun FilledRemoteIconButton() = RemoteSticker {
  RemoteKitIconButton(
    RemoteIconButtonDefaults.iconButtonColors(
      containerColor = RemoteMaterialTheme.colorScheme.primary,
      contentColor = RemoteMaterialTheme.colorScheme.onPrimary,
    )
  )
}

@CatalogComponent(
  id = "IconButton/FilledVariant",
  group = "Buttons",
  parallel = "IconButton/FilledVariant",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41409:52153",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "RemoteIconButton in the kit's highlighted palette.",
)
@CatalogRemoteModes
@RemoteContainedIconButtonKitCells
@Composable
fun FilledVariantRemoteIconButton() = RemoteSticker {
  RemoteKitIconButton(
    RemoteIconButtonDefaults.iconButtonColors(
      containerColor = RemoteMaterialTheme.colorScheme.primaryContainer,
      contentColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
    )
  )
}

@CatalogComponent(
  id = "IconButton/Tonal",
  group = "Buttons",
  parallel = "IconButton/Tonal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102989",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "RemoteIconButton on the theme's surface-container emphasis.",
)
@CatalogRemoteModes
@RemoteContainedIconButtonKitCells
@Composable
fun TonalRemoteIconButton() = RemoteSticker {
  // `surfaceContainer` / `onSurface`, the tokens Wear's `filledTonalIconButtonColors()` resolves
  // to on this platform — the same call `Button/Tonal` above makes for the same reason.
  RemoteKitIconButton(
    RemoteIconButtonDefaults.iconButtonColors(
      containerColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
      contentColor = RemoteMaterialTheme.colorScheme.onSurface,
    )
  )
}

@CatalogComponent(
  id = "IconButton/Outlined",
  group = "Buttons",
  parallel = "IconButton/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103002",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "RemoteIconButton with an explicit outline treatment.",
)
@CatalogRemoteModes
@RemoteContainedIconButtonKitCells
// The cell the shared annotation cannot carry — see its note. This style draws its own border at
// the container's size, so extra-small and small stay two pictures even with the glyph gone.
@OverrideVariant(
  name = "extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=extra-small"],
  kitProps = ["Size=Extra-Small", "Disabled=Yes"],
  secondary = true,
)
@Composable
fun OutlinedRemoteIconButton() = RemoteSticker {
  RemoteKitIconButton(
    colors =
      RemoteIconButtonDefaults.iconButtonColors(containerColor = RemoteColor(Color.Transparent)),
    border = 2.rdp,
    borderColor = RemoteMaterialTheme.colorScheme.outline,
  )
}

// COLLAPSED INTO `TitleCard`. This was `TitleCard/Subtitle`, a component of its own for the kit's
// `Title Card 3` layout — and the Wear sibling has always carried it as `TitleCard`'s
// `title-and-subtitle` cell. It now ships under that same name, with `39569:49145`'s four property
// assignments transcribed onto the variant so the nine cells it unlocked stay claimed.
//
// `RemoteTitleCard` still has no argument that puts the timestamp beside the title, so that cell's
// render still will not match its node. Unchanged, and still the point: the divergence is what the
// reference exists to surface.

// COLLAPSED INTO `AppCard`. This was `AppCard/NoAppImage`, a component of its own for an empty
// leading slot — but the slot it varies is `appImage`, the same argument `AppCard`'s `image` and
// `icon` cells turn, so it is a third value of that knob. It ships as that component's
// `no-app-image` cell, and the reason it names no node moved with it.

// COLLAPSED INTO `CircularProgressIndicator`. This was `CircularProgressIndicator-Indeterminate`, a
// second top-level component for what is one argument list on one function — and the Wear sibling
// has always carried it as that component's `indeterminate` cell. Two sheets that spell one
// component two ways cannot be read side by side, which is the whole job of this repo, so the
// still moved to `@OverrideVariant(name = "indeterminate", …)` on `CircularProgressRemote` and
// the recording below stayed put as that component's `motionPreview`.
//
// The reason it was never a kit reference still holds and is worth keeping written down: the kit's
// `Progress` axis is four determinate values — Zero, In progress, Complete, Overflow — and a still
// frame is all a kit cell can be, so nothing in `Progress-Indicator` is the indeterminate sweep.
// Any cell named for it would be a picture of a fixed arc, and this arc is never at rest. What
// stands in for a reference is the motion capture.
@CatalogRemoteDisplay
@AnimatedPreview(
  durationMs = 2000,
  frameIntervalMs = 50,
  showCurves = false,
  caption = "The indeterminate arc rotates and changes sweep continuously on the remote clock.",
)
@Composable
fun IndeterminateCircularProgressMotionRemote() = RemoteSticker {
  RemoteCircularProgressIndicator(modifier = RemoteModifier.fillMaxSize())
}

/**
 * **The kit's `Number` axis along the bottom** — nine cells against the one this drew.
 *
 * `Number` is two knobs, not one, past five pages: the kit draws `6` and `7+` three times each, for
 * a window sitting at the start, in the middle and at the end of the run, and which of those you
 * see is the selected page rather than the count. `7+` is eight pages here, far enough past the
 * maximum to show the indicator collapsing. Cell names and seeds are the Wear sibling's, so the
 * compare page pairs them ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)).
 */
@OverrideVariant(name = "two-pages", ints = ["pages=2"], kitAxis = "Number", kitValue = "2")
@OverrideVariant(name = "three-pages", ints = ["pages=3"], kitAxis = "Number", kitValue = "3")
@OverrideVariant(name = "five-pages", ints = ["pages=5"], kitAxis = "Number", kitValue = "5")
@OverrideVariant(
  name = "six-pages",
  ints = ["pages=6"],
  kitAxis = "Number",
  kitValue = "6 - Start",
)
@OverrideVariant(
  name = "six-pages-middle",
  ints = ["pages=6", "initialPage=3"],
  kitProps = ["Number=6  - MiddleEnd", "Position=Horizontal-Bottom"],
  secondary = true,
)
@OverrideVariant(
  name = "six-pages-end",
  ints = ["pages=6", "initialPage=5"],
  kitProps = ["Number=6 - End", "Position=Horizontal-Bottom"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages",
  ints = ["pages=8"],
  kitProps = ["Number=7+ - Start", "Position=Horizontal-Bottom"],
)
@OverrideVariant(
  name = "many-pages-middle",
  ints = ["pages=8", "initialPage=4"],
  kitProps = ["Number=7+  - MiddleEnd", "Position=Horizontal-Bottom"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages-end",
  ints = ["pages=8", "initialPage=7"],
  kitProps = ["Number=7+ - End", "Position=Horizontal-Bottom"],
  secondary = true,
)
annotation class RemoteHorizontalPageKitCells

/**
 * The same nine cells at `Position=Vertical-Right`, for the vertical indicator.
 *
 * The kit's third column, `Vertical-Left`, is not drawn: which bezel the rail sits against is where
 * the caller puts it, not a parameter of `RemoteVerticalPageIndicator` — the same absence the Wear
 * column states for the same ten cells.
 */
@OverrideVariant(name = "two-pages", ints = ["pages=2"], kitAxis = "Number", kitValue = "2")
@OverrideVariant(name = "three-pages", ints = ["pages=3"], kitAxis = "Number", kitValue = "3")
@OverrideVariant(name = "five-pages", ints = ["pages=5"], kitAxis = "Number", kitValue = "5")
@OverrideVariant(
  name = "six-pages",
  ints = ["pages=6"],
  kitAxis = "Number",
  kitValue = "6 - Start",
)
@OverrideVariant(
  name = "six-pages-middle",
  ints = ["pages=6", "initialPage=3"],
  kitProps = ["Number=6  - MiddleEnd", "Position=Vertical-Right"],
  secondary = true,
)
@OverrideVariant(
  name = "six-pages-end",
  ints = ["pages=6", "initialPage=5"],
  kitProps = ["Number=6 - End", "Position=Vertical-Right"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages",
  ints = ["pages=8"],
  kitProps = ["Number=7+ - Start", "Position=Vertical-Right"],
)
@OverrideVariant(
  name = "many-pages-middle",
  ints = ["pages=8", "initialPage=4"],
  kitProps = ["Number=7+  - MiddleEnd", "Position=Vertical-Right"],
  secondary = true,
)
@OverrideVariant(
  name = "many-pages-end",
  ints = ["pages=8", "initialPage=7"],
  kitProps = ["Number=7+ - End", "Position=Vertical-Right"],
  secondary = true,
)
annotation class RemoteVerticalPageKitCells

/** The two knobs the kit's `Number` axis needs, coerced so a live knob cannot outrun the count. */
@Composable
private fun rememberKitPageIndicatorState(): RemotePageIndicatorState {
  val pages = previewOverrideInt("pages", 4)
  val selected = previewOverrideInt("initialPage", 0).coerceIn(0, (pages - 1).coerceAtLeast(0))
  return rememberRemotePageIndicatorState(pageCount = pages, selectedPage = selected.ri)
}

@CatalogComponent(
  id = "PageIndicator/Horizontal",
  group = "Communication",
  parallel = "PageIndicator/Horizontal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:138301",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Four-page horizontal indicator curved along the bottom edge, on the first page.",
)
@CatalogRemoteDisplay
@RemoteHorizontalPageKitCells
@Composable
fun HorizontalPageIndicatorRemote() = RemoteSticker {
  // FOUR pages on the FIRST, because that is the kit cell this row's `reference` names and what
  // `wear-m3-catalog`'s `PageIndicator/Horizontal` draws (`pages = 4`, `initialPage = 0`). Five
  // pages with the third selected put a different picture under the same node — and a middle
  // selection is the one arrangement in which the selected dot is hardest to pick out.
  // MEASURED, not assumed: the SIZE this is given changes nothing. The comment here used to say
  // the indicator curves against the bezel and that an inset box would move the curve inward — it
  // does not curve at all. Handed the whole display it draws a straight, CONTENT-sized rail of
  // 36×8dp and centres it, which is 0.2% of a cell the kit publishes as the round face whole with
  // the rail struck against the edge
  // ([#149](https://github.com/yschimke/wear-m3-catalog/issues/149)).
  // `RemoteHorizontalPageIndicator` has no curvature and no edge affinity; the Wear sibling's
  // `HorizontalPageIndicator` has both.
  //
  // So the ALIGNMENT is what this sticker can supply, and it does: the rail sits bottom-centre of
  // the display, where the kit's sits, which is as close as the API goes. The remaining gap is the
  // curve, and it stays VISIBLE rather than hidden — a straight rail against a round bezel is the
  // divergence, where a rail floating in the middle said nothing about it either way.
  RemoteBox(
    modifier = RemoteModifier.fillMaxSize(),
    contentAlignment = RemoteAlignment.BottomCenter,
    content = { RemoteHorizontalPageIndicator(state = rememberKitPageIndicatorState()) },
  )
}

@CatalogComponent(
  id = "PageIndicator/Vertical",
  group = "Communication",
  parallel = "PageIndicator/Vertical",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38966:402",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Four-page vertical indicator against the right bezel, on the first page.",
)
@CatalogRemoteDisplay
@RemoteVerticalPageKitCells
@Composable
fun VerticalPageIndicatorRemote() = RemoteSticker {
  // Same four-on-the-first as the horizontal one, for the same reason: this row names a kit node,
  // and `wear-m3-catalog`'s `PageIndicator/Vertical` draws the same cell. Eight pages with the
  // fifth selected exercised the scrolling-dot window, which is a real behaviour — but it is a
  // behaviour neither the kit cell nor the parallel draws, so it was reported as a divergence on
  // every render rather than shown as itself.
  // Against the right bezel, for the reason spelled out on the horizontal one: the component draws
  // a straight content-sized rail wherever it is put and at whatever size, so the sticker supplies
  // the position the kit cell is about and leaves the missing curvature on show.
  RemoteBox(
    modifier = RemoteModifier.fillMaxSize(),
    contentAlignment = RemoteAlignment.CenterEnd,
    content = { RemoteVerticalPageIndicator(state = rememberKitPageIndicatorState()) },
  )
}

@CatalogComponent(
  id = "PageIndicator/Interactive",
  group = "Communication",
  parallel = "PageIndicator/Horizontal",
  noReference =
    "The four-page horizontal cell this would map to (`38684:138301`) is already named by " +
      "`PageIndicator/Horizontal`, and what this row adds is not in the picture: a Next button " +
      "over the indicator and a worm that animates between pages inside the document. The kit's " +
      "`Page-Indicator` set varies `Number` and `Position` only — it has no control affordance " +
      "and no in-between state — so the cell would score the button as a divergence and say " +
      "nothing about the animation, which is the only thing this row is for.",
  caption =
    "Interactive page indicator: tapping Next animates the worm between adjacent pages inside " +
      "the RemoteDocument.",
)
@CatalogRemoteDisplay
@Composable
fun InteractivePageIndicatorRemote() = RemoteSticker {
  val selected = rememberMutableRemoteInt(0)
  val animatedPage = animateRemoteFloat(selected.toRemoteFloat(), duration = 0.6f)
  val state =
    object : RemotePageIndicatorState {
      override val pageCount: Int = 4
      override val selectedPage: RemoteInt = 0.ri
      override val pageOffset: RemoteFloat = animatedPage
    }
  RemoteBox(
    modifier = RemoteModifier.fillMaxSize(),
    contentAlignment = RemoteAlignment.Center,
  ) {
    RemoteHorizontalPageIndicator(state = state)
    RemoteTextButton(
      onClick = valueChange(selected, (1.ri - selected).createReference()),
      content = { RemoteText("Next".rs) },
    )
  }
}

@CatalogRemoteDisplay
@AnimatedPreview(
  durationMs = 1000,
  frameIntervalMs = 33,
  showCurves = false,
  caption = "The selected-page worm expands, travels, and contracts between adjacent pages.",
)
@Composable
fun PageIndicatorMotionRemote() = RemoteSticker {
  val state =
    object : RemotePageIndicatorState {
      override val pageCount: Int = 4
      override val selectedPage: RemoteInt = 0.ri
      override val pageOffset: RemoteFloat =
        animateRemoteFloat(1f.rf, duration = 0.8f, initialValue = 0f)
    }
  RemoteHorizontalPageIndicator(state = state, modifier = RemoteModifier.fillMaxSize())
}

@Composable
@RemoteComposable
fun TimeTextRemoteSample() {
  RemoteTimeText(modifier = RemoteModifier.size(180.rdp), time = "10:10".rs)
}

@Composable
@RemoteComposable
fun TimeTextWithContextRemoteSample() {
  RemoteTimeText(
    modifier = RemoteModifier.size(180.rdp),
    time = "10:10".rs,
    leadingText = "Run".rs,
    trailingText = "72 bpm".rs,
  )
}
