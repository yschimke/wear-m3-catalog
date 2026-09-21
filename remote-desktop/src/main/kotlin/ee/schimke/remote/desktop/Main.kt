package ee.schimke.remote.desktop

import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.RemoteDensityBehavior
import androidx.compose.remote.creation.compose.capture.captureSingleRemoteDocument
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.state.rs
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.runBlocking

internal suspend fun createDesktopSample(): ByteArray =
  captureSingleRemoteDocument(
    creationDisplayInfo =
      RemoteCreationDisplayInfo(
        width = 192,
        height = 192,
        densityDpi = 160,
        densityBehavior = RemoteDensityBehavior.Dp,
      )
  ) {
    RemoteColumn { RemoteText("Remote Compose on Desktop".rs) }
  }

fun main(args: Array<String>) = runBlocking {
  val document = createDesktopSample()
  check(document.isNotEmpty()) { "Desktop capture produced an empty Remote Compose document" }
  val output = Path.of(args.firstOrNull() ?: "build/desktop-sample.rc")
  output.parent?.let(Files::createDirectories)
  Files.write(output, document)
  println("Wrote ${document.size} bytes to ${output.toAbsolutePath()}")
}
