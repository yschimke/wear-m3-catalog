package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.AdapterValueCodec
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull

/** Numeric codecs the tools API leaves to the catalog. Both bind to a `number` state variable. */
object WearValueCodecs {
  val Float: AdapterValueCodec<Float> =
    object : AdapterValueCodec<Float> {
      override val jsonType = "number"
      override val kotlinType = "kotlin.Float"
      override val stateType = "number"

      override fun encode(value: Float): JsonElement {
        require(value.isFinite()) { "expected a finite number" }
        return JsonPrimitive(value)
      }

      override fun decode(value: JsonElement): Float {
        require(value is JsonPrimitive && !value.isString) { "expected a JSON number" }
        return requireNotNull(value.floatOrNull?.takeIf { it.isFinite() }) {
          "expected a finite number"
        }
      }
    }

  /** Whole numbers; `3.0`, as a number state variable stores it, is accepted as `3`. */
  val Int: AdapterValueCodec<Int> =
    object : AdapterValueCodec<Int> {
      override val jsonType = "integer"
      override val kotlinType = "kotlin.Int"
      override val stateType = "number"

      override fun encode(value: Int): JsonElement = JsonPrimitive(value)

      override fun decode(value: JsonElement): Int {
        require(value is JsonPrimitive && !value.isString) { "expected a JSON integer" }
        val number = requireNotNull(value.doubleOrNull) { "expected a JSON integer" }
        require(
          number % 1.0 == 0.0 &&
            number in kotlin.Int.MIN_VALUE.toDouble()..kotlin.Int.MAX_VALUE.toDouble()
        ) {
          "expected a JSON integer"
        }
        return number.toInt()
      }
    }
}
