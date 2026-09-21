package androidx.compose.remote.creation.common

import kotlin.test.Test
import kotlin.test.assertContentEquals

class RemoteDocumentWriterTest {
  @Test
  fun matchesLegacyDesktopDocument() {
    val document =
      RemoteDocumentWriter(width = 192, height = 192).run {
        root { column { text("Remote Compose on Desktop") } }
        encodeToByteArray()
      }

    assertContentEquals(ExpectedDocument, document)
  }

  private companion object {
    val ExpectedDocument =
      """
      00 04 8c 00 01 00 00 00 01 00 00 00 00 00 00 00
      05 00 05 00 04 00 00 00 c0 00 06 00 04 00 00 00
      c0 0c 09 00 04 00 00 00 00 00 0e 00 04 00 00 00
      00 00 1b 00 04 00 00 00 02 c8 ff ff ff fe cc ff
      ff ff fd ff ff ff ff 00 00 00 01 00 00 00 04 00
      00 00 00 c9 ff ff ff fc 66 00 00 00 2a 00 00 00
      19 52 65 6d 6f 74 65 20 43 6f 6d 70 6f 73 65 20
      6f 6e 20 44 65 73 6b 74 6f 70 ef 00 00 00 2a 00
      02 01 ff ff ff fb 05 41 40 00 00 c9 ff ff ff fa
      d6 d6 d6 d6 d6
      """.trimIndent().split(Regex("\\s+")).map { it.toInt(16).toByte() }.toByteArray()
  }
}
