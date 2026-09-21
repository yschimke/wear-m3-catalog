package ee.schimke.remote.wasm

import androidx.compose.remote.creation.compose.action.combinedAction
import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.captureCommonRemoteDocument
import androidx.compose.remote.creation.compose.state.rs
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.wasm.awaitSkiko

/** Builds a real Remote Material 3 component tree and encodes it entirely in Wasm. */
public suspend fun createWasmMaterialSample(): ByteArray {
  awaitSkikoReady()
  return captureCommonRemoteDocument(RemoteCreationDisplayInfo(192, 192, 160)) {
    RemoteMaterialTheme {
      RemoteButton(onClick = combinedAction()) { RemoteText("Remote Material 3".rs) }
    }
  }
}

@OptIn(InternalSkikoApi::class)
private suspend fun awaitSkikoReady(): Unit = suspendCoroutine { continuation ->
  awaitSkiko.then(
    onFulfilled = {
      continuation.resume(Unit)
      null
    },
    onRejected = {
      continuation.resumeWithException(IllegalStateException("Skiko initialization failed: $it"))
      null
    },
  )
}

public fun main() {
  MainScope().launch {
    val output = createWasmMaterialSample()
    check(output.size > 149) { "Expected composed Material components, got ${output.size} bytes" }
    println("Remote Material 3 Wasm document: ${output.size} bytes")
  }
}
