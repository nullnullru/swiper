package com.fgames.swiper.model

class Point(val x: Int, val y: Int) {
    fun concat(point: Point) = Point(x + point.x, y + point.y)

    override fun equals(other: Any?): Boolean {
        if(other is Point) {
            return other.x == this.x && other.y == this.y
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}