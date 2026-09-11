package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

// The Android half of the catalog's typefaces: how a family is OBTAINED. The type scales built
// from them are common — see `CatalogTypography.kt`.

/**
 * The GMS Fonts provider every family below resolves through. The certificate array is deliberately
 * **empty**: the renderer's shadow short-circuits before signature verification, and this module is
 * only ever rendered, never shipped to a watch. Mirrors the Wear catalog sample in compose-ai-tools
 * rather than pulling in `play-services-base` for a signature nothing checks.
 */
private val GoogleFontsProvider =
  GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
  )

private fun downloadable(name: String): FontFamily {
  val font = GoogleFont(name)
  return FontFamily(
    Font(googleFont = font, fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = font, fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
  )
}

private fun flexFace(weight: Int): Font =
  Font(
    resId = R.font.roboto_flex,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
  )

actual val RobotoFlex: FontFamily = FontFamily(TypeScaleWeights.map(::flexFace))

actual val Inter: FontFamily = downloadable("Inter")

actual val GoogleSansFlex: FontFamily = downloadable("Google Sans Flex")

actual val JetBrainsMono: FontFamily = downloadable("JetBrains Mono")
