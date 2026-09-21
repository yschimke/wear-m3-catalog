package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.common.BitmapFontGlyph
import androidx.compose.remote.creation.common.PaintBundleData
import androidx.compose.remote.creation.common.RemoteModifierData
import androidx.compose.remote.creation.common.RemoteModifierOperation
import androidx.compose.remote.creation.common.RemoteTextData
import androidx.compose.remote.creation.common.RemoteImageData
import androidx.compose.remote.creation.common.RemoteWriter
import androidx.compose.remote.creation.common.DrawTextOnCircle

/** Temporary adapter while the common Kotlin encoder replaces [RemoteComposeWriter]. */
internal class LegacyRemoteWriterAdapter(private val delegate: RemoteComposeWriter) : RemoteWriter {
    override val componentIdForCache: Int
        get() = delegate.buffer.lastComponentId

    override fun addComponentWidthValue(): Float = delegate.addComponentWidthValue()

    override fun addComponentHeightValue(): Float = delegate.addComponentHeightValue()

    override fun startRoot() = delegate.startRoot()

    override fun endRoot() = delegate.endRoot()

    override fun startBox(modifier: RemoteModifierData, horizontal: Int, vertical: Int) {
        delegate.buffer.addBoxStart(modifier.componentId, -1, horizontal, vertical)
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endBox() = delegate.endBox()

    override fun startRow(modifier: RemoteModifierData, horizontal: Int, vertical: Int) {
        delegate.buffer.addRowStart(
            modifier.componentId,
            -1,
            horizontal,
            vertical,
            modifier.spacedBy,
        )
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endRow() = delegate.endRow()

    override fun startColumn(modifier: RemoteModifierData, horizontal: Int, vertical: Int) {
        delegate.buffer.addColumnStart(
            modifier.componentId,
            -1,
            horizontal,
            vertical,
            modifier.spacedBy,
        )
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endColumn() = delegate.endColumn()

    override fun startCanvas(modifier: RemoteModifierData) {
        delegate.buffer.addCanvasStart(modifier.componentId, -1)
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endCanvas() = delegate.endCanvas()

    override fun startCanvasOperations() = delegate.startCanvasOperations()

    override fun endCanvasOperations() = delegate.endCanvasOperations()

    override fun startFitBox(modifier: RemoteModifierData, horizontal: Int, vertical: Int) {
        delegate.buffer.addFitBoxStart(modifier.componentId, -1, horizontal, vertical)
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endFitBox() = delegate.endFitBox()

    override fun startFlow(
        modifier: RemoteModifierData,
        horizontal: Int,
        vertical: Int,
        maxItemsInEachRow: Int,
        maxLines: Int,
    ) {
        delegate.buffer.addFlowStart(
            modifier.componentId,
            -1,
            horizontal,
            vertical,
            modifier.spacedBy,
            maxItemsInEachRow,
            maxLines,
        )
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endFlow() = delegate.endFlow()

    override fun startStateLayout(modifier: RemoteModifierData, indexId: Int) {
        delegate.buffer.addStateLayout(modifier.componentId, -1, 0, 0, indexId)
        modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endStateLayout() = delegate.endStateLayout()

    override fun writeModifier(operation: RemoteModifierOperation) {
        when (operation) {
            is RemoteModifierOperation.Width ->
                delegate.addWidthModifierOperation(operation.type, operation.value)
            is RemoteModifierOperation.Height ->
                delegate.addHeightModifierOperation(operation.type, operation.value)
            is RemoteModifierOperation.Padding ->
                delegate.addModifierPadding(
                    operation.left,
                    operation.top,
                    operation.right,
                    operation.bottom,
                )
            is RemoteModifierOperation.Background -> {
                if (operation.flags == 2) {
                    delegate.addDynamicModifierBackground(operation.colorId, operation.shape)
                } else {
                    delegate.addModifierBackground(
                        operation.red,
                        operation.green,
                        operation.blue,
                        operation.alpha,
                        operation.shape,
                    )
                }
            }
            RemoteModifierOperation.ClipRect -> delegate.addClipRectModifier()
            is RemoteModifierOperation.RoundedClipRect ->
                delegate.addRoundClipRectModifier(
                    operation.topStart,
                    operation.topEnd,
                    operation.bottomStart,
                    operation.bottomEnd,
                )
            is RemoteModifierOperation.WidthIn ->
                delegate.addWidthInModifierOperation(operation.min, operation.max)
            is RemoteModifierOperation.HeightIn ->
                delegate.addHeightInModifierOperation(operation.min, operation.max)
            is RemoteModifierOperation.Offset -> delegate.addModifierOffset(operation.x, operation.y)
            is RemoteModifierOperation.ZIndex -> delegate.addModifierZIndex(operation.value)
            RemoteModifierOperation.Ripple -> delegate.addModifierRipple()
            RemoteModifierOperation.DrawContent -> delegate.addDrawContentOperation()
        }
    }

    override fun startText(data: RemoteTextData) {
        delegate.buffer.addTextComponentStart(
            data.modifier.componentId,
            -1,
            data.textId,
            data.textStyleId,
            data.color,
            data.colorId,
            data.fontSize,
            data.minFontSize,
            data.maxFontSize,
            data.fontStyle,
            data.fontWeight,
            data.fontFamilyId,
            data.textAlign,
            data.overflow,
            data.maxLines,
            data.letterSpacing,
            data.lineHeightAdd,
            data.lineHeightMultiplier,
            data.lineBreakStrategy,
            data.hyphenationFrequency,
            data.justificationMode,
            data.underline,
            data.strikethrough,
            data.fontAxisIds,
            data.fontAxisValues,
            data.autosize,
            data.flags,
        )
        data.modifier.writeTo(this)
        delegate.buffer.addContentStart()
    }

    override fun endText() {
        delegate.buffer.addContainerEnd()
        delegate.buffer.addContainerEnd()
    }

    override fun image(data: RemoteImageData) {
        delegate.buffer.addImage(
            data.modifier.componentId,
            -1,
            data.bitmapId,
            data.scaleType,
            data.alpha,
        )
        data.modifier.writeTo(this)
        delegate.buffer.addContainerEnd()
    }

    override fun setNamedVariable(id: Int, name: String, type: Int) =
        delegate.setNamedVariable(id, name, type)

    override fun addInteger(value: Int): Long = delegate.addInteger(value)

    override fun addNamedInt(name: String, initialValue: Int): Long =
        delegate.addNamedInt(name, initialValue)

    override fun addNamedFloat(name: String, initialValue: Float): Float =
        delegate.addNamedFloat(name, initialValue)

    override fun reserveFloatVariable(): Float = delegate.reserveFloatVariable()

    override fun addFloatArray(values: FloatArray): Float = delegate.addFloatArray(values)

    override fun addIdList(ids: IntArray): Float = delegate.addList(ids)

    override fun addDynamicFloatArray(size: Float): Float = delegate.addDynamicFloatArray(size)

    override fun setArrayValue(id: Int, index: Float, value: Float) =
        delegate.setArrayValue(id, index, value)

    override fun floatExpression(vararg values: Float): Float = delegate.floatExpression(*values)

    override fun floatExpression(values: FloatArray, animation: FloatArray?): Float =
        delegate.floatExpression(values, animation)

    override fun integerExpression(vararg values: Long): Long = delegate.integerExpression(*values)

    override fun timeAttribute(longId: Int, type: Short, vararg args: Int): Float =
        delegate.timeAttribute(longId, type, *args)

    override fun idLookup(arrayId: Float, index: Float): Int = delegate.idLookup(arrayId, index)

    override fun textLookup(arrayId: Float, index: Float): Int = delegate.textLookup(arrayId, index)

    override fun textLookup(arrayId: Float, indexId: Int): Int =
        delegate.textLookup(arrayId, indexId)

    override fun textLength(textId: Int): Float = delegate.textLength(textId)

    override fun textMerge(leftId: Int, rightId: Int): Int = delegate.textMerge(leftId, rightId)

    override fun textSubtext(textId: Int, start: Float, length: Float): Int =
        delegate.textSubtext(textId, start, length)

    override fun textTransform(textId: Int, start: Float, length: Float, operation: Int): Int =
        delegate.textTransform(textId, start, length, operation)

    override fun createTextFromFloat(value: Float, before: Int, after: Int, flags: Int): Int =
        delegate.createTextFromFloat(value, before, after, flags)

    override fun colorAttribute(colorId: Int, type: Short): Float =
        delegate.getColorAttribute(colorId, type)

    override fun bitmapAttribute(bitmapId: Int, type: Short): Float =
        delegate.bitmapAttribute(bitmapId, type)

    override fun bitmapTextMeasure(
        textId: Int,
        bitmapFontId: Int,
        type: Int,
        glyphSpacing: Float,
    ): Float = delegate.bitmapTextMeasure(textId, bitmapFontId, type, glyphSpacing)

    override fun addBitmapUrl(url: String, width: Int, height: Int): Int =
        delegate.addBitmapUrl(url, width, height)

    override fun addNamedBitmapUrl(name: String, url: String): Int =
        delegate.addNamedBitmapUrl(name, url)

    override fun createBitmap(width: Int, height: Int): Int = delegate.createBitmap(width, height)

    override fun addBitmapFont(
        glyphs: List<BitmapFontGlyph>,
        kerningTable: Map<String, Short>,
    ): Int =
        delegate.addBitmapFont(
            glyphs
                .map { glyph ->
                    androidx.compose.remote.core.operations.BitmapFontData.Glyph(
                        glyph.chars,
                        glyph.bitmapId,
                        glyph.marginLeft,
                        glyph.marginTop,
                        glyph.marginRight,
                        glyph.marginBottom,
                        glyph.width,
                        glyph.height,
                    )
                }
                .toTypedArray(),
            kerningTable,
        )

    override fun matrixExpression(vararg expression: Float): Float =
        delegate.matrixExpression(*expression)

    override fun addColor(argb: Int): Int = delegate.addColor(argb)

    override fun addNamedColor(name: String, argb: Int): Int = delegate.addNamedColor(name, argb)

    override fun addColorExpression(color1: Int, color2: Int, tween: Float): Short =
        delegate.addColorExpression(color1, color2, tween)

    override fun addColorExpression(color1: Short, color2: Short, tween: Float): Short =
        delegate.addColorExpression(color1, color2, tween)

    override fun addColorExpression(
        alpha: Int,
        hue: Float,
        saturation: Float,
        value: Float,
    ): Short = delegate.addColorExpression(alpha, hue, saturation, value)

    override fun addColorExpression(alpha: Float, red: Float, green: Float, blue: Float): Short =
        delegate.addColorExpression(alpha, red, green, blue)

    override fun addText(value: String): Int = delegate.addText(value)

    override fun addNamedString(name: String, initialValue: String): Int =
        delegate.addNamedString(name, initialValue)

    override fun addLong(value: Long): Int = delegate.addLong(value)

    override fun addNamedLong(name: String, initialValue: Long): Int =
        delegate.addNamedLong(name, initialValue)

    override fun addPathData(pathData: FloatArray, winding: Int): Int {
        return delegate.addPathData(pathData, winding)
    }

    override fun applyPaint(paint: PaintBundleData) {
        val values = paint.toIntArray()
        delegate.buffer.buffer.start(40)
        delegate.buffer.buffer.writeInt(values.size)
        values.forEach(delegate.buffer.buffer::writeInt)
    }

    override fun consumePaintReset(): Boolean = delegate.checkAndClearForceSendingNewPaint()

    override fun save() = delegate.save()

    override fun restore() = delegate.restore()

    override fun translate(dx: Float, dy: Float) = delegate.translate(dx, dy)

    override fun scale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) =
        delegate.scale(scaleX, scaleY, centerX, centerY)

    override fun rotate(angle: Float, centerX: Float, centerY: Float) =
        delegate.rotate(angle, centerX, centerY)

    override fun skew(skewX: Float, skewY: Float) = delegate.skew(skewX, skewY)

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) =
        delegate.clipRect(left, top, right, bottom)

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float) =
        delegate.drawRect(left, top, right, bottom)

    override fun drawRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radiusX: Float,
        radiusY: Float,
    ) = delegate.drawRoundRect(left, top, right, bottom, radiusX, radiusY)

    override fun drawCircle(centerX: Float, centerY: Float, radius: Float) =
        delegate.drawCircle(centerX, centerY, radius)

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float) =
        delegate.drawOval(left, top, right, bottom)

    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
    ) = delegate.drawArc(left, top, right, bottom, startAngle, sweepAngle)

    override fun drawSector(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
    ) = delegate.drawSector(left, top, right, bottom, startAngle, sweepAngle)

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) =
        delegate.drawLine(x1, y1, x2, y2)

