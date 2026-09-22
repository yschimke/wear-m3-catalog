package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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

/** Shared Glance Wear host frame used by both the editable stand-in and the played RC document. */
@Composable
fun WearWidgetContainerFrame(
  contentWidthDp: Float,
  contentHeightDp: Float,
  horizontalPaddingDp: Float,
  verticalPaddingDp: Float,
  cornerRadiusDp: Float,
  background: Color,
  modifier: Modifier = Modifier,
  backgroundContent: @Composable (RoundedCornerShape) -> Unit = {},
  content: @Composable () -> Unit,
) {
  val shape = RoundedCornerShape(cornerRadiusDp.dp)
  Box(
    modifier =
      modifier
        .size(
          (contentWidthDp + 2f * horizontalPaddingDp).dp,
          (contentHeightDp + 2f * verticalPaddingDp).dp,
        )
        .background(background, shape)
  ) {
    backgroundContent(shape)
    Box(Modifier.padding(horizontalPaddingDp.dp, verticalPaddingDp.dp)) { content() }
  }
}

/** `WearWidgetContainer`'s literal default, independent of the editor's selected theme. */
private val WidgetDefaultBackground = Color(red = 39, green = 36, blue = 48)
