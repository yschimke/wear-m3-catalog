package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.CanvasNodeScope
import ee.schimke.composeai.uibuilder.UiBuilderModifierPlan
import ee.schimke.composeai.uibuilder.UiBuilderNode
import ee.schimke.composeai.uibuilder.alignmentFor
import ee.schimke.composeai.uibuilder.canvasAdapterRegistry
import ee.schimke.composeai.uibuilder.uiBuilderModifier
import kotlinx.serialization.json.JsonObject

/** Foundation donor adapters compiled into this catalog runtime, never interpreted by the host. */
val foundationCanvasAdapters = canvasAdapterRegistry {
  register("layout/box") {
    val canvas = this
    Box(modifier = modifier, contentAlignment = alignmentFor(string("contentAlignment"))) {
      canvas.Items("children") { child -> Content(boxChildModifier(child)) }
    }
  }
  register("layout/column") {
    val canvas = this
    Column(
      modifier = modifier,
      verticalArrangement = verticalArrangement(),
      horizontalAlignment = horizontalAlignment(),
    ) {
      canvas.Items("children") { child -> Content(columnChildModifier(child)) }
    }
  }
  register("layout/row") {
    val canvas = this
    Row(
      modifier = modifier,
      horizontalArrangement = horizontalArrangement(),
      verticalAlignment = verticalAlignment(),
    ) {
      canvas.Items("children") { child -> Content(rowChildModifier(child)) }
    }
  }
  register("layout/spacer") { Spacer(modifier) }
}

private fun CanvasNodeScope.verticalArrangement(): Arrangement.Vertical {
  val spacing = float("verticalSpacingDp").dp
  return when (string("verticalArrangement")) {
    "center" -> Arrangement.spacedBy(spacing, Alignment.CenterVertically)
    "bottom" -> Arrangement.spacedBy(spacing, Alignment.Bottom)
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.spacedBy(spacing, Alignment.Top)
  }
}

private fun CanvasNodeScope.horizontalArrangement(): Arrangement.Horizontal {
  val spacing = float("horizontalSpacingDp").dp
  return when (string("horizontalArrangement")) {
    "center" -> Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
    "end" -> Arrangement.spacedBy(spacing, Alignment.End)
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.spacedBy(spacing, Alignment.Start)
  }
}

private fun CanvasNodeScope.horizontalAlignment(): Alignment.Horizontal =
  when (string("horizontalAlignment")) {
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
  }

private fun CanvasNodeScope.verticalAlignment(): Alignment.Vertical =
  when (string("verticalAlignment")) {
    "top" -> Alignment.Top
    "bottom" -> Alignment.Bottom
    else -> Alignment.CenterVertically
  }

private fun BoxScope.boxChildModifier(node: UiBuilderNode): Modifier {
  var result: Modifier = Modifier
  node.modifierPlans().forEach { plan ->
    when (plan) {
      UiBuilderModifierPlan.MatchParentSize -> result = result.matchParentSize()
      is UiBuilderModifierPlan.Align -> result = result.align(alignmentFor(plan.alignment))
      else -> Unit
    }
  }
  return result
}

private fun ColumnScope.columnChildModifier(node: UiBuilderNode): Modifier {
  var result: Modifier = Modifier
  node.modifierPlans().forEach { plan ->
    when (plan) {
      is UiBuilderModifierPlan.AlignHorizontal ->
        result =
          result.align(
            when (plan.alignment) {
              "centerHorizontally" -> Alignment.CenterHorizontally
              "end" -> Alignment.End
              else -> Alignment.Start
            }
          )
      is UiBuilderModifierPlan.Weight -> result = result.weight(plan.weight, plan.fill ?: true)
      else -> Unit
    }
  }
  return result
}

private fun RowScope.rowChildModifier(node: UiBuilderNode): Modifier {
  var result: Modifier = Modifier
  node.modifierPlans().forEach { plan ->
    when (plan) {
      is UiBuilderModifierPlan.AlignVertical ->
        result =
          result.align(
            when (plan.alignment) {
              "top" -> Alignment.Top
              "bottom" -> Alignment.Bottom
              else -> Alignment.CenterVertically
            }
          )
      is UiBuilderModifierPlan.Weight -> result = result.weight(plan.weight, plan.fill ?: true)
      else -> Unit
    }
  }
  return result
}

private fun UiBuilderNode.modifierPlans(): List<UiBuilderModifierPlan> = modifiers.mapNotNull {
  (it as? JsonObject)?.let(::uiBuilderModifier)
}
