package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density

/**
 * What a child of a collapsible column or row tells the layout about itself.
 *
 * Carried as parent data because an adapter composes one child at a time (`Items`) and so cannot
 * hand the layout a list up front.
 */
internal data class CollapsibleChildData(val priority: Float?, val weight: Float?)

internal fun Modifier.collapsibleChild(priority: Float?, weight: Float?): Modifier =
  if (priority == null && weight == null) this
  else then(CollapsibleChildModifier(CollapsibleChildData(priority, weight)))

private class CollapsibleChildModifier(val data: CollapsibleChildData) : ParentDataModifier {
  override fun Density.modifyParentData(parentData: Any?): Any = data

  override fun equals(other: Any?): Boolean =
    other is CollapsibleChildModifier && other.data == data

  override fun hashCode(): Int = data.hashCode()
}

/**
 * The canvas's drawing of `RemoteCollapsibleColumn` and `RemoteCollapsibleRow`.
 *
 * Compose foundation has no layout that HIDES a child rather than squeezing it, so this is one,
 * written to the rule the Remote Compose player applies (`CollapsibleColumnLayout` in remote-core):
 * when the children do not fit the main axis, the one with the lowest `collapsiblePriority` goes
 * first, a tie goes to the later child, and a child that states no priority is kept longest. A
 * weighted child shares what is left once the survivors are placed.
 */
@Composable
internal fun CollapsibleLinearLayout(
  vertical: Boolean,
  horizontalArrangement: Arrangement.Horizontal,
  verticalArrangement: Arrangement.Vertical,
  horizontalAlignment: Alignment.Horizontal,
  verticalAlignment: Alignment.Vertical,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Layout(content, modifier) { measurables, constraints ->
    val data = measurables.map { it.parentData as? CollapsibleChildData }
    val mainMax = if (vertical) constraints.maxHeight else constraints.maxWidth
    val crossMax = if (vertical) constraints.maxWidth else constraints.maxHeight
    val spacing =
      if (vertical) verticalArrangement.spacing.roundToPx()
      else horizontalArrangement.spacing.roundToPx()
    val loose =
      if (vertical) Constraints(maxWidth = crossMax) else Constraints(maxHeight = crossMax)
    fun weightOf(index: Int) = data[index]?.weight?.takeIf { it > 0f }
    val eager = arrayOfNulls<Placeable>(measurables.size)
    measurables.forEachIndexed { index, measurable ->
      if (weightOf(index) == null) eager[index] = measurable.measure(loose)
    }
    fun main(placeable: Placeable?) = placeable?.let { if (vertical) it.height else it.width } ?: 0
    val visible = measurables.indices.toMutableList()
    fun used() = visible.sumOf { main(eager[it]) } + spacing * (visible.size - 1).coerceAtLeast(0)
    if (mainMax != Constraints.Infinity) {
      while (visible.isNotEmpty() && used() > mainMax) {
        visible.remove(
          visible.minWith(
            compareBy<Int> { data[it]?.priority ?: Float.POSITIVE_INFINITY }.thenByDescending { it }
          )
        )
      }
    }
    val weighted = visible.filter { weightOf(it) != null }
    val remaining = if (mainMax == Constraints.Infinity) 0 else (mainMax - used()).coerceAtLeast(0)
    val totalWeight = weighted.sumOf { weightOf(it)!!.toDouble() }.toFloat()
    val placed = arrayOfNulls<Placeable>(measurables.size)
    visible.forEach { index ->
      placed[index] =
        weightOf(index)?.let { weight ->
          val share = if (totalWeight > 0f) (remaining * weight / totalWeight).toInt() else 0
          measurables[index].measure(
            if (vertical) Constraints(minHeight = share, maxHeight = share, maxWidth = crossMax)
            else Constraints(minWidth = share, maxWidth = share, maxHeight = crossMax)
          )
        } ?: eager[index]
    }
    val sizes = visible.map { main(placed[it]) }.toIntArray()
    val content = sizes.sum() + spacing * (visible.size - 1).coerceAtLeast(0)
    val mainSize =
      (content + if (weighted.isEmpty()) 0 else remaining).coerceIn(
        if (vertical) constraints.minHeight else constraints.minWidth,
        mainMax,
      )
    val crossSize =
      (visible.maxOfOrNull { placed[it]?.let { p -> if (vertical) p.width else p.height } ?: 0 }
          ?: 0)
        .coerceIn(if (vertical) constraints.minWidth else constraints.minHeight, crossMax)
    val positions = IntArray(sizes.size)
    if (vertical) with(verticalArrangement) { arrange(mainSize, sizes, positions) }
    else with(horizontalArrangement) { arrange(mainSize, sizes, layoutDirection, positions) }
    val width = if (vertical) crossSize else mainSize
    val height = if (vertical) mainSize else crossSize
    layout(width, height) {
      visible.forEachIndexed { slot, index ->
        val placeable = placed[index] ?: return@forEachIndexed
        if (vertical)
          placeable.place(
            horizontalAlignment.align(placeable.width, width, layoutDirection),
            positions[slot],
          )
        else placeable.place(positions[slot], verticalAlignment.align(placeable.height, height))
      }
    }
  }
}

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
