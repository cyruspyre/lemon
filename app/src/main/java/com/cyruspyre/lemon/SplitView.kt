package com.cyruspyre.lemon

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.PointerIcon
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.children
import com.google.android.material.R.attr.colorOnSurfaceVariant
import com.google.android.material.R.attr.colorOutline
import com.google.android.material.R.attr.colorPrimary
import com.google.android.material.color.MaterialColors.getColor
import kotlin.math.roundToInt

class SplitView<T : View>(context: Context) : ViewGroup(context) {
    private val resizePaint = arrayOf(
        Paint().apply {
            color = getColor(this@SplitView, colorOnSurfaceVariant)
            alpha = 25
            style = Paint.Style.FILL
        },
        Paint().apply {
            color = getColor(this@SplitView, colorOutline)
            style = Paint.Style.STROKE
        },
    )
    private val resizeAnimator = ValueAnimator.ofInt(0, 255).apply {
        interpolator = OvershootInterpolator()
        addUpdateListener {
            resizePaint[1].alpha = animatedValue as Int
            invalidate()
        }
    }
    private val activePaint = Paint().apply {
        color = getColor(this@SplitView, colorPrimary)
        style = Paint.Style.STROKE
    }
    private val activeAnimator = ValueAnimator.ofInt(0, 255).apply {
        interpolator = OvershootInterpolator()
        addUpdateListener {
            activePaint.alpha = animatedValue as Int
            invalidate()
        }
    }
    private val pointer = arrayOf(
        PointerIcon.getSystemIcon(context, PointerIcon.TYPE_ARROW),
        PointerIcon.getSystemIcon(context, PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW),
    )
    private var heightMeasureSpec = 0
    private var stamp = 0f
    private var prevX = 0
    private var idx = -1
    private lateinit var pair: Array<View>
    lateinit var active: T

    init {
        isClickable = true
    }

    fun addView(view: T): () -> Unit {
        active = view

        super.addView(view)

        return cb@{
            if (active == view) return@cb

            activeAnimator.start()

            active = view
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        this.heightMeasureSpec = makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val spaces = 8f.dp * (childCount - 1)
        val height = height
        val defaultWidth = ((width - spaces) / childCount).roundToInt()
        val width = width.toFloat()
        val diff = width != stamp
        var left = 0

        for (child in children) {
            var childWidth = child.measuredWidth

            if (childWidth == 0) {
                childWidth = defaultWidth
            } else if (diff) {
                childWidth = (childWidth / (stamp - spaces) * (width - spaces)).roundToInt()
            }

            child.measure(makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), heightMeasureSpec)
            child.layout(left, 0, left + childWidth, height)

            left += childWidth + 8.dp
        }

        if (diff) stamp = width
    }

    override fun onHoverChanged(hovered: Boolean) {
        pointerIcon = pointer[hovered.compareTo(false)]
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()

        if (event.action == MotionEvent.ACTION_DOWN) {
            for (i in 1..<childCount) {
                val rng = getChildAt(i - 1).right..getChildAt(i).left

                if (rng.contains(x)) {
                    idx = i
                    prevX = x
                    pair = arrayOf(getChildAt(i - 1), getChildAt(i))

                    resizeAnimator.start()
                    onTouchEvent(event.apply { action = MotionEvent.ACTION_MOVE })

                    break
                }
            }
        } else if (event.action == MotionEvent.ACTION_MOVE && idx > -1) {
            val (one, two) = pair
            val delta = x - prevX
            val side = delta > -1
            val oneWidth = one.measuredWidth + delta
            val twoWidth = two.measuredWidth - delta
            val smaller = if (side) twoWidth else oneWidth

            if (smaller < 96.dp) {
                val left: Int
                val right: Int
                val child: View

                if (side) {
                    child = one
                    left = one.left
                    right = two.right
                } else {
                    child = two
                    left = one.left
                    right = two.right
                }

                child.measure(makeMeasureSpec(right - left, MeasureSpec.EXACTLY), heightMeasureSpec)
                child.layout(left, 0, right, height)
                removeViewAt(idx + side.compareTo(true))

                idx = -1

                return true
            }

            prevX = x

            one.measure(makeMeasureSpec(oneWidth, MeasureSpec.EXACTLY), heightMeasureSpec)
            two.measure(makeMeasureSpec(twoWidth, MeasureSpec.EXACTLY), heightMeasureSpec)
            invalidate()

            // note: allows realtime preview of resizing with its actual content at the cost of potential ANR
            // one.layout(one.left, 0, one.left + oneWidth, height)
            // two.layout(two.left + delta, 0, two.right, height)
        } else if (event.action == MotionEvent.ACTION_UP) {
            idx = -1
            resizeAnimator.reverse()
            pair[0].run { layout(left, 0, left + measuredWidth, height) }
            pair[1].run { layout(right - measuredWidth, 0, right, height) }
        }

        return true
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        if (idx != -1) run {
            val left: Int
            val right: Int

            when (child) {
                pair[0] -> {
                    left = child.left
                    right = left + child.measuredWidth
                }

                pair[1] -> {
                    right = child.right
                    left = right - child.measuredWidth
                }

                else -> return@run
            }

            for (paint in resizePaint) {
                canvas.drawRoundRect(
                    left + 1f.dp,
                    child.top + 1f.dp,
                    right - 1f.dp,
                    child.bottom - 1f.dp,
                    8f.dp,
                    8f.dp,
                    paint,
                )
            }

            return false
        }

        val res = super.drawChild(canvas, child, drawingTime)

        if (childCount > 1 && child == active) child.run {
            canvas.drawRoundRect(
                left + 1f.dp,
                top + 1f.dp,
                right - 1f.dp,
                bottom - 1f.dp,
                8f.dp,
                8f.dp,
                activePaint,
            )
        }

        return res
    }
}