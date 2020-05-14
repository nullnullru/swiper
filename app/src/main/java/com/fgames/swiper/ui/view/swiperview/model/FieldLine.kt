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
    private val physCellSize: SizeF,
    private val orientation: Orientation,
    private val drawRequest: (() -> Unit)?,
    private val doOnRelease: () -> Unit
) {
    private val mainValue = if (orientation == Orientation.HORIZONTAL) physCellSize.w else physCellSize.h
    private var isBlocked = false
    private var physOffset: Float = 0f
    private var phantomPaint: Paint = Paint()

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

    fun contain(cell: Cell): Boolean {
        for(c in cells) {
            if(c.position == cell.position) {
                return true
            }
        }
        return false
    }

    fun release(withAnimation: Boolean = true, doOnRelease: (() -> Unit)? = null) {
        if(isBlocked) return

        block()

        if(withAnimation) {
            val from = physOffset
            val to = if (physOffset > mainValue / 2) mainValue else 0f

            val animator = ValueAnimator.ofFloat(from, to)

            animator.duration = RELEASE_DURATION
            animator.addUpdateListener {
                physOffset = it.animatedValue as Float
                checkOffset()
                drawRequest?.invoke()
            }
            animator.doOnEnd {
                animatePhantomsAlpha {
                    this.doOnRelease()
                    doOnRelease?.invoke()
                }
            }

            animator.start()
        } else {
            physOffset = if (physOffset > mainValue / 2) mainValue else 0f
            checkOffset()

            drawRequest?.invoke()

            this.doOnRelease()
            doOnRelease?.invoke()
        }
    }

    private fun animatePhantomsAlpha(doOnEnd: () -> Unit) {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = RELEASE_DURATION / 2
        animator.addUpdateListener {
            phantomPaint.alpha = (255 * (it.animatedValue as Float)).roundToInt()
            drawRequest?.invoke()
        }
        animator.doOnEnd { doOnEnd() }
        animator.start()
    }

    private fun block() {
        isBlocked = true
    }

    fun move(deltaP: PointF) {
        val delta = if(orientation == Orientation.HORIZONTAL) deltaP.x else deltaP.y

        if(!isBlocked) {
            physOffset += delta
            checkOffset()
            drawRequest?.invoke()
        }
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