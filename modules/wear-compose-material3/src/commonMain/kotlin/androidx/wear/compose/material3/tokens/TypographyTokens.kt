// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.
// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.
/*
 * Copyright 2024 The Android Open Source Project
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

// VERSION: v0_108 + manual changes
package androidx.wear.compose.material3.tokens

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material3.DefaultTextStyle

/**
 * ********************************************************
 * Modified by hand, don't override!!!
 * *********************************************************
 */
internal object TypographyTokens {
    internal val DefaultCurvedTextStyle = CurvedTextStyle(DefaultTextStyle)
    val ArcLarge =
        DefaultCurvedTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.ArcLargeWeight.toInt()),
            fontSize = TypeScaleTokens.ArcLargeSize,
            lineHeight = TypeScaleTokens.ArcLargeLineHeight,
            letterSpacing = TypeScaleTokens.ArcLargeTrackingTop,
            letterSpacingCounterClockwise = TypeScaleTokens.ArcLargeTrackingBottom,
        )
    val ArcMedium =
        DefaultCurvedTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.ArcMediumWeight.toInt()),
            fontSize = TypeScaleTokens.ArcMediumSize,
            lineHeight = TypeScaleTokens.ArcMediumLineHeight,
            letterSpacing = TypeScaleTokens.ArcMediumTrackingTop,
            letterSpacingCounterClockwise = TypeScaleTokens.ArcMediumTrackingBottom,
        )
    val ArcSmall =
        DefaultCurvedTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.ArcSmallWeight.toInt()),
            fontSize = TypeScaleTokens.ArcSmallSize,
            lineHeight = TypeScaleTokens.ArcSmallLineHeight,
            letterSpacing = TypeScaleTokens.ArcSmallTrackingTop,
            letterSpacingCounterClockwise = TypeScaleTokens.ArcSmallTrackingBottom,
        )
    val BodyExtraSmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.BodyExtraSmallWeight.toInt()),
            fontSize = TypeScaleTokens.BodyExtraSmallSize,
            lineHeight = TypeScaleTokens.BodyExtraSmallLineHeight,
            letterSpacing = TypeScaleTokens.BodyExtraSmallTracking,
        )
    val BodyLarge =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.BodyLargeWeight.toInt()),
            fontSize = TypeScaleTokens.BodyLargeSize,
            lineHeight = TypeScaleTokens.BodyLargeLineHeight,
            letterSpacing = TypeScaleTokens.BodyLargeTracking,
        )
    val BodyMedium =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.BodyMediumWeight.toInt()),
            fontSize = TypeScaleTokens.BodyMediumSize,
            lineHeight = TypeScaleTokens.BodyMediumLineHeight,
            letterSpacing = TypeScaleTokens.BodyMediumTracking,
        )
    val BodySmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.BodySmallWeight.toInt()),
            fontSize = TypeScaleTokens.BodySmallSize,
            lineHeight = TypeScaleTokens.BodySmallLineHeight,
            letterSpacing = TypeScaleTokens.BodySmallTracking,
        )
    val DisplayLarge =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.DisplayLargeWeight.toInt()),
            fontSize = TypeScaleTokens.DisplayLargeSize,
            lineHeight = TypeScaleTokens.DisplayLargeLineHeight,
            letterSpacing = TypeScaleTokens.DisplayLargeTracking,
        )
    val DisplayMedium =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.DisplayMediumWeight.toInt()),
            fontSize = TypeScaleTokens.DisplayMediumSize,
            lineHeight = TypeScaleTokens.DisplayMediumLineHeight,
            letterSpacing = TypeScaleTokens.DisplayMediumTracking,
        )
    val DisplaySmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.DisplaySmallWeight.toInt()),
            fontSize = TypeScaleTokens.DisplaySmallSize,
            lineHeight = TypeScaleTokens.DisplaySmallLineHeight,
            letterSpacing = TypeScaleTokens.DisplaySmallTracking,
        )
    val LabelLarge =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.LabelLargeWeight.toInt()),
            fontSize = TypeScaleTokens.LabelLargeSize,
            lineHeight = TypeScaleTokens.LabelLargeLineHeight,
            letterSpacing = TypeScaleTokens.LabelLargeTracking,
        )
    val LabelMedium =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.LabelMediumWeight.toInt()),
            fontSize = TypeScaleTokens.LabelMediumSize,
            lineHeight = TypeScaleTokens.LabelMediumLineHeight,
            letterSpacing = TypeScaleTokens.LabelMediumTracking,
        )
    val LabelSmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.LabelSmallWeight.toInt()),
            fontSize = TypeScaleTokens.LabelSmallSize,
            lineHeight = TypeScaleTokens.LabelSmallLineHeight,
            letterSpacing = TypeScaleTokens.LabelSmallTracking,
        )
    val NumeralExtraLarge =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.NumeralExtraLargeWeight.toInt()),
            fontSize = TypeScaleTokens.NumeralExtraLargeSize,
            lineHeight = TypeScaleTokens.NumeralExtraLargeLineHeight,
            letterSpacing = TypeScaleTokens.NumeralExtraLargeTracking,
        )
    val NumeralExtraSmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.NumeralExtraSmallWeight.toInt()),
            fontSize = TypeScaleTokens.NumeralExtraSmallSize,
            lineHeight = TypeScaleTokens.NumeralExtraSmallLineHeight,
            letterSpacing = TypeScaleTokens.NumeralExtraSmallTracking,
        )
    val NumeralLarge =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.NumeralLargeWeight.toInt()),
            fontSize = TypeScaleTokens.NumeralLargeSize,
            lineHeight = TypeScaleTokens.NumeralLargeLineHeight,
            letterSpacing = TypeScaleTokens.NumeralLargeTracking,
        )
    val NumeralMedium =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.NumeralMediumWeight.toInt()),
            fontSize = TypeScaleTokens.NumeralMediumSize,
            lineHeight = TypeScaleTokens.NumeralMediumLineHeight,
            letterSpacing = TypeScaleTokens.NumeralMediumTracking,
        )
    val NumeralSmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.NumeralSmallWeight.toInt()),
            fontSize = TypeScaleTokens.NumeralSmallSize,
            lineHeight = TypeScaleTokens.NumeralSmallLineHeight,
            letterSpacing = TypeScaleTokens.NumeralSmallTracking,
        )
    val TitleLarge =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.TitleLargeWeight.toInt()),
            fontSize = TypeScaleTokens.TitleLargeSize,
            lineHeight = TypeScaleTokens.TitleLargeLineHeight,
            letterSpacing = TypeScaleTokens.TitleLargeTracking,
        )
    val TitleMedium =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.TitleMediumWeight.toInt()),
            fontSize = TypeScaleTokens.TitleMediumSize,
            lineHeight = TypeScaleTokens.TitleMediumLineHeight,
            letterSpacing = TypeScaleTokens.TitleMediumTracking,
        )
    val TitleSmall =
        DefaultTextStyle.copy(
            fontFamily =
                FontFamily.SansSerif,
            fontWeight = FontWeight(TypeScaleTokens.TitleSmallWeight.toInt()),
            fontSize = TypeScaleTokens.TitleSmallSize,
            lineHeight = TypeScaleTokens.TitleSmallLineHeight,
            letterSpacing = TypeScaleTokens.TitleSmallTracking,
        )
}
