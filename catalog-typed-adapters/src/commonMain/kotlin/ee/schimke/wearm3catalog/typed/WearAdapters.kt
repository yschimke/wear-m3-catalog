package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.AdapterValueCodecs
import ee.schimke.composeai.discovery.ComponentRecord
import ee.schimke.composeai.discovery.TypedComponentAdapter

// One definition per compiled entry point in WearComponents.kt. Property references are compile
// checked against these models and `freeze()` checks them against the discovered signature.

data class TextProperties(
  val text: String,
  val style: String,
  val fontWeight: String,
  val fontStyle: String,
  val color: String,
  val fontSizeSp: Float,
  val lineHeightSp: Float,
  val letterSpacingSp: Float,
  val fontVariationSettings: String,
  val fontFeatureSettings: String,
  val minLines: Int,
  val maxLines: Int,
  val softWrap: Boolean,
  val overflow: String,
  val textAlign: String,
  val textDecoration: String,
)

class TextAdapter(record: ComponentRecord) :
  TypedComponentAdapter<TextProperties>("wear-m3/text", record) {
  val text =
    property(
      TextProperties::text,
      AdapterValueCodecs.String,
      WearTextDefaults.text,
      bindable = false,
    )
  val style =
    property(
      TextProperties::style,
      AdapterValueCodecs.String,
      WearTextDefaults.style,
      bindable = false,
    )
  val fontWeight =
    property(
      TextProperties::fontWeight,
      AdapterValueCodecs.String,
      WearTextDefaults.fontWeight,
      bindable = false,
    )
  val fontStyle =
    property(
      TextProperties::fontStyle,
      AdapterValueCodecs.String,
      WearTextDefaults.fontStyle,
      bindable = false,
    )
  val color =
    property(
      TextProperties::color,
      AdapterValueCodecs.String,
      WearTextDefaults.color,
      bindable = false,
    )
  val fontSizeSp =
    property(
      TextProperties::fontSizeSp,
      WearValueCodecs.Float,
      WearTextDefaults.fontSizeSp,
      bindable = false,
    )
  val lineHeightSp =
    property(
      TextProperties::lineHeightSp,
      WearValueCodecs.Float,
      WearTextDefaults.lineHeightSp,
      bindable = false,
    )
  val letterSpacingSp =
    property(
      TextProperties::letterSpacingSp,
      WearValueCodecs.Float,
      WearTextDefaults.letterSpacingSp,
      bindable = false,
    )
  val fontVariationSettings =
    property(
      TextProperties::fontVariationSettings,
      AdapterValueCodecs.String,
      WearTextDefaults.fontVariationSettings,
      bindable = false,
    )
  val fontFeatureSettings =
    property(
      TextProperties::fontFeatureSettings,
      AdapterValueCodecs.String,
      WearTextDefaults.fontFeatureSettings,
      bindable = false,
    )
  val minLines =
    property(
      TextProperties::minLines,
      WearValueCodecs.Int,
      WearTextDefaults.minLines,
      bindable = false,
    )
  val maxLines =
    property(
      TextProperties::maxLines,
      WearValueCodecs.Int,
      WearTextDefaults.maxLines,
      bindable = false,
    )
  val softWrap =
    property(
      TextProperties::softWrap,
      AdapterValueCodecs.Boolean,
      WearTextDefaults.softWrap,
      bindable = false,
    )
  val overflow =
    property(
      TextProperties::overflow,
      AdapterValueCodecs.String,
      WearTextDefaults.overflow,
      bindable = false,
    )
  val textAlign =
    property(
      TextProperties::textAlign,
      AdapterValueCodecs.String,
      WearTextDefaults.textAlign,
      bindable = false,
    )
  val textDecoration =
    property(
      TextProperties::textDecoration,
      AdapterValueCodecs.String,
      WearTextDefaults.textDecoration,
      bindable = false,
    )
}

data class IconProperties(
  val iconKey: String,
  val sizeDp: Float,
  val contentDescription: String,
  val color: String,
)

