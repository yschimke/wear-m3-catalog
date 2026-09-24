package ee.schimke.wearm3catalog.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import ee.schimke.composeai.rcplayer.compose.RcComposePlayer
import ee.schimke.composeai.rcplayer.compose.RcTypefaceLoader
import ee.schimke.composeai.rcplayer.compose.rcGoogleFontsTypefaceLoader
import ee.schimke.composeai.rcplayer.protocol.RcDocument
import ee.schimke.composeai.rcplayer.protocol.RcDocumentCodec
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The Compose Multiplatform player (`rc-player-compose`) replaying this sheet's documents **on
 * Android** — the platform the stickers are baked on, and until rc-players 1.72 the one form the
 * CMP player could not take. The Browser Preview (`:remote-catalog-ui-builder-renderer`) already
 * runs it on the JVM and in wasm; this is the fourth surface.
 *
 * Every capture's `.rc` sidecar is decoded and drawn by `RcComposePlayer` under Robolectric's
 * native graphics, at the baked PNG's size, with the document's `google:` families resolved by
 * `rcGoogleFontsTypefaceLoader` — the shared `composeai.fonts.cacheDir` cache when the render
 * provides one, else the GMS provider. The result is compared with the baked PNG (drawn by the
 * embedded player). The test fails only if the CMP player throws on a document; the pixel
 * comparison is reported, not gated, because the two are different interpreters and the comparison
 * is the point of the trial. `-PcmpTrialOut=<abs dir>` keeps every CMP render.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class CmpAndroidPlayerTrialTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val renders = File("build/compose-previews/renders")
  private var content by mutableStateOf<Pair<RcDocument, RcTypefaceLoader>?>(null)
  private var width by mutableStateOf(0)
  private var height by mutableStateOf(0)
  private var density by mutableStateOf(1f)

  @Test
  fun `the CMP player draws every sticker on Android`() {
    val captures =
      renders
        .listFiles { f -> f.name.endsWith(".rc") }
        .orEmpty()
        .sortedBy { it.name }
        .mapNotNull { rc ->
          File(rc.path.removeSuffix(".rc") + ".png").takeIf { it.isFile }?.let { rc to it }
        }
    assumeTrue(
      "no .rc sidecars under ${renders.absolutePath}; run the render first",
      captures.isNotEmpty(),
    )

    // The first frame after `setContent` under a paused clock draws nothing; warm the scene up on
    // the first capture so every measured render is a settled one.
    captures.first().let { (rc, png) ->
      val baked = BitmapFactory.decodeFile(png.path)
      val document = RcDocumentCodec.decode(rc.readBytes())
      render(
        document,
        baked.width,
        baked.height,
        documentDensity(png.name),
        RcTypefaceLoader.Default,
      )
    }
    val out = System.getProperty("cmpTrialOut")?.let { File(it).apply { mkdirs() } }
    val failures = mutableListOf<String>()
    val rows = mutableListOf<String>()
    for ((rc, png) in captures) {
      val baked = BitmapFactory.decodeFile(png.path) ?: continue
      val outcome = runCatching {
        val document = RcDocumentCodec.decode(rc.readBytes())
        render(
          document,
          baked.width,
          baked.height,
          documentDensity(png.name),
          rcGoogleFontsTypefaceLoader(document),
        )
      }
      val drawn = outcome.getOrNull()
      if (drawn == null) {
        failures += "${rc.name}: ${outcome.exceptionOrNull()}"
        continue
      }
      out?.let { dir ->
        File(dir, png.name).outputStream().use {
          drawn.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
      }
      rows += "%.4f\t%s".format(differingFraction(baked, drawn), readableStem(png.name))
    }
    val report = File("build/reports/cmp-android-player-trial.tsv")
    report.parentFile.mkdirs()
    report.writeText(
      "differing_fraction\tcapture\n" + rows.sortedDescending().joinToString("\n") + "\n"
    )
    println(
      "CMP Android trial: ${captures.size} captures, ${failures.size} failed, " +
        "${rows.count { it.substringBefore('\t').toDouble() <= 0.01 }} within 1% of the baked PNG; " +
        "report at ${report.absolutePath}"
    )
    assertTrue(
      "the CMP player threw on these documents:\n" + failures.joinToString("\n"),
      failures.isEmpty(),
    )
  }

  private fun render(
    document: RcDocument,
    w: Int,
    h: Int,
    documentDensity: Float,
    typefaces: RcTypefaceLoader,
  ): Bitmap {
    if (width == 0) {
      composeRule.setContent {
        // The baked PNG is at device pixels for the capture's `dpi_NNN`, so the document is laid
        // out at that density: the same dp, the same pixels.
        val scene = Density(density, 1f)
        CompositionLocalProvider(LocalDensity provides scene) {
          Box(Modifier.size(with(scene) { width.toDp() }, with(scene) { height.toDp() })) {
            content?.let { (doc, fonts) ->
              key(doc, fonts) {
                RcComposePlayer(doc, modifier = Modifier.fillMaxSize(), typefaces = fonts)
              }
            }
          }
        }
      }
    }
    width = w
    height = h
    density = documentDensity
    // Documents that animate forever never let Compose go idle; step a fixed 100 ms instead.
    composeRule.mainClock.autoAdvance = false
    content = document to typefaces
    composeRule.mainClock.advanceTimeBy(100)
    composeRule.waitForIdle()
    val root = composeRule.activity.findViewById<ViewGroup>(android.R.id.content)
    root.measure(
      MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY),
    )
    root.layout(0, 0, w, h)
    return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { root.draw(Canvas(it)) }
  }

  /** A capture's density, from the `dpi_NNN` its render name carries; Wear's xhdpi otherwise. */
  private fun documentDensity(name: String): Float =
    Regex("dpi_(\\d+)").find(name)?.groupValues?.get(1)?.toFloat()?.div(160f) ?: 2f

  /** Share of pixels whose channels differ by more than 16/255 — the rc-compare tolerance. */
  private fun differingFraction(a: Bitmap, b: Bitmap): Double {
    if (a.width != b.width || a.height != b.height) return 1.0
    val left =
      IntArray(a.width * a.height).also { a.getPixels(it, 0, a.width, 0, 0, a.width, a.height) }
    val right =
      IntArray(b.width * b.height).also { b.getPixels(it, 0, b.width, 0, 0, b.width, b.height) }
    val differing =
      left.indices.count { i ->
        (0 until 32 step 8).any { shift ->
          abs(((left[i] ushr shift) and 0xff) - ((right[i] ushr shift) and 0xff)) > 16
        }
      }
    return differing.toDouble() / left.size
  }
}
