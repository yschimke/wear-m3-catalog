// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.
// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.
/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.wear.compose.foundation

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import ee.schimke.wearcmp.port.platformReduceMotion

/**
 * [CompositionLocal] for global reduce-motion setting, which turns off animations and screen
 * movements. To use, call LocalReduceMotion.current, which returns a Boolean.
 */
public val LocalReduceMotion: ProvidableCompositionLocal<Boolean> =
    compositionLocalOf {
        // Upstream reads Settings.Global "reduce_motion" through a ContentResolver and keeps it
        // live with a ContentObserver. `platformReduceMotion()` answers the same question per
        // platform — on the web from `prefers-reduced-motion` — and is read once, so the observer
        // and its cache have nothing left to do.
        platformReduceMotion()
    }

/**
 * [CompositionLocal] containing the background scrim color of [BasicSwipeToDismissBox].
 *
 * Defaults to [Color.Black] if not explicitly set.
 */
public val LocalSwipeToDismissBackgroundScrimColor: ProvidableCompositionLocal<Color> =
    compositionLocalOf {
        Color.Black
    }

/**
 * [CompositionLocal] containing the content scrim color of [BasicSwipeToDismissBox].
 *
 * Defaults to [Color.Black] if not explicitly set.
 */
public val LocalSwipeToDismissContentScrimColor: ProvidableCompositionLocal<Color> =
    compositionLocalOf {
        Color.Black
    }

/**
 * [CompositionLocal] used to express/determine if a screen is active, as specified by each
 * component (for example, it could be updated when the user is in the middle of the gesture to
 * switch screens, or after the gesture is done). Components that manage multiple screens (like
 * pager and swipe to dismiss) provide this, and can be used at the screen level to update the UI or
 * perform optimizations on inactive screens.
 *
 * Defaults to true
 */
public val LocalScreenIsActive: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

internal const val TAG = "CompositionLocals"
