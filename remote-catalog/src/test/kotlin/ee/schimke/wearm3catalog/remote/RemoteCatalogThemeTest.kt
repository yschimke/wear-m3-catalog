@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pins what a declared theme *is* here: data applied to an already-recorded document, never
 * something installed while recording it.
 *
 * Every sticker is captured under the default theme, so the document carries the stock scheme as
 * named state (`USER:WearM3.<role>`). A theme is then a map of overrides onto those names, which
 * the player's `setNamedColorOverride` applies with no recomposition — which is what lets a
 * *published* catalog, whose bytecode was dropped at pack time, still be re-themed.
 *
 * The keys are therefore load-bearing in a way ordinary constants are not: they have to match the
 * names the document actually emits, or the override silently no-ops and the render comes back
 * unthemed under a successful status.
 */
class RemoteCatalogThemeTest {

  /**
   * The published set, pinned by name and order — and it is the **sibling `:catalog`'s** set, which
   * is the point of it (#99). Two catalogs of the same surface offering two different Theme selects
   * cannot be compared theme by theme, which is the comparison this repo publishes.
   *
   * These names are also what a published document's colour overrides are addressed by, so renaming
   * one re-points live re-themes at state that is no longer there. Change them here and in
   * `CatalogThemes.kt` together, or not at all.
   */
  @Test
  fun `the declared themes are the sibling catalog's set`() {
    assertThat(REMOTE_THEME_NAMES)
      .containsExactly(
        "Confetti (default)",
        "KotlinConf",
        "AndroidMakers",
        "Droidcon",
        "DevFest",
        "Google Sans Flex",
      )
      .inOrder()
  }

  /**
   * The four conference seeds, pinned as literals.
   *
   * The two modules cannot share a constant — different dependency lines, which is why there are
   * two modules at all — so the seeds are duplicated, and a duplicate that nothing pins is a
   * duplicate that drifts. `CatalogInventoryTest` pins the sibling's four to these same literals
   * (and holds them apart from each other); this pins this module's copy. A seed edited on one side
   * alone fails the other side's build.
   */
  @Test
  fun `the conference seeds are the sibling's seeds`() {
    assertThat(RemoteKotlinConfSeed).isEqualTo(Color(0xFF7F52FF))
    assertThat(RemoteAndroidMakersSeed).isEqualTo(Color(0xFFE59A4F))
    assertThat(RemoteDroidconSeed).isEqualTo(Color(0xFF00D775))
    assertThat(RemoteDevFestSeed).isEqualTo(Color(0xFF4285F4))
  }

  /**
   * Every key addresses a real `RemoteColorScheme` role, spelled the way the document names its
   * state. A typo costs nothing at build time and everything at render time: the player skips an
   * unknown name, so the sticker comes back stock under a successful status rather than failing.
   *
   * The list is `RemoteColorScheme`'s own — the 29 `WearM3.<role>` string constants it writes, read
   * off `remote-material3` 1.0.0-alpha10 — pinned here rather than reflected over, because what
   * matters is that a role we seed is a role a *published* document already carries: a name the
   * library adds later is not one an already-packed catalog emits.
   */
  @Test
  fun `every override key is a role the document actually names`() {
    val roles =
      setOf(
        "WearM3.primary",
        "WearM3.primaryDim",
        "WearM3.primaryContainer",
        "WearM3.onPrimary",
        "WearM3.onPrimaryContainer",
        "WearM3.secondary",
        "WearM3.secondaryDim",
        "WearM3.secondaryContainer",
        "WearM3.onSecondary",
        "WearM3.onSecondaryContainer",
        "WearM3.tertiary",
        "WearM3.tertiaryDim",
        "WearM3.tertiaryContainer",
        "WearM3.onTertiary",
        "WearM3.onTertiaryContainer",
        "WearM3.surfaceContainerLow",
        "WearM3.surfaceContainer",
        "WearM3.surfaceContainerHigh",
        "WearM3.onSurface",
        "WearM3.onSurfaceVariant",
        "WearM3.outline",
        "WearM3.outlineVariant",
        "WearM3.background",
        "WearM3.onBackground",
        "WearM3.error",
        "WearM3.errorDim",
        "WearM3.errorContainer",
        "WearM3.onError",
        "WearM3.onErrorContainer",
      )

    for (name in REMOTE_THEME_NAMES) {
      assertThat(roles).containsAtLeastElementsIn(remoteCatalogThemeColors(name).keys)
    }
  }

