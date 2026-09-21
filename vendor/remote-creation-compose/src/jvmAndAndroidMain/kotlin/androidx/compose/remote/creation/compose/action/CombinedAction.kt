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
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.common.RemoteActionData

/**
 * Creates an action that's a composite of multiple actions.
 *
 * @param actions The actions to combine.
 */
public fun combinedAction(vararg actions: Action): Action = CombinedAction(*actions)

internal class CombinedAction(public vararg val actions: Action) : RemoteAction() {
    override fun RemoteStateScope.toRemoteActionData(): List<RemoteActionData> =
        actions.flatMap { resolveAction(it) }
}
