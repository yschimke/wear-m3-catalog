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
import androidx.compose.remote.creation.compose.state.rb
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
import androidx.wear.compose.remote.material3.RemoteCompactButton
import androidx.wear.compose.remote.material3.RemoteHorizontalPageIndicator
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteIconButton
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemotePageIndicatorState
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTextButton
import androidx.wear.compose.remote.material3.RemoteTextButtonDefaults
import androidx.wear.compose.remote.material3.RemoteTimeText
import androidx.wear.compose.remote.material3.RemoteTitleCard
import androidx.wear.compose.remote.material3.RemoteVerticalPageIndicator
import androidx.wear.compose.remote.material3.rememberRemotePageIndicatorState
import ee.schimke.composeai.preview.AnimatedPreview
import ee.schimke.composeai.preview.CatalogComponent

// This file is the variant matrix for the public component surface in remote-material3 alpha09.
// CatalogPreviews.kt keeps the concise one-per-family set; these previews cover every public size,
// the supported emphasis treatments, and the optional icon/label/time/content slots discovered by
// reviewing the AndroidX source JAR that the module actually resolves.

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
  id = "Button/IconLabel",
  group = "Buttons",
  parallel = "Button/Filled",
  noReference =
    "Varies `Button/Filled`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
  caption = "Opinionated RemoteButton overload with its recommended icon and label slots.",
)
@CatalogRemoteLarge
@Composable
fun IconLabelRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteButton(
    onClick = onClick,
    icon = {
      RemoteIcon(
        addIcon,
        contentDescription = null,
        modifier = RemoteModifier.size(RemoteButtonDefaults.IconSize),
      )
    },
    label = { RemoteText(label) },
  )
}

@CatalogComponent(
  id = "Button/IconLabelSecondary",
  group = "Buttons",
  parallel = "Button/Filled",
  noReference =
    "Varies `Button/Filled`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
  caption = "Two-line RemoteButton with a large icon, primary label and secondary label.",
)
@CatalogRemoteLarge
@Composable
fun IconLabelSecondaryRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteButton(
    onClick = onClick,
    icon = {
      RemoteIcon(
        addIcon,
        contentDescription = null,
        modifier = RemoteModifier.size(RemoteButtonDefaults.LargeIconSize),
      )
    },
    secondaryLabel = { RemoteText(KitCopy.SECONDARY_LABEL.rs) },
    label = { RemoteText(label) },
  )
}

@CatalogComponent(
  id = "Button/Disabled",
  group = "Buttons",
  parallel = "Button/Filled",
  noReference =
    "Varies `Button/Filled`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
  caption = "Filled button with the library's disabled container and content colours.",
)
@CatalogRemoteModes
@Composable
fun DisabledRemoteButton() = RemoteSticker {
  RemoteButton(
    onClick = valueChange(rememberMutableRemoteInt(0), 1.ri),
    enabled = false.rb,
    content = { RemoteText(KitCopy.PRIMARY_LABEL.rs) },
  )
}

// The kit's `Button-Compact` `Icon=No` cell, and the Wear sibling's `text-only` cell on the same
// set. It used to be the OTHER way round — this was `Compact-IconLabel` and the base sticker drew
// the label alone — which published the kit's base cell under a variant name and a variant cell as
// the base. The base now draws what the kit's base cell draws (icon + label); what is left over,
// and needs a name of its own, is the label without the icon.
@CatalogComponent(
  id = "Button/Compact-TextOnly",
  group = "Buttons",
  parallel = "Button/Compact",
  noReference =
    "Varies `Button/Compact`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
  caption = "Compact button with a label and no icon — the kit's Icon=No cell.",
)
@CatalogRemoteModes
@Composable
fun CompactTextOnlyRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteCompactButton(onClick = onClick, label = { RemoteText(label) })
}

