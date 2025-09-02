package com.cyruspyre.lemon.tabs

import android.R.attr.state_hovered
import android.R.attr.state_selected
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import androidx.appcompat.content.res.AppCompatResources
import com.cyruspyre.lemon.R
import com.cyruspyre.lemon.dp

internal class TabRipple(context: Context, shape: Drawable) : RippleDrawable(
    AppCompatResources.getColorStateList(context, R.color.tab_ripple),
    shape, shape,
) {
    var hovered = false

    override fun onBoundsChange(bounds: Rect) {
        bounds.right += 24.dp
        super.onBoundsChange(bounds)
    }

    override fun onStateChange(stateSet: IntArray): Boolean {
        var active = false
        var hovered = false

        for (v in stateSet) when (v) {
            state_hovered -> hovered = true
            state_selected -> {
                active = true
                break
            }
        }

        if (hovered != this.hovered || active) {
            if (active) hovered = true
            if (hovered) {
                bounds.right = 221.dp
                bounds.left = -1
            } else {
                bounds.right = 220.dp
                bounds.left = 0
            }

            this.hovered = hovered
            onBoundsChange(bounds)
        }

        return super.onStateChange(stateSet)
    }
}