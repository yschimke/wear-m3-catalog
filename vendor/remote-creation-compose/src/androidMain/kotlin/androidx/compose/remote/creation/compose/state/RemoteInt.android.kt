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

@file:JvmName("RemoteIntKt")
@file:JvmMultifileClass

package androidx.compose.remote.creation.compose.state

import android.icu.text.DecimalFormat as IcuDecimalFormat

/**
 * Converts this [RemoteInt] to a [RemoteString] using the specified [format].
 *
 * This method maps the localized ICU [IcuDecimalFormat] configuration (including padding, rounding,
 * and digit constraints) to a remote-compatible string representation. It specifically handles
 * complex padding logic and threshold-based selections to ensure the formatted output remains
 * consistent when evaluated on the remote target.
 *
 * @param format The [IcuDecimalFormat] used to format the integer value.
 * @return A [RemoteString] representing the formatted integer value.
 */
public fun RemoteInt.toRemoteString(format: IcuDecimalFormat): RemoteString =
    formatRemoteInt(this, format)
