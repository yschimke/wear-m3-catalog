package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import ee.schimke.composeai.uibuilder.RecordFreeExport
import ee.schimke.composeai.uibuilder.protocol.AnimationStateV1
import ee.schimke.composeai.uibuilder.protocol.CatalogReferenceV1
import ee.schimke.composeai.uibuilder.protocol.DesignDocumentV1
import ee.schimke.composeai.uibuilder.protocol.DesignEnvironmentV1
import ee.schimke.composeai.uibuilder.protocol.DesignNodeV1
import ee.schimke.composeai.uibuilder.protocol.LayoutDirectionV1
import ee.schimke.composeai.uibuilder.protocol.StringValueV1
import ee.schimke.composeai.uibuilder.protocol.ThemeV1
import ee.schimke.composeai.uibuilder.protocol.WindowPostureV1
import java.io.File
import org.junit.Test

/**
 * A widget design → generated Kotlin → **compiled against this module's own classpath**.
 *
 * The half that makes this worth having is the one with no assertion in it.
 * `WidgetRoundTripWidget.kt` beside this file is not a string fixture: it is Kotlin in the
 * unit-test source set, so `compileDebugUnitTestKotlin` builds it against remote-material3, the
 * Remote Compose creation DSL and Glance Wear — the same artifacts the stickers use. Generated
 * source that stops compiling fails this module rather than reaching a person as a paste that does
 * not build.
 *
 * That gate could not live in the repository that writes the source. compose-preview-server has no
 * Remote Compose dependency anywhere, deliberately — `RemoteContentEmitter` is text generation — so
 * it can assert what it wrote and never that the text is valid Kotlin. Its own export test says as
 * much: the golden there "was compiled" once, by hand, and turning that into a standing gate is
 * listed as a follow-up. This is that gate for the Remote lane, and
 * `UI_BUILDER_CATALOG_CONTRACT.md` phase 2 item 11 asks for it here for exactly this reason.
 *
 * The assertion below is the other half: the exporter still produces the text the compiler
 * accepted. Both are needed. A golden with no compile proves the generator is stable and not that
 * it is right; a compile with no golden proves a checked-in file builds and not that anything still
 * generates it.
 *
 * **When this fails after an exporter upgrade**, regenerate rather than hand-edit: run
 * `WidgetExportRoundTripTest` with `-PwriteGolden=true` and read the diff. A green compile on the
 * new text is the review.
 *
 * What it does NOT cover, so nobody reads more into it: the design uses `layout/column` and
 * `m3/text`, which are two of the eleven ids `RemoteContentEmitter` can write. The twenty-five
 * `remote-m3/` components this catalog publishes have no emitter case, and their cost is measured
 * in `PublishedRemoteM3CatalogEquivalenceTest` over in compose-preview-server — six Remote Compose
 * value types rather than twenty-five components. Every one of those, when written, belongs in a
 * design here and in this golden.
 */
class WidgetExportRoundTripTest {

  private val goldenPath =
    "remote-catalog/src/test/kotlin/ee/schimke/wearm3catalog/remote/generated/" +
      "WidgetRoundTripWidget.kt"

  private val packageName = "ee.schimke.wearm3catalog.remote.generated"

  /**
   * A widget rooted at the small host container, holding the two ids the emitter can write.
   *
   * The slot is `children` and not `content`: `RemoteContentEmitter.container` reads
   * `node.slots["children"]`, and a design that names the wrong slot emits `RemoteColumn()` with
   * everything inside it silently dropped — which is what the first draft of this test did, and the
   * reason the golden is a compiled file rather than a `contains` check.
   */
  private fun document() =
    DesignDocumentV1(
      schema = "compose-ui-builder-document/v1-candidate",
      id = "widget-round-trip",
      title = "Widget round trip",
      revision = 1,
      catalogPin =
        CatalogReferenceV1(
          systemId = "remote-m3",
          catalogRevision = "candidate",
          capabilityDigest = "fixture",
          nativeRuntimeId = "candidate",
        ),
      environment =
        DesignEnvironmentV1(
          widthDp = 216,
          heightDp = 76,
          density = 1.0,
          theme = ThemeV1.LIGHT,
          locale = "en-US",
          fontScale = 1.0,
          layoutDirection = LayoutDirectionV1.LTR,
          windowPosture = WindowPostureV1.FLAT,
          animations = AnimationStateV1.SETTLED,
          networkAccess = false,
        ),
      roots = listOf("host"),
      nodes =
        linkedMapOf(
          "host" to
            DesignNodeV1(
              id = "host",
              componentId = "remote-m3/widget-container-small",
              slots = mapOf("content" to listOf("column")),
            ),
          "column" to
            DesignNodeV1(
              id = "column",
              componentId = "layout/column",
              slots = mapOf("children" to listOf("title", "detail")),
            ),
          "title" to
            DesignNodeV1(
              id = "title",
              componentId = "m3/text",
              properties = mapOf("text" to StringValueV1("Next train")),
            ),
          "detail" to
            DesignNodeV1(
              id = "detail",
              componentId = "m3/text",
              properties = mapOf("text" to StringValueV1("09:24 to Tallinn")),
            ),
        ),
    )

  private fun repositoryRoot(): File {
    var directory: File? = File(".").absoluteFile
    while (directory != null) {
      if (File(directory, goldenPath).isFile) return directory
      directory = directory.parentFile
    }
    error("could not find $goldenPath from ${File(".").absolutePath}")
  }

  @Test
  fun `a widget design generates the Kotlin this module compiles`() {
    val generated = RecordFreeExport.generate(document(), packageName = packageName)
    assertThat(generated).isInstanceOf(RecordFreeExport.Generated.Emitted::class.java)
    val source = (generated as RecordFreeExport.Generated.Emitted).source

    val golden = File(repositoryRoot(), goldenPath)
    if (System.getProperty("writeGolden") == "true") {
      golden.writeText(source)
      return
    }
    assertThat(source).isEqualTo(golden.readText())
  }

  /**
   * The design's own text reaches the source, which is what makes this a round trip rather than a
   * shape check.
   *
   * `RemoteText(text = "Next train".rs)` is the whole mapping question in one line: a
   * `remote-material3` component takes a `RemoteString`, not a `String`, and `.rs` is how a design
   * value becomes one. The twenty-five components with no emitter case are blocked on exactly this
   * for five more types.
   */
  @Test
  fun `the design's values reach the generated source as Remote Compose values`() {
    val source =
      (RecordFreeExport.generate(document(), packageName = packageName)
          as RecordFreeExport.Generated.Emitted)
        .source
    assertThat(source).contains("RemoteText(text = \"Next train\".rs)")
    assertThat(source).contains("RemoteText(text = \"09:24 to Tallinn\".rs)")
    assertThat(source).contains("import androidx.wear.compose.remote.material3.RemoteText")
  }
}
