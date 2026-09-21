package ee.schimke.remote.wasm

import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.captureCommonRemoteDocument
import androidx.compose.remote.creation.compose.state.rs
import androidx.wear.compose.remote.material3.RemoteText
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/** Builds a real Remote Material 3 component tree and encodes it entirely in Wasm. */
public suspend fun createWasmMaterialSample(): ByteArray =
  captureCommonRemoteDocument(RemoteCreationDisplayInfo(192, 192, 160)) {
    RemoteText("Remote Material 3".rs)
    RemoteText("Rendered in Wasm".rs)
  }

public fun main() {
  MainScope().launch {
    val output = createWasmMaterialSample()
    check(output.size > 149) { "Expected composed Material components, got ${output.size} bytes" }
    println("Remote Material 3 Wasm document: ${output.size} bytes")
  }
}
