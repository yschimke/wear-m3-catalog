package androidx.compose.remote.creation.compose.capture

import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.common.RemoteWriter
import androidx.compose.remote.creation.common.DrawTextOnCircle

/** Temporary adapter while the common Kotlin encoder replaces [RemoteComposeWriter]. */
internal class LegacyRemoteWriterAdapter(private val delegate: RemoteComposeWriter) : RemoteWriter {
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

    override fun addColor(argb: Int): Int = delegate.addColor(argb)

    override fun addNamedColor(name: String, argb: Int): Int = delegate.addNamedColor(name, argb)

    override fun addText(value: String): Int = delegate.addText(value)

    override fun addNamedString(name: String, initialValue: String): Int =
        delegate.addNamedString(name, initialValue)

    override fun addLong(value: Long): Int = delegate.addLong(value)

    override fun addNamedLong(name: String, initialValue: Long): Int =
        delegate.addNamedLong(name, initialValue)

    override fun addPathData(pathData: FloatArray, winding: Int): Int {
        require(winding == 0) { "The legacy writer adapter cannot encode non-zero path winding" }
        return delegate.addPathData(pathData)
    }

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
