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
import java.io.File
import org.json.JSONObject
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
 * `TransformingLazyColumn` and reads the padding the library computes.
 *
 * ## The table is published now, and this test owns it
 *
 * The builder used to carry a hand-transcribed copy and this test only shouted at it from across a
 * repository boundary. Under the catalog contract
 * (https://github.com/yschimke/compose-preview-server/blob/main/docs/design/UI_BUILDER_CATALOG_CONTRACT.md)
 * the table is **published data**: `frame.geometry.contentPadding` in this repository's
 * `ui-builder.policy.json`, generated into `ui-builder.json` and read by whichever builder is
 * drawing. So the assertion goes both ways — the library's number against the expectation in the
 * test name, and the committed policy against the library's number — which makes a hand-edit of the
 * published file a failing test rather than a silent lie, and a Wear Compose bump a diff in exactly
 * one place.
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
  fun `small round is 10dp by 20dp`() =
    assertPadding(screenDp = 192, horizontal = 10.dp, vertical = 20.dp)

  @Test
  @Config(qualifiers = "w227dp-h227dp-round-xhdpi")
  fun `large round is 12dp by 23dp`() =
    assertPadding(screenDp = 227, horizontal = 12.dp, vertical = 23.dp)

  @Test
  @Config(qualifiers = "w240dp-h240dp-round-xhdpi")
  fun `xl round is 13dp by 24dp`() =
    assertPadding(screenDp = 240, horizontal = 13.dp, vertical = 24.dp)

  /**
   * The published table names exactly the sizes this probe measures, and nothing else.
   *
   * Without it a row could be added by hand for a size nobody composes — which is precisely the
   * failure mode the block was moved here to end, reintroduced one row at a time. Each row's VALUES
   * are checked by the size-qualified tests above; this checks the shape of the set.
   */
  @Test
  fun `the published table has a row per measured size and no others`() {
    val screens = (0 until publishedPadding.length()).map { publishedPadding.getJSONObject(it) }
    assertEquals(
      "ui-builder.policy.json frame.geometry.contentPadding screen sizes",
      listOf(192, 227, 240),
      screens.map { it.getInt("screenDp") },
    )
  }

  private fun assertPadding(screenDp: Int, horizontal: Dp, vertical: Dp) {
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

    // …and the published file against what the library just did. The direction matters: the
    // library is the authority and the committed row is the claim, so a Wear Compose bump fails
    // here with the new number in hand rather than somewhere downstream with none.
    val published =
      (0 until publishedPadding.length())
        .map { publishedPadding.getJSONObject(it) }
        .singleOrNull { it.getInt("screenDp") == screenDp }
    assertEquals(
      "ui-builder.policy.json has no contentPadding row for ${'$'}{screenDp}dp",
      true,
      published != null,
    )
    assertEquals(
      "ui-builder.policy.json horizontalDp at ${'$'}{screenDp}dp",
      horizontal.value.toDouble(),
      published!!.getDouble("horizontalDp"),
      0.0,
    )
    assertEquals(
      "ui-builder.policy.json verticalDp at ${'$'}{screenDp}dp",
      vertical.value.toDouble(),
      published.getDouble("verticalDp"),
      0.0,
    )
  }

  /**
   * `frame.geometry.contentPadding` as committed, read from the repository root.
   *
   * `File("..")` because a unit test runs with the module directory as its working directory, the
   * same way `CatalogKitCoverageTest` reaches `kit-sets.json`. The policy file sits beside
   * `catalog.spec.json` at the root because it describes the catalog, not the module.
   */
  private val publishedPadding
    get() =
      JSONObject(File("../ui-builder.policy.json").readText())
        .getJSONObject("frame")
        .getJSONObject("geometry")
        .getJSONArray("contentPadding")
}
