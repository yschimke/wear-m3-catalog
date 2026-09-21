package androidx.compose.remote.creation.compose.text

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontVariation

internal actual fun platformVariationSettings(font: Font): FontVariation.Settings =
    FontVariation.Settings()
