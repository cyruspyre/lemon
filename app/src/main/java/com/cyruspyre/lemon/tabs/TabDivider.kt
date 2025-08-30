package com.cyruspyre.lemon.tabs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.dp
import com.google.android.material.R.attr.colorOutlineVariant
import com.google.android.material.color.MaterialColors

internal class TabDivider(context: Context) : RecyclerView.ItemDecoration() {
    val paint = Paint().apply {
        color = MaterialColors.getColor(context, colorOutlineVariant, null)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0..<parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val left = child.right.toFloat() + 12f.dp

            canvas.drawRect(left, 12f.dp, left + 1f.dp, child.height - 12f.dp, paint)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        outRect.right = 1.dp
    }
}