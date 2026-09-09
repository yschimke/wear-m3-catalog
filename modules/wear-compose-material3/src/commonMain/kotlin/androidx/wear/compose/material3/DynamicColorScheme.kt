// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.
// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.
/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.wear.compose.material3

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/*
 * `dynamicColorScheme(context)` is not ported. It reads the watch's current watch-face palette out
 * of `android.R.color.system_*` — resources the platform itself populates — and gates that on a
 * Wear system setting. There is no system palette off-watch, and inventing one would defeat the
 * purpose of the API, which is to follow whatever the wearer chose. Callers already have to handle
 * it returning null, so a host that wants dynamic colour builds a ColorScheme and provides it.
 *
 * What stays is `setLuminance`, which is not dynamic-colour-specific at all: ScrollIndicator uses
 * it to derive its two default colours, and it is pure CAM16 arithmetic over ColorAppearanceModel.
 */

/**
 * Set the luminance(tone) of this color. Chroma may decrease because chroma has a different maximum
 * for any given hue and luminance.
 *
 * @param newLuminance 0 <= newLuminance <= 100; invalid values are corrected.
 */
internal fun Color.setLuminance(@FloatRange(from = 0.0, to = 100.0) newLuminance: Float): Color {
    if ((newLuminance < 0.0001) or (newLuminance > 99.9999)) {
        return Color(CamUtils.argbFromLstar(newLuminance.toDouble()))
    }

    val baseCam: Cam = Cam.fromInt(this.toArgb())
    val baseColor = Cam.getInt(baseCam.hue, baseCam.chroma, newLuminance)

    return Color(baseColor)
}
