// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.
// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.
/*
 * Copyright 2021 The Android Open Source Project
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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * [basicCurvedText] is a component allowing developers to easily write curved text following the
 * curvature a circle (usually at the edge of a circular screen). [basicCurvedText] can be only
 * created within a [CurvedLayout] since it's not a composable.
 *
 * @sample androidx.wear.compose.foundation.samples.CurvedAndNormalText
 * @param text The text to display
 * @param modifier The [CurvedModifier] to apply to this curved text.
 * @param angularDirection Specify if the text is laid out clockwise or anti-clockwise, and if those
 *   needs to be reversed in a Rtl layout. If not specified, it will be inherited from the enclosing
 *   [curvedRow] or [CurvedLayout] See [CurvedDirection.Angular].
 * @param overflow How visual overflow should be handled. Note that this takes into account only
 *   explicit size curved modifiers in this element, to size this element matching the parent's, add
 *   a CurvedModifier.weight here.
 * @param style A @Composable factory to provide the style to use. This composable SHOULDN'T
 *   generate any compose nodes.
 */
public fun CurvedScope.basicCurvedText(
    text: String,
    modifier: CurvedModifier = CurvedModifier,
    angularDirection: CurvedDirection.Angular? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    style: @Composable () -> CurvedTextStyle = { CurvedTextStyle() },
): Unit =
    add(
        CurvedTextChild(
            text,
            curvedLayoutDirection.copy(overrideAngular = angularDirection).absoluteClockwise(),
            style,
            overflow,
        ),
        modifier,
    )

/**
 * [basicCurvedText] is a component allowing developers to easily write curved text following the
 * curvature a circle (usually at the edge of a circular screen). [basicCurvedText] can be only
 * created within a [CurvedLayout] since it's not a composable.
 *
 * @sample androidx.wear.compose.foundation.samples.CurvedAndNormalText
 * @param text The text to display
 * @param style A style to use.
 * @param modifier The [CurvedModifier] to apply to this curved text.
 * @param angularDirection Specify if the text is laid out clockwise or anti-clockwise, and if those
 *   needs to be reversed in a Rtl layout. If not specified, it will be inherited from the enclosing
 *   [curvedRow] or [CurvedLayout] See [CurvedDirection.Angular].
 * @param overflow How visual overflow should be handled.
 */
public fun CurvedScope.basicCurvedText(
    text: String,
    style: CurvedTextStyle,
    modifier: CurvedModifier = CurvedModifier,
    angularDirection: CurvedDirection.Angular? = null,
    overflow: TextOverflow = TextOverflow.Clip,
): Unit = basicCurvedText(text, modifier, angularDirection, overflow) { style }

