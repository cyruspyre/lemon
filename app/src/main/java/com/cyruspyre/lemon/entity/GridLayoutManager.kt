package com.cyruspyre.lemon.entity

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.binarySearch
import com.cyruspyre.lemon.dp
import com.cyruspyre.lemon.either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class GridLayoutManager : RecyclerView.LayoutManager() {
    var offset = 0
    private val rows = mutableListOf<Int>()
    private var span = 0
    private var spanWidth = 0
    private var spanStamp = 0

    companion object {
        private var measureJob: Job? = null
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        val tmp = offset

        offset = (offset + dy).coerceIn(0, ((rows.lastOrNull() ?: 0) - height).coerceAtLeast(0))
        onLayoutChildren(recycler, state)

        return offset - tmp
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int,
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        val width = View.MeasureSpec.getSize(widthSpec).toFloat() - 12f.dp

        span = (width / 110f.dp).roundToInt()
        spanWidth = (width / span).roundToInt()

        if (span != spanStamp) {
            rows.clear()
            spanStamp = span
        }

        measureJob?.cancel()
        measureJob = CoroutineScope(Dispatchers.Unconfined).launch {
            var maxHeight = 0
            var count = 0

            for (i in rows.size * span..<state.itemCount) {
                val child = recycler.getViewForPosition(i)

                measureChild(child, 0, 0)

                val height = child.measuredHeight

                if (height > maxHeight) maxHeight = height
                if (++count == span) {
                    rows.add((rows.lastOrNull() ?: 0) + maxHeight + 12.dp)

                    maxHeight = 0
                    count = 0
                }
            }

            measureJob = null
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.isPreLayout || rows.isEmpty()) return

        detachAndScrapAttachedViews(recycler)

        var index = rows.binarySearch(offset).either()
        val first = rows.getOrElse(index - 1) { 0 }
        val start = index * span
        val end = ((height / 90.dp + 1) * span + start).coerceAtMost(state.itemCount)
        var count = 0
        var left = 0
        var top = first - offset

        for (i in start..<end) {
            val child = recycler.getViewForPosition(i)

            addView(child)
            measureChild(child, 0, 0)
            layoutDecorated(child, left + 12.dp, top, left + spanWidth, top + child.measuredHeight)

            left += spanWidth

            if (++count == span) {
                top = rows.getOrElse(index++) { 0 } - offset
                count = 0
                left = 0
            }
        }
    }

    override fun canScrollVertically() = true
    override fun generateDefaultLayoutParams() = null
}