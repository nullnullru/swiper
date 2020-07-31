package com.fgames.swiper.ui.utils

import android.view.View
import com.fgames.swiper.ui.MainActivity

class ViewTools {
    companion object {
        const val ANIMATION_DURATION = 400L

        fun fadeInAndOut(listToIn: List<View>, listToOut: List<View>) {
            listToIn.forEach { fadeIn(it) }
            listToOut.forEach { fadeOut(it) }
        }

        fun fadeInAndOut(viewToFadeIn: View, viewToFadeOut: View) {
            fadeIn(viewToFadeIn)
            fadeOut(viewToFadeOut)
        }

        fun fadeOut(view: View) {
            view.animate()
                .setDuration(ANIMATION_DURATION)
                .alpha(0f)
                .start()
        }

        fun fadeIn(view: View) {
            view.animate()
                .setDuration(ANIMATION_DURATION)
                .alpha(1f)
                .start()
        }
    }
}