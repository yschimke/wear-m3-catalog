package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.UiBuilderPolicyFile
import ee.schimke.composeai.uibuilder.protocol.PropertyCapabilityV1
import ee.schimke.composeai.uibuilder.protocol.SlotCapabilityV1
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthoredPolicyTest {
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  @Test
  fun `Kotlin policy preserves the complete production vocabulary`() {
    fun canonical(policy: UiBuilderPolicyFile): JsonElement {
      val normalized =
        policy
          .newBuilder()
          .apply {
            comment = null
            components = components.mapValues { (_, component) ->
              component
                .newBuilder()
                .apply {
                  propertyCapabilities = propertyCapabilities?.map(::property)
                  slotCapabilities = slotCapabilities?.map(::slot)
                }
                .build()
            }
            builtins = builtins.mapValues { (_, builtin) ->
              builtin.newBuilder().apply { properties = properties.map(::property) }.build()
            }
          }
          .build()
      return withoutComments(json.encodeToJsonElement(UiBuilderPolicyFile.serializer(), normalized))
    }
    authoredPolicies().forEach { authored ->
      val production =
        json.decodeFromString(
          UiBuilderPolicyFile.serializer(),
          File(System.getProperty("catalogRoot"), authored.baseline).readText(),
        )
      assertEquals(canonical(production), canonical(authored.policy), authored.outputName)
      assertEquals(production.components.keys.toList(), authored.policy.components.keys.toList())
      assertEquals(production.builtins.keys.toList(), authored.policy.builtins.keys.toList())
      assertEquals(production.templates, authored.policy.templates)
    }
  }

  @Test
  fun `generation produces round trippable policies and keeps cutover blocked`() {
    val output = Files.createTempDirectory("typed-policy-artifacts")
    try {
      main(arrayOf(output.toString()))
      authoredPolicies().forEach { authored ->
        val directory = output.resolve(authored.outputName)
        val generated =
          json.decodeFromString(
            UiBuilderPolicyFile.serializer(),
            Files.readString(directory.resolve("ui-builder.policy.json")),
          )
        assertEquals(authored.policy, generated, authored.outputName)
        val readiness =
          json.parseToJsonElement(Files.readString(directory.resolve("readiness.json"))).jsonObject
        assertFalse(readiness.getValue("readyForCutover").jsonPrimitive.boolean)
        assertEquals(
          authored.policy.components.size,
          readiness.getValue("componentPolicies").jsonPrimitive.content.toInt(),
        )
        assertEquals(
          authored.policy.builtins.size,
          readiness.getValue("builtins").jsonPrimitive.content.toInt(),
        )
      }
    } finally {
      output.toFile().deleteRecursively()
    }
  }

  private fun property(value: JsonElement) =
    json.encodeToJsonElement(
      PropertyCapabilityV1.serializer(),
      json.decodeFromJsonElement(PropertyCapabilityV1.serializer(), value),
    )

  private fun slot(value: JsonElement) =
    json.encodeToJsonElement(
      SlotCapabilityV1.serializer(),
      json.decodeFromJsonElement(SlotCapabilityV1.serializer(), value),
    )

  private fun withoutComments(value: JsonElement): JsonElement =
    when (value) {
      is JsonObject ->
        JsonObject(
          value.filterKeys { !it.startsWith("\$comment") }.mapValues { withoutComments(it.value) }
        )
      is JsonArray -> JsonArray(value.map(::withoutComments))
      else -> value
    }
}