    override fun drawPath(pathId: Int) = delegate.drawPath(pathId)

    override fun clipPath(pathId: Int) = delegate.addClipPath(pathId)

    override fun drawTweenPath(
        path1Id: Int,
        path2Id: Int,
        tween: Float,
        start: Float,
        stop: Float,
    ) = delegate.drawTweenPath(path1Id, path2Id, tween, start, stop)

    override fun drawTextOnPath(
        textId: Int,
        pathId: Int,
        horizontalOffset: Float,
        verticalOffset: Float,
    ) = delegate.drawTextOnPath(textId, pathId, horizontalOffset, verticalOffset)

    override fun drawTextRun(
        textId: Int,
        start: Int,
        end: Int,
        contextStart: Int,
        contextEnd: Int,
        x: Float,
        y: Float,
        isRtl: Boolean,
    ) = delegate.drawTextRun(textId, start, end, contextStart, contextEnd, x, y, isRtl)

    override fun drawTextAnchored(
        textId: Int,
        anchorX: Float,
        anchorY: Float,
        panX: Float,
        panY: Float,
        flags: Int,
    ) = delegate.drawTextAnchored(textId, anchorX, anchorY, panX, panY, flags)

    override fun drawTextOnCircle(
        textId: Int,
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Float,
        warpRadiusOffset: Float,
        alignment: DrawTextOnCircle.Alignment,
        placement: DrawTextOnCircle.Placement,
    ) =
        delegate.drawTextOnCircle(
            textId,
            centerX,
            centerY,
            radius,
            startAngle,
            warpRadiusOffset,
            androidx.compose.remote.core.operations.DrawTextOnCircle.Alignment.values()[
                alignment.ordinal],
            androidx.compose.remote.core.operations.DrawTextOnCircle.Placement.values()[
                placement.ordinal],
        )

    override fun drawScaledBitmap(
        imageId: Int,
        srcLeft: Float,
        srcTop: Float,
        srcRight: Float,
        srcBottom: Float,
        dstLeft: Float,
        dstTop: Float,
        dstRight: Float,
        dstBottom: Float,
        scaleType: Int,
        scaleFactor: Float,
        contentDescription: String,
    ) =
        delegate.drawScaledBitmap(
            imageId,
            srcLeft,
            srcTop,
            srcRight,
            srcBottom,
            dstLeft,
            dstTop,
            dstRight,
            dstBottom,
            scaleType,
            scaleFactor,
            contentDescription,
        )
}
