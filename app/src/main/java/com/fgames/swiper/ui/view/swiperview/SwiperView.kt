package com.fgames.swiper.ui.view.swiperview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.fgames.swiper.R
import com.fgames.swiper.model.Orientation
import com.fgames.swiper.model.PointF
import com.fgames.swiper.model.Size
import com.fgames.swiper.ui.view.swiperview.model.Field
import com.fgames.swiper.ui.view.swiperview.model.FieldLine
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs
import kotlin.math.max

class SwiperView : View {
    private var fieldCreatingDisposable: Disposable? = null

    private var field: Field? = null
    private var fieldLine: FieldLine? = null

    private var onDownPoint: PointF? = null
    private var lastMovePoint: PointF? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        post {
            setImage(BitmapFactory.decodeResource(resources, R.drawable.picture))
        }
    }

    fun setImage(image: Bitmap) {
        fieldCreatingDisposable = Field.create(
                image,
                Size(width, height),
                Size(9, 9)
            )
            .doOnSuccess { it.drawRequest = { invalidate() } }
            .subscribe { field ->
                this.field = field
                invalidate()
            }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            field?.onDraw(it)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event != null && field != null) {
            val point = PointF(event.x, event.y)
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    if(fieldLine != null) {
                        fieldLine?.release { fieldLine = null }
                    } else {
                        onDownPoint = point
                        return true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if(fieldLine == null) {
                        getMoveOrientation(point)?.let {
                            fieldLine = field?.getLine(point, it)
                            lastMovePoint = onDownPoint
                        }
                    }

                    fieldLine?.run {
                        lastMovePoint?.let {
                            move(point.concat(it.inverse()))
                        }
                    }

                    lastMovePoint = point
                }
                MotionEvent.ACTION_UP -> {
                    fieldLine?.release {
                        fieldLine = null
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getMoveOrientation(point: PointF): Orientation? {
        return onDownPoint?.let {
            val diffX = abs(it.x - point.x)
            val diffY = abs(it.y - point.y)

            if(max(diffX, diffY) > DISTANCE_TO_ACTION) {
                return@let if (diffX > diffY) Orientation.HORIZONTAL else Orientation.VERTICAL
            }
            return@let null
        }
    }

    companion object {
        const val DISTANCE_TO_ACTION = 20
    }
}