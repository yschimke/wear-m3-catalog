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

package com.google.android.horologist.remotecompose.lottie.format.properties

import com.google.android.horologist.remotecompose.lottie.format.values.BezierValue
import com.google.android.horologist.remotecompose.lottie.format.values.BezierValueSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Base class for all Lottie Bezier (Shape) properties.
 *
 * Unifies static constant paths ([StaticBezierProperty]) and keyframed dynamic animations
 * ([AnimatedBezierProperty]) under a shared contract for the AST and renderer pipeline.
 */
@Serializable(with = BaseBezierPropertySerializer::class)
internal sealed class BaseBezierProperty {
  abstract val animated: Boolean
  abstract val slotId: String?
}

/** A static bezier property holding a [BezierValue]. */
@Serializable(with = StaticBezierPropertySerializer::class)
internal data class StaticBezierProperty(
  @SerialName("sid") override val slotId: String? = null,
  override val animated: Boolean = false,
  @SerialName("k") val value: BezierValue,
) : BaseBezierProperty()

/** An animated bezier property with keyframes. */
@Serializable(with = AnimatedBezierPropertySerializer::class)
internal data class AnimatedBezierProperty(
  @SerialName("sid") override val slotId: String? = null,
  @SerialName("a") val animatedInt: Int = 1,
  @SerialName("k") val keyframes: List<BezierPropertyKeyframe>,
) : BaseBezierProperty() {
  override val animated: Boolean
    get() = animatedInt == 1
}

/** A single keyframe for an animated bezier property. */
@Serializable(with = BezierPropertyKeyframeSerializer::class)
internal data class BezierPropertyKeyframe(
  @SerialName("t") val frame: Float = 0f,
  @SerialName("h") val hold: Boolean = false,
  @SerialName("i") val inTangent: ScalarKeyframeEasing? = null,
  @SerialName("o") val outTangent: ScalarKeyframeEasing? = null,
  @SerialName("s") val value: List<BezierValue> = emptyList(),
)

/** Polymorphic serializer for [BaseBezierProperty] based on "a" field. */
internal object BaseBezierPropertySerializer :
  JsonContentPolymorphicSerializer<BaseBezierProperty>(BaseBezierProperty::class) {
  override fun selectDeserializer(
    element: JsonElement
  ): DeserializationStrategy<BaseBezierProperty> {
    val animated = element is JsonObject && element["a"]?.jsonPrimitive?.intOrNull == 1
    return if (animated) {
      AnimatedBezierPropertySerializer
    } else {
      StaticBezierPropertySerializer
    }
  }
}

/** Serializer for [StaticBezierProperty] supporting slot IDs and raw bezier values. */
internal object StaticBezierPropertySerializer : KSerializer<StaticBezierProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("StaticBezierProperty") {
      element<String?>("sid", isOptional = true)
      element<Boolean>("animated", isOptional = true)
      element<BezierValue>("k")
    }

  override fun deserialize(decoder: Decoder): StaticBezierProperty {
    val jsonDecoder = decoder as JsonDecoder
    val element = jsonDecoder.decodeJsonElement()
    return when (element) {
      is JsonObject -> {
        val slotId = element["sid"]?.jsonPrimitive?.contentOrNull
        val kElem = element["k"]
        val bezierValue =
          if (kElem != null) {
            jsonDecoder.json.decodeFromJsonElement(BezierValueSerializer, kElem)
          } else {
            jsonDecoder.json.decodeFromJsonElement(BezierValueSerializer, element)
          }
        StaticBezierProperty(slotId = slotId, animated = false, value = bezierValue)
      }
      else -> {
        val bezierValue = jsonDecoder.json.decodeFromJsonElement(BezierValueSerializer, element)
        StaticBezierProperty(slotId = null, animated = false, value = bezierValue)
      }
    }
  }

  override fun serialize(encoder: Encoder, value: StaticBezierProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", 0)
        put("k", jsonEncoder.json.encodeToJsonElement(BezierValueSerializer, value.value))
      }
    )
  }
}

/** Serializer for [AnimatedBezierProperty] supporting slot IDs and keyframes. */
internal object AnimatedBezierPropertySerializer : KSerializer<AnimatedBezierProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("AnimatedBezierProperty") {
      element<String?>("sid", isOptional = true)
      element<Int>("a")
      element<List<BezierPropertyKeyframe>>("k")
    }

  override fun deserialize(decoder: Decoder): AnimatedBezierProperty {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject
    val slotId = obj["sid"]?.jsonPrimitive?.contentOrNull
    val animatedInt = obj["a"]?.jsonPrimitive?.intOrNull ?: 1
    val keyframesArray = obj["k"]?.jsonArray
    val keyframes =
      keyframesArray?.map { element ->
        jsonDecoder.json.decodeFromJsonElement(BezierPropertyKeyframeSerializer, element)
      } ?: emptyList()
    return AnimatedBezierProperty(slotId = slotId, animatedInt = animatedInt, keyframes = keyframes)
  }

  override fun serialize(encoder: Encoder, value: AnimatedBezierProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", value.animatedInt)
        put(
          "k",
          jsonEncoder.json.encodeToJsonElement(
            ListSerializer(BezierPropertyKeyframeSerializer),
            value.keyframes,
          ),
        )
      }
    )
  }
}

/** Serializer for [BezierPropertyKeyframe] handling timing, easing, and flexible shape values. */
internal object BezierPropertyKeyframeSerializer : KSerializer<BezierPropertyKeyframe> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("BezierPropertyKeyframe") {
      element<Float>("t", isOptional = true)
      element<Boolean>("h", isOptional = true)
      element<ScalarKeyframeEasing?>("i", isOptional = true)
      element<ScalarKeyframeEasing?>("o", isOptional = true)
      element<List<BezierValue>>("s", isOptional = true)
    }

  override fun deserialize(decoder: Decoder): BezierPropertyKeyframe {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject

    val frame = obj["t"]?.jsonPrimitive?.floatOrNull ?: 0f
    val hold =
      when (val hElem = obj["h"]) {
        is JsonPrimitive -> hElem.booleanOrNull ?: ((hElem.intOrNull ?: 0) == 1)
        else -> false
      }
    val inTangent =
      obj["i"]?.let { jsonDecoder.json.decodeFromJsonElement(ScalarKeyframeEasingSerializer, it) }
    val outTangent =
      obj["o"]?.let { jsonDecoder.json.decodeFromJsonElement(ScalarKeyframeEasingSerializer, it) }
    val sElem = obj["s"]
    val value =
      when (sElem) {
        is JsonArray ->
          sElem.map { jsonDecoder.json.decodeFromJsonElement(BezierValueSerializer, it) }
        is JsonObject ->
          listOf(jsonDecoder.json.decodeFromJsonElement(BezierValueSerializer, sElem))
        else -> emptyList()
      }

    return BezierPropertyKeyframe(
      frame = frame,
      hold = hold,
      inTangent = inTangent,
      outTangent = outTangent,
      value = value,
    )
  }

  override fun serialize(encoder: Encoder, value: BezierPropertyKeyframe) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        put("t", value.frame)
        if (value.hold) put("h", 1)
        value.inTangent?.let {
          put("i", jsonEncoder.json.encodeToJsonElement(ScalarKeyframeEasingSerializer, it))
        }
        value.outTangent?.let {
          put("o", jsonEncoder.json.encodeToJsonElement(ScalarKeyframeEasingSerializer, it))
        }
        put(
          "s",
          jsonEncoder.json.encodeToJsonElement(ListSerializer(BezierValueSerializer), value.value),
        )
      }
    )
  }
}
