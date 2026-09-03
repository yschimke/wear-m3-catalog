@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteLinearProgressIndicator
import androidx.wear.compose.remote.material3.RemoteLinearProgressIndicatorDefaults
import ee.schimke.composeai.daemon.rememberOverridableRemoteFloat
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// THE KIT'S `Progress-Indicator-Linear` SET, opened on the Remote column for the first time.
//
// A straight track, and the second progress set on this sheet — `Progress-Indicator` (the circular
// rail) has been drawn since the beginning, this one had nothing to call: `remote-material3`
// published `RemoteCircularProgressIndicator` and `RemoteCurvedProgressIndicator` and no linear
// one. The snapshot line adds `RemoteLinearProgressIndicator`.
//
// IT ARRIVES WITH ITS STROKE TOKENS, which is the part worth stating because the circular set next
// door does not have them. `CircularProgressRemote` reaches the kit's `Stroke Width` axis by
// borrowing `androidx.wear.compose.material3`'s `CircularProgressIndicatorDefaults` — a whole extra
// dependency on this module, declared in `remote-catalog/build.gradle.kts` with a paragraph
// explaining that `RemoteProgressIndicatorDefaults` publishes no determinate stroke token and that
// a literal typed in here would be a number invented under the kit's name. None of that is needed
// here: `RemoteLinearProgressIndicatorDefaults` publishes `StrokeWidthSmall` and `StrokeWidthLarge`
// outright, so this set's `Size` axis is spelled in the Remote library's own tokens.
//
// WHAT IS REACHABLE IS HALF THE SET, and it is the same half the Wear column draws — 8 of 16.
// The other eight are those eight again under `Context=In List`, and `Context` is not a parameter
// on either platform: it is where the caller PUTS the indicator, and the kit draws both because
// the surrounding row changes what you see around it. A sticker of the component alone is the
// `In Component` half, which is exactly the stance the Wear sibling takes and for the same reason.
//
// THE WIDTH IS THE WEAR SIBLING'S 150dp, not [KitRowWidth]. That is deliberate and it is the one
// number here that is not a token: a linear indicator has no intrinsic width, so whatever it is
// given is what it draws, and the two columns have to be given the SAME thing or the comparison is
// between two track lengths rather than between two renditions of one component. 150dp is what
// `:catalog`'s `LinearProgress` passes, so it is what this passes.

/**
 * **Every `Progress-Indicator-Linear` cell this function draws** — four `Progress` values by two
 * `Size` values, 8 of the set's 16 nodes.
 *
 * A transcription of the Wear sibling's `LinearProgressKitCells`
 * (`catalog/src/main/kotlin/…/sections/ProgressIndicators.kt`), cell name for cell name, because
 * both axes are arguments to the one function on both platforms and the crossings are therefore
 * identical. Keeping the spellings identical is what lets the compare page set `20-small` beside
 * `20-small` rather than pairing two names for one cell.
 *
 * The other eight nodes are these eight under `Context=In List` — see the note above for why a
 * component sticker draws the `In Component` half and stops.
 */
@OverrideVariant(
  name = "min",
  floats = ["progress=0.0"],
  kitAxis = "Progress",
  kitValue = "Min",
)
@OverrideVariant(
  name = "20",
  floats = ["progress=0.2"],
  kitAxis = "Progress",
  kitValue = "20%",
)
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(
  name = "min-small",
  strings = ["size=small"],
  floats = ["progress=0.0"],
  kitProps = ["Progress=Min", "Context=In Component", "Size=Small"],
  secondary = true,
)
@OverrideVariant(
  name = "20-small",
  strings = ["size=small"],
  floats = ["progress=0.2"],
  kitProps = ["Progress=20%", "Context=In Component", "Size=Small"],
  secondary = true,
)
@OverrideVariant(
  name = "small",
  strings = ["size=small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(
  name = "complete-small",
  strings = ["size=small"],
  floats = ["progress=1.0"],
  kitProps = ["Progress=Complete", "Context=In Component", "Size=Small"],
  secondary = true,
)
annotation class RemoteLinearProgressKitCells

@CatalogComponent(
  id = "LinearProgressIndicator",
  group = "Communication",
  parallel = "LinearProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/45011:259221",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/45011:259051",
  caption = "A straight track, for progress inside a component or a list row.",
)
@CatalogRemoteModes
@RemoteLinearProgressKitCells
@Composable
fun LinearProgressRemote() = RemoteSticker {
  // TWO override paths on one name, and the cells need both — the same pairing
  // `CircularProgressRemote` spells out next door. `rememberOverridableRemoteFloat` publishes
  // `progress` as a document NAMED VALUE the viewer reseeds live (`rc.progress=float:<0..1>`)
  // without re-capturing; a `@OverrideVariant` seeds a PREVIEW override, which that call never
  // reads. Feeding the preview override in as the named value's default settles it: a cell picks
  // the value the document is built with, and the live path is untouched.
  //
  // 0.5 is the base value, and it is the Wear sibling's — the two renditions of this kit cell fill
  // the same fraction of the same track.
  val progress = rememberOverridableRemoteFloat("progress", previewOverrideFloat("progress", 0.5f))
  RemoteLinearProgressIndicator(
    progress = progress,
    // See the file note: the Wear sibling's number, passed because a linear track has no width of
    // its own and the two columns must be handed the same one.
    modifier = RemoteModifier.width(150.rdp),
    enabled = previewOverrideBoolean("enabled", true).rb,
    // The kit's `Size` axis, in the Remote library's own tokens. `large` is the base cell: the kit
    // names two widths where the library names two, and the library's "large" is the kit's default
    // because the kit publishes no wider one. Same pairing the Wear sibling makes.
    strokeWidth =
      if (previewOverrideChoice("size", "large", listOf("large", "small")) == "small")
        RemoteLinearProgressIndicatorDefaults.StrokeWidthSmall
      else RemoteLinearProgressIndicatorDefaults.StrokeWidthLarge,
  )
}
