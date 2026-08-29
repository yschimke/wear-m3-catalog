@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.solidColor
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.preview.CatalogComponent

// ---------------------------------------------------------------------------
// The six declared themes, each drawn as a sticker that actually goes through the themed branch of
// `RemoteSticker`.
//
// WHY THESE EXIST. `RemoteThemeCatalogs.kt` declares six `@WearThemeCatalog` providers, and until
// now nothing in a plain `composePreviewRenderAll` ever ran one: the providers are applied by the
// renderer only for a `?themeProvider=` render on a session that can recompose, so
// `LocalRemoteCatalogTheme` is null in every committed capture and the whole themed path — colours
// AND, since #150, typography — went out of the build unrendered and undiffed. The specimen sheets
// `@WearThemeCatalog` synthesises are not that evidence; they are read reflectively off
// `androidx.wear.compose.material3.MaterialTheme`, a theme these providers never install, and they
// come out byte-identical to each other. The same file already says so: "a sticker re-rendered
// under a seeded theme is".
//
// So each of these applies its own theme by hand — the same `RemoteThemeOverride` the providers
// wrap with — and draws one ramp under it. Six stickers, one per theme, in one order, so a
// side-by-side reads as a comparison: the display face on the title and numeral rows, the body face
// under it, and the two colour roles that separate a seeded identity from the baseline.
//
// WHAT A DIFF ON THESE MEANS. They are the only place either half of a theme is a rendered pixel.
// A colour role that stops being mapped, a face that stops resolving, `withDefaultFontFamily`
// swallowing a family assignment again (`RemoteCatalogFonts.kt`) — each of those is a visible move
// here and invisible everywhere else in the sheet.
//
// Two of the six are deliberately palette-identical: `Confetti (default)` and `Google Sans Flex`
// seed no colour, so that pair differs in typeface alone and is the cleanest read of the font half
// on the sheet. If those two ever render byte-identical, the typography is not reaching the
// document.
// ---------------------------------------------------------------------------

/**
 * One theme's ramp: the display face on the title and numeral rows, the body face below, and the
 * two colour roles a seed moves.
 *
 * Read from `RemoteMaterialTheme` inside the sticker rather than from [remoteCatalogTypography]
 * directly, on purpose — that is the same lookup every other component in this sheet does, so what
 * these draw is what a themed button or card would draw, not a separate rendering of the same data.
 */
@Composable
private fun ThemeSpecimen(name: String) =
  RemoteThemeOverride(name) {
    RemoteSticker {
      RemoteColumn {
        RemoteText(name.rs, style = RemoteMaterialTheme.typography.titleMedium)
        RemoteText("0123456789".rs, style = RemoteMaterialTheme.typography.numeralSmall)
        RemoteText("Body Large".rs, style = RemoteMaterialTheme.typography.bodyLarge)
        RemoteText("Label Medium".rs, style = RemoteMaterialTheme.typography.labelMedium)
        RemoteRow {
          RemoteBox(
            modifier =
              RemoteModifier.size(28.rdp)
                .background(RemoteBrush.solidColor(RemoteMaterialTheme.colorScheme.primary)),
            content = {},
          )
          RemoteBox(
            modifier =
              RemoteModifier.size(28.rdp)
                .background(
                  RemoteBrush.solidColor(RemoteMaterialTheme.colorScheme.surfaceContainer)
                ),
            content = {},
          )
        }
      }
    }
  }

/** The reason every one of the six carries the same `noReference`. */
private const val THEME_SPECIMEN_NO_REFERENCE =
  "A theme specimen: one catalog theme's type ramp and colour roles drawn through the themed " +
    "branch of `RemoteSticker`. The kit publishes one Wear M3 palette and a styles page, not a " +
    "per-theme component set, so there is no cell to compare a conference identity against."

@CatalogComponent(
  id = "Theme/Specimen-ConfettiDefault",
  group = "Theme",
  noReference = THEME_SPECIMEN_NO_REFERENCE,
  caption =
    "Confetti's stock theme: the unseeded Wear M3 palette over its ship pairing — Roboto Flex " +
      "on title and numerals, Inter on body and label.",
)
@CatalogRemoteLarge
@Composable
fun ThemeSpecimenConfettiDefaultRemote() = ThemeSpecimen("Confetti (default)")

@CatalogComponent(
  id = "Theme/Specimen-KotlinConf",
  group = "Theme",
  noReference = THEME_SPECIMEN_NO_REFERENCE,
  caption =
    "KotlinConf: JetBrains purple, with JetBrains Mono on title and numerals over an Inter body.",
)
@CatalogRemoteLarge
@Composable
fun ThemeSpecimenKotlinConfRemote() = ThemeSpecimen("KotlinConf")

@CatalogComponent(
  id = "Theme/Specimen-AndroidMakers",
  group = "Theme",
  noReference = THEME_SPECIMEN_NO_REFERENCE,
  caption = "AndroidMakers: Parisian ochre over Confetti's ship pairing.",
)
@CatalogRemoteLarge
@Composable
fun ThemeSpecimenAndroidMakersRemote() = ThemeSpecimen("AndroidMakers")

@CatalogComponent(
  id = "Theme/Specimen-Droidcon",
  group = "Theme",
  noReference = THEME_SPECIMEN_NO_REFERENCE,
  caption = "Droidcon: the green at full saturation, over Confetti's ship pairing.",
)
@CatalogRemoteLarge
@Composable
fun ThemeSpecimenDroidconRemote() = ThemeSpecimen("Droidcon")

@CatalogComponent(
  id = "Theme/Specimen-DevFest",
  group = "Theme",
  noReference = THEME_SPECIMEN_NO_REFERENCE,
  caption =
    "DevFest: Google Blue with Google Sans Flex on every role — the one conference identity " +
      "that swaps typography as well as colour.",
)
@CatalogRemoteLarge
@Composable
fun ThemeSpecimenDevFestRemote() = ThemeSpecimen("DevFest")

@CatalogComponent(
  id = "Theme/Specimen-GoogleSansFlex",
  group = "Theme",
  noReference = THEME_SPECIMEN_NO_REFERENCE,
  caption =
    "The stock Wear M3 palette with its ramp re-pointed at Google Sans Flex. " +
      "Palette-identical to the Confetti baseline on purpose: this pair differs in typeface alone.",
)
@CatalogRemoteLarge
@Composable
fun ThemeSpecimenGoogleSansFlexRemote() = ThemeSpecimen("Google Sans Flex")
