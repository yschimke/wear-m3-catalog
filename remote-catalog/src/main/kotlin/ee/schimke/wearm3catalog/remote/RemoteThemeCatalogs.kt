@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.wear.compose.remote.material3.RemoteColorScheme
import com.materialkolor.dynamicColorScheme
import ee.schimke.composeai.preview.WearThemeCatalog

/**
 * The catalog's declared themes — **the sibling `:catalog`'s set**, name for name and seed for
 * seed: Confetti Wear's stock theme plus the four conference identities its `conferenceThemeFor`
 * resolves, and the stock palette re-typed in Google Sans Flex.
 *
 * They used to be a different five (`M3`, `Coral`, `Teal`, `Google Sans Flex`, `KotlinConf`),
 * inherited unchanged from the `wear-m3` harness catalog in yschimke/compose-ai-tools when this
 * sheet moved here. Two catalogs of the same surface offering two different Theme selects made the
 * one comparison this repo exists to publish impossible to read: a reviewer picking "Droidcon" on
 * the kit rendition had nothing to pick beside it on the Remote one, and picking "Coral" here
 * compared a palette that exists nowhere else against a component drawn in a palette that does
 * ([issue #99](https://github.com/yschimke/wear-m3-catalog/issues/99)). The sets are now one set,
 * so the compare page's two columns move together.
 *
 * ## A theme can be applied after recording, not only during it
 *
 * This is the load-bearing difference from the Wear sibling. Every sticker is recorded **once,
 * under the default theme**, and a selected theme can then be applied to the already-recorded
 * document by overriding **named values** — no recomposition, and no per-theme capture.
 *
 * That works because `RemoteMaterialTheme`'s scheme is not constant-folded into the document. Each
 * role it draws through is emitted as named state — `USER:WearM3.primary`,
 * `USER:WearM3.surfaceContainer`, `USER:WearM3.onSurface`, and so on — and the player's
 * `setNamedColorOverride` reaches exactly those. Applying [remoteCatalogThemeColors] to a replayed
 * document therefore re-themes it with no recomposition:
 * ```
 * /render/button-filled__ideal__default__compact.png
 *     ?rc.WearM3.primary=color:%237F52FF&rc.WearM3.onPrimary=color:%23FFFFFF
 * ```
 *
 * returns a KotlinConf-purple button from the *published* catalog, whose bytecode was dropped at
 * pack time.
 *
 * Recording under the default theme is what makes that possible. A document captured with a theme
 * baked in would carry that theme's values as its constants, so every theme would need its own
 * capture and a published catalog could only ever show the one it was packed with. One
 * theme-independent document plus a small map of overrides replaces N documents.
 *
 * The recorded documents stay theme-independent because of *how* they are recorded, not because the
 * providers below do nothing: `composePreviewRenderAll` renders each `@Preview` with no provider,
 * so [LocalRemoteCatalogTheme] is null and nothing is installed. A provider is only ever applied
 * when the renderer deliberately wraps a preview in one — a `?themeProvider=` render on a session
 * that can recompose — and there it installs the scheme through [remoteCatalogColorScheme], reading
 * the same [remoteCatalogThemeColors] map the replay path seeds. One definition, two lanes: a theme
 * that recomposed to one palette and replayed to another would be worse than either, and nothing
 * would catch it, because both renders succeed.
 *
 * ## The palettes are built from the seed, not transcribed from the sibling
 *
 * `AGENTS.md` → Themes: *reproduce a borrowed theme by its recipe, not its output*. So the four
 * conference palettes run the same seed through the same `materialkolor` dynamic dark scheme
 * `:catalog` runs it through, mapped onto the Wear roles by the same rules ([confettiWearRoles]) —
 * rather than a table of the hex values that module happened to resolve to, which would drift
 * silently the first time either side moved.
 *
 * The **seeds themselves** are duplicated, because the two modules cannot share code: `:catalog` is
 * on the stable Compose BOM and this one is on the alpha Remote line with no BOM, which is the
 * whole reason there are two modules (`AGENTS.md` → Two modules). `RemoteCatalogThemeTest` pins
 * each literal, and `CatalogInventoryTest` pins the sibling's, so a seed moved on one side fails
 * the other side's build rather than quietly publishing two "Droidcon"s.
 *
 * ## The typeface half rides the document, and the theme's half only on the themed branch
 *
 * A named value can carry a colour, a float, an int or a bool — not a face — so the two halves of a
 * theme arrive by different routes. The colours are named-value overrides, which is what lets one
 * default-themed capture be re-themed afterwards. The typeface cannot be, and that splits it in
 * two:
 *
 * - **A baseline face is in every capture.** `RemoteCatalogTypography` names `google:Roboto Flex`
 *   outright rather than emitting the stock `roboto-flex` device-family id and trusting each player
 *   to resolve it the same way; `RemoteTypographyIrCaptureTest` holds it there. That is determinism
 *   for the recorded documents, not a theme.
 * - **A theme's own pairing is installed on the themed branch only.** `RemoteCatalogFonts.kt` turns
 *   [remoteCatalogFont] / [remoteCatalogDisplayFont] into a `RemoteTypography` with all eighteen
 *   roles re-pointed, and `RemoteSticker` layers it over the baseline. A themed render is a live
 *   re-render rather than a replay, which is exactly why it can carry a face where the replay lane
 *   cannot.
 *
 * The second half was a real gap, not a stylistic one: before it a theme moved colour and nothing
 * else, so the six identities differed in palette alone while the sibling column showed four
 * typefaces ([#150](https://github.com/yschimke/wear-m3-catalog/issues/150)).
 *
 * The faces are the sibling's: Confetti's ship pairing (Roboto Flex on display / title / numerals,
 * Inter on body and label) under the three themes that ship it, JetBrains Mono over Inter for
 * KotlinConf, and Google Sans Flex throughout for DevFest and the Wear M3 type entry. Those two
 * functions stay the single statement of that — `RemoteCatalogFonts.kt` reads them rather than
 * repeating them, so the document and the published data cannot drift apart.
 *
 * Neither Inter nor JetBrains Mono is vendored yet, and neither half changes that. A face only has
 * to reach the `cmp-wasm-catalog` `fonts.json` in yschimke/compose-ai-tools once a *document* names
 * it — that lane is manifest-only and never fetches, so an unlisted family a document asks for
 * fails `RcComposeSupport.fontFamilyIssue`'s availability check rather than degrading to a
 * substitute. The documents that lane replays are the **recorded** ones, and those name only Roboto
 * Flex: `composePreviewRenderAll` records with no provider, so the themed branch never runs for
 * them. Only the theme specimen stickers (`RemoteThemeSpecimens.kt`) name Inter or JetBrains Mono,
 * and they are rendered here rather than replayed there. Vendoring the two now would add to the
 * Wasm player's size ratchet to buy nothing — Inter failed that ratchet once already. They land
 * with the lane that replays a themed document. Google Sans Flex stays vendored: it was already
 * there and the ratchet was already raised for it.
 *
 * ## What a theme deliberately does not reach
 *
 * The named-family stickers keep the exact faces they declare: [BrandedTextRemote],
 * [TypefaceSpecimenRemote], [VariableWeightRemote] and [VariableWidthRemote] are untouched under
 * every theme, because they exist to keep the named-family and font-variation-axis paths rendered
 * and diffed, and a theme that overrode them would delete the one place each capability is covered.
 *
 * ## A caveat on the synthesised specimen sheets
 *
 * `@WearThemeCatalog` also makes the renderer synthesise a specimen sheet per theme, read
 * reflectively off `androidx.wear.compose.material3.MaterialTheme` — a *phone/watch* Compose theme
 * these providers never install. Those sheets are byte-identical to each other and are not evidence
 * of anything here; a sticker re-rendered under a seeded theme is. `RemoteThemeSpecimens.kt` is
 * that sticker, six times — the only place in this sheet where either half of a theme is a rendered
 * pixel rather than a declaration.
 */

