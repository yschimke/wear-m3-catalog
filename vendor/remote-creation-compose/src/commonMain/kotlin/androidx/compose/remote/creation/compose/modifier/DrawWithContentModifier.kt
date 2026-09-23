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

package androidx.compose.remote.creation.compose.modifier

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.layout.RemoteContentDrawScope
import androidx.compose.remote.creation.compose.layout.RemoteCanvas
import androidx.compose.remote.creation.compose.layout.RemoteDrawScope
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.common.RemoteModifierOperation

/**
 * Creates a [RemoteModifier] that allows drawing with the component's content.
 *
 * @param onDraw The drawing block that provides access to [RemoteContentDrawScope].
 */
public fun RemoteModifier.drawWithContent(
    onDraw: RemoteContentDrawScope.() -> Unit
): RemoteModifier = then(DrawWithContentModifier(onDraw))

internal class DrawWithContentModifier(val onDraw: RemoteContentDrawScope.() -> Unit) :
    RemoteModifier.Element {
    override fun RemoteStateScope.toRemoteModifierOperation(): RemoteModifierOperation =
        RemoteModifierOperation.DrawContent
}

internal fun RemoteModifier.hasDrawWithContent(): Boolean =
    any { it is DrawWithContentModifier }

/** Runs every draw-with-content modifier in order, nesting each modifier's content lambda. */
internal fun RemoteModifier.drawWithContent(
    remoteCanvas: RemoteCanvas,
    content: RemoteDrawScope.() -> Unit,
): Boolean {
    val modifiers = mutableListOf<DrawWithContentModifier>()
    foldIn(Unit) { _, element ->
        (element as? DrawWithContentModifier)?.let(modifiers::add)
    }
    if (modifiers.isEmpty()) return false

    fun drawAt(index: Int) {
        if (index == modifiers.size) {
            RemoteDrawScope(remoteCanvas).content()
        } else {
            modifiers[index].onDraw(
                RemoteContentDrawScope(remoteCanvas) { drawAt(index + 1) }
            )
        }
    }
    drawAt(0)
    return true
}
