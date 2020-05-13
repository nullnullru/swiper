package com.fgames.swiper.ui.view.swiperview.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.fgames.swiper.model.*

class Cell(
    private val image: Bitmap,
    private val physicalSize: SizeF,
    private val fieldSize: Size,
    var position: Point
) {
    fun onDraw(canvas: Canvas, offset: SizeF, paint: Paint? = null) {
        canvas.drawBitmap(
            image,
            position.x * physicalSize.w + offset.w,
            position.y * physicalSize.h + offset.h,
            paint
        )
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
}