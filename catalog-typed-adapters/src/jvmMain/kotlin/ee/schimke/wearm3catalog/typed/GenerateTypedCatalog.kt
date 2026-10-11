package ee.schimke.wearm3catalog.typed

import ee.schimke.catalog.typed.wearM3Policy
import ee.schimke.composeai.discovery.BuilderPolicy
import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.ScreenDocument
import ee.schimke.composeai.discovery.ScreenGenerator
import ee.schimke.composeai.discovery.ScreenNode
import ee.schimke.composeai.discovery.ScreenState
import ee.schimke.composeai.discovery.ScreenValue
import ee.schimke.composeai.discovery.TypedAdapterDiscovery
import ee.schimke.composeai.discovery.UiBuilderCatalogFile
import ee.schimke.composeai.discovery.UiBuilderPolicyFile
import ee.schimke.composeai.discovery.writeTo
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** The production structure adapters the pilot keeps: the screen frame and its scrolling list. */
val structuralAdapterIds = setOf("frame/round-screen", "wear-m3/transforming-lazy-column")

/**
 * A production template as the pilot pair carries it: pinned to the pilot catalog, so a design
 * created from the pilot's template selects the pilot rather than production `wear-m3`.
 */
fun pilotTemplate(text: String): String {
  val template = json.parseToJsonElement(text).jsonObject
  val pin = template["catalogPin"]?.jsonObject.orEmpty()
  val repinned =
    JsonObject(
      template +
        ("catalogPin" to JsonObject(pin + ("systemId" to JsonPrimitive(TYPED_WEAR_CATALOG_ID))))
    )
  return json.encodeToString(JsonObject.serializer(), repinned) + "\n"
}

/** Discover the compiled typed entry points from bytecode; nothing here loads or runs them. */
fun discoverTypedWearControls(): ComponentRecordFile {
  val record =
    TypedAdapterDiscovery.discover(
      module = "catalog-typed-adapters",
      variant = "jvm",
      callables =
        wearDefinitions.map {
          TypedAdapterDiscovery.Callable(
            "ee.schimke.wearm3catalog.typed.WearComponentsKt",
            it.callable,
          )
        },
    )
  return record
    .newBuilder()
    .apply {
      components = components.map { component ->
        component
          .newBuilder()
          .apply {
            builder =
              BuilderPolicy.Builder()
                .apply {
                  group = wearDefinitions.single { it.callable == component.symbol.name }.group
                }
                .build()
          }
          .build()
      }
    }
    .build()
}

private val json = Json {
  prettyPrint = true
  encodeDefaults = true
  ignoreUnknownKeys = true
}

fun main(args: Array<String>) {
  require(args.size == 3) {
    "Expected repository root, task-owned catalog output and records source"
  }
  val root = Path.of(args[0])
  val output = Path.of(args[1])
  val sources = Path.of(args[2])
  val base = wearM3Policy()
  val typed = TypedWearCatalog(discoverTypedWearControls())
  val pair = typed.generate(base)

  // Both directories belong to this task alone; the pair is written beside them, then moved.
  output.toFile().deleteRecursively()
  Files.createDirectories(output.parent)
  val staging = Files.createTempDirectory(output.parent, ".wear-typed-")
  try {
    val pairDirectory = staging.resolve("pair")
    pair.portable.writeTo(pairDirectory)
    Files.writeString(
      pairDirectory.resolve("ui-builder.json"),
      json.encodeToString(UiBuilderCatalogFile.serializer(), pair.catalog) + "\n",
    )
    for (template in base.templates) {
      val path = Path.of(template.path).normalize()
      require(!path.isAbsolute && !path.startsWith("..")) { "Template escapes the catalog: $path" }
      val target = pairDirectory.resolve(path)
      Files.createDirectories(target.parent)
      // The stored production template is never rewritten; only the pilot's copy is re-pinned.
      Files.writeString(target, pilotTemplate(Files.readString(root.resolve(path))))
    }
    writeSourceFixtures(typed, pair.record, pairDirectory.resolve("generated-source"))
    writeReadiness(root, base, typed, pair.catalog, pairDirectory.resolve("readiness.json"))
    Files.move(pairDirectory, output)
  } finally {
    staging.toFile().deleteRecursively()
  }

  // JVM discovery is the only writer of the browser's records; signatures are never transcribed.
  sources.toFile().deleteRecursively()
  val source = sources.resolve("ee/schimke/wearm3catalog/typed/GeneratedTypedRecords.kt")
  Files.createDirectories(source.parent)
  val encoded =
    Json.encodeToString(ComponentRecordFile.serializer(), typed.discovered)
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("$", "\\$")
  Files.writeString(
    source,
    "package ee.schimke.wearm3catalog.typed\n\ninternal const val typedWearRecordsJson = \"$encoded\"\n",
  )
  println("Typed Wear pilot pair: $output")
}

