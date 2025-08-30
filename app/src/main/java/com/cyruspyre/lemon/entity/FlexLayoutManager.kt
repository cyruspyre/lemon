package com.cyruspyre.lemon.entity

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.dp
import com.google.android.flexbox.FlexboxLayoutManager
import kotlin.math.round
import kotlin.math.roundToInt

class FlexLayoutManager(context: Context) : FlexboxLayoutManager(context) {
    private var childWidth = 0

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount > 0) {
            val child = recycler.getViewForPosition(0)

            measureChildWithMargins(child, 0, 0)

            val width = width - 12f.dp
            val count = round(width / (child.measuredWidth + 12f.dp))

            childWidth = ((width - count * 12f.dp) / count).roundToInt()
        }

        super.onLayoutChildren(recycler, state)
    }

    override fun getChildWidthMeasureSpec(
        widthSpec: Int,
        padding: Int,
        childDimension: Int,
    ) = super.getChildWidthMeasureSpec(widthSpec, padding, childWidth)
}