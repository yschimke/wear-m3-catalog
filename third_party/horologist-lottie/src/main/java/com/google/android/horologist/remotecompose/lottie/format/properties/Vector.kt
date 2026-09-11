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
import kotlinx.serialization.json.add
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Base class for all Lottie vector properties (e.g. scale, size).
 *
 * Unifies static constant vectors ([StaticVectorProperty]) and keyframed dynamic animations
 * ([AnimatedVectorProperty]) under a shared contract for the AST and renderer pipeline.
 */
@Serializable(with = BaseVectorPropertySerializer::class)
internal sealed class BaseVectorProperty {
  abstract val animated: Boolean
  abstract val slotId: String?
}

/** A static vector property holding a list of [Float]s. */
@Serializable(with = StaticVectorPropertySerializer::class)
internal data class StaticVectorProperty(
  @SerialName("sid") override val slotId: String? = null,
  override val animated: Boolean = false,
  @SerialName("k") val value: List<Float>,
) : BaseVectorProperty()

/** An animated vector property with keyframes. */
@Serializable(with = AnimatedVectorPropertySerializer::class)
internal data class AnimatedVectorProperty(
  @SerialName("sid") override val slotId: String? = null,
  @SerialName("a") val animatedInt: Int = 1,
  @SerialName("k") val keyframes: List<VectorPropertyKeyframe>,
) : BaseVectorProperty() {
  override val animated: Boolean
    get() = animatedInt == 1
}

/** A single keyframe for an animated vector property. */
@Serializable(with = VectorPropertyKeyframeSerializer::class)
internal data class VectorPropertyKeyframe(
  @SerialName("t") val frame: Float = 0f,
  @SerialName("h") val hold: Boolean = false,
  @SerialName("i") val inTangent: KeyframeEasing? = null,
  @SerialName("o") val outTangent: KeyframeEasing? = null,
  @SerialName("s") val value: List<Float> = emptyList(),
)

/** Polymorphic serializer for [BaseVectorProperty] based on "a" field. */
internal object BaseVectorPropertySerializer :
  JsonContentPolymorphicSerializer<BaseVectorProperty>(BaseVectorProperty::class) {
  override fun selectDeserializer(
    element: JsonElement
  ): DeserializationStrategy<BaseVectorProperty> {
    val animated = element is JsonObject && element["a"]?.jsonPrimitive?.intOrNull == 1
    return if (animated) {
      AnimatedVectorPropertySerializer
    } else {
      StaticVectorPropertySerializer
    }
  }
}

/**
 * Helper to parse a vector (list of floats) from a [JsonElement], supporting primitive numbers,
 * float arrays, nested float arrays, and nested objects.
 */
internal fun parseVectorElement(element: JsonElement?): List<Float> {
  return when (element) {
    null -> emptyList()
    is JsonPrimitive -> element.floatOrNull?.let { listOf(it) } ?: emptyList()
    is JsonArray -> {
      if (element.isEmpty()) {
        emptyList()
      } else if (element.first() is JsonArray) {
        parseVectorElement(element.first())
      } else {
        element.mapNotNull { it.jsonPrimitive.floatOrNull }
      }
    }
    is JsonObject -> {
      element["k"]?.let { parseVectorElement(it) }
        ?: element["s"]?.let { parseVectorElement(it) }
        ?: emptyList()
    }
  }
}

/**
 * Serializer for [StaticVectorProperty] supporting numbers, arrays, nested arrays, and slot IDs.
 */
