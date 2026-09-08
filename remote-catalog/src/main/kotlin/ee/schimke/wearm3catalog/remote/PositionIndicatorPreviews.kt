@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.asRdp
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.LevelIndicatorDefaults
import androidx.wear.compose.remote.material3.RemoteCurvedProgressIndicator
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteProgressIndicatorDefaults
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideFloat
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant
import kotlin.math.asin

// THE KIT'S `Position Indicators` PAGE, on the Remote column for the first time. Both of its sets
// — `Scroll-Indicator` (7 cells) and `Level-Indicator-RSB` (6) — read `—` on this sheet, because
// `remote-material3` publishes NEITHER COMPONENT: the artifact carries
// `RemoteCircularProgressIndicator`, `RemoteLinearProgressIndicator`,
// `RemoteCurvedProgressIndicator` and `RemotePageIndicator`, and no `RemoteScrollIndicator` or
// `RemoteLevelIndicator` ([#356](https://github.com/yschimke/wear-m3-catalog/issues/356) is the
// upstream ask, and it stays open).
//
// SO BOTH RAILS ARE `RemoteCurvedProgressIndicator`, and that substitution is the same one
// `StepperPreviews.kt` already makes for the stepper's level rail
// ([#368](https://github.com/yschimke/wear-m3-catalog/pull/368)). It is admissible on one reading
// and one only: the kit's rails and the Remote arc are the SAME PICTURE STRUCK BY THE SAME
// PRIMITIVE — an arc of track along the bezel with a filled segment on it — so what the sticker
// varies is the arc's own published arguments rather than a component invented under the kit's
// name. Every number below is published by one library or the other and cited where it is used; a
// number chosen here would make the row a picture of this file rather than of either library.
//
// WHAT THAT COSTS, stated rather than tuned away. The Wear column calls `ScrollIndicator` and
// `LevelIndicator`, which own behaviour no argument to an arc reproduces: the scroll rail derives
// its thumb from a real `ScrollState` and animates the travel, and both clamp and inset themselves
// against the actual display. Here the thumb's length and offset are numbers the sticker supplies.
// That is why these rows are drawn against the KIT and pair with the Wear column by name — the two
// columns are two renditions of one kit cell, not two calls to one API.

/**
 * The kit's display cell is 192dp across, and half of it is the radius every angle below is struck
 * on. Both rails are pinned to that radius rather than to the frame's: [CatalogRemoteDisplay] fans
 * a sticker across the kit's five round breakpoints, and a `RemoteCurvedProgressIndicator` takes an
 * ANGLE where the Wear components take a HEIGHT in dp — so a rail that keeps its dp length would
 * need a different angle at each size, and there is no argument that computes one. The angle is
 * therefore right at 192dp, which is the size the kit draws and the base cell of the fan-out, and
 * the rail is proportionally a little short on the four larger frames. A stated divergence.
 */
private const val KitDisplayRadius = 96f

// THE FOUR SCROLL-RAIL NUMBERS ARE TRANSCRIBED, and it is worth saying why rather than reading
// them: `ScrollIndicatorDefaults` publishes `indicatorHeight`, `indicatorWidth`, `minSizeFraction`
// and `maxSizeFraction` as `internal` — public in the bytecode, `internal` in the Kotlin API — so
// there is nothing here to call. `LevelIndicatorDefaults`' pair below IS public and is read.

/** `ScrollIndicatorDefaults.indicatorHeight` — 50dp, the rail's full extent. */
private const val ScrollRailHeight = 50f

/**
 * `ScrollIndicatorDefaults.indicatorWidth`, at the 192dp base cell.
 *
 * Wear resolves it by breakpoint — 5dp below its large-screen threshold and 6dp above — and the
 * value is pinned here for the same reason [KitDisplayRadius] is: the sweep angle below is struck
 * on a radius this number is part of, so a stroke that changed with the frame would need an angle
 * that changed with it too.
 */
private const val ScrollRailStroke = 5f

/** `ScrollIndicatorDefaults.minSizeFraction` — the kit's `30% (Min)`, and the base cell. */
private const val ScrollRailMinSizeFraction = 0.3f

/** `ScrollIndicatorDefaults.maxSizeFraction` — the kit's `70% (Max)`. */
private const val ScrollRailMaxSizeFraction = 0.7f

/**
 * The rail's sweep, derived the way Wear derives it: a chord of [ScrollRailHeight] on the circle
 * the stroke is centred on subtends `2·asin(h/2r)`.
 *
 * The 2dp inset is `PaddingDefaults.edgePadding`, which is also
 * `RemoteProgressIndicatorDefaults.CurvedIndicatorPadding` — the arc applies it itself, so it
 * appears here only inside the radius this angle is struck on.
 */
private val ScrollRailSweepAngle =
  2f *
    Math.toDegrees(
        asin((ScrollRailHeight / 2f) / (KitDisplayRadius - 2f - ScrollRailStroke / 2f)).toDouble()
      )
      .toFloat()

