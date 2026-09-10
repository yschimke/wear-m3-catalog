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

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Path
import org.jetbrains.skia.PathVerb
import org.jetbrains.skia.Path as SkiaPath

/**
 * Bends a straight run of glyph outlines around a circle.
 *
 * This is upstream's `CircleWarper`, arithmetic for arithmetic. It maps the rectangle a run of text
 * occupies onto an annulus segment: a point's **x** becomes an angle (arc length over radius) and
 * its **y** becomes a distance from the centre. Adjacent glyphs therefore share the line between
 * them, which is what makes the result right for a cursive font, where rotating each glyph as a
 * rigid stamp visibly breaks the joins.
 *
 * The port's other renderer — `CurvedTextDelegate`, using `RSXform` — is the rigid one, and it is
 * what upstream itself falls back to below API 34. This is the higher-fidelity path.
 */
internal class CircleWarper(
    private val centerX: Float,
    private val centerY: Float,
    private val radius: Float,
    private val angle: Float,
    clockwise: Boolean,
) {
    private val sign = if (clockwise) 1f else -1f

    /** Where a point of the straight run lands on the circle. */
    fun warpX(x: Float, y: Float): Float = angleCos(x) * radiusAt(y) + centerX

    fun warpY(x: Float, y: Float): Float = angleSin(x) * radiusAt(y) + centerY

    private fun angleOf(x: Float) = x * sign / radius + angle

    private fun angleCos(x: Float) = cos(angleOf(x))

    private fun angleSin(x: Float) = sin(angleOf(x))

    private fun radiusAt(y: Float) = radius - y * sign

    /**
     * Warps one cubic, in place, as eight floats: `x0 y0 x1 y1 x2 y2 x3 y3`.
     *
     * Warping is not linear, so a cubic's control points cannot simply be moved — the curve between
     * them would no longer follow the arc. Upstream's answer, reproduced here: when the cubic is
     * effectively a straight segment (its middle control points coincide with an end), every point
     * can be warped directly; otherwise each END point is warped and its neighbouring control point
     * is derived from the tangent at that end, scaled by how far the point sits from the baseline.
     * That keeps the curve tangent-continuous across the join instead of kinking at every control
     * point.
     */
    fun warpCubic(c: FloatArray) {
        if (pointEqualish(c, 2, 4) && (pointEqualish(c, 0, 2) || pointEqualish(c, 4, 6))) {
            for (i in 0 until 4) {
                val x = c[i * 2]
                val y = c[i * 2 + 1]
                c[i * 2] = warpX(x, y)
                c[i * 2 + 1] = warpY(x, y)
            }
        } else {
            warpPair(c, 0, 2)
            warpPair(c, 6, 4)
        }
    }

    /** Warps the end point at [i] and derives the control point at [j] from the tangent there. */
    private fun warpPair(c: FloatArray, i: Int, j: Int) {
        val px = c[i]
        val py = c[i + 1]
        val tx = c[j] - px
        val ty = c[j + 1] - py

        val r0 = radiusAt(py)
        val vx = angleCos(px)
        val vy = angleSin(px)

        val outX = vx * r0 + centerX
        val outY = vy * r0 + centerY
        c[i] = outX
        c[i + 1] = outY

        // The tangent, rotated into the frame of the arc: along the arc it is scaled by how far
        // this point is from the baseline circle, across it, it is not.
        val scale = r0 / radius
        val alongX = -vy * scale * (tx * sign)
        val alongY = vx * scale * (tx * sign)
        val acrossX = vx * (ty * sign)
        val acrossY = vy * (ty * sign)
        c[j] = outX + alongX - acrossX
        c[j + 1] = outY + alongY - acrossY
    }

    private fun pointEqualish(c: FloatArray, i: Int, j: Int) =
        abs(c[i] - c[j]) < DistanceEpsilon && abs(c[i + 1] - c[j + 1]) < DistanceEpsilon

    private companion object {
        const val DistanceEpsilon = 1e-4f
    }
}

/**
 * [glyph]'s outline, offset along the run by [offsetX] and warped around the arc.
 *
 * Every segment becomes a cubic before warping, including straight lines: a line that is straight
 * in the run is an arc once bent, so keeping it a line would cut the corner. Skia's verbs are the
 * same set Android's `PathIterator` produces — upstream throws on CONIC and so does this, because a
 * glyph outline from a TrueType or OpenType font contains none: quadratics and cubics only.
 */
internal fun appendWarpedGlyph(
    target: Path,
    glyph: SkiaPath,
    offsetX: Float,
    baselineOffset: Float,
    warper: CircleWarper,
) {
    val cubic = FloatArray(8)
    for (segment in glyph) {
        if (segment == null) continue
        when (segment.verb) {
            PathVerb.MOVE -> {
                val p = segment.p0!!
                val x = p.x + offsetX
                val y = p.y + baselineOffset
                target.moveTo(warper.warpX(x, y), warper.warpY(x, y))
            }
            PathVerb.LINE -> {
                val a = segment.p0!!
                val b = segment.p1!!
                lerpInto(
                    cubic,
                    a.x + offsetX,
                    a.y + baselineOffset,
                    b.x + offsetX,
                    b.y + baselineOffset,
                )
                emit(target, cubic, warper)
            }
            PathVerb.QUAD -> {
                val a = segment.p0!!
                val q = segment.p1!!
                val b = segment.p2!!
                // A quadratic raised to a cubic: the two control points sit a third and two thirds
                // of the way from each end towards the quadratic's single control point.
                cubic[0] = a.x + offsetX
                cubic[1] = a.y + baselineOffset
                cubic[2] = a.x + offsetX + 2f / 3f * (q.x - a.x)
                cubic[3] = a.y + baselineOffset + 2f / 3f * (q.y - a.y)
                cubic[4] = b.x + offsetX + 2f / 3f * (q.x - b.x)
                cubic[5] = b.y + baselineOffset + 2f / 3f * (q.y - b.y)
                cubic[6] = b.x + offsetX
                cubic[7] = b.y + baselineOffset
                emit(target, cubic, warper)
            }
            PathVerb.CUBIC -> {
                val a = segment.p0!!
                val c1 = segment.p1!!
                val c2 = segment.p2!!
                val b = segment.p3!!
                cubic[0] = a.x + offsetX
                cubic[1] = a.y + baselineOffset
                cubic[2] = c1.x + offsetX
                cubic[3] = c1.y + baselineOffset
                cubic[4] = c2.x + offsetX
                cubic[5] = c2.y + baselineOffset
                cubic[6] = b.x + offsetX
                cubic[7] = b.y + baselineOffset
                emit(target, cubic, warper)
            }
            PathVerb.CLOSE -> target.close()
            PathVerb.CONIC ->
                throw UnsupportedOperationException(
                    "a conic in a glyph outline: fonts do not produce these, so something has " +
                        "transformed the path before it got here"
                )
            PathVerb.DONE -> return
        }
    }
}

/** A straight segment as a cubic, its control points evenly spaced along it. */
private fun lerpInto(c: FloatArray, x0: Float, y0: Float, x1: Float, y1: Float) {
    for (i in 0 until 4) {
        val t = i / 3f
        c[i * 2] = x0 * (1 - t) + x1 * t
        c[i * 2 + 1] = y0 * (1 - t) + y1 * t
    }
}

private fun emit(target: Path, cubic: FloatArray, warper: CircleWarper) {
    warper.warpCubic(cubic)
    target.cubicTo(cubic[2], cubic[3], cubic[4], cubic[5], cubic[6], cubic[7])
}
