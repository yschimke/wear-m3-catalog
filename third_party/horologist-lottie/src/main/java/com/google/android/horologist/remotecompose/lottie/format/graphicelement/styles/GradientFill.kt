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

package com.google.android.horologist.remotecompose.lottie.format.graphicelement.styles

import com.google.android.horologist.remotecompose.lottie.format.graphicelement.ShapeType
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseGradientProperty
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

/** Gradient fill */
@Serializable
internal data class GradientFill(
  @SerialName("nm") override val name: String? = "",
  @SerialName("hd") override val hidden: Boolean? = false,
  @SerialName("ty") override val type: ShapeType = ShapeType.GradientFill,
  @SerialName("ix") override val index: Int? = null,
  @SerialName("mn") override val matchName: String? = null,
  @SerialName("cix") override val propertyIndex: Int? = null,
  @SerialName("o") override val opacity: BaseScalarProperty = StaticScalarProperty(value = 100f),
  @SerialName("t") val gradientType: GradientType = GradientType.Linear,
  @SerialName("s")
  val startPoint: BasePositionProperty = StaticPositionProperty(value = listOf(0f, 0f)),
  @SerialName("e")
  val endPoint: BasePositionProperty = StaticPositionProperty(value = listOf(0f, 0f)),
  @SerialName("g") val colors: BaseGradientProperty,
  @SerialName("r") val fillRule: FillRule = FillRule.NonZero,
  @SerialName("h") val highlightLength: BaseScalarProperty? = null,
  @SerialName("a") val highlightAngle: BaseScalarProperty? = null,
) : ShapeStyle

@Serializable(with = GradientTypeSerializer::class)
internal enum class GradientType(val value: Int) {
  Linear(1),
  Radial(2);

  companion object {
    fun fromValueOrNull(value: Int): GradientType? = values().firstOrNull { it.value == value }
  }
}

internal object GradientTypeSerializer : KSerializer<GradientType> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("GradientType", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): GradientType {
    return try {
      val jsonDecoder = decoder as? JsonDecoder
      if (jsonDecoder != null) {
        val element = jsonDecoder.decodeJsonElement()
        val intVal =
          element.jsonPrimitive.intOrNull ?: element.jsonPrimitive.floatOrNull?.toInt() ?: 1
        GradientType.fromValueOrNull(intVal) ?: GradientType.Linear
      } else {
        val value = decoder.decodeInt()
        GradientType.fromValueOrNull(value) ?: GradientType.Linear
      }
    } catch (e: Exception) {
      GradientType.Linear
    }
  }

  override fun serialize(encoder: Encoder, value: GradientType) {
    encoder.encodeInt(value.value)
  }
}
