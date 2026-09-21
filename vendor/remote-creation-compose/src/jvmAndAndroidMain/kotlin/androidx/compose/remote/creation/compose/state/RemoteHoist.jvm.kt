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

package androidx.compose.remote.creation.compose.state

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.capture.RemoteComposeCreationState
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteComposeNode
import androidx.compose.runtime.Composable

internal class RemoteHoistNode : RemoteComposeNode() {
    var states: Array<out RemoteState<*>> = emptyArray()

    override fun render(creationState: RemoteComposeCreationState, remoteCanvas: RemoteCanvas) {
        for (state in states) {
            if (state is BaseRemoteState<*>) state.getIdForCreationState(creationState)
        }
    }
}

/** Hoists state expressions to the current container level. */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@RemoteComposable
@Composable
public fun Hoist(vararg states: RemoteState<*>) {
    RemoteComposeNode(factory = ::RemoteHoistNode, update = { set(states) { this.states = it } })
}
