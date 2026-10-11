package ee.schimke.wearm3catalog.typed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import ee.schimke.catalog.typed.wearM3Policy
import ee.schimke.composeai.uibuilder.export.UiBuilderNode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasAdapterRegistry
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasItemScope
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasMode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import ee.schimke.wearm3catalog.uibuilder.wearCanvasAdapters
import ee.schimke.wearm3catalog.uibuilder.wearScreenAdapters
import ee.schimke.wearm3catalog.uibuilder.wearTextAdapters
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.skia.Image

/**
 * The typed pilot draws what production draws. Every typed registration is rendered beside the
 * production adapter for the same id, from the same document node, in the spellings saved designs
 * use (`enum`, `colorToken`, `float`, …): once with every property the policy offers set, and once
 * per offered value of every enumerated property, so each variant branch is compared. The two must
 * agree pixel for pixel. The pickers' production adapters are browser-only, so on the JVM the typed
 * pickers are compared with the real Wear pickers called directly.
 */
@OptIn(ExperimentalTestApi::class)
class TypedWearRendererTest {
  private val typed = TypedWearCatalog(discoverTypedWearControls())
  private val typedRegistry = typedWearCanvasAdapters(typed)
  private val production: CanvasAdapterRegistry =
    wearCanvasAdapters + wearScreenAdapters + wearTextAdapters
  private val policy = wearM3Policy()

  @Test
  fun `every typed registration matches production for every offered value`() {
    val compared = mutableListOf<String>()
    for (definition in typed.runtimeAdapters) {
      if (definition.id in PICKERS) continue
      val offered = offeredProperties(definition.id)
      val base = offered.mapValues { (name, capability) -> sample(name, capability) }
      val variants =
        listOf("all" to base) +
          offered.flatMap { (name, capability) ->
            capability.allowed().spread().map { value ->
              "$name=$value" to base + (name to enumOf(value))
            }
          }
      for ((label, properties) in variants) {
        val case =
          "${definition.id.substringAfter('/')}-${label.replace(Regex("[^A-Za-z0-9=-]"), "_")}"
        val (direct, adapted) =
          captureBoth(
            { modifier ->
              production[definition.id]!!.invoke(scope(definition.id, properties, modifier))
            },
            { modifier ->
              typedRegistry[definition.id]!!.invoke(scope(definition.id, properties, modifier))
            },
          )
        assertSamePixels(case, direct, adapted, keep = label == "all" || "segmented=" in label)
        compared += case
      }
    }
    println("Typed/production parity: ${compared.size} cases identical")
    // Every typed id outside the pickers was compared, with its variants.
    assertTrue(compared.size > typed.runtimeAdapters.size, compared.joinToString())
  }

  @Test
  fun `a slider saved with the legacy segmented flag draws as production draws it`() {
    for (flag in listOf(true, false)) {
      val properties =
        mapOf(
          "segmented" to
            buildJsonObject {
              put("type", "bool")
              put("value", flag)
            },
          "steps" to
            buildJsonObject {
              put("type", "int")
              put("value", 3)
            },
        )
      val (direct, adapted) =
        captureBoth(
          { modifier ->
            production["wear-m3/slider"]!!.invoke(scope("wear-m3/slider", properties, modifier))
          },
          { modifier ->
            typedRegistry["wear-m3/slider"]!!.invoke(scope("wear-m3/slider", properties, modifier))
          },
        )
      assertSamePixels("slider-legacy-segmented-$flag", direct, adapted)
    }
  }

  @Test
  fun `typed pickers match the real Wear pickers`() {
    for (type in listOf("", "day-month-year", "month-day-year")) {
      val (direct, adapted) =
        captureBoth(
          { modifier ->
            DatePicker(
              initialDate = LocalDate.of(2025, 7, 14),
              onDatePicked = {},
              modifier = modifier,
              datePickerType =
                when (type) {
                  "day-month-year" -> DatePickerType.DayMonthYear
                  "month-day-year" -> DatePickerType.MonthDayYear
                  else -> DatePickerType.YearMonthDay
                },
            )
          },
          { modifier ->
            typedRegistry["wear-m3/date-picker"]!!.invoke(
              scope(
                "wear-m3/date-picker",
                mapOf("initialDate" to string("2025-07-14"), "type" to enumOf(type)),
                modifier,
              )
            )
          },
        )
      assertSamePixels("date-picker-$type", direct, adapted)
    }
    for (type in listOf("", "hours-minutes-am-pm", "hours-minutes-seconds")) {
      val (direct, adapted) =
        captureBoth(
          { modifier ->
            TimePicker(
              initialTime = LocalTime.of(7, 45),
              onTimePicked = {},
              modifier = modifier,
              timePickerType =
                when (type) {
                  "hours-minutes-am-pm" -> TimePickerType.HoursMinutesAmPm12H
                  "hours-minutes-seconds" -> TimePickerType.HoursMinutesSeconds24H
                  else -> TimePickerType.HoursMinutes24H
                },
            )
          },
          { modifier ->
            typedRegistry["wear-m3/time-picker"]!!.invoke(
              scope(
                "wear-m3/time-picker",
                mapOf("initialTime" to string("07:45"), "type" to enumOf(type)),
                modifier,
              )
            )
          },
        )
      assertSamePixels("time-picker-$type", direct, adapted)
    }
  }