class IconAdapter(record: ComponentRecord) :
  TypedComponentAdapter<IconProperties>("wear-m3/icon", record) {
  val iconKey =
    property(
      IconProperties::iconKey,
      AdapterValueCodecs.String,
      WearIconDefaults.iconKey,
      bindable = false,
    )
  val sizeDp =
    property(
      IconProperties::sizeDp,
      WearValueCodecs.Float,
      WearIconDefaults.sizeDp,
      bindable = true,
    )
  val contentDescription =
    property(
      IconProperties::contentDescription,
      AdapterValueCodecs.String,
      WearIconDefaults.contentDescription,
      bindable = false,
    )
  val color =
    property(
      IconProperties::color,
      AdapterValueCodecs.String,
      WearIconDefaults.color,
      bindable = false,
    )
}

data class ListHeaderProperties(val text: String, val maxLines: Int, val overflow: String)

class ListHeaderAdapter(record: ComponentRecord) :
  TypedComponentAdapter<ListHeaderProperties>("wear-m3/list-header", record) {
  val text =
    property(
      ListHeaderProperties::text,
      AdapterValueCodecs.String,
      WearListHeaderDefaults.text,
      bindable = false,
    )
  val maxLines =
    property(
      ListHeaderProperties::maxLines,
      WearValueCodecs.Int,
      WearListHeaderDefaults.maxLines,
      bindable = false,
    )
  val overflow =
    property(
      ListHeaderProperties::overflow,
      AdapterValueCodecs.String,
      WearListHeaderDefaults.overflow,
      bindable = false,
    )
}

data class ListSubHeaderProperties(val text: String, val maxLines: Int, val overflow: String)

class ListSubHeaderAdapter(record: ComponentRecord) :
  TypedComponentAdapter<ListSubHeaderProperties>("wear-m3/list-sub-header", record) {
  val text =
    property(
      ListSubHeaderProperties::text,
      AdapterValueCodecs.String,
      WearListHeaderDefaults.text,
      bindable = false,
    )
  val maxLines =
    property(
      ListSubHeaderProperties::maxLines,
      WearValueCodecs.Int,
      WearListHeaderDefaults.maxLines,
      bindable = false,
    )
  val overflow =
    property(
      ListSubHeaderProperties::overflow,
      AdapterValueCodecs.String,
      WearListHeaderDefaults.overflow,
      bindable = false,
    )
}

data class CardProperties(
  val variant: String,
  val containerColor: String,
  val contentColor: String,
  val onClick: () -> Unit,
)

class CardAdapter(record: ComponentRecord) :
  TypedComponentAdapter<CardProperties>("wear-m3/card", record) {
  val variant =
    property(
      CardProperties::variant,
      AdapterValueCodecs.String,
      WearCardDefaults.variant,
      bindable = false,
    )
  val containerColor =
    property(
      CardProperties::containerColor,
      AdapterValueCodecs.String,
      WearCardDefaults.containerColor,
      bindable = false,
    )
  val contentColor =
    property(
      CardProperties::contentColor,
      AdapterValueCodecs.String,
      WearCardDefaults.contentColor,
      bindable = false,
    )
  val onClick = event(CardProperties::onClick)
  val content = slot("content", required = true)
}

data class EdgeButtonProperties(
  val size: String,
  val enabled: Boolean,
  val containerColor: String,
  val contentColor: String,
  val onClick: () -> Unit,
)

class EdgeButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<EdgeButtonProperties>("wear-m3/edge-button", record) {
  val size =
    property(
      EdgeButtonProperties::size,
      AdapterValueCodecs.String,
      WearEdgeButtonDefaults.size,
      bindable = false,
    )
  val enabled =
    property(
      EdgeButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearEdgeButtonDefaults.enabled,
      bindable = false,
    )
  val containerColor =
    property(
      EdgeButtonProperties::containerColor,
      AdapterValueCodecs.String,
      WearEdgeButtonDefaults.containerColor,
      bindable = false,
    )
  val contentColor =
    property(
      EdgeButtonProperties::contentColor,
      AdapterValueCodecs.String,
      WearEdgeButtonDefaults.contentColor,
      bindable = false,
    )
  val onClick = event(EdgeButtonProperties::onClick)
  val content = slot("content", required = true)
}

