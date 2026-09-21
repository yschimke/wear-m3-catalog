package androidx.compose.remote.creation.common

/** Fully resolved image layout payload retained until serialization. */
public data class RemoteImageData(
  val modifier: RemoteModifierData,
  val bitmapId: Int,
  val scaleType: Int,
  val alpha: Float,
)
