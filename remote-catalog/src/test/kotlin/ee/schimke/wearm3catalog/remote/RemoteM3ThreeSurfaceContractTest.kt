package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

/** Locks the ownership boundaries documented in `docs/design/REMOTE_M3_UI_BUILDER.md`. */
class RemoteM3ThreeSurfaceContractTest {

  private val root = repositoryRoot()

  @Test
  fun `policy selects catalog runtime for browser preview and Android for authority`() {
    val policy =
      Json.parseToJsonElement(File(root, "remote-catalog/ui-builder.policy.json").readText())
        .jsonObject
    val surfaces = policy.getValue("previewSurfaces").jsonObject

    assertThat(policy).doesNotContainKey("browserPreview")
    assertThat(surfaces.getValue("wasm").jsonObject.getValue("fidelity").jsonPrimitive.content)
      .isEqualTo("approximate")
    assertThat(surfaces.getValue("native").jsonObject.getValue("fidelity").jsonPrimitive.content)
      .isEqualTo("authoritative")
    assertThat(surfaces.getValue("native").jsonObject.getValue("backend").jsonPrimitive.content)
      .isEqualTo("android")
  }

  @Test
  fun `runtime routes authoring to stand-ins and device preview to RC playback`() {
    val runtime =
      File(
          root,
          "remote-catalog-ui-builder-renderer/src/wasmJsMain/kotlin/ee/schimke/" +
            "wearm3catalog/remoteuibuilder/Main.kt",
        )
        .readText()
    val device =
      File(
          root,
          "remote-catalog-ui-builder-renderer/src/commonMain/kotlin/ee/schimke/" +
            "wearm3catalog/remoteuibuilder/RemoteM3DevicePreview.kt",
        )
        .readText()

    assertThat(runtime).contains("UiBuilderRendererSurfaceModeV2.AUTHORING_UNROLLED")
    assertThat(runtime).contains("SemanticCanvas(")
    assertThat(runtime).contains("RemoteM3DevicePreview(")
    assertThat(device).contains("captureCommonRemoteDocument(")
    assertThat(device).contains("RcComposePlayer(")
  }

  @Test
  fun `authoring colour literals use the sRGB Color constructor`() {
    val wearAdapter =
      File(
          root,
          "ui-builder-wear-adapters/src/wasmJsMain/kotlin/ee/schimke/" +
            "wearm3catalog/uibuilder/WearTextAdapters.kt",
        )
        .readText()
    val materialAdapter =
      File(
          root,
          "ui-builder-material-adapters/src/wasmJsMain/kotlin/ee/schimke/" +
            "wearm3catalog/uibuilder/MaterialCanvasAdapters.kt",
        )
        .readText()

    // Color(ULong) treats the value as a packed wide-gamut colour whose low bits name a colour
    // space. UI Builder literals are ordinary ARGB values and must select Color(Long) instead.
    assertThat(wearAdapter).contains("private fun parseArgb(value: String): Long")
    assertThat(wearAdapter).doesNotContain("private fun parseArgb(value: String): ULong")
    assertThat(materialAdapter).contains("private fun parseMaterialArgb(value: String): Long")
    assertThat(materialAdapter)
      .doesNotContain("private fun parseMaterialArgb(value: String): ULong")
  }

  @Test
  fun `first Remote M3 adapter preserves button properties and content slot`() {
    val device =
      File(
          root,
          "remote-catalog-ui-builder-renderer/src/commonMain/kotlin/ee/schimke/" +
            "wearm3catalog/remoteuibuilder/RemoteM3DevicePreview.kt",
        )
        .readText()

    assertThat(device).contains("\"remote-m3/remote-button\"")
    assertThat(device).contains("node.boolean(\"enabled\", true)")
    assertThat(device).contains("entry.slot(\"content\")")
    assertThat(device).contains("Unsupported: ${'$'}{node.componentId}")
  }

