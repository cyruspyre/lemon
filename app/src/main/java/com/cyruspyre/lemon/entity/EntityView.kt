package com.cyruspyre.lemon.entity

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.R
import com.cyruspyre.lemon.SplitView
import com.cyruspyre.lemon.onPathChange
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import java.nio.file.Path

@SuppressLint("ViewConstructor")
class EntityView(context: Context, split: SplitView<EntityView>) : FrameLayout(context) {
    private val onNotify = split.addView(this)
    val recycler = RecyclerView(context).apply {
        @SuppressLint("ClickableViewAccessibility") setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) (adapter as EntityAdapter<*>).clearActive()
            false
        }

        layoutManager = FlexLayoutManager(context)

        this@EntityView.addView(this)
    }
    internal val indicator = LinearProgressIndicator(context).apply {
        visibility = INVISIBLE
        isIndeterminate = true

        addView(this)
    }

    internal val special = MaterialTextView(context).apply {
        visibility = INVISIBLE
        gravity = Gravity.CENTER

        setText(R.string.access_denied)
        addView(this)
    }
    val stack = mutableListOf<Path?>()
    val path get() = stack[idx]
    var idx = 0

    init {
        isClickable = true

        setBackgroundResource(R.drawable.entity_view)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        onNotify()
        return super.dispatchTouchEvent(ev)
    }

    fun update() {
        val path = path
        val adapter = recycler.adapter

        onPathChange(path)

        recycler.adapter = if (path == null) VolumeAdapter()
        else if (adapter !is FileAdapter) FileAdapter(path).apply {
            entityView = this@EntityView
            load()
        } else {
            adapter.path = path

            adapter.clearActive()
            adapter.load()

            return
        }
    }
}