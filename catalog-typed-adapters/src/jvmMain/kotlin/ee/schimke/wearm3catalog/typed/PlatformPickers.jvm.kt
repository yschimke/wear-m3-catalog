package ee.schimke.wearm3catalog.typed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.TimePicker
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal actual fun PlatformDatePicker(date: String, type: String, modifier: Modifier) {
  DatePicker(
    initialDate = runCatching { LocalDate.parse(date) }.getOrElse { LocalDate.of(2026, 1, 1) },
    onDatePicked = {},
    modifier = modifier,
    datePickerType = datePickerType(type),
  )
}

@Composable
internal actual fun PlatformTimePicker(time: String, type: String, modifier: Modifier) {
  TimePicker(
    initialTime = runCatching { LocalTime.parse(time) }.getOrElse { LocalTime.of(10, 10) },
    onTimePicked = {},
    modifier = modifier,
    timePickerType = timePickerType(type),
  )
}