data class ButtonProperties(
  val variant: String,
  val enabled: Boolean,
  val containerColor: String,
  val contentColor: String,
  val onClick: () -> Unit,
)

class ButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<ButtonProperties>("wear-m3/button", record) {
  val variant =
    property(
      ButtonProperties::variant,
      AdapterValueCodecs.String,
      WearButtonDefaults.variant,
      bindable = false,
    )
  val enabled =
    property(
      ButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearButtonDefaults.enabled,
      bindable = false,
    )
  val containerColor =
    property(
      ButtonProperties::containerColor,
      AdapterValueCodecs.String,
      WearButtonDefaults.containerColor,
      bindable = false,
    )
  val contentColor =
    property(
      ButtonProperties::contentColor,
      AdapterValueCodecs.String,
      WearButtonDefaults.contentColor,
      bindable = false,
    )
  val onClick = event(ButtonProperties::onClick)
  val content = slot("content", required = true)
}

data class IconButtonProperties(
  val variant: String,
  val enabled: Boolean,
  val containerColor: String,
  val contentColor: String,
  val onClick: () -> Unit,
)

class IconButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<IconButtonProperties>("wear-m3/icon-button", record) {
  val variant =
    property(
      IconButtonProperties::variant,
      AdapterValueCodecs.String,
      WearIconButtonDefaults.variant,
      bindable = false,
    )
  val enabled =
    property(
      IconButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearIconButtonDefaults.enabled,
      bindable = false,
    )
  val containerColor =
    property(
      IconButtonProperties::containerColor,
      AdapterValueCodecs.String,
      WearIconButtonDefaults.containerColor,
      bindable = false,
    )
  val contentColor =
    property(
      IconButtonProperties::contentColor,
      AdapterValueCodecs.String,
      WearIconButtonDefaults.contentColor,
      bindable = false,
    )
  val onClick = event(IconButtonProperties::onClick)
  val content = slot("content", required = true)
}

data class TextButtonProperties(val variant: String, val enabled: Boolean, val onClick: () -> Unit)

class TextButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<TextButtonProperties>("wear-m3/text-button", record) {
  val variant =
    property(
      TextButtonProperties::variant,
      AdapterValueCodecs.String,
      WearTextButtonDefaults.variant,
      bindable = false,
    )
  val enabled =
    property(
      TextButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearTextButtonDefaults.enabled,
      bindable = false,
    )
  val onClick = event(TextButtonProperties::onClick)
  val content = slot("content", required = true)
}

data class CheckboxButtonProperties(
  val label: String,
  val secondaryLabel: String,
  val checked: Boolean,
  val enabled: Boolean,
  val onCheckedChange: (Boolean) -> Unit,
)

class CheckboxButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<CheckboxButtonProperties>("wear-m3/checkbox-button", record) {
  val label =
    property(
      CheckboxButtonProperties::label,
      AdapterValueCodecs.String,
      WearSelectionButtonDefaults.label,
      bindable = false,
    )
  val secondaryLabel =
    property(
      CheckboxButtonProperties::secondaryLabel,
      AdapterValueCodecs.String,
      WearSelectionButtonDefaults.secondaryLabel,
      bindable = false,
    )
  val checked =
    property(
      CheckboxButtonProperties::checked,
      AdapterValueCodecs.Boolean,
      WearSelectionButtonDefaults.checked,
      bindable = true,
    )
  val enabled =
    property(
      CheckboxButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearSelectionButtonDefaults.enabled,
      bindable = false,
    )
  val onCheckedChange = stateChange(CheckboxButtonProperties::onCheckedChange, checked)
}

