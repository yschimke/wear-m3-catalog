// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.
// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.
package androidx.wear.compose.material3.internal

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing

/**
 * The motion of each animated vector drawable, keyed by drawable name.
 *
 * A drawable absent from this map is one whose `<animated-vector>` moves something this
 * generator does not express — a `pathData` morph, most of them — and is drawn as the still
 * it has always been.
 */
internal val GeneratedVectorAnimations: Map<String, List<VectorAnimationTrack>> =
    mapOf(
        "wear_m3c_check_animation" to
            listOf(
                VectorAnimationTrack(
                    targetName = "_R_G_L_0_G_D_0_P_0",
                    segments =
                        listOf(
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 0,
                                durationMillis = 267,
                                from = 0.0f,
                                to = 1.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                        ),
                ),
            ),
        "wear_m3c_failure_animation" to
            listOf(
                VectorAnimationTrack(
                    targetName = "_R_G_L_1_G_D_0_P_0",
                    segments =
                        listOf(
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 0,
                                durationMillis = 100,
                                from = 0.0f,
                                to = 0.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 100,
                                durationMillis = 267,
                                from = 0.0f,
                                to = 1.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                        ),
                ),
                VectorAnimationTrack(
                    targetName = "_R_G_L_1_G_D_1_P_0",
                    segments =
                        listOf(
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 0,
                                durationMillis = 100,
                                from = 0.0f,
                                to = 0.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 100,
                                durationMillis = 267,
                                from = 0.0f,
                                to = 1.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                        ),
                ),
                VectorAnimationTrack(
                    targetName = "_R_G_L_0_G_D_0_P_0",
                    segments =
                        listOf(
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 0,
                                durationMillis = 267,
                                from = 0.0f,
                                to = 1.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                        ),
                ),
                VectorAnimationTrack(
                    targetName = "_R_G_L_0_G_D_1_P_0",
                    segments =
                        listOf(
                            VectorAnimationSegment(
                                property = VectorAnimatedProperty.TrimPathEnd,
                                startOffsetMillis = 0,
                                durationMillis = 267,
                                from = 0.0f,
                                to = 1.0f,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
                            ),
                        ),
                ),
            ),
    )
