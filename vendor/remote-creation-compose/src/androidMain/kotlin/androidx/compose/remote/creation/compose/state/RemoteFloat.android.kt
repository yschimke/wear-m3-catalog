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

@file:JvmName("RemoteFloatKt")
@file:JvmMultifileClass

package androidx.compose.remote.creation.compose.state

import android.icu.text.DecimalFormat as IcuDecimalFormat

/**
 * Returns a [RemoteString] that evaluates to the result of this [RemoteFloat] formatted according
 * to the provided [IcuDecimalFormat].
 *
 * This method maps the localized ICU [IcuDecimalFormat] configuration (including padding, rounding,
 * and digit constraints) to a remote-compatible string representation. It specifically handles
 * complex padding logic and threshold-based selections to ensure the formatted output remains
 * consistent when evaluated on the remote target.
 *
 * @param format The ICU [IcuDecimalFormat] to use for determining formatting options like
 *   separators, grouping, and padding width.
 * @return A [RemoteString] representing the formatted float.
 */
public fun RemoteFloat.toRemoteString(format: IcuDecimalFormat): RemoteString =
    formatRemoteFloat(this, format)
