package ee.schimke.wearm3catalog.typed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.CurvedScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.ArcProgressIndicator
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.ChildButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ConfirmationDialogContent
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.DatePickerType
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FailureConfirmationDialogContent
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.LinearProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SegmentedCircularProgressIndicator
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.SliderDefaults
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.SuccessConfirmationDialogContent
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import androidx.wear.compose.material3.TimePickerType
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.confirmationDialogCurvedText
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import ee.schimke.composeai.uibuilder.LocalUiBuilderFontFamilies
import ee.schimke.composeai.uibuilder.LocalUiBuilderFontRegistry
import ee.schimke.composeai.uibuilder.LocalUiBuilderFontVariants
import ee.schimke.composeai.uibuilder.export.FontSettings
import ee.schimke.composeai.uibuilder.export.VariableFontText
import ee.schimke.composeai.uibuilder.renderer.sdk.googleMaterialIcon
import ee.schimke.composeai.uibuilder.renderer.sdk.rememberGoogleMaterialIconVector
import ee.schimke.wearm3catalog.uibuilder.OutsideRow
import ee.schimke.wearm3catalog.uibuilder.resolveWearColor
import ee.schimke.wearm3catalog.uibuilder.rowTransformation
import ee.schimke.wearm3catalog.uibuilder.wearTextStyle
import ee.schimke.wearm3catalog.uibuilder.withFontSettings

// The typed pilot's compiled entry points. Each is an ordinary top-level composable whose wire
// parameters are plain Kotlin values, so `TypedAdapterDiscovery` reads its signature from bytecode
// and its typed adapter can be checked against that signature. Every body invokes the real Wear
// Compose Material 3 component the production adapter draws, with the same defaults and the same
// row-transformation rule, so a typed registration and the production one can be compared pixel
// for pixel (`TypedWearRendererTest`).

object WearTextDefaults {
  const val text: String = ""
  const val style: String = ""
  const val fontWeight: String = ""
  const val fontStyle: String = ""
  const val color: String = ""
  const val fontSizeSp: Float = 0f
  const val lineHeightSp: Float = 0f
  const val letterSpacingSp: Float = 0f
  const val fontVariationSettings: String = ""
  const val fontFeatureSettings: String = ""
  const val minLines: Int = 1
  const val maxLines: Int = Int.MAX_VALUE
  const val softWrap: Boolean = true
  const val overflow: String = ""
  const val textAlign: String = ""
  const val textDecoration: String = ""
}

/**
 * Wear `Text`. A zero size, line height or letter spacing keeps the style's own: the wire format
 * has no unset number, so an authored letter spacing of exactly 0 is indistinguishable from none.
 */
@Composable
fun WearText(
  text: String = WearTextDefaults.text,
  style: String = WearTextDefaults.style,
  fontWeight: String = WearTextDefaults.fontWeight,
  fontStyle: String = WearTextDefaults.fontStyle,
  color: String = WearTextDefaults.color,
  fontSizeSp: Float = WearTextDefaults.fontSizeSp,
  lineHeightSp: Float = WearTextDefaults.lineHeightSp,
  letterSpacingSp: Float = WearTextDefaults.letterSpacingSp,
  fontVariationSettings: String = WearTextDefaults.fontVariationSettings,
  fontFeatureSettings: String = WearTextDefaults.fontFeatureSettings,
  minLines: Int = WearTextDefaults.minLines,
  maxLines: Int = WearTextDefaults.maxLines,
  softWrap: Boolean = WearTextDefaults.softWrap,
  overflow: String = WearTextDefaults.overflow,
  textAlign: String = WearTextDefaults.textAlign,
  textDecoration: String = WearTextDefaults.textDecoration,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
) {
  val font = withFontSettings(wearTextStyle(style), fontFeatureSettings, fontVariationSettings)
  Text(
    text = text,
    modifier = modifier,
    color = resolveWearColor(color, Color.Unspecified),
    style = font.style,
    fontWeight = if (font.setsWeight) null else wearFontWeight(fontWeight),
    fontStyle = wearFontStyle(fontStyle),
    fontSize = fontSizeSp.takeIf { it > 0f }?.sp ?: TextUnit.Unspecified,
    lineHeight = lineHeightSp.takeIf { it > 0f }?.sp ?: TextUnit.Unspecified,
    letterSpacing = letterSpacingSp.takeIf { it != 0f }?.sp ?: TextUnit.Unspecified,
    textDecoration = wearTextDecoration(textDecoration),
    textAlign = wearTextAlign(textAlign),
    minLines = minLines.coerceAtLeast(1),
    maxLines = maxLines.coerceAtLeast(1),
    softWrap = softWrap,
    overflow = wearTextOverflow(overflow),
    onTextLayout = onTextLayout,
  )
}

