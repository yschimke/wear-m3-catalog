package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.UiBuilderPolicyFile
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun main(args: Array<String>) {
  require(args.size == 1) { "Expected a task-owned output directory" }
  val output = Path.of(args.single())
  val json = Json { prettyPrint = true }
  authoredPolicies().forEach { authored ->
    val directory = output.resolve(authored.outputName)
    Files.createDirectories(directory)
    Files.writeString(
      directory.resolve("ui-builder.policy.json"),
      json.encodeToString(UiBuilderPolicyFile.serializer(), authored.policy) + "\n",
    )
    val readiness = buildJsonObject {
      put("readyForCutover", false)
      put("phase", "authored-policy")
      put("componentPolicies", authored.policy.components.size)
      put("builtins", authored.policy.builtins.size)
      put(
        "unverifiedGates",
        buildJsonArray {
          add("Compiled property-reference adapters and real renderer registration")
          add("Published catalog pair, source export and saved-design compatibility")
          add("Packaged runtime rendering, state, events and inspection")
        },
      )
    }
    Files.writeString(
      directory.resolve("readiness.json"),
      json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), readiness) + "\n",
    )
  }
}
