package com.fgames.swiper.ui.view.swiperview.model

import android.graphics.Bitmap
import android.graphics.Canvas
import com.fgames.swiper.model.*
import kotlin.math.roundToInt

class Field(
    private val cells: List<Cell>,
    private val fieldSize: Size,
    private val physGameFieldSize: SizeF,
    private val physPadding: SizeF,
    private val physCellSize: SizeF,
    var drawRequest: (() -> Unit)? = null
) {
    private val gameFieldBound = BoundF(PointF(physPadding.w, physPadding.h), physGameFieldSize)

    private var line: FieldLine? = null

    fun onDraw(canvas: Canvas) {
        for (cell in cells) {
            if(line?.contain(cell) != true) {
                cell.onDraw(canvas, physPadding)
            }
        }
        line?.onDraw(canvas, physPadding)
    }

    fun getLine(point: PointF, orientation: Orientation): FieldLine? {
        line = null
        if(gameFieldBound.has(point)) {
            val pPoint = PointF(point.x - physPadding.w, point.y - physPadding.h)
            if(orientation == Orientation.HORIZONTAL) {
                val line = (pPoint.y / physCellSize.h).roundToInt()
                this.line = FieldLine(
                    cells.filter { it.position.y == line },
                    physCellSize,
                    orientation,
                    drawRequest,
                    { this.line = null }
                )
            } else {
                val row = (pPoint.x / physCellSize.w).roundToInt()
                this.line = FieldLine(
                    cells.filter { it.position.x == row },
                    physCellSize,
                    orientation,
                    drawRequest,
                    { this.line = null }
                )
            }
        }
        return line
    }

    companion object Factory {
        fun create(image: Bitmap, physFieldSize: Size, fieldSize: Size, paddingPercent: Float = 0.125f): Field {
            val padding = SizeF(
                physFieldSize.w * paddingPercent,
                physFieldSize.h * paddingPercent
            )
            val physGameFieldSize = SizeF(
                physFieldSize.w - padding.w * 2f,
                physFieldSize.h - padding.h * 2f
            )
            val physCellSize = SizeF(
                physGameFieldSize.w / fieldSize.w,
                physGameFieldSize.h / fieldSize.h
            )
            val iPhysCellSize = Size(
                physCellSize.w.roundToInt(),
                physCellSize.h.roundToInt()
            )

            val gameImage = Bitmap.createScaledBitmap(
                image,
                physGameFieldSize.w.roundToInt(),
                physGameFieldSize.h.roundToInt(),
                true
            )

            val cells = mutableListOf<Cell>()
            for (x in 0 until fieldSize.w) {
                val xR = (physCellSize.w * x).roundToInt()
                for (y in 0 until fieldSize.h) {
                    val yR = (physCellSize.h * y).roundToInt()

                    val cellImage = Bitmap.createBitmap(gameImage, xR, yR, iPhysCellSize.w, iPhysCellSize.h)
                    cells.add(Cell(cellImage, physCellSize, fieldSize, Point(x, y)))
                }
            }

            return Field(
                cells,
                fieldSize,
                physGameFieldSize,
                padding,
                physCellSize
            )
        }
    }
}