package ee.schimke.wearcmp.port

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import org.jetbrains.skia.FontVariation as SkiaFontVariation
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

public actual fun FontFamily.withFontVariation(
    variationSettings: FontVariation.Settings
): FontFamily {
    // A `Setting` that needs a density is one written in `sp`. The type scale uses none, and
    // resolving one against a density it was not given would be worse than dropping it.
    val axes =
        variationSettings.settings
            .filterNot { it.needsDensity }
            .map { SkiaFontVariation(it.axisName, it.toVariationValue(null)) }
    // Nothing to apply, or a family that came from somewhere other than `deviceFontFamily` — the
    // typeface is not ours to clone, so hand back what we were given.
    val base = skiaTypefaceForFamily(this)
    if (axes.isEmpty() || base == null) return this

    // Keyed by the axes as well as the typeface: two styles of one family at different weights are
    // two different typefaces, and sharing a cache entry would give whichever asked first.
    val key = axes.joinToString(",", prefix = "${base.uniqueId}@") { "${it.tag}=${it.value}" }
    return variationCache.getOrPut(key) {
        // An axis the font does not declare is ignored rather than rejected, so asking for `wdth`
        // on a font with no width axis is safe.
        val varied = base.makeClone(axes.toTypedArray())
        FontFamily(androidx.compose.ui.text.platform.Typeface(varied)).also {
            typefaces[it] = varied
        }
    }
}

private val variationCache = mutableMapOf<String, FontFamily>()

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
