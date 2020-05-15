package com.fgames.swiper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import co.revely.gradient.RevelyGradient
import kotlin.math.abs

class GradientGenerator {
    companion object {
        private val GRADIENTS = listOf(
            GradientModel(Color.parseColor("#fce38a"), Color.parseColor("#f38181"), 70f),
            GradientModel(Color.parseColor("#f54ea2"), Color.parseColor("#ff7676"), 135f),
            GradientModel(Color.parseColor("#17ead9"), Color.parseColor("#6078ea"), 120f),
            GradientModel(Color.parseColor("#622774"), Color.parseColor("#c53364"), 45f),
            GradientModel(Color.parseColor("#7117ea"), Color.parseColor("#ea6060"), 160f),
            GradientModel(Color.parseColor("#42e695"), Color.parseColor("#3bb2b8"), 215f),
            GradientModel(Color.parseColor("#f02fc2"), Color.parseColor("#6094ea"), 135f),
            GradientModel(Color.parseColor("#65799b"), Color.parseColor("#5e2563"), 115f),
            GradientModel(Color.parseColor("#184e68"), Color.parseColor("#57ca85"), 130f),
            GradientModel(Color.parseColor("#5b247a"), Color.parseColor("#1bcedf"), 315f)
        )

        fun getGradient(key: Int, size: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val gradient = GRADIENTS[abs(key) % GRADIENTS.size]

            RevelyGradient.linear()
                .colors(intArrayOf(gradient.color1, gradient.color2))
                .angle(gradient.angle)
                .on {
                    it.bounds = Rect(0, 0, size, size)
                    it.draw(canvas)
                }

            return bitmap
        }
    }

    private class GradientModel(
        val color1: Int,
        val color2: Int,
        val angle: Float
    )
}