package com.fgames.swiper.ui.view.swiperview.model

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.animation.doOnEnd
import com.fgames.swiper.model.Direction
import com.fgames.swiper.model.Orientation
import com.fgames.swiper.model.PointF
import com.fgames.swiper.model.SizeF
import kotlin.math.roundToInt

class FieldLine(
    val cells: List<Cell>,
    physCellSize: SizeF,
    private val orientation: Orientation,
    private val drawRequest: () -> Unit
) {

    val mainValue = if (orientation == Orientation.HORIZONTAL) physCellSize.w else physCellSize.h
    private var phantomPaint: Paint = Paint()
    var physOffset: Float = 0f
        set(value) {
            field = value
            checkOffset()
            drawRequest.invoke()
        }

    fun onDraw(canvas: Canvas, physPadding: SizeF) {
        val deltaX = if(orientation == Orientation.HORIZONTAL) physOffset else 0f
        val deltaY = if(orientation == Orientation.VERTICAL) physOffset else 0f

        // Draw cells
        val offset = physPadding.concat(SizeF(deltaX, deltaY))
        for (cell in cells) {
            cell.onDraw(canvas, offset)
        }

        // Draw phantoms
        val phantomOffset = SizeF(
                mainValue * cells.size * if(orientation == Orientation.HORIZONTAL) 1 else 0,
                mainValue * cells.size * if(orientation == Orientation.VERTICAL) 1 else 0
        )

        val phantomEndOffset = offset.concat(phantomOffset)
        val phantomStartOffset = offset.concat(phantomOffset.inverse())
        for (cell in cells) {
            cell.onDraw(canvas, phantomStartOffset, phantomPaint)
            cell.onDraw(canvas, phantomEndOffset, phantomPaint)
        }
    }

    fun setPhantomsAlpha(value: Float){
        phantomPaint.alpha = (255 * value).roundToInt()
        drawRequest.invoke()
    }

    fun contain(cell: Cell): Boolean {
        for(c in cells) {
            if(c.position == cell.position) {
                return true
            }
        }
        return false
    }

    fun move(deltaP: PointF) {
        val delta = if(orientation == Orientation.HORIZONTAL) deltaP.x else deltaP.y

        physOffset += delta
        checkOffset()
        drawRequest.invoke()
    }

    private fun checkOffset() {
        if(physOffset < 0) {
            physOffset += mainValue
            moveCellsByDirection(Direction.START)
        } else if(physOffset >= mainValue) {
            physOffset -= mainValue
            moveCellsByDirection(Direction.END)
        }
    }

    fun moveCellsByDirection(direction: Direction) {
        for (cell in cells) {
            cell.move(direction, orientation)
        }
    }

    companion object {
        const val RELEASE_DURATION = 200L
    }
}