package ee.schimke.wearm3catalog.uitemplate

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.schimke.composeai.uibuilder.RecordFreeExport
import ee.schimke.composeai.uibuilder.protocol.DesignDocumentV1
import ee.schimke.wearm3catalog.uitemplate.generated.samples.JetcasterEpisodeScreen
import ee.schimke.wearm3catalog.uitemplate.generated.samples.JetcasterLibraryScreen
import ee.schimke.wearm3catalog.uitemplate.generated.samples.JetcasterQueueScreen
import ee.schimke.wearm3catalog.uitemplate.generated.samples.StarterGreetingScreen
import ee.schimke.wearm3catalog.uitemplate.generated.samples.StarterListScreen
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The upstream Wear sample screens, as UI builder designs: document → generated Kotlin → **compiled
 * and rendered by real Wear Compose Material 3**.
 *
 * `ui-builder/designs/samples/` holds five designs transcribed from Android's own samples — the
 * ComposeStarter greeting and list screens from `android/wear-os-samples`, and Jetcaster's
 * library, episode and queue screens from `android/compose-samples` — authored against the
 * `wear-m3` catalog in compose-ui-builder, where the canvas and the device previews draw them
 * through the CMP port. This is the other lane: the same documents through the published exporter,
 * the output checked in under `generated/samples/` so `compileDebugUnitTestKotlin` builds it
 * against the AndroidX library the stickers are drawn with, and each screen composed on the two
 * round sizes a `wear-m3` design exports to.
 *
 * Unlike [WearScreenTemplateRoundTripTest] these are not templates: nothing seeds a new design
 * from them, and `ui-builder.policy.json` does not list them. They are worked samples, and a check
 * that the builder can express a real app's screens well enough for the result to build and draw.
 *
 * The captures land in `build/ui-builder-samples/` — the native render, the picture the canvas
 * approximates. The assertion on them is only that something was drawn beyond the black ground;
 * whether it is the *right* picture is a comparison against the upstream sample, recorded with the
 * designs' write-up in compose-ui-builder rather than as a tuned threshold here.
 *
 * **One deliberate difference from the compose-ui-builder fixtures:** the ComposeStarter greeting and
 * card body are two- and three-line strings upstream, and here each line break is a space. The
 * published exporter this module pins writes a line break into a `"…"` literal unescaped, which
 * does not compile; compose-ui-builder escapes it now (`WearScreenCodeExporter.quoted`), and these
 * documents take the line breaks back when `composePreviewServer` moves to a release carrying it.
 *
 * **When the golden check fails after an exporter upgrade**, regenerate rather than hand-edit:
 * `-PwriteGolden=true`, then read the diff. A green compile on the new text is the review.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WearSampleDesignRoundTripTest {

  @get:Rule val rule = createComposeRule()

  private val packageName = "ee.schimke.wearm3catalog.uitemplate.generated.samples"

  /** Each design, the file its golden lives in, and the composable that golden declares. */
  private val samples: List<Triple<String, String, @Composable () -> Unit>> =
    listOf(
      Triple("wear-starter-greeting", "StarterGreeting.kt") { StarterGreetingScreen() },
      Triple("wear-starter-list", "StarterList.kt") { StarterListScreen() },
      Triple("jetcaster-wear-library", "JetcasterLibrary.kt") { JetcasterLibraryScreen() },
      Triple("jetcaster-wear-episode", "JetcasterEpisode.kt") { JetcasterEpisodeScreen() },
      Triple("jetcaster-wear-queue", "JetcasterQueue.kt") { JetcasterQueueScreen() },
    )

  @Test
  fun `every sample design generates the Kotlin this module compiles`() {
    for ((design, goldenName, _) in samples) {
      val generated = RecordFreeExport.generate(document(design), packageName = packageName)
      assertTrue(
        "$design: expected generated source, got $generated",
        generated is RecordFreeExport.Generated.Emitted,
      )
      val source = (generated as RecordFreeExport.Generated.Emitted).source
      val golden =
        File(
          repositoryRoot(),
          "catalog/src/androidHostTest/kotlin/ee/schimke/wearm3catalog/uitemplate/generated/" +
            "samples/$goldenName",
        )
      if (System.getProperty("writeGolden") == "true") {
        golden.parentFile.mkdirs()
        golden.writeText(source)
        continue
      }
      assertTrue("$design: no golden at ${golden.path}", golden.isFile)
      assertEquals(
        "$design: the exporter no longer produces the checked-in source",
        golden.readText(),
        source,
      )
    }
  }

  @Test
  @Config(qualifiers = "w192dp-h192dp-round-xhdpi")
  fun `every sample renders on the small round`() = renderAll("small-round")

  @Test
  @Config(qualifiers = "w240dp-h240dp-round-xhdpi")
  fun `every sample renders on the large round`() = renderAll("large-round")

  /**
   * One composition, switched between the samples: a compose rule takes `setContent` once, and a
   * fresh test per sample per size would be ten Robolectric environments for ten screenshots.
   */
  private fun renderAll(device: String) {
    val current = mutableIntStateOf(0)
    rule.setContent { key(current.intValue) { samples[current.intValue].third() } }
    val out = File(repositoryRoot(), "catalog/build/ui-builder-samples").apply { mkdirs() }
    for ((i, sample) in samples.withIndex()) {
      current.intValue = i
      rule.mainClock.advanceTimeBy(2_000)
      rule.waitForIdle()
      val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
      File(out, "${sample.first}-$device.png").outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
      }
      assertTrue(
        "${sample.first} on $device drew nothing but the ground",
        litFraction(bitmap) > MINIMUM_LIT_FRACTION,
      )
    }
  }

  /** The share of pixels brighter than the black a Wear screen is drawn on. */
  private fun litFraction(bitmap: Bitmap): Double {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val lit =
      pixels.count { argb ->
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        r + g + b > 48
      }
    return lit.toDouble() / pixels.size
  }

  private fun document(design: String): DesignDocumentV1 =
    Json { ignoreUnknownKeys = true }
      .decodeFromString(
        File(repositoryRoot(), "ui-builder/designs/samples/$design.json").readText()
      )

  private fun repositoryRoot(): File {
    var directory: File? = File(".").absoluteFile
    while (directory != null) {
      if (File(directory, "ui-builder/designs/samples").isDirectory) return directory
      directory = directory.parentFile
    }
    error("could not find ui-builder/designs/samples from ${File(".").absolutePath}")
  }

  private companion object {
    /** A header and one row on a round screen is several percent; a blank capture is zero. */
    const val MINIMUM_LIT_FRACTION = 0.01
  }
}
