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
  public data class Offset(val x: Float, val y: Float) : RemoteModifierOperation
  public data class ZIndex(val value: Float) : RemoteModifierOperation
  public data object Ripple : RemoteModifierOperation
  public data object DrawContent : RemoteModifierOperation
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
