package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

/**
 * The canvas's drawing of `RemoteFitBox`: its children are alternatives, and it shows the FIRST one
 * that fits the space it is given without shrinking. When none fits it draws nothing and takes no
 * room, which is what the player does (`FitBoxLayout` in remote-core).
 */
@Composable
internal fun FitBoxLayout(
  alignment: Alignment,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Layout(content, modifier) { measurables, constraints ->
    // Each child measured once, unconstrained, to learn what it WANTS: a child that would have to
    // be squeezed does not fit. The first that fits is placed as measured.
    val placeable =
      measurables
        .asSequence()
        .map { it.measure(Constraints()) }
        .firstOrNull { it.width <= constraints.maxWidth && it.height <= constraints.maxHeight }
        ?: return@Layout layout(constraints.minWidth, constraints.minHeight) {}
    val width = placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
    val height = placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(width, height) {
      placeable.place(
        alignment.align(
          androidx.compose.ui.unit.IntSize(placeable.width, placeable.height),
          androidx.compose.ui.unit.IntSize(width, height),
          layoutDirection,
        )
      )
    }
  }
}
