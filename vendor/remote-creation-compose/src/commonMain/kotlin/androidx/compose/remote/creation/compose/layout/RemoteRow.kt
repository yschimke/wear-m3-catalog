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

package androidx.compose.remote.creation.compose.layout

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.modifier.RemoteDimensionType
import androidx.compose.remote.creation.compose.capture.RemoteComposeCreationState
import androidx.compose.remote.creation.compose.capture.WriterOp
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.WidthModifier
import androidx.compose.remote.creation.compose.modifier.toRemoteModifierData
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Receiver scope used by [RemoteRow] for its content. */
public class RemoteRowScope {
    /**
     * Sets the horizontal weight of this element relative to its siblings in the [RemoteRow].
     *
     * @param weight The proportional width to allocate to this element.
     */
    public fun RemoteModifier.weight(weight: RemoteFloat): RemoteModifier =
        then(WidthModifier(RemoteDimensionType.WEIGHT, weight))

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun RemoteModifier.weight(weight: Float): RemoteModifier =
        then(WidthModifier(RemoteDimensionType.WEIGHT, RemoteFloat(weight)))
}

internal class RemoteRowNode : RemoteComposeNode() {
    var horizontalArrangement: RemoteArrangement.Horizontal = RemoteArrangement.Start
    var verticalAlignment: RemoteAlignment.Vertical = RemoteAlignment.Top
    var layoutDirection: LayoutDirection = LayoutDirection.Ltr

    override fun render(creationState: RemoteComposeCreationState, remoteCanvas: RemoteCanvas) {
        val scope = overriddenScope(creationState)
        val remoteModifier = scope.toRemoteModifierData(modifier)
        (horizontalArrangement as? RemoteSpaced)?.let {
            remoteModifier.spacedBy = it.getSpacingFloatId(creationState)
        }
        remoteCanvas.internalCanvas.recordRenderingOp(
            WriterOp.StartRow(
                remoteModifier,
                horizontalArrangement.toRemote(layoutDirection),
                verticalAlignment.toRemote(),
            )
        )
        renderChildren(
            creationState,
            remoteCanvas,
            reversed = shouldReverse(horizontalArrangement, layoutDirection),
        )
        remoteCanvas.internalCanvas.recordRenderingOp(WriterOp.EndRow)
    }
}

/**
 * A layout composable that positions its children in a horizontal sequence.
 *
 * `RemoteRow` allows you to arrange children horizontally and control their [horizontalArrangement]
 * (spacing) and [verticalAlignment].
 *
 * @sample androidx.compose.remote.creation.compose.samples.RemoteRowSample
 * @sample androidx.compose.remote.creation.compose.samples.RemoteRowWeightSample
 * @param modifier The modifier to be applied to this row.
 * @param horizontalArrangement The horizontal arrangement of the children.
 * @param verticalAlignment The vertical alignment of the children.
 * @param content The content of the row, which has access to [RemoteRowScope].
 */
@RemoteComposable
@Composable
public fun RemoteRow(
    modifier: RemoteModifier = RemoteModifier,
    horizontalArrangement: RemoteArrangement.Horizontal = RemoteArrangement.Start,
    verticalAlignment: RemoteAlignment.Vertical = RemoteAlignment.Top,
    content: @Composable RemoteRowScope.() -> Unit,
) {
    val scope = remember { RemoteRowScope() }
    val layoutDirection = LocalLayoutDirection.current
    RemoteComposeNode(
        factory = ::RemoteRowNode,
        update = {
            set(modifier) { nodeModifier -> this.modifier = nodeModifier }
            set(horizontalArrangement) { nodeHorizontalArrangement ->
                this.horizontalArrangement = nodeHorizontalArrangement
            }
            set(verticalAlignment) { nodeVerticalAlignment ->
                this.verticalAlignment = nodeVerticalAlignment
            }
            set(layoutDirection) { nodeLayoutDirection ->
                this.layoutDirection = nodeLayoutDirection
            }
        },
        content = { scope.content() },
    )
}
