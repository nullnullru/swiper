package com.fgames.swiper.ui

import android.animation.ValueAnimator
import android.graphics.Point
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fgames.swiper.R
import com.fgames.swiper.databinding.SettingsLayoutBinding
import com.fgames.swiper.model.FieldSize
import com.fgames.swiper.model.MixIntensity
import com.fgames.swiper.model.SettingsModel
import com.fgames.swiper.model.Size
import com.fgames.swiper.ui.utils.ViewTools
import com.fgames.swiper.ui.view.swiperview.SwiperView
import com.fgames.swiper.viewmodel.MainViewModel
import com.fgames.swiper.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.settings_layout.view.*
import java.util.concurrent.TimeUnit


class MainActivity : SettingsActivity(), SwiperView.SwiperViewListener, View.OnClickListener {

    private lateinit var mvm: MainViewModel

    private var showSwiper: Disposable? = null

    private var timerAnimator: ValueAnimator? = null
    private var timerScaleValue: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mvm = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(MainViewModel::class.java)

        mvm.getUpdateFlag().observe(this, Observer { flag ->
            if(flag == MainViewModel.INITIAL_FLAG) {
                swiper.alpha = 0f
                picture_preview.alpha = 0f
                picture_preview_small.alpha = 0f
            } else {
                ViewTools.fadeInAndOut(
                    listOf(animation_view),
                    listOf(swiper, picture_preview, picture_preview_small, view_done)
                )

                button_refresh.alpha = 0f
                button_refresh.visibility = View.INVISIBLE

                showSwiper?.dispose()
            }
        })
        mvm.getLoadedPicture().observe(this, Observer { response ->
            ViewTools.fadeOut(animation_view)

            if(response.isRefresh) {
                swiper.setImage(response.picture)
                ViewTools.fadeOut(view_done)
            } else {
                when (response.size) {
                    MainViewModel.SMALL_PICTURE_SIZE ->
                        picture_preview_small.let {
                            it.setImageBitmap(response.picture)
                            ViewTools.fadeIn(it)
                        }
                    MainViewModel.DEFAULT_PICTURE_SIZE -> {
                        picture_preview.let {
                            it.setImageBitmap(response.picture)
                            ViewTools.fadeInAndOut(it, picture_preview_small)

                            Single
                                .timer(500, TimeUnit.MILLISECONDS)
                                .subscribe { _ -> swiper.setImage(response.picture) }

                            showSwiper()
                        }
                    }
                }
            }
        })
        mvm.getPrevAvailable().observe(this, Observer { available ->
            button_prev.visibility = if(available) View.VISIBLE else View.INVISIBLE
        })

        svm.getSettings().observe(this, Observer { settings ->
            swiper.mixIntensity = settings.intensity.value
            swiper.size = Size(settings.size.value, settings.size.value)
            mvm.refresh()
        })

        swiper.listener = this

        button_next.setOnClickListener(this)
        button_prev.setOnClickListener(this)
        button_refresh.setOnClickListener(this)
        button_settings.setOnClickListener(this)

        initTimerView()
    }

    override fun onCompleted() {
        ViewTools.fadeIn(view_done)
    }

    override fun onReady() { }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v?.id) {
            R.id.button_next -> mvm.nextPicture()
            R.id.button_prev -> mvm.prevPicture()
            R.id.button_refresh -> mvm.refresh()
            R.id.button_settings -> openSettings()
        }
    }

    private fun initTimerView() {
        timer_view.post {
            timer_view.let {
                val height = it.height
                val displaySize = Point().apply {
                    windowManager.defaultDisplay.getSize(this)
                }

                it.translationX = ((height - displaySize.x) / 2f) * -1

                timerScaleValue = view.height / height.toFloat()
            }
        }
    }

    private fun showSwiper() {
        showSwiper?.dispose()
        showSwiper = Single
            .timer(TIMER_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { animateTimer() }
            .doAfterSuccess { stopTimer() }
            .doAfterSuccess {
                button_refresh.visibility = View.VISIBLE
                ViewTools.fadeIn(button_refresh)
            }
            .doOnDispose {
                timerAnimator?.let {
                    if(it.isRunning) {
                        it.cancel()
                        fastAnimateTimer()
                    }
                }
            }
            .subscribe { _ ->
                ViewTools.fadeOut(picture_preview)
                ViewTools.fadeOut(picture_preview_small)
                ViewTools.fadeIn(swiper)
            }
    }

    private fun stopTimer() {
        timerAnimator?.let {
            it.cancel()
            timer_view.alpha = 0f
        }
    }

    private fun fastAnimateTimer() {
        val startScale = timer_view.scaleX
        timerAnimator = ValueAnimator.ofFloat(startScale, timerScaleValue).apply {
            val startAlpha = timer_view.alpha
            val way = startScale - timerScaleValue

            duration = ViewTools.ANIMATION_DURATION
            addUpdateListener {
                val value = it.animatedValue as Float
                timer_view.run {
                    alpha = startAlpha * ((value - timerScaleValue) / way)
                    scaleY = value
                    scaleX = value
                }
            }
            doOnEnd { timer_view.alpha = 0f }
            start()
        }
    }

    private fun animateTimer() {
        timerAnimator?.cancel()
        timerAnimator = ValueAnimator.ofFloat(1f, timerScaleValue).apply {
            duration = TIMER_ANIMATION_DURATION
            addUpdateListener {
                val value = it.animatedValue as Float
                timer_view.run {
                    alpha = (1f - value) / 2f
                    scaleY = value
                    scaleX = value
                }
            }
            start()
        }
    }

    companion object {
        const val TIMER_ANIMATION_DURATION = 2000L
    }

}
