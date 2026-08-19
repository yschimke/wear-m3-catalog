@file:CatalogGroup(name = "Pickers", section = "Selection")

package ee.schimke.wearm3catalog.sections

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import androidx.wear.compose.material3.rememberPickerState
import ee.schimke.composeai.overrides.previewOverrideString
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
// and the delivery branch's history becomes noise. The date is a fixed instant and the time is
// 10:10, which is also what the kit draws.
//
// These fill the screen — a picker IS the screen it appears on — so they publish on the round
// frame.

private val PINNED_DATE: LocalDate = LocalDate.of(2026, 8, 19)
private val PINNED_TIME: LocalTime = LocalTime.of(10, 10)

@CatalogComponent(
  id = "DatePicker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "Three wheels for a date, with the kit's field orderings folded in.",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "month-first",
  strings = ["order=month"],
  kitAxis = "Type",
  kitValue = "Date Picker (Month first)",
)
@OverrideVariant(
  name = "year-first",
  strings = ["order=year"],
  kitAxis = "Type",
  kitValue = "Date Picker (Year first)",
)
@Composable
fun DateWheels() = FullScreenSticker {
  val type =
    when (previewOverrideString("order", "day")) {
      "month" -> DatePickerType.MonthDayYear
      "year" -> DatePickerType.YearMonthDay
      else -> DatePickerType.DayMonthYear
    }
  DatePicker(initialDate = PINNED_DATE, onDatePicked = {}, datePickerType = type)
}

@CatalogComponent(
  id = "TimePicker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "Wheels for a time, pinned to 10:10; the kit's clock formats fold in as cells.",
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
    when (previewOverrideString("format", "12")) {
      "24" -> TimePickerType.HoursMinutes24H
      "24s" -> TimePickerType.HoursMinutesSeconds24H
      else -> TimePickerType.HoursMinutesAmPm12H
    }
  TimePicker(initialTime = PINNED_TIME, onTimePicked = {}, timePickerType = type)
}

@CatalogComponent(
  id = "Picker",
  reference = "figma:B24oss2tTeXAFykyeyusz0/43678:8580",
  caption = "One wheel of options — the primitive the date and time pickers are built from.",
)
@CatalogFullScreenModes
@Composable
fun SingleColumnPicker() = FullScreenSticker {
  val state = rememberPickerState(initialNumberOfOptions = 12, initiallySelectedIndex = 4)
  Picker(state = state, contentDescription = { "Minutes" }) { index -> Text("${(index + 1) * 5}") }
}
