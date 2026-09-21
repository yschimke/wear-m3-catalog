package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.common.PaintBundleData
import androidx.compose.remote.creation.compose.state.ComposeRemoteColorFilter

internal actual fun PaintBundleData.setComposeColorFilter(filter: ComposeRemoteColorFilter) {
    error("Platform Compose color filters are not serializable on JVM; use RemoteBlendModeColorFilter")
}
