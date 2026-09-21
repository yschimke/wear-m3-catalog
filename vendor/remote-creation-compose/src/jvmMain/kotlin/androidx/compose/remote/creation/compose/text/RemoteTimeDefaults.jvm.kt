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

package androidx.compose.remote.creation.compose.text

import androidx.compose.remote.core.RemoteContext.FLOAT_TIME_IN_HR
import androidx.compose.remote.core.RemoteContext.FLOAT_TIME_IN_MIN
import androidx.compose.remote.core.operations.TextFromFloat.PAD_PRE_ZERO
import androidx.compose.remote.creation.compose.state.RemoteBoolean
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import java.text.DateFormat
import java.text.SimpleDateFormat

/** JVM counterpart to AndroidX's Android-only time-format defaults. */
public object RemoteTimeDefaults {
  @Composable
  public fun is24HourFormat(): RemoteBoolean {
    val pattern = (DateFormat.getTimeInstance(DateFormat.SHORT) as? SimpleDateFormat)?.toPattern()
    return RemoteBoolean(pattern?.any { it == 'H' || it == 'k' } == true)
  }

  @Composable
  public fun defaultTimeString(is24HourFormat: RemoteBoolean = is24HourFormat()): RemoteString {
    val mins = (RemoteFloat(FLOAT_TIME_IN_MIN) % 60f).toRemoteStringOptions(2, 0, PAD_PRE_ZERO)
    val hours24String = RemoteFloat(FLOAT_TIME_IN_HR).toRemoteStringOptions(2, 0, PAD_PRE_ZERO)
    val currentHour = RemoteFloat(FLOAT_TIME_IN_HR)
    val hour12 =
      ((currentHour % 12f).isEqualTo(0.rf)).select(RemoteFloat(12f), currentHour % 12f)
    val hours12String = hour12.toRemoteStringOptions(2, 0, PAD_PRE_ZERO)
    val amPm = (currentHour.isLessThan(12.rf)).select(" AM".rs, " PM".rs)
    val time24 = hours24String + ":" + mins
    val time12 = hours12String + ":" + mins + amPm
    return is24HourFormat.select(time24, time12)
  }
}
