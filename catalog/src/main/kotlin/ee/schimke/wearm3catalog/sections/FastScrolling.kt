@file:CatalogGroup(name = "Fast scrolling", section = "Horologist")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.android.horologist.compose.layout.m3.FastScrollingTransformingLazyColumn
import com.google.android.horologist.compose.layout.m3.HeaderInfo
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.ScreenSticker

// Horologist's fast-scrolling list — the long-list idiom Wear Compose leaves to the app.
//
// WHAT IT IS. A `TransformingLazyColumn` that watches the rotating side button: past a velocity
// threshold it stops scrolling *through* the rows and starts jumping between SECTION HEADERS,
// raising a large letter over the list as it goes, with a haptic tick per section. On a
// two-hundred-contact list that is the difference between a usable list and a spinning bezel, and
// there is no way to opt into it from `TransformingLazyColumn` itself — the behaviour is the
// component.
//
// IT ENTERS THROUGH THE LIBRARY'S DOOR, like the plain list already on this sheet: the kit's list
// pages publish the ROWS (list header, cards) rather than the scrolling container, so there is no
// set to point at. `Lists.kt` says the same thing about `TransformingLazyColumn`; this is the same
// gap one component further along.
//
// WHAT A STILL CANNOT SHOW, AND WHAT IS DONE ABOUT IT. The skim indicator is raised by rotary
// VELOCITY and fades on a timeout — neither of which a baked frame has. So the published capture is
// the list at rest and scrolled, and the section-jumping is described here rather than implied by a
// picture of it. `@InteractionPreview` is not an option: it is implemented in the desktop renderer
// only, and on Robolectric it costs the component its ordinary PNG as well (AGENTS.md).

@CatalogComponent(
  id = "FastScrollingTransformingLazyColumn",
  noReference =
    "No kit set, for the same reason `TransformingLazyColumn` has none: the kit's list pages " +
      "publish the rows, not the scrolling container — and the rotary skim this adds on top is a " +
      "behaviour rather than a drawn state, so there is nothing in the kit it could be a cell of.",
  caption =
    "A long list that skims by section when the rotating bezel is spun hard, instead of crawling.",
)
@CatalogFullScreenModes
@ScrollingPreview(modes = [ScrollMode.END])
@Composable
fun FastScrollingList() = ScreenSticker {
  val state = rememberTransformingLazyColumnState()
  val spec = rememberTransformationSpec()

  // The headers are addressed by their INDEX INTO THE LIST, counting every item — the rows between
  // them included. Building both from one table is what keeps the two in step: an index computed by
  // hand goes stale the moment a section gains a row, and the failure is silent (the skim lands on
  // the wrong section and nothing errors).
  val sections = remember { SECTIONS }
  val headers = remember {
    var index = 0
    sections
      .map { (letter, names) ->
        HeaderInfo(index = index, value = letter).also { index += names.size + 1 }
      }
      .toMutableStateList()
  }

  ScreenScaffold(scrollState = state) { padding ->
    FastScrollingTransformingLazyColumn(
      state = state,
      headers = headers,
      contentPadding = padding,
      modifier = Modifier.fillMaxSize(),
    ) {
      for ((letter, names) in sections) {
        item {
          ListHeader(
            modifier = Modifier.transformedHeight(this, spec),
            transformation = SurfaceTransformation(spec),
          ) {
            Text(letter)
          }
        }
        items(names.size) { row ->
          Button(
            onClick = {},
            label = { Text(names[row]) },
            modifier = Modifier.transformedHeight(this, spec),
            transformation = SurfaceTransformation(spec),
          )
        }
      }
    }
  }
}

/**
 * The list the sticker scrolls: enough sections that skimming between them is the point.
 *
 * Fixed rather than generated. A list built from a random or clock-seeded source would render
 * differently on every publish, and the delivery branch is diffed over time (AGENTS.md).
 */
private val SECTIONS: List<Pair<String, List<String>>> =
  listOf(
    "A" to listOf("Alex", "Amara", "Anders"),
    "B" to listOf("Bao", "Bram"),
    "C" to listOf("Cai", "Chidi", "Clara"),
    "D" to listOf("Dara", "Dmitri"),
    "E" to listOf("Edda", "Emeka"),
    "F" to listOf("Farid", "Fiona"),
  )
