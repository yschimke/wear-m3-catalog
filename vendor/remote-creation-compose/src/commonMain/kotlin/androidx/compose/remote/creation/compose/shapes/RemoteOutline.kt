/*
 * Copyright 2025 The Android Open Source Project
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

package androidx.compose.remote.creation.compose.shapes

import androidx.compose.remote.creation.RemotePath
import androidx.compose.remote.creation.compose.layout.RemoteOffset
import androidx.compose.remote.creation.compose.layout.RemoteSize
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.vector.RemotePathScope

/**
 * Defines a simple shape, used for bounding graphical regions.
 *
 * Can be used for defining a shape of the component background, a shape of shadows cast by the
 * component, or to clip the contents.
 */
public sealed class RemoteOutline {

    /** Rectangular area. */
    public class Rectangle(public val topLeft: RemoteOffset, public val size: RemoteSize) :
        RemoteOutline()

    /**
     * Rectangular area with rounded corners.
     *
     * @param topStart the resolved corner radius of the top start corner
     * @param topEnd the resolved corner radius of the top end corner
     * @param bottomEnd the resolved corner radius of the bottom end corner
     * @param bottomStart the resolved corner radius of the bottom start corner
     * @param offset the top-left offset of the rounded rectangle bounding box (e.g. `(0, 0)` for a
     *   solid background fill, or `(halfStroke, halfStroke)` to center a stroked border within the
     *   component bounds)
     * @param size the dimensions (width and height) of the rounded rectangle (e.g. `(width -
     *   strokeWidth, height - strokeWidth)` for an inset stroked border). If null, defaults to the
     *   full canvas width and height.
     */
    public class Rounded(
        public val topStart: RemoteFloat,
        public val topEnd: RemoteFloat,
        public val bottomEnd: RemoteFloat,
        public val bottomStart: RemoteFloat,
        public val offset: RemoteOffset = RemoteOffset.Zero,
        public val size: RemoteSize? = null,
    ) : RemoteOutline()

    /** An area defined as a path. */
    public class Generic : RemoteOutline {
        public val path: RemotePath?
        internal val block: (RemotePathScope.() -> Unit)?

        public constructor(path: RemotePath) : super() {
            this.path = path
            this.block = null
        }

        public constructor(block: RemotePathScope.() -> Unit) : super() {
            this.path = null
            this.block = block
        }
    }

    private object NonExhaustive : RemoteOutline()
}