object WearIconDefaults {
  const val iconKey: String = ""
  const val sizeDp: Float = 24f
  const val contentDescription: String = ""
  const val color: String = ""
}

/** Wear `Icon`, drawing a Material Symbols vector the runtime resolves by key. */
@Composable
fun WearIcon(
  iconKey: String = WearIconDefaults.iconKey,
  sizeDp: Float = WearIconDefaults.sizeDp,
  contentDescription: String = WearIconDefaults.contentDescription,
  color: String = WearIconDefaults.color,
  modifier: Modifier = Modifier,
) {
  val vector = rememberGoogleMaterialIconVector(iconKey)
  if (vector == null) {
    if (googleMaterialIcon(iconKey) == null) Text("?", modifier = modifier.size(sizeDp.dp))
    else Box(modifier.size(sizeDp.dp))
  } else {
    Icon(
      imageVector = vector,
      contentDescription = contentDescription.ifEmpty { null },
      modifier = modifier.size(sizeDp.dp),
      tint = resolveWearColor(color, LocalContentColor.current),
    )
  }
}

object WearListHeaderDefaults {
  const val text: String = ""
  const val maxLines: Int = Int.MAX_VALUE
  const val overflow: String = ""
}

@Composable
fun WearListHeader(
  text: String = WearListHeaderDefaults.text,
  maxLines: Int = WearListHeaderDefaults.maxLines,
  overflow: String = WearListHeaderDefaults.overflow,
  modifier: Modifier = Modifier,
) {
  val transformation = rowTransformation()
  OutsideRow {
    ListHeader(modifier = modifier, transformation = transformation) {
      Text(text, maxLines = maxLines.coerceAtLeast(1), overflow = wearTextOverflow(overflow))
    }
  }
}

@Composable
fun WearListSubHeader(
  text: String = WearListHeaderDefaults.text,
  maxLines: Int = WearListHeaderDefaults.maxLines,
  overflow: String = WearListHeaderDefaults.overflow,
  modifier: Modifier = Modifier,
) {
  val transformation = rowTransformation()
  OutsideRow {
    ListSubHeader(modifier = modifier, transformation = transformation) {
      Text(text, maxLines = maxLines.coerceAtLeast(1), overflow = wearTextOverflow(overflow))
    }
  }
}

object WearCardDefaults {
  const val variant: String = ""
  const val containerColor: String = ""
  const val contentColor: String = ""
}

/** Wear `Card`, `TitleCard`, `AppCard` or `OutlinedCard`, picked by [variant]. */
@Composable
fun WearCard(
  variant: String = WearCardDefaults.variant,
  containerColor: String = WearCardDefaults.containerColor,
  contentColor: String = WearCardDefaults.contentColor,
  onClick: () -> Unit = {},
  content: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val transformation = rowTransformation()
  OutsideRow {
    WearCardVariant(
      variant,
      containerColor,
      contentColor,
      onClick,
      content,
      modifier,
      transformation,
    )
  }
}

