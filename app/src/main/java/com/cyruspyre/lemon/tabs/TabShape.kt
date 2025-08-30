package com.cyruspyre.lemon.tabs

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.cyruspyre.lemon.dp

internal class TabShape(val colors: ColorStateList) : Drawable() {
    lateinit var path: Path
    val paint = Paint()
    val radius = 12f.dp
    val diameter = radius * 2

    override fun draw(canvas: Canvas) = canvas.drawPath(path, paint)

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        val circle = Path().apply { addCircle(0f, 0f, radius, Path.Direction.CW) }
        val side = Path().apply {
            addRect(0f, 0f, radius, height - radius, Path.Direction.CW)
        }

        path = Path().apply {
            addRect(
                0f, radius,
                width, height,
                Path.Direction.CW,
            )

            op(circle.apply { offset(0f, height - radius) }, Path.Op.DIFFERENCE)
            op(side, Path.Op.DIFFERENCE)
            op(circle.apply { offset(diameter, -height + diameter) }, Path.Op.UNION)
            op(
                Path().apply {
                    addRect(diameter, 0f, width - diameter, radius, Path.Direction.CW)
                }, Path.Op.UNION
            )
            op(circle.apply { offset(width - diameter * 2, 0f) }, Path.Op.UNION)
            op(side.apply { offset(width - radius, 0f) }, Path.Op.DIFFERENCE)
            op(circle.apply { offset(diameter, height - diameter) }, Path.Op.DIFFERENCE)
        }
    }

    override fun isStateful() = true

    override fun onStateChange(state: IntArray): Boolean {
        paint.color = colors.getColorForState(state, colors.defaultColor)
        invalidateSelf()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSPARENT
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
}