package androidx.compose.remote.creation.compose.text

import androidx.compose.remote.creation.common.RemoteContext.FLOAT_TIME_IN_HR
import androidx.compose.remote.creation.common.RemoteContext.FLOAT_TIME_IN_MIN
import androidx.compose.remote.creation.common.TextFromFloat.PAD_PRE_NONE
import androidx.compose.remote.creation.common.TextFromFloat.PAD_PRE_ZERO
import androidx.compose.remote.creation.compose.state.RemoteBoolean
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.runtime.Composable

/** Default time values backed by the player's clock variables. */
public object RemoteTimeDefaults {
    /** Whether capture-time locale preferences use a 24-hour clock. */
    @Composable public fun is24HourFormat(): RemoteBoolean = RemoteBoolean(platformUses24HourTime())

    /** A player-evaluated current-time string: "HH:mm" in 24-hour format, "h:mm" in 12-hour. */
    @Composable
    public fun defaultTimeString(
        is24HourFormat: RemoteBoolean = is24HourFormat()
    ): RemoteString {
        val mins = (RemoteFloat(FLOAT_TIME_IN_MIN) % 60f).toRemoteStringOptions(2, 0, PAD_PRE_ZERO)
        val currentHour = RemoteFloat(FLOAT_TIME_IN_HR)
        val hours24 = currentHour.toRemoteStringOptions(2, 0, PAD_PRE_ZERO)
        val hour12 =
            ((currentHour % 12f).isEqualTo(0.rf)).select(RemoteFloat(12f), currentHour % 12f)
        // Upstream dropped the AM/PM suffix and the leading zero: 12-hour time reads "h:mm".
        val hours12 = hour12.toRemoteStringOptions(2, 0, PAD_PRE_NONE)
        return is24HourFormat.select(hours24 + ":" + mins, hours12 + ":" + mins)
    }
}

@Composable internal expect fun platformUses24HourTime(): Boolean
