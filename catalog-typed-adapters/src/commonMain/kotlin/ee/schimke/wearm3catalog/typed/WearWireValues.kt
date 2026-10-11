package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.AdapterProperty
import ee.schimke.composeai.discovery.TypedComponentAdapter
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasAdapterRegistry
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * The typed registrations behind the spellings saved Wear designs already use. The typed bridge
 * accepts exactly its codec's literal wrapper (`string`, `bool`, `int`, `number`) or `state`;
 * designs write `enum`, `colorToken`, `typographyToken`, `float` and friends, all after the SDK has
 * resolved bindings. Each is mapped onto the codec's own spelling here, so a saved design draws the
 * same through either renderer, and anything else still reaches the bridge's strict check.
 */
fun typedWearCanvasAdapters(catalog: TypedWearCatalog): CanvasAdapterRegistry {
  val typed = rawTypedWearCanvasAdapters(catalog)
  return canvasAdapterRegistry {
    catalog.runtimeAdapters.forEach { definition ->
      register(definition.id) { typed[definition.id]!!.invoke(normalizedScope(this, definition)) }
    }
  }
}

private val stringSpellings =
  setOf("enum", "color", "colorToken", "typographyToken", "shapeToken", "text", "icon")
private val numberSpellings = setOf("float", "dp", "sp", "int", "number", "integer")

internal fun normalizedScope(
  scope: CanvasNodeScope,
  definition: TypedComponentAdapter<*>,
): CanvasNodeScope {
  val properties = scope.node.properties.toMutableMap()
  definition.properties.forEach { property ->
    val wrapper = properties[property.name] as? JsonObject ?: return@forEach
    val type = (wrapper["type"] as? JsonPrimitive)?.contentOrNull
    if (type == "state") {
      // An unset state variable resolves to null: the component's own default stands in.
      if (wrapper["value"] == JsonNull)
        properties[property.name] = JsonObject(wrapper + ("value" to defaultValue(property)))
      return@forEach
    }
    val raw = wrapper["value"] as? JsonPrimitive
    val literal = property.codec.literalType
    val normalized =
      when (property.codec.jsonType) {
        "string" ->
          when {
            type in stringSpellings && raw != null && raw.isString -> literal to raw
            raw == JsonNull -> literal to JsonPrimitive("")
            else -> null
          }
        "boolean" ->
          when {
            type == "boolean" -> literal to raw
            type == "string" && raw?.booleanOrNull != null ->
              literal to JsonPrimitive(raw.booleanOrNull!!)
            else -> null
          }
        "number",
        "integer" ->
          if (type in numberSpellings && raw != null && !raw.isString && raw.doubleOrNull != null)
            literal to raw
          else null
        else -> null
      }
    if (normalized != null && normalized.second != null) {
      properties[property.name] =
        JsonObject(
          wrapper + mapOf("type" to JsonPrimitive(normalized.first), "value" to normalized.second!!)
        )
    }
  }
  return CanvasNodeScope(
    node = scope.node.copy(properties = JsonObject(properties)),
    modifier = scope.modifier,
    mode = scope.mode,
    renderSlot = { name, modifier -> scope.Slot(name, modifier) },
    renderItems = { name, render -> scope.Items(name, render) },
    countItems = scope::itemCount,
    renderItem = { name, index, modifier -> scope.Item(name, index, modifier) },
    dispatchEvent = scope::dispatch,
    // The copy resolves the same variable from the same wrapper; write it through the original
    // node's property so the SDK's own state routing stays the only writer.
    updateState = { variable, value ->
      scope.node.properties.entries
        .firstOrNull { (_, wrapper) ->
          ((wrapper as? JsonObject)?.get("variable") as? JsonPrimitive)?.contentOrNull == variable
        }
        ?.let { scope.updateBoundState(it.key, value) }
    },
    recordText = scope::recordTextLayout,
    registerScrolling = scope::registerScrolling,
    unrolledHorizontally = scope.unrolledHorizontally,
  )
}

private fun <P, T> defaultValue(property: AdapterProperty<P, T>) =
  property.codec.encode(property.default)
