package ee.schimke.wearm3catalog.uid

import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.uibuilder.export.CatalogScreenStructure
import ee.schimke.composeai.uibuilder.export.ScreenExportGate
import ee.schimke.composeai.uibuilder.protocol.DesignDocumentV1
import java.io.File
import kotlin.system.exitProcess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val json = Json {
  ignoreUnknownKeys = true
  explicitNulls = false
}

/**
 * `<module dir> <output dir> <catalog discovery dir>`: exports every `stickers/<name>.uid`
 * document, and every variant cell its sibling `stickers/<name>.json` lists, to one Kotlin file per
 * component.
 *
 * A variant cell is a list of builder operations, spelled the way `ui_builder_apply` spells them
 * (`setProperty`, `removeNodeProperty`, `deleteNode`, `setModifiers`, `addNode`), plus
 * `setComponentId` and `setEventBindings` for a kit axis Compose spells as a sibling function with
 * its own callbacks (a split selection control). They are applied to the document before export, so
 * a cell is the same kind of edit a designer makes in the builder and the cell's Kotlin is exactly
 * what the builder would export for it.
 *
 * Each document is a component definition placed once. Only the components are written
 * (`exportComponents`), as `internal` functions whose parameters are the document's bindings and
 * its action-free events, so the hand-written shell supplies the catalog's live knobs and click
 * counters and the generated body stays free of catalog code.
 */
fun main(args: Array<String>) {
  val module = File(args[0])
  val out = File(args[1])
  val record = vocabulary(module, File(args[2]))
  val manifest = json.parseToJsonElement(module.resolve("stickers.json").readText()).jsonObject
  // The catalog's string resource accessor, so `resource` values export as its translated strings.
  val structure =
    CatalogScreenStructure(stringResources = manifest["stringResources"]?.jsonPrimitive?.content)
  out.deleteRecursively()
  val failures = mutableListOf<String>()
  val documents =
    module
      .resolve("stickers")
      .listFiles { f -> f.name.endsWith(".uid") }
      .orEmpty()
      .sortedBy { it.name }
  for (document in documents) {
    val file = "stickers/${document.name}"
    val base = json.parseToJsonElement(document.readText()).jsonObject
    val side = document.resolveSibling(document.nameWithoutExtension + ".json")
    val sticker =
      if (side.isFile) json.parseToJsonElement(side.readText()).jsonObject
      else JsonObject(emptyMap())
    val cells = buildMap {
      put(null, emptyList<JsonObject>())
      sticker["variants"]?.jsonObject?.forEach { (name, ops) ->
        put(name, ops.jsonArray.map { it.jsonObject })
      }
    }
    for ((cell, operations) in cells) {
      val title = listOfNotNull(base.string("title"), cell).joinToString(" ")
      val patched =
        operations
          .fold(base) { doc, op -> apply(doc, op) }
          .with("title", title)
          .let { suffixComponents(it, cell) }
          .let(::pruneArguments)
      val document = json.decodeFromJsonElement(DesignDocumentV1.serializer(), patched)
      // Only the components: the shell is the screen, and calls them with the catalog's knobs.
      when (
        val outcome = ScreenExportGate.exportComponents(document, record, emptyMap(), structure)
      ) {
        is ScreenExportGate.Outcome.Emitted -> {
          val name = functionName(outcome.source) ?: error("$file: no composable in the export")
          out.resolve("generated/uibuilder/$name.kt").apply {
            parentFile.mkdirs()
            writeText(outcome.source)
          }
          println("$file${cell?.let { " [$it]" }.orEmpty()} -> $name")
        }
        is ScreenExportGate.Outcome.Refused ->
          failures +=
            "$file${cell?.let { " [$it]" }.orEmpty()}: ${outcome.reasons.joinToString("; ")}"
      }
    }
  }
  if (failures.isNotEmpty()) {
    System.err.println("The builder refuses to export:\n" + failures.joinToString("\n"))
    exitProcess(1)
  }
}

