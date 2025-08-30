package com.cyruspyre.lemon.tabs

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import androidx.appcompat.content.res.AppCompatResources
import com.cyruspyre.lemon.R

internal class TabRipple(context: Context, shape: Drawable) : RippleDrawable(
    AppCompatResources.getColorStateList(context, R.color.tab_ripple),
    shape, shape,
) {
    override fun onBoundsChange(bounds: Rect) {
        bounds.right += 24
        super.onBoundsChange(bounds)
    }
}