data class SwitchButtonProperties(
  val label: String,
  val secondaryLabel: String,
  val checked: Boolean,
  val enabled: Boolean,
  val onCheckedChange: (Boolean) -> Unit,
)

class SwitchButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<SwitchButtonProperties>("wear-m3/switch-button", record) {
  val label =
    property(
      SwitchButtonProperties::label,
      AdapterValueCodecs.String,
      WearSelectionButtonDefaults.label,
      bindable = false,
    )
  val secondaryLabel =
    property(
      SwitchButtonProperties::secondaryLabel,
      AdapterValueCodecs.String,
      WearSelectionButtonDefaults.secondaryLabel,
      bindable = false,
    )
  val checked =
    property(
      SwitchButtonProperties::checked,
      AdapterValueCodecs.Boolean,
      WearSelectionButtonDefaults.checked,
      bindable = true,
    )
  val enabled =
    property(
      SwitchButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearSelectionButtonDefaults.enabled,
      bindable = false,
    )
  val onCheckedChange = stateChange(SwitchButtonProperties::onCheckedChange, checked)
}

data class RadioButtonProperties(
  val label: String,
  val secondaryLabel: String,
  val selected: Boolean,
  val enabled: Boolean,
  val onSelectedChange: (Boolean) -> Unit,
)

class RadioButtonAdapter(record: ComponentRecord) :
  TypedComponentAdapter<RadioButtonProperties>("wear-m3/radio-button", record) {
  val label =
    property(
      RadioButtonProperties::label,
      AdapterValueCodecs.String,
      WearSelectionButtonDefaults.label,
      bindable = false,
    )
  val secondaryLabel =
    property(
      RadioButtonProperties::secondaryLabel,
      AdapterValueCodecs.String,
      WearSelectionButtonDefaults.secondaryLabel,
      bindable = false,
    )
  val selected =
    property(
      RadioButtonProperties::selected,
      AdapterValueCodecs.Boolean,
      WearSelectionButtonDefaults.checked,
      bindable = true,
    )
  val enabled =
    property(
      RadioButtonProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearSelectionButtonDefaults.enabled,
      bindable = false,
    )
  val onSelectedChange = stateChange(RadioButtonProperties::onSelectedChange, selected)
}

data class SliderProperties(
  val value: Float,
  val valueFrom: Float,
  val valueTo: Float,
  val steps: Int,
  val segmented: String,
  val enabled: Boolean,
  val onValueChange: (Float) -> Unit,
)

class SliderAdapter(record: ComponentRecord) :
  TypedComponentAdapter<SliderProperties>("wear-m3/slider", record) {
  val value =
    property(
      SliderProperties::value,
      WearValueCodecs.Float,
      WearRangeDefaults.value,
      bindable = true,
    )
  val valueFrom =
    property(
      SliderProperties::valueFrom,
      WearValueCodecs.Float,
      WearRangeDefaults.valueFrom,
      bindable = true,
    )
  val valueTo =
    property(
      SliderProperties::valueTo,
      WearValueCodecs.Float,
      WearRangeDefaults.valueTo,
      bindable = true,
    )
  val steps =
    property(SliderProperties::steps, WearValueCodecs.Int, WearRangeDefaults.steps, bindable = true)
  val segmented =
    property(
      SliderProperties::segmented,
      AdapterValueCodecs.String,
      WearRangeDefaults.segmented,
      bindable = false,
    )
  val enabled =
    property(
      SliderProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearRangeDefaults.enabled,
      bindable = false,
    )
  val onValueChange = stateChange(SliderProperties::onValueChange, value)
}

data class StepperProperties(
  val value: Float,
  val valueFrom: Float,
  val valueTo: Float,
  val steps: Int,
  val enabled: Boolean,
  val onValueChange: (Float) -> Unit,
)

