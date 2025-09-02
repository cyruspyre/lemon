package com.cyruspyre.lemon.tabs

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.R
import com.cyruspyre.lemon.SplitView
import com.cyruspyre.lemon.databinding.TabBinding
import com.cyruspyre.lemon.entity.EntityView
import kotlin.io.path.name

class TabAdapter(
    private val list: List<SplitView<EntityView>>,
) : RecyclerView.Adapter<TabAdapter.ViewHolder>() {
    data class ViewHolder(val binding: TabBinding) : RecyclerView.ViewHolder(binding.root)

    var active = 0
    private var prevHover = -1
    private lateinit var context: Context
    private lateinit var home: Drawable
    private lateinit var folder: Drawable
    private lateinit var inflater: LayoutInflater

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        inflater = LayoutInflater.from(context)
        folder = AppCompatResources.getDrawable(context, R.drawable.folder_24px)!!
        home = AppCompatResources.getDrawable(context, R.drawable.home)!!
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val tab = TabBinding.inflate(inflater, parent, false)
        val root = tab.root
        val holder = ViewHolder(tab)

        root.background = TabRipple(
            context, TabShape(AppCompatResources.getColorStateList(context, R.color.tab))
        )
        root.isActive = {
            val pos = holder.absoluteAdapterPosition

            if (prevHover != pos) {
                notifyItemChanged(prevHover, Unit)
                notifyItemChanged(pos, Unit)
                prevHover = pos
            }

            pos == active
        }

        root.setOnClickListener {
            val idx = holder.absoluteAdapterPosition

            if (active != idx) {
                val tmp = active

                active = idx

                notifyItemChanged(tmp, Unit)
                notifyItemChanged(idx, Unit)
                onTabChange(list[idx])
            }
        }

        return holder
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val tab = holder.binding
        val root = tab.root
        val path = list[position].active.path
        val active = position == active
        val isHome = path == null

        root.isSelected = active
        root.elevation = if (active) 2f else if (root.isHovered) 1f else 0f
        tab.icon.background = if (isHome) home else folder
        tab.label.text = if (isHome) "Home" else path.name
        tab.button.visibility = if (active || root.isHovered) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount() = list.size
}