/** The declared theme names, in display order — the sibling's order. */
val REMOTE_THEME_NAMES: List<String> =
  listOf(
    "Confetti (default)",
    "KotlinConf",
    "AndroidMakers",
    "Droidcon",
    "DevFest",
    "Google Sans Flex",
  )

// The four curated seeds, lifted from Confetti Wear's `conferenceThemeFor` and duplicated from the
// sibling's `CatalogThemes.kt` — see this file's header for why they are duplicated rather than
// shared, and `RemoteCatalogThemeTest` for what holds the two copies together.

/** KotlinConf: JetBrains purple, the warm end of the gradient the 2025/2026 site leans on. */
internal val RemoteKotlinConfSeed = Color(0xFF7F52FF)

/** AndroidMakers: warm Parisian ochre, from the venue imagery the droidcon-run edition uses. */
internal val RemoteAndroidMakersSeed = Color(0xFFE59A4F)

/** Droidcon: the green is the whole identity, used at full saturation on their site. */
internal val RemoteDroidconSeed = Color(0xFF00D775)

/** DevFest: Google Blue, the anchor of the GDG identity. */
internal val RemoteDevFestSeed = Color(0xFF4285F4)

/**
 * The named-value overrides that apply [name] to an already-recorded document, keyed by the
 * document's own state names (`WearM3.<role>`, which the player qualifies to `USER:WearM3.<role>`).
 *
 * Empty for the two themes that move no colour: **Confetti (default)** is Confetti's unseeded
 * theme, which is the stock Wear scheme (it is the baseline the four identities are departures
 * from, and it differs from stock only in its type scale), and **Google Sans Flex** is
 * palette-identical to it on purpose, so a side-by-side of the two is a type comparison and nothing
 * else.
 *
 * A key absent from a given document is simply not overridden — each sticker emits only the roles
 * it actually draws through — so one map applies unchanged across the whole catalog.
 */