/**
 * Compose source for every typed entry point, with defaults and with explicit arguments and filled
 * slots, plus a state read for each two-way control. `:catalog-typed-consumer` compiles them
 * against the public wrapper API, a separate compilation from discovery and registration.
 */
private fun writeSourceFixtures(
  typed: TypedWearCatalog,
  record: ComponentRecordFile,
  directory: Path,
) {
  Files.createDirectories(directory)
  val exportRecord =
    record
      .newBuilder()
      .apply { components = record.components + typed.builtinAdapters.map { it.component } }
      .build()
  val textRecord = typed.text.component.canonicalId
  for (component in exportRecord.components) {
    for (explicit in listOf(false, true)) {
      val name = component.symbol.name + if (explicit) "ExplicitExport" else "DefaultExport"
      val arguments =
        if (!explicit) emptyMap()
        else
          component.parameters
            .filter {
              it.typeFqn in setOf("kotlin.Boolean", "kotlin.String", "kotlin.Float", "kotlin.Int")
            }
            .associate { parameter ->
              parameter.name to
                when (parameter.typeFqn) {
                  "kotlin.Boolean" -> ScreenValue.Bool(true)
                  "kotlin.String" ->
                    ScreenValue.Text(if (parameter.name == "text") "Typed Wear" else "")
                  "kotlin.Float" -> ScreenValue.Fractional32(1f)
                  else -> ScreenValue.Whole(1)
                }
            }
      val slots =
        if (!explicit) emptyMap()
        else
          component.parameters
            .filter { it.composableSlot }
            .associate {
              it.name to
                listOf(ScreenNode(textRecord, mapOf("text" to ScreenValue.Text("Slot content"))))
            }
      val result =
        ScreenGenerator.generate(
          ScreenDocument(
            name,
            ScreenNode(component.canonicalId, arguments = arguments, slots = slots),
          ),
          exportRecord,
          packageName = "ee.schimke.wearm3catalog.typed.consumer",
        )
      check(result is ScreenGenerator.Result.Emitted) { "Source fixture refused: $result" }
      Files.writeString(directory.resolve("$name.kt"), result.source)
    }
  }
  for (component in record.components.filter { it.builder?.stateCallbacks?.isNotEmpty() == true }) {
    val binding = component.builder!!.stateCallbacks.single()
    val property = binding.value.substringBefore(':')
    val parameter = component.parameters.single { it.name == property }
    val type = requireNotNull(parameter.typeFqn)
    val initial =
      when (type) {
        "kotlin.Boolean" -> ScreenValue.Bool(false)
        "kotlin.Float" -> ScreenValue.Fractional32(0.5f)
        else -> error("No state fixture for $type")
      }
    val name = component.symbol.name + "StateExport"
    val result =
      ScreenGenerator.generate(
        ScreenDocument(
          name,
          ScreenNode(
            component.canonicalId,
            arguments = mapOf(property to ScreenValue.StateRead("selection", type)),
          ),
          state = listOf(ScreenState("selection", type, initial)),
        ),
        exportRecord,
        packageName = "ee.schimke.wearm3catalog.typed.consumer",
      )
    check(result is ScreenGenerator.Result.Emitted) { "State fixture refused: $result" }
    // These prove typed state reads compile. The generic generator does not yet project payload
    // callback write-back, so readiness keeps the production source-export gate open.
    Files.writeString(directory.resolve("$name.kt"), result.source)
  }
}

