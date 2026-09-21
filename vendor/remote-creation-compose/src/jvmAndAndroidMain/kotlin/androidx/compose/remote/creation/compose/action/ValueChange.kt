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
import androidx.compose.remote.creation.compose.state.MutableRemoteFloat
import androidx.compose.remote.creation.compose.state.MutableRemoteInt
import androidx.compose.remote.creation.compose.state.MutableRemoteState
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.RemoteState
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.isLiteral
import androidx.compose.remote.creation.common.RemoteActionData

/** Update a value on click. */
internal class ValueChangeAction<T>(
    public val remoteValue: MutableRemoteState<T>,
    public val updatedValue: RemoteState<T>,
) : RemoteAction() {
    override fun RemoteStateScope.toRemoteActionData(): List<RemoteActionData> {
        val actualMutable = remoteValue.asEncodedMutable
        val actualValue = updatedValue.asEncoded
        val action =
            when (actualMutable) {
                is MutableRemoteInt -> {
                    actualValue as RemoteInt
                    val array = actualValue.arrayForCreationState(this)
                    if (array.isLiteral()) {
                        RemoteActionData.IntegerChange(actualMutable.id, array[0].toInt())
                    } else {
                        RemoteActionData.IntegerExpressionChange(
                            actualMutable.longId,
                            actualValue.longId,
                        )
                    }
                }
                is MutableRemoteFloat -> {
                    actualValue as RemoteFloat
                    RemoteActionData.FloatExpressionChange(actualMutable.id, actualValue.id)
                }
                is RemoteString -> {
                    actualValue as RemoteString
                    RemoteActionData.StringChange(actualMutable.id, actualValue.id)
                }
                else -> error("Unsupported value change type $actualMutable")
            }
        return listOf(action)
    }
}

internal class ValueFloatChangeAction(
    public val value: MutableRemoteFloat,
    public val updatedValue: Float,
) : RemoteAction() {
    override fun RemoteStateScope.toRemoteActionData(): List<RemoteActionData> =
        listOf(RemoteActionData.FloatChange(value.id, updatedValue))
}

/**
 * Creates an [Action] that updates the value of a [MutableRemoteState] to a new [RemoteState].
 *
 * @param remoteState The mutable remote state to be updated.
 * @param updatedValue The new remote state value to apply.
 * @return An [Action] representing the value change.
 */
public fun <T> valueChange(
    remoteState: MutableRemoteState<T>,
    updatedValue: RemoteState<T>,
): Action = ValueChangeAction(remoteState, updatedValue)
