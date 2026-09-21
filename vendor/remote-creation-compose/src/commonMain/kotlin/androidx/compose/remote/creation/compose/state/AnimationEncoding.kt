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

package androidx.compose.remote.creation.compose.state

/** Encodes an animation without depending on the player-side Remote Compose core. */
internal fun packAnimation(
    duration: Float,
    type: Int,
    specification: FloatArray?,
    initialValue: Float,
    wrap: Float,
): FloatArray {
    val hasInitialValue = !initialValue.isNaN()
    val hasWrap = !wrap.isNaN()
    val specificationSize = specification?.size ?: 0
    var size = 0

    if (hasInitialValue) size++
    if (specification != null) size++
    if (specification != null || type != 1) size += 1 + specificationSize
    if (hasInitialValue) size++
    if (hasWrap) size++
    if (duration != 1f || size > 0) size++
    if (hasWrap || hasInitialValue) size++

    if (size == 0) return floatArrayOf()

    val result = FloatArray(size)
    var position = 0
    result[position++] = duration
    if (size > 1) {
        val wrapBit = if (hasWrap) 1 else 0
        val initialValueBit = if (hasInitialValue) 2 else 0
        val bits = type or ((wrapBit or initialValueBit) shl 8)
        result[position++] = Float.fromBits((specificationSize shl 16) or bits)
    }
    specification?.copyInto(result, position).also { position += specificationSize }
    if (hasInitialValue) result[position++] = initialValue
    if (hasWrap) result[position] = wrap
    return result
}
