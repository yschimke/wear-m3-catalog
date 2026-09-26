package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.export.UiBuilderNode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import ee.schimke.composeai.uibuilder.renderer.sdk.UiBuilderModifierPlan
import ee.schimke.composeai.uibuilder.renderer.sdk.alignmentFor
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
import ee.schimke.composeai.uibuilder.renderer.sdk.uiBuilderModifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull

/** Foundation donor adapters compiled into this catalog runtime, never interpreted by the host. */
@OptIn(ExperimentalLayoutApi::class)
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
  // Remote Compose's own layouts. The flow row is foundation's `FlowRow`, which lays out exactly
  // as `RemoteFlowRow` does; the collapsibles and the fit box have no foundation counterpart, so
  // they are drawn by the two layouts in RemoteOnlyLayouts.kt, written to the player's rules. Only
  // the fit box is in the Glance Wear widget profile; the others draw here and fail on the native
  // lane, which is where a widget that cannot ship is supposed to fail.
  register("layout/flow-row") {
    val canvas = this
    FlowRow(
      modifier = modifier,
      horizontalArrangement = horizontalArrangement(),
      verticalArrangement = verticalArrangement(),
      maxItemsInEachRow = integer("maxItemsInEachRow").takeIf { it > 0 } ?: Int.MAX_VALUE,
    ) {
      canvas.Items("children") { Content() }
    }
  }
  register("layout/collapsible-column") {
    val canvas = this
    CollapsibleLinearLayout(
      vertical = true,
      horizontalArrangement = Arrangement.Start,
      verticalArrangement = verticalArrangement(),
      horizontalAlignment = horizontalAlignment(),
      verticalAlignment = Alignment.Top,
      modifier = modifier,
    ) {
      canvas.Items("children") { child -> Content(collapsibleChildModifier(child)) }
    }
  }
  register("layout/collapsible-row") {
    val canvas = this
    CollapsibleLinearLayout(
      vertical = false,
      horizontalArrangement = horizontalArrangement(),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
      verticalAlignment = verticalAlignment(),
      modifier = modifier,
    ) {
      canvas.Items("children") { child -> Content(collapsibleChildModifier(child)) }
    }
  }
  register("layout/fit-box") {
    val canvas = this
    FitBoxLayout(
      alignment =
        BiasAlignment(
          horizontalBias =
            when (string("horizontalAlignment")) {
              "start" -> -1f
              "end" -> 1f
              else -> 0f
            },
          verticalBias =
            when (string("verticalArrangement")) {
              "top" -> -1f
              "bottom" -> 1f
              else -> 0f
            },
        ),
      modifier = modifier,
    ) {
      canvas.Items("children") { Content() }
    }
  }
}

/**
 * A collapsible child's `collapsiblePriority` and `weight`, handed to the layout as parent data.
 */
private fun collapsibleChildModifier(node: UiBuilderNode): Modifier {
  val chain = node.modifiers.mapNotNull { it as? JsonObject }
  fun number(type: String, field: String): Float? =
    chain
      .firstOrNull { (it["type"] as? JsonPrimitive)?.contentOrNull == type }
      ?.let { (it[field] as? JsonPrimitive)?.floatOrNull ?: if (type == "weight") 1f else 0f }
  return Modifier.collapsibleChild(
    priority = number("collapsiblePriority", "priority"),
    weight = number("weight", "weight"),
  )
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
  var result: Modifier =
    node.propertyString("alignment")?.let { Modifier.align(alignmentFor(it)) } ?: Modifier
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

private fun UiBuilderNode.propertyString(name: String): String? =
  ((properties[name] as? JsonObject)?.get("value") as? JsonPrimitive)?.contentOrNull