  @Test
  fun `Golden Widget Remote components are discovered without orphaned policies`() {
    val build = File(root, "remote-catalog/build/compose-previews")
    val components =
      Json.parseToJsonElement(File(build, "components.json").readText())
        .jsonObject
        .getValue("components")
        .jsonArray
        .map { it.jsonObject.getValue("canonicalId").jsonPrimitive.content }
        .toSet()
    val uiBuilder = Json.parseToJsonElement(File(build, "ui-builder.json").readText()).jsonObject
    val published =
      uiBuilder.getValue("statusSemantics").jsonObject.getValue("components").jsonObject.keys
    val required =
      mapOf(
        "remote-m3/remote-text" to
          "remote-catalog/androidx.wear.compose.remote.material3.RemoteTextKt.RemoteText",
        "remote-m3/remote-button" to
          "remote-catalog/androidx.wear.compose.remote.material3.RemoteButtonKt.RemoteButton",
        "remote-m3/remote-compact-button" to
          "remote-catalog/androidx.wear.compose.remote.material3.RemoteButtonKt.RemoteCompactButton",
        "remote-m3/remote-circular-progress-indicator" to
          "remote-catalog/androidx.wear.compose.remote.material3.RemoteCircularProgressIndicatorKt." +
            "RemoteCircularProgressIndicator",
      )

    assertThat(components).containsAtLeastElementsIn(required.values)
    assertThat(published).containsAtLeastElementsIn(required.keys)

    val orphaned =
      uiBuilder
        .getValue("diagnostics")
        .jsonArray
        .map { it.jsonObject }
        .filter { it.getValue("code").jsonPrimitive.content == "component.policy.orphaned" }
        .map { it.getValue("subject").jsonPrimitive.content }
    assertThat(orphaned).containsNoneIn(required.keys)
  }

  @Test
  fun `device adapter preserves canonical layout and widget host properties`() {
    val device =
      File(
          root,
          "remote-catalog-ui-builder-renderer/src/commonMain/kotlin/ee/schimke/" +
            "wearm3catalog/remoteuibuilder/RemoteM3DevicePreview.kt",
        )
        .readText()

    // The host frame comes from the shape's spec, never from properties on the container.
    assertThat(device).contains("hostSpec?.cornerRadiusDp")
    assertThat(device).doesNotContain("number(\"cornerRadiusDp\")")
    assertThat(device).contains("RenderRootSlot(\"background\")")
    assertThat(device).contains("string(\"contentAlignment\")")
    assertThat(device).contains("string(\"horizontalAlignment\")")
    assertThat(device).contains("string(\"verticalAlignment\")")
    assertThat(device).contains("string(\"horizontalArrangement\")")
    assertThat(device).contains("string(\"verticalArrangement\")")
  }

  @Test
  fun `published CMP writer pin matches immutable port identity`() {
    val versions = File(root, "gradle/libs.versions.toml").readText()
    val published =
      Regex("""remote-compose-cmp\s*=\s*"([^"]+)"""").find(versions)?.groupValues?.get(1)
    val port =
      Json.parseToJsonElement(File(root, "vendor/remote-compose-upstream.json").readText())
        .jsonObject
    val expected =
      "${port.getValue("change").jsonPrimitive.content}-" +
        "ps${port.getValue("patchSet").jsonPrimitive.content}-" +
        "cmp${port.getValue("portRevision").jsonPrimitive.content.padStart(2, '0')}"

    assertThat(published).isEqualTo(expected)
    assertThat(versions).contains("rcEmbeddedPlayer = \"1.70.0\"")
  }

  private fun repositoryRoot(): File {
    var directory: File? = File(".").absoluteFile
    while (directory != null) {
      if (File(directory, "remote-catalog/ui-builder.policy.json").isFile) return directory
      directory = directory.parentFile
    }
    error("could not find repository root from ${File(".").absolutePath}")
  }
}
