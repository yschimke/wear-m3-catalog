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

import androidx.compose.ui.graphics.Path
import androidx.graphics.shapes.Cubic
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

/**
 * `Morph.toPath` / `RoundedPolygon.toPath`, which `androidx.graphics.shapes` only publishes for
 * Android — they return an `android.graphics.Path`.
 *
 * The shape data itself is multiplatform: a morph is a list of cubic Bézier segments at any
 * progress, and every one of them maps one-for-one onto a `Path.cubicTo`. So this is a transcription
 * rather than an approximation — the same curve, drawn through Compose's own Path.
 */
public fun Morph.toComposePath(progress: Float, path: Path = Path()): Path {
    path.reset()
    asCubics(progress).forEachIndexed { index, cubic -> path.appendCubic(cubic, first = index == 0) }
    path.close()
    return path
}

/** [toComposePath] for a static shape. */
public fun RoundedPolygon.toComposePath(path: Path = Path()): Path {
    path.reset()
    cubics.forEachIndexed { index, cubic -> path.appendCubic(cubic, first = index == 0) }
    path.close()
    return path
}

private fun Path.appendCubic(cubic: Cubic, first: Boolean) {
    if (first) moveTo(cubic.anchor0X, cubic.anchor0Y)
    cubicTo(
        cubic.control0X,
        cubic.control0Y,
        cubic.control1X,
        cubic.control1Y,
        cubic.anchor1X,
        cubic.anchor1Y,
    )
}
