/*
 * Copyright 2026 Yuri Schimke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteComposeApplier
import androidx.compose.remote.creation.compose.layout.RemoteRootNode
import androidx.compose.remote.creation.profile.Profile
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Captures one Remote Compose document without Android framework services. */
public suspend fun captureSingleRemoteDocument(
  creationDisplayInfo: RemoteCreationDisplayInfo,
  remoteDensity: RemoteDensity = RemoteDensity.from(creationDisplayInfo),
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
  profile: Profile = desktopProfile(),
  content: @Composable @RemoteComposable () -> Unit,
): ByteArray =
  withContext(Dispatchers.Default.limitedParallelism(1)) {
    val rootNode = RemoteRootNode()
    val recomposer = Recomposer(coroutineContext)
    val composition = Composition(RemoteComposeApplier(rootNode), recomposer)
    val imageProvider =
      object : PlatformImageProvider {
        override fun addBitmap(image: androidx.compose.ui.graphics.ImageBitmap): Int = -1

        override fun addNamedBitmap(
          name: String,
          image: androidx.compose.ui.graphics.ImageBitmap,
        ): Int = -1
      }
    val document = profile.create(creationDisplayInfo.toCreationDisplayInfo(), null)
    val creationState =
      legacyCreationState(
        creationDisplayInfo,
        profile,
        document,
        remoteDensity,
        layoutDirection,
        imageProvider,
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
        lateinit var frameClock: BroadcastFrameClock
        frameClock = BroadcastFrameClock { launch { frameClock.sendFrame(System.nanoTime()) } }
        val runner = launch(frameClock) { recomposer.runRecomposeAndApplyChanges() }
        recomposer.currentState.filter { it == Recomposer.State.Idle }.first()
        recomposer.cancel()
        runner.join()
      }

      Snapshot.withMutableSnapshot {
        val recordingCanvas = JvmRecordingCanvas().apply { setRemoteComposeCreationState(creationState) }
        rootNode.render(creationState, RemoteCanvas(recordingCanvas))
        recordingCanvas.flush()
        creationState.legacyDocument.encodeToByteArray()
      }
    } finally {
      composition.dispose()
      recomposer.cancel()
    }
  }

private fun desktopProfile(): Profile =
  Profile(
    CoreDocument.DOCUMENT_API_LEVEL,
    0,
    RcPlatformServices.None,
  ) { displayInfo, profile, callback ->
    RemoteComposeWriter(displayInfo, null, profile, callback)
  }
