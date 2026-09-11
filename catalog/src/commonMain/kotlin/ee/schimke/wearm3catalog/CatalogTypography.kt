package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.FontFamily
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
 * Roboto Flex, Inter, Google Sans Flex and JetBrains Mono, supplied by the platform.
 *
 * `expect` because HOW a face is obtained is the one genuinely platform-bound thing here, and the
 * type scales below are not: the scale is arithmetic over a [FontFamily], identical everywhere. On
 * Android each family resolves through the GMS downloadable-font provider (Roboto Flex from the
 * vendored variable face) — see `CatalogFonts.android.kt` for why that split is not arbitrary.
 * Other targets fall back to the platform's own families, which is honest rather than silent: a
 * desktop render says "this is the scale, in the default face", not "this is Confetti Wear's face"
 * when it is not.
 */
/**
 * Roboto Flex — the display / title face of Confetti Wear's ship typography, AND the face the stock
 * Wear roles are designed around. Vendored and variable; see [TypeScaleWeights].
 */
expect val RobotoFlex: FontFamily

/** Inter — Confetti Wear's body / label face, hinted for small sizes on a round display. */
expect val Inter: FontFamily

/** Google Sans Flex — the Material 3 Expressive brand face, and DevFest's identity face. */
expect val GoogleSansFlex: FontFamily

/** JetBrains Mono — KotlinConf's title face. */
expect val JetBrainsMono: FontFamily

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
