package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.capture.RemoteImageVector
import androidx.compose.remote.creation.compose.capture.toRemoteImageVector
import androidx.compose.ui.graphics.vector.ImageVector

/** The current snapshot requires Remote Compose's captured vector representation. */
internal fun ImageVector.asCatalogRemoteIcon(): RemoteImageVector = toRemoteImageVector()
