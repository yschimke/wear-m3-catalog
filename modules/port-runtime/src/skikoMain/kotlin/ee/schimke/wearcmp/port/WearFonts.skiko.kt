package ee.schimke.wearcmp.port

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface

public actual object WearFonts {
    public actual val RobotoFlex: String = "roboto-flex"

    private val registered = mutableMapOf<String, Typeface>()

    public actual fun register(familyName: String, data: ByteArray) {
        // Skia parses the font here rather than at first draw, so a file that is not a font fails
        // at the call that supplied it instead of silently rendering in a fallback face.
        val typeface =
            FontMgr.default.makeFromData(Data.makeFromBytes(data))
                ?: error("$familyName is not a font Skia can read (${data.size} bytes)")
        registered[familyName] = typeface
    }

    public actual fun isRegistered(familyName: String): Boolean = familyName in registered

    internal fun typefaceOrNull(familyName: String): Typeface? = registered[familyName]
}
