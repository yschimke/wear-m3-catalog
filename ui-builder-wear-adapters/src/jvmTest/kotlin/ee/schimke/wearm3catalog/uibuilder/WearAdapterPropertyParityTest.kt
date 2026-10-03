package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.export.UiBuilderNode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasItemScope
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasMode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Every property `ui-builder.policy.json` offers on a component is read by the adapter that draws
 * it in the runtime.
 *
 * The editor's own canvas and this runtime are two implementations of the same components, and the
 * policy is what tells the editor which properties a design may set. A property the runtime never
 * reads is silently dropped: `wear-m3/button` ignored `containerColor` and `contentColor`, so a
 * design styled through them drew light lavender buttons under light text in every device preview,
 * and nothing failed (yschimke/wear-m3-catalog#697).
 *
 * Each adapter is drawn once for every value of every property with `allowedValues` — the variants,
 * whose branches read different properties — with every property set, through a node whose property
 * map records each lookup — drawn on its own and as the child of each foundation layout, since a
 * child's `weight` or `alignment` is read by the parent it sits in, as parent data. A property no
 * draw looked up is a gap. [KNOWN_UNREAD] lists the ones that are deliberate or not fixed yet, each
 * with its reason; one that is read after all must leave the list, so it cannot hide the next gap.
 */
@OptIn(ExperimentalTestApi::class)
class WearAdapterPropertyParityTest {
  private val policy: JsonObject =
    Json.parseToJsonElement(
        generateSequence(File(".").absoluteFile) { it.parentFile }
          .map { File(it, "ui-builder.policy.json") }
          .first { it.isFile }
          .readText()
      )
      .jsonObject

  /** The adapters the JVM carries; the pickers are wasm-only ([wearPickerAdapters]). */
  private val registry = wearCanvasAdapters + wearScreenAdapters + wearTextAdapters

  private class Component(
    val id: String,
    val adapterId: String,
    val properties: List<JsonObject>,
  )

  private val components: List<Component> =
    listOf("builtins", "components").flatMap { section ->
      policy[section]?.jsonObject.orEmpty().mapNotNull { (id, element) ->
        val entry = element.jsonObject
        if (entry["excluded"] != null) return@mapNotNull null
        val adapterId = entry["canvas"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val properties =
          (entry["propertyCapabilities"] ?: entry["properties"])?.jsonArray.orEmpty().map {
            it.jsonObject
          }
        Component(id, adapterId, properties)
      }
    }

  @Test
  fun `every property the policy offers is read by the runtime adapter that draws it`() {
    val drawable = components.filter {
      registry[it.adapterId] != null && it.properties.isNotEmpty()
    }
    val unread = sortedMapOf<String, Set<String>>()
    drawable.forEach { component ->
      val declared = component.properties.mapNotNull { it["name"]?.jsonPrimitive?.contentOrNull }
      val read = reads(component)
      val missing = (declared.toSet() - read - SDK_HANDLED).sorted().toSet()
      if (missing.isNotEmpty()) unread[component.id] = missing
    }
    assertEquals(
      KNOWN_UNREAD.toSortedMap(),
      unread,
      "A property the policy offers that the runtime adapter never reads is dropped from every " +
        "design drawn by the runtime. Read it in the adapter (as the editor's canvas does), or add " +
        "it to KNOWN_UNREAD with the reason. Not drawn on the JVM: " +
        components.filter { registry[it.adapterId] == null }.joinToString { it.id },
    )
  }

  /** Every property [component]'s adapter looked up, over one draw per enumerated value. */
  private fun reads(component: Component): Set<String> {
    val base =
      component.properties.associate { property ->
        val name = property["name"]!!.jsonPrimitive.content
        name to sample(name, property)
      }
    val variants =
      listOf(base) +
        component.properties.flatMap { property ->
          val name = property["name"]!!.jsonPrimitive.content
          property.allowed().map { value -> base + (name to typed(value)) }
        }
    val read = mutableSetOf<String>()
    val adapter = registry[component.adapterId]!!
    // One draw per variant, each on its own: the properties are looked up while composing, and a
    // component that cannot be measured with no children (a `ButtonGroup`) fails only after that.
    // The last draw puts the component in each foundation layout, whose reads of it as parent
    // data do not depend on the variant.
    val draws = variants.map { it to false } + (base to true)
    draws.forEach { (properties, asChild) ->
      runCatching {
        runDesktopComposeUiTest(width = 454, height = 454) {
          setContent {
            val node =
              UiBuilderNode(
                id = component.id,
                componentId = component.id,
                properties = JsonObject(Recording(properties, read)),
              )
            Box(Modifier.size(227.dp)) {
              if (!asChild) {
                scope(node).adapter()
              } else {
                val child = CanvasItemScope { modifier -> scope(node, modifier).adapter() }
                PARENTS.forEach { parentId ->
                  val layout = foundationCanvasAdapters[parentId]!!
                  val parent = UiBuilderNode(id = parentId, componentId = parentId)
                  scope(parent, children = { content -> child.content(node) }).layout()
                }
              }
            }
          }
          waitForIdle()
        }
      }
    }
    return read
  }

  private fun scope(
    node: UiBuilderNode,
    modifier: Modifier = Modifier,
    children: @Composable (@Composable CanvasItemScope.(UiBuilderNode) -> Unit) -> Unit = {},
  ) =
    CanvasNodeScope(
      node = node,
      modifier = modifier,
      mode = CanvasMode.Device,
      renderSlot = { _, _ -> },
      renderItems = { _, content -> children(content) },
      countItems = { 0 },
      renderItem = { _, _, _ -> },
      dispatchEvent = {},
      updateState = { _, _ -> },
      recordText = {},
    )

  /** A property map that remembers every name looked up in it. */
  private class Recording(
    private val content: Map<String, JsonElement>,
    private val read: MutableSet<String>,
  ) : Map<String, JsonElement> by content {
    override fun get(key: String): JsonElement? = content[key].also { read += key }

    override fun containsKey(key: String): Boolean = content.containsKey(key).also { read += key }
  }

  private fun sample(name: String, property: JsonObject): JsonElement {
    property.allowed().firstOrNull()?.let {
      return typed(it)
    }
    val jsonType = property["jsonType"]
    val types =
      (jsonType as? JsonPrimitive)?.let { listOf(it.content) }
        ?: (jsonType as? kotlinx.serialization.json.JsonArray)?.mapNotNull {
          (it as? JsonPrimitive)?.content
        }
        ?: emptyList()
    return when (types.firstOrNull { it != "null" }) {
      "boolean" ->
        buildJsonObject {
          put("type", "boolean")
          put("value", true)
        }
      "number",
      "integer" ->
        buildJsonObject {
          put("type", "float")
          put("value", 1)
        }
      else -> typed(if (name.endsWith("Color", ignoreCase = true)) "#FF336699" else "Sample")
    }
  }

  /** The scalar values a property is offered, if it enumerates any. */
  private fun JsonObject.allowed(): List<String> =
    (this["allowedValues"] as? kotlinx.serialization.json.JsonArray).orEmpty().mapNotNull {
      (it as? JsonPrimitive)?.content
    }

  private fun typed(value: String): JsonObject = buildJsonObject {
    put("type", "string")
    put("value", JsonPrimitive(value))
  }

  companion object {
    /**
     * Properties the SDK spends before an adapter runs: an action is the node's click, wired into
     * the modifier it hands the adapter.
     */
    val SDK_HANDLED: Set<String> = setOf("onClickAction")

    /** Gaps known today, by component, each with why it is not read. */
    val KNOWN_UNREAD: Map<String, Set<String>> =
      mapOf(
        // The generated lazy list's `key`: an identity, which nothing draws.
        "wear-m3/card" to setOf("stableKey")
      )

    /** The layouts a child is drawn in, so that what they read off the child counts as read. */
    val PARENTS: List<String> = listOf("layout/box", "layout/column", "layout/row")
  }
}
