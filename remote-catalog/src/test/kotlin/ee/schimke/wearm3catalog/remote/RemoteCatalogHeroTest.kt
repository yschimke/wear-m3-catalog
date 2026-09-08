package ee.schimke.wearm3catalog.remote

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Guards `display.hero` in `remote-catalog/catalog.spec.json` against naming a component that does
 * not exist.
 *
 * The sibling `:catalog` module has carried this guard since its own hero was declared, and the
 * reason is the same on both sheets: the hero is the ONE field in a cover-sheet spec that names
 * something the annotations own, and **nothing downstream complains when it names nothing**. The
 * preview server resolves a declared hero by id and then by slug and, finding no preview, silently
 * falls back to its own representative pick (`ServeBundleHost.declaredHeroPreviewId` in
 * compose-preview-server). So a renamed or mistyped component id costs this catalog its front-door
 * picture and reports it nowhere — the index simply features a different sticker than the one that
 * was chosen, which is indistinguishable from having chosen that one.
 *
 * Matched against the `@CatalogComponent(id = …)` annotations in this module's sources rather than
 * against a built inventory, so it holds without a render: the failure this catches is a rename in
 * the same commit as a stale spec, and both are text.
 */
class RemoteCatalogHeroTest {

  private val heroPattern = Regex(""""hero"\s*:\s*"([^"]+)"""")
  private val componentIdPattern = Regex("""@CatalogComponent\([^)]*?id\s*=\s*"([^"]+)"""")

  @Test
  fun `the declared front-door hero names a real component`() {
    val spec = File("catalog.spec.json").readText()
    val hero =
      heroPattern.find(spec)?.groupValues?.get(1)
        ?: error("remote-catalog/catalog.spec.json declares no display.hero")
    val ids =
      File("src/main/kotlin")
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .flatMap { componentIdPattern.findAll(it.readText()) }
        .map { it.groupValues[1] }
        .toSet()
    assertWithMessage(
        "remote-catalog/catalog.spec.json's display.hero is \"$hero\", which is not a " +
          "@CatalogComponent id in this module — the front door would quietly feature whatever " +
          "the server picks instead. Known ids: ${ids.sorted().joinToString(", ")}"
      )
      .that(ids)
      .contains(hero)
  }

  @Test
  fun `the sources this test reads are actually there`() {
    // Without this the walk above degrades into an empty id set on a working-directory change, and
    // an assertion over nothing is one that cannot fail for the reason it was written.
    val ids =
      File("src/main/kotlin")
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .flatMap { componentIdPattern.findAll(it.readText()) }
        .map { it.groupValues[1] }
        .toSet()
    assertWithMessage("no @CatalogComponent ids were found — is the working directory the module?")
      .that(ids.size)
      .isAtLeast(10)
  }
}
