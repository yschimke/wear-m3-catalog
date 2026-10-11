package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.UiBuilderCatalogFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class GeneratedCatalogTest {
  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun `published pair preserves every discovered signature and template`() {
    val root = Path.of(System.getProperty("catalogRoot"))
    val output = Path.of(System.getProperty("generatedCatalogRoot"))
    catalogInputs().forEach { input ->
      val original = root.resolve(input.module).resolve("build/compose-previews/components.json")
      val record =
        json.decodeFromString(ComponentRecordFile.serializer(), Files.readString(original))
      val catalog =
        json.decodeFromString(
          UiBuilderCatalogFile.serializer(),
          Files.readString(output.resolve(input.policy.catalogId!!).resolve("ui-builder.json")),
        )
      val pair = output.resolve(catalog.catalog.id)
      assertTrue(record.components.isNotEmpty())
      assertEquals(Files.readString(original), Files.readString(pair.resolve("components.json")))
      assertEquals(
        record,
        json.decodeFromString(
          ComponentRecordFile.serializer(),
          Files.readString(pair.resolve("components.json")),
        ),
      )
      input.policy.templates.forEach {
        assertTrue(Files.isRegularFile(pair.resolve(it.path)), it.path)
      }
    }
  }

  @Test
  fun `missing discovery refuses generation before creating output`() {
    val root = Files.createTempDirectory("missing-discovery")
    val output = root.resolve("output")
    try {
      kotlin.test.assertFailsWith<IllegalStateException> { generateCatalogs(root, output) }
      kotlin.test.assertFalse(Files.exists(output))
    } finally {
      root.toFile().deleteRecursively()
    }
  }

  @Test
  fun `changed contract refuses publication and preserves the previous artifact`() {
    val original = Path.of(System.getProperty("catalogRoot"))
    val scratch = Files.createTempDirectory("changed-contract")
    val output = scratch.resolve("output")
    try {
      val input = catalogInputs().first()
      val discovery = scratch.resolve(input.module).resolve("build/compose-previews")
      Files.createDirectories(discovery)
      val spec = scratch.resolve(input.spec)
      Files.createDirectories(spec.parent)
      Files.copy(original.resolve(input.spec), spec)
      val originalDiscovery = original.resolve(input.module).resolve("build/compose-previews")
      Files.copy(originalDiscovery.resolve("components.json"), discovery.resolve("components.json"))
      val baseline =
        json.decodeFromString(
          UiBuilderCatalogFile.serializer(),
          Files.readString(originalDiscovery.resolve("ui-builder.json")),
        )
      val changed =
        baseline
          .newBuilder()
          .apply { catalog = catalog.newBuilder().apply { title = "Incompatible title" }.build() }
          .build()
      Files.writeString(
        discovery.resolve("ui-builder.json"),
        json.encodeToString(UiBuilderCatalogFile.serializer(), changed),
      )
      val previous = output.resolve(input.policy.catalogId!!)
      Files.createDirectories(previous)
      Files.writeString(previous.resolve("sentinel"), "previous validated pair")
      kotlin.test.assertFailsWith<IllegalStateException> { generateCatalogs(scratch, output) }
      assertEquals("previous validated pair", Files.readString(previous.resolve("sentinel")))
      kotlin.test.assertFalse(Files.exists(previous.resolve("ui-builder.json")))
    } finally {
      scratch.toFile().deleteRecursively()
    }
  }
}
