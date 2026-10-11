package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.UiBuilderCatalogFile
import ee.schimke.composeai.discovery.UiBuilderCatalogs
import ee.schimke.composeai.discovery.UiBuilderPolicyFile
import ee.schimke.composeai.uibuilder.protocol.PropertyCapabilityV1
import ee.schimke.composeai.uibuilder.protocol.SlotCapabilityV1
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal data class CatalogInput(
  val module: String,
  val spec: String,
  val policy: UiBuilderPolicyFile,
)

internal fun catalogInputs() =
  listOf(
    CatalogInput("catalog", "catalog.spec.json", wearM3Policy()),
    CatalogInput("samples-catalog", "samples-catalog/catalog.spec.json", wearM3SamplesPolicy()),
  )

private val artifactJson = Json {
  prettyPrint = true
  encodeDefaults = true
  ignoreUnknownKeys = true
}

// Comments do not execute. Capability defaults are normalized by their owner serializers.
// Everything else,
// including order, code contracts, IDs and diagnostics,
// must survive the migration. Generation never loads ui-builder.policy.json.
internal fun executable(value: JsonElement): JsonElement =
  when (value) {
    is JsonObject ->
      JsonObject(
        value
          .filterKeys { !it.startsWith("\$comment") }
          .mapValues { (key, child) ->
            val normalized =
              when {
                key in setOf("propertyCapabilities", "properties") && child is JsonArray ->
                  JsonArray(
                    child.map {
                      artifactJson.encodeToJsonElement(
                        PropertyCapabilityV1.serializer(),
                        artifactJson.decodeFromJsonElement(PropertyCapabilityV1.serializer(), it),
                      )
                    }
                  )
                key == "slotCapabilities" && child is JsonArray ->
                  JsonArray(
                    child.map {
                      artifactJson.encodeToJsonElement(
                        SlotCapabilityV1.serializer(),
                        artifactJson.decodeFromJsonElement(SlotCapabilityV1.serializer(), it),
                      )
                    }
                  )
                else -> child
              }
            executable(normalized)
          }
      )
    is JsonArray -> JsonArray(value.map(::executable))
    else -> value
  }

fun generateCatalogs(root: Path, output: Path) {
  for (input in catalogInputs()) {
    val discovery = root.resolve(input.module).resolve("build/compose-previews")
    val recordPath = discovery.resolve("components.json")
    check(Files.isRegularFile(recordPath)) {
      "Discover :${input.module}:composePreviewDiscover before generating this pair: $recordPath"
    }
    val recordText = Files.readString(recordPath)
    val record = artifactJson.decodeFromString(ComponentRecordFile.serializer(), recordText)
    require(record.components.isNotEmpty()) {
      "Refusing an empty discovered inventory: ${input.module}"
    }
    val spec =
      artifactJson.parseToJsonElement(Files.readString(root.resolve(input.spec))).jsonObject
    val cover =
      UiBuilderCatalogs.CoverSheet(
        spec.getValue("system").jsonPrimitive.content,
        spec.getValue("title").jsonPrimitive.content,
      )
    val catalog = requireNotNull(UiBuilderCatalogs.generate(record, cover, input.policy))
    // A fresh discovery using the legacy policy is the compatibility oracle. A mismatch blocks
    // publication, rather than quietly emitting a second interpretation of the same saved design.
    val baseline =
      artifactJson.decodeFromString(
        UiBuilderCatalogFile.serializer(),
        Files.readString(discovery.resolve("ui-builder.json")),
      )
    check(
      executable(artifactJson.encodeToJsonElement(UiBuilderCatalogFile.serializer(), catalog)) ==
        executable(artifactJson.encodeToJsonElement(UiBuilderCatalogFile.serializer(), baseline))
    ) {
      "${catalog.catalog.id}: Kotlin generation changed the discovered catalog contract"
    }
    val destination = output.resolve(catalog.catalog.id)
    Files.createDirectories(output)
    val staging = Files.createTempDirectory(output, ".catalog-")
    try {
      // Preserve the real compiled signatures and annotation-owned inventory byte for byte.
      Files.writeString(staging.resolve("components.json"), recordText)
      Files.writeString(
        staging.resolve("ui-builder.json"),
        artifactJson.encodeToString(UiBuilderCatalogFile.serializer(), catalog) + "\n",
      )
      Files.writeString(
        staging.resolve("ui-builder.policy.json"),
        artifactJson.encodeToString(UiBuilderPolicyFile.serializer(), input.policy) + "\n",
      )
      for (template in input.policy.templates) {
        val path = Path.of(template.path).normalize()
        require(!path.isAbsolute && !path.startsWith("..")) {
          "Template escapes the catalog: $path"
        }
        val source =
          listOf(root.resolve(input.module).resolve(path), root.resolve(path)).firstOrNull {
            Files.isRegularFile(it)
          }
        check(source != null) { "Missing template: $path" }
        val target = staging.resolve(path)
        Files.createDirectories(target.parent)
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
      }
      val evidence = buildJsonObject {
        put("catalogId", catalog.catalog.id)
        put("discoveredComponents", record.components.size)
        put("completeCatalogEquivalent", true)
        put("compiledInventoryPreserved", true)
        put("templates", input.policy.templates.size)
        put("readyForCutover", false)
        put(
          "remainingGate",
          "Experimental property-reference renderer bridge and packaged-runtime verification",
        )
      }
      Files.writeString(
        staging.resolve("readiness.json"),
        artifactJson.encodeToString(JsonObject.serializer(), evidence) + "\n",
      )
      // All validation finishes before replacing this task-owned artifact directory.
      if (Files.exists(destination)) destination.toFile().deleteRecursively()
      Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE)
      println(
        "${catalog.catalog.id}: ${record.components.size} compiled records, equivalent catalog pair, ${input.policy.templates.size} templates"
      )
    } finally {
      staging.toFile().deleteRecursively()
    }
  }
}

fun main(args: Array<String>) {
  require(args.size == 2) { "Expected repository root and task-owned output directory" }
  generateCatalogs(Path.of(args[0]), Path.of(args[1]))
}
