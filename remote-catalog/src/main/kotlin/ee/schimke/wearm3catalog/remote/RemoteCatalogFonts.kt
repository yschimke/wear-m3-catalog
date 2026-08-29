package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.text.RemoteFontFamily
import androidx.compose.remote.creation.compose.text.RemoteTextStyle
import androidx.wear.compose.remote.material3.RemoteTypography

/**
 * The type scale a theme installs into the remote document, built from the faces
 * `RemoteThemeCatalogs.kt` already publishes.
 *
 * Before this, a theme moved colour and nothing else: every one of the six emitted the built-in
 * `roboto-flex` device family on all eighteen roles, so the six specimens differed only in palette
 * while the Wear column showed four typefaces
 * ([#150](https://github.com/yschimke/wear-m3-catalog/issues/150)).
 *
 * ## The faces are not re-declared here
 *
 * [remoteCatalogFont] and [remoteCatalogDisplayFont] are the existing statement of which family
 * each theme draws — published as data for a player lane to point its default-family resolution at.
 * This reads *those*, so the document and the data cannot drift into disagreeing about what
 * "KotlinConf" means. A second `when (themeName)` here would have been a second source of truth for
 * exactly the fact the sibling module is already pinned against.
 *
 * ## Named Google families, not device families
 *
 * `RemoteFontFamily.Named("google:…")` puts the family NAME in the document and the lane resolves
 * the face; a `DeviceFontFamilyName` asks the player for whatever it has under an id. The named
 * form is what the sibling's `GoogleFont(…)` faces are, and it is checkable: the renderer raises
 * `FontFallbackException` when a downloadable face does not resolve, so a typo — or a family Google
 * serves no TTF for — fails the render instead of quietly drawing Roboto.
 *
 * This is the point where the typeface stops being purely a host choice and becomes a document
 * property, and it is scoped to the themed branch only. `composePreviewRenderAll` renders with no
 * provider, so the **recorded** documents still name no family and the Wasm lane's font ratchet is
 * untouched — see `RemoteThemeCatalogs.kt` on why Inter and JetBrains Mono are not vendored yet.
 * The theme specimen stickers are what render this branch.
 *
 * ## Every role is re-pointed EXPLICITLY, and that is not belt-and-braces
 *
 * `RemoteTypography(fontFamily = …)` reads like the one-liner for this. It routes through
 * `withDefaultFontFamily`, which is the same trap the sibling's `CatalogFonts.kt` documents: it
 * fills a family in only where a style has none, and the stock roles already declare one. A theme
 * built that way renders in the stock face no matter what it declares — silently, with both renders
 * succeeding. Setting each role's `fontFamily` is what actually moves the face.
 */
private fun RemoteTypography.withFamilies(
  display: RemoteFontFamily,
  body: RemoteFontFamily,
): RemoteTypography {
  fun RemoteTextStyle.on(family: RemoteFontFamily) = copy(fontFamily = family)
  return copy(
    displayLarge = displayLarge.on(display),
    displayMedium = displayMedium.on(display),
    displaySmall = displaySmall.on(display),
    titleLarge = titleLarge.on(display),
    titleMedium = titleMedium.on(display),
    titleSmall = titleSmall.on(display),
    numeralExtraLarge = numeralExtraLarge.on(display),
    numeralLarge = numeralLarge.on(display),
    numeralMedium = numeralMedium.on(display),
    numeralSmall = numeralSmall.on(display),
    numeralExtraSmall = numeralExtraSmall.on(display),
    bodyLarge = bodyLarge.on(body),
    bodyMedium = bodyMedium.on(body),
    bodySmall = bodySmall.on(body),
    bodyExtraSmall = bodyExtraSmall.on(body),
    labelLarge = labelLarge.on(body),
    labelMedium = labelMedium.on(body),
    labelSmall = labelSmall.on(body),
  )
}

/** A Google Fonts family name as the document carries it. */
private fun googleFamily(name: String) = RemoteFontFamily.Named("google:$name")

/**
 * [stock] with every role re-pointed at the faces [themeName] declares.
 *
 * A theme that pairs nothing ([remoteCatalogDisplayFont] is null) draws one family throughout, so
 * the display half falls back to the body face rather than to the stock one — that is what
 * "single-family" means for DevFest and the Wear M3 type entry.
 */
internal fun remoteCatalogTypography(
  themeName: String,
  stock: RemoteTypography,
): RemoteTypography {
  val body = remoteCatalogFont(themeName)
  val display = remoteCatalogDisplayFont(themeName) ?: body
  return stock.withFamilies(display = googleFamily(display), body = googleFamily(body))
}
