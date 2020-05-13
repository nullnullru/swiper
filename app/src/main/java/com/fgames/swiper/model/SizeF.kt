package com.fgames.swiper.model

class SizeF(val w: Float, val h: Float) {
    fun concat(size: SizeF) = SizeF(w + size.w, h + size.h)
    fun inverse() = SizeF(w * -1, h * -1)
    companion object {
        val empty = SizeF(0f, 0f)
    }
}