package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.compose.layout.RemoteBox
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class CommonCaptureTest {
    @Test
    fun commonCaptureIsDeterministicAndContainsTheComposedLayout() = runBlocking {
        val display = RemoteCreationDisplayInfo(192, 192, 160)
        val empty = captureCommonRemoteDocument(display) {}
        val first = captureCommonRemoteDocument(display) { RemoteBox {} }
        val second = captureCommonRemoteDocument(display) { RemoteBox {} }

        assertContentEquals(first, second)
        assertTrue(first.size > empty.size)
        assertFalse(first.contentEquals(empty))
    }
}