/**
 * **Every cell of the kit's `Scroll-Indicator` set** — all seven, which is four more than the Wear
 * column draws.
 *
 * The set's one axis, `Position`, is two different quantities under one name and that is what
 * decides the cells. `Middle` and `Bottom` are where the thumb SITS; `30% (Min)` … `70% (Max)` are
 * how LONG it is, and they are literally `ScrollIndicator`'s own `minSizeFraction` and
 * `maxSizeFraction` and the three steps between — the fraction of the rail a thumb takes up, which
 * the Wear component derives from how much of the content fits on screen.
 *
 * THE WEAR COLUMN CANNOT REACH THE LENGTH HALF, and that is not a limitation this column shares.
 * `kit-sets.json` records the measurement: seeded to 2.5, 2 and 1.67 screenfuls of content the Wear
 * thumb renders byte-identical to the base's five, so those four cells stay out over there. Here
 * the length is `sizeFraction`, an argument, so the kit's five lengths are five different pictures
 * and are drawn. `middle` and `bottom` keep the Wear sibling's cell names so the two columns pair
 * on the cells both draw; the five length cells are one-sided and named for the kit's own
 * percentages.
 */
@OverrideVariant(
  name = "size-40",
  floats = ["sizeFraction=0.4"],
  kitAxis = "Position",
  kitValue = "40%",
)
@OverrideVariant(
  name = "size-50",
  floats = ["sizeFraction=0.5"],
  kitAxis = "Position",
  kitValue = "50%",
)
@OverrideVariant(
  name = "size-60",
  floats = ["sizeFraction=0.6"],
  kitAxis = "Position",
  kitValue = "60%",
)
@OverrideVariant(
  name = "size-70",
  floats = ["sizeFraction=0.7"],
  kitAxis = "Position",
  kitValue = "70% (Max)",
)
@OverrideVariant(
  name = "middle",
  floats = ["position=0.5"],
  kitAxis = "Position",
  kitValue = "Middle",
)
@OverrideVariant(
  name = "bottom",
  floats = ["position=1.0"],
  kitAxis = "Position",
  kitValue = "Bottom",
)
annotation class RemoteScrollIndicatorKitCells

@CatalogComponent(
  id = "ScrollIndicator",
  group = "Position indicators",
  parallel = "ScrollIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/44998:18343",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/44998:18342",
  caption =
    "Where the screen sits in a scroll, drawn against the right bezel — two curved progress arcs " +
      "standing in for the scroll indicator remote-material3 does not publish.",
)
@CatalogRemoteDisplay
@RemoteScrollIndicatorKitCells
@Composable
fun ScrollRailRemote() = RemoteSticker {
  // The kit's `Position` axis, as its two quantities. `position` is the Wear sibling's knob name
  // and its base value (the thumb at the top); `sizeFraction` is `ScrollIndicator`'s own parameter
  // name for the length, seeded at the `minSizeFraction` the kit calls `30% (Min)`.
  val position = previewOverrideFloat("position", 0f).coerceIn(0f, 1f)
  val sizeFraction =
    previewOverrideFloat("sizeFraction", ScrollRailMinSizeFraction)
      .coerceIn(ScrollRailMinSizeFraction, ScrollRailMaxSizeFraction)
  val stroke = ScrollRailStroke.rdp
  // 0 degrees is 3 o'clock and a sweep runs CLOCKWISE from there, so a rail centred on the RIGHT
  // bezel starts half a sweep above the centre line and a clockwise thumb travels DOWNWARD — which
  // is what makes `position` read as a scroll offset without a `reverseDirection` flip. (The level
  // rail below is the mirror case: on the left bezel clockwise is upward, so it grows from the
  // bottom with no flip either.)
  val trackStart = -ScrollRailSweepAngle / 2f
  val thumbSweep = ScrollRailSweepAngle * sizeFraction
  // TWO ARCS, because one cannot draw a segment that does not start at the rail's end. A
  // `RemoteCurvedProgressIndicator` fills from its own `startAngle`, so the thumb is a second arc
  // struck at the offset with `progress = 1`, and the first arc is there for the track alone —
  // held at `progress = 0` with a TRANSPARENT indicator so that whatever the lane's dot affordance
  // does at zero (`dotCollapsible` on the snapshot line, `dotCollapseFreezeFraction` on the
  // released one — two spellings, so neither is passed) it cannot draw ink the kit's cell has not
  // got.
  RemoteCurvedProgressIndicator(
    progress = 0f.rf,
    modifier = RemoteModifier.fillMaxSize(),
    startAngle = trackStart.rf,
    sweepAngle = ScrollRailSweepAngle.rf,
    colors =
      RemoteProgressIndicatorDefaults.colors(
        indicatorColor = RemoteColor(Color.Transparent),
        trackColor = RemoteScrollRailTrackColor(),
      ),
    strokeWidth = stroke,
  )
  RemoteCurvedProgressIndicator(
    progress = 1f.rf,
    modifier = RemoteModifier.fillMaxSize(),
    // The thumb slides over the part of the rail it does not fill, which is why the travel is
    // `sweep − thumbSweep` rather than the whole sweep: at `position = 1` its far end lands on the
    // rail's end rather than a thumb's length past it.
    startAngle = (trackStart + position * (ScrollRailSweepAngle - thumbSweep)).rf,
    sweepAngle = thumbSweep.rf,
    colors =
      RemoteProgressIndicatorDefaults.colors(
        indicatorColor = RemoteScrollRailThumbColor(),
        trackColor = RemoteColor(Color.Transparent),
      ),
    strokeWidth = stroke,
  )
}

