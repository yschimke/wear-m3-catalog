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
 * Base class for all Lottie position properties.
 *
 * Unifies static constant positions ([StaticPositionProperty]), keyframed dynamic animations
 * ([AnimatedPositionProperty]), and split dimensional positions ([SplitPositionProperty]) under a
 * shared contract for the AST and renderer pipeline.
 */
@Serializable(with = BasePositionPropertySerializer::class)
internal sealed class BasePositionProperty {
  abstract val animated: Boolean
  abstract val slotId: String?
}

/** A static position property holding a list of [Float]s (e.g., [x, y] or [x, y, z]). */
@Serializable(with = StaticPositionPropertySerializer::class)
internal data class StaticPositionProperty(
  @SerialName("sid") override val slotId: String? = null,
  override val animated: Boolean = false,
  @SerialName("k") val value: List<Float>,
) : BasePositionProperty()

/** An animated position property with multi-dimensional keyframes. */
@Serializable(with = AnimatedPositionPropertySerializer::class)
internal data class AnimatedPositionProperty(
  @SerialName("sid") override val slotId: String? = null,
  @SerialName("a") val animatedInt: Int = 1,
  @SerialName("k") val keyframes: List<PositionPropertyKeyframe>,
) : BasePositionProperty() {
  override val animated: Boolean
    get() = animatedInt == 1
}

/**
 * A split position property where individual X, Y, and optional Z coordinates are animated
 * separately using scalar properties.
 */
@Serializable(with = SplitPositionPropertySerializer::class)
internal data class SplitPositionProperty(
  @SerialName("sid") override val slotId: String? = null,
  @SerialName("s") val split: Boolean = true,
  @SerialName("x") val x: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("y") val y: BaseScalarProperty = StaticScalarProperty(value = 0f),
  @SerialName("z") val z: BaseScalarProperty? = null,
) : BasePositionProperty() {
  override val animated: Boolean
    get() = x.animated || y.animated || (z?.animated ?: false)
}

/** A single keyframe for an animated position property with optional spatial tangents. */
@Serializable(with = PositionPropertyKeyframeSerializer::class)
internal data class PositionPropertyKeyframe(
  @SerialName("t") val frame: Float = 0f,
  @SerialName("h") val hold: Boolean = false,
  @SerialName("i") val inTangent: KeyframeEasing? = null,
  @SerialName("o") val outTangent: KeyframeEasing? = null,
  @SerialName("s") val value: List<Float> = emptyList(),
  @SerialName("ti") val spatialInTangent: List<Float>? = null,
  @SerialName("to") val spatialOutTangent: List<Float>? = null,
)

/** Polymorphic serializer for [BasePositionProperty] based on "s" and "a" fields. */
internal object BasePositionPropertySerializer :
  JsonContentPolymorphicSerializer<BasePositionProperty>(BasePositionProperty::class) {
  override fun selectDeserializer(
    element: JsonElement
  ): DeserializationStrategy<BasePositionProperty> {
    if (element is JsonObject) {
      val isSplit =
        element["s"]?.let { sElem ->
          (sElem as? JsonPrimitive)?.booleanOrNull ?: ((sElem as? JsonPrimitive)?.intOrNull == 1)
        } ?: false
      if (isSplit) {
        return SplitPositionPropertySerializer
      }
      val animated = element["a"]?.jsonPrimitive?.intOrNull == 1
      if (animated) {
        return AnimatedPositionPropertySerializer
      }
    }
    return StaticPositionPropertySerializer
  }
}

/**
 * Helper to parse a position coordinate list from a [JsonElement], supporting primitive numbers,
 * float arrays, nested float arrays, and nested objects.
 */
internal fun parsePositionVectorElement(element: JsonElement?): List<Float> {
  return when (element) {
    null -> emptyList()
    is JsonPrimitive -> element.floatOrNull?.let { listOf(it) } ?: emptyList()
    is JsonArray -> {
      if (element.isEmpty()) {
        emptyList()
      } else if (element.first() is JsonArray) {
        parsePositionVectorElement(element.first())
      } else {
        element.mapNotNull { it.jsonPrimitive.floatOrNull }
      }
    }
    is JsonObject -> {
      element["k"]?.let { parsePositionVectorElement(it) }
        ?: element["s"]?.let { parsePositionVectorElement(it) }
        ?: emptyList()
    }
  }
}

/**
 * Serializer for [StaticPositionProperty] supporting numbers, arrays, nested arrays, and slot IDs.
 */
