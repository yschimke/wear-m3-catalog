package androidx.compose.remote.creation

import androidx.compose.remote.creation.common.Utils
import kotlin.test.Test
import kotlin.test.assertContentEquals

class RemotePathTest {
    @Test
    fun pathUsesProtocolCompatibleEncoding() {
        val path = RemotePath()
        path.moveTo(1f, 2f)
        path.lineTo(3f, 4f)
        path.quadTo(5f, 6f, 7f, 8f)
        path.cubicTo(9f, 10f, 11f, 12f, 13f, 14f)
        path.close()

        assertContentEquals(
            floatArrayOf(
                Utils.asNan(10),
                1f,
                2f,
                Utils.asNan(11),
                0f,
                0f,
                3f,
                4f,
                Utils.asNan(12),
                0f,
                0f,
                5f,
                6f,
                7f,
                8f,
                Utils.asNan(14),
                0f,
                0f,
                9f,
                10f,
                11f,
                12f,
                13f,
                14f,
                Utils.asNan(15),
            ),
            path.toFloatArray(),
        )
    }
}
