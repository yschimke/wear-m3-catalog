package ee.schimke.wearm3catalog.typed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.schimke.composeai.discovery.AdapterSlot
import ee.schimke.composeai.uibuilder.renderer.sdk.TypedCanvasNodeScope
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
import ee.schimke.composeai.uibuilder.renderer.sdk.register

/**
 * The typed registrations: each reads its definition's property handles, passes compiled callbacks
 * and draws its slots through the SDK. Saved documents reach these through
 * [typedWearCanvasAdapters], which first normalizes the document's established wrapper spellings.
 */
fun rawTypedWearCanvasAdapters(catalog: TypedWearCatalog) = canvasAdapterRegistry {
  register(catalog.text) {
    val adapter = catalog.text
    WearText(
      text = value(adapter.text),
      style = value(adapter.style),
      fontWeight = value(adapter.fontWeight),
      fontStyle = value(adapter.fontStyle),
      color = value(adapter.color),
      fontSizeSp = value(adapter.fontSizeSp),
      lineHeightSp = value(adapter.lineHeightSp),
      letterSpacingSp = value(adapter.letterSpacingSp),
      fontVariationSettings = value(adapter.fontVariationSettings),
      fontFeatureSettings = value(adapter.fontFeatureSettings),
      minLines = value(adapter.minLines),
      maxLines = value(adapter.maxLines),
      softWrap = value(adapter.softWrap),
      overflow = value(adapter.overflow),
      textAlign = value(adapter.textAlign),
      textDecoration = value(adapter.textDecoration),
      modifier = modifier,
      onTextLayout = canvas::recordTextLayout,
    )
  }

  register(catalog.icon) {
    val adapter = catalog.icon
    WearIcon(
      iconKey = value(adapter.iconKey),
      sizeDp = value(adapter.sizeDp),
      contentDescription = value(adapter.contentDescription),
      color = value(adapter.color),
      modifier = modifier,
    )
  }

  register(catalog.listHeader) {
    val adapter = catalog.listHeader
    WearListHeader(
      text = value(adapter.text),
      maxLines = value(adapter.maxLines),
      overflow = value(adapter.overflow),
      modifier = modifier,
    )
  }

  register(catalog.listSubHeader) {
    val adapter = catalog.listSubHeader
    WearListSubHeader(
      text = value(adapter.text),
      maxLines = value(adapter.maxLines),
      overflow = value(adapter.overflow),
      modifier = modifier,
    )
  }

  register(catalog.card) {
    val adapter = catalog.card
    WearCard(
      variant = value(adapter.variant),
      containerColor = value(adapter.containerColor),
      contentColor = value(adapter.contentColor),
      onClick = callback(adapter.onClick),
      content = { Slot(adapter.content) },
      modifier = modifier,
    )
  }

  register(catalog.edgeButton) {
    val adapter = catalog.edgeButton
    WearEdgeButton(
      size = value(adapter.size),
      enabled = value(adapter.enabled),
      containerColor = value(adapter.containerColor),
      contentColor = value(adapter.contentColor),
      onClick = callback(adapter.onClick),
      content = { Slot(adapter.content) },
      modifier = modifier,
    )
  }

  register(catalog.button) {
    val adapter = catalog.button
    WearButton(
      variant = value(adapter.variant),
      enabled = value(adapter.enabled),
      containerColor = value(adapter.containerColor),
      contentColor = value(adapter.contentColor),
      onClick = callback(adapter.onClick),
      content = { Slot(adapter.content) },
      modifier = modifier,
    )
  }

  register(catalog.iconButton) {
    val adapter = catalog.iconButton
    WearIconButton(
      variant = value(adapter.variant),
      enabled = value(adapter.enabled),
      containerColor = value(adapter.containerColor),
      contentColor = value(adapter.contentColor),
      onClick = callback(adapter.onClick),
      content = { Slot(adapter.content) },
      modifier = modifier,
    )
  }

  register(catalog.textButton) {
    val adapter = catalog.textButton
    WearTextButton(
      variant = value(adapter.variant),
      enabled = value(adapter.enabled),
      onClick = callback(adapter.onClick),
      content = { Slot(adapter.content) },
      modifier = modifier,
    )
  }

  register(catalog.checkboxButton) {
    val adapter = catalog.checkboxButton
    WearCheckboxButton(
      label = value(adapter.label),
      secondaryLabel = value(adapter.secondaryLabel),
      checked = value(adapter.checked),
      enabled = value(adapter.enabled),
      onCheckedChange = callback(adapter.onCheckedChange),
      modifier = modifier,
    )
  }

  register(catalog.switchButton) {
    val adapter = catalog.switchButton
    WearSwitchButton(
      label = value(adapter.label),
      secondaryLabel = value(adapter.secondaryLabel),
      checked = value(adapter.checked),
      enabled = value(adapter.enabled),
      onCheckedChange = callback(adapter.onCheckedChange),
      modifier = modifier,
    )
  }

  register(catalog.radioButton) {
    val adapter = catalog.radioButton
    WearRadioButton(
      label = value(adapter.label),
      secondaryLabel = value(adapter.secondaryLabel),
      selected = value(adapter.selected),
      enabled = value(adapter.enabled),
      onSelectedChange = callback(adapter.onSelectedChange),
      modifier = modifier,
    )
  }

  register(catalog.slider) {
    val adapter = catalog.slider
    WearSlider(
      value = value(adapter.value),
      valueFrom = value(adapter.valueFrom),
      valueTo = value(adapter.valueTo),
      steps = value(adapter.steps),
      segmented = value(adapter.segmented),
      enabled = value(adapter.enabled),
      onValueChange = callback(adapter.onValueChange),
      modifier = modifier,
    )
  }

  register(catalog.stepper) {
    val adapter = catalog.stepper
    WearStepper(
      value = value(adapter.value),
      valueFrom = value(adapter.valueFrom),
      valueTo = value(adapter.valueTo),
      steps = value(adapter.steps),
      enabled = value(adapter.enabled),
      onValueChange = callback(adapter.onValueChange),
      content = { Slot(adapter.content) },
      modifier = modifier,
    )
  }

  register(catalog.buttonGroup) {
    val adapter = catalog.buttonGroup
    WearButtonGroup(
      // Each child as its own item, as production draws the group, so per-child parent data holds.
      children = { canvas.Items(adapter.children.name) { Content(Modifier) } },
      modifier = modifier,
    )
  }

  register(catalog.datePicker) {
    val adapter = catalog.datePicker
    WearDatePicker(
      initialDate = value(adapter.initialDate),
      type = value(adapter.type),
      modifier = modifier,
    )
  }

  register(catalog.timePicker) {
    val adapter = catalog.timePicker
    WearTimePicker(
      initialTime = value(adapter.initialTime),
      type = value(adapter.type),
      modifier = modifier,
    )
  }

  register(catalog.alertDialog) {
    val adapter = catalog.alertDialog
    WearAlertDialog(
      title = value(adapter.title),
      text = value(adapter.text),
      visible = value(adapter.visible),
      content = { Slot(adapter.content) },
      confirmButton = optionalSlot(adapter.confirmButton),
      dismissButton = optionalSlot(adapter.dismissButton),
      modifier = modifier,
    )
  }

  register(catalog.confirmationDialog) {
    val adapter = catalog.confirmationDialog
    WearConfirmationDialog(
      text = value(adapter.text),
      variant = value(adapter.variant),
      visible = value(adapter.visible),
      modifier = modifier,
    )
  }

  register(catalog.openOnPhoneDialog) {
    val adapter = catalog.openOnPhoneDialog
    WearOpenOnPhoneDialog(
      text = value(adapter.text),
      visible = value(adapter.visible),
      modifier = modifier,
    )
  }

  register(catalog.progressIndicator) {
    val adapter = catalog.progressIndicator
    WearProgressIndicator(
      variant = value(adapter.variant),
      progress = value(adapter.progress),
      segments = value(adapter.segments),
      indicatorColor = value(adapter.indicatorColor),
      trackColor = value(adapter.trackColor),
      enabled = value(adapter.enabled),
      modifier = modifier,
    )
  }

  register(catalog.variableFontText) {
    val adapter = catalog.variableFontText
    WearVariableFontText(
      text = value(adapter.text),
      font = value(adapter.font),
      wght = value(adapter.wght),
      wdth = value(adapter.wdth),
      slnt = value(adapter.slnt),
      rond = value(adapter.rond),
      fontSizeSp = value(adapter.fontSizeSp),
      color = value(adapter.color),
      modifier = modifier,
      onTextLayout = canvas::recordTextLayout,
    )
  }
}

/** A slot the overload treats as absent when the document puts nothing in it. */
private fun <P> TypedCanvasNodeScope<P>.optionalSlot(
  slot: AdapterSlot<P>
): (@Composable () -> Unit)? =
  if (canvas.itemCount(slot.name) == 0) null
  else {
    { Slot(slot) }
  }
