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

package androidx.wear.compose.remote.material3

import androidx.compose.remote.creation.compose.state.asRemoteTextUnit
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.remote.creation.compose.text.RemoteTextStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The Wear type scale expressed directly in Remote Compose types.
 *
 * Kotlin/Wasm production optimization currently miscompiles Wear Material 3's 21-argument
 * [androidx.wear.compose.material3.Typography] default constructor. Remote Material only needs the
 * resulting scalar token values, so constructing that intermediary on Wasm is both unnecessary and
 * unsafe. JVM and Android retain the upstream conversion path.
 */
internal actual object RemoteTypographyTokens {
    actual val DisplayLarge =
        style(size = 40, lineHeight = 44, tracking = 0.2f, weight = 500, width = 110)
    actual val DisplayMedium =
        style(size = 30, lineHeight = 34, tracking = 0.2f, weight = 520, width = 110)
    actual val DisplaySmall =
        style(size = 24, lineHeight = 26, tracking = 0.2f, weight = 550, width = 110)

    actual val TitleLarge =
        style(size = 18, lineHeight = 20, tracking = 0.2f, weight = 500, width = 110)
    actual val TitleMedium =
        style(size = 16, lineHeight = 18, tracking = 0.4f, weight = 550, width = 110)
    actual val TitleSmall =
        style(size = 14, lineHeight = 16, tracking = 0.4f, weight = 550, width = 110)

    actual val LabelLarge =
        style(size = 20, lineHeight = 22, tracking = 0.4f, weight = 500, width = 110)
    actual val LabelMedium =
        style(size = 15, lineHeight = 18, tracking = 0.4f, weight = 500, width = 110)
    actual val LabelSmall =
        style(size = 13, lineHeight = 16, tracking = 0.4f, weight = 500, width = 110)

    actual val BodyLarge =
        style(size = 16, lineHeight = 18, tracking = 0.4f, weight = 450, width = 110)
    actual val BodyMedium =
        style(size = 14, lineHeight = 16, tracking = 0.4f, weight = 450, width = 110)
    actual val BodySmall =
        style(size = 12, lineHeight = 14, tracking = 0.4f, weight = 500, width = 110)
    actual val BodyExtraSmall =
        style(size = 10, lineHeight = 12, tracking = 0.2f, weight = 500, width = 104)

    actual val NumeralExtraLarge =
        style(size = 60, lineHeight = 60, tracking = 0f, weight = 560, width = 110)
    actual val NumeralLarge =
        style(size = 50, lineHeight = 50, tracking = 0f, weight = 580, width = 110)
    actual val NumeralMedium =
        style(size = 40, lineHeight = 40, tracking = 0f, weight = 580, width = 100)
    actual val NumeralSmall =
        style(size = 30, lineHeight = 30, tracking = 0f, weight = 550, width = 100)
    actual val NumeralExtraSmall =
        style(size = 24, lineHeight = 24, tracking = 0f, weight = 550, width = 100)

    private fun style(
        size: Int,
        lineHeight: Int,
        tracking: Float,
        weight: Int,
        width: Int,
    ) =
        RemoteTextStyle(
            fontSize = size.rsp,
            fontWeight = FontWeight(weight),
            letterSpacing = tracking.sp.asRemoteTextUnit(),
            lineHeight = lineHeight.rsp,
            fontFeatureSettings = "pnum",
            fontVariationSettings =
                FontVariation.Settings(
                    FontVariation.Setting("wdth", width.toFloat()),
                    FontVariation.Setting("wght", weight.toFloat()),
                ),
        )
}
