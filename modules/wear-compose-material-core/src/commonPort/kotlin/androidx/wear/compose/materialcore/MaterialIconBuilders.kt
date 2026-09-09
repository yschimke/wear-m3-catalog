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

package androidx.wear.compose.materialcore

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/*
 * `androidx.compose.material.icons.materialIcon` / `.materialPath` — the two builders the two
 * icons in RangeDefaults.kt are declared with — have no Compose Multiplatform equivalent:
 * `material-icons-core` was deprecated and then dropped from CMP, and nothing replaced these.
 *
 * They are five lines of `ImageVector.Builder` defaults each, so this is a like-for-like copy of
 * the upstream bodies rather than a port with a decision in it. Keeping the NAMES identical is
 * what makes it cheap: RangeDefaults.kt is generated, and matching the name means the transform
 * only has to drop an import (`transform-rules.json` -> `dropImports`) instead of carrying a patch
 * that would need re-cutting every time upstream touches that file.
 *
 * Same package as the generated sources, and `internal`, so this widens nothing publicly.
 */

/** Copy of `androidx.compose.material.icons.materialIcon`, which CMP does not publish. */
internal inline fun materialIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector =
    ImageVector.Builder(
            name = name,
            defaultWidth = MaterialIconDimension.dp,
            defaultHeight = MaterialIconDimension.dp,
            viewportWidth = MaterialIconDimension,
            viewportHeight = MaterialIconDimension,
            autoMirror = autoMirror,
        )
        .block()
        .build()

/** Copy of `androidx.compose.material.icons.materialPath`, which CMP does not publish. */
internal inline fun ImageVector.Builder.materialPath(
    fillAlpha: Float = 1f,
    strokeAlpha: Float = 1f,
    pathFillType: PathFillType = PathFillType.NonZero,
    pathBuilder: PathBuilder.() -> Unit,
): ImageVector.Builder =
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = fillAlpha,
        stroke = null,
        strokeAlpha = strokeAlpha,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = pathFillType,
        pathBuilder = pathBuilder,
    )

/** The 24dp viewport every Material icon is drawn in. */
@PublishedApi internal const val MaterialIconDimension: Float = 24f
