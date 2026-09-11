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

package com.google.android.horologist.remotecompose.lottie.format.graphicelement.geometry

import com.google.android.horologist.remotecompose.lottie.format.graphicelement.ShapeType
import com.google.android.horologist.remotecompose.lottie.format.properties.BasePositionProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticPositionProperty
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

/** A polystar (star or regular polygon) parametric shape. */
@Serializable
internal data class PolyStar(
  @SerialName("nm") override val name: String? = "",
  @SerialName("hd") override val hidden: Boolean? = false,
  @SerialName("ty") override val type: ShapeType = ShapeType.PolyStar,
  @SerialName("ix") override val index: Int? = null,
  @SerialName("mn") override val matchName: String? = null,
  @SerialName("cix") override val propertyIndex: Int? = null,
  @SerialName("d") override val direction: Int? = null,
  @SerialName("sy") val starType: PolyStarType = PolyStarType.Star,
  @SerialName("pt") val points: BaseScalarProperty = StaticScalarProperty(value = 5f),
  @SerialName("p")
  val position: BasePositionProperty = StaticPositionProperty(value = listOf(0f, 0f)),
  @SerialName("r") val rotation: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("or") val outerRadius: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("os") val outerRoundedness: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("ir") val innerRadius: BaseScalarProperty? = null,
  @SerialName("is") val innerRoundedness: BaseScalarProperty? = null,
) : GeometryShape

@Serializable(with = PolyStarTypeSerializer::class)
internal enum class PolyStarType(val value: Int) {
  Star(1),
  Polygon(2);

  companion object {
    fun fromValueOrNull(value: Int): PolyStarType? = values().firstOrNull { it.value == value }
  }
}

internal object PolyStarTypeSerializer : KSerializer<PolyStarType> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("PolyStarType", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): PolyStarType {
    return try {
      val jsonDecoder = decoder as? JsonDecoder
      if (jsonDecoder != null) {
        val element = jsonDecoder.decodeJsonElement()
        val intVal =
          element.jsonPrimitive.intOrNull ?: element.jsonPrimitive.floatOrNull?.toInt() ?: 1
        PolyStarType.fromValueOrNull(intVal) ?: PolyStarType.Star
      } else {
        val value = decoder.decodeInt()
        PolyStarType.fromValueOrNull(value) ?: PolyStarType.Star
      }
    } catch (e: Exception) {
      PolyStarType.Star
    }
  }

  override fun serialize(encoder: Encoder, value: PolyStarType) {
    encoder.encodeInt(value.value)
  }
}
