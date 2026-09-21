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

internal expect object RemoteTypographyTokens {
    val DisplayLarge: RemoteTextStyle
    val DisplayMedium: RemoteTextStyle
    val DisplaySmall: RemoteTextStyle

    val TitleLarge: RemoteTextStyle
    val TitleMedium: RemoteTextStyle
    val TitleSmall: RemoteTextStyle

    val LabelLarge: RemoteTextStyle
    val LabelMedium: RemoteTextStyle
    val LabelSmall: RemoteTextStyle

    val BodyLarge: RemoteTextStyle
    val BodyMedium: RemoteTextStyle
    val BodySmall: RemoteTextStyle
    val BodyExtraSmall: RemoteTextStyle

    val NumeralExtraLarge: RemoteTextStyle
    val NumeralLarge: RemoteTextStyle
    val NumeralMedium: RemoteTextStyle
    val NumeralSmall: RemoteTextStyle
    val NumeralExtraSmall: RemoteTextStyle
}
