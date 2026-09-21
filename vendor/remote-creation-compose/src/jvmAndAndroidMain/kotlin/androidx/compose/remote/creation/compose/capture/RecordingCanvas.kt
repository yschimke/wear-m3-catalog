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

package androidx.compose.remote.creation.compose.capture

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemotePaint
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.RoundedPolygon

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface RecordingCanvas : RemoteStateScope {
    public val enableOptimizations: Boolean
    public var creationState: RemoteComposeCreationState
    public val creationDisplayInfo: RemoteCreationDisplayInfo

    override val document: RemoteComposeWriter
    override val remoteDensity: RemoteDensity
    override val layoutDirection: LayoutDirection

    public fun save(): Int

    public fun restore()

    /**
     * Preconcat the current matrix with the specified translation
     *
     * @param dx The distance to translate in X
     * @param dy The distance to translate in Y
     */
    public fun translate(dx: Float, dy: Float)

    /**
     * Preconcat the current matrix with the specified translation
     *
     * @param dx The [RemoteFloat] distance to translate in X
     * @param dy The [RemoteFloat] distance to translate in Y
     */
    public fun translate(dx: RemoteFloat, dy: RemoteFloat)

    public fun scale(sx: RemoteFloat, sy: RemoteFloat)

    public fun scale(sx: RemoteFloat, sy: RemoteFloat, px: RemoteFloat, py: RemoteFloat)

    public fun rotate(degrees: RemoteFloat)

    public fun rotate(degrees: RemoteFloat, px: RemoteFloat, py: RemoteFloat)

    public fun concat(matrix: Matrix)

    public fun drawComponentContent()

    public fun flush()

    public fun setRemoteComposeCreationState(creationState: RemoteComposeCreationState)

    public fun drawRoundedPolygon(roundedPolygon: RoundedPolygon, paint: RemotePaint?)

    public fun drawRoundedPolygonMorph(
        from: RoundedPolygon,
        to: RoundedPolygon,
        progress: RemoteFloat,
        paint: RemotePaint?,
    )

    public fun custom(
        config: String,
        modifier: androidx.compose.remote.creation.compose.modifier.RemoteModifier =
            androidx.compose.remote.creation.compose.modifier.RemoteModifier,
        content: (() -> Unit)? = null,
        properties:
            androidx.compose.remote.creation.compose.layout.RemoteCustomPropertiesScope.() -> Unit =
            {},
    )
}

internal interface InternalRecordingCanvas : RecordingCanvas {
    val buffer: RemoteDocumentProgram
    var forceSendingPaint: Boolean
    var currentDrawToBitmapId: Int

    fun recordRenderingOp(op: DocumentOp): RemoteDocumentProgram.SpanOp

    fun recordRenderingOp(action: () -> Unit): RemoteDocumentProgram.SpanOp

    fun recordRenderingOp(operation: WriterOp): RemoteDocumentProgram.SpanOp

    fun recordRenderingOp(
        paint: RemotePaint?,
        action: () -> Unit,
    ): RemoteDocumentProgram.SpanOp

    fun recordRenderingOp(
        paint: RemotePaint?,
        operation: WriterOp,
    ): RemoteDocumentProgram.SpanOp

    fun recordInChildSpan(action: () -> Unit): RemoteDocumentProgram.Span

    fun recordInOffscreenChildSpan(
        bitmapId: Int,
        action: () -> Unit,
    ): RemoteDocumentProgram.Span
}
