package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.RemoteComposeBuffer
import androidx.compose.remote.core.operations.DrawRect
import androidx.compose.remote.core.operations.PaintData
import androidx.compose.remote.core.operations.PathData
import androidx.compose.remote.core.operations.layout.CanvasContent
import androidx.compose.remote.core.operations.layout.LayoutComponentContent
import androidx.compose.remote.creation.RemotePath
import androidx.compose.remote.creation.common.Utils
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.RemotePaint
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Every layout content section starts with a full paint, from both writers.
 *
 * Players give each component a fresh paint, while the writer's paint tracker only sends fields
 * that changed. So the writer has to forget what it sent at every content start, as
 * `RemoteComposeWriter.addContentStart()` does. Without that, the second canvas below never
 * receives its colour and draws with the player's default.
 */
class WriterOperationsTest {
    private val display = RemoteCreationDisplayInfo(192, 192, 160)

    @Test
    fun remoteDocumentWriterSendsAFullPaintInEveryContentSection() = runBlocking {
        assertEveryDrawIsColoured(captureCommonRemoteDocument(display) { TwoCanvasesOfOneColour() })
    }

    @Test
    fun legacyWriterAdapterSendsAFullPaintInEveryContentSection() = runBlocking {
        assertEveryDrawIsColoured(captureSingleRemoteDocument(display) { TwoCanvasesOfOneColour() })
    }

    /** One path drawn twice is declared once, as `RemoteComposeWriter`'s `cacheData` does. */
    @Test
    fun bothWritersDeclareARepeatedPathOnce() = runBlocking {
        for (document in
            listOf(
                captureCommonRemoteDocument(display) { OnePathTwice() },
                captureSingleRemoteDocument(display) { OnePathTwice() },
            )) {
            assertEquals(1, operations(document).count { it is PathData })
        }
    }

    /**
     * Paths that differ only in which variable a coordinate reads are two paths. A variable
     * reference is a NaN payload, so a cache that compares floats by value (every NaN equal) would
     * declare the second as the first.
     */
    @Test
    fun bothWritersKeepPathsThatDifferOnlyInAVariable() = runBlocking {
        for (document in
            listOf(
                captureCommonRemoteDocument(display) { TwoPathsOneShape() },
                captureSingleRemoteDocument(display) { TwoPathsOneShape() },
            )) {
            assertEquals(2, operations(document).count { it is PathData })
        }
    }

    @Composable
    private fun TwoPathsOneShape() {
        RemoteCanvas(RemoteModifier.size(10.rdp)) {
            for (variable in listOf(42, 43)) {
                val path = RemotePath().apply { moveTo(Utils.asNan(variable), 0f) }
                drawPath(path, RemotePaint { color = Color.Red.rc })
            }
        }
    }

    @Composable
    private fun OnePathTwice() {
        RemoteCanvas(RemoteModifier.size(10.rdp)) {
            repeat(2) {
                val path = RemotePath().apply { moveTo(0f, 0f); lineTo(10f, 10f) }
                drawPath(path, RemotePaint { color = Color.Red.rc })
            }
        }
    }

    private fun operations(document: ByteArray): List<Operation> =
        ArrayList<Operation>().also {
            RemoteComposeBuffer.fromInputStream(document.inputStream()).inflateFromBuffer(it)
        }

    @Composable
    private fun TwoCanvasesOfOneColour() {
        RemoteColumn {
            repeat(2) {
                RemoteCanvas(RemoteModifier.size(10.rdp)) {
                    drawRect(RemotePaint { color = Color.Red.rc })
                }
            }
        }
    }

    private fun assertEveryDrawIsColoured(document: ByteArray) {
        var coloured = false
        var draws = 0
        for (operation in operations(document)) {
            when (operation) {
                is LayoutComponentContent,
                is CanvasContent -> coloured = false
                is PaintData -> coloured = coloured || "Color(" in operation.toString()
                is DrawRect -> {
                    draws++
                    assertTrue(coloured, "draw #$draws has no colour in its content section")
                }
            }
        }
        assertEquals(2, draws)
    }
}
