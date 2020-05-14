package com.fgames.swiper

import android.animation.ValueAnimator
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fgames.swiper.databinding.SettingsLayoutBinding
import com.fgames.swiper.model.FieldSize
import com.fgames.swiper.model.MixIntensity
import com.fgames.swiper.model.SettingsModel
import com.fgames.swiper.model.Size
import com.fgames.swiper.ui.view.swiperview.SwiperView
import com.fgames.swiper.viewmodel.MainViewModel
import com.fgames.swiper.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.settings_layout.*
import kotlinx.android.synthetic.main.settings_layout.view.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), SwiperView.SwiperViewListener, View.OnClickListener {
    private lateinit var mvm: MainViewModel
    private lateinit var svm: SettingsViewModel

    private var showSwiper: Disposable? = null

    private var timerAnimator: ValueAnimator? = null
    private var timerScaleValue: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mvm = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(MainViewModel::class.java)
        svm = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(SettingsViewModel::class.java)

        mvm.getUpdateFlag().observe(this, Observer { flag ->
            if(flag == MainViewModel.INITIAL_FLAG) {
                swiper.alpha = 0f
                picture_preview.alpha = 0f
                picture_preview_small.alpha = 0f
            } else {
                fadeOut(swiper)
                fadeOut(picture_preview)
                fadeOut(picture_preview_small)
                fadeOut(view_done)

                fadeIn(animation_view)

                button_refresh.alpha = 0f
                button_refresh.visibility = View.INVISIBLE

                showSwiper?.dispose()
            }
        })
        mvm.getLoadedPicture().observe(this, Observer { response ->
            fadeOut(animation_view)

            if(response.isRefresh) {
                swiper.setImage(response.picture)
                fadeOut(view_done)
            } else {
                when (response.size) {
                    MainViewModel.SMALL_PICTURE_SIZE ->
                        picture_preview_small.let {
                            it.setImageBitmap(response.picture)
                            fadeIn(it)
                        }
                    MainViewModel.DEFAULT_PICTURE_SIZE -> {
                        picture_preview.let {
                            it.setImageBitmap(response.picture)
                            fadeOut(picture_preview_small)
                            fadeIn(it)

                            swiper.setImage(response.picture)
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

        button_next.setOnClickListener { mvm.nextPicture() }
        button_prev.setOnClickListener { mvm.prevPicture() }
        button_refresh.setOnClickListener { mvm.refresh() }
        button_settings.setOnClickListener { openSettings() }

        initTimerView()
    }

    override fun onCompleted() {
        fadeIn(view_done)
    }

    override fun onReady() { }

    private fun openSettings() {
        val binding = DataBindingUtil.inflate<SettingsLayoutBinding>(
            layoutInflater,
            R.layout.settings_layout,
            null,
            false
        )
        val dialog = BottomSheetDialog(this)
        val settingsObserver = Observer<SettingsModel> { settings -> binding.model = settings }

        binding.model = svm.getSettings().value
        svm.getSettings().observeForever(settingsObserver)
        dialog.setOnDismissListener { svm.getSettings().removeObserver(settingsObserver) }

        binding.root.field_size_3.setOnClickListener(this)
        binding.root.field_size_5.setOnClickListener(this)
        binding.root.field_size_7.setOnClickListener(this)
        binding.root.intensity_low.setOnClickListener(this)
        binding.root.intensity_medium.setOnClickListener(this)
        binding.root.intensity_high.setOnClickListener(this)

        dialog.setContentView(binding.root)
        dialog.show()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.field_size_3 -> svm.setSizeField(FieldSize.S3x3)
            R.id.field_size_5 -> svm.setSizeField(FieldSize.S5x5)
            R.id.field_size_7 -> svm.setSizeField(FieldSize.S7x7)
            R.id.intensity_low -> svm.setMixIntensity(MixIntensity.LOW)
            R.id.intensity_medium -> svm.setMixIntensity(MixIntensity.MEDIUM)
            R.id.intensity_high -> svm.setMixIntensity(MixIntensity.HIGH)
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
                fadeIn(button_refresh)
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
                fadeOut(picture_preview)
                fadeOut(picture_preview_small)
                fadeIn(swiper)
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

            duration = ANIMATION_DURATION
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

    private fun fadeOut(view: View) {
        view.animate()
            .setDuration(ANIMATION_DURATION)
            .alpha(0f)
            .start()
    }

    private fun fadeIn(view: View) {
        view.animate()
            .setDuration(ANIMATION_DURATION)
            .alpha(1f)
            .start()
    }

    companion object {
        const val ANIMATION_DURATION = 400L
        const val ANIMATION_DELAY = ANIMATION_DURATION
        const val TIMER_ANIMATION_DURATION = 2000L
    }

}
