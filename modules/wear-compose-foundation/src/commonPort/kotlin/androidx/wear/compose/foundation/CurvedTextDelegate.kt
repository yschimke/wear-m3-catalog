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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Replaces the `CurvedTextDelegate` upstream declares at the bottom of `BasicCurvedText.kt`, which
 * the patch on that file removes. Everything above it there — the public `basicCurvedText`,
 * `CurvedTextChild` and all the angular layout arithmetic — is upstream's and unchanged; this is
 * only the measuring and drawing underneath, and these members are the whole contract between them.
 *
 * It is `expect` rather than common because neither half can be written in common code: Compose
 * Multiplatform exposes no way to measure a font's ascent without laying out a paragraph, and no
 * way at all to place a glyph. Both are ordinary in Skia, which every target this library publishes
 * for renders through — so the one implementation lives in `skikoMain`.
 */
internal expect class CurvedTextDelegate() {
    var textWidth: Float
    var textHeight: Float
    var baseLinePosition: Float

    @Composable
    fun UpdateFontIfNeeded(
        fontFamily: FontFamily?,
        fontWeight: FontWeight?,
        fontStyle: FontStyle?,
        fontSynthesis: FontSynthesis?,
    )

    fun updateIfNeeded(
        text: String,
        clockwise: Boolean,
        fontSizePx: Float,
        letterSpacing: TextUnit,
        density: Float,
        lineHeightPx: Float,
        warpOffset: CurvedTextStyle.WarpOffset,
    )

    /** Zero here: the warping renderer this offset belongs to is not ported. */
    fun getMeasureOffset(): Float

    fun DrawScope.doDraw(
        layoutInfo: CurvedLayoutInfo,
        parentSweepRadians: Float,
        overflow: TextOverflow,
        color: Color,
        background: Color,
    )
}
