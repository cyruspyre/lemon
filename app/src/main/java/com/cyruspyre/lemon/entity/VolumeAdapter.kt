package com.cyruspyre.lemon.entity

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.cyruspyre.lemon.VOLUMES
import com.cyruspyre.lemon.changePath
import com.cyruspyre.lemon.databinding.VolumeBinding
import com.cyruspyre.lemon.prettyByte

class VolumeAdapter : EntityAdapter<VolumeBinding>() {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder<VolumeBinding>,
        position: Int,
    ) {
        super.onBindViewHolder(holder, position)

        val vol = VOLUMES[position]
        val dir = vol.directory!!
        val binding = holder.binding
        val total = dir.totalSpace.toFloat()
        val used = total - dir.freeSpace

        binding.label.text = vol.getDescription(context)
        binding.stat.text = "${used.prettyByte} / ${total.prettyByte}"
        binding.indicator.progress = (used / total * 100).toInt()
    }

    override fun onOpen(idx: Int) = changePath(VOLUMES[idx].directory!!.toPath())
    override fun inflate(parent: ViewGroup) = VolumeBinding.inflate(inflater, parent, false)
    override fun getItemCount() = VOLUMES.size
}