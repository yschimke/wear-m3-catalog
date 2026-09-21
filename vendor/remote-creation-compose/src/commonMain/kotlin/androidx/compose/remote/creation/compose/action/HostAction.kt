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

package androidx.compose.remote.creation.compose.action

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.RemoteState
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.common.RemoteActionData

/**
 * Creates an [Action] that triggers a named action on the host.
 *
 * @param name The name of the action.
 */
public fun hostAction(name: RemoteString): Action = HostAction(name, HostAction.Type.NONE)

/**
 * Creates an [Action] that triggers a named action on the host with a float value.
 *
 * @param name The name of the action.
 * @param value The float value to pass with the action.
 */
public fun hostAction(name: RemoteString, value: RemoteFloat): Action = HostAction(name, value)

/**
 * Creates an [Action] that triggers a named action on the host with an int value.
 *
 * @param name The name of the action.
 * @param value The int value to pass with the action.
 */
public fun hostAction(name: RemoteString, value: RemoteInt): Action = HostAction(name, value)

/**
 * Creates an [Action] that triggers a named action on the host with a string value.
 *
 * @param name The name of the action.
 * @param value The string value to pass with the action.
 */
public fun hostAction(name: RemoteString, value: RemoteString): Action = HostAction(name, value)

/** Run the named host action when invoked. */
internal class HostAction(
    public val name: RemoteString,
    public val type: Type = Type.NONE,
    public val id: Int = 0,
    public val value: RemoteState<*>? = null,
) : RemoteAction() {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public enum class Type(public val value: Int) {
        FLOAT(0),
        INT(1),
        STRING(2),
        FLOAT_ARRAY(3),
        NONE(-1),
    }

    // TODO: Add a RemoteFloatArray type and use it here!
    public constructor(name: RemoteString, value: RemoteFloat) : this(name, Type.FLOAT, 0, value)

    public constructor(name: RemoteString, value: RemoteInt) : this(name, Type.INT, 0, value)

    public constructor(name: RemoteString, value: RemoteString) : this(name, Type.STRING, 0, value)

    public constructor(
        id: Int,
        name: RemoteString,
        value: RemoteString,
    ) : this(name, Type.STRING, id, value)

    override fun RemoteStateScope.toRemoteActionData(): List<RemoteActionData> {
        val valueId = value?.id ?: -1
        return listOf(
            if (id != 0) {
                RemoteActionData.HostMetadata(id, valueId)
            } else {
                RemoteActionData.HostNamed(name.id, type.value, valueId)
            }
        )
    }
}