@CatalogComponent(
  id = "Button/Compact-IconOnly",
  group = "Buttons",
  parallel = "Button/Compact",
  noReference =
    "Varies `Button/Compact`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
  caption = "Icon-only compact button using its dedicated 52dp visible width.",
)
@CatalogRemoteModes
@Composable
fun CompactIconOnlyRemoteButton() = RemoteSticker {
  val (on, onClick) = toggledRemote()
  val stock = RemoteButtonDefaults.buttonColors()
  RemoteCompactButton(
    onClick = onClick,
    colors =
      RemoteButtonDefaults.buttonColors(
        containerColor =
          tween(stock.containerColor, RemoteMaterialTheme.colorScheme.tertiaryContainer, on)
      ),
    icon = {
      RemoteIcon(
        addIcon,
        contentDescription = "Add".rs,
        modifier = RemoteModifier.size(RemoteButtonDefaults.SmallIconSize),
      )
    },
    label = null,
  )
}

@CatalogComponent(
  id = "Button/Icon-ExtraSmall",
  group = "Buttons",
  parallel = "IconButton/Standard",
  noReference =
    "Varies `IconButton/Standard`, whose kit set the Wear sibling maps; the specific variant " +
      "cell this sticker draws has not been mapped against its export yet, and a mapping onto the " +
      "base cell would score this against the wrong variant.",
  caption = "Extra-small RemoteIconButton at the defined 32dp size.",
)
@CatalogRemoteModes
@Composable
fun ExtraSmallRemoteIconButton() = RemoteSticker {
  SizedIconButton(RemoteIconButtonDefaults.ExtraSmallButtonSize)
}

@CatalogComponent(
  id = "Button/Icon-Small",
  group = "Buttons",
  parallel = "IconButton/Standard",
  noReference =
    "Varies `IconButton/Standard`, whose kit set the Wear sibling maps; the specific variant " +
      "cell this sticker draws has not been mapped against its export yet, and a mapping onto the " +
      "base cell would score this against the wrong variant.",
  caption = "Small RemoteIconButton at the defined 48dp size.",
)
@CatalogRemoteModes
@Composable
fun SmallRemoteIconButton() = RemoteSticker {
  SizedIconButton(RemoteIconButtonDefaults.SmallButtonSize)
}

@CatalogComponent(
  id = "Button/Icon-Large",
  group = "Buttons",
  parallel = "IconButton/Standard",
  noReference =
    "Varies `IconButton/Standard`, whose kit set the Wear sibling maps; the specific variant " +
      "cell this sticker draws has not been mapped against its export yet, and a mapping onto the " +
      "base cell would score this against the wrong variant.",
  caption = "Large RemoteIconButton at the defined 60dp size.",
)
@CatalogRemoteModes
@Composable
fun LargeRemoteIconButton() = RemoteSticker {
  SizedIconButton(RemoteIconButtonDefaults.LargeButtonSize)
}

/**
 * The three size cells of `IconButton/Standard` — and standard means **no container**.
 *
 * The container is what the STYLE cells vary (`Button/Icon-Filled`, `Button/Icon-Outlined`); these
 * three vary only the size, so they take the stock colours their base sticker ([IconRemoteButton])
 * takes. Painting `surfaceContainer` behind them, as this did, drew a filled icon button under the
 * name of the kit's child style and put a disc beside three parallels that have none — a difference
 * on three rows that was this sticker's own doing rather than Remote Compose's. The tween end
 * colour is unchanged: at rest `on` is 0f and `tween(a, b, 0f)` is `a`, so the baked capture is the
 * stock (transparent) container and only a live tap moves it.
 */
@Composable
@RemoteComposable
private fun SizedIconButton(size: androidx.compose.remote.creation.compose.state.RemoteDp) {
  val (on, onClick) = toggledRemote()
  val stock = RemoteIconButtonDefaults.iconButtonColors()
  RemoteIconButton(
    onClick = onClick,
    modifier = RemoteModifier.size(size),
    colors =
      RemoteIconButtonDefaults.iconButtonColors(
        containerColor =
          tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primaryContainer, on)
      ),
    content = {
      RemoteIcon(
        addIcon,
        contentDescription = "Add".rs,
        modifier = RemoteModifier.size(RemoteIconButtonDefaults.iconSizeFor(size)),
      )
    },
  )
}

