package androidx.compose.remote.core

import androidx.compose.remote.core.operations.FloatConstant
import androidx.compose.remote.core.operations.IncludeReferencedOperations
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.layout.modifiers.ScrollModifierOperation
import androidx.compose.remote.core.operations.loom.PatternArgument
import androidx.compose.remote.core.operations.loom.PatternBlock
import androidx.compose.remote.core.operations.loom.PatternForEach
import androidx.compose.remote.core.operations.loom.PatternInflation
import androidx.compose.remote.core.types.IntegerConstant
import androidx.compose.remote.core.types.LongConstant
import kotlin.test.Test
import kotlin.test.assertContentEquals

class WriteOperationsTest {
  @Test
  fun codecsMatchUpstreamOperationWriters() {
    assertEncoding({ TextData.apply(it, 42, "Remote") }) {
      WriteOperations.textData(it, 42, "Remote")
    }
    assertEncoding({ IntegerConstant.apply(it, 43, -17) }) {
      WriteOperations.integerConstant(it, 43, -17)
    }
    assertEncoding({ FloatConstant.apply(it, 44, 1.25f) }) {
      WriteOperations.floatConstant(it, 44, 1.25f)
    }
    assertEncoding({ LongConstant.apply(it, 45, Long.MAX_VALUE) }) {
      WriteOperations.longConstant(it, 45, Long.MAX_VALUE)
    }
    assertEncoding({ IncludeReferencedOperations.apply(it, 46) }) {
      WriteOperations.includeReferencedOperations(it, 46)
    }
    assertEncoding({ PatternInflation.apply(it, 47, intArrayOf(1, 2, 3)) }) {
      WriteOperations.patternInflation(it, 47, intArrayOf(1, 2, 3))
    }
    assertEncoding({ PatternBlock.apply(it, 2) }) { WriteOperations.patternBlock(it, 2) }
    assertEncoding({ PatternArgument.apply(it, 3) }) { WriteOperations.patternArgument(it, 3) }
    assertEncoding({ PatternForEach.apply(it, 48, 49) }) {
      WriteOperations.patternForEach(it, 48, 49)
    }
    assertEncoding({ ScrollModifierOperation.apply(it, 1, 2.5f, 20f, 5f) }) {
      WriteOperations.scrollModifier(it, 1, 2.5f, 20f, 5f)
    }
  }

  private fun assertEncoding(upstream: (WireBuffer) -> Unit, extracted: (WireBuffer) -> Unit) {
    val upstreamBuffer = WireBuffer()
    val extractedBuffer = WireBuffer()

    upstream(upstreamBuffer)
    extracted(extractedBuffer)

    assertContentEquals(
      upstreamBuffer.buffer.copyOf(upstreamBuffer.size),
      extractedBuffer.buffer.copyOf(extractedBuffer.size),
    )
  }
}
