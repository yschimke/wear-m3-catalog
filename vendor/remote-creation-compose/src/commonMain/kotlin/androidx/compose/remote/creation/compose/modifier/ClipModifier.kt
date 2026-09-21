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
import androidx.compose.remote.creation.compose.layout.RemoteFloatContext
import androidx.compose.remote.creation.compose.layout.RemoteSize
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRectangleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.shapes.RemoteShape
import androidx.compose.remote.creation.compose.shapes.toDimension
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.min
import androidx.compose.remote.creation.common.RemoteModifierOperation
import androidx.compose.ui.unit.LayoutDirection

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ClipModifier(public val shape: RemoteShape = RemoteRectangleShape) :
    RemoteModifier.Element {
    override fun RemoteStateScope.toRemoteModifierOperation(): RemoteModifierOperation {
        if (shape == RemoteRectangleShape) return RemoteModifierOperation.ClipRect
        val context = RemoteFloatContext(this)
        val remoteSize = RemoteSize(context.componentWidth(), context.componentHeight())
        if (shape == RemoteCircleShape) {
            val radius =
                if (densityBehavior == RemoteDensityBehavior.Dp) {
                    min(remoteSize.width, remoteSize.height) / remoteDensity.density / 2f
                } else {
                    min(remoteSize.width, remoteSize.height) / 2f
                }
            return RemoteModifierOperation.RoundedClipRect(
                radius.floatId,
                radius.floatId,
                radius.floatId,
                radius.floatId,
            )
        }
        if (shape is RemoteRoundedCornerShape) {
            val isRtl = layoutDirection == LayoutDirection.Rtl
            return RemoteModifierOperation.RoundedClipRect(
                (if (isRtl) shape.topEnd else shape.topStart)
                    .toDimension(remoteSize, remoteDensity, densityBehavior)
                    .floatId,
                (if (isRtl) shape.topStart else shape.topEnd)
                    .toDimension(remoteSize, remoteDensity, densityBehavior)
                    .floatId,
                (if (isRtl) shape.bottomEnd else shape.bottomStart)
                    .toDimension(remoteSize, remoteDensity, densityBehavior)
                    .floatId,
                (if (isRtl) shape.bottomStart else shape.bottomEnd)
                    .toDimension(remoteSize, remoteDensity, densityBehavior)
                    .floatId,
            )
        }
        return RemoteModifierOperation.ClipRect
    }
}

public fun RemoteModifier.clip(shape: RemoteShape = RemoteRectangleShape): RemoteModifier =
    then(ClipModifier(shape))
