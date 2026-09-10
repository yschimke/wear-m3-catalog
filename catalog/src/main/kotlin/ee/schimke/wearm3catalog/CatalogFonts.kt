package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.wear.compose.material3.Typography

/**
 * The typefaces the declared themes in `CatalogThemes.kt` are built from, and the Wear type scales
 * that pair them.
 *
 * Every face resolves as a **downloadable Google font** rather than a TTF vendored under
 * `res/font`, so this module ships no font bytes: on a device the request goes to Play Services,
 * and under the renderer's Robolectric harness `ShadowFontsContractCompat` intercepts it and
 * answers from the shared `~/.cache/composeai/fonts/` cache (fetched once from
 * `fonts.googleapis.com`). That is the same path Confetti Wear's own `FontFamilies.kt` takes, which
 * is what keeps a theme sticker here from drifting off the app it is reproducing.
 *
 * Two weights per family — Normal and Medium — because that is what the Wear type scale asks for
 * across its display / title / body / label roles. Registering a weight nothing requests costs
 * nothing (Compose resolves a face per typeface request, not per declaration); registering too few
 * costs a synthesised, visibly wrong emboldening.
 */

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

/**
 * Roboto Flex — the display / title face of Confetti Wear's ship typography, AND the face the stock
 * Wear roles are designed around. Vendored and variable; see [TypeScaleWeights].
 */
val RobotoFlex: FontFamily = FontFamily(TypeScaleWeights.map(::flexFace))

/** Inter — Confetti Wear's body / label face, hinted for small sizes on a round display. */
val Inter: FontFamily = downloadable("Inter")

/** Google Sans Flex — the Material 3 Expressive brand face, and DevFest's identity face. */
val GoogleSansFlex: FontFamily = downloadable("Google Sans Flex")

/** JetBrains Mono — KotlinConf's title face. */
val JetBrainsMono: FontFamily = downloadable("JetBrains Mono")

/**
 * Confetti Wear's ship type scale: Roboto Flex on display / title / numerals, Inter on body and
 * label. Its `ExpressiveTypography`, reproduced role for role.
 *
 * Worth knowing what re-pointing costs, because it is not free and it is not a defect: the stock
 * Wear roles reach Roboto Flex as a *device* font carrying per-role `variationSettings` — the
 * expressive variable axes — and naming a downloadable family of the same name drops those axes.
 * Confetti makes that trade to get one face on every device rather than whatever the watch happens
 * to ship, and a catalog reproducing Confetti's themes has to make it too, or the sticker stops
 * being a picture of the app.
 */
val ExpressiveTypography: Typography = wearTypography(display = RobotoFlex, body = Inter)

/**
 * KotlinConf's type scale: JetBrains Mono on display / title / numerals — JetBrains' own
 * OFL-licensed monospace, which gives a conference name a terminal feel — with Inter kept on body
 * and label, where a full monospace would be exhausting at 12–14dp.
 */
val KotlinConfTypography: Typography = wearTypography(display = JetBrainsMono, body = Inter)

/**
 * Google Sans Flex on every role. DevFest's identity face, and — over the stock Wear palette — the
 * whole of the [GoogleSansFlexTheme] entry: a single-family pairing, the way Google uses it on
 * developers.google.com and the DevFest site.
 */
val GoogleSansFlexTypography: Typography =
  wearTypography(display = GoogleSansFlex, body = GoogleSansFlex)

/**
 * A Wear [Typography] with [body] on the body and label roles and [display] on the display, title
 * and numeral roles.
 *
 * Every role is re-pointed **explicitly**. `Typography(defaultFontFamily = …)` looks like the
 * one-liner for this and is a no-op on Wear: it fills in a family only where a style has none, and
 * every stock role already declares one. A theme built that way renders in the stock face no matter
 * what it declares — silently, which is the failure this spelling exists to avoid.
 *
 * The three **arc** (curved) roles keep the stock face. They are `CurvedTextStyle`, whose only
 * `fontFamily` overload is deprecated, and they draw the curved status strip — system chrome rather
 * than app typography. Numerals ride with [display]: they are the glanceable hero digits, and
 * JetBrains Mono's tabular figures are exactly what that role wants.
 */
private fun wearTypography(display: FontFamily, body: FontFamily): Typography {
  val base = Typography()
  return base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = display),
    displayMedium = base.displayMedium.copy(fontFamily = display),
    displaySmall = base.displaySmall.copy(fontFamily = display),
    titleLarge = base.titleLarge.copy(fontFamily = display),
    titleMedium = base.titleMedium.copy(fontFamily = display),
    titleSmall = base.titleSmall.copy(fontFamily = display),
    numeralExtraLarge = base.numeralExtraLarge.copy(fontFamily = display),
    numeralLarge = base.numeralLarge.copy(fontFamily = display),
    numeralMedium = base.numeralMedium.copy(fontFamily = display),
    numeralSmall = base.numeralSmall.copy(fontFamily = display),
    numeralExtraSmall = base.numeralExtraSmall.copy(fontFamily = display),
    labelLarge = base.labelLarge.copy(fontFamily = body),
    labelMedium = base.labelMedium.copy(fontFamily = body),
    labelSmall = base.labelSmall.copy(fontFamily = body),
    bodyLarge = base.bodyLarge.copy(fontFamily = body),
    bodyMedium = base.bodyMedium.copy(fontFamily = body),
    bodySmall = base.bodySmall.copy(fontFamily = body),
    bodyExtraSmall = base.bodyExtraSmall.copy(fontFamily = body),
  )
}

/**
 * The typography the **stock** (un-themed) sticker frame runs under: the Wear scale, re-pointed at
 * the vendored variable [RobotoFlex].
 *
 * Wear Material 3 ships `TypefaceTokens.Brand = DeviceFontFamilyName("roboto-flex")` — it asks the
 * PLATFORM for a family by that exact name. On a watch the system has one and the scale's `wght` /
 * `wdth` land on it. The Robolectric runtime does not: its font directory ships static
 * `Roboto-Regular` … `Roboto-Black` and no `roboto-flex` family at all, so the request cannot
 * resolve, Compose falls back, and every one of those axes is dropped. The catalog was therefore
 * publishing the expressive scale rendered without its expressive axes — most of this sheet, since
 * the default theme is what a sticker uses unless a `@WearThemeCatalog` provider says otherwise.
 *
 * Naming the face explicitly fixes that, and only that: [wearTypography] copies each role, so every
 * size, line height, tracking and per-role `fontVariationSettings` the library set is preserved
 * untouched. The three arc roles keep the stock face for the reason [wearTypography] gives, which
 * means their `wght 599 / wdth 100` stays inert — curved system chrome, not app typography.
 */
val DefaultTypography: Typography = wearTypography(display = RobotoFlex, body = RobotoFlex)
