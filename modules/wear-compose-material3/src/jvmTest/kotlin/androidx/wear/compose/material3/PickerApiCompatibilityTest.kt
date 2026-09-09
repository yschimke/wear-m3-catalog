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

package androidx.wear.compose.material3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The pickers' published signature on the JVM is upstream's, unchanged.
 *
 * `DatePicker` takes `java.time.LocalDate` and `TimePicker` takes `java.time.LocalTime` in the real
 * `androidx.wear.compose.material3`, and a caller that compiles against that artifact has to
 * compile and link against this one. The port reaches that through an `expect class` actualised by
 * a `typealias` (see `ee.schimke.wearcmp.port.PortDateTime`), which is invisible in the compiled
 * signature — but only as long as nobody swaps the JVM actual for a library type. So the assertion
 * is made against the bytecode rather than the source: this fails if the alias ever moves.
 */
class PickerApiCompatibilityTest {

    @Test
    fun datePickerTakesJavaTimeLocalDate() {
        val parameters =
            parameterTypesOf("androidx.wear.compose.material3.DatePickerKt", "DatePicker")
        assertEquals(java.time.LocalDate::class.java, parameters[0], "initialDate")
        // initialDate plus the two nullable bounds, minValidDate and maxValidDate.
        assertEquals(3, parameters.count { it == java.time.LocalDate::class.java })
    }

    @Test
    fun timePickerTakesJavaTimeLocalTime() {
        val parameters =
            parameterTypesOf("androidx.wear.compose.material3.TimePickerKt", "TimePicker")
        assertEquals(java.time.LocalTime::class.java, parameters[0], "initialTime")
    }

    @Test
    fun noKotlinxDateTypeReachesThePublicJvmSignature() {
        for ((owner, name) in
            listOf(
                "androidx.wear.compose.material3.DatePickerKt" to "DatePicker",
                "androidx.wear.compose.material3.TimePickerKt" to "TimePicker",
            )) {
            val leaked =
                parameterTypesOf(owner, name).filter { it.name.startsWith("kotlinx.datetime") }
            assertTrue(leaked.isEmpty(), "$name leaks $leaked into its JVM signature")
        }
    }

    /**
     * The parameter types of the public `@Composable` named [name] in [owner], taking the widest
     * overload.
     *
     * The match allows a `name-hash` suffix: both pickers take a value class ([DatePickerType],
     * [TimePickerType]) and the Kotlin compiler mangles the name of any function that does, exactly
     * as it does upstream. The trailing `Composer`/`Int` parameters the Compose compiler adds are
     * left in — nothing here looks past the date ones.
     */
    private fun parameterTypesOf(owner: String, name: String): List<Class<*>> =
        Class.forName(owner)
            .declaredMethods
            .filter { it.name == name || it.name.startsWith("$name-") }
            .maxByOrNull { it.parameterCount }
            .let { requireNotNull(it) { "no $name in $owner" } }
            .parameterTypes
            .toList()
}
