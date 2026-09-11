@file:CatalogGroup(name = "Theme", section = "Styles")

package ee.schimke.wearm3catalog.sections

import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Shapes
import androidx.wear.compose.material3.Typography
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.ColorCatalog
import ee.schimke.composeai.preview.ShapeCatalog
import ee.schimke.composeai.preview.TypographyCatalog

// The theme itself, as specimen sheets the renderer builds from the token objects rather than as
// hand-drawn swatches. Each annotation hands over the WHOLE object — every role in the Wear colour
// scheme, every style in the type scale, every corner in the shape set — so a token the library
// adds appears here without an edit, and none of it is transcribed.
//
// These are the kit's `Tokens` and `Styles` pages in code, and they are not `@CatalogComponent`s: a
// token sheet is not a component, and the kit itself draws them as documentation rather than as
// component sets.
//
// Stock constructors, deliberately. `ColorScheme()` / `Typography()` / `Shapes()` with no arguments
// ARE Wear Material 3's defaults, so what publishes is what any app gets before it themes anything
// — the same reasoning the phone catalog uses for its baseline schemes. A hand-typed palette here
// would be a copy that can drift from the library it claims to document.

@ColorCatalog(name = "Wear Material 3", group = "Scheme")
val WearColorScheme: ColorScheme = ColorScheme()

@TypographyCatalog(name = "Wear Material 3", group = "Type")
val WearTypography: Typography = Typography()

@ShapeCatalog(name = "Wear Material 3", group = "Shape") val WearShapes: Shapes = Shapes()
