package ee.schimke.wearm3catalog.uibuilder

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** The published Wear policy must name adapters this catalog ships, including its Wasm pickers. */
class WearPublishedPolicyAdapterCoverageTest {
  private val policy =
    Json.parseToJsonElement(
        generateSequence(File(".").absoluteFile) { it.parentFile }
          .map { File(it, "ui-builder.policy.json") }
          .first { it.isFile }
          .readText()
      )
      .jsonObject

  @Test
  fun `every published canvas adapter has an implementation`() {
    val declared =
      listOf("builtins", "components").flatMap { section ->
        policy.getValue(section).jsonObject.mapNotNull { (_, value) ->
          val entry = value.jsonObject
          if ("excluded" in entry) null else entry["canvas"]?.jsonPrimitive?.contentOrNull
        }
      }
    assertTrue(declared.size >= 20, "the published Wear policy unexpectedly lost its adapters")

    val jvm = wearCanvasAdapters + wearScreenAdapters + wearTextAdapters
    val pickerSource =
      generateSequence(File(".").absoluteFile) { it.parentFile }
        .map {
          File(
            it,
            "ui-builder-wear-adapters/src/wasmJsMain/kotlin/ee/schimke/wearm3catalog/uibuilder/WearPickerAdapters.kt",
          )
        }
        .first { it.isFile }
        .readText()
    val pickerIds =
      Regex("register\\(\"([^\"]+)\"\\)").findAll(pickerSource).map { it.groupValues[1] }.toSet()
    val missing = declared.filterNot { jvm[it] != null || it in pickerIds }.toSet()
    assertEquals(
      emptySet<String>(),
      missing,
      "the catalog offers a canvas adapter that neither the JVM nor Wasm runtime implements",
    )
    assertEquals(
      setOf("wear-m3/date-picker", "wear-m3/time-picker"),
      pickerIds,
      "the JVM test must account for the Wasm-only picker adapters exactly",
    )
  }
}
