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

package androidx.compose.remote.creation

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.common.Utils

/** Platform-neutral encoded path used by Creation Compose. */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class RemotePath(initialCapacity: Int = DefaultCapacity) {
    private var values = FloatArray(initialCapacity)

    public var size: Int = 0
        private set

    public val pathArray: FloatArray
        get() = values

    private var currentX = 0f
    private var currentY = 0f

    public fun rewind() {
        size = 0
        currentX = 0f
        currentY = 0f
    }

    public fun isEmpty(): Boolean = size == 0

    public fun moveTo(x: Float, y: Float) {
        appendMove(Move, x, y)
        currentX = x
        currentY = y
    }

    public fun rMoveTo(dx: Float, dy: Float) {
        currentX += dx
        currentY += dy
        appendMove(Move, currentX, currentY)
    }

    public fun lineTo(x: Float, y: Float) {
        append(Line, x, y)
        currentX = x
        currentY = y
    }

    public fun rLineTo(dx: Float, dy: Float) {
        currentX += dx
        currentY += dy
        append(Line, currentX, currentY)
    }

    public fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        append(Quadratic, x1, y1, x2, y2)
        currentX = x2
        currentY = y2
    }

    public fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float) {
        append(Quadratic, currentX + dx1, currentY + dy1, currentX + dx2, currentY + dy2)
        currentX += dx2
        currentY += dy2
    }

    public fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, weight: Float) {
        append(Conic, x1, y1, x2, y2, weight)
        currentX = x2
        currentY = y2
    }

    public fun rConicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, weight: Float) {
        append(Conic, currentX + dx1, currentY + dy1, currentX + dx2, currentY + dy2, weight)
        currentX += dx2
        currentY += dy2
    }

    public fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        append(Cubic, x1, y1, x2, y2, x3, y3)
        currentX = x3
        currentY = y3
    }

    public fun rCubicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float) {
        append(
            Cubic,
            currentX + dx1,
            currentY + dy1,
            currentX + dx2,
            currentY + dy2,
            currentX + dx3,
            currentY + dy3,
        )
        currentX += dx3
        currentY += dy3
    }

    public fun close() {
        ensureCapacity(1)
        values[size++] = Utils.asNan(Close)
    }

    public fun toFloatArray(): FloatArray = values.copyOf(size)

    private fun appendMove(type: Int, first: Float, second: Float) {
        ensureCapacity(3)
        values[size++] = Utils.asNan(type)
        values[size++] = first
        values[size++] = second
    }

    private fun append(type: Int, vararg coordinates: Float) {
        // The two reserved zero slots are part of the current wire format.
        ensureCapacity(3 + coordinates.size)
        values[size++] = Utils.asNan(type)
        values[size++] = 0f
        values[size++] = 0f
        coordinates.copyInto(values, destinationOffset = size)
        size += coordinates.size
    }

    private fun ensureCapacity(additional: Int) {
        val required = size + additional
        if (required > values.size) values = values.copyOf(maxOf(values.size * 2, required))
    }

    private companion object {
        const val DefaultCapacity = 64
        const val Move = 10
        const val Line = 11
        const val Quadratic = 12
        const val Conic = 13
        const val Cubic = 14
        const val Close = 15
    }
}
