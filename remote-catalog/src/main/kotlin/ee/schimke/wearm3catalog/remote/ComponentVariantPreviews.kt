@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.animateRemoteFloat
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.tween
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteAppCard
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteHorizontalPageIndicator
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteIconButton
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemotePageIndicatorState
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTextButton
import androidx.wear.compose.remote.material3.RemoteTimeText
import androidx.wear.compose.remote.material3.RemoteTitleCard
import androidx.wear.compose.remote.material3.RemoteVerticalPageIndicator
import androidx.wear.compose.remote.material3.rememberRemotePageIndicatorState
import ee.schimke.composeai.preview.AnimatedPreview
import ee.schimke.composeai.preview.CatalogComponent

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
//   * A style whose Wear counterpart is a SEPARATE FUNCTION. `Button/Tonal`, `Button/Icon-Filled`
//     and `Button/Icon-Outlined` fold by the call-site test — `remote-material3` publishes one
//     `RemoteButton` and one `RemoteIconButton`, taking emphasis as `colors` — but they pair with
//     `Button/Tonal`, `IconButton/Filled` and `IconButton/Outlined`, which are five separate
//     functions on the Wear column and therefore five separate cards. Folding them here would
//     leave those cards facing nothing.
//   * A render the kit publishes NO CELL FOR. A cell resolves against the kit set and a cell that
//     resolves to nothing is compared against nothing, with no diagnostic anywhere; a component
//     carries `noReference` and says why instead. That is `TitleCard/Subtitle`,
//     `AppCard/NoAppImage`, `Progress/Circular-Indeterminate`, and the two Remote-only capability
//     rows in CatalogPreviews.kt (`Button/CustomShape`, `Button/NamedLabel`).
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
@Composable
fun TonalRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteButton(
    onClick = onClick,
    // `surfaceContainer`, NOT `secondaryContainer`. `RemoteButtonDefaults` publishes no tonal
    // colours, so this style is written out here — and written out against the wrong token it drew
    // a blue button beside a kit cell (and a `wear-m3-catalog` parallel) that is neutral grey.
    // Wear Material 3 is where the token comes from: `ButtonDefaults.filledTonalButtonColors()` is
    // `surfaceContainer` / `onSurface` on this platform, unlike phone M3's secondary-container
    // tonal, and `Button/Tonal` in the sibling catalog is that function.
    colors =
      RemoteButtonDefaults.buttonColors(
        containerColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
        contentColor = RemoteMaterialTheme.colorScheme.onSurface,
        secondaryContentColor = RemoteMaterialTheme.colorScheme.onSurfaceVariant,
        iconColor = RemoteMaterialTheme.colorScheme.primary,
      ),
    content = { RemoteText(label) },
  )
}

@CatalogComponent(
  id = "Button/Icon-Filled",
  group = "Buttons",
  parallel = "IconButton/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:102976",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "RemoteIconButton with a filled primary container.",
)
@CatalogRemoteModes
@Composable
fun FilledRemoteIconButton() = RemoteSticker {
  val (on, onClick) = toggledRemote()
  RemoteIconButton(
    onClick = onClick,
    colors =
      RemoteIconButtonDefaults.iconButtonColors(
        containerColor =
          tween(
            RemoteMaterialTheme.colorScheme.primary,
            RemoteMaterialTheme.colorScheme.tertiaryContainer,
            on,
          ),
        contentColor =
          tween(
            RemoteMaterialTheme.colorScheme.onPrimary,
            RemoteMaterialTheme.colorScheme.onTertiaryContainer,
            on,
          ),
      ),
    content = { RemoteIcon(addIcon, "Add".rs) },
  )
}

@CatalogComponent(
  id = "Button/Icon-Outlined",
  group = "Buttons",
  parallel = "IconButton/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103002",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "RemoteIconButton with an explicit outline treatment.",
)
@CatalogRemoteModes
@Composable
fun OutlinedRemoteIconButton() = RemoteSticker {
  val (on, onClick) = toggledRemote()
  RemoteIconButton(
    onClick = onClick,
    colors =
      RemoteIconButtonDefaults.iconButtonColors(
        containerColor =
          tween(
            RemoteColor(androidx.compose.ui.graphics.Color.Transparent),
            RemoteMaterialTheme.colorScheme.primaryContainer,
            on,
          )
      ),
    border = 2.rdp,
    borderColor = RemoteMaterialTheme.colorScheme.outline,
    content = { RemoteIcon(addIcon, "Add".rs) },
  )
}

