package ee.schimke.wearm3catalog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * What `ScreenScaffold` hands its list as `contentPadding`, at each round screen size.
 *
 * ## Why this repository owns a test about somebody else's builder
 *
 * The Compose UI builder in yschimke/compose-preview-server draws Wear screens with a stand-in —
 * its canvas is Compose Multiplatform for Wasm, which cannot link an Android AAR, so it cannot call
 * `ScreenScaffold` at all. To draw the right picture it has to know what the real scaffold would
 * have done, and it now carries this table as constants.
 *
 * A number copied out of a render is a number that goes stale silently. This is the assertion that
 * makes it fail loudly instead: it composes the real `ScreenScaffold` over a real
 * `TransformingLazyColumn` and reads the padding the library computes. When a Wear Compose bump
 * changes it, this test says so, and the builder's table is what needs updating.
 *
 * The vertical numbers are cross-checked by the `ScrollMode.LONG` render of [WearList]: bottom
 * padding on the stitched capture is 20dp at 192, 23dp at 225 and 24dp at 240, which is this table.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ScreenScaffoldContentPaddingTest {

  @get:Rule val rule = createComposeRule()

  @Test
  @Config(qualifiers = "w192dp-h192dp-round-xhdpi")
  fun `small round is 10dp by 20dp`() = assertPadding(horizontal = 10.dp, vertical = 20.dp)

  @Test
  @Config(qualifiers = "w227dp-h227dp-round-xhdpi")
  fun `large round is 12dp by 23dp`() = assertPadding(horizontal = 12.dp, vertical = 23.dp)

  @Test
  @Config(qualifiers = "w240dp-h240dp-round-xhdpi")
  fun `xl round is 13dp by 24dp`() = assertPadding(horizontal = 13.dp, vertical = 24.dp)

  private fun assertPadding(horizontal: Dp, vertical: Dp) {
    lateinit var padding: PaddingValues
    var start = Dp.Unspecified
    var end = Dp.Unspecified
    rule.setContent {
      val direction = LocalLayoutDirection.current
      ScreenSticker {
        val state = rememberTransformingLazyColumnState()
        ScreenScaffold(scrollState = state) { contentPadding ->
          padding = contentPadding
          start = contentPadding.calculateStartPadding(direction)
          end = contentPadding.calculateEndPadding(direction)
          TransformingLazyColumn(
            state = state,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
          ) {}
        }
      }
    }
    rule.waitForIdle()

    assertEquals("start", horizontal, start)
    assertEquals("end", horizontal, end)
    assertEquals("top", vertical, padding.calculateTopPadding())
    assertEquals("bottom", vertical, padding.calculateBottomPadding())
  }
}
