package ee.schimke.wearcmp.port

import java.time.Instant
import java.time.ZoneId

public actual fun platformLocalTime(epochMillis: Long): LocalTimeParts {
    val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    return LocalTimeParts(hour = time.hour, minute = time.minute, second = time.second)
}