internal object StaticPositionPropertySerializer : KSerializer<StaticPositionProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("StaticPositionProperty") {
      element<String?>("sid", isOptional = true)
      element<Boolean>("animated", isOptional = true)
      element<List<Float>>("k")
    }

  override fun deserialize(decoder: Decoder): StaticPositionProperty {
    val jsonDecoder = decoder as JsonDecoder
    val element = jsonDecoder.decodeJsonElement()
    return when (element) {
      is JsonObject -> {
        val slotId = element["sid"]?.jsonPrimitive?.contentOrNull
        val kElem = element["k"]
        val vector =
          if (kElem != null) parsePositionVectorElement(kElem)
          else parsePositionVectorElement(element)
        StaticPositionProperty(slotId = slotId, animated = false, value = vector)
      }
      is JsonArray -> {
        val vector = parsePositionVectorElement(element)
        StaticPositionProperty(slotId = null, animated = false, value = vector)
      }
      is JsonPrimitive -> {
        val vector = parsePositionVectorElement(element)
        StaticPositionProperty(slotId = null, animated = false, value = vector)
      }
    }
  }

  override fun serialize(encoder: Encoder, value: StaticPositionProperty) {
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

/**
 * Serializer for [AnimatedPositionProperty] supporting keyframed position animations and slot IDs.
 */
internal object AnimatedPositionPropertySerializer : KSerializer<AnimatedPositionProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("AnimatedPositionProperty") {
      element<String?>("sid", isOptional = true)
      element<Int>("a")
      element<List<PositionPropertyKeyframe>>("k")
    }

  override fun deserialize(decoder: Decoder): AnimatedPositionProperty {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject
    val slotId = obj["sid"]?.jsonPrimitive?.contentOrNull
    val animatedInt = obj["a"]?.jsonPrimitive?.intOrNull ?: 1
    val keyframesArray = obj["k"]?.jsonArray
    val keyframes =
      keyframesArray?.map { element ->
        jsonDecoder.json.decodeFromJsonElement(PositionPropertyKeyframeSerializer, element)
      } ?: emptyList()
    return AnimatedPositionProperty(
      slotId = slotId,
      animatedInt = animatedInt,
      keyframes = keyframes,
    )
  }

  override fun serialize(encoder: Encoder, value: AnimatedPositionProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", value.animatedInt)
        put(
          "k",
          jsonEncoder.json.encodeToJsonElement(
            ListSerializer(PositionPropertyKeyframeSerializer),
            value.keyframes,
          ),
        )
      }
    )
  }
}

/** Serializer for [SplitPositionProperty] supporting x, y, z scalar properties and slot IDs. */
internal object SplitPositionPropertySerializer : KSerializer<SplitPositionProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("SplitPositionProperty") {
      element<String?>("sid", isOptional = true)
      element<Boolean>("s", isOptional = true)
      element<BaseScalarProperty>("x", isOptional = true)
      element<BaseScalarProperty>("y", isOptional = true)
      element<BaseScalarProperty?>("z", isOptional = true)
    }

  override fun deserialize(decoder: Decoder): SplitPositionProperty {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject
    val slotId = obj["sid"]?.jsonPrimitive?.contentOrNull
    val split =
      obj["s"]?.let { sElem ->
        (sElem as? JsonPrimitive)?.booleanOrNull ?: ((sElem as? JsonPrimitive)?.intOrNull == 1)
      } ?: true
    val x =
      obj["x"]?.let { jsonDecoder.json.decodeFromJsonElement(BaseScalarPropertySerializer, it) }
        ?: StaticScalarProperty(value = 0f)
    val y =
      obj["y"]?.let { jsonDecoder.json.decodeFromJsonElement(BaseScalarPropertySerializer, it) }
        ?: StaticScalarProperty(value = 0f)
    val z =
      obj["z"]?.let { jsonDecoder.json.decodeFromJsonElement(BaseScalarPropertySerializer, it) }

    return SplitPositionProperty(slotId = slotId, split = split, x = x, y = y, z = z)
  }

  override fun serialize(encoder: Encoder, value: SplitPositionProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("s", value.split)
        put("x", jsonEncoder.json.encodeToJsonElement(BaseScalarPropertySerializer, value.x))
        put("y", jsonEncoder.json.encodeToJsonElement(BaseScalarPropertySerializer, value.y))
        value.z?.let {
          put("z", jsonEncoder.json.encodeToJsonElement(BaseScalarPropertySerializer, it))
        }
      }
    )
  }
}

/**
 * Serializer for [PositionPropertyKeyframe] handling keyframe timing, easing, hold flag, position
 * value, and spatial tangents.
 */
internal object PositionPropertyKeyframeSerializer : KSerializer<PositionPropertyKeyframe> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("PositionPropertyKeyframe") {
      element<Float>("t", isOptional = true)
      element<Boolean>("h", isOptional = true)
      element<KeyframeEasing?>("i", isOptional = true)
      element<KeyframeEasing?>("o", isOptional = true)
      element<List<Float>>("s", isOptional = true)
      element<List<Float>?>("ti", isOptional = true)
      element<List<Float>?>("to", isOptional = true)
    }

  override fun deserialize(decoder: Decoder): PositionPropertyKeyframe {
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
    val value = parsePositionVectorElement(sElem)
    val spatialInTangent = obj["ti"]?.let { parsePositionVectorElement(it) }
    val spatialOutTangent = obj["to"]?.let { parsePositionVectorElement(it) }

    return PositionPropertyKeyframe(
      frame = frame,
      hold = hold,
      inTangent = inTangent,
      outTangent = outTangent,
      value = value,
      spatialInTangent = spatialInTangent,
      spatialOutTangent = spatialOutTangent,
    )
  }

  override fun serialize(encoder: Encoder, value: PositionPropertyKeyframe) {
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
        value.spatialInTangent?.let { tangents ->
          put(
            "ti",
            buildJsonArray {
              for (v in tangents) {
                add(JsonPrimitive(v))
              }
            },
          )
        }
        value.spatialOutTangent?.let { tangents ->
          put(
            "to",
            buildJsonArray {
              for (v in tangents) {
                add(JsonPrimitive(v))
              }
            },
          )
        }
      }
    )
  }
}
