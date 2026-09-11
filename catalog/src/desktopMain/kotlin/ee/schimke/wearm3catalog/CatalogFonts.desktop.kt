package ee.schimke.wearm3catalog

import androidx.compose.ui.text.font.FontFamily

// The desktop half of the catalog's typefaces.
//
// Every family falls back to a platform default. The Android faces are obtained through the GMS
// downloadable-font provider and a vendored variable Roboto Flex under `res/font` — an Android
// resource and an Android system service, neither of which exists here.
//
// This is deliberately a FALLBACK and not an attempt to reproduce the brand faces. The published
// kit rendition is the Robolectric one, drawn with the real faces; what a desktop compilation is
// for today is proving the component bodies are genuinely multiplatform. A desktop render shows
// the type SCALE — every size, line height, tracking and per-role variation setting the library
// sets, preserved by `wearTypography` — in the platform's own face, which is a true statement about
// what it is. Bundling the OFL faces here would make it a truer picture and is a separate change.

actual val RobotoFlex: FontFamily = FontFamily.Default

actual val Inter: FontFamily = FontFamily.SansSerif

actual val GoogleSansFlex: FontFamily = FontFamily.SansSerif

actual val JetBrainsMono: FontFamily = FontFamily.Monospace
