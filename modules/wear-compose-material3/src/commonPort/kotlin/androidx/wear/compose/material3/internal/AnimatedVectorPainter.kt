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
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes
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

/**
 * One `<objectAnimator>` on `pathData`: a shape morphing from [from] to [to].
 *
 * The two path strings are the same sequence of commands — AVD requires it, the exporter honours
 * it, and `tools/transform.py` refuses the drawable when it does not hold — so the morph is a lerp
 * of the control points, one for one. Parsed lazily and once: a path string is a few hundred
 * characters and every frame would otherwise re-parse both ends.
 */
internal class VectorPathSegment(
    val startOffsetMillis: Int,
    val durationMillis: Int,
    val from: String,
    val to: String,
    val easing: Easing = LinearEasing,
) {
    internal val fromNodes: List<PathNode> by lazy(LazyThreadSafetyMode.NONE) { addPathNodes(from) }
    internal val toNodes: List<PathNode> by lazy(LazyThreadSafetyMode.NONE) { addPathNodes(to) }
}

/** Everything animating one named element of the drawable. */
internal data class VectorAnimationTrack(
    val targetName: String,
    val segments: List<VectorAnimationSegment>,
    val pathSegments: List<VectorPathSegment> = emptyList(),
)

/** The last moment at which anything moves. */
internal val List<VectorAnimationTrack>.totalDurationMillis: Int
    get() =
        maxOfOrNull { track ->
            maxOf(
                track.segments.maxOfOrNull { it.startOffsetMillis + it.durationMillis } ?: 0,
                track.pathSegments.maxOfOrNull { it.startOffsetMillis + it.durationMillis } ?: 0,
            )
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

/**
 * The shape this track holds at [elapsedMillis], or null when it morphs nothing.
 *
 * Same windowing as the float segments: before a morph starts it is the `from` shape, after it ends
 * the `to` shape, and the interpolation runs only in between. Both endpoints are returned as the
 * PARSED node list rather than re-interpolated, so a drawable sitting at either end costs nothing.
 */
internal fun VectorAnimationTrack.pathAt(elapsedMillis: Float): List<PathNode>? {
    var value: List<PathNode>? = null
    for (segment in pathSegments) {
        val start = segment.startOffsetMillis.toFloat()
        val end = start + segment.durationMillis
        value =
            when {
                elapsedMillis <= start -> value ?: segment.fromNodes
                elapsedMillis >= end -> segment.toNodes
                else -> {
                    val fraction = segment.easing.transform((elapsedMillis - start) / (end - start))
                    lerpPathNodes(segment.fromNodes, segment.toNodes, fraction)
                }
            }
    }
    return value
}

/**
 * [from] and [to] interpolated control point by control point.
 *
 * A pair whose commands do not line up is not interpolable at all, and there is nothing sensible to
 * draw between two different shapes — so it snaps at the half-way point rather than emitting a
 * shape that is neither. The generator refuses such a pair up front, which is where the problem is
 * cheap to see; this is the runtime's half of the same rule.
 */
internal fun lerpPathNodes(
    from: List<PathNode>,
    to: List<PathNode>,
    fraction: Float,
): List<PathNode> {
    if (from.size != to.size) return if (fraction < 0.5f) from else to
    return List(from.size) { index ->
        val start = from[index]
        val end = to[index]
        lerpPathNode(start, end, fraction) ?: (if (fraction < 0.5f) start else end)
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction

/** One node, or null when the two are different commands and no interpolation is defined. */
private fun lerpPathNode(from: PathNode, to: PathNode, fraction: Float): PathNode? =
    when {
        from is PathNode.MoveTo && to is PathNode.MoveTo ->
            PathNode.MoveTo(lerp(from.x, to.x, fraction), lerp(from.y, to.y, fraction))
        from is PathNode.RelativeMoveTo && to is PathNode.RelativeMoveTo ->
            PathNode.RelativeMoveTo(lerp(from.dx, to.dx, fraction), lerp(from.dy, to.dy, fraction))
        from is PathNode.LineTo && to is PathNode.LineTo ->
            PathNode.LineTo(lerp(from.x, to.x, fraction), lerp(from.y, to.y, fraction))
        from is PathNode.RelativeLineTo && to is PathNode.RelativeLineTo ->
            PathNode.RelativeLineTo(lerp(from.dx, to.dx, fraction), lerp(from.dy, to.dy, fraction))
        from is PathNode.HorizontalTo && to is PathNode.HorizontalTo ->
            PathNode.HorizontalTo(lerp(from.x, to.x, fraction))
        from is PathNode.RelativeHorizontalTo && to is PathNode.RelativeHorizontalTo ->
            PathNode.RelativeHorizontalTo(lerp(from.dx, to.dx, fraction))
        from is PathNode.VerticalTo && to is PathNode.VerticalTo ->
            PathNode.VerticalTo(lerp(from.y, to.y, fraction))
        from is PathNode.RelativeVerticalTo && to is PathNode.RelativeVerticalTo ->
            PathNode.RelativeVerticalTo(lerp(from.dy, to.dy, fraction))
        from is PathNode.CurveTo && to is PathNode.CurveTo ->
            PathNode.CurveTo(
                lerp(from.x1, to.x1, fraction),
                lerp(from.y1, to.y1, fraction),
                lerp(from.x2, to.x2, fraction),
                lerp(from.y2, to.y2, fraction),
                lerp(from.x3, to.x3, fraction),
                lerp(from.y3, to.y3, fraction),
            )
        from is PathNode.RelativeCurveTo && to is PathNode.RelativeCurveTo ->
            PathNode.RelativeCurveTo(
                lerp(from.dx1, to.dx1, fraction),
                lerp(from.dy1, to.dy1, fraction),
                lerp(from.dx2, to.dx2, fraction),
                lerp(from.dy2, to.dy2, fraction),
                lerp(from.dx3, to.dx3, fraction),
                lerp(from.dy3, to.dy3, fraction),
            )
        from is PathNode.ReflectiveCurveTo && to is PathNode.ReflectiveCurveTo ->
            PathNode.ReflectiveCurveTo(
                lerp(from.x1, to.x1, fraction),
                lerp(from.y1, to.y1, fraction),
                lerp(from.x2, to.x2, fraction),
                lerp(from.y2, to.y2, fraction),
            )
        from is PathNode.RelativeReflectiveCurveTo && to is PathNode.RelativeReflectiveCurveTo ->
            PathNode.RelativeReflectiveCurveTo(
                lerp(from.dx1, to.dx1, fraction),
                lerp(from.dy1, to.dy1, fraction),
                lerp(from.dx2, to.dx2, fraction),
                lerp(from.dy2, to.dy2, fraction),
            )
        from is PathNode.QuadTo && to is PathNode.QuadTo ->
            PathNode.QuadTo(
                lerp(from.x1, to.x1, fraction),
                lerp(from.y1, to.y1, fraction),
                lerp(from.x2, to.x2, fraction),
                lerp(from.y2, to.y2, fraction),
            )
        from is PathNode.RelativeQuadTo && to is PathNode.RelativeQuadTo ->
            PathNode.RelativeQuadTo(
                lerp(from.dx1, to.dx1, fraction),
                lerp(from.dy1, to.dy1, fraction),
                lerp(from.dx2, to.dx2, fraction),
                lerp(from.dy2, to.dy2, fraction),
            )
        from is PathNode.ReflectiveQuadTo && to is PathNode.ReflectiveQuadTo ->
            PathNode.ReflectiveQuadTo(
                lerp(from.x, to.x, fraction),
                lerp(from.y, to.y, fraction),
            )
        from is PathNode.RelativeReflectiveQuadTo && to is PathNode.RelativeReflectiveQuadTo ->
            PathNode.RelativeReflectiveQuadTo(
                lerp(from.dx, to.dx, fraction),
                lerp(from.dy, to.dy, fraction),
            )
        from is PathNode.ArcTo && to is PathNode.ArcTo ->
            PathNode.ArcTo(
                lerp(from.horizontalEllipseRadius, to.horizontalEllipseRadius, fraction),
                lerp(from.verticalEllipseRadius, to.verticalEllipseRadius, fraction),
                lerp(from.theta, to.theta, fraction),
                // Flags are not numbers to interpolate: an arc is large or it is not. The start
                // value holds until the end of the segment, which is what Android does too.
                from.isMoreThanHalf,
                from.isPositiveArc,
                lerp(from.arcStartX, to.arcStartX, fraction),
                lerp(from.arcStartY, to.arcStartY, fraction),
            )
        from is PathNode.RelativeArcTo && to is PathNode.RelativeArcTo ->
            PathNode.RelativeArcTo(
                lerp(from.horizontalEllipseRadius, to.horizontalEllipseRadius, fraction),
                lerp(from.verticalEllipseRadius, to.verticalEllipseRadius, fraction),
                lerp(from.theta, to.theta, fraction),
                from.isMoreThanHalf,
                from.isPositiveArc,
                lerp(from.arcStartDx, to.arcStartDx, fraction),
                lerp(from.arcStartDy, to.arcStartDy, fraction),
            )
        from is PathNode.Close && to is PathNode.Close -> PathNode.Close
        else -> null
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
                is VectorProperty.PathData ->
                    return (track.pathAt(elapsedMillis.value) as? T) ?: defaultValue
                // Fill and Stroke animate a type the generator never emits; the drawable keeps
                // whatever the artwork declares.
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
