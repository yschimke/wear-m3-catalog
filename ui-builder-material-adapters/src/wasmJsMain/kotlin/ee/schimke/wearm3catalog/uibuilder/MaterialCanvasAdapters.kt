package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.MaterialTheme
import ee.schimke.composeai.uibuilder.CanvasNodeScope
import ee.schimke.composeai.uibuilder.canvasAdapterRegistry
import ee.schimke.composeai.uibuilder.googleMaterialIconImageVector

/** Mobile Material donors used by persisted Remote and mobile designs. */
val materialCanvasAdapters = canvasAdapterRegistry {
  materialText("material3/text")
  materialText("m3/text")
  materialIcon("material3/icon")
  materialIcon("m3/icon")
}

private fun ee.schimke.composeai.uibuilder.CanvasAdapterRegistry.Builder.materialText(id: String) {
  register(id) {
    Text(
      text = string("text"),
      modifier = modifier,
      color = materialColor("color", Color.Unspecified),
      style = materialTextStyle(string("style")),
      fontSize = float("fontSizeSp").takeIf { it > 0f }?.sp ?: TextUnit.Unspecified,
      textAlign = textAlign(),
      maxLines = integer("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
      softWrap = boolean("softWrap", true),
      overflow = textOverflow(),
      onTextLayout = ::recordTextLayout,
    )
  }
}

private fun ee.schimke.composeai.uibuilder.CanvasAdapterRegistry.Builder.materialIcon(id: String) {
  register(id) {
    val vector = googleMaterialIconImageVector(string("iconKey"))
    if (vector == null) {
      Text("?", modifier = modifier.size(float("sizeDp", 24f).dp))
    } else {
      Icon(
        imageVector = vector,
        contentDescription = string("contentDescription").ifEmpty { null },
        modifier = modifier.size(float("sizeDp", 24f).dp),
        tint = materialColor("color", MaterialTheme.colorScheme.onSurface),
      )
    }
  }
}

@Composable
private fun CanvasNodeScope.materialColor(name: String, fallback: Color): Color =
  when (val value = string(name)) {
    "background" -> MaterialTheme.colorScheme.background
    "surface" -> MaterialTheme.colorScheme.surfaceContainer
    "surfaceContainer" -> MaterialTheme.colorScheme.surfaceContainer
    "surfaceContainerLow" -> MaterialTheme.colorScheme.surfaceContainerLow
    "surfaceContainerHigh" -> MaterialTheme.colorScheme.surfaceContainerHigh
    "surfaceContainerHighest" -> MaterialTheme.colorScheme.surfaceContainer
    "primary" -> MaterialTheme.colorScheme.primary
    "onPrimary" -> MaterialTheme.colorScheme.onPrimary
    "tertiary" -> MaterialTheme.colorScheme.tertiary
    "onTertiary" -> MaterialTheme.colorScheme.onTertiary
    "onSurface" -> MaterialTheme.colorScheme.onSurface
    "onSurfaceVariant" -> MaterialTheme.colorScheme.onSurfaceVariant
    "outlineVariant" -> MaterialTheme.colorScheme.outlineVariant
    "transparent" -> Color.Transparent
    else -> if (value.startsWith("#")) Color(parseMaterialArgb(value)) else fallback
  }

@Composable
private fun materialTextStyle(value: String): TextStyle =
  when (value) {
    "displayLarge" -> MaterialTheme.typography.displayLarge
    "displayMedium" -> MaterialTheme.typography.displayMedium
    "displaySmall" -> MaterialTheme.typography.displaySmall
    "headlineLarge" -> MaterialTheme.typography.titleLarge
    "headlineMedium" -> MaterialTheme.typography.titleMedium
    "headlineSmall" -> MaterialTheme.typography.titleSmall
    "titleLarge" -> MaterialTheme.typography.titleLarge
    "titleMedium" -> MaterialTheme.typography.titleMedium
    "titleSmall" -> MaterialTheme.typography.titleSmall
    "bodyLarge" -> MaterialTheme.typography.bodyLarge
    "bodyMedium" -> MaterialTheme.typography.bodyMedium
    "bodySmall" -> MaterialTheme.typography.bodySmall
    "labelLarge" -> MaterialTheme.typography.labelLarge
    "labelMedium" -> MaterialTheme.typography.labelMedium
    "labelSmall" -> MaterialTheme.typography.labelSmall
    else -> LocalTextStyle.current
  }

private fun CanvasNodeScope.textOverflow(): TextOverflow =
  when (string("overflow")) {
    "ellipsis" -> TextOverflow.Ellipsis
    "visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
  }

private fun CanvasNodeScope.textAlign(): TextAlign? =
  when (string("textAlign")) {
    "left" -> TextAlign.Left
    "right" -> TextAlign.Right
    "center" -> TextAlign.Center
    "justify" -> TextAlign.Justify
    "start" -> TextAlign.Start
    "end" -> TextAlign.End
    else -> null
  }

private fun parseMaterialArgb(value: String): ULong {
  val hex = value.removePrefix("#")
  return when (hex.length) {
    6 -> ("FF$hex").toULong(16)
    8 -> hex.toULong(16)
    else -> 0u
  }
}
