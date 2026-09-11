package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

// The desktop half of the catalog's typefaces.
//
// [RobotoFlex] is the SAME committed variable face the Android lane draws with. The other three
// fall back to a platform family: they are obtained on Android through the GMS
// downloadable-font provider, which is an Android system service with no desktop equivalent, and
// the bytes it serves are not this repository's to vendor. `CatalogTypography.kt` states which of
// the two each family is and why.

/**
 * The vendored variable Roboto Flex, read off the classpath.
 *
 * One file, two lanes: the TTF lives at `catalog/src/androidMain/res/font/roboto_flex.ttf`, where
 * AGP needs it to be `R.font.roboto_flex`, and `catalog/build.gradle.kts` republishes that same
 * file into the desktop compilation's resources. A second copy would be a second thing to bump when
 * the face moves, and byte-identical faces are the only way a cross-lane comparison of a sticker is
 * measuring the COMPONENT rather than the font.
 *
 * A face per token weight with the matching `wght` axis setting, exactly as the Android actual
 * registers them and for the reason [TypeScaleWeights] gives. Compose Desktop resolves the axis on
 * a resource font through the same `FontVariation.Settings` API, so the two lanes ask the face for
 * the same instance rather than one of them silently taking the default.
 */
actual val RobotoFlex: FontFamily =
  FontFamily(
    TypeScaleWeights.map { weight ->
      Font(
        resource = "ee/schimke/wearm3catalog/fonts/roboto_flex.ttf",
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
      )
    }
  )

actual val Inter: FontFamily = FontFamily.SansSerif

actual val GoogleSansFlex: FontFamily = FontFamily.SansSerif

actual val JetBrainsMono: FontFamily = FontFamily.Monospace
