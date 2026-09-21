/*
 * Copyright 2025 The Android Open Source Project
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
@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package androidx.compose.remote.creation.compose.layout

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.common.AnimatedFloatExpression
import androidx.compose.remote.creation.compose.capture.RemoteComposeCreationState
import androidx.compose.remote.creation.compose.state.RemoteComponentCacheKey
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteFloatExpression
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.creationState

public class RemoteFloatContext internal constructor(internal val state: RemoteStateScope) {
    public fun componentWidth(): RemoteFloat {
        val componentKey = (state.creationState as RemoteComposeCreationState).componentCacheKey
        return RemoteFloatExpression(
            constantValueOrNull = null,
            cacheKey = RemoteComponentCacheKey(componentKey, "width"),
            arrayProvider = { creationState ->
                floatArrayOf(creationState.writer.addComponentWidthValue(componentKey))
            },
        )
    }

    public fun componentHeight(): RemoteFloat {
        val componentKey = (state.creationState as RemoteComposeCreationState).componentCacheKey
        return RemoteFloatExpression(
            constantValueOrNull = null,
            cacheKey = RemoteComponentCacheKey(componentKey, "height"),
            arrayProvider = { creationState ->
                floatArrayOf(creationState.writer.addComponentHeightValue(componentKey))
            },
        )
    }

    public fun componentCenterX(): RemoteFloat {
        val componentKey = (state.creationState as RemoteComposeCreationState).componentCacheKey
        return RemoteFloatExpression(
            constantValueOrNull = null,
            cacheKey = RemoteComponentCacheKey(componentKey, "centerX"),
            arrayProvider = { creationState ->
                floatArrayOf(
                    creationState.writer.addComponentWidthValue(componentKey),
                    2f,
                    AnimatedFloatExpression.DIV,
                )
            },
        )
    }

    public fun componentCenterY(): RemoteFloat {
        val componentKey = (state.creationState as RemoteComposeCreationState).componentCacheKey
        return RemoteFloatExpression(
            constantValueOrNull = null,
            cacheKey = RemoteComponentCacheKey(componentKey, "centerY"),
            arrayProvider = { creationState ->
                floatArrayOf(
                    creationState.writer.addComponentHeightValue(componentKey),
                    2f,
                    AnimatedFloatExpression.DIV,
                )
            },
        )
    }
}
