/*
 * Copyright 2026 Yuri Schimke
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

import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.compose.layout.RemoteCustomPropertiesScope
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.shapes.MorphTweenUtility
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemotePaint
import androidx.compose.remote.creation.compose.state.StandardRemotePaint
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.RoundedPolygon

/** JVM recorder for the platform-neutral RemoteCanvas write path. */
internal class JvmRecordingCanvas(
  override val enableOptimizations: Boolean = false,
) : InternalRecordingCanvas {
  override val buffer = RemoteDocumentProgram(enableOptimizations)
  override lateinit var creationState: RemoteComposeCreationState
  override val parentScope: RemoteStateScope
    get() = creationState
  private val document: RemoteComposeWriter
    get() = creationState.legacyDocument
  override val remoteDensity: RemoteDensity
    get() = creationState.remoteDensity
  override val layoutDirection: LayoutDirection
    get() = creationState.layoutDirection
  override val creationDisplayInfo: RemoteCreationDisplayInfo
    get() = creationState.creationDisplayInfo
  override var forceSendingPaint: Boolean = false
  override var currentDrawToBitmapId: Int = 0
  private val paintTracker = PaintTracker()

  override fun recordRenderingOp(op: DocumentOp): RemoteDocumentProgram.SpanOp =
    buffer.recordRenderingOp(op)

  override fun recordRenderingOp(action: () -> Unit): RemoteDocumentProgram.SpanOp =
    recordRenderingOp(DocumentOp.Draw { action() })

  override fun recordRenderingOp(operation: WriterOp): RemoteDocumentProgram.SpanOp =
    recordRenderingOp(DocumentOp.Draw(operation))

  override fun recordRenderingOp(
    paint: RemotePaint?,
    action: () -> Unit,
  ): RemoteDocumentProgram.SpanOp = recordRenderingOp(action)

  override fun recordRenderingOp(
    paint: RemotePaint?,
    operation: WriterOp,
  ): RemoteDocumentProgram.SpanOp {
    val snapshot = paint?.let(::StandardRemotePaint)
    val force = forceSendingPaint
    forceSendingPaint = false
    return recordRenderingOp(WriterOp.Painted(snapshot, operation, paintTracker, force))
  }

  override fun recordInChildSpan(action: () -> Unit): RemoteDocumentProgram.Span {
    val child = buffer.insertPoint.createChildSpan()
    val previous = buffer.insertPoint
    buffer.insertPoint = child
    try {
      action()
    } finally {
      buffer.insertPoint = previous
    }
    return child
  }

  override fun recordInOffscreenChildSpan(
    bitmapId: Int,
    action: () -> Unit,
  ): RemoteDocumentProgram.Span {
    val previous = currentDrawToBitmapId
    return recordInChildSpan {
      currentDrawToBitmapId = bitmapId
      try {
        action()
      } finally {
        currentDrawToBitmapId = previous
      }
    }
  }

  override fun save(): Int {
    recordRenderingOp(WriterOp.Save)
    return 1
  }

  override fun restore() {
    recordRenderingOp(WriterOp.Restore)
  }

  override fun translate(dx: Float, dy: Float) {
    recordRenderingOp(WriterOp.Translate(dx.rf, dy.rf))
  }

  override fun translate(dx: RemoteFloat, dy: RemoteFloat) {
    val op = recordRenderingOp(WriterOp.Translate(dx, dy))
    buffer.addRoots(op, dx, dy)
  }

  override fun scale(sx: RemoteFloat, sy: RemoteFloat) {
    val op = recordRenderingOp(WriterOp.Scale(sx, sy, null, null))
    buffer.addRoots(op, sx, sy)
  }

  override fun scale(
    sx: RemoteFloat,
    sy: RemoteFloat,
    px: RemoteFloat,
    py: RemoteFloat,
  ) {
    translate(px, py)
    scale(sx, sy)
    translate(-px, -py)
  }

  override fun rotate(degrees: RemoteFloat) {
    val op = recordRenderingOp(WriterOp.Rotate(degrees, null, null))
    buffer.addRoots(op, degrees)
  }

  override fun rotate(degrees: RemoteFloat, px: RemoteFloat, py: RemoteFloat) {
    translate(px, py)
    rotate(degrees)
    translate(-px, -py)
  }

  override fun concat(matrix: Matrix) {
    // Remote Compose has no matrix primitive. Preserve the affine operations used by Compose.
    val values = matrix.values
    translate(values[12], values[13])
  }

  override fun drawComponentContent() {
    recordRenderingOp(WriterOp.DrawComponentContent)
  }

  override fun flush() {
    buffer.flush(creationState)
  }

  override fun setRemoteComposeCreationState(creationState: RemoteComposeCreationState) {
    this.creationState = creationState
  }

  override fun drawRoundedPolygon(roundedPolygon: RoundedPolygon, paint: RemotePaint?) {
    recordRenderingOp(
      paint,
      WriterOp.DrawPath(MorphTweenUtility.cubicsToPathData(roundedPolygon.cubics)),
    )
  }

  override fun drawRoundedPolygonMorph(
    from: RoundedPolygon,
    to: RoundedPolygon,
    progress: RemoteFloat,
    paint: RemotePaint?,
  ) {
    val morph = androidx.graphics.shapes.Morph(from, to)
    val op =
      recordRenderingOp(
        paint,
        WriterOp.DrawTweenPath(
          MorphTweenUtility.cubicsToPathData(morph.asCubics(0f)),
          MorphTweenUtility.cubicsToPathData(morph.asCubics(1f)),
          progress,
          0f.rf,
          1f.rf,
        ),
      )
    buffer.addRoots(op, progress)
  }

  override fun custom(
    config: String,
    modifier: RemoteModifier,
    content: (() -> Unit)?,
    properties: RemoteCustomPropertiesScope.() -> Unit,
  ) {
    val scope = RemoteCustomPropertiesScope().apply(properties)
    val child = content?.let(::recordInChildSpan)
    val op = recordRenderingOp(DocumentOp.CustomComponent(config, modifier, scope.entries, child))
    scope.entries.mapNotNull { it.state }.forEach { buffer.addRoots(op, it) }
  }
}
