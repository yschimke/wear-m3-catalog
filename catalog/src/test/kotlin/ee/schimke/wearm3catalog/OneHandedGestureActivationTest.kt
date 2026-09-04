package ee.schimke.wearm3catalog

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The one-handed gesture stickers' ACTIVATION path — the half that has no wrist behind it.
 *
 * Two failures this pins, both of which shipped and neither of which any existing test could see.
 * `CatalogRenderTest` reads baked PNGs, and a baked capture is exactly where a gesture does not
 * fire: the published pictures were correct while the live component did nothing at all.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w192dp-h192dp-round-xhdpi")
class OneHandedGestureActivationTest {

  @get:Rule val rule = createComposeRule()

  private val source =
    File("src/main/kotlin/ee/schimke/wearm3catalog/sections/OneHandedGestures.kt").readText()

  /**
   * THE BUG: `GestureHorizontalPages` and `GestureVerticalPages` drew an indicator for a gesture
   * that was registered nowhere — no `Modifier.oneHandedGesture` in either, so no `onGesture`
   * existed and the pager could never advance. A component announcing an action it cannot perform
   * renders perfectly, which is why this is a source assertion rather than a pixel one.
   */
  @Test
  fun `every gesture sticker registers a gesture`() {
    val stickers = Regex("""^fun (Gesture\w+)\(\)""", RegexOption.MULTILINE)
    val bodies = source.split(Regex("""(?=^@CatalogComponent\()""", RegexOption.MULTILINE)).drop(1)
    assertEquals("four gesture components", 4, bodies.size)
    for (body in bodies) {
      val name = stickers.find(body)?.groupValues?.get(1) ?: error("no sticker function:\n$body")
      assertTrue(
        "$name draws an indicator but calls no .oneHandedGesture(…), so nothing can fire it",
        body.contains(".oneHandedGesture("),
      )
      assertTrue(
        "$name registers a gesture but offers no GestureActivation, so a live reader cannot " +
          "reach it off a watch",
        body.contains("GestureActivation("),
      )
    }
  }

  /** In the baked lane the activation control does not exist — the published PNG is unchanged. */
  @Test
  fun `no activation button in inspection mode`() {
    rule.setContent {
      CompositionLocalProvider(LocalInspectionMode provides true) {
        GestureActivation(OneHandedGestureAction.Primary, onGesture = {})
      }
    }
    rule.onNodeWithText("Double pinch").assertDoesNotExist()
  }

  /** Off a watch, the button is there and pressing it runs the component's own `onGesture`. */
  @Test
  fun `the activation button fires the gesture`() {
    var fired = 0
    rule.setContent { GestureActivation(OneHandedGestureAction.Primary, onGesture = { fired++ }) }
    rule.onNodeWithText("Double pinch").assertIsDisplayed().performClick()
    rule.waitForIdle()
    assertEquals("pressing the stand-in runs onGesture exactly once", 1, fired)
  }

  /** The dismiss action names the wearer's gesture, not the API's word for it. */
  @Test
  fun `the dismiss action is labelled as the wrist turn`() {
    rule.setContent { GestureActivation(OneHandedGestureAction.Dismiss, onGesture = {}) }
    rule.onNodeWithText("Wrist turn").assertIsDisplayed()
  }
}
