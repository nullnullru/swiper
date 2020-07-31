package com.fgames.swiper.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fgames.swiper.R
import com.fgames.swiper.databinding.SettingsLayoutBinding
import com.fgames.swiper.model.FieldSize
import com.fgames.swiper.model.MixIntensity
import com.fgames.swiper.model.SettingsModel
import com.fgames.swiper.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.settings_layout.view.*

abstract class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    protected lateinit var svm: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        svm = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(SettingsViewModel::class.java)
    }

    protected fun openSettings() {
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
}