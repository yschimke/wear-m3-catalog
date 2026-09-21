package androidx.compose.remote.creation.compose.state

import androidx.compose.remote.core.RemoteComposeBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals

class AnimationEncodingTest {
    @Test
    fun matchesPublishedCoreEncoding() {
        val cases =
            listOf(
                Animation(1f, 1, null, Float.NaN, Float.NaN),
                Animation(0.5f, 1, null, Float.NaN, Float.NaN),
                Animation(2f, 3, floatArrayOf(0.2f, 0.8f), Float.NaN, Float.NaN),
                Animation(1f, 1, null, 4f, Float.NaN),
                Animation(1f, 1, null, Float.NaN, 360f),
                Animation(3f, 2, floatArrayOf(1f, 2f, 3f), 5f, 360f),
            )

        cases.forEach { case ->
            assertContentEquals(
                RemoteComposeBuffer.packAnimation(
                    case.duration,
                    case.type,
                    case.specification,
                    case.initialValue,
                    case.wrap,
                ),
                packAnimation(
                    case.duration,
                    case.type,
                    case.specification,
                    case.initialValue,
                    case.wrap,
                ),
            )
        }
    }

    private data class Animation(
        val duration: Float,
        val type: Int,
        val specification: FloatArray?,
        val initialValue: Float,
        val wrap: Float,
    )
}
