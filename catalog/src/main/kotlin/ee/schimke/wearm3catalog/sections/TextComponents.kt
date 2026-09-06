@file:CatalogGroup(name = "Text", section = "Text")

package ee.schimke.wearm3catalog.sections

import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AnimatedText
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FadingExpandingLabel
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.rememberAnimatedTextFontRegistry
import androidx.wear.compose.material3.timeTextCurvedText
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.CatalogTransparentScreenModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.TransparentScreenSticker
import ee.schimke.wearm3catalog.kitCopy
import ee.schimke.wearm3catalog.kitRowWidth

// The kit's `Text` page: the two list headers, the two body roles, and the curved clock.
//
// `Text-Body` and `Text-Caption` are `Text` at a named role from the Wear type scale rather than
// two components — but they are two kit sets, and membership is the kit's call, so they are two
// stickers. What each names on the Compose side is a `MaterialTheme.typography` role, which is why
// the caption says which one: the sticker is otherwise indistinguishable from any other text.

/** The kit's `Alignment` axis for the text specimens. */
enum class TextAlignment {
  @KnobValue("centre") Centre,
  @KnobValue("left") Left,
  @KnobValue("right") Right,
}

/**
 * The kit's `Alignment=` axis, as the `textAlign` its three text sets take.
 *
 * A choice rather than a text box: `TextAlign` is a closed set, and a control that only shows
 * `centre` leaves the other values reachable only by someone who has read this file. `right` is
 * offered too — the kit publishes Left and Centre, but the parameter takes it and a live session is
 * where a reader tries the thing the kit did not draw.
 */
@Composable
private fun textAlign(align: TextAlignment): TextAlign =
  when (align) {
    TextAlignment.Left -> TextAlign.Start
    TextAlignment.Right -> TextAlign.End
    TextAlignment.Centre -> TextAlign.Center
  }

@CatalogComponent(
  id = "ListHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66978",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66977",
  caption = "Titles the screen a list belongs to; the first thing above the list.",
)
@CatalogModes
// The kit's other axis, `Type = Page-Top | Page-Mid`, is NOT a cell and cannot be one:
// `ListHeaderDefaults` publishes a single `contentPadding` and `ListHeader` takes no argument that
// distinguishes the first header on a screen from one further down a list. The two kit cells
// differ in the space above them, which is a property of where the header sits rather than of the
// component — so the set stays at two of its four cells, with the reason here rather than in a
// silence ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun ListHeading(align: TextAlignment = TextAlignment.Centre) = Sticker {
  ListHeader(modifier = Modifier.width(180.dp)) {
    Text(kitCopy("label", KitCopy.TITLE), textAlign = textAlign(align))
  }
}

/** The kit's `Alignment` axis for a sub-heading. */
enum class SubHeadingAlign {
  @KnobValue("left") Left,
  @KnobValue("centre") Centre,
}

@CatalogComponent(
  id = "ListSubHeader",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66983",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66982",
  caption = "Divides a long list into named runs, without leaving the screen's title behind.",
)
@CatalogModes
// The kit publishes `Icon=Yes` at `Alignment=Left` only — the icon leads the row, so there is
// nothing for a centred variant of it to be — which is why the icon cell names one axis and the
// centred one names the pair.
@OverrideVariant(
  name = "icon",
  booleans = ["icon=true"],
  kitProps = ["Icon=Yes", "Alignment=Left"],
)
@OverrideVariant(
  name = "centred",
  strings = ["align=centre"],
  kitProps = ["Icon=No", "Alignment=Centre"],
)
@Composable
fun ListSubHeading(
  icon: Boolean = false,
  align: SubHeadingAlign = SubHeadingAlign.Left,
) = Sticker {
  ListSubHeader(
    modifier = Modifier.width(180.dp),
    icon =
      if (icon) {
        { Icon(Icons.Filled.Add, contentDescription = null) }
      } else null,
    // The kit's `Alignment` axis, which is what the label does with the width it is given rather
    // than a parameter of the sub-header. `left` is the base cell here (a run of list rows starts
    // its text at the same place), so this knob's default differs from the shared `textAlign`
    // helper's — the header above it is centred and this one is not.
    label = {
      Text(
        kitCopy("label", KitCopy.SUBTITLE),
        modifier = Modifier.fillMaxWidth(),
        textAlign = if (align == SubHeadingAlign.Centre) TextAlign.Center else TextAlign.Start,
      )
    },
  )
}