internal class CurvedTextChild(
    val text: String,
    val clockwise: Boolean = true,
    val style: @Composable () -> CurvedTextStyle = { CurvedTextStyle() },
    val overflow: TextOverflow,
) : CurvedChild() {
    private lateinit var delegate: CurvedTextDelegate
    private lateinit var actualStyle: CurvedTextStyle

    // We create a compose-ui node so that we can attach a11y info.
    private lateinit var placeable: Placeable

    @Composable
    override fun SubComposition(semanticProperties: CurvedSemanticProperties) {
        actualStyle = DefaultCurvedTextStyles + style()

        // Avoid recreating the delegate if possible, as it's expensive
        delegate = remember { CurvedTextDelegate() }
        delegate.UpdateFontIfNeeded(
            actualStyle.fontFamily,
            actualStyle.fontWeight,
            actualStyle.fontStyle,
            actualStyle.fontSynthesis,
        )

        val mergedSemantics =
            semanticProperties.merge(CurvedSemanticProperties(contentDescription = text))

        // Empty compose-ui node to attach a11y info.
        Box(Modifier.semantics { with(mergedSemantics) { applySemantics() } })
    }

    override fun CurvedMeasureScope.initializeMeasure(
        measurables: Iterator<Measurable>
    ): (Placeable.PlacementScope).() -> Unit {
        if (isLookingAhead) {
            // TODO(b/486792667): Investigate properly supporting Lookahead animations.
            val lookaheadPlaceable = measurables.next().measure(Constraints())
            return { lookaheadPlaceable.place(0, 0) }
        }
        delegate.updateIfNeeded(
            text,
            clockwise,
            actualStyle.fontSize.toPx(),
            if (clockwise || actualStyle.letterSpacingCounterClockwise.isUnspecified)
                actualStyle.letterSpacing
            else actualStyle.letterSpacingCounterClockwise,
            density,
            if (actualStyle.lineHeight.isSpecified) actualStyle.lineHeight.toPx() else -1f,
            actualStyle.warpOffset,
        )

        // Size the compose-ui node reasonably.

        // Heuristic calculations of maxWidth:
        // 1. Find the text's center point. This is offset from the circle's center by a distance
        //    equal to:
        //    radius - (textHeight / 2)
        // 2. Construct a perpendicular line from the text's center point, extending it until it
        //    intersects the circle's circumference.
        // 3. Using the Pythagorean theorem, we can determine half of the desired width. We know:
        //    * The circle's radius
        //    * The distance of the text from the circle's center (calculated in step 1)
        // 4. The Pythagorean equation in this context is:
        //    radius^2 = (radius - textHeight/2)^2 + (maxWidth/2)^2
        // 5. Solving for maxWidth, we get:
        //    maxWidth = 2 * sqrt( textHeight * (radius - textHeight/4) )

        val height = delegate.textHeight.roundToInt()
        val maxWidth = 2 * sqrt(height * (radius - height / 4))
        val width = delegate.textWidth.coerceAtMost(maxWidth).roundToInt()

        // Measure the corresponding measurable.
        placeable =
            measurables
                .next()
                .measure(
                    Constraints(
                        minWidth = width,
                        maxWidth = width,
                        minHeight = height,
                        maxHeight = height,
                    )
                )
        return {
            // clockwise doesn't matter, we have no content in placeable.
            place(placeable, layoutInfo!!, parentSweepRadians, clockwise = false)
        }
    }

    override fun doEstimateThickness(maxRadius: Float): Float = delegate.textHeight

    override fun doRadialPosition(
        parentOuterRadius: Float,
        parentThickness: Float,
    ): PartialLayoutInfo {
        val baselineRadius = parentOuterRadius - delegate.baseLinePosition
        // getMeasureOffset is 0 when there is no warping, and the warping offset when there is
        // this defines the horizontal line at which the text will maintain its width (lines closer
        // to the center will be shrunk and lines further away will be stretched).
        val realMeasureRadius =
            baselineRadius + delegate.getMeasureOffset() * (if (clockwise) 1f else -1f)
        return PartialLayoutInfo(
            delegate.textWidth / realMeasureRadius,
            parentOuterRadius,
            delegate.textHeight,
            baselineRadius,
        )
    }

    private var parentSweepRadians: Float = 0f

    override fun doAngularPosition(
        parentStartAngleRadians: Float,
        parentSweepRadians: Float,
        centerOffset: Offset,
    ): Float {
        this.parentSweepRadians = parentSweepRadians
        return super.doAngularPosition(parentStartAngleRadians, parentSweepRadians, centerOffset)
    }

    override fun DrawScope.draw() {
        with(delegate) {
            doDraw(
                layoutInfo!!,
                parentSweepRadians,
                overflow,
                actualStyle.color,
                actualStyle.background,
            )
        }
    }
}

/*
 * `CurvedTextDelegate` and `CurvedTextRenderer` ended here, and are replaced by
 * `src/commonPort/.../CurvedTextDelegate.kt`. Everything above — the public `basicCurvedText`, the
 * `CurvedTextChild` and all of its angular layout arithmetic — is upstream's and unchanged: it is
 * platform-free, and it is the reason this is a patch on the tail of the file rather than a
 * replacement of the whole thing.
 *
 * What ended here was 330 lines of `android.graphics`: a `TextPaint` for measuring, a `Path` for
 * the background arc, `StaticLayout`/`TextUtils` for ellipsizing, and a renderer that either drew
 * the run along a path or warped its glyph outlines around one.
 */
