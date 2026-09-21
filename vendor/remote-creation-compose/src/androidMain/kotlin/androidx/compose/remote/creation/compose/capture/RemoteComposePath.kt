/*
 * Copyright (C) 2024 The Android Open Source Project
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
@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package androidx.compose.remote.creation.compose.capture

import android.graphics.Path
import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.RemotePath
import androidx.compose.remote.creation.common.Utils
import androidx.compose.ui.graphics.asComposePath

/**
 * This wraps a RemotePath in class that implements Compose.ui.graphicsPath This would allow passing
 * path through standard compose interfaces. But providing access to the RemotePath object
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class RemoteComposePath(
    foo: androidx.compose.ui.graphics.Path,
    public val remote: RemotePath,
) : androidx.compose.ui.graphics.Path by foo {
    public fun asAndroidPath(): Path {
        return remote.toAndroidPath()
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemotePath.asComposePath(): RemoteComposePath {
    return RemoteComposePath(toAndroidPath().asComposePath(), this)
}

private fun RemotePath.toAndroidPath(): Path {
    val result = Path()
    val values = pathArray
    var index = 0
    while (index < size) {
        when (Utils.idFromNan(values[index])) {
            10 -> {
                result.moveTo(values[index + 1], values[index + 2])
                index += 3
            }
            11 -> {
                result.lineTo(values[index + 3], values[index + 4])
                index += 5
            }
            12 -> {
                result.quadTo(
                    values[index + 3],
                    values[index + 4],
                    values[index + 5],
                    values[index + 6],
                )
                index += 7
            }
            13 -> {
                result.conicTo(
                    values[index + 3],
                    values[index + 4],
                    values[index + 5],
                    values[index + 6],
                    values[index + 7],
                )
                index += 8
            }
            14 -> {
                result.cubicTo(
                    values[index + 3],
                    values[index + 4],
                    values[index + 5],
                    values[index + 6],
                    values[index + 7],
                    values[index + 8],
                )
                index += 9
            }
            15 -> {
                result.close()
                index++
            }
            else -> error("Unknown RemotePath command at index $index")
        }
    }
    return result
}
