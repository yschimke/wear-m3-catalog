package ee.schimke.wearm3catalog.samples

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.wear.compose.material3.MaterialTheme
import ee.schimke.composeai.preview.WearThemeCatalog

/**
 * The theme every vendored sample is drawn in, declared as a `@WearThemeCatalog` wrapper provider.
 *
 * ## Why this is Kotlin here rather than `themes[]` in the spec
 *
 * `catalog.spec.json` has a `themes[]` block for exactly this — "themes an IMPORTED project has but
 * cannot declare for itself" — and this module used it, because the vendored sources are upstream's
 * bytes and cannot carry an annotation. That was the wrong reading of where the boundary is: the
 * VENDORED TREE cannot be annotated, but this module is ours, and a file outside `upstream/` is
 * first-party code that survives the next import untouched.
 *
 * The distinction stopped being academic when the first publish failed on it.
 * `generate-theme-catalogs.mjs` writes the providers a `themes[]` entry declares, and it always
 * emits the **mobile** `@ThemeCatalog`. The renderer picks its specimen from that annotation —
 * mobile `ThemeSpecimen` for `@ThemeCatalog`, `WearThemeSpecimen` for `@WearThemeCatalog` — so the
 * generated provider drew the specimen with `androidx.compose.material3.MaterialTheme`, which a
 * Wear-only module does not have on its classpath:
 * ```
 * themecatalog__Material.png.error.json
 *   Caused by: java.lang.NoClassDefFoundError: androidx/compose/material3/MaterialTheme
 *     at …PreviewRenderStrategyKt.ThemeSpecimen(PreviewRenderStrategy.kt:672)
 * ```
 *
 * One failed preview fails the whole render, so the sheet published nothing at all.
 *
 * **Crashing was the good outcome.** `:catalog`'s own `CatalogInventoryTest` already names the
 * other one: a Wear provider under the mobile annotation "would report the baseline *mobile* M3
 * palette instead of the theme it declares — silently, with no error and a plausible-looking
 * sheet". A module that carried both Material 3 flavours would have published that lie. This one
 * had nothing to lie with.
 *
 * That the spec's `themes[]` cannot express a Wear theme is an upstream gap and is reported
 * separately; declaring it here is not a workaround for it but the thing a module that CAN annotate
 * should have been doing anyway — the same shape `:catalog` uses, one directory over.
 *
 * ## What it installs
 *
 * The stock Wear M3 palette and type scale, which is the system the samples demonstrate. No seed,
 * no re-skin: unlike `:catalog`'s named themes, these are AndroidX's own call sites, and a sample
 * re-tinted into somebody's conference identity would be showing the theme rather than the API.
 */
@WearThemeCatalog(name = "Material", group = "Wear Material 3")
class SamplesMaterialTheme : PreviewWrapperProvider {
  @Composable override fun Wrap(content: @Composable () -> Unit) = MaterialTheme(content = content)
}
