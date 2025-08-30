package com.cyruspyre.lemon.entity

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.cyruspyre.lemon.R
import java.util.BitSet
import kotlin.math.sign
import kotlin.time.TimeSource

private val TIME = TimeSource.Monotonic

abstract class EntityAdapter<T : ViewBinding> :
    RecyclerView.Adapter<EntityAdapter.ViewHolder<T>>() {
    data class ViewHolder<T : ViewBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)

    internal lateinit var entityView: EntityView
    protected lateinit var context: Context
    protected lateinit var inflater: LayoutInflater

    private companion object {
        val active = BitSet()
        var stamp = -1
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        active.clear()

        context = recyclerView.context
        inflater = LayoutInflater.from(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder<T> {
        val binding = inflate(parent)
        val root = binding.root
        val holder = ViewHolder(binding)

        root.isClickable = true
        root.background = AppCompatResources.getDrawable(parent.context, R.drawable.entity)

        var button: Int
        var mark = TIME.markNow()

        root.setOnTouchListener { _, event ->
            val idx = holder.adapterPosition

            if (event.action == MotionEvent.ACTION_DOWN) button = event.buttonState
            else return@setOnTouchListener false

            if (event.metaState and KeyEvent.META_SHIFT_ON != 0 && idx != stamp && (stamp != -1 || !active.isEmpty)) {
                var cur = idx
                val sign = (stamp - cur).sign

                while (cur != stamp) {
                    if (!active.get(cur)) {
                        active.set(cur)
                        notifyItemChanged(cur, Unit)
                    }

                    cur += sign
                }

                val first = active.nextSetBit(0)
                val last = active.length() - 1
                val flag = sign == 1
                var one = if (flag) last else first

                while (one != stamp) {
                    active.clear(one)
                    notifyItemChanged(one, Unit)

                    one -= sign
                }

                var two = if (flag) first else last

                while (two != idx) {
                    active.clear(two)
                    notifyItemChanged(two, Unit)

                    two += sign
                }
            } else {
                val ctrl = event.metaState and KeyEvent.META_CTRL_ON != 0

                if (!ctrl) {
                    clearActive()
                }
                if (active.isEmpty) stamp = idx
                if (active.get(idx)) active.clear(idx) else active.set(idx)

                notifyItemChanged(idx, Unit)
            }

            val now = TIME.markNow()

            when (button) {
                MotionEvent.BUTTON_SECONDARY -> println("right click")
                MotionEvent.BUTTON_TERTIARY -> println("middle click")
                else if (now - mark).inWholeMilliseconds <= 500 -> onOpen(idx)
                else -> {
                    mark = now
                }
            }

            true
        }

        return holder
    }

    fun clearActive() {
        var idx = active.nextSetBit(0)

        while (idx != -1) {
            active.clear(idx)
            notifyItemChanged(idx, Unit)

            idx = active.nextSetBit(idx + 1)
        }
    }

    @CallSuper
    override fun onBindViewHolder(
        holder: ViewHolder<T>,
        position: Int,
    ) {
        holder.binding.root.isSelected = active.get(position)
    }

    internal abstract fun onOpen(idx: Int)
    internal abstract fun inflate(parent: ViewGroup): T
    abstract override fun getItemCount(): Int
}