package ee.schimke.wearm3catalog.uibuilder

import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * The pickers, apart from [wearCanvasAdapters] because the Wear port types their dates with
 * kotlinx-datetime here and with `java.time` on the JVM; everything else is common code, which is
 * what lets `WearAdapterPropertyParityTest` draw it on the JVM.
 */
val wearPickerAdapters = canvasAdapterRegistry {
  register("wear-m3/date-picker") {
    DatePicker(
      initialDate =
        runCatching { LocalDate.parse(string("initialDate")) }.getOrElse { LocalDate(2026, 1, 1) },
      onDatePicked = {},
      modifier = modifier,
      datePickerType =
        when (string("type")) {
          "day-month-year" -> DatePickerType.DayMonthYear
          "month-day-year" -> DatePickerType.MonthDayYear
          else -> DatePickerType.YearMonthDay
        },
    )
  }
  register("wear-m3/time-picker") {
    TimePicker(
      initialTime =
        runCatching { LocalTime.parse(string("initialTime")) }.getOrElse { LocalTime(10, 10) },
      onTimePicked = {},
      modifier = modifier,
      timePickerType =
        when (string("type")) {
          "hours-minutes-am-pm" -> TimePickerType.HoursMinutesAmPm12H
          "hours-minutes-seconds" -> TimePickerType.HoursMinutesSeconds24H
          else -> TimePickerType.HoursMinutes24H
        },
    )
  }
}