class StepperAdapter(record: ComponentRecord) :
  TypedComponentAdapter<StepperProperties>("wear-m3/stepper", record) {
  val value =
    property(
      StepperProperties::value,
      WearValueCodecs.Float,
      WearRangeDefaults.value,
      bindable = true,
    )
  val valueFrom =
    property(
      StepperProperties::valueFrom,
      WearValueCodecs.Float,
      WearRangeDefaults.valueFrom,
      bindable = true,
    )
  val valueTo =
    property(
      StepperProperties::valueTo,
      WearValueCodecs.Float,
      WearRangeDefaults.valueTo,
      bindable = true,
    )
  val steps =
    property(
      StepperProperties::steps,
      WearValueCodecs.Int,
      WearRangeDefaults.steps,
      bindable = true,
    )
  val enabled =
    property(
      StepperProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearRangeDefaults.enabled,
      bindable = false,
    )
  val onValueChange = stateChange(StepperProperties::onValueChange, value)
  val content = slot("content", required = true)
}

class ButtonGroupProperties

class ButtonGroupAdapter(record: ComponentRecord) :
  TypedComponentAdapter<ButtonGroupProperties>("wear-m3/button-group", record) {
  val children = slot("children")
}

data class DatePickerProperties(val initialDate: String, val type: String)

class DatePickerAdapter(record: ComponentRecord) :
  TypedComponentAdapter<DatePickerProperties>("wear-m3/date-picker", record) {
  val initialDate =
    property(
      DatePickerProperties::initialDate,
      AdapterValueCodecs.String,
      WearPickerDefaults.initialDate,
      bindable = false,
    )
  val type =
    property(
      DatePickerProperties::type,
      AdapterValueCodecs.String,
      WearPickerDefaults.type,
      bindable = false,
    )
}

data class TimePickerProperties(val initialTime: String, val type: String)

class TimePickerAdapter(record: ComponentRecord) :
  TypedComponentAdapter<TimePickerProperties>("wear-m3/time-picker", record) {
  val initialTime =
    property(
      TimePickerProperties::initialTime,
      AdapterValueCodecs.String,
      WearPickerDefaults.initialTime,
      bindable = false,
    )
  val type =
    property(
      TimePickerProperties::type,
      AdapterValueCodecs.String,
      WearPickerDefaults.type,
      bindable = false,
    )
}

data class AlertDialogProperties(val title: String, val text: String, val visible: Boolean)

class AlertDialogAdapter(record: ComponentRecord) :
  TypedComponentAdapter<AlertDialogProperties>("wear-m3/alert-dialog", record) {
  val title =
    property(
      AlertDialogProperties::title,
      AdapterValueCodecs.String,
      WearAlertDialogDefaults.title,
      bindable = false,
    )
  val text =
    property(
      AlertDialogProperties::text,
      AdapterValueCodecs.String,
      WearAlertDialogDefaults.text,
      bindable = false,
    )
  val visible =
    property(
      AlertDialogProperties::visible,
      AdapterValueCodecs.Boolean,
      WearAlertDialogDefaults.visible,
      bindable = true,
    )
  val content = slot("content")
  val confirmButton = slot("confirmButton")
  val dismissButton = slot("dismissButton")
}

data class ConfirmationDialogProperties(val text: String, val variant: String, val visible: Boolean)

class ConfirmationDialogAdapter(record: ComponentRecord) :
  TypedComponentAdapter<ConfirmationDialogProperties>("wear-m3/confirmation-dialog", record) {
  val text =
    property(
      ConfirmationDialogProperties::text,
      AdapterValueCodecs.String,
      WearConfirmationDialogDefaults.text,
      bindable = false,
    )
  val variant =
    property(
      ConfirmationDialogProperties::variant,
      AdapterValueCodecs.String,
      WearConfirmationDialogDefaults.variant,
      bindable = false,
    )
  val visible =
    property(
      ConfirmationDialogProperties::visible,
      AdapterValueCodecs.Boolean,
      WearConfirmationDialogDefaults.visible,
      bindable = true,
    )
}

data class OpenOnPhoneDialogProperties(val text: String, val visible: Boolean)

