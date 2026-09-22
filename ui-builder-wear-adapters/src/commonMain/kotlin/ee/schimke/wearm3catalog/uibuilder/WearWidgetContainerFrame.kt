package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
