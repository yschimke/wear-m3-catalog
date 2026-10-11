package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.uibuilder.export.UiBuilderNode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasMode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import kotlin.test.Test
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@OptIn(ExperimentalTestApi::class)
class WearCatalogCoverageTest {
  @Test
  fun `alert dialog renders authored action slots including a single button`() {
    listOf(
        listOf("confirmButton", "dismissButton"),
        listOf("confirmButton"),
        listOf("dismissButton"),
      )
      .forEach { slots ->
        runDesktopComposeUiTest(width = 454, height = 454) {
          setContent {
            val child = UiBuilderNode(id = "child", componentId = "wear-m3/text")
            val node =
              UiBuilderNode(
                id = "dialog",
                componentId = "wear-m3/alert-dialog",
                slots = slots.associateWith { listOf(child.id) },
              )
            Box(Modifier.size(227.dp)) {
              val scope = scope(node) { name -> if (name in slots) Text("Authored $name") }
              val adapter = wearCanvasAdapters[node.componentId]!!
              scope.adapter()
            }
          }
          slots.forEach { onNodeWithText("Authored $it", useUnmergedTree = true).assertExists() }
        }
      }
  }

  @Test
  fun `variable font text is registered and renders its authored text`() {
    runDesktopComposeUiTest(width = 454, height = 454) {
      setContent {
        val node =
          UiBuilderNode(
            id = "variable-text",
            componentId = "wear-m3/variable-font-text",
            properties =
              buildJsonObject {
                putJsonObject("text") {
                  put("type", "string")
                  put("value", "Variable Wear text")
                }
                putJsonObject("wght") {
                  put("type", "float")
                  put("value", 700f)
                }
                putJsonObject("fontSizeSp") {
                  put("type", "float")
                  put("value", 24f)
                }
              },
          )
        Box(Modifier.size(227.dp)) {
          val scope = scope(node)
          val adapter = wearTextAdapters[node.componentId]!!
          scope.adapter()
        }
      }
      onNodeWithText("Variable Wear text").assertExists()
    }
  }

  private fun scope(
    node: UiBuilderNode,
    slot: @androidx.compose.runtime.Composable (String) -> Unit = {},
  ) =
    CanvasNodeScope(
      node = node,
      modifier = Modifier,
      mode = CanvasMode.Device,
      renderSlot = { name, _ -> slot(name) },
      renderItems = { _, _ -> },
      countItems = { 0 },
      renderItem = { _, _, _ -> },
      dispatchEvent = {},
      updateState = { _, _ -> },
      recordText = {},
    )
}
