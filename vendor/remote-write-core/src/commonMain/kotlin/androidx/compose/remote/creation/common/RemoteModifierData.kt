package androidx.compose.remote.creation.common

/** A resolved modifier command that writes only protocol operations. */
public fun interface RemoteModifierOperation {
  public fun write(writer: RemoteWriter)
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
    operations.forEach { it.write(writer) }
  }
}