@CatalogComponent(
  id = "Button/Icon-Filled",
  group = "Buttons",
  parallel = "IconButton/Filled",
  noReference =
    "Varies `IconButton/Filled`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
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
  noReference =
    "Varies `IconButton/Outlined`, whose kit set the Wear sibling maps; the specific variant " +
      "cell this sticker draws has not been mapped against its export yet, and a mapping onto the " +
      "base cell would score this against the wrong variant.",
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

@CatalogComponent(
  id = "Button/Text-Small",
  group = "Buttons",
  parallel = "TextButton",
  noReference =
    "Varies `TextButton`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "Small RemoteTextButton at the defined 48dp size.",
)
@CatalogRemoteModes
@Composable
fun SmallRemoteTextButton() = RemoteSticker {
  SizedTextButton(
    size = RemoteTextButtonDefaults.SmallButtonSize,
    style = RemoteTextButtonDefaults.smallButtonTextStyle,
    label = KitCopy.GLYPHS,
  )
}

@CatalogComponent(
  id = "Button/Text-Large",
  group = "Buttons",
  parallel = "TextButton",
  noReference =
    "Varies `TextButton`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "Large RemoteTextButton at the defined 60dp size and labelLarge typography.",
)
@CatalogRemoteModes
@Composable
fun LargeRemoteTextButton() = RemoteSticker {
  SizedTextButton(
    size = RemoteTextButtonDefaults.LargeButtonSize,
    style = RemoteTextButtonDefaults.largeButtonTextStyle,
    label = KitCopy.GLYPHS,
  )
}

/**
 * The two size cells of `TextButton`.
 *
 * They vary the SIZE, so they take the same filled container the base sticker ([TextRemoteButton])
 * takes — the kit's `Text-Button` size cells are drawn on its base style, and the style cells are
 * the ones that change the container. Painting `surfaceContainer` behind them, as this did, made
 * every size cell read as a third style beside two parallels that are filled.
 */
@Composable
@RemoteComposable
private fun SizedTextButton(
  size: androidx.compose.remote.creation.compose.state.RemoteDp,
  style: androidx.compose.remote.creation.compose.text.RemoteTextStyle,
  label: String,
) {
  val (on, onClick) = toggledRemote()
  RemoteTextButton(
    onClick = onClick,
    modifier = RemoteModifier.size(size),
    colors =
      RemoteTextButtonDefaults.textButtonColors(
        containerColor =
          tween(
            RemoteMaterialTheme.colorScheme.primary,
            RemoteMaterialTheme.colorScheme.primaryDim,
            on,
          ),
        contentColor = RemoteMaterialTheme.colorScheme.onPrimary,
      ),
    content = { RemoteText(label.rs, style = style) },
  )
}

// The kit's `Style=Child (No background)` cell — a round text button with no container at all,
// which is what `RemoteTextButtonDefaults.textButtonColors()` returns. It was published as
// `Button/Text-Filled` while the BASE sticker drew this style; the two swapped names when the base
// took the kit's filled base cell, so no picture was lost, only relabelled.
@CatalogComponent(
  id = "Button/Text-Child",
  group = "Buttons",
  parallel = "TextButton",
  noReference =
    "Varies `TextButton`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "Round text button with no container — the kit's child style.",
)
@CatalogRemoteModes
@Composable
fun ChildRemoteTextButton() = RemoteSticker {
  // A colour tween rather than a click tally: `MMM` already fills this circle, so a counter would
  // draw `MMM (1)` through its edge. See `toggledRemote`.
  val (on, onClick) = toggledRemote()
  val stock = RemoteTextButtonDefaults.textButtonColors()
  RemoteTextButton(
    onClick = onClick,
    colors =
      RemoteTextButtonDefaults.textButtonColors(
        // The content travels with the container — see `OutlinedRemoteTextButton` for why.
        containerColor = tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primary, on),
        contentColor = tween(stock.contentColor, RemoteMaterialTheme.colorScheme.onPrimary, on),
      ),
    content = { RemoteText(KitCopy.GLYPHS.rs) },
  )
}

