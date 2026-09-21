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

@file:JvmName("RemoteTypefaceKt")

package androidx.compose.remote.creation.compose.text

import android.graphics.Typeface
import android.os.Build

/** Converts this [RemoteTypeface] to an Android framework [Typeface]. */
public fun RemoteTypeface.toAndroidTypeface(): Typeface {
    return when (this) {
        RemoteTypeface.Default -> Typeface.DEFAULT
        RemoteTypeface.DefaultBold -> Typeface.DEFAULT_BOLD
        RemoteTypeface.SansSerif -> Typeface.SANS_SERIF
        RemoteTypeface.Serif -> Typeface.SERIF
        RemoteTypeface.Monospace -> Typeface.MONOSPACE
        is RemoteTypeface.Named -> {
            val base = Typeface.create(name, Typeface.NORMAL)
            Typeface.create(base, weight, isItalic)
        }
    }
}

/** Maps an Android framework [Typeface] to a [RemoteTypeface]. */
public fun RemoteTypeface.Companion.fromAndroidTypeface(typeface: Typeface?): RemoteTypeface {
    return when (typeface) {
        null -> RemoteTypeface.Default
        Typeface.DEFAULT -> RemoteTypeface.Default
        Typeface.DEFAULT_BOLD -> RemoteTypeface.DefaultBold
        Typeface.SANS_SERIF -> RemoteTypeface.SansSerif
        Typeface.SERIF -> RemoteTypeface.Serif
        Typeface.MONOSPACE -> RemoteTypeface.Monospace
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val name = typeface.systemFontFamilyName
                val weight = typeface.weight
                val italic = typeface.isItalic
                if (name != null) {
                    RemoteTypeface.Named(name, weight, italic)
                } else {
                    RemoteTypeface.Default
                }
            } else {
                RemoteTypeface.Default
            }
        }
    }
}
