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

package androidx.wear.compose.material3.internal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The `pathData` morph behind the one-handed-gesture hints.
 *
 * These drawables animate their SHAPE — the pointer's outline bends as the wrist turns — which is
 * the one thing a frozen still cannot show and a float track cannot express. What a rendered frame
 * would check is that something was drawn; what matters is that the shape half way through is half
 * way between the two, and that is arithmetic.
 */
class PathMorphTest {

    private val square = "M 0 0 L 10 0 L 10 10 L 0 10 Z"
    private val bigger = "M 0 0 L 20 0 L 20 20 L 0 20 Z"

    private fun morph(from: String, to: String, duration: Int = 100) =
        VectorAnimationTrack(
            targetName = "target",
            segments = emptyList(),
            pathSegments =
                listOf(
                    VectorPathSegment(
                        startOffsetMillis = 0,
                        durationMillis = duration,
                        from = from,
                        to = to,
                        easing = LinearEasing,
                    )
                ),
        )

    @Test
    fun theMiddleOfAMorphIsBetweenTheTwoShapes() {
        val nodes = morph(square, bigger).pathAt(50f)!!
        // The second node is the top-right corner: 10 in one shape, 20 in the other.
        assertEquals(PathNode.LineTo(15f, 0f), nodes[1])
    }

    @Test
    fun theEndsAreTheShapesThemselves() {
        val track = morph(square, bigger)
        assertEquals(addPathNodes(square), track.pathAt(0f))
        assertEquals(addPathNodes(bigger), track.pathAt(100f))
        assertEquals(addPathNodes(bigger), track.pathAt(10_000f))
    }

    @Test
    fun aTrackThatMorphsNothingSaysSo() {
        assertNull(
            VectorAnimationTrack(targetName = "target", segments = emptyList()).pathAt(50f)
        )
    }

    /** Two shapes with different commands cannot be blended, so the morph snaps instead. */
    @Test
    fun mismatchedShapesSnapRatherThanBlend() {
        val curve = addPathNodes("M 0 0 C 1 1 2 2 3 3")
        val line = addPathNodes("M 0 0 L 3 3")
        assertEquals(curve, lerpPathNodes(curve, line, 0.4f))
        assertEquals(line, lerpPathNodes(curve, line, 0.6f))
    }

    @Test
    fun everyCommandThatCanBeBlendedIs() {
        val from =
            addPathNodes(
                "M0 0 m1 1 L2 2 l1 1 H3 h1 V4 v1 C0 0 1 1 2 2 c0 0 1 1 2 2 Q0 0 1 1 q0 0 1 1 Z"
            )
        val to =
            addPathNodes(
                "M2 2 m3 3 L4 4 l3 3 H5 h3 V6 v3 C2 2 3 3 4 4 c2 2 3 3 4 4 Q2 2 3 3 q2 2 3 3 Z"
            )
        val middle = lerpPathNodes(from, to, 0.5f)
        // Nothing fell through to the snap: every node is a real blend of the two, so no node of
        // the result equals its own endpoint except `Close`, which has nothing to interpolate.
        assertEquals(from.size, middle.size)
        from.indices.forEach { index ->
            if (from[index] !is PathNode.Close) {
                assertTrue(
                    middle[index] != from[index] && middle[index] != to[index],
                    "node $index (${from[index]}) snapped instead of blending",
                )
            }
        }
    }

    /** A path morph counts towards the drawable's length exactly as a float segment does. */
    @Test
    fun aMorphExtendsTheTimeline() {
        assertEquals(100, listOf(morph(square, bigger)).totalDurationMillis)
        assertEquals(370, listOf(morph(square, bigger, duration = 370)).totalDurationMillis)
    }

    /**
     * The generated data itself, on the real artwork.
     *
     * The generator refuses a drawable whose two path strings are not the same sequence of
     * commands, so this is that promise checked on what it actually emitted — the property the
     * whole morph rests on, and the one that would produce a shape belonging to neither if it were
     * ever false.
     */
    @Test
    fun everyGeneratedMorphIsInterpolable() {
        val morphs =
            GeneratedVectorAnimations.values.flatten().flatMap { it.pathSegments }
        assertTrue(morphs.isNotEmpty(), "the gesture indicators should contribute path morphs")
        morphs.forEach { segment ->
            assertEquals(
                segment.fromNodes.map { it::class },
                segment.toNodes.map { it::class },
                "a generated morph is between two different shapes",
            )
        }
    }

    /** And the indicators' own drawables are in the map, with motion rather than a frozen still. */
    @Test
    fun theGestureIndicatorsAnimate() {
        listOf(
                "wear_one_handed_gesture_primary_indicator_animation",
                "wear_one_handed_gesture_dismiss_indicator_animation",
            )
            .forEach { name ->
                val tracks = GeneratedVectorAnimations.getValue(name)
                assertTrue(
                    tracks.any { it.pathSegments.isNotEmpty() },
                    "$name should morph at least one path",
                )
                assertTrue(tracks.totalDurationMillis > 0, "$name should take some time")
            }
    }
}