@CatalogComponent(
  id = "Button/Text-Outlined",
  group = "Buttons",
  parallel = "TextButton",
  noReference =
    "Varies `TextButton`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "Round text button with an explicit outline treatment.",
)
@CatalogRemoteModes
@Composable
fun OutlinedRemoteTextButton() = RemoteSticker {
  val (on, onClick) = toggledRemote()
  val stock = RemoteTextButtonDefaults.textButtonColors()
  RemoteTextButton(
    onClick = onClick,
    border = 2.rdp,
    borderColor = RemoteMaterialTheme.colorScheme.outline,
    colors =
      RemoteTextButtonDefaults.textButtonColors(
        // The content travels with the container. `primary` is a LIGHT fill in the dark-first
        // scheme, so leaving the label at the stock near-white `onSurface` would land light text on
        // a light container at the end of the tween — and further off under the catalog's declared
        // themes, whose seeded primaries are lighter still. At rest `on` is 0f and both are their
        // stock values, so the baked capture is unchanged.
        containerColor = tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primary, on),
        contentColor = tween(stock.contentColor, RemoteMaterialTheme.colorScheme.onPrimary, on),
      ),
    content = { RemoteText(KitCopy.GLYPHS.rs) },
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

// Renamed from `TitleCard/TimeContent`, which drew title + time + content — that is `Title Card 1`,
// and `Title Card 1` is the BASE row's own cell. Time and content are no longer what tells this
// one apart now that the base fills them; the subtitle is, which is exactly what `Title Card 2`
// adds to `Title Card 1`.
@CatalogComponent(
  id = "TitleCard/WithSubtitle",
  group = "Containment",
  parallel = "TitleCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39662:45982",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "Title card with the subtitle under its body — the kit's Title Card 2 layout.",
)
@CatalogRemoteLarge
@Composable
fun WithSubtitleRemoteTitleCard() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteTitleCard(
    onClick = onClick,
    title = { RemoteText(title) },
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    subtitle = { RemoteText(KitCopy.SUBTITLE.rs) },
    content = { RemoteText(KitCopy.CARD_CONTENT.rs) },
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
  id = "Progress/Circular-Disabled",
  group = "Communication",
  parallel = "CircularProgressIndicator",
  noReference =
    "Varies `CircularProgressIndicator`, whose kit set the Wear sibling maps; the specific " +
      "variant cell this sticker draws has not been mapped against its export yet, and a mapping " +
      "onto the base cell would score this against the wrong variant.",
  caption = "Determinate progress using the component's disabled indicator and track brushes.",
)
@CatalogRemoteDisplay
@Composable
fun DisabledCircularProgressRemote() = RemoteSticker {
  // Same 0.6 fill and same display-edge rail as `Progress/Circular`, so the only thing this cell
  // varies against it is `enabled`.
  RemoteCircularProgressIndicator(
    progress = 0.6f.rf,
    enabled = false.rb,
    modifier = RemoteModifier.fillMaxSize(),
  )
}

@CatalogComponent(
  id = "Progress/Circular-Indeterminate",
  group = "Communication",
  parallel = "CircularProgressIndicator",
  noReference =
    "Varies `CircularProgressIndicator`, whose kit set the Wear sibling maps; the specific " +
      "variant cell this sticker draws has not been mapped against its export yet, and a mapping " +
      "onto the base cell would score this against the wrong variant.",
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
    "Varies `PageIndicator/Horizontal`, whose kit set the Wear sibling maps; the specific " +
      "variant cell this sticker draws has not been mapped against its export yet, and a mapping " +
      "onto the base cell would score this against the wrong variant.",
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
