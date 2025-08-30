package com.cyruspyre.lemon.tabs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.SplitView
import com.cyruspyre.lemon.changePath
import com.cyruspyre.lemon.dp
import com.cyruspyre.lemon.entity.EntityView
import com.google.android.material.R.attr.colorSurface
import com.google.android.material.R.attr.materialIconButtonStyle
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import java.nio.file.Path

internal lateinit var onTabChange: (SplitView<EntityView>) -> Unit

class Tabs(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private val list = mutableListOf<SplitView<EntityView>>()
    val active get() = list[adapter.active]
    val adapter = TabAdapter(list)
    private val recycler = RecyclerView(context).apply {
        adapter = this@Tabs.adapter
        overScrollMode = OVER_SCROLL_NEVER
        layoutManager = LinearLayoutManager(context).apply {
            clipChildren = false
            orientation = LinearLayoutManager.HORIZONTAL
        }

        setOnScrollChangeListener { _, _, _, _, _ ->
            shade.visibility = if (canScrollHorizontally(1)) VISIBLE else INVISIBLE
        }
        addItemDecoration(TabDivider(context))
        this@Tabs.addView(this)
    }
    private val shade = View(context).apply {
        setBackgroundColor(MaterialColors.getColor(this, colorSurface))
        addView(this)
    }
    private val button = MaterialButton(context, null, materialIconButtonStyle).apply {
        cornerRadius = 12.dp

        setOnClickListener { addTab() }
        setIconResource(com.cyruspyre.lemon.R.drawable.add)
        addView(this)
    }

    init {
        clipChildren = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        button.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = button.measuredHeight

        recycler.measure(
            MeasureSpec.makeMeasureSpec(width - button.measuredWidth - 12.dp, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = recycler.measuredWidth

        recycler.layout(0, 0, width, b)
        button.layout(width + 12.dp, 0, width + button.measuredWidth + 12.dp, b)
        shade.layout(button.left, 0, r, b)
    }

    fun addTab(path: Path? = null) {
        val idx = list.size
        val split = SplitView<EntityView>(context)

        EntityView(context, split)

        list.add(split)
        adapter.apply {
            notifyItemInserted(idx)
            notifyItemChanged(active, Unit)
            active = idx
            changePath(path)
        }
        onTabChange(split)
    }
}