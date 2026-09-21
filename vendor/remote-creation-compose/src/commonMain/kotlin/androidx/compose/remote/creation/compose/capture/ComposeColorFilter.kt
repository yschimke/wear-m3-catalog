package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.common.PaintBundleData
import androidx.compose.remote.creation.compose.state.ComposeRemoteColorFilter

internal expect fun PaintBundleData.setComposeColorFilter(filter: ComposeRemoteColorFilter)