@CatalogComponent(
  id = "Text/Body",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66993",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66990",
  caption = "Running text at the `bodyMedium` role of the Wear type scale.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun BodyText(align: TextAlignment = TextAlignment.Centre) = Sticker {
  Text(
    kitCopy("text", KitCopy.BODY),
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier.width(160.dp),
    textAlign = textAlign(align),
  )
}

@CatalogComponent(
  id = "Text/Caption",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66998",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66995",
  caption = "Secondary text at the `labelSmall` role — a timestamp, a unit, a footnote.",
)
@CatalogModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun CaptionText(align: TextAlignment = TextAlignment.Centre) = Sticker {
  Text(
    kitCopy("text", KitCopy.CAPTION),
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.width(160.dp),
    textAlign = textAlign(align),
  )
}

// Pinned to 10:10, never the system clock. An unpinned clock would make every nightly render differ
// from the last, which turns the delivery branch's history into noise — and the strip is curved to
// the bezel, so it publishes on the round frame.
@CatalogComponent(
  id = "TimeText",
  reference = "figma:B24oss2tTeXAFykyeyusz0/48151:45209",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38973:10025",
  caption = "The curved status strip every Wear screen carries, pinned to a fixed time.",
)
@CatalogTransparentScreenModes
// The kit's `Size=` axis is the WEARER'S font setting, not a parameter — `User Config - Largest
// (124%)` is the top notch of the watch's text-size control. Compose spells that as the density's
// `fontScale`, which is what turns an sp into a px, so a cell for it provides a density with the
// scale in it and calls the same `TimeText` underneath. That is the setting itself rather than a
// picture of one: the strip re-measures its own curve at the larger size exactly as it does on a
// watch whose owner has turned the text up.
@OverrideVariant(
  name = "24-hour",
  strings = ["time=09:30"],
  kitAxis = "Type",
  kitValue = "24hr",
)
@OverrideVariant(
  name = "largest-font",
  floats = ["fontScale=1.24"],
  kitProps = ["Type=12hr", "Size=User Config - Largest (124%)"],
)
@OverrideVariant(
  name = "24-hour-largest-font",
  strings = ["time=09:30"],
  floats = ["fontScale=1.24"],
  kitProps = ["Type=24hr", "Size=User Config - Largest (124%)"],
  secondary = true,
)
@Composable
fun FixedTimeText(fontScale: Float = 1f) = TransparentScreenSticker {
  val time = kitCopy("time", KitCopy.TIME_12H)
  val density = LocalDensity.current
  CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
    TimeText { timeTextCurvedText(time) }
  }
}

// ---------------------------------------------------------------------------
// The two text components whose subject is a MOTION, and which therefore enter through the
// library's door: the kit draws states, and neither of these is one.
// ---------------------------------------------------------------------------

/**
 * The three lengths the fading label is drawn at, as the strings each cell shows.
 *
 * Plain literals rather than [KitCopy], because there is no kit node for either component below and
 * so no reference whose words the capture has to match (AGENTS.md → Sticker conventions). What they
 * do have to be is *the same sentence growing*: the animation is the arrival of a LINE, so three
 * unrelated strings would record three different labels rather than one label expanding.
 */
private val FADING_LABEL_LINES =
  listOf(
    "Run started",
    "Run started, 2.4 km logged",
    "Run started, 2.4 km logged and your pace is holding steady",
  )

/** The label text for a given number of lines, clamped to what [FADING_LABEL_LINES] holds. */
internal fun fadingLabelText(lines: Int): String =
  FADING_LABEL_LINES[lines.coerceIn(1, FADING_LABEL_LINES.size) - 1]