/**
 * The thumb's colour, and the closest published role to what Wear resolves.
 *
 * `ScrollIndicatorDefaults.colors()` is `onBackground` put through a HCT luminance transform — 80
 * for the thumb, 20 for the track — which is a computed colour rather than a scheme role. A Remote
 * document is re-themed by overriding NAMED colour state (`USER:WearM3.<role>`, see
 * `RemoteThemeCatalogs.kt`), so baking the two resolved constants in would give this row a rail
 * that ignores every theme in the switcher. The roles are named instead and the difference in
 * luminance is left visible.
 */
@Composable private fun RemoteScrollRailThumbColor() = RemoteMaterialTheme.colorScheme.onBackground

/** @see RemoteScrollRailThumbColor */
@Composable
private fun RemoteScrollRailTrackColor() = RemoteMaterialTheme.colorScheme.surfaceContainer

/**
 * **The kit's `Level-Indicator-RSB` set at `Size=Default`** — both of its cells, which is what the
 * Wear column draws too.
 *
 * The other four are `Size = Long | Short`, which in Compose is the arc's `sweepAngle` — 72
 * degrees, a fifth of the circumference, by default. Both libraries take it; the kit does not
 * publish the two angles its cells draw. They are bound to no variable and the cells export as
 * flattened SVGs, so the numbers live in that path data and guessing one would put an invented
 * angle under the kit's name. Same blocker, same wording, as the `catalog` row states — see
 * `kit-sets.json`.
 *
 * `low` and `full` name no kit node, and that is deliberate rather than a typo'd `kitValue`: the
 * kit publishes `Size` and `Disabled` and no value axis, so there is no node for a level to be a
 * picture of. They are here because the Wear sibling publishes exactly these two under exactly
 * these names — a rail at one value says nothing about what the value does — and a cell the other
 * column has and this one does not is a row that half-pairs.
 */
@OverrideVariant(name = "low", floats = ["value=0.15"])
@OverrideVariant(name = "full", floats = ["value=1.0"])
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
annotation class RemoteLevelIndicatorKitCells

@CatalogComponent(
  id = "LevelIndicator",
  group = "Position indicators",
  parallel = "LevelIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/46640:262811",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/46619:47146",
  caption =
    "The value a rotating side button is setting, while it is being turned — a curved progress " +
      "arc standing in for the level indicator remote-material3 does not publish.",
)
@CatalogRemoteDisplay
@RemoteLevelIndicatorKitCells
@Composable
fun LevelRailRemote() = RemoteSticker {
  // `value`, not `level`: the knob carries the name of the parameter it sets on the Wear column, so
  // a reader of the controls panel can find it in `LevelIndicator`'s signature. The kit's word for
  // the axis rides on the cell, not on the knob.
  val value = previewOverrideFloat("value", 0.6f).coerceIn(0f, 1f)
  val enabled = previewOverrideBoolean("enabled", true).rb
  // READ FROM `LevelIndicatorDefaults`, not transcribed: this module already depends on
  // `androidx.wear.compose:compose-material3` (see remote-catalog/build.gradle.kts), so the sweep
  // and the stroke are the Wear tokens themselves — 72 degrees and 6dp. The Remote arc's own
  // defaults are 90 degrees and 8dp, which are the PROGRESS arc's numbers and not a rail's.
  // `StepperPreviews.kt` holds the sweep as a private constant instead, on a claim about the
  // dependency that is not true of this module; that is worth folding together when the stepper is
  // next touched, and is left alone here rather than moving an existing render.
  val sweep = LevelIndicatorDefaults.SweepAngle
  RemoteCurvedProgressIndicator(
    progress = value.rf,
    modifier = RemoteModifier.fillMaxSize(),
    enabled = enabled,
    // The arc centred on the LEFT bezel, which is where `LevelIndicator` puts it by passing
    // `rsbSide = false`. Derived from the sweep so it moves with it rather than being a second
    // literal — and on the left bezel a clockwise fill runs UPWARD, which is the bottom-anchored
    // growth Wear gets from `positionFraction`, so no `reverseDirection` is needed.
    startAngle = (180f - sweep / 2f).rf,
    sweepAngle = sweep.rf,
    // `secondaryDim` over `surfaceContainer` are `LevelIndicatorColors`' indicator and track roles.
    // The Remote arc's default indicator is `primary`, which is the progress colour; the track
    // already agrees. The DISABLED pair is left alone — both libraries resolve it to `onSurface` at
    // 0.38 / 0.12, so overriding it would restate the default.
    colors =
      RemoteProgressIndicatorDefaults.colors(
        indicatorColor = RemoteMaterialTheme.colorScheme.secondaryDim,
        trackColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
      ),
    strokeWidth = LevelIndicatorDefaults.StrokeWidth.asRdp(),
  )
}
