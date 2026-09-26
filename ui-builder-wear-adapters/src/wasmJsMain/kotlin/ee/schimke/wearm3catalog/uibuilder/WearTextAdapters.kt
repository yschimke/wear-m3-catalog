package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
import ee.schimke.composeai.uibuilder.renderer.sdk.googleMaterialIconImageVector

val wearTextAdapters = canvasAdapterRegistry {
  register("wear-m3/text") {
    Text(
      text = string("text"),
      modifier = modifier,
      color = color("color", Color.Unspecified),
      style = textStyle(string("style")),
      fontWeight = fontWeight(),
      fontStyle = fontStyle(),
      fontSize = float("fontSizeSp").takeIf { it > 0f }?.sp ?: TextUnit.Unspecified,
      lineHeight = float("lineHeightSp").takeIf { it > 0f }?.sp ?: TextUnit.Unspecified,
      letterSpacing =
        float("letterSpacingSp").takeIf { "letterSpacingSp" in node.properties }?.sp
          ?: TextUnit.Unspecified,
      textDecoration = textDecoration(),
      textAlign = textAlign(),
      minLines = integer("minLines", 1).coerceAtLeast(1),
      maxLines = integer("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
      softWrap = boolean("softWrap", true),
      overflow = textOverflow(),
      onTextLayout = ::recordTextLayout,
    )
  }
  register("wear-m3/icon") {
    val vector = googleMaterialIconImageVector(string("iconKey"))
    if (vector == null) {
      Text("?", modifier = modifier.size(float("sizeDp", 24f).dp))
    } else {
      Icon(
        imageVector = vector,
        contentDescription = string("contentDescription").ifEmpty { null },
        modifier = modifier.size(float("sizeDp", 24f).dp),
        tint = color("color", MaterialTheme.colorScheme.onSurface),
      )
    }
  }
}

@Composable
fun resolveWearColor(value: String, fallback: Color = Color.Unspecified): Color =
  when {
    value.startsWith("#") -> Color(parseArgb(value))
    value == "background" -> MaterialTheme.colorScheme.background
    value == "surface" -> MaterialTheme.colorScheme.surfaceContainer
    value == "surfaceContainer" -> MaterialTheme.colorScheme.surfaceContainer
    value == "surfaceContainerLow" -> MaterialTheme.colorScheme.surfaceContainerLow
    value == "surfaceContainerHigh" -> MaterialTheme.colorScheme.surfaceContainerHigh
    value == "surfaceContainerHighest" -> MaterialTheme.colorScheme.surfaceContainer
    value == "primary" -> MaterialTheme.colorScheme.primary
    value == "onPrimary" -> MaterialTheme.colorScheme.onPrimary
    value == "tertiary" -> MaterialTheme.colorScheme.tertiary
    value == "onTertiary" -> MaterialTheme.colorScheme.onTertiary
    value == "onSurface" -> MaterialTheme.colorScheme.onSurface
    value == "onSurfaceVariant" -> MaterialTheme.colorScheme.onSurfaceVariant
    value == "outlineVariant" -> MaterialTheme.colorScheme.outlineVariant
    value == "transparent" -> Color.Transparent
    else -> fallback
  }

@Composable
private fun textStyle(value: String): TextStyle =
  when (value) {
    "displayLarge" -> MaterialTheme.typography.displayLarge
    "displayMedium" -> MaterialTheme.typography.displayMedium
    "displaySmall" -> MaterialTheme.typography.displaySmall
    "headlineLarge",
    "titleLarge" -> MaterialTheme.typography.titleLarge
    "headlineMedium",
    "titleMedium" -> MaterialTheme.typography.titleMedium
    "headlineSmall",
    "titleSmall" -> MaterialTheme.typography.titleSmall
    "bodyLarge" -> MaterialTheme.typography.bodyLarge
    "bodyMedium" -> MaterialTheme.typography.bodyMedium
    "bodySmall" -> MaterialTheme.typography.bodySmall
    "labelLarge" -> MaterialTheme.typography.labelLarge
    "labelMedium" -> MaterialTheme.typography.labelMedium
    "labelSmall" -> MaterialTheme.typography.labelSmall
    else -> LocalTextStyle.current
  }

@Composable
private fun CanvasNodeScope.color(name: String, fallback: Color): Color =
  resolveWearColor(string(name), fallback)

private fun CanvasNodeScope.fontWeight(): FontWeight? =
  when (string("fontWeight")) {
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

private fun CanvasNodeScope.fontStyle(): FontStyle? =
  when (string("fontStyle")) {
    "italic" -> FontStyle.Italic
    "normal" -> FontStyle.Normal
    else -> null
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

private fun CanvasNodeScope.textDecoration(): TextDecoration? =
  when (string("textDecoration")) {
    "underline" -> TextDecoration.Underline
    "lineThrough" -> TextDecoration.LineThrough
    else -> null
  }

/**
 * Parses a UI Builder colour as an sRGB ARGB value.
 *
 * Keep the return type as [Long]. `Color(ULong)` is the packed wide-gamut value-class constructor,
 * where the low six bits are a colour-space id. Feeding an ordinary `#RRGGBB` value to that
 * constructor can therefore crash the Wasm renderer while reading `Color.colorSpace`.
 */
private fun parseArgb(value: String): Long {
  val hex = value.removePrefix("#")
  return when (hex.length) {
    6 -> ("FF$hex").toLong(16)
    8 -> hex.toLong(16)
    else -> 0L
  }
}
