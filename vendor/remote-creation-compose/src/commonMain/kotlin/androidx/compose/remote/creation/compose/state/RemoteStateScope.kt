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

package androidx.compose.remote.creation.compose.state

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.capture.RemoteComposeCreationContext
import androidx.compose.remote.creation.compose.capture.RemoteDensity
import androidx.compose.remote.creation.compose.capture.RemoteDensityBehavior
import androidx.compose.remote.creation.RemotePath
import androidx.compose.remote.creation.compose.capture.toRemotePath
import androidx.compose.remote.creation.compose.vector.RemotePathScope
import androidx.compose.ui.unit.LayoutDirection

/** Scope for accessing remote state IDs. */
public interface RemoteStateScope {
    /** The [RemoteComposeCreationState] associated with the document being drawn into. */
    @get:Suppress("HiddenAbstractMethodInInterface")
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val parentScope: RemoteStateScope

    /** The [RemoteDensity] associated with the document being drawn into. */
    public val remoteDensity: RemoteDensity

    /** The [LayoutDirection] associated with the document being drawn into. */
    public val layoutDirection: LayoutDirection

    /** The density behavior associated with the document being drawn into. */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val densityBehavior: RemoteDensityBehavior
        get() = parentScope.densityBehavior

    /** Returns the ID for this state within the scope. */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val RemoteState<*>.id: Int
        get() = (this as BaseRemoteState<*>).getIdForCreationState(creationState)

    /** Returns the float ID for this state within the scope. */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val RemoteState<*>.floatId: Float
        get() = (this as BaseRemoteState<*>).getFloatIdForCreationState(creationState)

    /** Returns the long ID for this state within the scope. */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val RemoteState<*>.longId: Long
        get() = (this as BaseRemoteState<*>).getLongIdForCreationState(creationState)
}

/** The [RemoteComposeCreationContext] associated with the document being drawn into. */
@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public val RemoteStateScope.creationState: RemoteComposeCreationContext
    get() = this as? RemoteComposeCreationContext ?: parentScope.creationState

/** Builds a platform-neutral encoded path using this state allocation scope. */
public fun RemoteStateScope.remotePath(block: RemotePathScope.() -> Unit): RemotePath =
    RemotePathScope().apply(block).nodes.toRemotePath(creationState = this)
