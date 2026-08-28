@file:CatalogGroup(name = "Pickers", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import androidx.wear.compose.material3.rememberPickerState
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
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

private val PINNED_DATE: LocalDate = LocalDate.of(2026, 1, 1)
private val PINNED_TIME: LocalTime = LocalTime.of(0, 0)

@CatalogComponent(
  id = "DatePicker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8942",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "Three wheels for a date, with the kit's field orderings folded in.",
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
)
@Composable
fun DateWheels() = FullScreenSticker {
  val type =
    when (previewOverrideChoice("order", "day", listOf("day", "month", "year"))) {
      "month" -> DatePickerType.MonthDayYear
      "year" -> DatePickerType.YearMonthDay
      else -> DatePickerType.DayMonthYear
    }
  // The kit's `Limit` axis, as the two bounds `DatePicker` takes. The pinned date is the bound in
  // both directions, so `future` starts the valid range at the date on the wheels and `past` ends
  // it there — which is what the kit's cells draw, a picker with half its range already spent.
  // Branched rather than passing a bound of `null`: `minValidDate` / `maxValidDate` are
  // non-nullable with library defaults spanning a century either way, so `none` has to be the call
  // that names neither.
  when (previewOverrideChoice("limit", "none", listOf("none", "future", "past"))) {
    "future" ->
      DatePicker(
        initialDate = PINNED_DATE,
        onDatePicked = {},
        minValidDate = PINNED_DATE,
        datePickerType = type,
      )
    "past" ->
      DatePicker(
        initialDate = PINNED_DATE,
        onDatePicked = {},
        maxValidDate = PINNED_DATE,
        datePickerType = type,
      )
    else -> DatePicker(initialDate = PINNED_DATE, onDatePicked = {}, datePickerType = type)
  }
}

@CatalogComponent(
  id = "TimePicker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8697",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "Wheels for a time, pinned to midnight; the kit's clock formats fold in as cells.",
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
@Composable
fun TimeWheels() = FullScreenSticker {
  val type =
    when (previewOverrideChoice("format", "12", listOf("12", "24", "24s"))) {
      "24" -> TimePickerType.HoursMinutes24H
      "24s" -> TimePickerType.HoursMinutesSeconds24H
      else -> TimePickerType.HoursMinutesAmPm12H
    }
  TimePicker(initialTime = PINNED_TIME, onTimePicked = {}, timePickerType = type)
}

@CatalogComponent(
  id = "Picker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8581",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "One wheel of options — the primitive the date and time pickers are built from.",
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
