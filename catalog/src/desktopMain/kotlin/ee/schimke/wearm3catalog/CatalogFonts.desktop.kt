package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

// The desktop half of the catalog's typefaces: the SAME four committed variable faces the Android
// lane draws with, read off the classpath instead of out of `res/font`. The type scales built from
// them are common — see `CatalogTypography.kt`.

/**
 * A vendored variable face, registered once per weight the Wear type scale names.
 *
 * One file, two lanes: each TTF lives under `catalog/src/androidMain/res/font/`, where AGP needs it
 * to become an `R.font` id, and `catalog/build.gradle.kts` republishes that directory into the
 * desktop compilation's resources. A second copy would be a second thing to bump when a face moves,
 * and byte-identical faces are the only way a cross-lane comparison of a sticker is measuring the
 * COMPONENT rather than the font.
 *
 * Compose Desktop resolves `FontVariation.Settings` on a resource font through the same API the
 * Android actual uses, so both lanes ask the face for the same instance rather than one of them
 * silently taking the default.
 */
private fun vendored(file: String): FontFamily =
  FontFamily(
    TypeScaleWeights.map { weight ->
      Font(
        resource = "ee/schimke/wearm3catalog/fonts/$file",
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
      )
    }
  )

actual val RobotoFlex: FontFamily = vendored("roboto_flex.ttf")

actual val Inter: FontFamily = vendored("inter.ttf")

actual val GoogleSansFlex: FontFamily = vendored("google_sans_flex.ttf")

actual val JetBrainsMono: FontFamily = vendored("jetbrains_mono.ttf")
