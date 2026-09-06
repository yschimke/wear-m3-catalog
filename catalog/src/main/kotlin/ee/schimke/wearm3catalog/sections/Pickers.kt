@file:CatalogGroup(name = "Pickers", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.PickerGroup
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import androidx.wear.compose.material3.rememberPickerState
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.FullScreenSticker
import java.time.LocalDate
import java.time.LocalTime

// The kit's single `Picker` set carries `Type = Single Column | Date Picker (…) | Time Picker …`,
// and Compose splits those across three functions — `Picker`, `DatePicker`, `TimePicker` — so the
// Type axis splits and the orderings within each fold as cells (AGENTS.md).
//
// PINNED, ALWAYS. A picker opened on "today" or "now" renders differently on every nightly publish,
// and the delivery branch's history becomes noise.
//
// PINNED TO THE KIT'S OWN INSTANT, which is not the one this file used to carry. The kit's `Picker`
// cells draw **1 January** and **00:00** — its date wheels read `31 / 01 / 02` beside `Dec / Jan /
// Feb`, and every clock cell reads `00 : 00` — so a comparison against 19 August at 10:10 differed
// on every visible wheel of every picker cell. A pinned instant is only worth pinning to the kit's:
// the value is arbitrary to us and load-bearing for the diff.
//
// These fill the screen — a picker IS the screen it appears on — so they publish on the round
// frame.

/**
 * What the 225dp capture means to the kit, for the one set that publishes screen size as an axis.
 *
 * `CatalogFullScreenModes` draws every full-screen sticker at the five sizes the kit recognises and
 * folds the four non-base ones under their component as `<dp>dp` cells. A folded cell is seeded
 * with the width it was drawn at — a fact about the render, not a value any kit vocabulary contains
 * — so it resolves against nothing, which is the right answer for every other set here: they are
 * drawn at 192 only, and the projector reports those captures as renders with no kit counterpart
 * rather than pretending to have matched them.
 *
 * `Picker` is the exception. Its set publishes `Larger Screen (BP)` as a variant property and draws
 * 21 cells behind `Yes`, at 225 — so the pictures existed all along and were compared against
 * nothing (compose-ai-tools#4827). Declaring the mapping is what lets the 225dp capture pair with
 * the node it was always the counterpart of.
 *
 * A `const` shared by the three components of that one set rather than the literal spelled out
 * three times: it is one fact about one kit set, and three copies is three places for the kit's
 * spelling to drift out of sync with the kit.
 */
private const val BP_225 = "225=Larger Screen (BP)=Yes"

private val PINNED_DATE: LocalDate = LocalDate.of(2026, 1, 1)
private val PINNED_TIME: LocalTime = LocalTime.of(0, 0)

/**
 * The kit's `Type` axis for the date wheels, as the closed set the picker actually takes.
 *
 * `@KnobValue` carries the value each `@OverrideVariant(strings = ["order=…"])` already seeds and
 * the kit already spells, so the constant can be named the Kotlin way without moving the seed
 * vocabulary a single character.
 */
enum class DateOrder {
  @KnobValue("day") Day,
  @KnobValue("month") Month,
  @KnobValue("year") Year,
}

/** The kit's `Limit` axis: which side of the pinned date the valid range is spent on. */
enum class DateLimit {
  @KnobValue("none") None,
  @KnobValue("future") Future,
  @KnobValue("past") Past,
}

@CatalogComponent(
  id = "DatePicker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8942",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "Three wheels for a date, with the kit's field orderings folded in.",
  breakpointKit = [BP_225],
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "month-first",
  strings = ["order=month"],
  kitAxis = "Type",
  kitValue = "Date Picker (Month first)",
)
// The kit publishes `Type=Date Picker (Year first)` ONLY under a limit — `Future only` and
// `Past only`, never `Limit=None` — so a `year-first` cell naming the type alone asked for a node
// between the ones it drew, and the projector said so: `no counterpart for order=year`. Both cells
// declare the pair, and the year-first render now carries a limit the way the kit's does.
@OverrideVariant(
  name = "year-first",
  strings = ["order=year", "limit=future"],
  kitProps = ["Type=Date Picker (Year first)", "Limit=Future only", "Focus=One"],
)
@OverrideVariant(
  name = "year-first-past-only",
  strings = ["order=year", "limit=past"],
  kitProps = ["Type=Date Picker (Year first)", "Limit=Past only", "Focus=One"],
  secondary = true,
)
@SettledPreview
@Composable
fun DateWheels(
  order: DateOrder = DateOrder.Day,
  limit: DateLimit = DateLimit.None,
) = FullScreenSticker {
  val type =
    when (order) {
      DateOrder.Month -> DatePickerType.MonthDayYear
      DateOrder.Year -> DatePickerType.YearMonthDay
      DateOrder.Day -> DatePickerType.DayMonthYear
    }
  // The kit's `Limit` axis, as the two bounds `DatePicker` takes. The pinned date is the bound in
  // both directions, so `future` starts the valid range at the date on the wheels and `past` ends
  // it there — which is what the kit's cells draw, a picker with half its range already spent.
  // Branched rather than passing a bound of `null`: `minValidDate` / `maxValidDate` are
  // non-nullable with library defaults spanning a century either way, so `none` has to be the call
  // that names neither.
  when (limit) {
    DateLimit.Future ->
      DatePicker(
        initialDate = PINNED_DATE,
        onDatePicked = {},
        minValidDate = PINNED_DATE,
        datePickerType = type,
      )
    DateLimit.Past ->
      DatePicker(
        initialDate = PINNED_DATE,
        onDatePicked = {},
        maxValidDate = PINNED_DATE,
        datePickerType = type,
      )
    DateLimit.None ->
      DatePicker(initialDate = PINNED_DATE, onDatePicked = {}, datePickerType = type)
  }
}

/**
 * The kit's clock-format axis. The constants are backticked because a Kotlin identifier cannot
 * begin with a digit — which is precisely why the seed text is declared rather than the constant
 * renamed: `format=24s` is what the `@OverrideVariant` and the kit both already say.
 */
enum class ClockFormat {
  @KnobValue("12") `12`,
  @KnobValue("24") `24`,
  @KnobValue("24s") `24s`,
}

@CatalogComponent(
  id = "TimePicker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8697",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "Wheels for a time, pinned to midnight; the kit's clock formats fold in as cells.",
  breakpointKit = [BP_225],
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "24-hour",
  strings = ["format=24"],
  kitAxis = "Type",
  kitValue = "Time Picker 24",
)
@OverrideVariant(
  name = "24-hour-with-seconds",
  strings = ["format=24s"],
  kitAxis = "Type",
  kitValue = "Time Picker 24 + Seconds",
)
@SettledPreview
@Composable
fun TimeWheels(format: ClockFormat = ClockFormat.`12`) = FullScreenSticker {
  val type =
    when (format) {
      ClockFormat.`24` -> TimePickerType.HoursMinutes24H
      ClockFormat.`24s` -> TimePickerType.HoursMinutesSeconds24H
      ClockFormat.`12` -> TimePickerType.HoursMinutesAmPm12H
    }
  TimePicker(initialTime = PINNED_TIME, onTimePicked = {}, timePickerType = type)
}

@CatalogComponent(
  id = "Picker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8581",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "One wheel of options — the primitive the date and time pickers are built from.",
  breakpointKit = [BP_225],
)
@CatalogFullScreenModes
@Composable
fun SingleColumnPicker() = FullScreenSticker {
  val state = rememberPickerState(initialNumberOfOptions = 25, initiallySelectedIndex = 0)
  // `fillMaxSize()`, and it is not decoration. `Picker` hands the caller's modifier to a plain
  // `Box` that wraps its content — so with no modifier the wheel takes the full height (the lazy
  // column inside fills it) but only its own content's width, and a wrapped child lands at the
  // frame's top-start. That published a picker pinned to the left edge of the watch with the round
  // mask biting a piece out of every option. The library's own `DatePicker` never hits it because
  // each of its `PickerGroupItem`s is sized `Modifier.width(…).fillMaxHeight()` inside a centred
  // row.
  //
  // A full-screen picker gets the screen: the wheel then centres on it, and the scaling and the
  // top/bottom gradients read the way they do on the watch.
  Picker(
    state = state,
    contentDescription = { "Minutes" },
    modifier = Modifier.fillMaxSize(),
  ) { index ->
    // The option style is the caller's to supply — `Picker` styles nothing. This is the token the
    // date and time pickers use for their own options at this screen size
    // (`DatePickerTokens.ContentLargeTypography`), so the primitive reads like the wheels it
    // builds; the theme's body default drew numerals a third of the size and stacked fifteen of
    // them up the screen.
    // Zero-padded, and starting at zero: the kit's cell reads `24 / 00 / 01` down the wheel,
    // which is 25 options rendered "%02d" resting on the first. Counting by fives from 25 put
    // three numbers on screen that the reference does not contain.
    Text(index.toString().padStart(2, '0'), style = MaterialTheme.typography.numeralMedium)
  }
}

// `PickerGroup` is a Wear Compose component with no kit set, and the reason is the same shape as
// `ButtonGroup`'s: the kit draws multi-column pickers as the two it publishes — `Date Picker (…)`
// and `Time Picker …`, both cells of the one `Picker` set above — rather than as the container that
// generalises them. There is no `Type` value for "some columns an app chose", because a kit cannot
// draw one. The library disagrees, and it is the library a reader of this sheet is calling: this is
// what `DatePicker` and `TimePicker` are built out of, and what an app builds its own wheels from.
// So it enters through the second door (AGENTS.md) with the reason stated
// ([#311](https://github.com/yschimke/wear-m3-catalog/issues/311)).
@CatalogComponent(
  id = "PickerGroup",
  noReference =
    "The kit publishes no picker-group set — its multi-column cells are the `Date Picker` and " +
      "`Time Picker` types of the one `Picker` set, which this sheet draws as `DatePicker` and " +
      "`TimePicker`. The container those are built from is a Wear Compose component with no kit " +
      "counterpart.",
  caption = "Several wheels side by side, with the focused one centred on the screen.",
)
@CatalogFullScreenModes
@OverrideVariant(name = "two-columns", ints = ["columns=2"])
// Which column has the focus is the other half of what this component does — `autoCenter` moves
// the group so the selected wheel sits on the screen's centre line, and the unselected ones scale
// down beside it. Both ends get a cell, because the base is the MIDDLE one: three 56dp columns are
// 168dp of a 192dp screen, so centring an outer column pushes the far one past the bezel, which is
// a true picture of the component and a poor picture of a picker group. The kit's own multi-column
// cells (`DatePicker`, `TimePicker`) rest on their middle wheel for the same reason.
@OverrideVariant(name = "first-column-selected", ints = ["selected=0"])
@OverrideVariant(name = "last-column-selected", ints = ["selected=2"])
// Seeded off the middle column ON PURPOSE, and it is the one cell here that turns two knobs.
// `autoCenter` moves the group so the SELECTED wheel lands on the centre line — and with the
// middle of three selected it is already there, so switching it off against the base changes
// nothing and publishes the base render under a second name. Against `first-column-selected`,
// which is the same seed with the centring left on, it is exactly one knob's difference.
@OverrideVariant(
  name = "no-auto-centering",
  booleans = ["autoCenter=false"],
  ints = ["selected=0"],
)
@SettledPreview
@Composable
fun PickerColumns(
  columns: Int = 3,
  selected: Int = 1,
  autoCenter: Boolean = true,
) = FullScreenSticker {
  // Clamped, because both are live knobs and a group asked for the fourth of three columns has
  // nothing to select. The pickers themselves are built per column rather than pinned at three, so
  // the `columns` knob really changes the group rather than hiding wheels that are still there.
  val columns = columns.coerceIn(1, 4)
  val states = List(columns) { rememberPickerState(initialNumberOfOptions = 25) }
  var selectedColumn by
    remember(columns, selected) { mutableIntStateOf(selected.coerceIn(0, columns - 1)) }
  PickerGroup(
    // The group's own knob, and the one a still cannot otherwise show: with it off the wheels stay
    // where the row put them, and the selected one is no longer the one on the centre line.
    autoCenter = autoCenter,
    // Which `PickerState` the group centres ON. Handing it the selected column's state is what
    // makes `autoCenter` mean anything: given none, the group has nothing to centre.
    selectedPickerState = states[selectedColumn],
    // The screen, for the reason `SingleColumnPicker` takes it above: `PickerGroup` lays its
    // children out in a row that wraps what it is given, so a group with no size lands at the
    // frame's top-start with the round mask biting into every wheel.
    modifier = Modifier.fillMaxSize(),
  ) {
    states.forEachIndexed { column, state ->
      // A fixed width and the full height, which is how the library's own `DatePicker` sizes each
      // of its columns. Left to wrap, a wheel is as wide as its widest option and the group's
      // spacing collapses.
      PickerGroupItem(
        pickerState = state,
        selected = column == selectedColumn,
        onSelected = { selectedColumn = column },
        modifier = Modifier.width(56.dp).fillMaxHeight(),
        contentDescription = { "Column ${column + 1}" },
      ) { index, _ ->
        // The same numeral token and the same zero-padded options as the single wheel above, so
        // the group reads as three of the primitive it is a group of rather than as another
        // component that happens to scroll.
        Text(index.toString().padStart(2, '0'), style = MaterialTheme.typography.numeralMedium)
      }
    }
  }
}