fun remoteCatalogThemeColors(name: String): Map<String, Color> =
  when (name) {
    "KotlinConf" -> confettiWearRoles(RemoteKotlinConfSeed)
    "AndroidMakers" -> confettiWearRoles(RemoteAndroidMakersSeed)
    "Droidcon" -> confettiWearRoles(RemoteDroidconSeed)
    "DevFest" -> confettiWearRoles(RemoteDevFestSeed)
    else -> emptyMap()
  }

/**
 * [seed] through `materialkolor`'s dynamic **dark** scheme, mapped onto the `WearM3.<role>` names a
 * recorded document carries.
 *
 * This is Confetti Wear's `toWearMaterialColors`, role for role, and the sibling runs the identical
 * mapping into `androidx.wear.compose.material3.ColorScheme`. Wear names some roles differently and
 * has some the mobile scheme lacks:
 * * `surfaceContainerLowest` (M3) becomes Wear's `background` — the darkest surface, which is what
 *   the round bezel blends into.
 * * Wear's `*Dim` roles have no mobile equivalent, so each reuses its container colour — Confetti's
 *   own choice, and close enough for a dimmed state on a round display.
 * * Wear has no `surfaceVariant` ramp, so the mobile one is simply not carried across.
 *
 * `isDark = true` is not a default worth parameterising: every document here is recorded in the
 * dark-first Remote Material scheme and the catalog publishes one dark mode (`modes: ["dark"]`), so
 * a light variant of a theme would be a palette nothing on this sheet could be rendered in.
 */