/** What the pilot covers and what still blocks a production switch, stated rather than inferred. */
private fun writeReadiness(
  root: Path,
  base: UiBuilderPolicyFile,
  typed: TypedWearCatalog,
  catalog: UiBuilderCatalogFile,
  output: Path,
) {
  val covered = typed.adapters.map { it.id }.toSet()
  val builtinCovered = typed.builtinAdapters.map { it.id }.toSet()
  val structural = base.builtins.filterValues { it.canvas in structuralAdapterIds }.keys
  val remainingComponents =
    base.components.filterValues { it.excluded.isNullOrBlank() }.keys - covered
  val remainingBuiltins = base.builtins.keys - builtinCovered - structural
  val compatibilityChanges =
    covered
      .sorted()
      .associateWith { id ->
        val old = requireNotNull(base.components[id])
        val new = catalog.statusSemantics.components.getValue(id)
        buildList {
          if (old.canvas != new.canvas) add("canvas")
          if (old.modifierCapabilities != new.modifierCapabilities) add("modifierCapabilities")
          if (old.propertyCapabilities != new.propertyCapabilities) add("propertyCapabilities")
          if (old.slotCapabilities != new.slotCapabilities) add("slotCapabilities")
        }
      }
      .filterValues { it.isNotEmpty() }
  val unverified =
    listOf(
      "Packaged browser runtime exercised through the sandbox host",
      "Production source export with state/event write-back",
      "Production delivery of the typed pair and runtime",
    )
  val templateCoverage =
    base.templates.associate { template ->
      val document =
        json.parseToJsonElement(Files.readString(root.resolve(template.path))).jsonObject
      val referenced =
        document
          .getValue("nodes")
          .jsonObject
          .values
          .map { it.jsonObject.getValue("componentId").jsonPrimitive.content }
          .toSet()
      template.path to
        buildJsonObject {
          put(
            "uncoveredComponents",
            JsonArray(
              (referenced - covered - builtinCovered - structural)
                .filterNot { it.startsWith("layout/") }
                .sorted()
                .map(::JsonPrimitive)
            ),
          )
        }
    }
  val report = buildJsonObject {
    put("catalogId", TYPED_WEAR_CATALOG_ID)
    put(
      "readyForCutover",
      remainingComponents.isEmpty() &&
        remainingBuiltins.isEmpty() &&
        compatibilityChanges.isEmpty() &&
        unverified.isEmpty(),
    )
    put("typedComponents", JsonArray(covered.sorted().map(::JsonPrimitive)))
    put("typedBuiltins", JsonArray(builtinCovered.sorted().map(::JsonPrimitive)))
    put("productionStructureAdapters", JsonArray(structural.sorted().map(::JsonPrimitive)))
    put("remainingComponents", JsonArray(remainingComponents.sorted().map(::JsonPrimitive)))
    put("remainingBuiltins", JsonArray(remainingBuiltins.sorted().map(::JsonPrimitive)))
    put(
      "compatibilityChanges",
      JsonObject(compatibilityChanges.mapValues { (_, f) -> JsonArray(f.map(::JsonPrimitive)) }),
    )
    put(
      "knownDifferences",
      JsonArray(
        listOf(
            "wear-m3/text letterSpacingSp: an authored 0 is read as unset (the style's own spacing)",
            "Selection-button labels draw the label string; production also accepts a label slot",
          )
          .map(::JsonPrimitive)
      ),
    )
    put("unverifiedGates", JsonArray(unverified.map(::JsonPrimitive)))
    put("templateCoverage", JsonObject(templateCoverage))
  }
  Files.writeString(output, json.encodeToString(JsonObject.serializer(), report) + "\n")
}