internal object StaticVectorPropertySerializer : KSerializer<StaticVectorProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("StaticVectorProperty") {
      element<String?>("sid", isOptional = true)
      element<Boolean>("animated", isOptional = true)
      element<List<Float>>("k")
    }

  override fun deserialize(decoder: Decoder): StaticVectorProperty {
    val jsonDecoder = decoder as JsonDecoder
    val element = jsonDecoder.decodeJsonElement()
    return when (element) {
      is JsonObject -> {
        val slotId = element["sid"]?.jsonPrimitive?.contentOrNull
        val kElem = element["k"]
        val vector = if (kElem != null) parseVectorElement(kElem) else parseVectorElement(element)
        StaticVectorProperty(slotId = slotId, animated = false, value = vector)
      }
      is JsonArray -> {
        val vector = parseVectorElement(element)
        StaticVectorProperty(slotId = null, animated = false, value = vector)
      }
      is JsonPrimitive -> {
        val vector = parseVectorElement(element)
        StaticVectorProperty(slotId = null, animated = false, value = vector)
      }
    }
  }

  override fun serialize(encoder: Encoder, value: StaticVectorProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", 0)
        put(
          "k",
          buildJsonArray {
            for (v in value.value) {
              add(JsonPrimitive(v))
            }
          },
        )
      }
    )
  }
}

/** Serializer for [AnimatedVectorProperty] supporting keyframed vector animations and slot IDs. */
internal object AnimatedVectorPropertySerializer : KSerializer<AnimatedVectorProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("AnimatedVectorProperty") {
      element<String?>("sid", isOptional = true)
      element<Int>("a")
      element<List<VectorPropertyKeyframe>>("k")
    }

  override fun deserialize(decoder: Decoder): AnimatedVectorProperty {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject
    val slotId = obj["sid"]?.jsonPrimitive?.contentOrNull
    val animatedInt = obj["a"]?.jsonPrimitive?.intOrNull ?: 1
    val keyframesArray = obj["k"]?.jsonArray
    val keyframes =
      keyframesArray?.map { element ->
        jsonDecoder.json.decodeFromJsonElement(VectorPropertyKeyframeSerializer, element)
      } ?: emptyList()
    return AnimatedVectorProperty(slotId = slotId, animatedInt = animatedInt, keyframes = keyframes)
  }

  override fun serialize(encoder: Encoder, value: AnimatedVectorProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", value.animatedInt)
        put(
          "k",
          jsonEncoder.json.encodeToJsonElement(
            ListSerializer(VectorPropertyKeyframeSerializer),
            value.keyframes,
          ),
        )
      }
    )
  }
}

/**
 * Serializer for [VectorPropertyKeyframe] handling keyframe timing, easing, hold flag, and vector
 * value.
 */
internal object VectorPropertyKeyframeSerializer : KSerializer<VectorPropertyKeyframe> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("VectorPropertyKeyframe") {
      element<Float>("t", isOptional = true)
      element<Boolean>("h", isOptional = true)
      element<KeyframeEasing?>("i", isOptional = true)
      element<KeyframeEasing?>("o", isOptional = true)
      element<List<Float>>("s", isOptional = true)
    }

  override fun deserialize(decoder: Decoder): VectorPropertyKeyframe {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject

    val frame = obj["t"]?.jsonPrimitive?.floatOrNull ?: 0f
    val hold =
      when (val hElem = obj["h"]) {
        is JsonPrimitive -> hElem.booleanOrNull ?: ((hElem.intOrNull ?: 0) == 1)
        else -> false
      }
    val inTangent =
      obj["i"]?.let { jsonDecoder.json.decodeFromJsonElement(KeyframeEasingSerializer, it) }
    val outTangent =
      obj["o"]?.let { jsonDecoder.json.decodeFromJsonElement(KeyframeEasingSerializer, it) }
    val sElem = obj["s"]
    val value = parseVectorElement(sElem)

    return VectorPropertyKeyframe(
      frame = frame,
      hold = hold,
      inTangent = inTangent,
      outTangent = outTangent,
      value = value,
    )
  }

  override fun serialize(encoder: Encoder, value: VectorPropertyKeyframe) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        put("t", value.frame)
        if (value.hold) put("h", 1)
        value.inTangent?.let {
          put("i", jsonEncoder.json.encodeToJsonElement(KeyframeEasingSerializer, it))
        }
        value.outTangent?.let {
          put("o", jsonEncoder.json.encodeToJsonElement(KeyframeEasingSerializer, it))
        }
        put(
          "s",
          buildJsonArray {
            for (v in value.value) {
              add(JsonPrimitive(v))
            }
          },
        )
      }
    )
  }
}
