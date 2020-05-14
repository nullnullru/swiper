package com.fgames.swiper.ui.view.swiperview.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.fgames.swiper.BuildConfig
import com.fgames.swiper.model.*

class Cell(
    private val image: Bitmap,
    private val physSize: SizeF,
    private val fieldSize: Size,
    var position: Point
) {
    val initPosition = Point(position.x, position.y)

    fun onDraw(canvas: Canvas, offset: SizeF, paint: Paint? = null, initPosition: Boolean = false) {
        var position = PointF(
            position.x * physSize.w + offset.w,
            position.y * physSize.h + offset.h
        )

        if(initPosition) {
            val initOffset = getInitOffset()
            position = position.concat(PointF(initOffset.w, initOffset.h))
        }

        canvas.drawBitmap(image, position.x, position.y, paint)
    }

    fun move(direction: Direction, orientation: Orientation) {
        val vector = if(direction == Direction.END) 1 else -1

        position = if(orientation == Orientation.HORIZONTAL) {
            var newX = position.x + vector
            newX = if(newX < 0) fieldSize.w - 1 else newX % fieldSize.w
            Point(newX, position.y)
        } else {
            var newY = position.y + vector
            newY = if(newY < 0) fieldSize.h - 1 else newY % fieldSize.h
            Point(position.x, newY)
        }
    }

    private fun getInitOffset() = SizeF(
        (initPosition.x - position.x) * physSize.w,
        (initPosition.y - position.y) * physSize.h
    )

    fun getPhysCenter(physOffset: SizeF) = PointF(
        position.x * physSize.w + physOffset.w + physSize.w / 2,
        position.y * physSize.h + physOffset.h + physSize.h / 2
    )
}