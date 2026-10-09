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

package androidx.wear.compose.foundation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A curved run measured into a sweep must fit that sweep again when it is drawn.
 *
 * The layout and the renderer each convert between a run's width and its sweep; if they do it at
 * different radii, a run that exactly fills its own sweep overflows it and loses its last glyph.
 */
class CurvedRunAllowanceTest {

    private val width = 120f
    private val baselineRadius = 180f
    private val warpRadiusOffset = 6f

    @Test
    fun clockwiseRunFitsTheSweepItWasMeasuredInto() {
        // As `CurvedTextChild.doRadialPosition` measures a clockwise warped run.
        val sweep = width / (baselineRadius + warpRadiusOffset)

        assertEquals(
            width,
            curvedRunAllowance(sweep, baselineRadius, warpRadiusOffset, clockwise = true),
            0.001f,
        )
    }

    @Test
    fun anticlockwiseRunFitsTheSweepItWasMeasuredInto() {
        val sweep = width / (baselineRadius - warpRadiusOffset)

        assertEquals(
            width,
            curvedRunAllowance(sweep, baselineRadius, warpRadiusOffset, clockwise = false),
            0.001f,
        )
    }

    @Test
    fun unwarpedRunIsMeasuredAtItsBaseline() {
        val sweep = width / baselineRadius

        assertEquals(width, curvedRunAllowance(sweep, baselineRadius, 0f, clockwise = true), 0.001f)
    }
}
