package ee.schimke.wearcmp.port

import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle

/**
 * Skia's font manager is the same registry Compose resolves families through, and it will match by
 * family name — which is exactly what `DeviceFontFamilyName` does on Android. On the web the
 * registry holds whatever fonts the page has loaded, so a host that wants Roboto Flex loads it as
 * a web font under that name and everything else follows.
 *
 * Matching, not loading: this never fetches. A missing font falls back rather than blocking.
 */
public actual fun deviceFontFamily(name: String): FontFamily {
    val typeface = FontMgr.default.matchFamilyStyle(name, FontStyle.NORMAL) ?: return FontFamily.SansSerif
    return FontFamily(androidx.compose.ui.text.platform.Typeface(typeface))
}
