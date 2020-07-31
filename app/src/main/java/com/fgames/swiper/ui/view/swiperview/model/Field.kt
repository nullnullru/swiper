package com.fgames.swiper.ui.view.swiperview.model

import android.graphics.Bitmap
import android.graphics.Canvas
import com.fgames.swiper.model.*
import kotlin.math.roundToInt
import kotlin.random.Random

class Field(
    private val cells: List<Cell>,
    physGameFieldSize: SizeF,
    private val physPadding: SizeF,
    private val physCellSize: SizeF,
    private val drawRequest: () -> Unit
) {

    private val gameFieldBound = BoundF(PointF(physPadding.w, physPadding.h), physGameFieldSize)
    private var line: FieldLine? = null

    private var isCompleted = true
    var listener: FieldListener? = null

    fun onDraw(canvas: Canvas, initPosition: Boolean = false) {
        for (cell in cells) {
            if (line?.contain(cell) != true) {
                cell.onDraw(canvas, physPadding, initPosition = initPosition)
            }
        }
        line?.onDraw(canvas, physPadding)
    }

    fun mix(iterations: Int = 3) {
        isCompleted = false

        for (i in 0 until iterations) {
            val orientation =
                if (Random.nextBoolean()) Orientation.HORIZONTAL else Orientation.VERTICAL
            val direction = if (Random.nextBoolean()) Direction.END else Direction.START

            val cell = cells[Random.nextInt(cells.size)]
            getLine(cell.getPhysCenter(physPadding), orientation)?.let {
                for (move in 0..(Random.nextInt(it.cells.size - 1) + 1)) {
                    it.moveCellsByDirection(direction)
                }
                line = null
            }
        }

        if (isCompleted()) {
            mix(iterations)
        }
    }

    fun getLine(point: PointF, orientation: Orientation): FieldLine? {
        if (!isCompleted && gameFieldBound.has(point)) {
            val isHorizontal = orientation == Orientation.HORIZONTAL
            val pickingPoint = PointF(point.x - physPadding.w, point.y - physPadding.h)
            val pickingLine = if (orientation == Orientation.HORIZONTAL) {
                (pickingPoint.y / physCellSize.h).toInt()
            } else {
                (pickingPoint.x / physCellSize.w).toInt()
            }

            line = FieldLine(
                cells.filter {
                    it.position.y == pickingLine && isHorizontal ||
                            it.position.x == pickingLine && !isHorizontal
                },
                physCellSize,
                orientation,
                drawRequest
            )
        }
        return line
    }

    private fun isCompleted(): Boolean {
        for (cell in cells) {
            if (cell.position != cell.initPosition)
                return false
        }
        return true
    }

    fun doOnLineReleased() {
        line = null
        isCompleted = isCompleted()
        if (isCompleted) listener?.onComplete()
    }

    companion object Factory {
        fun create(data: FieldData, drawRequest: () -> Unit): Field {
            val padding = SizeF(
                data.physFieldSize.w * data.paddingPercent,
                data.physFieldSize.h * data.paddingPercent
            )
            val physGameFieldSize = SizeF(
                data.physFieldSize.w - padding.w * 2f,
                data.physFieldSize.h - padding.h * 2f
            )
            val physCellSize = SizeF(
                physGameFieldSize.w / data.fieldSize.w,
                physGameFieldSize.h / data.fieldSize.h
            )
            val iPhysCellSize = SizeF(
                physCellSize.w,
                physCellSize.h
            )

            val gameImage = Bitmap.createScaledBitmap(
                data.image,
                physGameFieldSize.w.roundToInt(),
                physGameFieldSize.h.roundToInt(),
                true
            )

            val cells = mutableListOf<Cell>()
            for (x in 0 until data.fieldSize.w) {
                val xR = (physCellSize.w * x).roundToInt()
                for (y in 0 until data.fieldSize.h) {
                    val yR = (physCellSize.h * y).roundToInt()

                    val cellImage = Bitmap.createScaledBitmap(
                        Bitmap.createBitmap(
                            gameImage, xR, yR, iPhysCellSize.w.toInt(), iPhysCellSize.h.toInt()
                        ),
                        iPhysCellSize.w.roundToInt(),
                        iPhysCellSize.h.roundToInt(),
                        true
                    )
                    cells.add(Cell(cellImage, physCellSize, data.fieldSize, Point(x, y)))
                }
            }

            return Field(cells, physGameFieldSize, padding, physCellSize, drawRequest)
        }
    }

    class FieldData(
        val image: Bitmap,
        val physFieldSize: Size,
        val fieldSize: Size,
        val paddingPercent: Float = 0.125f
    )

    interface FieldListener {
        fun onComplete()
    }
}