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

/**
 * The weights the Wear Material 3 type scale asks for, which is why Roboto Flex is VENDORED here
 * while every other family below is downloadable.
 *
 * `TypeScaleTokens` does not ask for `FontWeight.Normal` and `FontWeight.Medium`. It asks for
 * `wght` **450, 500, 520, 550, 560, 580, 599, 650, 700, 750, 760 and 780** — twelve values on a
 * continuous axis, paired with `wdth` 100 / 104 / 110 — and `TypographyTokens` puts each pair on
 * its role as `fontVariationSettings`. Those are instructions to a VARIABLE font, and a static face
 * ignores every one of them silently: the type scale is inert, and each role renders at whatever
 * the nearest registered weight happens to be.
 *
 * That is what the downloadable provider gives you, and it cannot give anything else. Google Fonts
 * serves Roboto Flex as a **static instance** — a single 88 KB file with no `fvar` table —
 * whichever way it is asked. A single weight, an axis range and the full axis tuple all return the
 * identical file, and the render cache shows the same thing from the other end:
 * `roboto-flex-400.ttf` and `roboto-flex-500.ttf` are byte-for-byte the same font, so even the
 * Medium roles were a synthesised emboldening of Regular.
 *
 * So the real variable face is committed under `res/font/` (OFL, `licenses/RobotoFlex-OFL.txt`),
 * with `fvar`, `gvar` and all thirteen axes. The scale's `wght` and `wdth` now land on a font that
 * has them, which is what a watch does with its device font and what this catalog has been claiming
 * to draw.
 *
 * A face is registered per token weight rather than the usual Normal/Medium pair. The matcher picks
 * the nearest registered weight and Compose synthesises the rest of the difference; with a face at
 * every value the scale names, there is nothing left to synthesise, and the `wght` baked into the
 * matched face already agrees with the `fontVariationSettings` the role carries. Registering two
 * faces and letting a 780 role resolve against a 400 one is how a variable font ends up looking
 * like a faked bold.
 */
private val TypeScaleWeights =
  listOf(400, 450, 500, 520, 550, 560, 580, 599, 650, 700, 750, 760, 780)

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
