package ee.schimke.wearm3catalog.uibuilder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** The renderer's actual Wasm registry must serve every adapter in the published policy. */
class PublishedRuntimeAdapterCoverageTest {
  @Test
  fun `every published canvas adapter is registered at runtime`() {
    val policy = Json.parseToJsonElement(catalogUiBuilderPolicyJson).jsonObject
    val declared =
      listOf("builtins", "components").flatMap { section ->
        policy.getValue(section).jsonObject.values.mapNotNull { entry ->
          entry.jsonObject["canvas"]?.jsonPrimitive?.contentOrNull
        }
      }
    assertTrue(declared.size >= 20, "the published Wear policy lost its adapters")
    assertEquals(emptySet(), declared.filterNot { runtimeAdapters[it] != null }.toSet())
    assertTrue(runtimeAdapters["wear-m3/date-picker"] != null)
    assertTrue(runtimeAdapters["wear-m3/time-picker"] != null)
  }
}
