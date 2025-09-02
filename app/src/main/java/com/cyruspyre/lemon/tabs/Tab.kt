package com.cyruspyre.lemon.tabs

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton

class Tab(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    private lateinit var btn: MaterialButton
    internal var isActive: () -> Boolean = { false }
        set(value) {
            btn = getChildAt(2) as MaterialButton
            field = value
        }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (!isActive()) {
            if (event.action == MotionEvent.ACTION_HOVER_MOVE) {
                btn.isHovered = (btn.left..btn.right)
                    .contains(event.x.toInt())
                    .and((btn.top..btn.bottom).contains(event.y.toInt()))
                return true
            }

            isHovered = event.action == MotionEvent.ACTION_HOVER_ENTER
            btn.visibility = if (isHovered) VISIBLE else INVISIBLE

            return true
        }

        return super.dispatchHoverEvent(event)
    }
}