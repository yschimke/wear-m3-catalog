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

package ee.schimke.wearcmp.port

/**
 * `System.identityHashCode`, which several Material 3 classes use to give an object a stable hash
 * that does not depend on its contents.
 */
public expect fun identityHashCode(value: Any): Int

/** `java.lang.Math.toRadians`. */
public fun toRadians(degrees: Double): Double = degrees * kotlin.math.PI / 180.0

/**
 * `java.lang.Math.round`, which is NOT `kotlin.math.round`: Java rounds half up and narrows the
 * result, Kotlin rounds half to even and keeps the width. Both overloads are here because both
 * distinctions matter to the colour maths that calls them.
 */
public fun round(value: Double): Long = kotlin.math.floor(value + 0.5).toLong()

public fun round(value: Float): Int = kotlin.math.floor(value + 0.5f).toInt()

/** `TimeUnit.MILLISECONDS.toNanos`, which is the only thing `java.util.concurrent` is used for. */
public fun millisToNanos(milliseconds: Long): Long = milliseconds * 1_000_000L

/**
 * `MutableList.removeIf`, a Java 8 default method Kotlin's common `MutableList` does not have.
 * Returns whether anything was removed, as Java's does.
 */
public fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean): Boolean {
    val iterator = iterator()
    var removed = false
    while (iterator.hasNext()) {
        if (predicate(iterator.next())) {
            iterator.remove()
            removed = true
        }
    }
    return removed
}

/**
 * `MutableMap.computeIfAbsent`, likewise. Java's contract: a null from [mapping] records nothing
 * and returns null, which is what the call site in `AnimatedCornerShape` relies on when a shape
 * cannot be built for a size yet.
 */
public fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, mapping: (K) -> V?): V? {
    // `V` is deliberately not bounded to `Any`: the one call site is a
    // `mutableStateMapOf<Size, Morph?>`, whose value type is itself nullable. A stored null and an
    // absent key both mean "not computed yet", which is exactly Java's own contract.
    this[key]?.let {
        return it
    }
    val computed = mapping(key) ?: return null
    this[key] = computed
    return computed
}