// The Wear sibling's `title-and-subtitle` cell, and for the same reason it is a cell of its own
// there rather than a kit node: see the `noReference` below.
@CatalogComponent(
  id = "TitleCard/Subtitle",
  group = "Containment",
  parallel = "TitleCard",
  noReference =
    "The kit's nine `Title Card 3` cells are the `Card` set's remaining gap, in this rendition " +
      "and in the Wear one (#101). That layout has NO body: its subtitle sits straight under the " +
      "title with the timestamp beside it, and neither `RemoteTitleCard` nor Wear's `TitleCard` " +
      "arranges a subtitle without a body. What Compose does have is a title over a subtitle, " +
      "which is this — published under its own name rather than mapped onto a node it is not a " +
      "picture of.",
  caption = "Title card led by a title over a subtitle, with no body — a Compose-only arrangement.",
)
@CatalogRemoteLarge
@Composable
fun SubtitleRemoteTitleCard() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteTitleCard(
    onClick = onClick,
    title = { RemoteText(title) },
    subtitle = { RemoteText(KitCopy.SUBTITLE.rs) },
  )
}

// Renamed from `AppCard/Time` when the base sticker took the kit's own timestamp: the time is no
// longer what tells this cell from the base one — the missing app image is.
@CatalogComponent(
  id = "AppCard/NoAppImage",
  group = "Containment",
  parallel = "AppCard",
  noReference =
    "The leading slot is not an axis of the kit's `Card` set: every one of its App-Card cells " +
      "carries the app's square artwork, and the set spells the alternative as a different " +
      "LAYOUT — `Title Card + Icon`, the same slot holding a vector icon — rather than as an " +
      "empty slot. An app card with nothing in front of its name is a Compose arrangement the " +
      "kit does not draw, so mapping it onto an App-Card cell would report the missing artwork " +
      "as a divergence forever.",
  caption = "App card with the app-image slot left empty — name, title, time and content only.",
)
@CatalogRemoteLarge
@Composable
fun NoAppImageRemoteAppCard() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteAppCard(
    onClick = onClick,
    appName = { RemoteText(KitCopy.APP_LABEL.rs) },
    title = { RemoteText(title) },
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    content = { RemoteText(KitCopy.CARD_CONTENT.rs) },
  )
}

@CatalogComponent(
  id = "Progress/Circular-Indeterminate",
  group = "Communication",
  parallel = "CircularProgressIndicator",
  noReference =
    "The kit's `Progress` axis is four determinate values — Zero, In progress, Complete, " +
      "Overflow — and a still frame is all a kit cell can be, so nothing in the " +
      "`Progress-Indicator` set is the indeterminate sweep. Any cell this named would be a " +
      "picture of a fixed arc, and the arc here is never at rest; what stands in for a reference " +
      "is the motion capture below.",
  motionPreview = "IndeterminateCircularProgressMotionRemote",
  caption =
    "Indeterminate circular progress; its motion capture records the continuous remote-clock " +
      "animation.",
)
@CatalogRemoteDisplay
@Composable
fun IndeterminateCircularProgressRemote() = RemoteSticker {
  RemoteCircularProgressIndicator(modifier = RemoteModifier.fillMaxSize())
}

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

@CatalogComponent(
  id = "PageIndicator/Horizontal",
  group = "Communication",
  parallel = "PageIndicator/Horizontal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:138301",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Four-page horizontal indicator curved along the bottom edge, on the first page.",
)
@CatalogRemoteDisplay
@Composable
fun HorizontalPageIndicatorRemote() = RemoteSticker {
  // FOUR pages on the FIRST, because that is the kit cell this row's `reference` names and what
  // `wear-m3-catalog`'s `PageIndicator/Horizontal` draws (`pages = 4`, `initialPage = 0`). Five
  // pages with the third selected put a different picture under the same node — and a middle
  // selection is the one arrangement in which the selected dot is hardest to pick out.
  RemoteHorizontalPageIndicator(
    state = rememberRemotePageIndicatorState(pageCount = 4, selectedPage = 0.ri),
    // `fillMaxSize`, not 180dp: the indicator curves against the BEZEL, so an inset box moves the
    // curve inward and shrinks it. Same reason the sticker is on the 227dp display frame at all.
    modifier = RemoteModifier.fillMaxSize(),
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
@Composable
fun VerticalPageIndicatorRemote() = RemoteSticker {
  // Same four-on-the-first as the horizontal one, for the same reason: this row names a kit node,
  // and `wear-m3-catalog`'s `PageIndicator/Vertical` draws the same cell. Eight pages with the
  // fifth selected exercised the scrolling-dot window, which is a real behaviour — but it is a
  // behaviour neither the kit cell nor the parallel draws, so it was reported as a divergence on
  // every render rather than shown as itself.
  RemoteVerticalPageIndicator(
    state = rememberRemotePageIndicatorState(pageCount = 4, selectedPage = 0.ri),
    modifier = RemoteModifier.fillMaxSize(),
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
