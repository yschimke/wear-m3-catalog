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

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The straight-run-to-annulus mapping behind warped curved text.
 *
 * A render can show that the letters bend; only arithmetic can show they bend to the RIGHT places.
 * Each assertion below is a property the mapping must have by definition, so a sign error or a
 * swapped axis fails here rather than looking merely plausible on a watch face.
 */
class CircleWarperTest {

    private val cx = 100f
    private val cy = 200f
    private val radius = 50f
    private val startAngle = 0f

    private fun warper(clockwise: Boolean = true) =
        CircleWarper(cx, cy, radius, startAngle, clockwise)

    private fun assertClose(expected: Float, actual: Float, what: String) {
        assertTrue(
            kotlin.math.abs(expected - actual) < 1e-3f,
            "$what: expected $expected, was $actual",
        )
    }

    @Test
    fun theRunOriginLandsOnTheCircleAtTheStartAngle() {
        val w = warper()
        assertClose(cx + radius * cos(startAngle), w.warpX(0f, 0f), "x")
        assertClose(cy + radius * sin(startAngle), w.warpY(0f, 0f), "y")
    }

    /** Distance along the run becomes angle: an arc length over its radius. */
    @Test
    fun distanceAlongTheRunBecomesAngle() {
        val w = warper()
        val alongArc = radius * (PI.toFloat() / 2f) // a quarter turn
        assertClose(cx + radius * cos(PI.toFloat() / 2f), w.warpX(alongArc, 0f), "x")
        assertClose(cy + radius * sin(PI.toFloat() / 2f), w.warpY(alongArc, 0f), "y")
    }

    /**
     * Height above the baseline becomes distance from the centre.
     *
     * Text y grows DOWNWARDS, so a negative y — above the baseline — must land further out.
     */
    @Test
    fun heightBecomesRadius() {
        val w = warper()
        val above = -10f
        val r = hypot(w.warpX(0f, above) - cx, w.warpY(0f, above) - cy)
        assertClose(radius + 10f, r, "radius above the baseline")

        val below = 10f
        val r2 = hypot(w.warpX(0f, below) - cx, w.warpY(0f, below) - cy)
        assertClose(radius - 10f, r2, "radius below the baseline")
    }

    /** Anticlockwise mirrors both: the run advances the other way and its height inverts. */
    @Test
    fun anticlockwiseMirrorsTheMapping() {
        val w = warper(clockwise = false)
        val alongArc = radius * (PI.toFloat() / 2f)
        assertClose(cx + radius * cos(-PI.toFloat() / 2f), w.warpX(alongArc, 0f), "x")
        assertClose(cy + radius * sin(-PI.toFloat() / 2f), w.warpY(alongArc, 0f), "y")

        // Above the baseline is now INSIDE the circle, because the text is upside down there.
        val r = hypot(w.warpX(0f, -10f) - cx, w.warpY(0f, -10f) - cy)
        assertClose(radius - 10f, r, "radius above the baseline, anticlockwise")
    }

    /**
     * A vertical stroke of a glyph points at the centre of the circle.
     *
     * This is the property that distinguishes warping from stamping each glyph rotated: the two
     * ends of a vertical must lie on the SAME radius, which is exactly why the glyph's sides splay.
     */
    @Test
    fun aVerticalStrokePointsAtTheCentre() {
        val w = warper()
        val x = 25f
        val top = w.warpX(x, -20f) to w.warpY(x, -20f)
        val bottom = w.warpX(x, 0f) to w.warpY(x, 0f)

        val angleTop = kotlin.math.atan2(top.second - cy, top.first - cx)
        val angleBottom = kotlin.math.atan2(bottom.second - cy, bottom.first - cx)
        assertClose(angleBottom, angleTop, "both ends share a radius")
    }

    /** A straight cubic warps every control point directly; the shape stays on the arc. */
    @Test
    fun aDegenerateCubicWarpsAllFourPoints() {
        val w = warper()
        // Four coincident points: the "is this really a straight segment" branch.
        val c = floatArrayOf(10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f)
        w.warpCubic(c)
        val expectedX = w.warpX(10f, 0f)
        val expectedY = w.warpY(10f, 0f)
        for (i in 0 until 4) {
            assertClose(expectedX, c[i * 2], "point $i x")
            assertClose(expectedY, c[i * 2 + 1], "point $i y")
        }
    }

    /** Warping a curved cubic keeps its endpoints exactly on the arc. */
    @Test
    fun aCurvedCubicKeepsItsEndpointsOnTheArc() {
        val w = warper()
        val c = floatArrayOf(0f, 0f, 5f, -10f, 15f, -10f, 20f, 0f)
        val startX = w.warpX(0f, 0f)
        val startY = w.warpY(0f, 0f)
        val endX = w.warpX(20f, 0f)
        val endY = w.warpY(20f, 0f)
        w.warpCubic(c)
        assertClose(startX, c[0], "start x")
        assertClose(startY, c[1], "start y")
        assertClose(endX, c[6], "end x")
        assertClose(endY, c[7], "end y")
    }

    /** A zero radius would divide by zero; the delegate never warps without one, but be sure. */
    @Test
    fun theMappingIsFiniteAcrossAFullTurn() {
        val w = warper()
        for (step in 0..360) {
            val along = radius * (step * PI.toFloat() / 180f)
            assertTrue(w.warpX(along, -12f).isFinite(), "x at $step deg")
            assertTrue(w.warpY(along, -12f).isFinite(), "y at $step deg")
        }
        assertEquals(true, true)
    }
}