@Composable
private fun WearCardVariant(
  variant: String,
  containerColor: String,
  contentColor: String,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
  modifier: Modifier,
  transformation: SurfaceTransformation?,
) {
  // `OutlinedCard` draws no container and `outlinedCardColors` takes none. The slot is the title of
  // `TitleCard`/`AppCard`, and `Card` provides `titleColor` to its content, so the content colour
  // is
  // the title colour too.
  val container = resolveWearColor(containerColor)
  val contentTint = resolveWearColor(contentColor)
  val colors =
    CardDefaults.cardColors(
      containerColor = container,
      contentColor = contentTint,
      titleColor = contentTint,
    )
  val outlinedColors = CardDefaults.outlinedCardColors(contentColor = contentTint)
  when (variant) {
    "title" ->
      TitleCard(
        onClick = onClick,
        title = { content() },
        modifier = modifier,
        colors = colors,
        transformation = transformation,
      ) {}
    "app" ->
      AppCard(
        onClick = onClick,
        appName = {},
        title = { content() },
        modifier = modifier,
        colors = colors,
        transformation = transformation,
      ) {}
    "outlined" ->
      OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        colors = outlinedColors,
        transformation = transformation,
      ) {
        content()
      }
    else ->
      Card(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        transformation = transformation,
      ) {
        content()
      }
  }
}

object WearButtonDefaults {
  const val variant: String = ""
  const val enabled: Boolean = true
  const val containerColor: String = ""
  const val contentColor: String = ""
}

/** Wear `Button`, `FilledTonalButton`, `OutlinedButton` or `ChildButton`, picked by [variant]. */
@Composable
fun WearButton(
  variant: String = WearButtonDefaults.variant,
  enabled: Boolean = WearButtonDefaults.enabled,
  containerColor: String = WearButtonDefaults.containerColor,
  contentColor: String = WearButtonDefaults.contentColor,
  onClick: () -> Unit = {},
  content: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val label: @Composable RowScope.() -> Unit = { content() }
  val transformation = rowTransformation()
  OutsideRow {
    val container = resolveWearColor(containerColor)
    val tint = resolveWearColor(contentColor)
    when (variant) {
      "filled-tonal" ->
        FilledTonalButton(
          onClick = onClick,
          modifier = modifier,
          enabled = enabled,
          colors =
            ButtonDefaults.filledTonalButtonColors(
              containerColor = container,
              contentColor = tint,
              iconColor = tint,
            ),
          transformation = transformation,
          label = label,
        )
      "outlined" ->
        OutlinedButton(
          onClick = onClick,
          modifier = modifier,
          enabled = enabled,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = tint, iconColor = tint),
          transformation = transformation,
          label = label,
        )
      "child" ->
        ChildButton(
          onClick = onClick,
          modifier = modifier,
          enabled = enabled,
          colors = ButtonDefaults.childButtonColors(contentColor = tint, iconColor = tint),
          transformation = transformation,
          label = label,
        )
      else ->
        Button(
          onClick = onClick,
          modifier = modifier,
          enabled = enabled,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = container,
              contentColor = tint,
              iconColor = tint,
            ),
          transformation = transformation,
          label = label,
        )
    }
  }
}

object WearEdgeButtonDefaults {
  const val size: String = ""
  const val enabled: Boolean = true
  const val containerColor: String = ""
  const val contentColor: String = ""
}

/** Wear `EdgeButton`, always the filled shape, so `ButtonDefaults.buttonColors`. */
@Composable
fun WearEdgeButton(
  size: String = WearEdgeButtonDefaults.size,
  enabled: Boolean = WearEdgeButtonDefaults.enabled,
  containerColor: String = WearEdgeButtonDefaults.containerColor,
  contentColor: String = WearEdgeButtonDefaults.contentColor,
  onClick: () -> Unit = {},
  content: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val tint = resolveWearColor(contentColor)
  EdgeButton(
    onClick = onClick,
    buttonSize =
      when (size) {
        "extra-small" -> EdgeButtonSize.ExtraSmall
        "medium" -> EdgeButtonSize.Medium
        "large" -> EdgeButtonSize.Large
        else -> EdgeButtonSize.Small
      },
    modifier = modifier,
    enabled = enabled,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = resolveWearColor(containerColor),
        contentColor = tint,
        iconColor = tint,
      ),
  ) {
    content()
  }
}

object WearIconButtonDefaults {
  const val variant: String = ""
  const val enabled: Boolean = true
  const val containerColor: String = ""
  const val contentColor: String = ""
}

