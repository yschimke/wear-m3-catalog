package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import ee.schimke.composeai.remotecompose.json.RemoteComposeJson
import ee.schimke.composeai.remotecompose.json.RemoteComposeJsonException
import java.io.File
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

/**
 * Every sticker this sheet publishes can be **read as text**, not only looked at.
 *
 * ## What this is the gate for
 *
 * `design-artifacts.yml` sets `rc-document-json: true` for the `remote-m3` job, so every document
 * under the published bundle's `ir/` tree gets a `documents/<id>.rc.json` beside it on the delivery
 * branch: the operation stream as text, which `git diff` reads directly.
 *
 * (Written that way round on purpose: an `ir/` glob in a KDoc opens a nested block comment, because
 * Kotlin's nest. It costs a compile to find out.)
 *
 * That export is deliberately **fail-soft** — a document it cannot project drops its file rather
 * than the publish — which is right for a delivery lane and useless as a guarantee. A sheet whose
 * documents silently stopped projecting would keep publishing, with `documents/` quietly emptying
 * out, and the first person to notice would be someone who went looking for a file that was not
 * there. This test is the hard end the soft lane needs: it fails the build here, on the module that
 * produces the documents, before anything is published at all.
 *
 * It is the same division `StickerBakeCoverageTest` beside it makes for blank captures — the differ
 * declines to score them, this module refuses to produce them.
 *
 * ## Why the repository-level question is worth a test at all
 *
 * This repo's whole premise is a three-way comparison: the Figma kit, the Wear Compose rendition,
 * and the Remote one. Two of those columns are pictures, and a picture diff cannot tell a moved
 * padding from a different antialiasing pass. The `.rc` bytes could always have answered that and
 * never have, because nothing could read them. `documents/` is what makes "did the component change
 * or did the player draw it differently" answerable from the branch — which is the question this
 * repository asks of its Remote sheet more than any other.
 *
 * ## What it reads
 *
 * `build/compose-previews/renders/<stem>.rc`, the IR sidecars the render step captures beside each
 * PNG. `renderBeforeUnitTests` in `build.gradle.kts` is what puts them there before this runs — the
 * same wiring `WidgetContainerIrCaptureTest` and `StickerBakeCoverageTest` depend on.
 */
class RemoteDocumentJsonExportTest {

  private val rendersDir = File("build/compose-previews/renders")

  private fun documents(): List<File> =
    rendersDir.listFiles { f -> f.isFile && f.extension == "rc" }.orEmpty().sortedBy { it.name }

  @Test
  fun `the render captured documents to project`() {
    // Without this every assertion below passes vacuously the moment the render stops emitting
    // `.rc` sidecars — which is exactly how a capture regression hides.
    // `CapturingWearWidgetPreview`
    // documents the same degradation for its own lane: "renders fine, emits no .rc", with a green
    // build.
    assertThat(documents()).isNotEmpty()
  }

  @Test
  fun `every captured document projects to json`() {
    val failures = mutableListOf<String>()

    for (document in documents()) {
      try {
        val projected = RemoteComposeJson.dumpToJsonObject(document.readBytes())

        // Not just "it did not throw". A projection is a wrong answer far more easily than it is a
        // failure, so assert it actually described a document: a header, and an operation stream
        // with a layout root in it. A sticker that projected to `{"header": …, "operations": []}`
        // would be a file on the delivery branch that reads like a document and contains nothing.
        val operations = projected["operations"] as? JsonArray
        if (operations.isNullOrEmpty()) {
          failures += "${document.name}: projected to no operations"
          continue
        }
        if (operations.none { it.typeName() == ROOT }) {
          failures += "${document.name}: projected without a $ROOT"
        }
      } catch (e: RemoteComposeJsonException) {
        failures += "${document.name}: ${e.message}"
      }
    }

    assertWithMessage(
        "Every sticker in this sheet must project to document JSON, because the delivery lane " +
          "that publishes documents/ is fail-soft and would drop these silently. A failure here " +
          "usually means the Remote Compose runtime this module renders with has moved ahead of " +
          "the codec on composePreviewCore — bump it, or pin the sheet back."
      )
      .that(failures)
      .isEmpty()
  }

  @Test
  fun `a projected document names its declared size`() {
    // The header is read from the `Header` operation rather than from the inflated document's
    // geometry, and that distinction is load-bearing enough to pin: nothing here runs a layout
    // pass, so `CoreDocument.getWidth()` would report 0 for every one of these. A `documents/` tree
    // full of `"width": 0` would be worse than no tree at all — it reads like a measurement.
    val sized =
      documents().count { document ->
        val header = RemoteComposeJson.header(document.readBytes())
        (header.width ?: 0) > 0 && (header.height ?: 0) > 0
      }

    assertWithMessage("stickers declaring a non-zero size").that(sized).isEqualTo(documents().size)
  }

  private fun kotlinx.serialization.json.JsonElement.typeName(): String? =
    ((this as? JsonObject) ?: return null).let { (it["type"] as? JsonPrimitive)?.content }

  private companion object {
    /** The operation every well-formed document's layout tree hangs off. */
    const val ROOT = "RootLayoutComponent"
  }
}
