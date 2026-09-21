/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.remote.core

/** Write-only Remote Compose wire codecs, independent of operation decoding and playback. */
public object WriteOperations {
  private const val DATA_FLOAT = 80
  private const val DATA_TEXT = 102
  private const val DATA_INT = 140
  private const val DATA_LONG = 148
  private const val MODIFIER_SCROLL = 226
  private const val MACRO_FOR_EACH = 244
  private const val INCLUDE_REFERENCED_OPERATIONS = 245
  private const val MACRO_CALL = 247
  private const val MACRO_ARGUMENT = 248
  private const val MACRO_BLOCK = 249

  @JvmStatic
  public fun textData(buffer: WireBuffer, id: Int, value: String) {
    buffer.start(DATA_TEXT)
    buffer.writeInt(id)
    buffer.writeUTF8(value)
  }

  @JvmStatic
  public fun integerConstant(buffer: WireBuffer, id: Int, value: Int) {
    buffer.start(DATA_INT)
    buffer.writeInt(id)
    buffer.writeInt(value)
  }

  @JvmStatic
  public fun floatConstant(buffer: WireBuffer, id: Int, value: Float) {
    buffer.start(DATA_FLOAT)
    buffer.writeInt(id)
    buffer.writeFloat(value)
  }

  @JvmStatic
  public fun longConstant(buffer: WireBuffer, id: Int, value: Long) {
    buffer.start(DATA_LONG)
    buffer.writeInt(id)
    buffer.writeLong(value)
  }

  @JvmStatic
  public fun includeReferencedOperations(buffer: WireBuffer, id: Int) {
    buffer.start(INCLUDE_REFERENCED_OPERATIONS)
    buffer.writeInt(id)
  }

  @JvmStatic
  public fun patternInflation(buffer: WireBuffer, id: Int, argumentIds: IntArray) {
    buffer.start(MACRO_CALL)
    buffer.writeInt(id)
    buffer.writeInt(argumentIds.size)
    argumentIds.forEach(buffer::writeInt)
  }

  @JvmStatic
  public fun patternBlock(buffer: WireBuffer, parameterIndex: Int) {
    buffer.start(MACRO_BLOCK)
    buffer.writeInt(parameterIndex)
  }

  @JvmStatic
  public fun patternArgument(buffer: WireBuffer, parameterIndex: Int) {
    buffer.start(MACRO_ARGUMENT)
    buffer.writeInt(parameterIndex)
  }

  @JvmStatic
  public fun patternForEach(buffer: WireBuffer, collectionId: Int, localItemId: Int) {
    buffer.start(MACRO_FOR_EACH)
    buffer.writeInt(collectionId)
    buffer.writeInt(localItemId)
  }

  @JvmStatic
  public fun scrollModifier(
    buffer: WireBuffer,
    direction: Int,
    position: Float,
    maximum: Float,
    notchMaximum: Float,
  ) {
    buffer.start(MODIFIER_SCROLL)
    buffer.writeInt(direction)
    buffer.writeFloat(position)
    buffer.writeFloat(maximum)
    buffer.writeFloat(notchMaximum)
  }
}
