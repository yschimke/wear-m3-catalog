package ee.schimke.wearm3catalog.typed

import ee.schimke.catalog.typed.wearM3Policy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TypedWearCatalogTest {
  private val typed = TypedWearCatalog(discoverTypedWearControls())

  @Test
  fun `every definition validates against its freshly discovered compiled signature`() {
    for (definition in typed.runtimeAdapters) {
      assertEquals(emptyList(), definition.validateAgainst(definition.component), definition.id)
    }
    assertEquals(wearDefinitions.map { it.id }.toSet(), typed.runtimeAdapters.map { it.id }.toSet())
  }

  @Test
  fun `typed ids are production ids, so saved designs address them unchanged`() {
    val policy = wearM3Policy()
    for (definition in typed.adapters) {
      assertEquals(definition.id, policy.components[definition.id]?.canvas, definition.id)
    }
    for (definition in typed.builtinAdapters) {
      assertEquals(definition.id, policy.builtins[definition.id]?.canvas, definition.id)
    }
  }

  @Test
  fun `the pilot pair keeps production capabilities and swaps in the typed records`() {
    val policy = wearM3Policy()
    val output = typed.generate(policy)
    assertEquals(TYPED_WEAR_CATALOG_ID, output.catalog.catalog.id)
    val components = output.catalog.statusSemantics.components
    assertEquals(typed.adapters.map { it.id }.toSet(), components.keys)
    for (definition in typed.adapters) {
      val generated = components.getValue(definition.id)
      assertEquals(definition.component.canonicalId, generated.record, definition.id)
      val offered = policy.components.getValue(definition.id).propertyCapabilities.orEmpty()
      assertTrue(generated.propertyCapabilities.orEmpty().containsAll(offered), definition.id)
    }
    assertEquals(policy.builtins.keys, output.catalog.statusSemantics.builtins.keys)
  }

  @Test
  fun `the pilot's template copies pin the pilot and keep everything else`() {
    val original =
      """{"id":"t","catalogPin":{"systemId":"wear-m3","nativeRuntimeId":"candidate"},"nodes":{}}"""
    val copy = kotlinx.serialization.json.Json.parseToJsonElement(pilotTemplate(original))
    val pin = copy.jsonObject.getValue("catalogPin").jsonObject
    assertEquals(TYPED_WEAR_CATALOG_ID, pin.getValue("systemId").jsonPrimitive.content)
    assertEquals("candidate", pin.getValue("nativeRuntimeId").jsonPrimitive.content)
    assertEquals("t", copy.jsonObject.getValue("id").jsonPrimitive.content)
  }
}
