package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.Typography

/**
 * This Wear scale with each role in [families] re-pointed at its family, by role name.
 *
 * Explicitly per role, for the reason `CatalogTypography.wearTypography` gives:
 * `Typography(defaultFontFamily = …)` is a no-op on Wear, because every stock role already names a
 * family. The arc roles keep the stock face — curved system chrome, not the design's type.
 */
internal fun Typography.withRoleFamilies(families: Map<String, FontFamily>): Typography {
  if (families.isEmpty()) return this
  fun f(role: String) = families[role]
  return copy(
    displayLarge = f("displayLarge")?.let { displayLarge.copy(fontFamily = it) } ?: displayLarge,
    displayMedium =
      f("displayMedium")?.let { displayMedium.copy(fontFamily = it) } ?: displayMedium,
    displaySmall = f("displaySmall")?.let { displaySmall.copy(fontFamily = it) } ?: displaySmall,
    titleLarge = f("titleLarge")?.let { titleLarge.copy(fontFamily = it) } ?: titleLarge,
    titleMedium = f("titleMedium")?.let { titleMedium.copy(fontFamily = it) } ?: titleMedium,
    titleSmall = f("titleSmall")?.let { titleSmall.copy(fontFamily = it) } ?: titleSmall,
    labelLarge = f("labelLarge")?.let { labelLarge.copy(fontFamily = it) } ?: labelLarge,
    labelMedium = f("labelMedium")?.let { labelMedium.copy(fontFamily = it) } ?: labelMedium,
    labelSmall = f("labelSmall")?.let { labelSmall.copy(fontFamily = it) } ?: labelSmall,
    bodyLarge = f("bodyLarge")?.let { bodyLarge.copy(fontFamily = it) } ?: bodyLarge,
    bodyMedium = f("bodyMedium")?.let { bodyMedium.copy(fontFamily = it) } ?: bodyMedium,
    bodySmall = f("bodySmall")?.let { bodySmall.copy(fontFamily = it) } ?: bodySmall,
    bodyExtraSmall =
      f("bodyExtraSmall")?.let { bodyExtraSmall.copy(fontFamily = it) } ?: bodyExtraSmall,
    numeralExtraLarge =
      f("numeralExtraLarge")?.let { numeralExtraLarge.copy(fontFamily = it) } ?: numeralExtraLarge,
    numeralLarge = f("numeralLarge")?.let { numeralLarge.copy(fontFamily = it) } ?: numeralLarge,
    numeralMedium =
      f("numeralMedium")?.let { numeralMedium.copy(fontFamily = it) } ?: numeralMedium,
    numeralSmall = f("numeralSmall")?.let { numeralSmall.copy(fontFamily = it) } ?: numeralSmall,
    numeralExtraSmall =
      f("numeralExtraSmall")?.let { numeralExtraSmall.copy(fontFamily = it) } ?: numeralExtraSmall,
  )
}
