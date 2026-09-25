package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.common.RemoteDocumentWriter
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteComposeApplier
import androidx.compose.remote.creation.compose.layout.RemoteRootNode
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Captures one platform-neutral Remote Compose document through the common write-only encoder.
 *
 * @param encodePng how this platform turns an [ImageBitmap] into PNG bytes. Given, every bitmap the
 *   content draws is carried inline in the document; absent, bitmaps are dropped, as they always
 *   were here, because common code has no image encoder of its own.
 */
public suspend fun captureCommonRemoteDocument(
    creationDisplayInfo: RemoteCreationDisplayInfo,
    remoteDensity: RemoteDensity = RemoteDensity.from(creationDisplayInfo),
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    supportedOperations: Set<Int> = emptySet(),
    encodePng: ((ImageBitmap) -> ByteArray?)? = null,
    content: @Composable @RemoteComposable () -> Unit,
): ByteArray =
    withContext(Dispatchers.Default.limitedParallelism(1)) {
        val rootNode = RemoteRootNode()
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(RemoteComposeApplier(rootNode), recomposer)
        val writer =
            RemoteDocumentWriter(
                creationDisplayInfo.size.width.toInt(),
                creationDisplayInfo.size.height.toInt(),
                creationDisplayInfo.densityBehavior.value,
            )
        val creationState =
            RemoteComposeCreationState(
                creationDisplayInfo = creationDisplayInfo,
                writer = writer,
                remoteDensity = remoteDensity,
                layoutDirection = layoutDirection,
                supportedOperations = supportedOperations,
                platformImageProvider =
                    encodePng?.let { InlinePngImageProvider(writer, it) }
                        ?: NoOpPlatformImageProvider,
            )

        try {
            composition.setContent {
                CompositionLocalProvider(
                    LocalRemoteComposeCreationState provides creationState,
                    LocalRemoteDensity provides remoteDensity,
                    LocalDensity provides creationDisplayInfo.density,
                    LocalInspectionMode provides creationDisplayInfo.isInspectionMode,
                    LocalLayoutDirection provides layoutDirection,
                    content = content,
                )
            }
            coroutineScope {
                var frameTime = 0L
                lateinit var frameClock: BroadcastFrameClock
                frameClock =
                    BroadcastFrameClock {
                        launch {
                            frameTime += 16_666_667L
                            frameClock.sendFrame(frameTime)
                        }
                    }
                val runner = launch(frameClock) { recomposer.runRecomposeAndApplyChanges() }
                recomposer.currentState.filter { it == Recomposer.State.Idle }.first()
                recomposer.cancel()
                runner.join()
            }

            Snapshot.withMutableSnapshot {
                val recordingCanvas =
                    CommonRecordingCanvas().apply { setRemoteComposeCreationState(creationState) }
                rootNode.render(creationState, RemoteCanvas(recordingCanvas))
                recordingCanvas.flush()
                writer.encodeToByteArray()
            }
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

/** Bitmaps written inline as PNG, through a platform's own encoder. */
private class InlinePngImageProvider(
    private val writer: RemoteDocumentWriter,
    private val encodePng: (ImageBitmap) -> ByteArray?,
) : PlatformImageProvider {
    override fun addBitmap(image: ImageBitmap): Int =
        encodePng(image)?.let { writer.addBitmapPng(it, image.width, image.height) } ?: -1

    // Named, so a host can still replace the picture through the name the content declared.
    override fun addNamedBitmap(name: String, image: ImageBitmap): Int =
        encodePng(image)?.let { writer.addNamedBitmapPng(name, it, image.width, image.height) }
            ?: -1
}