private fun confettiWearRoles(seed: Color): Map<String, Color> {
  val m3 = dynamicColorScheme(seedColor = seed, isDark = true, isAmoled = false)
  return mapOf(
    "WearM3.primary" to m3.primary,
    "WearM3.primaryDim" to m3.primaryContainer,
    "WearM3.primaryContainer" to m3.primaryContainer,
    "WearM3.onPrimary" to m3.onPrimary,
    "WearM3.onPrimaryContainer" to m3.onPrimaryContainer,
    "WearM3.secondary" to m3.secondary,
    "WearM3.secondaryDim" to m3.secondaryContainer,
    "WearM3.secondaryContainer" to m3.secondaryContainer,
    "WearM3.onSecondary" to m3.onSecondary,
    "WearM3.onSecondaryContainer" to m3.onSecondaryContainer,
    "WearM3.tertiary" to m3.tertiary,
    "WearM3.tertiaryDim" to m3.tertiaryContainer,
    "WearM3.tertiaryContainer" to m3.tertiaryContainer,
    "WearM3.onTertiary" to m3.onTertiary,
    "WearM3.onTertiaryContainer" to m3.onTertiaryContainer,
    "WearM3.surfaceContainerLow" to m3.surfaceContainerLow,
    "WearM3.surfaceContainer" to m3.surfaceContainer,
    "WearM3.surfaceContainerHigh" to m3.surfaceContainerHigh,
    "WearM3.onSurface" to m3.onSurface,
    "WearM3.onSurfaceVariant" to m3.onSurfaceVariant,
    "WearM3.outline" to m3.outline,
    "WearM3.outlineVariant" to m3.outlineVariant,
    "WearM3.background" to m3.surfaceContainerLowest,
    "WearM3.onBackground" to m3.onBackground,
    "WearM3.error" to m3.error,
    "WearM3.errorDim" to m3.errorContainer,
    "WearM3.errorContainer" to m3.errorContainer,
    "WearM3.onError" to m3.onError,
    "WearM3.onErrorContainer" to m3.onErrorContainer,
  )
}

/**
 * The Google Fonts family [name] draws its **body** text in — data for a player lane to point its
 * default-family resolution at, not something installed into the document.
 *
 * Confetti's ship pairing puts Inter on body and label under every theme it ships, which is four of
 * the six here; DevFest and the Wear M3 type entry draw Google Sans Flex throughout.
 */
fun remoteCatalogFont(name: String): String =
  when (name) {
    "DevFest",
    "Google Sans Flex" -> "Google Sans Flex"
    else -> "Inter"
  }

/**
 * The face [name] pairs against its body face on the display / title / numeral roles, or null when
 * it uses one family throughout — the sibling's type scales, read back out.
 *
 * Confetti's ship scale pairs Roboto Flex against Inter; KotlinConf swaps in JetBrains Mono, whose
 * tabular figures are exactly what the numeral roles want. DevFest and the Wear M3 type entry are
 * single-family, so they pair nothing.
 */
fun remoteCatalogDisplayFont(name: String): String? =
  when (name) {
    "KotlinConf" -> "JetBrains Mono"
    "DevFest",
    "Google Sans Flex" -> null
    else -> "Roboto Flex"
  }

/**
 * The theme a **recomposing** session selected, or null — the state every capture is taken in.
 *
 * `composePreviewRenderAll` renders each `@Preview` with no provider, so the recorded documents are
 * default-themed regardless of what is declared here: this local is only ever non-null when the
 * renderer deliberately wraps a preview in a provider, which it does for a `?themeProvider=` render
 * on a session that can recompose.
 */
internal val LocalRemoteCatalogTheme = compositionLocalOf<String?> { null }

/**
 * [base] with [name]'s roles replaced — the **same** [remoteCatalogThemeColors] map the replay path
 * seeds, read through `RemoteMaterialTheme` instead of the player's named-value channel.
 *
 * Deliberately one definition feeding both lanes. A theme that recomposed to one palette and
 * replayed to another would be worse than either, and nothing would catch it: both renders succeed.
 *
 * A role the map doesn't mention keeps whatever the library's dark-first default supplies, exactly
 * as an unseeded named value does on the replay side.
 */
