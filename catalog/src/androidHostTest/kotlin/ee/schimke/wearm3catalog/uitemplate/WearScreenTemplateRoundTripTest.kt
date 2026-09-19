package ee.schimke.wearm3catalog.uitemplate

import ee.schimke.composeai.uibuilder.RecordFreeExport
import ee.schimke.composeai.uibuilder.protocol.DesignDocumentV1
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every `wear-m3` template document → generated Kotlin → **compiled against this module's own
 * classpath**.
 *
 * ## The half with no assertion in it
 *
 * `catalog/src/androidHostTest/kotlin/ee/schimke/wearm3catalog/uitemplate/generated/` is not a
 * string fixture. It is Kotlin in the unit-test source set, so `compileDebugUnitTestKotlin` builds
 * it against Wear Compose Material 3 — the same artifacts the stickers use. The generated screen
 * names `AppScaffold`, `ScreenScaffold`, `TransformingLazyColumn`, `rememberTransformationSpec`,
 * `SurfaceTransformation` and `TimeText`, and nothing in the repository that *writes* it has any of
 * those on a classpath. That is the round trip this repository owes the seed templates it
 * publishes: a template whose generated Kotlin stops compiling fails this module rather than
 * reaching a person as a paste that does not build.
 *
 * The assertion below is the other half: the exporter still produces the text the compiler
 * accepted. Both are needed. A golden with no compile proves the generator is stable and not that
 * it is right; a compile with no golden proves a checked-in file builds and not that anything still
 * generates it.
 *
 * ## Where the documents come from
 *
 * `ui-builder/designs/wear-screen.json` and `ui-builder/designs/wear-list.json`, listed in
 * [`ui-builder.policy.json`](../../../../../../../ui-builder.policy.json)'s `templates`. They are
 * transcribed from compose-ui-builder's seed builders and authored here from then on, so this
 * repository owns what a new `wear-m3` design opens as. The build's `UiBuilderTemplateLookup`
 * resolves those paths and the design-artifacts pipeline copies them to the delivery branch beside
 * `ui-builder.json`, so this test reads exactly the document a new `wear-m3` design is seeded from.
 *
 * ## What it does not cover
 *
 * The `wear-list` rows and the scaffold's chrome are not compared to the kit here — that is
 * design-parity's job on the sticker sheet, and the compose-ai-tools harness's for the generated
 * output. This test is a compile and a generator-stability check, nothing about pixels.
 *
 * **When this fails after an exporter upgrade**, regenerate rather than hand-edit: run
 * `WearScreenTemplateRoundTripTest` with `-PwriteGolden=true` and read the diff. A green compile on
 * the new text is the review.
 */
class WearScreenTemplateRoundTripTest {

  private val templates = listOf("wear-screen", "wear-list")

  private val packageName = "ee.schimke.wearm3catalog.uitemplate.generated"

  /** The template's golden, named after the template rather than the composable it happens to
   * declare, so a title change is a diff inside the file and not a renamed path. */
  private fun goldenPath(template: String) =
    "catalog/src/androidHostTest/kotlin/ee/schimke/wearm3catalog/uitemplate/generated/" +
      when (template) {
        "wear-screen" -> "WearScreenTemplate.kt"
        "wear-list" -> "WearListTemplate.kt"
        else -> error("no golden for $template")
      }

  private fun document(template: String): DesignDocumentV1 =
    Json { ignoreUnknownKeys = true }
      .decodeFromString(File(repositoryRoot(), "ui-builder/designs/$template.json").readText())

  private fun repositoryRoot(): File {
    var directory: File? = File(".").absoluteFile
    while (directory != null) {
      if (File(directory, "ui-builder/designs/wear-screen.json").isFile) return directory
      directory = directory.parentFile
    }
    error("could not find ui-builder/designs from ${File(".").absolutePath}")
  }

  @Test
  fun `every wear-m3 template generates the Kotlin this module compiles`() {
    for (template in templates) {
      val generated = RecordFreeExport.generate(document(template), packageName = packageName)
      assertTrue(
        "$template: expected generated source, got $generated",
        generated is RecordFreeExport.Generated.Emitted,
      )
      val source = (generated as RecordFreeExport.Generated.Emitted).source
      val golden = File(repositoryRoot(), goldenPath(template))
      if (System.getProperty("writeGolden") == "true") {
        golden.parentFile.mkdirs()
        golden.writeText(source)
        continue
      }
      assertTrue("$template: no golden at ${golden.path}", golden.isFile)
      assertEquals("$template: the exporter no longer produces the checked-in source", golden.readText(), source)
    }
  }
}
