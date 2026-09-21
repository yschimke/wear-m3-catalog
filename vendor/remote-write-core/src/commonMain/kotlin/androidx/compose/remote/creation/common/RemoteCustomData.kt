package androidx.compose.remote.creation.common

/** Resolved property passed to a platform custom component. */
public data class RemoteCustomPropertyData(
  val id: Short,
  val dataType: Short,
  val intValue: Int = 0,
  val floatValue: Float = 0f,
) {
  public companion object {
    public const val IntProperty: Short = 0
    public const val FloatProperty: Short = 1
    public const val StringProperty: Short = 2
    public const val FloatReturn: Short = 3
    public const val TextReturn: Short = 4
    public const val IntReturn: Short = 5
    public const val ColorReturn: Short = 6
    public const val ColorIdProperty: Short = 7
    public const val ColorProperty: Short = 8
    public const val IntIdProperty: Short = 9

    public fun int(id: Short, dataType: Short, value: Int): RemoteCustomPropertyData =
      RemoteCustomPropertyData(id, dataType, intValue = value)

    public fun float(id: Short, dataType: Short, value: Float): RemoteCustomPropertyData =
      RemoteCustomPropertyData(id, dataType, floatValue = value)
  }
}
