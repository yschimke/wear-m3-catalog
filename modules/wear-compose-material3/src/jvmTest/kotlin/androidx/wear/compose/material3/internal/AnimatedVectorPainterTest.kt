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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The timeline arithmetic behind the animated dialog icons.
 *
 * A rendered frame cannot check this: the interesting values are the ones between frames, and the
 * one thing a still would show — the end state — is the one value that was already right when the
 * drawables were frozen.
 */
class AnimatedVectorPainterTest {

    private fun track(vararg segments: VectorAnimationSegment) =
        VectorAnimationTrack(targetName = "target", segments = segments.toList())

    private val simple =
        track(
            VectorAnimationSegment(
                property = VectorAnimatedProperty.TrimPathEnd,
                startOffsetMillis = 0,
                durationMillis = 200,
                from = 0f,
                to = 1f,
                easing = LinearEasing,
            )
        )

    @Test
    fun holdsTheStartValueBeforeItBegins() {
        assertEquals(0f, simple.valueOf(VectorAnimatedProperty.TrimPathEnd, 0f))
    }

    @Test
    fun holdsTheEndValueAfterItFinishes() {
        assertEquals(1f, simple.valueOf(VectorAnimatedProperty.TrimPathEnd, 200f))
        // Past the end too: the clock keeps counting, the value does not keep growing.
        assertEquals(1f, simple.valueOf(VectorAnimatedProperty.TrimPathEnd, 10_000f))
    }

    @Test
    fun interpolatesInBetween() {
        assertEquals(0.5f, simple.valueOf(VectorAnimatedProperty.TrimPathEnd, 100f))
        assertEquals(0.25f, simple.valueOf(VectorAnimatedProperty.TrimPathEnd, 50f))
    }

    @Test
    fun aPropertyNothingAnimatesHasNoValue() {
        assertNull(simple.valueOf(VectorAnimatedProperty.Rotation, 100f))
    }

    /**
     * The shape the failure animation actually uses: two of its four strokes hold at zero for
     * 100 ms and only then draw. Upstream writes that as a `0 → 0` segment followed by the real
     * one, so a start offset that was ignored would make all four strokes draw together.
     */
    @Test
    fun aStaggeredTrackWaitsForItsOffset() {
        val staggered =
            track(
                VectorAnimationSegment(
                    property = VectorAnimatedProperty.TrimPathEnd,
                    startOffsetMillis = 0,
                    durationMillis = 100,
                    from = 0f,
                    to = 0f,
                    easing = LinearEasing,
                ),
                VectorAnimationSegment(
                    property = VectorAnimatedProperty.TrimPathEnd,
                    startOffsetMillis = 100,
                    durationMillis = 200,
                    from = 0f,
                    to = 1f,
                    easing = LinearEasing,
                ),
            )

        assertEquals(0f, staggered.valueOf(VectorAnimatedProperty.TrimPathEnd, 50f))
        assertEquals(0f, staggered.valueOf(VectorAnimatedProperty.TrimPathEnd, 100f))
        assertEquals(0.5f, staggered.valueOf(VectorAnimatedProperty.TrimPathEnd, 200f))
        assertEquals(1f, staggered.valueOf(VectorAnimatedProperty.TrimPathEnd, 300f))
    }

    @Test
    fun theTotalDurationIsTheLastMomentAnythingMoves() {
        assertEquals(200, listOf(simple).totalDurationMillis)
        assertEquals(
            300,
            listOf(
                    simple,
                    track(
                        VectorAnimationSegment(
                            property = VectorAnimatedProperty.TrimPathEnd,
                            startOffsetMillis = 100,
                            durationMillis = 200,
                            from = 0f,
                            to = 1f,
                        )
                    ),
                )
                .totalDurationMillis,
        )
    }

    /** What the generator actually emitted, checked against the upstream XML it came from. */
    @Test
    fun theGeneratedTracksMatchUpstream() {
        val check = GeneratedVectorAnimations.getValue("wear_m3c_check_animation")
        assertEquals(1, check.size, "the check mark is one stroke drawing itself on")
        assertEquals(267, check.totalDurationMillis)
        assertEquals(0f, check.single().valueOf(VectorAnimatedProperty.TrimPathEnd, 0f))
        assertEquals(1f, check.single().valueOf(VectorAnimatedProperty.TrimPathEnd, 267f))

        val failure = GeneratedVectorAnimations.getValue("wear_m3c_failure_animation")
        assertEquals(4, failure.size, "the failure icon is four strokes")
        assertEquals(367, failure.totalDurationMillis, "two strokes wait 100ms, then draw for 267")
        // Half of them are staggered, half start immediately — at 100ms they must disagree.
        val at100 = failure.map { it.valueOf(VectorAnimatedProperty.TrimPathEnd, 100f) }
        assertTrue(at100.toSet().size > 1, "expected a stagger, got $at100")
    }

    /** A drawable whose motion the generator cannot express must be absent, not half-emitted. */
    @Test
    fun theUnexpressibleDrawablesAreAbsent() {
        assertTrue("wear_m3c_open_on_phone_animation" !in GeneratedVectorAnimations)
        assertTrue("wear_m3c_error" !in GeneratedVectorAnimations)
    }
}