@CatalogComponent(
  id = "FadingExpandingLabel",
  noReference =
    "The kit publishes no set for it, and could not: what this component draws is the ARRIVAL " +
      "of a line of text — the container growing while the new line fades in — which is a " +
      "transition rather than a state, and the kit's text pages publish states. Wear Compose " +
      "ships it as its own component in its own file, so it enters through the library's door.",
  caption =
    "A label that grows its container a line at a time, fading each new line in as it arrives.",
  // The whole of it. Every cell below is one frame of the thing the component is named after —
  // see `Motion.FadingExpandingLabelMotion`.
  motionPreview = "FadingExpandingLabelMotion",
)
@CatalogModes
@OverrideVariant(name = "one-line", ints = ["lines=1"])
@OverrideVariant(name = "three-lines", ints = ["lines=3"])
@Composable
fun FadingLabel(lines: Int = 2) = Sticker {
  // IN A BUTTON, because the container is half of what the component does: its KDoc says it is
  // "intended to be used for labels in a Button or Card, where we want the container to expand to
  // fit the contents". Drawn bare it is a `Text` that changes height and nothing visibly grows.
  //
  // `kitRowWidth()` for the reason every row-shaped control here takes it: the button has no width
  // of its own, and the wrap sandbox would resolve one that is not the kit's content column.
  Button(
    onClick = {},
    modifier = Modifier.kitRowWidth(),
    label = { FadingExpandingLabel(text = fadingLabelText(lines)) },
  )
}

/**
 * The variable-font axes the animated numeral travels along, and the sizes it travels between.
 *
 * Declared here rather than inline because `Motion.AnimatedTextMotion` draws the same numeral
 * moving: a recording seeded from a second copy of these numbers is a recording of something
 * slightly other than the component.
 *
 * **Weight AND size, not weight alone.** The registry interpolates font variation settings, so on a
 * face that carries a `wght` axis the numeral thickens; on one that does not it simply does not
 * move. The size lerp is what makes the animation legible either way, and it is what the library's
 * own samples animate too — the digit grows as it counts.
 */
private val ANIMATED_TEXT_START_AXES = FontVariation.Settings(FontVariation.weight(400))
private val ANIMATED_TEXT_END_AXES = FontVariation.Settings(FontVariation.weight(900))

/** The numeral's size at each end of the animation. */
private val AnimatedTextStartSize = 30.sp
private val AnimatedTextEndSize = 48.sp

/**
 * The registry [AnimatedText] animates through, built from the axes and sizes above.
 *
 * `numeralMedium` is the role, because this is a glanceable hero digit and that is the role Wear's
 * type scale publishes for one — the same token the pickers give their options.
 */
@RequiresApi(31)
@Composable
private fun animatedNumeralFonts() =
  rememberAnimatedTextFontRegistry(
    startFontVariationSettings = ANIMATED_TEXT_START_AXES,
    endFontVariationSettings = ANIMATED_TEXT_END_AXES,
    textStyle = MaterialTheme.typography.numeralMedium,
    startFontSize = AnimatedTextStartSize,
    endFontSize = AnimatedTextEndSize,
  )

/**
 * The numeral itself, shared by the card below and by `Motion.AnimatedTextMotion`.
 *
 * One function rather than two copies, because `motionPreview` promises the recording is a
 * recording OF this component: a second spelling of the same call is where the two quietly stop
 * being the same picture. [progress] is a lambda for the same reason the component takes one — the
 * recording drives it from an animation that must not recompose the text on every frame.
 */
@RequiresApi(31)
@Composable
internal fun AnimatedNumeralText(progress: () -> Float) {
  // A two-digit numeral, because the component's own size lerp is what is being shown and a single
  // glyph makes the width change hard to read. `AnimatedText` shapes the text itself on a
  // `Canvas` — it is not a `Text` — so the string is passed straight through rather than styled.
  AnimatedText(text = "24", fontRegistry = animatedNumeralFonts(), progressFraction = progress)
}

@CatalogComponent(
  id = "AnimatedText",
  noReference =
    "The kit publishes no animated-text set: this is a motion treatment — a numeral travelling " +
      "along a variable font's axes and between two sizes — rather than a text style, which is " +
      "why the library ships it as its own component taking a font registry and a progress " +
      "lambda instead of a `TextStyle`.",
  caption =
    "A numeral animated along a variable font's weight axis and between two sizes; the still is " +
      "one frame of it.",
  motionPreview = "AnimatedTextMotion",
)
@CatalogModes
// `progress` is the component's only real argument, and every cell here is a frame of the
// animation rather than a state the component has. They are published anyway because the ends are
// what a reader is choosing between — how big, how heavy — and the recording is what shows the
// travel between them.
@OverrideVariant(name = "start", floats = ["progress=0.0"])
@OverrideVariant(name = "midway", floats = ["progress=0.5"])
@RequiresApi(31)
@Composable
fun AnimatedNumeral(progress: Float = 1f) = Sticker { AnimatedNumeralText { progress } }