  @Test
  fun `typed buttons pass their own click callback to the component`() {
    for (id in
      listOf(
        "wear-m3/button",
        "wear-m3/card",
        "wear-m3/icon-button",
        "wear-m3/text-button",
        "wear-m3/edge-button",
      )) {
      val events = mutableListOf<String>()
      runDesktopComposeUiTest(width = 454, height = 454) {
        setContent {
          MaterialTheme {
            Box(Modifier.size(227.dp)) {
              typedRegistry[id]!!.invoke(scope(id, emptyMap(), Modifier, events = events))
            }
          }
        }
        onNodeWithText("Slot content").performClick()
        waitForIdle()
      }
      assertEquals(listOf("click"), events, id)
    }
  }

  @Test
  fun `typed selection controls write their bound state back and fire their change event`() {
    for ((id, property, event) in
      listOf(
        Triple("wear-m3/checkbox-button", "checked", "checkedChange"),
        Triple("wear-m3/switch-button", "checked", "checkedChange"),
        Triple("wear-m3/radio-button", "selected", "select"),
      )) {
      val events = mutableListOf<String>()
      val updates = mutableListOf<Pair<String, String?>>()
      runDesktopComposeUiTest(width = 454, height = 454) {
        setContent {
          MaterialTheme {
            Box(Modifier.size(227.dp)) {
              typedRegistry[id]!!.invoke(
                scope(
                  id,
                  mapOf(
                    property to
                      buildJsonObject {
                        put("type", "state")
                        put("variable", "notify")
                        put("value", "false")
                      },
                    "label" to string("Notify"),
                  ),
                  Modifier,
                  events = events,
                  updates = updates,
                )
              )
            }
          }
        }
        onNodeWithText("Notify").performClick()
        waitForIdle()
      }
      assertEquals<List<Pair<String, String?>>>(listOf("notify" to "true"), updates, id)
      assertEquals(event, events.single(), id)
    }
  }

  @Test
  fun `a malformed value is a node-local diagnostic, never a silent default`() {
    runDesktopComposeUiTest(width = 454, height = 454) {
      setContent {
        MaterialTheme {
          Column {
            typedRegistry["wear-m3/slider"]!!.invoke(
              scope("wear-m3/slider", mapOf("steps" to string("three")), Modifier)
            )
            typedRegistry["wear-m3/text"]!!.invoke(
              scope("wear-m3/text", mapOf("text" to string("Healthy sibling")), Modifier)
            )
          }
        }
      }
      onNodeWithText("invalid properties", substring = true).assertExists()
      onNodeWithText("Healthy sibling").assertExists()
    }
  }

  private fun captureBoth(
    direct: @Composable (Modifier) -> Unit,
    adapted: @Composable (Modifier) -> Unit,
  ): Pair<ImageBitmap?, ImageBitmap?> =
    // Each in its own window, so neither's layout or focus can perturb the other.
    capture(direct) to capture(adapted)

  /** The control's pixels, or null when it lays out empty (nothing to capture). */
  private fun capture(content: @Composable (Modifier) -> Unit): ImageBitmap? {
    var image: ImageBitmap? = null
    runDesktopComposeUiTest(width = 454, height = 454) {
      setContent {
        MaterialTheme { Box(Modifier.size(227.dp)) { content(Modifier.testTag("control")) } }
      }
      waitForIdle()
      onNodeWithText("invalid properties", substring = true).assertDoesNotExist()
      val control = onNodeWithTag("control", useUnmergedTree = true)
      val size = control.fetchSemanticsNode().size
      if (size.width > 0 && size.height > 0) image = control.captureToImage()
    }
    return image
  }

