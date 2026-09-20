package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.uibuilder.canvasAdapterRegistry

/** First catalog-owned adapters, invoking the real Wear Compose Multiplatform components. */
internal val wearCanvasAdapters = canvasAdapterRegistry {
  register("wear-m3/list-header") {
    ListHeader(modifier = modifier) {
      Text(
        text = string("text"),
        maxLines = integer("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
        overflow = textOverflow(),
      )
    }
  }
  register("wear-m3/list-sub-header") {
    ListSubHeader(modifier = modifier) {
      Text(
        text = string("text"),
        maxLines = integer("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
        overflow = textOverflow(),
      )
    }
  }
}

private fun ee.schimke.composeai.uibuilder.CanvasNodeScope.textOverflow(): TextOverflow =
  when (string("overflow")) {
    "ellipsis" -> TextOverflow.Ellipsis
    "visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
  }
