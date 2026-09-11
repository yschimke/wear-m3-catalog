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

import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.ui.graphics.Color
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
 * Base class for all Lottie color properties.
 *
 * Unifies static constant colors ([StaticColorProperty]) and keyframed dynamic animations
 * ([AnimatedColorProperty]) under a shared contract for the AST and renderer pipeline.
 */
@Serializable(with = BaseColorPropertySerializer::class)
internal sealed class BaseColorProperty {
  abstract val animated: Boolean
  abstract val slotId: String?
}

/** A static color property holding a [RemoteColor] value. */
@Serializable(with = StaticColorPropertySerializer::class)
internal data class StaticColorProperty(
  @SerialName("sid") override val slotId: String? = null,
  override val animated: Boolean = false,
  @SerialName("k") val value: RemoteColor,
) : BaseColorProperty()

/** An animated color property with keyframes. */
@Serializable(with = AnimatedColorPropertySerializer::class)
internal data class AnimatedColorProperty(
  @SerialName("sid") override val slotId: String? = null,
  @SerialName("a") val animatedInt: Int = 1,
  @SerialName("k") val keyframes: List<ColorPropertyKeyframe>,
) : BaseColorProperty() {
  override val animated: Boolean
    get() = animatedInt == 1
}

/** A single keyframe for an animated color property. */
@Serializable(with = ColorPropertyKeyframeSerializer::class)
internal data class ColorPropertyKeyframe(
  @SerialName("t") val frame: Float = 0f,
  @SerialName("h") val hold: Boolean = false,
  @SerialName("i") val inTangent: ScalarKeyframeEasing? = null,
  @SerialName("o") val outTangent: ScalarKeyframeEasing? = null,
  @SerialName("s") val value: RemoteColor,
)

/** Polymorphic serializer for [BaseColorProperty] based on "a" field. */
internal object BaseColorPropertySerializer :
  JsonContentPolymorphicSerializer<BaseColorProperty>(BaseColorProperty::class) {
  override fun selectDeserializer(
    element: JsonElement
  ): DeserializationStrategy<BaseColorProperty> {
    val animated = element is JsonObject && element["a"]?.jsonPrimitive?.intOrNull == 1
    return if (animated) {
      AnimatedColorPropertySerializer
    } else {
      StaticColorPropertySerializer
    }
  }
}

/**
 * Helper to parse a color from a [JsonElement], supporting hex strings
 * (#RGB, #ARGB, #RRGGBB, #AARRGGBB), float/integer arrays ([r, g, b] or [r, g, b, a]), numbers, and
 * nested objects.
 */
internal fun parseColorElement(element: JsonElement?): Color {
  return when (element) {
    null -> Color.Transparent
    is JsonPrimitive -> {
      if (element.isString) {
        parseHexColor(element.content) ?: Color.Transparent
      } else {
        element.intOrNull?.let { Color(it) } ?: Color.Transparent
      }
    }
    is JsonArray -> parseArrayColor(element)
    is JsonObject -> {
      element["k"]?.let { parseColorElement(it) }
        ?: element["s"]?.let { parseColorElement(it) }
        ?: Color.Transparent
    }
  }
}

/** Parses Hex color strings (#RGB, #ARGB, #RRGGBB, #AARRGGBB). */
internal fun parseHexColor(hexString: String): Color? {
  val hex = hexString.trim().removePrefix("#")
  return try {
    when (hex.length) {
      3 -> {
        val r = hex.substring(0, 1).repeat(2).toInt(16)
        val g = hex.substring(1, 2).repeat(2).toInt(16)
        val b = hex.substring(2, 3).repeat(2).toInt(16)
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = 1f)
      }
      4 -> {
        val a = hex.substring(0, 1).repeat(2).toInt(16)
        val r = hex.substring(1, 2).repeat(2).toInt(16)
        val g = hex.substring(2, 3).repeat(2).toInt(16)
        val b = hex.substring(3, 4).repeat(2).toInt(16)
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = a / 255f)
      }
      6 -> {
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = 1f)
      }
      8 -> {
        val a = hex.substring(0, 2).toInt(16)
        val r = hex.substring(2, 4).toInt(16)
        val g = hex.substring(4, 6).toInt(16)
        val b = hex.substring(6, 8).toInt(16)
        Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = a / 255f)
      }
      else -> null
    }
  } catch (e: Exception) {
    null
  }
}

