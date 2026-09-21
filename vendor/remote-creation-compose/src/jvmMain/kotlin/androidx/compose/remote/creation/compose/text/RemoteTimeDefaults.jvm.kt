package androidx.compose.remote.creation.compose.text

import androidx.compose.runtime.Composable
import java.text.DateFormat
import java.text.SimpleDateFormat

@Composable
internal actual fun platformUses24HourTime(): Boolean {
    val pattern = (DateFormat.getTimeInstance(DateFormat.SHORT) as? SimpleDateFormat)?.toPattern()
    return pattern?.any { it == 'H' || it == 'k' } == true
}
