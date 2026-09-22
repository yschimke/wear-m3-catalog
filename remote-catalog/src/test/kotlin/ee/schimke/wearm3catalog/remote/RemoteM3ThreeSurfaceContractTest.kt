package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.serialization.json.Json
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
          "remote-catalog-ui-builder-renderer/src/wasmJsMain/kotlin/ee/schimke/" +
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
  fun `first Remote M3 adapter preserves button properties and content slot`() {
    val device =
      File(
          root,
          "remote-catalog-ui-builder-renderer/src/wasmJsMain/kotlin/ee/schimke/" +
            "wearm3catalog/remoteuibuilder/RemoteM3DevicePreview.kt",
        )
        .readText()

    assertThat(device).contains("\"remote-m3/remote-button\"")
    assertThat(device).contains("node.boolean(\"enabled\", true)")
    assertThat(device).contains("entry.slot(\"content\")")
    assertThat(device).contains("Unsupported: ${'$'}{node.componentId}")
  }

  @Test
  fun `device adapter preserves canonical layout and widget host properties`() {
    val device =
      File(
          root,
          "remote-catalog-ui-builder-renderer/src/wasmJsMain/kotlin/ee/schimke/" +
            "wearm3catalog/remoteuibuilder/RemoteM3DevicePreview.kt",
        )
        .readText()

    assertThat(device).contains("number(\"horizontalPaddingDp\")")
    assertThat(device).contains("number(\"verticalPaddingDp\")")
    assertThat(device).contains("number(\"cornerRadiusDp\")")
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
    assertThat(versions).contains("rcEmbeddedPlayer = \"1.69.0\"")
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
