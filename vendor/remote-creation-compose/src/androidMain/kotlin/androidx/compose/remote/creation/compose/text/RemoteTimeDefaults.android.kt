package androidx.compose.remote.creation.compose.text

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun platformUses24HourTime(): Boolean = DateFormat.is24HourFormat(LocalContext.current)
