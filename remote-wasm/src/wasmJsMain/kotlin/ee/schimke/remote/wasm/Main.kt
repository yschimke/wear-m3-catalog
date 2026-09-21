package ee.schimke.remote.wasm

import androidx.compose.remote.creation.common.RemoteDocumentWriter

public fun createWasmSample(): ByteArray =
  RemoteDocumentWriter(width = 192, height = 192).run {
    root { column { text("Remote Compose on Desktop") } }
    encodeToByteArray()
  }

public fun main() {
  val output = createWasmSample()
  check(output.size == 149) { "Expected a 149-byte document, got ${output.size}" }
  println("Remote Compose Wasm document: ${output.size} bytes")
}
