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
 * `android.util.Log`, with the same shape, printing to stdout.
 *
 * Every call site in the ported sources is behind a `DEBUG`/`isEnabled` flag that is `false` in a
 * release build, so this is diagnostics for whoever is debugging the port, not a logging facility
 * to build on. Kept as an object with the same method names so the transform ports each call site
 * by rewriting one import.
 */
public object Log {
    public fun v(tag: String, message: String): Int = print("V", tag, message)

    public fun d(tag: String, message: String): Int = print("D", tag, message)

    public fun i(tag: String, message: String): Int = print("I", tag, message)

    public fun w(tag: String, message: String): Int = print("W", tag, message)

    public fun e(tag: String, message: String): Int = print("E", tag, message)

    // The four-argument forms: Android's overloads take a trailing Throwable.
    public fun v(tag: String, message: String, error: Throwable?): Int = print("V", tag, message, error)

    public fun d(tag: String, message: String, error: Throwable?): Int = print("D", tag, message, error)

    public fun i(tag: String, message: String, error: Throwable?): Int = print("I", tag, message, error)

    public fun w(tag: String, message: String, error: Throwable?): Int = print("W", tag, message, error)

    public fun e(tag: String, message: String, error: Throwable?): Int = print("E", tag, message, error)

    private fun print(level: String, tag: String, message: String, error: Throwable? = null): Int {
        println("$level/$tag: $message" + (error?.let { " ${it.stackTraceToString()}" } ?: ""))
        return 0
    }
}
