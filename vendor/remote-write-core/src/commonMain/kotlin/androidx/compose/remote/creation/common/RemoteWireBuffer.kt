package androidx.compose.remote.creation.common

/** Growable, write-only Remote Compose protocol buffer. */
public class RemoteWireBuffer(initialCapacity: Int = 256) {
  private var bytes = ByteArray(initialCapacity)
  public var size: Int = 0
    private set

  private fun ensureCapacity(additionalBytes: Int) {
    val required = size + additionalBytes
    if (required > bytes.size) bytes = bytes.copyOf(maxOf(bytes.size * 2, required))
  }

  public fun writeByte(value: Int) {
    ensureCapacity(1)
    bytes[size++] = value.toByte()
  }

  public fun writeShort(value: Int) {
    ensureCapacity(2)
    bytes[size++] = (value ushr 8).toByte()
    bytes[size++] = value.toByte()
  }

  public fun writeInt(value: Int) {
    ensureCapacity(4)
    bytes[size++] = (value ushr 24).toByte()
    bytes[size++] = (value ushr 16).toByte()
    bytes[size++] = (value ushr 8).toByte()
    bytes[size++] = value.toByte()
  }

  public fun writeLong(value: Long) {
    ensureCapacity(8)
    for (shift in 56 downTo 0 step 8) bytes[size++] = (value ushr shift).toByte()
  }

  public fun writeFloat(value: Float): Unit = writeInt(value.toRawBits())

  public fun writeBytes(value: ByteArray) {
    ensureCapacity(value.size)
    value.copyInto(bytes, size)
    size += value.size
  }

  public fun writeUtf8(value: String) {
    val encoded = value.encodeToByteArray()
    writeInt(encoded.size)
    writeBytes(encoded)
  }

  public fun toByteArray(): ByteArray = bytes.copyOf(size)
}