  /**
   * Confetti's unseeded theme is the stock scheme, and Google Sans Flex is palette-identical to it
   * on purpose so the pair isolates the typeface. Both must therefore override no colour at all — a
   * stray entry would make that comparison a type *and* colour change.
   */
  @Test
  fun `the baseline themes override no colour`() {
    assertThat(remoteCatalogThemeColors("Confetti (default)")).isEmpty()
    assertThat(remoteCatalogThemeColors("Google Sans Flex")).isEmpty()
  }

  /**
   * A seeded identity re-skins the whole scheme, not a role or two: the sibling maps every Wear
   * role `materialkolor` resolves, and a Remote theme that moved only `primary` would show a
   * conference palette on the kit column and a stock card on the Remote one.
   */
  @Test
  fun `every conference identity carries the full mapped scheme`() {
    for (name in listOf("KotlinConf", "AndroidMakers", "Droidcon", "DevFest")) {
      val colors = remoteCatalogThemeColors(name)

      assertThat(colors.keys)
        .containsAtLeast(
          "WearM3.primary",
          "WearM3.primaryDim",
          "WearM3.primaryContainer",
          "WearM3.onPrimary",
          "WearM3.onPrimaryContainer",
          "WearM3.secondary",
          "WearM3.secondaryDim",
          "WearM3.secondaryContainer",
          "WearM3.onSecondary",
          "WearM3.onSecondaryContainer",
          "WearM3.tertiary",
          "WearM3.surfaceContainer",
          "WearM3.onSurface",
          "WearM3.background",
          "WearM3.error",
        )
    }
  }

  /**
   * The palettes are built from the seeds, so two identities cannot silently be the same palette
   * under two names — the failure `CatalogInventoryTest` guards on the sibling, guarded here on the
   * output rather than the input.
   */
  @Test
  fun `no two conference identities resolve to the same palette`() {
    val palettes =
      listOf("KotlinConf", "AndroidMakers", "Droidcon", "DevFest").map {
        remoteCatalogThemeColors(it)
      }

    assertThat(palettes.toSet()).hasSize(palettes.size)
  }

  /**
   * A seeded identity actually moves `primary` *away* from stock. Cheap, and it is the one
   * assertion that fails if `materialkolor` ever returns an unseeded scheme for a seed we passed —
   * a theme select whose entries all render the same pixels, which no reviewer catches by eye.
   */
  @Test
  fun `a seeded identity moves primary off the seed-free baseline`() {
    val kotlinConf = remoteCatalogThemeColors("KotlinConf")["WearM3.primary"]
    val droidcon = remoteCatalogThemeColors("Droidcon")["WearM3.primary"]

    assertThat(kotlinConf).isNotNull()
    assertThat(kotlinConf).isNotEqualTo(droidcon)
  }

  /**
   * The faces are the sibling's type scales read back out: Confetti's ship pairing puts Inter on
   * body and label under the themes that ship it, and the two Google Sans Flex entries draw one
   * family throughout.
   */
  @Test
  fun `the body faces are the sibling's`() {
    for (name in listOf("Confetti (default)", "KotlinConf", "AndroidMakers", "Droidcon")) {
      assertThat(remoteCatalogFont(name)).isEqualTo("Inter")
    }
    assertThat(remoteCatalogFont("DevFest")).isEqualTo("Google Sans Flex")
    assertThat(remoteCatalogFont("Google Sans Flex")).isEqualTo("Google Sans Flex")
  }

  /** Only the single-family themes pair nothing; everything else pairs Confetti's display face. */
  @Test
  fun `only the single-family themes pair no display face`() {
    assertThat(remoteCatalogDisplayFont("KotlinConf")).isEqualTo("JetBrains Mono")
    for (name in listOf("Confetti (default)", "AndroidMakers", "Droidcon")) {
      assertThat(remoteCatalogDisplayFont(name)).isEqualTo("Roboto Flex")
    }
    for (name in listOf("DevFest", "Google Sans Flex")) {
      assertThat(remoteCatalogDisplayFont(name)).isNull()
    }
  }
}
