package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import ee.schimke.composeai.uibuilder.RecordFreeExport
import ee.schimke.composeai.uibuilder.UiBuilderDocument
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Every template design this catalog declares → generated Kotlin → **compiled against this module's
 * own classpath**.
 *
 * [WidgetExportRoundTripTest] proves the round trip for one hand-built design; this proves it for
 * the four `templates` entries in `ui-builder.policy.json` — the documents a new design opens as,
 * transcribed from compose-ui-builder's Kotlin seed builders. A template is the one document nobody
 * authored, so nothing else catches a property the catalog does not declare or an emitter
 * regression a seed needs: this does, at the same gate — the goldens beside this file are Kotlin in
 * the unit-test source set, so `compileDebugUnitTestKotlin` builds them against remote-material3,
 * the Remote Compose creation DSL and Glance Wear, the same artifacts the stickers use.
 *
 * The generated widget previews fan out over the three host corner shapes
 * `androidx.glance.wear.tooling.preview` publishes — `SquircleSmallWidgetPreviewParams` (the Pixel
 * Watch's 26dp squircle), `RoundSmallWidgetPreviewParams` (the Galaxy Watch's fully-round host) and
 * the rectangular picker asset — so each golden is also the corner pair the sheet draws as
 * WidgetContainer stickers.
 *
 * **When this fails after an exporter upgrade**, regenerate rather than hand-edit: run with
 * `-PwriteGolden=true` and read the diff. A green compile on the new text is the review.
 */
@RunWith(Parameterized::class)
class WidgetTemplateRoundTripTest(private val templateId: String) {

  companion object {
    /**
     * The `templates` entries of remote-catalog/ui-builder.policy.json, one parameter per design.
     */
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun templates(): List<String> =
      listOf("wear-widget-small", "wear-widget-large", "hello-widget", "weather-widget")

    private val designsDir = "remote-catalog/ui-builder/designs"

    private val json = Json { ignoreUnknownKeys = true }
  }

  private val goldenPath =
    "remote-catalog/src/test/kotlin/ee/schimke/wearm3catalog/remote/generated/" +
      "WidgetTemplate_$templateId.kt"

  /**
   * One sub-package per template. The widget class is named from the design's title before the '·'
   * (`WearWidgetCodeExporter.widgetIdentifier`), and the two host frames share the "Wear widget"
   * prefix — both become `WearWidget`, so a single package would collide. Separate designs are
   * separate files wherever else they land; this is the same shape.
   */
  private val packageName =
    "ee.schimke.wearm3catalog.remote.generated.${templateId.replace("-", "")}"

  private fun document(): UiBuilderDocument =
    json.decodeFromString(
      UiBuilderDocument.serializer(),
      File(repositoryRoot(), "$designsDir/$templateId.json").readText(),
    )

  private fun generated(): String {
    val generated = RecordFreeExport.generate(document(), packageName = packageName)
    assertThat(generated).isInstanceOf(RecordFreeExport.Generated.Emitted::class.java)
    return (generated as RecordFreeExport.Generated.Emitted).source
  }

  private fun repositoryRoot(): File {
    var directory: File? = File(".").absoluteFile
    while (directory != null) {
      if (File(directory, designsDir).isDirectory) return directory
      directory = directory.parentFile
    }
    error("could not find $designsDir from ${File(".").absolutePath}")
  }

  @Test
  fun `the template generates the Kotlin this module compiles`() {
    val source = generated()
    val golden = File(repositoryRoot(), goldenPath)
    if (System.getProperty("writeGolden") == "true") {
      golden.writeText(source)
      return
    }
    assertThat(source).isEqualTo(golden.readText())
  }

  /**
   * The design's own values reach the source — the round-trip half that makes this a check on the
   * document rather than a shape test. What is asserted per template is what the template is FOR:
   * the host frames name their container, and the two samples name the strings and colours of the
   * wear-os-samples widgets they reproduce.
   */
  @Test
  fun `the design's values reach the generated source`() {
    val source = generated()
    // The host scaffold is erased from the generated widget — `WearWidgetCodeExporter`'s
    // host-frame erasure — so what proves the size is the preview params family the widget
    // renders through: Small on the 216×76 host, Large on the 216×124 one.
    when (templateId) {
      "wear-widget-small" -> {
        assertThat(source).contains("WearWidgetDocument(background = WearWidgetBrush)")
        assertThat(source).contains("SquircleSmallWidgetPreviewParams")
      }
      "wear-widget-large" -> {
        assertThat(source).contains("WearWidgetDocument(background = WearWidgetBrush)")
        assertThat(source).contains("SquircleLargeWidgetPreviewParams")
      }
      "hello-widget" -> {
        // The widget's own background is the scaffold's, painted by the host as the round rect —
        // `colorScheme.primary`, with `onPrimary` on the text.
        assertThat(source).contains("WearWidgetBrush.color(colorScheme.primary)")
        assertThat(source).contains("color = RemoteMaterialTheme.colorScheme.onPrimary")
        assertThat(source).contains("text = \"Hello, World!\".rs")
      }
      "weather-widget" -> {
        // ColorSunny from the sample's WeatherWidget.kt, as a literal: the widget picks it by
        // weather, not by theme.
        assertThat(source).contains("WearWidgetBrush.color(Color(0xFF2196F3).rc)")
        assertThat(source).contains("text = \"London\".rs")
        assertThat(source).contains("text = \"75° ☀️\".rs")
      }
    }
  }

  /**
   * Every template's generated widget carries the three host corner previews, including the two a
   * Wear widget is actually checked on: the Pixel Watch squircle and the Samsung round host. The
   * preview names come from the emitter, and a widget template that stopped generating them would
   * hand a designer source with no way to see either corner.
   */
  @Test
  fun `the generated widget previews both host corner shapes`() {
    val source = generated()
    assertThat(source).contains("Squircle")
    assertThat(source).contains("Round")
    assertThat(source).contains("WearWidgetPreview")
  }
}