  /** Compare, keeping the images for [keep] cases and for any case that differs. */
  private fun assertSamePixels(
    case: String,
    direct: ImageBitmap?,
    adapted: ImageBitmap?,
    keep: Boolean = true,
  ) {
    if (direct == null || adapted == null) {
      assertEquals(direct == null, adapted == null, "$case: only one side laid out empty")
      return
    }
    val left = direct.toPixelMap()
    val right = adapted.toPixelMap()
    var differing = if (left.width == right.width && left.height == right.height) 0 else -1
    if (differing == 0) {
      for (y in 0 until left.height) for (x in 0 until left.width) {
        if (left[x, y] != right[x, y]) differing++
      }
    }
    if (keep || differing != 0) {
      val output = Path.of(System.getProperty("typedRenderEvidence")).resolve("parity")
      Files.createDirectories(output)
      listOf("production" to direct, "typed" to adapted).forEach { (kind, bitmap) ->
        Files.write(
          output.resolve("$case-$kind.png"),
          Image.makeFromBitmap(bitmap.asSkiaBitmap()).use {
            it.encodeToData()!!.use { data -> data.bytes }
          },
        )
      }
    }
    assertEquals(direct.width, adapted.width, "$case width")
    assertEquals(direct.height, adapted.height, "$case height")
    assertEquals(0, differing, "$case: pixels differing from production")
  }

  /**
   * Every offered value of a variant or style switch; for a vocabulary the size of the icon set,
   * eight spread across it, first and last included.
   */
  private fun List<String>.spread(): List<String> =
    if (size <= 32) this else (0 until 8).map { this[it * (size - 1) / 7] }.distinct()

  private fun scope(
    id: String,
    properties: Map<String, JsonElement>,
    modifier: Modifier,
    events: MutableList<String> = mutableListOf(),
    updates: MutableList<Pair<String, String?>> = mutableListOf(),
  ): CanvasNodeScope {
    val definition = typed.runtimeAdapters.single { it.id == id }
    // One child in every slot: the label text a designer drops in, as the editor fills them.
    val slots = definition.slots.associate { it.name to listOf("child") }
    return CanvasNodeScope(
      node = UiBuilderNode("node", id, JsonObject(properties), slots = slots),
      modifier = modifier,
      mode = CanvasMode.Device,
      renderSlot = { _, slotModifier -> Text("Slot content", modifier = slotModifier) },
      renderItems = { _, content ->
        CanvasItemScope { itemModifier -> Text("Slot content", modifier = itemModifier) }
          .content(UiBuilderNode("child", "wear-m3/text"))
      },
      countItems = { 1 },
      renderItem = { _, _, itemModifier -> Text("Slot content", modifier = itemModifier) },
      dispatchEvent = { events += it },
      updateState = { variable, value -> updates += variable to value },
      recordText = {},
    )
  }

  /** The properties production's policy offers for [id], by name. */
  private fun offeredProperties(id: String): Map<String, JsonObject> {
    val entry = policy.components[id]
    val capabilities =
      entry?.propertyCapabilities?.map { it.jsonObject }
        ?: policy.builtins.getValue(id).properties.map { it.jsonObject }
    return capabilities.associateBy { it.getValue("name").jsonPrimitive.content }
  }

  /** A saved design's value for a property: an offered value, a colour, a number or a flag. */
  private fun sample(name: String, capability: JsonObject): JsonElement {
    capability.allowed().firstOrNull()?.let {
      return enumOf(it)
    }
    val jsonType = capability["jsonType"]
    val types =
      (jsonType as? JsonPrimitive)?.let { listOf(it.content) }
        ?: (jsonType as? kotlinx.serialization.json.JsonArray)?.map { it.jsonPrimitive.content }
        ?: emptyList()
    return when {
      "boolean" in types ->
        buildJsonObject {
          put("type", "bool")
          put("value", name != "segmented")
        }
      name in INTEGERS ->
        buildJsonObject {
          put("type", "int")
          put("value", 2)
        }
      "number" in types || "integer" in types ->
        buildJsonObject {
          put("type", "float")
          put("value", if (name == "progress" || name == "value") 0.5 else 18)
        }
      name.endsWith("Color", ignoreCase = true) || name == "color" ->
        buildJsonObject {
          put("type", "colorToken")
          put("value", "#FF336699")
        }
      else -> string(if (name == "iconKey") "favorite" else "Sample")
    }
  }

  private fun JsonObject.allowed(): List<String> =
    (this["allowedValues"] as? kotlinx.serialization.json.JsonArray).orEmpty().map {
      it.jsonPrimitive.content
    }

  private fun enumOf(value: String) = buildJsonObject {
    put("type", "enum")
    put("value", value)
  }

  private fun string(value: String) = buildJsonObject {
    put("type", "string")
    put("value", value)
  }

  private companion object {
    val PICKERS = setOf("wear-m3/date-picker", "wear-m3/time-picker")
    val INTEGERS = setOf("minLines", "maxLines", "steps", "segments")
  }
}
