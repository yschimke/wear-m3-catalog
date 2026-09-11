/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.horologist.remotecompose.lottie.format.graphicelement.modifiers

import com.google.android.horologist.remotecompose.lottie.format.graphicelement.ShapeType
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticScalarProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/** ZigZag modifier */
@Serializable
internal data class ZigZag(
  @SerialName("nm") override val name: String? = "",
  @SerialName("hd") override val hidden: Boolean? = false,
  @SerialName("ty") override val type: ShapeType = ShapeType.ZigZag,
  @SerialName("ix") override val index: Int? = null,
  @SerialName("mn") override val matchName: String? = null,
  @SerialName("cix") override val propertyIndex: Int? = null,
  @SerialName("s") val size: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("r") val ridgesPerSegment: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("pt") val pointType: ZigZagType = ZigZagType.Corner,
) : ShapeModifier

@Serializable(with = ZigZagTypeSerializer::class)
internal enum class ZigZagType(val value: Int) {
  Corner(1),
  Smooth(2);

  companion object {
    fun fromValueOrNull(value: Int): ZigZagType? = values().firstOrNull { it.value == value }
  }
}

internal object ZigZagTypeSerializer : KSerializer<ZigZagType> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("ZigZagType", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): ZigZagType {
    return try {
      val jsonDecoder = decoder as? JsonDecoder
      if (jsonDecoder != null) {
        val element = jsonDecoder.decodeJsonElement()
        val intVal =
          element.jsonPrimitive.intOrNull ?: element.jsonPrimitive.floatOrNull?.toInt() ?: 1
        ZigZagType.fromValueOrNull(intVal) ?: ZigZagType.Corner
      } else {
        val value = decoder.decodeInt()
        ZigZagType.fromValueOrNull(value) ?: ZigZagType.Corner
      }
    } catch (e: Exception) {
      ZigZagType.Corner
    }
  }

  override fun serialize(encoder: Encoder, value: ZigZagType) {
    encoder.encodeInt(value.value)
  }
}
