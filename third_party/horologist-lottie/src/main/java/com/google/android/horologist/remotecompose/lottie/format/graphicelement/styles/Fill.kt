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

import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.ui.graphics.Color
import com.google.android.horologist.remotecompose.lottie.format.graphicelement.ShapeType
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseColorProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.BaseScalarProperty
import com.google.android.horologist.remotecompose.lottie.format.properties.StaticColorProperty
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

/** Solid fill color */
@Serializable
internal data class Fill(
  @SerialName("nm") override val name: String? = "",
  @SerialName("hd") override val hidden: Boolean? = false,
  @SerialName("ty") override val type: ShapeType = ShapeType.Fill,
  @SerialName("ix") override val index: Int? = null,
  @SerialName("mn") override val matchName: String? = null,
  @SerialName("cix") override val propertyIndex: Int? = null,
  @SerialName("o") override val opacity: BaseScalarProperty = StaticScalarProperty(value = 100f),
  @SerialName("c") val color: BaseColorProperty = StaticColorProperty(value = Color.Black.rc),
  @SerialName("r") val fillRule: FillRule = FillRule.NonZero,
) : ShapeStyle

@Serializable(with = FillRuleSerializer::class)
internal enum class FillRule(val value: Int) {
  NonZero(1),
  EvenOdd(2);

  companion object {
    fun fromValueOrNull(value: Int): FillRule? = values().firstOrNull { it.value == value }
  }
}

internal object FillRuleSerializer : KSerializer<FillRule> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("FillRule", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): FillRule {
    return try {
      val jsonDecoder = decoder as? JsonDecoder
      if (jsonDecoder != null) {
        val element = jsonDecoder.decodeJsonElement()
        val intVal =
          element.jsonPrimitive.intOrNull ?: element.jsonPrimitive.floatOrNull?.toInt() ?: 1
        FillRule.fromValueOrNull(intVal) ?: FillRule.NonZero
      } else {
        val value = decoder.decodeInt()
        FillRule.fromValueOrNull(value) ?: FillRule.NonZero
      }
    } catch (e: Exception) {
      FillRule.NonZero
    }
  }

  override fun serialize(encoder: Encoder, value: FillRule) {
    encoder.encodeInt(value.value)
  }
}
