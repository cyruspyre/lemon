package com.cyruspyre.lemon

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyruspyre.lemon.databinding.NavBarBinding
import com.cyruspyre.lemon.databinding.PathSegmentBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.nio.file.Path

lateinit var onPathChange: (Path?) -> Unit

fun navBar(context: Context, binding: NavBarBinding): (Path?) -> Unit {
    val field = binding.pathBar.getChildAt(0) as TextInputEditText
    val recycler = binding.pathBar.getChildAt(1) as RecyclerView
    val adapter = Adapter()

    binding.back.setOnClickListener { goBack() }
    binding.up.setOnClickListener {
        val tmp = active().path?.parent
        if (tmp != null) changePath(tmp)
    }
    binding.forward.setOnClickListener {
        val tmp = active()

        if (tmp.idx < tmp.stack.lastIndex) {
            tmp.idx++
            tmp.update()
        }
    }
    recycler.apply {
        this.adapter = adapter
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    return { adapter.updatePath(it) }
}

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
    data class ViewHolder(val btn: MaterialButton) : RecyclerView.ViewHolder(btn)

    var path: Path? = null
    private var root = "/"
    private var offset = 0
    private lateinit var home: Drawable
    private lateinit var context: Context
    private lateinit var separator: Drawable
    private lateinit var inflater: LayoutInflater

    fun updatePath(path: Path?) {
        if (path != null) tmp@ for (vol in VOLUMES) {
            val one = path
            val two = vol.directory!!.toPath()
            val count = if (one.nameCount >= two.nameCount) two.nameCount else continue

            for (i in 0..<count) {
                if (two.getName(i) != one.getName(i)) continue@tmp
            }

            root = vol.getDescription(context)
            offset = count
            break
        }

        this.path = path
        @SuppressLint("NotifyDataSetChanged") notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        inflater = LayoutInflater.from(context)
        home = AppCompatResources.getDrawable(context, R.drawable.home)!!
        separator = AppCompatResources.getDrawable(context, R.drawable.right)!!
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val btn = PathSegmentBinding.inflate(inflater, parent, false).root
        val padding = if (viewType == 0) 6.dp else 12.dp

        btn.updatePadding(left = padding, right = padding)

        return ViewHolder(btn)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val btn = holder.btn

        if (position == 0) {
            btn.icon = home
            btn.text = null
        } else if (position == 2) {
            btn.text = root
        } else if ((position + 1) % 2 != 0) {
            btn.icon = null
            btn.text = path!!.getName(offset + (position - 3) / 2).toString()
        } else {
            btn.icon = separator
        }
    }

    override fun getItemViewType(position: Int) = if (position == 0) 2 else (position + 1) % 2
    override fun getItemCount() = if (path == null) 2 else (path!!.nameCount - offset) * 2 + 4
}