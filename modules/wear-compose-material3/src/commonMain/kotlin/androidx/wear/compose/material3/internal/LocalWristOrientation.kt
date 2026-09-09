// Generated from AndroidX Wear Compose by tools/transform.py — DO NOT EDIT.
// Re-run scripts/regenerate.sh; make changes in transform-rules.json or patches/.
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

package androidx.wear.compose.material3.internal

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import ee.schimke.wearcmp.port.LocalWearDeviceConfiguration
import ee.schimke.wearcmp.port.WearWristOrientation

internal enum class WristOrientation {
    /**
     * Device worn on the left wrist with the screen in its default orientation. The RSB is
     * typically on the right side.
     */
    LEFT_WRIST_ROTATION_0,

    /**
     * Device worn on the left wrist with the screen rotated 180°. The RSB is typically on the left
     * side.
     */
    LEFT_WRIST_ROTATION_180,

    /**
     * Device worn on the right wrist with the screen in its default orientation. The RSB is
     * typically on the right side.
     */
    RIGHT_WRIST_ROTATION_0,

    /**
     * Device worn on the right wrist with the screen rotated 180°. The RSB is typically on the left
     * side.
     */
    RIGHT_WRIST_ROTATION_180,
}

internal fun WristOrientation.isLeftWrist(): Boolean =
    this == WristOrientation.LEFT_WRIST_ROTATION_0 ||
        this == WristOrientation.LEFT_WRIST_ROTATION_180

/**
 * [CompositionLocal] providing the global wrist orientation and hardware alignment.
 *
 * This includes the choice of wrist (left or right) and the screen rotation, which determines
 * whether the Rotating Side Button (RSB) is positioned on the left or right side of the device.
 */
internal val LocalWristOrientation: ProvidableCompositionLocal<WristOrientation> =
    compositionLocalWithComputedDefaultOf {
        // Upstream reads Settings.Global "wear_wrist_orientation_mode" through a ContentResolver
        // and keeps it live with a ContentObserver. Off-Android the host is the one that knows
        // which wrist it is portraying, and it says so through the device configuration.
        when (LocalWearDeviceConfiguration.currentValue.wristOrientation) {
            WearWristOrientation.LeftWristRotation0 -> WristOrientation.LEFT_WRIST_ROTATION_0
            WearWristOrientation.LeftWristRotation180 -> WristOrientation.LEFT_WRIST_ROTATION_180
            WearWristOrientation.RightWristRotation0 -> WristOrientation.RIGHT_WRIST_ROTATION_0
            WearWristOrientation.RightWristRotation180 -> WristOrientation.RIGHT_WRIST_ROTATION_180
        }
    }

internal const val TAG = "CompositionLocals"
