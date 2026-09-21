package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.BaseRemoteState
import androidx.compose.remote.creation.compose.state.RemoteStateInstanceKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RemoteDocumentProgramTest {
    @Test
    fun optimizesBeforeWriting() {
        val creationState = NoRemoteCompose()
        val program = RemoteDocumentProgram(enableOptimizations = true)
        var writes = 0

        program.recordRenderingOp(
            DocumentOp.Transform(DocumentTransform.Translate(0f.rf, 0f.rf))
        )
        program.recordRenderingOp(DocumentOp.Draw { writes++ })

        program.optimize(creationState)

        assertEquals(0, writes)
        assertFalse("Transform" in program.toString())

        program.writeTo(creationState.writer, creationState)

        assertEquals(1, writes)
        assertEquals("RemoteDocumentProgram(empty)", program.toString())
    }

    @Test
    fun declarationsAreWrittenBeforeTheBody() {
        val creationState = NoRemoteCompose()
        val program = RemoteDocumentProgram()
        val writes = mutableListOf<String>()

        program.recordRenderingOp(DocumentOp.Draw { writes += "body" })
        program.recordDeclaration { writes += "declaration" }

        program.optimize(creationState)
        program.writeTo(creationState.writer, creationState)

        assertEquals(listOf("declaration", "body"), writes)
    }

    @Test
    fun globalStateIsDrainedIntoTheDeclarationPreamble() {
        val creationState = NoRemoteCompose()
        val program = RemoteDocumentProgram()
        val writes = mutableListOf<String>()
        val state =
            object : BaseRemoteState<Int>(RemoteStateInstanceKey()) {
                override val constantValueOrNull: Int? = null

                override fun writeToDocument(creationState: RemoteComposeCreationContext): Int {
                    writes += "declaration"
                    return 42
                }
            }

        creationState.enqueueGlobalDeclaration(state)
        program.recordRenderingOp(DocumentOp.Draw { writes += "body" })
        program.flush(creationState)

        assertEquals(listOf("declaration", "body"), writes)
    }
}
