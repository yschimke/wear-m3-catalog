package androidx.wear.compose.remote.material3

import androidx.compose.remote.creation.compose.capture.LocalRemoteComposeCreationState
import androidx.compose.remote.creation.compose.capture.heightDp
import androidx.compose.runtime.Composable

@Composable
internal actual fun currentScreenHeightDp(): Float =
    LocalRemoteComposeCreationState.current.creationDisplayInfo.heightDp.value
