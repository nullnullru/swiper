package com.fgames.swiper.utils

import android.view.View
import androidx.databinding.BindingAdapter
import com.fgames.swiper.R

class ViewBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter(value = ["hasStroke"])
        fun hasStroke(view: View, has: Boolean) {
            if(has) {
                view.setBackgroundResource(R.drawable.bg_stroke)
            } else {
                view.background = null
            }
        }
    }
}