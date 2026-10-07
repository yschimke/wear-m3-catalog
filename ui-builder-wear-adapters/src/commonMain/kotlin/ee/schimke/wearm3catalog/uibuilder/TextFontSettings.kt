package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSynthesis
import ee.schimke.composeai.uibuilder.LocalUiBuilderFontFamilies
import ee.schimke.composeai.uibuilder.LocalUiBuilderFontRegistry
import ee.schimke.composeai.uibuilder.LocalUiBuilderFontVariants
import ee.schimke.composeai.uibuilder.export.FontSettings
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope

/** A text's style with its [FontSettings] applied, and whether `wght` now decides its weight. */
class FontSettingsStyle(val style: TextStyle, val setsWeight: Boolean)

/**
 * [style] with this text node's `fontFeatureSettings` and `fontVariationSettings` applied, as the
 * editor's canvas applies them (compose-ui-builder's `resolveFontSettings`).
 *
 * Features go on the style. Axes need the font file, so they become an instance of the family
 * [style] is already set in — a theme typeface the runtime's registry loaded, found by the family
 * it handed out — or of Wear's own Roboto Flex when the role keeps the device face. A face that is
 * not loaded draws the features alone, and a face without a `wght` axis keeps the authored
 * `fontWeight` ([FontSettingsStyle.setsWeight] stays false).
 */
@Composable
fun CanvasNodeScope.withFontSettings(style: TextStyle): FontSettingsStyle {
  val features =
    FontSettings.formatFeatures(FontSettings.parseFeatures(string(FontSettings.FEATURE_PROPERTY)))
      .ifEmpty { null }
  val axes = FontSettings.parseVariations(string(FontSettings.VARIATION_PROPERTY))
  val withFeatures = if (features == null) style else style.copy(fontFeatureSettings = features)
  if (axes.isEmpty()) return FontSettingsStyle(withFeatures, setsWeight = false)
  val families = LocalUiBuilderFontFamilies.current
  val family =
    families.entries.firstOrNull { it.value == style.fontFamily }?.key
      ?: FontSettings.WEAR_DEFAULT_FAMILY
  val registry = LocalUiBuilderFontRegistry.current
  LaunchedEffect(registry, family) { registry?.request(family) }
  val variants = LocalUiBuilderFontVariants.current
  // Read through the families first, which is the registry's snapshot map: the text recomposes in
  // the instance once the family arrives.
  val instance =
    family.takeIf { families[it] != null }?.let { variants?.variant(it, axes) }
      ?: return FontSettingsStyle(withFeatures, setsWeight = false)
  val weightAxis = axes.any { it.tag == "wght" } && variants?.hasAxis(family, "wght") == true
  return FontSettingsStyle(
    withFeatures.copy(
      fontFamily = instance,
      // The axis is the weight; a synthesised bold over `wght` 800 would be two bolds.
      fontSynthesis = if (weightAxis) FontSynthesis.None else withFeatures.fontSynthesis,
    ),
    setsWeight = weightAxis,
  )
}
