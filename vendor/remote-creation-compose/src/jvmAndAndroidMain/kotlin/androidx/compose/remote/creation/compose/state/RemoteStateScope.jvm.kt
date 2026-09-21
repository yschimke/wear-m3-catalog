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
import androidx.compose.remote.creation.RemotePath
import androidx.compose.remote.creation.compose.capture.LocalRemoteComposeCreationState
import androidx.compose.remote.creation.compose.capture.toRemotePath
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.vector.RemotePathScope
import androidx.compose.runtime.Composable

/** Build the [RemotePath], encoding using this [RemoteStateScope]. */
public fun RemoteStateScope.remotePath(fn: RemotePathScope.() -> Unit): RemotePath {
    return RemotePathScope().apply(fn).nodes.toRemotePath(creationState = this)
}

/** Allocates this state in document-global scope. */
@Composable
@RemoteComposable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun <T : RemoteState<*>> T.withGlobalScope(): T {
    LocalRemoteComposeCreationState.current.enqueueGlobalDeclaration(this as BaseRemoteState<*>)
    return this
}