fun remoteCatalogColorScheme(name: String, base: RemoteColorScheme): RemoteColorScheme {
  val colors = remoteCatalogThemeColors(name)
  if (colors.isEmpty()) return base
  fun role(role: String) = colors["WearM3.$role"]?.let(::RemoteColor)
  return base.copy(
    primary = role("primary") ?: base.primary,
    primaryDim = role("primaryDim") ?: base.primaryDim,
    primaryContainer = role("primaryContainer") ?: base.primaryContainer,
    onPrimary = role("onPrimary") ?: base.onPrimary,
    onPrimaryContainer = role("onPrimaryContainer") ?: base.onPrimaryContainer,
    secondary = role("secondary") ?: base.secondary,
    secondaryDim = role("secondaryDim") ?: base.secondaryDim,
    secondaryContainer = role("secondaryContainer") ?: base.secondaryContainer,
    onSecondary = role("onSecondary") ?: base.onSecondary,
    onSecondaryContainer = role("onSecondaryContainer") ?: base.onSecondaryContainer,
    tertiary = role("tertiary") ?: base.tertiary,
    tertiaryDim = role("tertiaryDim") ?: base.tertiaryDim,
    tertiaryContainer = role("tertiaryContainer") ?: base.tertiaryContainer,
    onTertiary = role("onTertiary") ?: base.onTertiary,
    onTertiaryContainer = role("onTertiaryContainer") ?: base.onTertiaryContainer,
    surfaceContainerLow = role("surfaceContainerLow") ?: base.surfaceContainerLow,
    surfaceContainer = role("surfaceContainer") ?: base.surfaceContainer,
    surfaceContainerHigh = role("surfaceContainerHigh") ?: base.surfaceContainerHigh,
    onSurface = role("onSurface") ?: base.onSurface,
    onSurfaceVariant = role("onSurfaceVariant") ?: base.onSurfaceVariant,
    outline = role("outline") ?: base.outline,
    outlineVariant = role("outlineVariant") ?: base.outlineVariant,
    background = role("background") ?: base.background,
    onBackground = role("onBackground") ?: base.onBackground,
    error = role("error") ?: base.error,
    errorDim = role("errorDim") ?: base.errorDim,
    errorContainer = role("errorContainer") ?: base.errorContainer,
    onError = role("onError") ?: base.onError,
    onErrorContainer = role("onErrorContainer") ?: base.onErrorContainer,
  )
}

/** Publishes [name] to [RemoteSticker], which installs it once inside the remote document. */
@Composable
internal fun RemoteThemeOverride(name: String, content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalRemoteCatalogTheme provides name, content = content)
}

// Each provider declares its own `Wrap` rather than inheriting one from a
// shared base: the renderer resolves the method reflectively **on the concrete class**, so an
// inherited implementation is a `NoSuchMethodException` and every specimen sheet fails to render.
//
// The `name` / `group` pairs are the sibling's, exactly — that is what makes the two Theme selects
// one set. The values each name stands for are in [remoteCatalogThemeColors].

/**
 * Confetti Wear's stock theme: the Wear M3 defaults, unseeded, over its ship typography. The
 * baseline the four conference identities are departures from.
 */
@WearThemeCatalog(name = "Confetti (default)", group = "Confetti")
class RemoteConfettiDefaultThemeCatalog : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    RemoteThemeOverride("Confetti (default)", content)
}

/** KotlinConf: [RemoteKotlinConfSeed] purple with JetBrains Mono titles over an Inter body. */
@WearThemeCatalog(name = "KotlinConf", group = "Confetti")
class RemoteKotlinConfThemeCatalog : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) = RemoteThemeOverride("KotlinConf", content)
}

/** AndroidMakers: [RemoteAndroidMakersSeed] ochre over Confetti's ship typography. */
@WearThemeCatalog(name = "AndroidMakers", group = "Confetti")
class RemoteAndroidMakersThemeCatalog : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) = RemoteThemeOverride("AndroidMakers", content)
}

/** Droidcon: [RemoteDroidconSeed] green over Confetti's ship typography. */
@WearThemeCatalog(name = "Droidcon", group = "Confetti")
class RemoteDroidconThemeCatalog : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) = RemoteThemeOverride("Droidcon", content)
}

/**
 * DevFest: [RemoteDevFestSeed] blue with Google Sans Flex on every role — the only conference
 * identity Confetti swaps typography for, because the face is as much of the GDG brand as the
 * colour.
 */
@WearThemeCatalog(name = "DevFest", group = "Confetti")
class RemoteDevFestThemeCatalog : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) = RemoteThemeOverride("DevFest", content)
}

/**
 * The stock Wear M3 theme with its type scale re-pointed at **Google Sans Flex**, the Material 3
 * Expressive brand face. Palette-identical to [RemoteConfettiDefaultThemeCatalog] on purpose: it
 * isolates the typeface. Not one of Confetti's — hence its own group.
 */
@WearThemeCatalog(name = "Google Sans Flex", group = "Wear M3")
class RemoteGoogleSansFlexThemeCatalog : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) =
    RemoteThemeOverride("Google Sans Flex", content)
}
