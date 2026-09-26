package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.CurvedScope
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.ArcProgressIndicator
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ConfirmationDialogContent
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FailureConfirmationDialogContent
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.LinearProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SegmentedCircularProgressIndicator
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.SliderDefaults
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.SuccessConfirmationDialogContent
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import androidx.wear.compose.material3.VerticalPageIndicator
import androidx.wear.compose.material3.confirmationDialogCurvedText
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Catalog-owned adapters, invoking the real Wear Compose Multiplatform components. */
val wearCanvasAdapters = canvasAdapterRegistry {
  register("wear-m3/list-header") {
    ListHeader(modifier = modifier) {
      Text(
        text = string("text"),
        maxLines = integer("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
        overflow = textOverflow(),
      )
    }
  }
  register("wear-m3/list-sub-header") {
    ListSubHeader(modifier = modifier) {
      Text(
        text = string("text"),
        maxLines = integer("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
        overflow = textOverflow(),
      )
    }
  }
  register("wear-m3/switch-button") {
    SwitchButton(
      checked = boolean("checked"),
      onCheckedChange = {},
      modifier = modifier.fillMaxWidth(),
      enabled = boolean("enabled", true),
      label = { Label("label") },
      secondaryLabel = optionalLabel("secondaryLabel"),
    )
  }
  register("wear-m3/checkbox-button") {
    CheckboxButton(
      checked = boolean("checked"),
      onCheckedChange = {},
      modifier = modifier.fillMaxWidth(),
      enabled = boolean("enabled", true),
      label = { Label("label") },
      secondaryLabel = optionalLabel("secondaryLabel"),
    )
  }
  register("wear-m3/radio-button") {
    RadioButton(
      selected = boolean("selected"),
      onSelect = {},
      modifier = modifier.fillMaxWidth(),
      enabled = boolean("enabled", true),
      label = { Label("label") },
      secondaryLabel = optionalLabel("secondaryLabel"),
    )
  }
  register("wear-m3/slider") {
    Slider(
      value = float("value"),
      onValueChange = {},
      modifier = modifier.fillMaxWidth(),
      enabled = boolean("enabled", true),
      steps = integer("steps").coerceAtLeast(0),
      valueRange = guardedRange(),
      segmented = boolean("segmented"),
    )
  }
  register("wear-m3/stepper") {
    Stepper(
      value = float("value"),
      onValueChange = {},
      steps = integer("steps").coerceAtLeast(0),
      modifier = modifier.fillMaxWidth(),
      enabled = boolean("enabled", true),
      valueRange = guardedRange(),
      decreaseIcon = { SliderDefaults.DecreaseIcon() },
      increaseIcon = { SliderDefaults.IncreaseIcon() },
      content = { Slot("content") },
    )
  }
  register("wear-m3/progress-indicator") {
    val progress = float("progress").coerceIn(0f, 1f)
    when (string("variant")) {
      "linear" ->
        LinearProgressIndicator(
          progress = { progress },
          modifier = modifier.fillMaxWidth(),
          enabled = boolean("enabled", true),
        )
      "segmented-circular" ->
        SegmentedCircularProgressIndicator(
          segmentCount = integer("segments", 1).coerceAtLeast(1),
          progress = { progress },
          modifier = modifier,
          enabled = boolean("enabled", true),
        )
      "arc" -> ArcProgressIndicator(modifier = modifier)
      else ->
        CircularProgressIndicator(
          progress = { progress },
          modifier = modifier,
          enabled = boolean("enabled", true),
        )
    }
  }
  register("wear-m3/page-indicator") {
    val state = rememberPagerState(initialPage = 0) { 4 }
    if (string("variant") == "vertical") {
      VerticalPageIndicator(pagerState = state, modifier = modifier)
    } else {
      HorizontalPageIndicator(pagerState = state, modifier = modifier)
    }
  }
  register("wear-m3/edge-button") {
    EdgeButton(
      onClick = {},
      buttonSize = edgeButtonSize(),
      modifier = modifier,
      enabled = boolean("enabled", true),
    ) {
      Slot("content")
    }
  }
  register("wear-m3/button-group") {
    ButtonGroup(modifier = modifier.fillMaxWidth()) { Items("children") { Content(Modifier) } }
  }
  register("wear-m3/icon-button") {
    val canvas = this
    val content: @Composable BoxScope.() -> Unit = { canvas.Slot("content") }
    when (string("variant")) {
      "filled" ->
        FilledIconButton(
          onClick = {},
          modifier = modifier,
          enabled = boolean("enabled", true),
          content = content,
        )
      "filled-tonal" ->
        FilledTonalIconButton(
          onClick = {},
          modifier = modifier,
          enabled = boolean("enabled", true),
          content = content,
        )
      "outlined" ->
        OutlinedIconButton(
          onClick = {},
          modifier = modifier,
          enabled = boolean("enabled", true),
          content = content,
        )
      else ->
        IconButton(
          onClick = {},
          modifier = modifier,
          enabled = boolean("enabled", true),
          content = content,
        )
    }
  }
  register("wear-m3/text-button") {
    TextButton(
      onClick = {},
      modifier = modifier,
      enabled = boolean("enabled", true),
      colors =
        when (string("variant")) {
          "filled" -> TextButtonDefaults.filledTextButtonColors()
          "filled-tonal" -> TextButtonDefaults.filledTonalTextButtonColors()
          "filled-variant" -> TextButtonDefaults.filledVariantTextButtonColors()
          "outlined" -> TextButtonDefaults.outlinedTextButtonColors()
          else -> TextButtonDefaults.textButtonColors()
        },
    ) {
      Slot("content")
    }
  }
  register("wear-m3/alert-dialog") { if (boolean("visible", true)) AlertDialog() }
  register("wear-m3/confirmation-dialog") { if (boolean("visible", true)) ConfirmationDialog() }
  register("wear-m3/open-on-phone-dialog") { if (boolean("visible", true)) OpenOnPhoneDialog() }
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

private fun CanvasNodeScope.textOverflow(): TextOverflow =
  when (string("overflow")) {
    "ellipsis" -> TextOverflow.Ellipsis
    "visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
  }

@Composable
private fun CanvasNodeScope.Label(name: String) {
  if (node.slots[name].isNullOrEmpty()) Text(string(name)) else Slot(name)
}

private fun CanvasNodeScope.optionalLabel(name: String): (@Composable RowScope.() -> Unit)? {
  val canvas = this
  return when {
    !node.slots[name].isNullOrEmpty() -> ({ canvas.Slot(name) })
    string(name).isNotEmpty() -> ({ Text(canvas.string(name)) })
    else -> null
  }
}

private fun CanvasNodeScope.guardedRange(): ClosedFloatingPointRange<Float> {
  val start = float("valueFrom")
  val end = float("valueTo", 1f)
  return if (end > start) start..end else 0f..1f
}

private fun CanvasNodeScope.edgeButtonSize(): EdgeButtonSize =
  when (string("size")) {
    "extra-small" -> EdgeButtonSize.ExtraSmall
    "medium" -> EdgeButtonSize.Medium
    "large" -> EdgeButtonSize.Large
    else -> EdgeButtonSize.Small
  }

@Composable
private fun CanvasNodeScope.AlertDialog() {
  val canvas = this
  val title: @Composable () -> Unit = { Text(canvas.string("title")) }
  val text: (@Composable () -> Unit)? =
    string("text").takeIf { it.isNotEmpty() }?.let { value -> { Text(value) } }
  val content: androidx.wear.compose.foundation.lazy.ScalingLazyListScope.() -> Unit = {
    item { canvas.Slot("content") }
  }
  val hasConfirm = !node.slots["confirmButton"].isNullOrEmpty()
  val hasDismiss = !node.slots["dismissButton"].isNullOrEmpty()
  if (hasConfirm && hasDismiss) {
    AlertDialogContent(
      confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
      dismissButton = { AlertDialogDefaults.DismissButton(onClick = {}) },
      modifier = modifier,
      title = title,
      text = text,
      content = content,
    )
  } else {
    AlertDialogContent(
      modifier = modifier,
      title = title,
      text = text,
      content = content,
    )
  }
}

@Composable
private fun CanvasNodeScope.ConfirmationDialog() {
  val style = ConfirmationDialogDefaults.curvedTextStyle
  val curvedText: (CurvedScope.() -> Unit)? =
    string("text")
      .takeIf { it.isNotEmpty() }
      ?.let { value -> { confirmationDialogCurvedText(value, style) } }
  when (string("variant")) {
    "success" -> SuccessConfirmationDialogContent(modifier = modifier, curvedText = curvedText)
    "failure" -> FailureConfirmationDialogContent(modifier = modifier, curvedText = curvedText)
    else -> ConfirmationDialogContent(modifier = modifier, curvedText = curvedText) {}
  }
}

@Composable
private fun CanvasNodeScope.OpenOnPhoneDialog() {
  val style = OpenOnPhoneDialogDefaults.curvedTextStyle
  val curvedText: (CurvedScope.() -> Unit)? =
    string("text")
      .takeIf { it.isNotEmpty() }
      ?.let { value -> { openOnPhoneDialogCurvedText(value, style) } }
  OpenOnPhoneDialogContent(
    curvedText = curvedText,
    durationMillis = OpenOnPhoneDialogDefaults.DurationMillis,
    modifier = modifier,
  ) {
    OpenOnPhoneDialogDefaults.Icon()
  }
}
