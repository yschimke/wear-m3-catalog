/*
 * Copyright (C) 2024 The Android Open Source Project
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

package androidx.compose.remote.creation.compose.modifier

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.capture.RemoteDensityBehavior
import androidx.compose.remote.creation.compose.state.RemoteDp
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.common.RemoteModifierOperation

internal class WidthInModifier(val min: RemoteDp? = null, val max: RemoteDp? = null) :
    RemoteModifier.Element {
    override fun RemoteStateScope.toRemoteModifierOperation(): RemoteModifierOperation {
        val isPixels = densityBehavior == RemoteDensityBehavior.Pixels
        val minValue = min?.let { if (isPixels) it.toPx().floatId else it.value.floatId } ?: 0f
        val maxValue =
            max?.let { if (isPixels) it.toPx().floatId else it.value.floatId } ?: Float.MAX_VALUE
        return RemoteModifierOperation.WidthIn(minValue, maxValue)
    }
}

/**
 * Sets the minimum and maximum width of the content.
 *
 * @param min The minimum width.
 * @param max The maximum width.
 */
public fun RemoteModifier.widthIn(min: RemoteDp? = null, max: RemoteDp? = null): RemoteModifier {
    return then(WidthInModifier(min = min, max = max))
}
