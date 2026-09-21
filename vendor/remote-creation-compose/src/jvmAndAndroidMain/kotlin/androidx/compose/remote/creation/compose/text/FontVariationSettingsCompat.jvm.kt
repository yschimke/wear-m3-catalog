package androidx.compose.remote.creation.compose.text

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontVariation

internal actual fun platformVariationSettings(font: Font): FontVariation.Settings {
    val getter = font.javaClass.methods.firstOrNull { it.name == "getVariationSettings" }
    return (getter?.invoke(font) as? FontVariation.Settings) ?: FontVariation.Settings()
}
