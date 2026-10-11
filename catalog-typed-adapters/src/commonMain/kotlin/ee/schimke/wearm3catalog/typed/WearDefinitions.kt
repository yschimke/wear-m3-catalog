package ee.schimke.wearm3catalog.typed

/** A typed pilot entry point: its published id, compiled callable, shelf group and role. */
data class WearDefinition(
  val id: String,
  val callable: String,
  val group: String,
  val builtin: Boolean,
)

val wearDefinitions =
  listOf(
    WearDefinition("wear-m3/text", "WearText", "Text", false),
    WearDefinition("wear-m3/icon", "WearIcon", "Iconography", false),
    WearDefinition("wear-m3/list-header", "WearListHeader", "Lists and screens", false),
    WearDefinition("wear-m3/list-sub-header", "WearListSubHeader", "Lists and screens", false),
    WearDefinition("wear-m3/card", "WearCard", "Cards", false),
    WearDefinition("wear-m3/edge-button", "WearEdgeButton", "Edge-hugging buttons", false),
    WearDefinition("wear-m3/button", "WearButton", "Buttons", false),
    WearDefinition("wear-m3/icon-button", "WearIconButton", "Icon buttons", false),
    WearDefinition("wear-m3/text-button", "WearTextButton", "Buttons", false),
    WearDefinition("wear-m3/checkbox-button", "WearCheckboxButton", "Toggle buttons", false),
    WearDefinition("wear-m3/switch-button", "WearSwitchButton", "Toggle buttons", false),
    WearDefinition("wear-m3/radio-button", "WearRadioButton", "Selection buttons", false),
    WearDefinition("wear-m3/slider", "WearSlider", "Sliders and steppers", false),
    WearDefinition("wear-m3/stepper", "WearStepper", "Sliders and steppers", false),
    WearDefinition("wear-m3/button-group", "WearButtonGroup", "Buttons", false),
    WearDefinition("wear-m3/date-picker", "WearDatePicker", "Pickers", false),
    WearDefinition("wear-m3/time-picker", "WearTimePicker", "Pickers", false),
    WearDefinition("wear-m3/alert-dialog", "WearAlertDialog", "Dialogs", true),
    WearDefinition("wear-m3/confirmation-dialog", "WearConfirmationDialog", "Dialogs", true),
    WearDefinition("wear-m3/open-on-phone-dialog", "WearOpenOnPhoneDialog", "Dialogs", true),
    WearDefinition(
      "wear-m3/progress-indicator",
      "WearProgressIndicator",
      "Progress indicators",
      true,
    ),
    WearDefinition("wear-m3/variable-font-text", "WearVariableFontText", "Text", true),
  )
