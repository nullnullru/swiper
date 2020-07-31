package com.fgames.swiper.ui.view.swiperview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
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
import kotlin.math.roundToInt

class SwiperView : View, Field.FieldListener {
    private var fieldCreatingDisposable: Disposable? = null

    private var field: Field? = null
    private var fieldLine: FieldLine? = null

    var size = Size(3, 3)
    var mixIntensity = 3
    var listener: SwiperViewListener? = null

    private var onDownPoint: PointF? = null
    private var lastMovePoint: PointF? = null
    private var longPress: Boolean = false
    private var isBlocked = false

    private var gd: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                longPress = true
                release(false)
                invalidate()
            }

            override fun onDown(event: MotionEvent?): Boolean {
                if (event != null) {
                    if (fieldLine == null) {
                        longPress = false
                        lastMovePoint = null
                        onDownPoint = PointF(event.x, event.y)
                    }
                    return true
                }
                return super.onDown(event)
            }
        })

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun setImage(image: Bitmap) {
        fieldCreatingDisposable?.dispose()
        fieldCreatingDisposable = Single.just(Field.FieldData(image, Size(width, height), size))
            .subscribeOn(Schedulers.io())
            .map {
                val field = Field.create(it) { invalidate() }
                return@map field.apply {
                    mix(mixIntensity)
                    listener = this@SwiperView
                }
            }
            .doOnSubscribe { longPress = false }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { listener?.onReady() }
            .subscribe { field ->
                this.field = field
                invalidate()
            }
    }

    override fun onComplete() {
        listener?.onCompleted()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            field?.onDraw(it, longPress)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null && field != null) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (!longPress) {
                        val point = PointF(event.x, event.y)

                        if (fieldLine == null) {
                            getMoveOrientation(point)?.let {
                                fieldLine = field?.getLine(point, it)
                                lastMovePoint = onDownPoint
                            }
                        }

                        if(!isBlocked &&
                            fieldLine != null &&
                            lastMovePoint != null
                        ) {
                            fieldLine!!.move(point.concat(lastMovePoint!!.inverse()))
                        }

                        lastMovePoint = point
                    }
                }
                MotionEvent.ACTION_UP -> {
                    release { fieldLine = null }
                    longPress = false
                    invalidate()
                }
            }
        }
        return gd.onTouchEvent(event)
    }

    private fun getMoveOrientation(point: PointF): Orientation? {
        return onDownPoint?.let {
            val diffX = abs(it.x - point.x)
            val diffY = abs(it.y - point.y)

            if (max(diffX, diffY) > DISTANCE_TO_ACTION) {
                return@let if (diffX > diffY) Orientation.HORIZONTAL else Orientation.VERTICAL
            }
            return@let null
        }
    }

    private fun release(withAnimation: Boolean = true, doOnRelease: (() -> Unit)? = null) {
        if(isBlocked) return
        block()

        fieldLine?.run {
            if(withAnimation) {
                val from = physOffset
                val to = if (physOffset > mainValue / 2) mainValue else 0f

                val animator = ValueAnimator.ofFloat(from, to)

                animator.duration = FieldLine.RELEASE_DURATION
                animator.addUpdateListener {
                    physOffset = it.animatedValue as Float
                }
                animator.doOnEnd {
                    animatePhantomsAlpha {
                        doOnRelease?.invoke()
                        field?.doOnLineReleased()
                        unblock()
                    }
                }

                animator.start()
            } else {
                physOffset = if (physOffset > mainValue / 2) mainValue else 0f

                doOnRelease?.invoke()
                field?.doOnLineReleased()
                unblock()
            }
        } ?: run {
            unblock()
        }
    }

    private fun block() {
        isBlocked = true
    }

    private fun unblock() {
        isBlocked = false
    }

    private fun animatePhantomsAlpha(doOnEnd: () -> Unit) {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = FieldLine.RELEASE_DURATION / 2
        animator.addUpdateListener {
            fieldLine?.setPhantomsAlpha(it.animatedValue as Float)
        }
        animator.doOnEnd {
            doOnEnd()
        }
        animator.start()
    }

    companion object {
        const val DISTANCE_TO_ACTION = 12
    }

    interface SwiperViewListener {
        fun onCompleted()
        fun onReady()
    }
}