/** Wear's icon buttons, picked by [variant]. */
@Composable
fun WearIconButton(
  variant: String = WearIconButtonDefaults.variant,
  enabled: Boolean = WearIconButtonDefaults.enabled,
  containerColor: String = WearIconButtonDefaults.containerColor,
  contentColor: String = WearIconButtonDefaults.contentColor,
  onClick: () -> Unit = {},
  content: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val container = resolveWearColor(containerColor)
  val tint = resolveWearColor(contentColor)
  when (variant) {
    "filled" ->
      FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(container, tint),
      ) {
        content()
      }
    "filled-tonal" ->
      FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.filledTonalIconButtonColors(container, tint),
      ) {
        content()
      }
    "filled-variant" ->
      FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.filledVariantIconButtonColors(container, tint),
      ) {
        content()
      }
    "outlined" ->
      OutlinedIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = tint),
      ) {
        content()
      }
    else ->
      IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(container, tint),
      ) {
        content()
      }
  }
}

object WearTextButtonDefaults {
  const val variant: String = ""
  const val enabled: Boolean = true
}

@Composable
fun WearTextButton(
  variant: String = WearTextButtonDefaults.variant,
  enabled: Boolean = WearTextButtonDefaults.enabled,
  onClick: () -> Unit = {},
  content: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  TextButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors =
      when (variant) {
        "filled" -> TextButtonDefaults.filledTextButtonColors()
        "filled-tonal" -> TextButtonDefaults.filledTonalTextButtonColors()
        "filled-variant" -> TextButtonDefaults.filledVariantTextButtonColors()
        "outlined" -> TextButtonDefaults.outlinedTextButtonColors()
        else -> TextButtonDefaults.textButtonColors()
      },
  ) {
    content()
  }
}

object WearSelectionButtonDefaults {
  const val label: String = ""
  const val secondaryLabel: String = ""
  const val checked: Boolean = false
  const val enabled: Boolean = true
}

@Composable
fun WearCheckboxButton(
  label: String = WearSelectionButtonDefaults.label,
  secondaryLabel: String = WearSelectionButtonDefaults.secondaryLabel,
  checked: Boolean = WearSelectionButtonDefaults.checked,
  enabled: Boolean = WearSelectionButtonDefaults.enabled,
  onCheckedChange: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val transformation = rowTransformation()
  OutsideRow {
    CheckboxButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = modifier.fillMaxWidth(),
      enabled = enabled,
      label = { Text(label) },
      secondaryLabel = optionalLabel(secondaryLabel),
      transformation = transformation,
    )
  }
}

@Composable
fun WearSwitchButton(
  label: String = WearSelectionButtonDefaults.label,
  secondaryLabel: String = WearSelectionButtonDefaults.secondaryLabel,
  checked: Boolean = WearSelectionButtonDefaults.checked,
  enabled: Boolean = WearSelectionButtonDefaults.enabled,
  onCheckedChange: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val transformation = rowTransformation()
  OutsideRow {
    SwitchButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = modifier.fillMaxWidth(),
      enabled = enabled,
      label = { Text(label) },
      secondaryLabel = optionalLabel(secondaryLabel),
      transformation = transformation,
    )
  }
}

/** A radio row selects itself, so its change is always to selected. */
@Composable
fun WearRadioButton(
  label: String = WearSelectionButtonDefaults.label,
  secondaryLabel: String = WearSelectionButtonDefaults.secondaryLabel,
  selected: Boolean = WearSelectionButtonDefaults.checked,
  enabled: Boolean = WearSelectionButtonDefaults.enabled,
  onSelectedChange: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val transformation = rowTransformation()
  OutsideRow {
    RadioButton(
      selected = selected,
      onSelect = { onSelectedChange(true) },
      modifier = modifier.fillMaxWidth(),
      enabled = enabled,
      label = { Text(label) },
      secondaryLabel = optionalLabel(secondaryLabel),
      transformation = transformation,
    )
  }
}

object WearRangeDefaults {
  const val value: Float = 0f
  const val valueFrom: Float = 0f
  const val valueTo: Float = 1f
  const val steps: Int = 0
  const val segmented: String = ""
  const val enabled: Boolean = true
}

