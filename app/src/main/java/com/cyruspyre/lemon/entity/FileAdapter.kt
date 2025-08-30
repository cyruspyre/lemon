package com.cyruspyre.lemon.entity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.BlockList
import com.cyruspyre.lemon.FileInfo
import com.cyruspyre.lemon.R
import com.cyruspyre.lemon.WATCH_DOG
import com.cyruspyre.lemon.changePath
import com.cyruspyre.lemon.databinding.FileBinding
import java.nio.file.Path

class FileAdapter(var path: Path) : EntityAdapter<FileBinding>() {
    private lateinit var file: Drawable
    private lateinit var folder: Drawable
    private lateinit var list: BlockList<FileInfo>
    private var size = 0

    internal fun load() {
        var res: BlockList<FileInfo>? = null

        res = WATCH_DOG.load(
            path, FileInfo.Order.Type, this,
            { notifyItemInserted(size++) },
            {
                entityView.indicator.apply {
                    isIndeterminate = true
                    show()
                }
            },
        ) { cached ->
            val flag = res == null

            entityView.special.visibility = if (flag) {
                size = 0
                VISIBLE
            } else {
                size = list.size
                INVISIBLE
            }

            if (cached || flag) @SuppressLint("NotifyDataSetChanged") notifyDataSetChanged()

            entityView.indicator.hide()
        }

        if (res != null) {
            size = res.size
            list = res
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        file = AppCompatResources.getDrawable(context, R.drawable.file)!!
        folder = AppCompatResources.getDrawable(context, R.drawable.folder_48px)!!
    }

    override fun onBindViewHolder(holder: ViewHolder<FileBinding>, position: Int) {
        super.onBindViewHolder(holder, position)

        val info = list[position]

        holder.binding.apply {
            label.text = info.name
            icon.setImageDrawable(if (info.isDir) folder else file)
        }
    }

    override fun onOpen(idx: Int) {
        val info = list[idx]

        if (info.isDir) changePath(path.resolve(info.name))
    }

    override fun inflate(parent: ViewGroup) = FileBinding.inflate(inflater, parent, false)
    override fun getItemCount() = size
}