package ee.schimke.remote.desktop

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.common.RemoteDocumentWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class DesktopCaptureTest {
  @Test
  fun capturesDeterministicRemoteDocumentWithoutAndroid() = runBlocking {
    val first = createDesktopSample()
    val second = createDesktopSample()

    assertTrue(first.isNotEmpty())
    assertContentEquals(first, second)
  }

  @Test
  fun usesSingleWriteCoreWithoutJsonImporter() {
    assertEquals(
      CoreDocument::class.java.protectionDomain.codeSource.location,
      RemoteComposeWriter::class.java.protectionDomain.codeSource.location,
    )
    assertFailsWith<ClassNotFoundException> {
      Class.forName("androidx.compose.remote.creation.json.RemoteComposeJsonParser")
    }
  }

  @Test
  fun commonWriterMatchesComposeCapture() = runBlocking {
    val common =
      RemoteDocumentWriter(width = 192, height = 192).run {
        root {
          column(componentId = -1_000_000) {
            text("Remote Compose on Desktop", componentId = -1_000_001)
          }
        }
        encodeToByteArray()
      }

    assertContentEquals(createDesktopSample(), common)
  }
}
