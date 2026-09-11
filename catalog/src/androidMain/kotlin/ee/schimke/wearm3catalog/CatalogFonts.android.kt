package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

// The Android half of the catalog's typefaces: how a family is OBTAINED. The type scales built
// from them are common — see `CatalogTypography.kt`, which is also where the reasoning behind the
// per-weight registration lives.

/**
 * A vendored variable face, registered once per weight the Wear type scale names.
 *
 * `resId` rather than a downloadable `GoogleFont`: see [TypeScaleWeights] for why every family
 * here is vendored, which is the same reason Roboto Flex always was.
 */
private fun vendored(resId: Int): FontFamily =
  FontFamily(
    TypeScaleWeights.map { weight ->
      Font(
        resId = resId,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
      )
    }
  )

actual val RobotoFlex: FontFamily = vendored(R.font.roboto_flex)

actual val Inter: FontFamily = vendored(R.font.inter)

actual val GoogleSansFlex: FontFamily = vendored(R.font.google_sans_flex)

actual val JetBrainsMono: FontFamily = vendored(R.font.jetbrains_mono)