/** Parses float or integer color arrays ([r, g, b] or [r, g, b, a]). */
internal fun parseArrayColor(array: JsonArray): Color {
  val r = array.getOrNull(0)?.jsonPrimitive?.floatOrNull ?: 0f
  val g = array.getOrNull(1)?.jsonPrimitive?.floatOrNull ?: 0f
  val b = array.getOrNull(2)?.jsonPrimitive?.floatOrNull ?: 0f
  val a = if (array.size > 3) array[3].jsonPrimitive.floatOrNull ?: 1f else 1f

  val red = if (r > 1f) (r / 255f).coerceIn(0f, 1f) else r.coerceIn(0f, 1f)
  val green = if (g > 1f) (g / 255f).coerceIn(0f, 1f) else g.coerceIn(0f, 1f)
  val blue = if (b > 1f) (b / 255f).coerceIn(0f, 1f) else b.coerceIn(0f, 1f)
  val alpha = if (a > 1f) (a / 255f).coerceIn(0f, 1f) else a.coerceIn(0f, 1f)

  return Color(red, green, blue, alpha)
}

/** Serializer for [StaticColorProperty] supporting Hex strings, arrays, objects, and slot IDs. */
internal object StaticColorPropertySerializer : KSerializer<StaticColorProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("StaticColorProperty") {
      element<String?>("sid", isOptional = true)
      element<Boolean>("animated", isOptional = true)
      element<List<Float>>("k")
    }

  override fun deserialize(decoder: Decoder): StaticColorProperty {
    val jsonDecoder = decoder as JsonDecoder
    val element = jsonDecoder.decodeJsonElement()
    return when (element) {
      is JsonObject -> {
        val slotId = element["sid"]?.jsonPrimitive?.contentOrNull
        val kElem = element["k"]
        val color = if (kElem != null) parseColorElement(kElem) else parseColorElement(element)
        StaticColorProperty(slotId = slotId, animated = false, value = color.rc)
      }
      is JsonArray -> {
        val color = parseArrayColor(element)
        StaticColorProperty(slotId = null, animated = false, value = color.rc)
      }
      is JsonPrimitive -> {
        val color = parseColorElement(element)
        StaticColorProperty(slotId = null, animated = false, value = color.rc)
      }
    }
  }

  override fun serialize(encoder: Encoder, value: StaticColorProperty) {
    val jsonEncoder = encoder as JsonEncoder
    val color = value.value.constantValueOrNull ?: Color.Transparent
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", 0)
        put(
          "k",
          buildJsonArray {
            add(JsonPrimitive(color.red))
            add(JsonPrimitive(color.green))
            add(JsonPrimitive(color.blue))
            add(JsonPrimitive(color.alpha))
          },
        )
      }
    )
  }
}

/** Serializer for [AnimatedColorProperty] supporting keyframed color animations. */
internal object AnimatedColorPropertySerializer : KSerializer<AnimatedColorProperty> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("AnimatedColorProperty") {
      element<String?>("sid", isOptional = true)
      element<Int>("a")
      element<List<ColorPropertyKeyframe>>("k")
    }

  override fun deserialize(decoder: Decoder): AnimatedColorProperty {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject
    val slotId = obj["sid"]?.jsonPrimitive?.contentOrNull
    val animatedInt = obj["a"]?.jsonPrimitive?.intOrNull ?: 1
    val keyframesArray = obj["k"]?.jsonArray
    val keyframes =
      keyframesArray?.map { element ->
        jsonDecoder.json.decodeFromJsonElement(ColorPropertyKeyframeSerializer, element)
      } ?: emptyList()
    return AnimatedColorProperty(slotId = slotId, animatedInt = animatedInt, keyframes = keyframes)
  }

  override fun serialize(encoder: Encoder, value: AnimatedColorProperty) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(
      buildJsonObject {
        value.slotId?.let { put("sid", it) }
        put("a", value.animatedInt)
        put(
          "k",
          jsonEncoder.json.encodeToJsonElement(
            ListSerializer(ColorPropertyKeyframeSerializer),
            value.keyframes,
          ),
        )
      }
    )
  }
}

/** Serializer for [ColorPropertyKeyframe] handling keyframe timing, easing, and color value. */
internal object ColorPropertyKeyframeSerializer : KSerializer<ColorPropertyKeyframe> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("ColorPropertyKeyframe") {
      element<Float>("t", isOptional = true)
      element<Boolean>("h", isOptional = true)
      element<ScalarKeyframeEasing?>("i", isOptional = true)
      element<ScalarKeyframeEasing?>("o", isOptional = true)
      element<List<Float>>("s", isOptional = true)
    }

  override fun deserialize(decoder: Decoder): ColorPropertyKeyframe {
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
    val color = parseColorElement(sElem)

    return ColorPropertyKeyframe(
      frame = frame,
      hold = hold,
      inTangent = inTangent,
      outTangent = outTangent,
      value = color.rc,
    )
  }

  override fun serialize(encoder: Encoder, value: ColorPropertyKeyframe) {
    val jsonEncoder = encoder as JsonEncoder
    val color = value.value.constantValueOrNull ?: Color.Transparent
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
          buildJsonArray {
            add(JsonPrimitive(color.red))
            add(JsonPrimitive(color.green))
            add(JsonPrimitive(color.blue))
            add(JsonPrimitive(color.alpha))
          },
        )
      }
    )
  }
}
