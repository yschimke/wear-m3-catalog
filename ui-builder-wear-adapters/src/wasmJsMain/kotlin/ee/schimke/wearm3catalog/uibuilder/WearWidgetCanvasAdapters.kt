package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.uibuilder.WearWidgetScaffoldSize
import ee.schimke.composeai.uibuilder.canvasAdapterRegistry
import ee.schimke.composeai.uibuilder.hostSpec

/**
 * Host chrome for Remote Compose widgets; this frame is supplied by Glance Wear, not Material 3.
 */
val wearWidgetCanvasAdapters = canvasAdapterRegistry {
  register("widget-container") {
    val size =
      WearWidgetScaffoldSize.entries.firstOrNull { it.componentId == node.componentId }
        ?: WearWidgetScaffoldSize.Small
    val spec = size.hostSpec(LocalWearWidgetHostShape.current)
    // Padding and radius are the host's (WearWidgetParams), never the design's.
    val horizontalPadding = spec.horizontalPaddingDp
    val verticalPadding = spec.verticalPaddingDp
    val cornerRadius = spec.cornerRadiusDp
    val background =
      string("background").takeIf(String::isNotEmpty)?.let {
        resolveWearColor(it, WidgetDefaultBackground)
      } ?: WidgetDefaultBackground
    WearWidgetContainerFrame(
      modifier = modifier,
      contentWidthDp = (spec.frameWidthDp - 2f * horizontalPadding).coerceAtLeast(0f),
      contentHeightDp = (spec.frameHeightDp - 2f * verticalPadding).coerceAtLeast(0f),
      horizontalPaddingDp = horizontalPadding,
      verticalPaddingDp = verticalPadding,
      cornerRadiusDp = cornerRadius,
      background = background,
      backgroundContent = { Slot("background", Modifier.fillMaxSize().clip(it)) },
    ) {
      Slot("content", Modifier.fillMaxSize())
    }
  }
}

/** `WearWidgetContainer`'s literal default, independent of the editor's selected theme. */
private val WidgetDefaultBackground = Color(red = 39, green = 36, blue = 48)