@Composable
fun WearSlider(
  value: Float = WearRangeDefaults.value,
  valueFrom: Float = WearRangeDefaults.valueFrom,
  valueTo: Float = WearRangeDefaults.valueTo,
  steps: Int = WearRangeDefaults.steps,
  segmented: String = WearRangeDefaults.segmented,
  enabled: Boolean = WearRangeDefaults.enabled,
  onValueChange: (Float) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  Slider(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
    steps = steps.coerceAtLeast(0),
    valueRange = guardedRange(valueFrom, valueTo),
    segmented = segmented == "segmented",
  )
}

@Composable
fun WearStepper(
  value: Float = WearRangeDefaults.value,
  valueFrom: Float = WearRangeDefaults.valueFrom,
  valueTo: Float = WearRangeDefaults.valueTo,
  steps: Int = WearRangeDefaults.steps,
  enabled: Boolean = WearRangeDefaults.enabled,
  onValueChange: (Float) -> Unit = {},
  content: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  Stepper(
    value = value,
    onValueChange = onValueChange,
    steps = steps.coerceAtLeast(0),
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
    valueRange = guardedRange(valueFrom, valueTo),
    decreaseIcon = { SliderDefaults.DecreaseIcon() },
    increaseIcon = { SliderDefaults.IncreaseIcon() },
    content = { content() },
  )
}

/**
 * Wear `ButtonGroup`. The group takes the row's transformation as a whole and its buttons none,
 * since a transformation on both is applied twice, each button about its own centre.
 */
@Composable
fun WearButtonGroup(children: @Composable () -> Unit = {}, modifier: Modifier = Modifier) {
  val transformation = rowTransformation()
  OutsideRow {
    ButtonGroup(modifier = modifier.fillMaxWidth(), transformation = transformation) { children() }
  }
}

object WearPickerDefaults {
  const val initialDate: String = ""
  const val initialTime: String = ""
  const val type: String = ""
}

/** Wear `DatePicker`; an unparsable date starts at 2026-01-01. */
@Composable
fun WearDatePicker(
  initialDate: String = WearPickerDefaults.initialDate,
  type: String = WearPickerDefaults.type,
  modifier: Modifier = Modifier,
) {
  PlatformDatePicker(initialDate, type, modifier)
}

/** Wear `TimePicker`; an unparsable time starts at 10:10. */
@Composable
fun WearTimePicker(
  initialTime: String = WearPickerDefaults.initialTime,
  type: String = WearPickerDefaults.type,
  modifier: Modifier = Modifier,
) {
  PlatformTimePicker(initialTime, type, modifier)
}

// The Wear port types picker dates with java.time on the JVM and kotlinx-datetime in the browser.
@Composable internal expect fun PlatformDatePicker(date: String, type: String, modifier: Modifier)

@Composable internal expect fun PlatformTimePicker(time: String, type: String, modifier: Modifier)

object WearAlertDialogDefaults {
  const val title: String = ""
  const val text: String = ""
  const val visible: Boolean = true
}

/**
 * Wear `AlertDialogContent`. An authored confirm or dismiss slot selects the two-button overload,
 * which draws each slot it is given; with neither, the edge-button-less overload.
 */
@Composable
fun WearAlertDialog(
  title: String = WearAlertDialogDefaults.title,
  text: String = WearAlertDialogDefaults.text,
  visible: Boolean = WearAlertDialogDefaults.visible,
  content: @Composable () -> Unit = {},
  confirmButton: (@Composable () -> Unit)? = null,
  dismissButton: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  if (!visible) return
  val titleContent: @Composable () -> Unit = { Text(title) }
  val textContent: (@Composable () -> Unit)? =
    text.takeIf { it.isNotEmpty() }?.let { value -> { Text(value) } }
  val items: ScalingLazyListScope.() -> Unit = { item { content() } }
  if (confirmButton != null || dismissButton != null) {
    AlertDialogContent(
      confirmButton = { confirmButton?.invoke() },
      dismissButton = { dismissButton?.invoke() },
      modifier = modifier,
      title = titleContent,
      text = textContent,
      content = items,
    )
  } else {
    AlertDialogContent(
      modifier = modifier,
      title = titleContent,
      text = textContent,
      content = items,
    )
  }
}