/**
 * The vocabulary documents resolve against: the catalog's own published pair, so a sticker can use
 * exactly what the builder serves for `wear-m3` and nothing else.
 *
 * - `:catalog`'s discovered `components.json`, joined through its `ui-builder.json` policy: each
 *   declared component (`wear-m3/button`, `wear-m3/card`, …) is the record entry its policy names,
 *   answering to that id. This is the join the builder's server makes when it serves the pair.
 * - `builder-foundation-record.json`: the builder's own `layout/` and `asset/` builtins.
 * - `project-components.json`: components this module declares for its documents.
 */
private fun vocabulary(module: File, discovery: File): ComponentRecordFile {
  fun read(file: File) = json.decodeFromString(ComponentRecordFile.serializer(), file.readText())
  val published = read(discovery.resolve("components.json"))
  val byCanonicalId = published.components.associateBy { it.canonicalId }
  val policy =
    json
      .parseToJsonElement(discovery.resolve("ui-builder.json").readText())
      .jsonObject
      .getValue("statusSemantics")
      .jsonObject
      .getValue("components")
      .jsonObject
  val declared = policy.mapNotNull { (id, declaration) ->
    val entry = declaration.jsonObject
    if (entry["excluded"]?.jsonPrimitive?.contentOrNull != null) return@mapNotNull null
    val canonicalId = entry["record"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
    val component =
      byCanonicalId[canonicalId] ?: error("ui-builder.json declares $id over missing $canonicalId")
    component.newBuilder().also { it.componentIds = listOf(id) }.build()
  }
  val builder = read(module.resolve("builder-foundation-record.json")).components
  // `project-components.json` plus one `project-components-<group>.json` per migrated group.
  val project =
    module
      .listFiles { f -> f.name.startsWith("project-components") && f.name.endsWith(".json") }
      .orEmpty()
      .sortedBy { it.name }
      .flatMap { read(it).components }
  return published.newBuilder().also { it.components = declared + builder + project }.build()
}

/** The first component the exporter writes, which names the file. */
private fun functionName(source: String): String? =
  Regex("""^internal fun (\w+)\(""", RegexOption.MULTILINE).find(source)?.groupValues?.get(1)

/**
 * Each cell's components get the cell's name (`KitCard` → `KitCardElevated`): every cell is
 * generated into the same package, and a cell is a different body, not an argument.
 */
private fun suffixComponents(doc: JsonObject, cell: String?): JsonObject {
  if (cell == null) return doc
  val suffix = cell.split(' ').joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
  val components = doc["components"]?.jsonObject ?: return doc
  return doc.with(
    "components",
    JsonObject(
      components.mapValues { (_, c) ->
        c.jsonObject.with("name", c.jsonObject.string("name") + suffix)
      }
    ),
  )
}

/**
 * Drops each placement's arguments its component no longer reads, after a cell deleted the nodes
 * that bound them: the slot cell's card has no copy, so `KitCardSlot` takes none.
 */
private fun pruneArguments(doc: JsonObject): JsonObject {
  val nodes = doc.getValue("nodes").jsonObject
  val components = doc["components"]?.jsonObject ?: return doc
  val reads = components.mapValues { (_, c) ->
    descendants(nodes, c.jsonObject.string("root"))
      .flatMap { id -> nodes[id]?.jsonObject?.get("properties")?.jsonObject?.values.orEmpty() }
      .mapNotNull { v ->
        v.jsonObject.takeIf { it["type"]?.jsonPrimitive?.content == "binding" }?.string("value")
      }
      .toSet()
  }
  val pruned = nodes.mapValues { (_, n) ->
    val placement = n.jsonObject["component"]?.jsonObject ?: return@mapValues n
    val used = reads[placement.string("componentKey")] ?: return@mapValues n
    val arguments = placement["arguments"]?.jsonObject.orEmpty().filterKeys { it in used }
    n.jsonObject.with("component", placement.with("arguments", JsonObject(arguments)))
  }
  return doc.with("nodes", JsonObject(pruned))
}

private fun apply(doc: JsonObject, op: JsonObject): JsonObject {
  val nodes = doc.getValue("nodes").jsonObject
  fun node(id: String) = nodes[id]?.jsonObject ?: error("operation names unknown node $id")
  fun withNode(id: String, node: JsonObject) = doc.with("nodes", JsonObject(nodes + (id to node)))
  return when (op.string("type")) {
    "setProperty" -> {
      val id = op.string("nodeId")
      val n = node(id)
      val properties = n["properties"]?.jsonObject.orEmpty()
      withNode(
        id,
        n.with(
          "properties",
          JsonObject(properties + (op.string("property") to op.getValue("value"))),
        ),
      )
    }
    "removeNodeProperty" -> {
      val id = op.string("nodeId")
      val n = node(id)
      withNode(
        id,
        n.with(
          "properties",
          JsonObject(n["properties"]?.jsonObject.orEmpty() - op.string("property")),
        ),
      )
    }
    // The same node as another component of the same family (`m3/suggestion-chip` →
    // `m3/elevated-suggestion-chip`): the kit's variant axis that Compose spells as a function.
    "setComponentId" -> {
      val id = op.string("nodeId")
      withNode(id, node(id).with("componentId", op.getValue("componentId")))
    }
    // A new node (with its id) appended to a parent's slot, e.g. the kit's `Show icon=True`.
    "addNode" -> {
      val added = op.getValue("node").jsonObject
      val addedId = added.string("id")
      require(addedId !in nodes) { "addNode: node $addedId already exists" }
      val parentId = op.string("parentId")
      val parent = node(parentId)
      val slot = op.string("slot")
      val slots = parent["slots"]?.jsonObject.orEmpty()
      val children = slots[slot]?.jsonArray.orEmpty() + JsonPrimitive(addedId)
      val withParent = parent.with("slots", JsonObject(slots + (slot to JsonArray(children))))
      doc.with("nodes", JsonObject(nodes + (parentId to withParent) + (addedId to added)))
    }
    // The node's events, replaced: a split control's two tap targets are two callbacks
    // (`checkedChange` and `containerClick`) where the whole-row control has one.
    "setEventBindings" -> {
      val id = op.string("nodeId")
      withNode(id, node(id).with("eventBindings", op.getValue("eventBindings")))
    }
    "setModifiers" -> {
      val id = op.string("nodeId")
      withNode(id, node(id).with("modifiers", op.getValue("modifiers")))
    }
    "deleteNode" -> {
      val gone = descendants(nodes, op.string("nodeId"))
      val kept =
        (nodes - gone).mapValues { (_, n) ->
          val slots = n.jsonObject["slots"]?.jsonObject.orEmpty()
          n.jsonObject.with(
            "slots",
            JsonObject(
              slots.mapValues { (_, kids) ->
                JsonArray(kids.jsonArray.filter { it.jsonPrimitive.content !in gone })
              }
            ),
          )
        }
      doc.with("nodes", JsonObject(kept))
    }
    else -> error("unsupported variant operation ${op.string("type")}")
  }
}

private fun descendants(nodes: Map<String, JsonElement>, id: String): Set<String> =
  setOf(id) +
    nodes[id]
      ?.jsonObject
      ?.get("slots")
      ?.jsonObject
      .orEmpty()
      .values
      .flatMap { it.jsonArray }
      .flatMap { descendants(nodes, it.jsonPrimitive.content) }

private fun JsonObject.string(key: String): String = getValue(key).jsonPrimitive.content

private fun JsonObject.with(key: String, value: JsonElement): JsonObject =
  JsonObject(this + (key to value))

private fun JsonObject.with(key: String, value: String): JsonObject =
  with(key, JsonPrimitive(value))
