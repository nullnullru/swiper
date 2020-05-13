package com.fgames.swiper.model

class BoundsF(val point: PointF, val size: SizeF) {
    fun has(point: PointF) =
        point.x >= this.point.x
                && point.y >= this.point.y
                && point.x <= this.point.x + size.w
                && point.y <= this.point.y + size.h
}