object WearConfirmationDialogDefaults {
  const val text: String = ""
  const val variant: String = ""
  const val visible: Boolean = true
}

@Composable
fun WearConfirmationDialog(
  text: String = WearConfirmationDialogDefaults.text,
  variant: String = WearConfirmationDialogDefaults.variant,
  visible: Boolean = WearConfirmationDialogDefaults.visible,
  modifier: Modifier = Modifier,
) {
  if (!visible) return
  val style = ConfirmationDialogDefaults.curvedTextStyle
  val curvedText: (CurvedScope.() -> Unit)? =
    text.takeIf { it.isNotEmpty() }?.let { value -> { confirmationDialogCurvedText(value, style) } }
  when (variant) {
    "success" -> SuccessConfirmationDialogContent(modifier = modifier, curvedText = curvedText)
    "failure" -> FailureConfirmationDialogContent(modifier = modifier, curvedText = curvedText)
    else -> ConfirmationDialogContent(modifier = modifier, curvedText = curvedText) {}
  }
}

@Composable
fun WearOpenOnPhoneDialog(
  text: String = WearConfirmationDialogDefaults.text,
  visible: Boolean = WearConfirmationDialogDefaults.visible,
  modifier: Modifier = Modifier,
) {
  if (!visible) return
  val style = OpenOnPhoneDialogDefaults.curvedTextStyle
  val curvedText: (CurvedScope.() -> Unit)? =
    text.takeIf { it.isNotEmpty() }?.let { value -> { openOnPhoneDialogCurvedText(value, style) } }
  OpenOnPhoneDialogContent(
    curvedText = curvedText,
    durationMillis = OpenOnPhoneDialogDefaults.DurationMillis,
    modifier = modifier,
  ) {
    OpenOnPhoneDialogDefaults.Icon()
  }
}

object WearProgressIndicatorDefaults {
  const val variant: String = ""
  const val progress: Float = 0f
  const val segments: Int = 1
  const val indicatorColor: String = ""
  const val trackColor: String = ""
  const val enabled: Boolean = true
}

/** Wear's progress indicators, picked by [variant]; unset colours keep Wear's own. */
@Composable
fun WearProgressIndicator(
  variant: String = WearProgressIndicatorDefaults.variant,
  progress: Float = WearProgressIndicatorDefaults.progress,
  segments: Int = WearProgressIndicatorDefaults.segments,
  indicatorColor: String = WearProgressIndicatorDefaults.indicatorColor,
  trackColor: String = WearProgressIndicatorDefaults.trackColor,
  enabled: Boolean = WearProgressIndicatorDefaults.enabled,
  modifier: Modifier = Modifier,
) {
  val value = progress.coerceIn(0f, 1f)
  val colors =
    ProgressIndicatorDefaults.colors(
      indicatorColor = resolveWearColor(indicatorColor),
      trackColor = resolveWearColor(trackColor),
    )
  when (variant) {
    "linear" ->
      LinearProgressIndicator(
        progress = { value },
        modifier = modifier.fillMaxWidth(),
        colors = colors,
        enabled = enabled,
      )
    "segmented-circular" ->
      SegmentedCircularProgressIndicator(
        segmentCount = segments.coerceAtLeast(1),
        progress = { value },
        modifier = modifier,
        colors = colors,
        enabled = enabled,
      )
    "arc" -> ArcProgressIndicator(modifier = modifier, colors = colors)
    else ->
      CircularProgressIndicator(
        progress = { value },
        modifier = modifier,
        colors = colors,
        enabled = enabled,
      )
  }
}

object WearVariableFontTextDefaults {
  const val text: String = ""
  const val font: String = ""
  const val wght: Float = 400f
  const val wdth: Float = 100f
  const val slnt: Float = 0f
  const val rond: Float = 0f
  const val fontSizeSp: Float = 32f
  const val color: String = ""
}

