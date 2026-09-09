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

package ee.schimke.wearcmp.port

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The one function `androidx.core.graphics.ColorUtils` is used for: CIE XYZ (D65, 2° observer) to
 * sRGB. It sits at the bottom of the CAM16 colour-appearance model in `ColorAppearanceModel.kt`,
 * which is otherwise pure arithmetic and ports unchanged.
 *
 * Transcribed from the AOSP implementation — same matrix, same sRGB companding, same rounding —
 * because the values feed a colour scheme that has to match Android's pixel for pixel.
 */
public object ColorUtils {
    public fun XYZToColor(x: Double, y: Double, z: Double): Int {
        var red = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100
        var green = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100
        var blue = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100

        red = companding(red)
        green = companding(green)
        blue = companding(blue)

        return argb(constrain(red), constrain(green), constrain(blue))
    }

    /** The sRGB transfer function. */
    private fun companding(component: Double): Double =
        if (component > 0.0031308) 1.055 * component.pow(1 / 2.4) - 0.055 else 12.92 * component

    private fun constrain(component: Double): Int =
        (component * 255).roundToInt().coerceIn(0, 255)

    private fun argb(red: Int, green: Int, blue: Int): Int =
        (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
}
