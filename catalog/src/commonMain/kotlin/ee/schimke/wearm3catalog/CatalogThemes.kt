package ee.schimke.wearm3catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import com.materialkolor.rememberDynamicColorScheme
import ee.schimke.composeai.preview.WearThemeCatalog

/**
 * The catalog's **named themes**, declared as `@WearThemeCatalog` wrapper providers.
 *
 * The kit itself publishes one theme — the stock Wear M3 dark palette every sticker on this sheet
 * is drawn in — so these are not kit membership and they answer to no kit node. They are the other
 * half of what a design system is: proof that a component *survives* re-skinning. Each declaration
 * becomes an entry in the preview server's **Theme** select, so any sticker here can be re-rendered
 * under any of them, and the plugin bakes one specimen sheet per theme showing the Wear roles and
 * type scale it actually resolves to.
 *
 * The palettes are [Confetti Wear](https://github.com/joreilly/Confetti)'s: its stock theme plus
 * the four curated conference identities its `conferenceThemeFor` resolves at runtime, each
 * reproduced the way Confetti builds it — a seed colour through `materialkolor`'s dynamic dark
 * scheme, mapped onto the Wear roles — rather than as a transcribed table of hex literals that
 * would drift the first time either side moved. A theme is a **typeface pairing** as much as a
 * palette, so each carries Confetti's type scale too; a theme that only re-tints is half a theme,
 * and half a theme renders as a convincing-looking lie about what the app looks like.
 *
 * [GoogleSansFlexTheme] is the one entry that is not Confetti's: the stock Wear palette with only
 * the face changed, so a side-by-side against an un-themed sticker reads as a pure type comparison.
 *
 * **The Wear annotation, not the mobile `@ThemeCatalog`.** These providers install
 * `androidx.wear.compose.material3.MaterialTheme`, whose `ColorScheme` carries `primaryDim` and the
 * `surfaceContainer*` family and no `surfaceVariant` ramp. Annotated with the mobile one, every
 * generated sheet would report the baseline *mobile* M3 palette instead of the theme it declares —
 * silently, with no error and a plausible-looking sheet.
 */
@Composable
private fun ConfettiTheme(
  seedColor: Color?,
  typography: Typography,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    if (seedColor == null) ColorScheme()
    else
      rememberDynamicColorScheme(seedColor = seedColor, isDark = true, isAmoled = false)
        .toWearMaterialColors()
  MaterialTheme(colorScheme = colorScheme, typography = typography) {
    CompositionLocalProvider(LocalCatalogThemeOverride provides true, content = content)
  }
}

// The four curated seeds, lifted from Confetti Wear's `conferenceThemeFor`. Internal so
// `CatalogInventoryTest` can hold them apart: two identities sharing a seed is a copy-paste slip
// that publishes as two themes rendering identical pixels, which no reviewer catches by eye.

/** KotlinConf: JetBrains purple, the warm end of the gradient the 2025/2026 site leans on. */
internal val KotlinConfSeed = Color(0xFF7F52FF)

/** AndroidMakers: warm Parisian ochre, from the venue imagery the droidcon-run edition uses. */
internal val AndroidMakersSeed = Color(0xFFE59A4F)

/** Droidcon: the green is the whole identity, used at full saturation on their site. */
internal val DroidconSeed = Color(0xFF00D775)

/** DevFest: Google Blue, the anchor of the GDG identity. */
internal val DevFestSeed = Color(0xFF4285F4)

/**
 * Confetti Wear's stock theme: the Wear M3 defaults, unseeded, over its ship typography. The
 * baseline the four conference identities are departures from.
 */
@WearThemeCatalog(name = "Confetti (default)", group = "Confetti")
class ConfettiDefaultTheme : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    ConfettiTheme(seedColor = null, typography = ExpressiveTypography, content = content)
}

/** KotlinConf: [KotlinConfSeed] purple with JetBrains Mono titles over an Inter body. */
@WearThemeCatalog(name = "KotlinConf", group = "Confetti")
class KotlinConfTheme : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    ConfettiTheme(seedColor = KotlinConfSeed, typography = KotlinConfTypography, content = content)
}

/** AndroidMakers: [AndroidMakersSeed] ochre over Confetti's ship typography. */
@WearThemeCatalog(name = "AndroidMakers", group = "Confetti")
class AndroidMakersTheme : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    ConfettiTheme(
      seedColor = AndroidMakersSeed,
      typography = ExpressiveTypography,
      content = content,
    )
}

/** Droidcon: [DroidconSeed] green over Confetti's ship typography. */
@WearThemeCatalog(name = "Droidcon", group = "Confetti")
class DroidconTheme : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    ConfettiTheme(seedColor = DroidconSeed, typography = ExpressiveTypography, content = content)
}

/**
 * DevFest: [DevFestSeed] blue with Google Sans Flex on every role — the only conference identity
 * Confetti swaps typography for, because the face is as much of the GDG brand as the colour.
 */
@WearThemeCatalog(name = "DevFest", group = "Confetti")
class DevFestTheme : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    ConfettiTheme(
      seedColor = DevFestSeed,
      typography = GoogleSansFlexTypography,
      content = content,
    )
}

/**
 * The stock Wear M3 theme with its type scale re-pointed at **Google Sans Flex**, the Material 3
 * Expressive brand face.
 *
 * Palette-identical to an un-themed sticker on purpose: it isolates the typeface, so a side-by-side
 * against the default sheet reads as a pure type comparison rather than a type *and* colour change.
 * Not one of Confetti's — hence its own group.
 */
@WearThemeCatalog(name = "Google Sans Flex", group = "Wear M3")
class GoogleSansFlexTheme : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    ConfettiTheme(seedColor = null, typography = GoogleSansFlexTypography, content = content)
}

/**
 * Map a mobile Material 3 [androidx.compose.material3.ColorScheme] — what `materialkolor` builds
 * from a seed — onto the Wear [ColorScheme]. Confetti Wear's `toWearMaterialColors`, kept name for
 * name so the two can be diffed.
 *
 * Wear names some roles differently and has some the mobile scheme lacks:
 * * `surfaceContainerLowest` (M3) becomes Wear's `background` — the darkest surface, which is what
 *   the round bezel blends into.
 * * Wear's `*Dim` roles (`primaryDim`, `secondaryDim`, `tertiaryDim`, `errorDim`) have no mobile
 *   equivalent, so each reuses its container colour — close enough for a dimmed state on a round
 *   display, and Confetti's own choice.
 * * Wear has no `surfaceVariant` ramp, so the mobile one is simply not carried across.
 */
private fun androidx.compose.material3.ColorScheme.toWearMaterialColors(): ColorScheme =
  ColorScheme(
    primary = primary,
    primaryDim = primaryContainer,
    primaryContainer = primaryContainer,
    onPrimary = onPrimary,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    secondaryDim = secondaryContainer,
    secondaryContainer = secondaryContainer,
    onSecondary = onSecondary,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    tertiaryDim = tertiaryContainer,
    tertiaryContainer = tertiaryContainer,
    onTertiary = onTertiary,
    onTertiaryContainer = onTertiaryContainer,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    background = surfaceContainerLowest,
    onBackground = onBackground,
    error = error,
    errorDim = errorContainer,
    errorContainer = errorContainer,
    onError = onError,
    onErrorContainer = onErrorContainer,
  )