/**
 * The canvas stand-in for flexpress's `VariableFontText`: Wear `Text` in the vendored variable
 * font, instanced at the same axes, loaded through the runtime's font registry.
 */
@Composable
fun WearVariableFontText(
  text: String = WearVariableFontTextDefaults.text,
  font: String = WearVariableFontTextDefaults.font,
  wght: Float = WearVariableFontTextDefaults.wght,
  wdth: Float = WearVariableFontTextDefaults.wdth,
  slnt: Float = WearVariableFontTextDefaults.slnt,
  rond: Float = WearVariableFontTextDefaults.rond,
  fontSizeSp: Float = WearVariableFontTextDefaults.fontSizeSp,
  color: String = WearVariableFontTextDefaults.color,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
) {
  val face = VariableFontText.Font.fromWire(font) ?: VariableFontText.Font.RobotoFlex
  val axes =
    face.axes.map { axis ->
      FontSettings.Axis(
        axis.tag,
        when (axis.property) {
          "wght" -> wght
          "wdth" -> wdth
          "slnt" -> slnt
          "rond" -> rond
          else -> axis.default
        },
      )
    }
  val registry = LocalUiBuilderFontRegistry.current
  LaunchedEffect(registry, face) { registry?.request(face.family) }
  val loaded = LocalUiBuilderFontFamilies.current[face.family]
  val family =
    if (loaded == null) null
    else LocalUiBuilderFontVariants.current?.variant(face.family, axes) ?: loaded
  Text(
    text = text,
    modifier = modifier,
    color = resolveWearColor(color, Color.Unspecified),
    style =
      TextStyle(
        fontFamily = family,
        fontWeight = if (family == null) FontWeight(wght.toInt().coerceIn(1, 1000)) else null,
        fontSize = fontSizeSp.coerceAtLeast(1f).sp,
      ),
    onTextLayout = onTextLayout,
  )
}

private fun optionalLabel(value: String): (@Composable RowScope.() -> Unit)? =
  value.takeIf { it.isNotEmpty() }?.let { text -> { Text(text) } }

private fun guardedRange(start: Float, end: Float): ClosedFloatingPointRange<Float> =
  if (end > start) start..end else 0f..1f

internal fun wearTextOverflow(value: String): TextOverflow =
  when (value) {
    "ellipsis" -> TextOverflow.Ellipsis
    "visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
  }

private fun wearFontWeight(value: String): FontWeight? =
  when (value) {
    "thin" -> FontWeight.Thin
    "extraLight" -> FontWeight.ExtraLight
    "light" -> FontWeight.Light
    "medium" -> FontWeight.Medium
    "semiBold" -> FontWeight.SemiBold
    "bold" -> FontWeight.Bold
    "extraBold" -> FontWeight.ExtraBold
    "black" -> FontWeight.Black
    "normal" -> FontWeight.Normal
    else -> null
  }

private fun wearFontStyle(value: String): FontStyle? =
  when (value) {
    "italic" -> FontStyle.Italic
    "normal" -> FontStyle.Normal
    else -> null
  }

private fun wearTextAlign(value: String): TextAlign? =
  when (value) {
    "left" -> TextAlign.Left
    "right" -> TextAlign.Right
    "center" -> TextAlign.Center
    "justify" -> TextAlign.Justify
    "start" -> TextAlign.Start
    "end" -> TextAlign.End
    else -> null
  }

private fun wearTextDecoration(value: String): TextDecoration? =
  when (value) {
    "underline" -> TextDecoration.Underline
    "lineThrough" -> TextDecoration.LineThrough
    else -> null
  }

internal fun datePickerType(value: String): DatePickerType =
  when (value) {
    "day-month-year" -> DatePickerType.DayMonthYear
    "month-day-year" -> DatePickerType.MonthDayYear
    else -> DatePickerType.YearMonthDay
  }

internal fun timePickerType(value: String): TimePickerType =
  when (value) {
    "hours-minutes-am-pm" -> TimePickerType.HoursMinutesAmPm12H
    "hours-minutes-seconds" -> TimePickerType.HoursMinutesSeconds24H
    else -> TimePickerType.HoursMinutes24H
  }
