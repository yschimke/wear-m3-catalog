package androidx.compose.remote.creation.compose.capture

import android.graphics.BlendModeColorFilter
import androidx.compose.remote.creation.common.PaintBundleData
import androidx.compose.remote.creation.compose.layout.toComposeBlendMode
import androidx.compose.remote.creation.compose.layout.toInt
import androidx.compose.remote.creation.compose.state.ComposeRemoteColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter

internal actual fun PaintBundleData.setComposeColorFilter(filter: ComposeRemoteColorFilter) {
    val native = filter.composeColorFilter.asAndroidColorFilter()
    require(native is BlendModeColorFilter) { "Native color filter not supported: $native" }
    setColorFilter(native.color, native.mode.toComposeBlendMode().toInt())
}
