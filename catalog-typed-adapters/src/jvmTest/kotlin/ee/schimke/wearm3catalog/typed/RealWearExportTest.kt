package ee.schimke.wearm3catalog.typed

import ee.schimke.catalog.typed.wearM3Policy
import ee.schimke.composeai.discovery.UiBuilderCatalogFile
import ee.schimke.composeai.uibuilder.export.CatalogExportProjections
import ee.schimke.composeai.uibuilder.export.CatalogScreenStructure
import ee.schimke.composeai.uibuilder.export.ScreenExportGate
import ee.schimke.composeai.uibuilder.protocol.DesignDocumentV1
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * The pilot pair, exported end to end the way the editor exports it: the projections and `code`
 * block it publishes, read back by compose-ui-builder's own readers, and every fixture design
 * through `ScreenExportGate`.
 */
class RealWearExportTest {
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  private val pair =
    TypedWearCatalog(discoverTypedWearControls(), RealWearRecords.publication())
      .generate(wearM3Policy())

  private val semantics =
    json
      .encodeToJsonElement(UiBuilderCatalogFile.serializer(), pair.catalog)
      .jsonObject
      .getValue("statusSemantics")
      .jsonObject

  private val root =
    generateSequence(File(".").absoluteFile) { it.parentFile }
      .first { File(it, "ui-builder/designs").isDirectory }

  /**
   * The record as a host serves it: each published component's id joined to the record entry its
   * policy names (`PublishedUiBuilderCatalog` does the same), so a design's ids resolve.
   */
  private val record =
    pair.record
      .newBuilder()
      .also { b ->
        val ids =
          pair.catalog.statusSemantics.components.entries
            .mapNotNull { (id, policy) -> policy.record?.let { it to id } }
            .groupBy({ it.first }, { it.second })
        b.components =
          pair.record.components.map { component ->
            val joined = ids[component.canonicalId].orEmpty()
            if (joined.isEmpty()) component
            else
              component
                .newBuilder()
                .also { it.componentIds = (component.componentIds + joined).distinct() }
                .build()
          }
      }
      .build()

  private fun export(name: String): ScreenExportGate.Outcome =
    ScreenExportGate.export(
      json.decodeFromString<DesignDocumentV1>(File(root, "ui-builder/designs/$name").readText()),
      record,
      CatalogExportProjections.from(semantics),
      CatalogScreenStructure.from(semantics),
    )

  private fun emitted(name: String): String {
    val outcome = export(name)
    return assertIs<ScreenExportGate.Outcome.Emitted>(outcome, "$name: $outcome").source
  }

  private fun refusals(name: String): List<String> =
    assertIs<ScreenExportGate.Outcome.Refused>(export(name), name).reasons

  @Test
  fun `a list screen exports as the frame, the real list and its transformed rows`() {
    val source = emitted("samples/jetcaster-wear-library.json")
    for (expected in
      listOf(
        "val listState = rememberTransformingLazyColumnState()",
        "val spec = rememberTransformationSpec()",
        "ScreenScaffold(scrollState = listState) { contentPadding ->",
        "TransformingLazyColumn(",
        "ListHeader(modifier = Modifier.transformedHeight(this, spec)",
        "transformation = SurfaceTransformation(spec)",
        "FilledTonalButton(onClick = {}",
        "ButtonDefaults.minimumVerticalListContentPadding",
        "Icon(imageVector = Icons.Filled.Podcasts",
        "import androidx.compose.material.icons.filled.Podcasts",
        "Text(text = \"Up Next\")",
      )) {
      assertContains(source, expected)
    }
  }

  @Test
  fun `an edge button exports into the scaffold's edge button frame`() {
    assertContains(
      emitted("samples/wear-starter-greeting.json"),
      "    ScreenScaffold(\n" +
        "        scrollState = listState,\n" +
        "        edgeButton = {\n" +
        "            EdgeButton(onClick = {}, buttonSize = EdgeButtonSize.ExtraSmall) {\n" +
        "                Text(text = \"Show list\")\n" +
        "            }\n" +
        "        },\n" +
        "    ) { contentPadding ->\n",
    )
  }

  @Test
  fun `the screen fixture exports through the frame`() {
    assertContains(emitted("wear-screen.json"), "ScreenScaffold(scrollState = listState)")
  }

  /**
   * What is left is named, never dropped: components with no real definition yet, and the frame's
   * holes. Each line here is port work; a definition that lands moves its fixture up.
   */
  @Test
  fun `the rest is refused by name`() {
    assertTrue(refusals("wear-list.json").any { "no component `layout/column`" in it })
    assertTrue(refusals("samples/wear-starter-list.json").single().contains("`overlays` slot"))
    for (name in
      listOf("samples/jetcaster-wear-queue.json", "samples/jetcaster-wear-episode.json")) {
      val reasons = refusals(name)
      assertTrue(reasons.none { "Material icon" in it }, "$name: $reasons")
    }
  }
}
