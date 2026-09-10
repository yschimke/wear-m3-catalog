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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorConfig
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.graphics.vector.rememberVectorPainter

/**
 * Runs an `<animated-vector>`'s motion, off Android.
 *
 * ## Why this exists rather than a library call
 *
 * On Android the whole of this is `animatedVectorResource(R.drawable.…)` plus
 * `rememberAnimatedVectorPainter`. Neither is portable: both live in the `res/` package of
 * `compose-animation-graphics`, which is published for Android only. Compose Multiplatform DOES
 * publish that library's model and animator vocabulary — `AnimatedImageVector`, `ObjectAnimator`,
 * `StateVectorConfig` — but it is a dead end here twice over: there is no XML inflation, and
 * `AnimatedImageVector.targets` is `internal`, so a painter written outside that module can build
 * one and never read its targets back.
 *
 * What IS public, in compose-ui itself, is everything actually needed:
 * ```
 * RenderVectorGroup(root: VectorGroup, configs: Map<String, VectorConfig>)
 * VectorConfig            // one method: getOrDefault(VectorProperty<T>, T)
 * VectorProperty.TrimPathEnd, .Rotation, .TranslateX, … all sixteen
 * ```
 * `RenderVectorGroup` is precisely what Android's own painter calls underneath. So the port needs
 * no new dependency at all: implement the one-method interface, drive it from an `Animatable`, and
 * render the group with the overrides applied.
 *
 * ## What drives it
 *
 * `GeneratedVectorAnimations` — emitted by `tools/transform.py` from the same `<animated-vector>`
 * the artwork comes from, so an upstream retiming arrives with the next sync. A drawable whose
 * motion the generator cannot express (a `pathData` morph) is absent from that map and is drawn as
 * the still it has always been.
 */

/** A float property of a vector element that an `<objectAnimator>` can move. */
internal enum class VectorAnimatedProperty {
    TrimPathStart,
    TrimPathEnd,
    TrimPathOffset,
    Rotation,
    ScaleX,
    ScaleY,
    TranslateX,
    TranslateY,
    PivotX,
    PivotY,
    FillAlpha,
    StrokeAlpha,
    StrokeLineWidth,
}

/** One `<objectAnimator>`: a property moving from [from] to [to] over a window of the timeline. */
internal data class VectorAnimationSegment(
    val property: VectorAnimatedProperty,
    val startOffsetMillis: Int,
    val durationMillis: Int,
    val from: Float,
    val to: Float,
    val easing: Easing = LinearEasing,
)

/** Everything animating one named element of the drawable. */
internal data class VectorAnimationTrack(
    val targetName: String,
    val segments: List<VectorAnimationSegment>,
)

/** The last moment at which anything moves. */
internal val List<VectorAnimationTrack>.totalDurationMillis: Int
    get() =
        maxOfOrNull { track ->
            track.segments.maxOfOrNull { it.startOffsetMillis + it.durationMillis } ?: 0
        } ?: 0

/**
 * The value [property] holds at [elapsedMillis], or null when nothing in this track moves it.
 *
 * Before a segment starts the value is its `from`; after it ends, its `to`. That is what makes the
 * staggered failure animation work: two of its four strokes hold at 0 for 100 ms before drawing,
 * expressed upstream as a zero-length-looking `0 → 0` segment followed by the real one.
 */
internal fun VectorAnimationTrack.valueOf(
    property: VectorAnimatedProperty,
    elapsedMillis: Float,
): Float? {
    var value: Float? = null
    for (segment in segments) {
        if (segment.property != property) continue
        val start = segment.startOffsetMillis.toFloat()
        val end = start + segment.durationMillis
        value =
            when {
                elapsedMillis <= start -> value ?: segment.from
                elapsedMillis >= end -> segment.to
                else -> {
                    val fraction = segment.easing.transform((elapsedMillis - start) / (end - start))
                    segment.from + (segment.to - segment.from) * fraction
                }
            }
    }
    return value
}

/** [VectorConfig] over one track, reading whatever the clock currently says. */
private class TrackVectorConfig(
    private val track: VectorAnimationTrack,
    private val elapsedMillis: State<Float>,
) : VectorConfig {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOrDefault(property: VectorProperty<T>, defaultValue: T): T {
        val animated =
            when (property) {
                is VectorProperty.TrimPathStart -> VectorAnimatedProperty.TrimPathStart
                is VectorProperty.TrimPathEnd -> VectorAnimatedProperty.TrimPathEnd
                is VectorProperty.TrimPathOffset -> VectorAnimatedProperty.TrimPathOffset
                is VectorProperty.Rotation -> VectorAnimatedProperty.Rotation
                is VectorProperty.ScaleX -> VectorAnimatedProperty.ScaleX
                is VectorProperty.ScaleY -> VectorAnimatedProperty.ScaleY
                is VectorProperty.TranslateX -> VectorAnimatedProperty.TranslateX
                is VectorProperty.TranslateY -> VectorAnimatedProperty.TranslateY
                is VectorProperty.PivotX -> VectorAnimatedProperty.PivotX
                is VectorProperty.PivotY -> VectorAnimatedProperty.PivotY
                is VectorProperty.FillAlpha -> VectorAnimatedProperty.FillAlpha
                is VectorProperty.StrokeAlpha -> VectorAnimatedProperty.StrokeAlpha
                is VectorProperty.StrokeLineWidth -> VectorAnimatedProperty.StrokeLineWidth
                // PathData, Fill and Stroke animate a type the generator never emits; the drawable
                // keeps whatever the artwork declares.
                else -> return defaultValue
            }
        return (track.valueOf(animated, elapsedMillis.value) as? T) ?: defaultValue
    }
}

/**
 * A painter for [image] that runs [tracks] once, from the start, when [atEnd] becomes true.
 *
 * The whole timeline is one `Animatable` counting milliseconds, and every track reads its own value
 * out of it. That is simpler than an animation per property and it is what makes the start offsets
 * exact — a segment beginning at 100 ms begins at 100 ms, rather than after whatever the previous
 * animation happened to take.
 *
 * [reduceMotion] does not shorten the animation, it skips it: the clock jumps to the end, so the
 * drawable shows its finished artwork immediately. That is the same end state the frozen still had.
 */
@Composable
internal fun rememberAnimatedVectorPainter(
    image: ImageVector,
    tracks: List<VectorAnimationTrack>,
    atEnd: Boolean,
    reduceMotion: Boolean,
): Painter {
    val totalDuration = remember(tracks) { tracks.totalDurationMillis }
    val elapsed = remember(tracks) { Animatable(0f) }

    LaunchedEffect(atEnd, reduceMotion, totalDuration) {
        when {
            !atEnd -> elapsed.snapTo(0f)
            reduceMotion || totalDuration == 0 -> elapsed.snapTo(totalDuration.toFloat())
            else ->
                elapsed.animateTo(
                    targetValue = totalDuration.toFloat(),
                    // Linear: the easing lives in each segment, where upstream put it.
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing),
                )
        }
    }

    val configs =
        remember(tracks) {
            tracks.associate { track ->
                track.targetName to
                    TrackVectorConfig(track, derivedStateOf { elapsed.value }) as VectorConfig
            }
        }

    return rememberVectorPainter(
        defaultWidth = image.defaultWidth,
        defaultHeight = image.defaultHeight,
        viewportWidth = image.viewportWidth,
        viewportHeight = image.viewportHeight,
        name = image.name,
        tintColor = Color.Unspecified,
        tintBlendMode = image.tintBlendMode,
        autoMirror = image.autoMirror,
    ) { _, _ ->
        RenderVectorGroup(group = image.root, configs = configs)
    }
}
