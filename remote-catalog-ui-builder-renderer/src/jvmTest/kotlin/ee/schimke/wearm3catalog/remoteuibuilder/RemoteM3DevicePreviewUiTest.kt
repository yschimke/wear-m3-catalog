package ee.schimke.wearm3catalog.remoteuibuilder

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.UiBuilderDocument
import ee.schimke.composeai.uibuilder.WearWidgetHostShape
import ee.schimke.composeai.uibuilder.WearWidgetScaffoldSize
import ee.schimke.composeai.uibuilder.hostSpec
import ee.schimke.wearm3catalog.uibuilder.WEAR_WIDGET_HOST_SHAPE_ENVIRONMENT_KEY
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray

/** Compose UI coverage for the real Remote M3 creation-and-player surface used by UI Builder. */
@OptIn(ExperimentalTestApi::class)
class RemoteM3DevicePreviewUiTest {

  @Test
  fun `authored widget captures content and background into the fixed host frame`() =
    runDesktopComposeUiTest(width = 432, height = 240) {
      var readyCalls = 0
      setContent {
        RemoteM3DevicePreview(document(), widthDp = 216f, heightDp = 76f) { readyCalls++ }
      }

      waitUntil(timeoutMillis = 10_000) { readyCalls == 1 }

      onNodeWithTag(REMOTE_M3_DEVICE_PREVIEW_TEST_TAG)
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Ready"))
      onNodeWithTag(REMOTE_M3_WIDGET_HOST_TEST_TAG)
        // UI Builder's default host is the widest rectangular preview spec. Authored padding
        // changes the content box, not this launcher-owned footprint.
        .assertWidthIsEqualTo(224.dp)
        .assertHeightIsEqualTo(84.dp)
      onNodeWithTag(REMOTE_M3_WIDGET_BACKGROUND_TEST_TAG).assertIsDisplayed()
      onNodeWithTag(REMOTE_M3_WIDGET_CONTENT_TEST_TAG).assertIsDisplayed()
      assertEquals(1, readyCalls)
    }

  @Test
  fun `the host frame is the shape the editor names for the pane`() {
    // The editor sends each preview pane's shape in the environment; without it every pane was
    // framed as the default, so the Samsung and Pixel Watch panes drew the same rectangle.
    WearWidgetHostShape.entries.forEach { shape ->
      runDesktopComposeUiTest(width = 480, height = 320) {
        val spec = WearWidgetScaffoldSize.Small.hostSpec(shape)
        var readyCalls = 0
        val sent =
          document().let {
            it.copy(
              environment =
                JsonObject(
                  it.environment +
                    (WEAR_WIDGET_HOST_SHAPE_ENVIRONMENT_KEY to JsonPrimitive(shape.id))
                )
            )
          }
        setContent {
          RemoteM3DevicePreview(sent, spec.frameWidthDp.toFloat(), spec.frameHeightDp.toFloat()) {
            readyCalls++
          }
        }

        waitUntil(timeoutMillis = 10_000) { readyCalls == 1 }

        onNodeWithTag(REMOTE_M3_WIDGET_HOST_TEST_TAG)
          .assertWidthIsEqualTo(spec.frameWidthDp.toFloat().dp)
          .assertHeightIsEqualTo(spec.frameHeightDp.toFloat().dp)
      }
    }
  }

  @Test
  fun `unsupported modifier fails visibly instead of changing the document`() =
    runDesktopComposeUiTest(width = 432, height = 240) {
      var readyCalls = 0
      setContent {
        RemoteM3DevicePreview(
          document()
            .copy(
              nodes =
                document().nodes.mapValues { (id, node) ->
                  if (id == "button")
                    node.copy(
                      modifiers =
                        Json.parseToJsonElement("""[{"type":"unsupported-test-modifier"}]""")
                          .jsonArray
                    )
                  else node
                }
            ),
          widthDp = 216f,
          heightDp = 76f,
        ) {
          readyCalls++
        }
      }

      waitUntil(timeoutMillis = 10_000) { readyCalls == 1 }

      onNodeWithTag(REMOTE_M3_DEVICE_PREVIEW_TEST_TAG)
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Failed"))
      onNodeWithText("Unsupported Remote Compose modifier", substring = true).assertIsDisplayed()
      assertEquals(1, readyCalls)
    }

  private fun document(): UiBuilderDocument = Json.decodeFromString(DOCUMENT)

  private companion object {
    val DOCUMENT =
      """
      {
        "schema": "compose-ui-builder-document/v1-candidate",
        "id": "remote-m3-compose-ui-test",
        "title": "Remote M3 Compose UI test",
        "revision": 2,
        "catalogPin": {
          "systemId": "remote-m3",
          "catalogRevision": "test",
          "capabilityDigest": "test",
          "nativeRuntimeId": "remote-m3-test-runtime"
        },
        "environment": {
          "widthDp": 216,
          "heightDp": 76,
          "density": 1,
          "theme": "dark",
          "fontScale": 1,
          "layoutDirection": "ltr"
        },
        "stateVariables": {},
        "roots": ["root"],
        "nodes": {
          "root": {
            "id": "root",
            "componentId": "remote-m3/widget-container-small",
            "properties": {
              "background": { "type": "colorToken", "value": "primary" },
              "horizontalPaddingDp": { "type": "float", "value": 12 },
              "verticalPaddingDp": { "type": "float", "value": 6 },
              "cornerRadiusDp": { "type": "float", "value": 18 }
            },
            "modifiers": [],
            "slots": { "background": ["background"], "content": ["column"] }
          },
          "background": {
            "id": "background",
            "componentId": "m3/text",
            "properties": {
              "text": { "type": "string", "value": "BG" },
              "color": { "type": "colorToken", "value": "tertiary" }
            },
            "modifiers": [],
            "slots": {}
          },
          "column": {
            "id": "column",
            "componentId": "layout/column",
            "properties": {
              "horizontalAlignment": { "type": "enum", "value": "end" },
              "verticalArrangement": { "type": "enum", "value": "center" },
              "verticalSpacingDp": { "type": "float", "value": 4 }
            },
            "modifiers": [{ "type": "fillMaxSize" }],
            "slots": { "children": ["button"] }
          },
          "button": {
            "id": "button",
            "componentId": "remote-m3/remote-button",
            "properties": { "enabled": { "type": "boolean", "value": true } },
            "modifiers": [{ "type": "fillMaxWidth" }],
            "slots": { "content": ["label"] }
          },
          "label": {
            "id": "label",
            "componentId": "remote-m3/remote-text",
            "properties": {
              "text": { "type": "string", "value": "Primary label" },
              "color": { "type": "colorToken", "value": "onPrimary" },
              "style": { "type": "enum", "value": "labelLarge" }
            },
            "modifiers": [],
            "slots": {}
          }
        }
      }
      """
        .trimIndent()
  }
}
