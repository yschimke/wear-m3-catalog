package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.export.UiBuilderNode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasMode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * A selection row bound to a design flag is two-way bound on the canvas: tapping it writes the new
 * value back to the variable `checked` reads, then runs the row's `click` actions — and steps aside
 * when those actions already write that variable, so a hand-authored `toggle` is not doubled.
 */
@OptIn(ExperimentalTestApi::class)
class WearTwoWayBindingTest {
  private class Recorded {
    val writes = mutableListOf<Pair<String, String?>>()
    val events = mutableListOf<String>()
  }

  private fun tap(
    componentId: String,
    property: String,
    eventBindings: JsonObject = JsonObject(emptyMap()),
  ): Recorded {
    val recorded = Recorded()
    val node =
      UiBuilderNode(
        id = "row",
        componentId = componentId,
        properties =
          buildJsonObject {
            // As the SDK hands it over: the wrapper kept, its live value resolved into it.
            putJsonObject(property) {
              put("type", "state")
              put("variable", "notify")
              put("value", "false")
            }
            putJsonObject("label") {
              put("type", "string")
              put("value", "Notify")
            }
          },
        eventBindings = eventBindings,
      )
    runDesktopComposeUiTest(width = 454, height = 454) {
      setContent {
        Box(Modifier.size(227.dp)) {
          val scope =
            CanvasNodeScope(
              node = node,
              modifier = Modifier,
              mode = CanvasMode.Device,
              renderSlot = { _, _ -> },
              renderItems = { _, _ -> },
              countItems = { 0 },
              renderItem = { _, _, _ -> },
              dispatchEvent = { recorded.events += it },
              updateState = { name, value -> recorded.writes += name to value },
              recordText = {},
            )
          val adapter = wearCanvasAdapters[componentId]!!
          scope.adapter()
        }
      }
      onNodeWithText("Notify").performClick()
      waitForIdle()
    }
    return recorded
  }

  @Test
  fun `a bound checkbox button writes its new value back and runs its click actions`() {
    listOf("wear-m3/checkbox-button", "wear-m3/switch-button").forEach { componentId ->
      val recorded = tap(componentId, "checked")
      assertEquals(listOf<Pair<String, String?>>("notify" to "true"), recorded.writes, componentId)
      assertEquals(listOf("click"), recorded.events, componentId)
    }
  }

  @Test
  fun `a bound radio button selects itself`() {
    val recorded = tap("wear-m3/radio-button", "selected")
    assertEquals(listOf<Pair<String, String?>>("notify" to "true"), recorded.writes)
    assertEquals(listOf("click"), recorded.events)
  }

  @Test
  fun `an authored toggle of the bound flag is the only write`() {
    val toggle = buildJsonObject {
      put(
        "click",
        JsonArray(
          listOf(
            buildJsonObject {
              put("type", "toggle")
              put("variable", "notify")
            }
          )
        ),
      )
    }
    val recorded = tap("wear-m3/checkbox-button", "checked", toggle)
    assertEquals(emptyList<Pair<String, String?>>(), recorded.writes)
    assertEquals(listOf("click"), recorded.events)
  }
}
