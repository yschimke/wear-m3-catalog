package ee.schimke.wearm3catalog.typed

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun main(args: Array<String>) {
  require(args.size == 3) { "Expected task-owned assets, runtime output and runtime identity" }
  val assets = Path.of(args[0]).toFile()
  val output = Path.of(args[1]).toFile()
  val required =
    setOf(
      "index.html",
      "wearM3TypedCatalogRenderer.mjs",
      "wearM3TypedCatalogRenderer.wasm",
      "skiko.mjs",
      "skiko.wasm",
      "js-joda.esm.js",
    )
  check(required.all { assets.resolve(it).isFile }) {
    "Incomplete browser runtime: ${required.filterNot { assets.resolve(it).isFile }}"
  }
  output.deleteRecursively()
  assets.copyRecursively(output)
  val digest = MessageDigest.getInstance("SHA-256")
  output
    .walkTopDown()
    .filter { it.isFile }
    .map { it.relativeTo(output).invariantSeparatorsPath to it }
    .sortedBy { it.first }
    .forEach { (name, file) ->
      val bytes = file.readBytes()
      digest.update(name.toByteArray(Charsets.UTF_8))
      digest.update(0)
      digest.update(bytes.size.toString().toByteArray(Charsets.UTF_8))
      digest.update(0)
      digest.update(bytes)
    }
  val manifest = buildJsonObject {
    put("schema", "compose-ui-builder-runtime/v1")
    put("runtimeId", args[2])
    put("protocolVersion", 2)
    put("entrypoint", "index.html")
    put("integritySha256", digest.digest().joinToString("") { "%02x".format(it) })
  }
  Files.writeString(output.toPath().resolve("runtime-manifest.json"), manifest.toString() + "\n")
}
