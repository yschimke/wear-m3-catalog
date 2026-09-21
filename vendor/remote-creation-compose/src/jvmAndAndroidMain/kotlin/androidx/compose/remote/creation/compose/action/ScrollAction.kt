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

package androidx.compose.remote.creation.compose.action

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.modifier.RemoteScrollState
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.common.RemoteActionData

internal class ScrollByAction(
    public val scrollState: RemoteScrollState,
    public val value: RemoteFloat,
) : RemoteAction() {
    override fun RemoteStateScope.toRemoteActionData(): List<RemoteActionData> {
        val positionState = scrollState.positionState
        return listOf(RemoteActionData.FloatExpressionChange(positionState.id, (positionState + value).id))
    }
}

internal class ScrollToAction(
    public val scrollState: RemoteScrollState,
    public val value: RemoteFloat,
) : RemoteAction() {
    override fun RemoteStateScope.toRemoteActionData(): List<RemoteActionData> =
        listOf(RemoteActionData.FloatExpressionChange(scrollState.positionState.id, value.id))
}

/**
 * Creates an [Action] that scrolls this [RemoteScrollState] by a delta [value].
 *
 * @param value The delta in pixels to scroll by.
 * @return An [Action] representing the scroll change.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteScrollState.scrollBy(value: RemoteFloat): Action = ScrollByAction(this, value)

/**
 * Creates an [Action] that scrolls this [RemoteScrollState] to a target [value].
 *
 * @param value The target position in pixels to scroll to.
 * @return An [Action] representing the scroll change.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteScrollState.scrollTo(value: RemoteFloat): Action = ScrollToAction(this, value)
