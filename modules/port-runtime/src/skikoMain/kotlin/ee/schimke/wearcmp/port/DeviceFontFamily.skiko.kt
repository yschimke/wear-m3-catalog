package ee.schimke.wearcmp.port

import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle

/**
 * Three places are asked, in order, and the first that answers wins:
 *
 *  1. [WearFonts] — a font the host registered under this name. This is the one that matters:
 *     Wear's type scale asks for `roboto-flex`, and registering it is what makes the whole scale
 *     render in the typeface it was designed for rather than in a fallback.
 *  2. Skia's own font manager, which is the registry Compose resolves families through, and which
 *     does match by family name — exactly what `DeviceFontFamilyName` does on Android. On a JVM
 *     that means the system fonts; in a browser, whatever Skia has been given.
 *  3. [FontFamily.SansSerif], the honest fallback: the right size and weight in the wrong face.
 *
 * Matching, not loading: this never fetches and never blocks. Resolution is cached, so a font
 * registered after first use will not retroactively apply.
 */
public actual fun deviceFontFamily(name: String): FontFamily =
    cache.getOrPut(name) {
        val typeface =
            WearFonts.typefaceOrNull(name) ?: FontMgr.default.matchFamilyStyle(name, FontStyle.NORMAL)
        if (typeface == null) FontFamily.SansSerif
        else FontFamily(androidx.compose.ui.text.platform.Typeface(typeface)).also {
            typefaces[it] = typeface
        }
    }

private val cache = mutableMapOf<String, FontFamily>()

/**
 * The Skia typeface behind a family [deviceFontFamily] produced, if it is one.
 *
 * Curved text needs the typeface itself — it places glyphs, which Compose's text stack will not do
 * — and cannot get it back out of a `FontFamily`: Compose wraps it in a platform type whose
 * innards are internal, so asking the font resolver returns something that cannot be unwrapped.
 * Remembering the pairing on the way out is the way through, and it is exact: the same object that
 * went in comes back.
 *
 * Port-internal despite being public — `:wear-compose-foundation` is a separate module.
 */
public fun skiaTypefaceForFamily(family: FontFamily?): org.jetbrains.skia.Typeface? =
    typefaces[family]

private val typefaces = mutableMapOf<FontFamily, org.jetbrains.skia.Typeface>()
