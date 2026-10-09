package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A design's colour literal is an ordinary sRGB ARGB value.
 *
 * `parseArgb` must stay a `Long`: handed to `Color(ULong)`, the packed wide-gamut constructor, the
 * low six bits of `#RRGGBB` are read as a colour-space id, and the Wasm canvas crashes reading
 * `Color.colorSpace`. remote-m3-catalog draws its visual editor with these adapters too, so this is
 * the one guard for both canvases.
 */
@OptIn(ExperimentalTestApi::class)
class WearColorLiteralTest {
  private fun resolve(vararg literals: String): List<Color> {
    val resolved = mutableListOf<Color>()
    runDesktopComposeUiTest {
      setContent { literals.forEach { resolved += resolveWearColor(it) } }
      waitForIdle()
    }
    return resolved
  }

  @Test
  fun rgbAndArgbLiteralsAreSrgb() {
    val (rgb, argb) = resolve("#2BE4D3", "#802BE4D3")

    assertEquals(Color(red = 0x2B, green = 0xE4, blue = 0xD3), rgb)
    assertEquals(ColorSpaces.Srgb, rgb.colorSpace)
    assertEquals(Color(red = 0x2B, green = 0xE4, blue = 0xD3, alpha = 0x80), argb)
    assertEquals(ColorSpaces.Srgb, argb.colorSpace)
  }

  @Test
  fun malformedLiteralIsTransparentBlack() {
    val (malformed) = resolve("#12345")

    assertEquals(Color(0x00000000), malformed)
    assertEquals(ColorSpaces.Srgb, malformed.colorSpace)
  }
}
