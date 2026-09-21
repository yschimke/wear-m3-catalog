package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.WearWidgetHostShape
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
    val spec = size.hostSpec(WearWidgetHostShape.Default)
    val horizontalPadding = float("horizontalPaddingDp", spec.horizontalPaddingDp)
    val verticalPadding = float("verticalPaddingDp", spec.verticalPaddingDp)
    val cornerRadius = float("cornerRadiusDp", spec.cornerRadiusDp)
    val shape = RoundedCornerShape(cornerRadius.dp)
    val background =
      string("background").takeIf(String::isNotEmpty)?.let {
        resolveWearColor(it, WidgetDefaultBackground)
      } ?: WidgetDefaultBackground
    Box(
      modifier =
        modifier
          .size(
            (spec.contentWidthDp + 2f * horizontalPadding).dp,
            (spec.contentHeightDp + 2f * verticalPadding).dp,
          )
          .background(background, shape)
    ) {
      Slot("background", Modifier.fillMaxSize().clip(shape))
      Box(Modifier.padding(horizontalPadding.dp, verticalPadding.dp)) {
        Slot("content", Modifier.fillMaxSize())
      }
    }
  }
}

/** `WearWidgetContainer`'s literal default, independent of the editor's selected theme. */
private val WidgetDefaultBackground = Color(red = 39, green = 36, blue = 48)