class OpenOnPhoneDialogAdapter(record: ComponentRecord) :
  TypedComponentAdapter<OpenOnPhoneDialogProperties>("wear-m3/open-on-phone-dialog", record) {
  val text =
    property(
      OpenOnPhoneDialogProperties::text,
      AdapterValueCodecs.String,
      WearConfirmationDialogDefaults.text,
      bindable = false,
    )
  val visible =
    property(
      OpenOnPhoneDialogProperties::visible,
      AdapterValueCodecs.Boolean,
      WearConfirmationDialogDefaults.visible,
      bindable = true,
    )
}

data class ProgressIndicatorProperties(
  val variant: String,
  val progress: Float,
  val segments: Int,
  val indicatorColor: String,
  val trackColor: String,
  val enabled: Boolean,
)

class ProgressIndicatorAdapter(record: ComponentRecord) :
  TypedComponentAdapter<ProgressIndicatorProperties>("wear-m3/progress-indicator", record) {
  val variant =
    property(
      ProgressIndicatorProperties::variant,
      AdapterValueCodecs.String,
      WearProgressIndicatorDefaults.variant,
      bindable = false,
    )
  val progress =
    property(
      ProgressIndicatorProperties::progress,
      WearValueCodecs.Float,
      WearProgressIndicatorDefaults.progress,
      bindable = true,
    )
  val segments =
    property(
      ProgressIndicatorProperties::segments,
      WearValueCodecs.Int,
      WearProgressIndicatorDefaults.segments,
      bindable = true,
    )
  val indicatorColor =
    property(
      ProgressIndicatorProperties::indicatorColor,
      AdapterValueCodecs.String,
      WearProgressIndicatorDefaults.indicatorColor,
      bindable = false,
    )
  val trackColor =
    property(
      ProgressIndicatorProperties::trackColor,
      AdapterValueCodecs.String,
      WearProgressIndicatorDefaults.trackColor,
      bindable = false,
    )
  val enabled =
    property(
      ProgressIndicatorProperties::enabled,
      AdapterValueCodecs.Boolean,
      WearProgressIndicatorDefaults.enabled,
      bindable = false,
    )
}

data class VariableFontTextProperties(
  val text: String,
  val font: String,
  val wght: Float,
  val wdth: Float,
  val slnt: Float,
  val rond: Float,
  val fontSizeSp: Float,
  val color: String,
)

class VariableFontTextAdapter(record: ComponentRecord) :
  TypedComponentAdapter<VariableFontTextProperties>("wear-m3/variable-font-text", record) {
  val text =
    property(
      VariableFontTextProperties::text,
      AdapterValueCodecs.String,
      WearVariableFontTextDefaults.text,
      bindable = false,
    )
  val font =
    property(
      VariableFontTextProperties::font,
      AdapterValueCodecs.String,
      WearVariableFontTextDefaults.font,
      bindable = false,
    )
  val wght =
    property(
      VariableFontTextProperties::wght,
      WearValueCodecs.Float,
      WearVariableFontTextDefaults.wght,
      bindable = true,
    )
  val wdth =
    property(
      VariableFontTextProperties::wdth,
      WearValueCodecs.Float,
      WearVariableFontTextDefaults.wdth,
      bindable = true,
    )
  val slnt =
    property(
      VariableFontTextProperties::slnt,
      WearValueCodecs.Float,
      WearVariableFontTextDefaults.slnt,
      bindable = true,
    )
  val rond =
    property(
      VariableFontTextProperties::rond,
      WearValueCodecs.Float,
      WearVariableFontTextDefaults.rond,
      bindable = true,
    )
  val fontSizeSp =
    property(
      VariableFontTextProperties::fontSizeSp,
      WearValueCodecs.Float,
      WearVariableFontTextDefaults.fontSizeSp,
      bindable = false,
    )
  val color =
    property(
      VariableFontTextProperties::color,
      AdapterValueCodecs.String,
      WearVariableFontTextDefaults.color,
      bindable = false,
    )
}
