package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

/** Guards the exact font family recorded into ordinary and widget Remote Compose documents. */
class RemoteTypographyIrCaptureTest {

  private val rendersDir = File("build/compose-previews/renders")

  @Test
  fun `material text names Roboto Flex instead of a device family`() {
    val stickers =
      listOf(
        "RemoteTextSticker_width_227dp_height_100dp_dpi_320",
        "WidgetContainerLargeRemote",
      )

    for (stem in stickers) {
      val rc = renderFile(rendersDir, stem, ext = "rc")
      assertThat(rc.exists()).isTrue()
      val document = rc.readBytes().toString(Charsets.UTF_8)
      assertThat(document).contains("google:Roboto Flex")
      assertThat(document).doesNotContain("roboto-flex")
    }
  }
}
