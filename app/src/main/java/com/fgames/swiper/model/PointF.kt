package com.fgames.swiper.model

class PointF(val x: Float, val y: Float) {
    fun inverse() = PointF(x * -1, y * -1)
    fun concat(point: PointF) = PointF(x + point.x, y + point.y)
}