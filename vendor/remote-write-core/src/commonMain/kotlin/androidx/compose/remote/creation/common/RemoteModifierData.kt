package androidx.compose.remote.creation.common

/** Typed, resolved modifier operations retained until document serialization. */
public sealed interface RemoteModifierOperation {
  public data class Width(val type: Int, val value: Float) : RemoteModifierOperation
  public data class Height(val type: Int, val value: Float) : RemoteModifierOperation
  public data class Padding(val left: Float, val top: Float, val right: Float, val bottom: Float) :
    RemoteModifierOperation
  public data class Background(
    val flags: Int,
    val colorId: Int,
    val red: Float,
    val green: Float,
    val blue: Float,
    val alpha: Float,
    val shape: Int,
  ) : RemoteModifierOperation
  public data object ClipRect : RemoteModifierOperation
  public data class RoundedClipRect(
    val topStart: Float,
    val topEnd: Float,
    val bottomStart: Float,
    val bottomEnd: Float,
  ) : RemoteModifierOperation
  public data class WidthIn(val min: Float, val max: Float) : RemoteModifierOperation
  public data class HeightIn(val min: Float, val max: Float) : RemoteModifierOperation
  public data class Offset(val x: Float, val y: Float) : RemoteModifierOperation
  public data class ZIndex(val value: Float) : RemoteModifierOperation
  public data object Ripple : RemoteModifierOperation
  public data object DrawContent : RemoteModifierOperation
  public data class MacroCall(val id: Int, val argumentIds: IntArray) : RemoteModifierOperation
  public data class Click(val clickType: Int, val actions: List<RemoteActionData>) :
    RemoteModifierOperation
  public data class Touch(val type: Int, val actions: List<RemoteActionData>) : RemoteModifierOperation
  public data class Semantics(
    val contentDescriptionId: Int,
    val role: Int,
    val textId: Int,
    val stateDescriptionId: Int,
    val mode: Int,
    val enabled: Boolean,
    val clickable: Boolean,
  ) : RemoteModifierOperation
  public data class GraphicsLayer(val attributes: List<RemoteLayerAttribute>) :
    RemoteModifierOperation
  public data class Border(
    val width: Float,
    val roundedCorner: Float,
    val color: Int,
    val dynamicColor: Boolean,
    val shapeType: Int,
  ) : RemoteModifierOperation
  public data class Visibility(val valueId: Int) : RemoteModifierOperation
  public data class CollapsiblePriority(val orientation: Int, val priority: Float) :
    RemoteModifierOperation
  public data class AlignBy(val line: Float, val flags: Int = 0) : RemoteModifierOperation
  public data class Marquee(
    val iterations: Int,
    val animationMode: Int,
    val repeatDelayMillis: Float,
    val initialDelayMillis: Float,
    val spacing: Float,
    val velocity: Float,
  ) : RemoteModifierOperation
  public data class AnimationSpec(
    val animationId: Int,
    val motionDuration: Float,
    val motionEasingType: Int,
    val visibilityDuration: Float,
    val visibilityEasingType: Int,
    val enterAnimation: Int,
    val exitAnimation: Int,
  ) : RemoteModifierOperation
  public data class Scroll(
    val direction: Int,
    val position: Float,
    val maximum: Float,
    val notchMaximum: Float,
    val notches: Int,
  ) : RemoteModifierOperation
}

public sealed interface RemoteLayerAttribute {
  public val id: Int

  public data class FloatValue(override val id: Int, val value: Float) : RemoteLayerAttribute
  public data class IntValue(override val id: Int, val value: Int) : RemoteLayerAttribute
}

/** Typed action payloads nested under click and touch modifier containers. */
public sealed interface RemoteActionData {
  public data class Host(val actionId: Int) : RemoteActionData
  public data class HostMetadata(val actionId: Int, val metadataId: Int) : RemoteActionData
  public data class HostNamed(val nameId: Int, val type: Int, val valueId: Int) : RemoteActionData
  public data class IntegerChange(val targetId: Int, val value: Int) : RemoteActionData
  public data class IntegerExpressionChange(val targetId: Long, val expressionId: Long) :
    RemoteActionData
  public data class FloatChange(val targetId: Int, val value: Float) : RemoteActionData
  public data class FloatExpressionChange(val targetId: Int, val expressionId: Int) : RemoteActionData
  public data class StringChange(val targetId: Int, val valueId: Int) : RemoteActionData
}

/** Resolved modifier chain attached to one layout component. */
public class RemoteModifierData(
  public var componentId: Int = -1,
  public var spacedBy: Float = 0f,
  public val operations: MutableList<RemoteModifierOperation> = mutableListOf(),
) {
  public fun then(operation: RemoteModifierOperation): RemoteModifierData {
    operations += operation
    return this
  }

  public fun writeTo(writer: RemoteWriter) {
    operations.forEach { writer.writeModifier(it) }
  }
}
