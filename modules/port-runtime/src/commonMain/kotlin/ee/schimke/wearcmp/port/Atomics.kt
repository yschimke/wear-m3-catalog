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

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.AtomicReference as KotlinAtomicReference

/**
 * `java.util.concurrent.atomic.AtomicReference`, over the multiplatform stdlib.
 *
 * The Kotlin type has the same semantics under different names (`load`/`store` for `get`/`set`),
 * so this exists purely to keep the JDK spelling — which is what lets the transform rewire
 * `InternalMutatorMutex` and the two `SwipeToReveal`s by rewriting one import line each.
 *
 * Wasm is single-threaded, so on the target this port exists for the atomicity is free; the JVM
 * target, where it is not, gets the real thing underneath.
 */
@OptIn(ExperimentalAtomicApi::class)
public class AtomicReference<T>(initialValue: T) {
    private val delegate = KotlinAtomicReference(initialValue)

    public fun get(): T = delegate.load()

    public fun set(value: T) {
        delegate.store(value)
    }

    public fun compareAndSet(expect: T, update: T): Boolean = delegate.compareAndSet(expect, update)

    override fun toString(): String = get().toString()
}

/** `java.util.concurrent.atomic.AtomicInteger`, over the multiplatform stdlib. See [AtomicReference]. */
@OptIn(ExperimentalAtomicApi::class)
public class AtomicInteger(initialValue: Int = 0) {
    private val delegate = AtomicInt(initialValue)

    public fun get(): Int = delegate.load()

    public fun set(value: Int) {
        delegate.store(value)
    }

    public fun getAndIncrement(): Int = delegate.fetchAndAdd(1)

    public fun incrementAndGet(): Int = delegate.addAndFetch(1)

    public fun getAndAdd(delta: Int): Int = delegate.fetchAndAdd(delta)

    public fun compareAndSet(expect: Int, update: Int): Boolean =
        delegate.compareAndSet(expect, update)

    override fun toString(): String = get().toString()
}
