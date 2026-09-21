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

import androidx.compose.remote.creation.compose.text.RemoteTextStyle
import androidx.wear.compose.material3.Typography

internal actual object RemoteTypographyTokens {
    private val defaultTypography = Typography()
    actual val DisplayLarge = RemoteTextStyle.fromTextStyle(defaultTypography.displayLarge)
    actual val DisplayMedium = RemoteTextStyle.fromTextStyle(defaultTypography.displayMedium)
    actual val DisplaySmall = RemoteTextStyle.fromTextStyle(defaultTypography.displaySmall)

    actual val TitleLarge = RemoteTextStyle.fromTextStyle(defaultTypography.titleLarge)
    actual val TitleMedium = RemoteTextStyle.fromTextStyle(defaultTypography.titleMedium)
    actual val TitleSmall = RemoteTextStyle.fromTextStyle(defaultTypography.titleSmall)

    actual val LabelLarge = RemoteTextStyle.fromTextStyle(defaultTypography.labelLarge)
    actual val LabelMedium = RemoteTextStyle.fromTextStyle(defaultTypography.labelMedium)
    actual val LabelSmall = RemoteTextStyle.fromTextStyle(defaultTypography.labelSmall)

    actual val BodyLarge = RemoteTextStyle.fromTextStyle(defaultTypography.bodyLarge)
    actual val BodyMedium = RemoteTextStyle.fromTextStyle(defaultTypography.bodyMedium)
    actual val BodySmall = RemoteTextStyle.fromTextStyle(defaultTypography.bodySmall)
    actual val BodyExtraSmall = RemoteTextStyle.fromTextStyle(defaultTypography.bodyExtraSmall)

    actual val NumeralExtraLarge =
        RemoteTextStyle.fromTextStyle(defaultTypography.numeralExtraLarge)
    actual val NumeralLarge = RemoteTextStyle.fromTextStyle(defaultTypography.numeralLarge)
    actual val NumeralMedium = RemoteTextStyle.fromTextStyle(defaultTypography.numeralMedium)
    actual val NumeralSmall = RemoteTextStyle.fromTextStyle(defaultTypography.numeralSmall)
    actual val NumeralExtraSmall =
        RemoteTextStyle.fromTextStyle(defaultTypography.numeralExtraSmall)
}
