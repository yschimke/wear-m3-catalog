package ee.schimke.wearm3catalog

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The placeholder stickers' LIVE half — the one no render can see.
 *
 * `Modifier.placeholderShimmer` reads a frame clock that only `AppScaffold` composes, so a
 * placeholder drawn in a bare `Sticker` is frozen. That is right for the baked capture and wrong for
 * a held session, where a shimmer that never sweeps reads as a component drawn in grey blocks
 * ([#487](https://github.com/yschimke/wear-m3-catalog/issues/487)). `PlaceholderSticker` composes
 * the scaffold — sized to nothing, only for its clock — behind `catalogInteractive()`.
 *
 * Both halves are asserted here because both are invisible to `CatalogRenderTest`: the baked PNGs
 * were correct while the live component stood still, and they would stay correct if the gate were
 * dropped and every publish started shipping a different frame of the sweep.
 *
 * A SOURCE ASSERTION, for want of a reachable behaviour. `PlaceholderState`'s frame time and the
 * coordinator's registration are both library-internal, and the one thing a test could otherwise
 * observe — a sweeping pixel — is what a Robolectric render deliberately does not have a clock for.
 * What actually regresses is a fifth placeholder sticker written with `Sticker {` like its
 * neighbours, and that this can see.
 */
class PlaceholderShimmerTest {

  private val source =
    File("src/commonMain/kotlin/ee/schimke/wearm3catalog/sections/Placeholders.kt").readText()

  @Test
  fun `every placeholder sticker takes the frame that carries the clock`() {
    val stickers = Regex("""^fun (\w+)\(""", RegexOption.MULTILINE)
    val bodies = source.split(Regex("""(?=^@CatalogComponent\()""", RegexOption.MULTILINE)).drop(1)
    assertEquals("three placeholder components", 3, bodies.size)
    for (body in bodies) {
      val name = stickers.find(body)?.groupValues?.get(1) ?: error("no sticker function:\n$body")
      assertTrue(
        "$name draws a placeholder in a frame with no animation clock, so its shimmer cannot " +
          "sweep on the live lane; use PlaceholderSticker",
        body.contains("PlaceholderSticker {"),
      )
    }
  }

  /**
   * The gate, without which the clock runs in the baked lane too and every render publishes
   * whichever frame of the sweep it caught — a changed PNG on every publish for a component nobody
   * touched.
   */
  @Test
  fun `the clock is composed on the live lane only`() {
    val frame =
      source.substringAfter("private fun PlaceholderSticker(").substringBefore("\n/**")
    assertTrue(
      "PlaceholderSticker composes an AppScaffold for its clock but does not gate it on " +
        "catalogInteractive(), so a baked capture would catch a moving shimmer",
      frame.contains("catalogInteractive()") && frame.contains("AppScaffold("),
    )
  }
}
