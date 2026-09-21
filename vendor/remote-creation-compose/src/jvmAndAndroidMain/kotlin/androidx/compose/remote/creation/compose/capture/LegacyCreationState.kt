package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.profile.Profile
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.ui.unit.LayoutDirection

private data class LegacyPlatformState(
    val document: RemoteComposeWriter,
    val profile: Profile,
)

internal var RemoteComposeCreationState.legacyDocument: RemoteComposeWriter
    get() = checkNotNull((platformState as? LegacyPlatformState)?.document)
    set(value) {
        val current = checkNotNull(platformState as? LegacyPlatformState)
        platformState = current.copy(document = value)
    }

internal val RemoteComposeCreationState.platform: RcPlatformServices
    get() = checkNotNull((platformState as? LegacyPlatformState)?.profile?.platform)

internal fun legacyCreationState(
    creationDisplayInfo: RemoteCreationDisplayInfo,
    profile: Profile,
    document: RemoteComposeWriter,
    remoteDensity: RemoteDensity = RemoteDensity.from(creationDisplayInfo),
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    platformImageProvider: PlatformImageProvider,
): RemoteComposeCreationState =
    RemoteComposeCreationState(
        creationDisplayInfo = creationDisplayInfo,
        writer = LegacyRemoteWriterAdapter(document),
        remoteDensity = remoteDensity,
        layoutDirection = layoutDirection,
        supportedOperations = profile.supportedOperations,
        platformImageProvider = platformImageProvider,
        platformState = LegacyPlatformState(document, profile),
    )
