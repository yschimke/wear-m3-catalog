@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
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
  caption = "Tonal button using the theme's secondary-container emphasis.",
)
@CatalogRemoteModes
@Composable
fun TonalRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteButton(
    onClick = onClick,
    colors =
      RemoteButtonDefaults.buttonColors(
        containerColor = RemoteMaterialTheme.colorScheme.secondaryContainer,
        contentColor = RemoteMaterialTheme.colorScheme.onSecondaryContainer,
        secondaryContentColor = RemoteMaterialTheme.colorScheme.onSecondaryContainer,
        iconColor = RemoteMaterialTheme.colorScheme.onSecondaryContainer,
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
        starIcon,
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
        starIcon,
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

@CatalogComponent(
  id = "Button/Compact-IconLabel",
  group = "Buttons",
  parallel = "Button/Compact",
  noReference =
    "Varies `Button/Compact`, whose kit set the Wear sibling maps; the specific variant cell " +
      "this sticker draws has not been mapped against its export yet, and a mapping onto the base " +
      "cell would score this against the wrong variant.",
  caption = "Compact button with the defined extra-small icon plus a label.",
)
@CatalogRemoteModes
@Composable
fun CompactIconLabelRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteCompactButton(
    onClick = onClick,
    icon = {
      RemoteIcon(
        starIcon,
        contentDescription = null,
        modifier = RemoteModifier.size(RemoteButtonDefaults.ExtraSmallIconSize),
      )
    },
    label = { RemoteText(label) },
  )
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
        starIcon,
        contentDescription = "Favourite".rs,
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

@Composable
@RemoteComposable
private fun SizedIconButton(size: androidx.compose.remote.creation.compose.state.RemoteDp) {
  val (on, onClick) = toggledRemote()
  RemoteIconButton(
    onClick = onClick,
    modifier = RemoteModifier.size(size),
    colors =
      RemoteIconButtonDefaults.iconButtonColors(
        containerColor =
          tween(
            RemoteMaterialTheme.colorScheme.surfaceContainer,
            RemoteMaterialTheme.colorScheme.primaryContainer,
            on,
          ),
        contentColor = RemoteMaterialTheme.colorScheme.onSurface,
      ),
    content = {
      RemoteIcon(
        starIcon,
        contentDescription = "Favourite".rs,
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
    content = { RemoteIcon(starIcon, "Favourite".rs) },
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
    content = { RemoteIcon(starIcon, "Favourite".rs) },
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
            RemoteMaterialTheme.colorScheme.surfaceContainer,
            RemoteMaterialTheme.colorScheme.primary,
            on,
          ),
        // Travels with the container — see `OutlinedRemoteTextButton` for why.
        contentColor =
          tween(
            RemoteMaterialTheme.colorScheme.onSurface,
            RemoteMaterialTheme.colorScheme.onPrimary,
            on,
          ),
      ),
    content = { RemoteText(label.rs, style = style) },
  )
}

@CatalogComponent(
  id = "Button/Text-Filled",
  group = "Buttons",
  parallel = "TextButton",
  noReference =
    "Varies `TextButton`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "Round text button with a filled primary container.",
)
@CatalogRemoteModes
@Composable
fun FilledRemoteTextButton() = RemoteSticker {
  // A colour tween rather than a click tally: `MMM` already fills this circle, so a counter would
  // draw `MMM (1)` through its edge. See `toggledRemote`.
  val (on, onClick) = toggledRemote()
  RemoteTextButton(
    onClick = onClick,
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
        // a light container at the end of the tween — and further off under the catalog's light
        // Coral / Teal primary overrides. At rest `on` is 0f and both are their stock values, so
        // the baked capture is unchanged.
        containerColor = tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primary, on),
        contentColor = tween(stock.contentColor, RemoteMaterialTheme.colorScheme.onPrimary, on),
      ),
    content = { RemoteText(KitCopy.GLYPHS.rs) },
  )
}

@CatalogComponent(
  id = "TitleCard/TitleOnly",
  group = "Containment",
  parallel = "TitleCard",
  noReference =
    "Varies `TitleCard`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "Minimal title-card layout with no optional time, subtitle or body slots.",
)
@CatalogRemoteLarge
@Composable
fun TitleOnlyRemoteTitleCard() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteTitleCard(onClick = onClick, title = { RemoteText(title) })
}

@CatalogComponent(
  id = "TitleCard/TimeContent",
  group = "Containment",
  parallel = "TitleCard",
  noReference =
    "Varies `TitleCard`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption =
    "Title card with time and supporting content, exercising the alternate title-row layout.",
)
@CatalogRemoteLarge
@Composable
fun TimeContentRemoteTitleCard() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteTitleCard(
    onClick = onClick,
    title = { RemoteText(title) },
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    content = { RemoteText(KitCopy.CARD_CONTENT.rs) },
  )
}

@CatalogComponent(
  id = "AppCard/Time",
  group = "Containment",
  parallel = "AppCard",
  noReference =
    "Varies `AppCard`, whose kit set the Wear sibling maps; the specific variant cell this " +
      "sticker draws has not been mapped against its export yet, and a mapping onto the base cell " +
      "would score this against the wrong variant.",
  caption = "App card with the optional time slot and no app image.",
)
@CatalogRemoteLarge
@Composable
fun TimeRemoteAppCard() = RemoteSticker {
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
  RemoteCircularProgressIndicator(
    progress = 0.66f.rf,
    enabled = false.rb,
    modifier = RemoteModifier.size(72.rdp),
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
  RemoteCircularProgressIndicator(modifier = RemoteModifier.size(72.rdp))
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
  RemoteCircularProgressIndicator(modifier = RemoteModifier.size(72.rdp))
}

@CatalogComponent(
  id = "PageIndicator/Horizontal",
  group = "Communication",
  parallel = "PageIndicator/Horizontal",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38684:138301",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Five-page horizontal indicator curved along the bottom edge.",
)
@CatalogRemoteDisplay
@Composable
fun HorizontalPageIndicatorRemote() = RemoteSticker {
  RemoteHorizontalPageIndicator(
    state = rememberRemotePageIndicatorState(pageCount = 5, selectedPage = 2.ri),
    modifier = RemoteModifier.size(180.rdp),
  )
}

@CatalogComponent(
  id = "PageIndicator/Vertical",
  group = "Communication",
  parallel = "PageIndicator/Vertical",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38966:402",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38684:137917",
  caption = "Eight-page vertical indicator exercising the scrolling-dot window.",
)
@CatalogRemoteDisplay
@Composable
fun VerticalPageIndicatorRemote() = RemoteSticker {
  RemoteVerticalPageIndicator(
    state = rememberRemotePageIndicatorState(pageCount = 8, selectedPage = 4.ri),
    modifier = RemoteModifier.size(180.rdp),
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
    modifier = RemoteModifier.size(180.rdp),
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
  RemoteHorizontalPageIndicator(
    state = state,
    modifier = RemoteModifier.size(180.rdp),
  )
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
