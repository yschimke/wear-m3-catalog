package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.Typography

/**
 * The `:catalog-cmp` stand-in for [`CatalogFonts.kt`][ee.schimke.wearm3catalog], which is excluded
 * from this module's sources.
 *
 * ## THIS LANE DRAWS THE WRONG TYPEFACES, ON PURPOSE
 *
 * Read that before comparing a theme sticker across the two lanes. `:catalog` resolves Roboto Flex,
 * Inter, JetBrains Mono and Google Sans Flex as **downloadable Google fonts** — an Android
 * `FontsContract` request, intercepted under Robolectric by `ShadowFontsContractCompat` and
 * answered from the render cache. There is no such provider off Android, and this module ships no
 * font bytes, so every role here resolves to [FontFamily.Default]: whatever face the JVM's Skia
 * picks up.
 *
 * So a typography or theme render from this lane differs from the Android one **by design**, and a
 * diff between the two on those stickers says nothing about the port. That is why this is a
 * declared, named substitution rather than a `FontFamily.Default` quietly inherited from a missing
 * dependency: the difference is visible in the source a reader lands on.
 *
 * Closing it means vendoring the four families as Compose Multiplatform resources and loading them
 * per-target — real work with a licensing question attached (three are OFL, Google Sans Flex is
 * not), and out of scope for standing the lane up. docs/CMP_PORT.md tracks it.
 *
 * ## What is faithfully reproduced
 *
 * The SHAPE of the type scale, which is what the rest of the catalog reads: [wearTypography]
 * re-points exactly the roles the Android file re-points — display, title and numeral to one
 * family, body and label to the other, the three curved roles left stock — so a sticker that
 * depends on which roles carry the display face behaves identically here. Only the faces differ.
 */
private val DisplayFace: FontFamily = FontFamily.Default

private val BodyFace: FontFamily = FontFamily.Default

/** Stand-in for the Android file's `RobotoFlex`. See the file KDoc. */
val RobotoFlex: FontFamily = DisplayFace

/** Stand-in for the Android file's `Inter`. See the file KDoc. */
val Inter: FontFamily = BodyFace

/** Stand-in for the Android file's `GoogleSansFlex`. See the file KDoc. */
val GoogleSansFlex: FontFamily = DisplayFace

/** Stand-in for the Android file's `JetBrainsMono`. See the file KDoc. */
val JetBrainsMono: FontFamily = DisplayFace

/** Confetti Wear's ship type scale, in the substitute faces. */
val ExpressiveTypography: Typography = wearTypography(display = RobotoFlex, body = Inter)

/** KotlinConf's type scale, in the substitute faces. */
val KotlinConfTypography: Typography = wearTypography(display = JetBrainsMono, body = Inter)

/** DevFest's single-family pairing, in the substitute faces. */
val GoogleSansFlexTypography: Typography =
  wearTypography(display = GoogleSansFlex, body = GoogleSansFlex)

/**
 * A Wear [Typography] with [body] on the body and label roles and [display] on the display, title
 * and numeral roles.
 *
 * Role-for-role identical to the Android file's private helper, deliberately: the two lanes must
 * disagree about the FACES and about nothing else. `Typography(defaultFontFamily = …)` is not the
 * one-liner for this — it fills in a family only where a style has none, and every stock Wear role
 * already declares one, so a theme built that way renders stock no matter what it declares.
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
