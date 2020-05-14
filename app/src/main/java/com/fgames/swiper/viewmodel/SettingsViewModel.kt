package com.fgames.swiper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fgames.swiper.model.FieldSize
import com.fgames.swiper.model.MixIntensity
import com.fgames.swiper.model.SettingsModel
import com.fgames.swiper.repository.SettingsRepository

class SettingsViewModel : ViewModel() {
    private val repository = SettingsRepository()

    private val _settings: MutableLiveData<SettingsModel> = MutableLiveData()

    private var intensity: MixIntensity
    private var size: FieldSize

    init {
        repository.get().run {
            this@SettingsViewModel.intensity = intensity
            this@SettingsViewModel.size = size
            _settings.value = this
        }
    }

    fun getSettings(): LiveData<SettingsModel> = _settings

    fun setSizeField(size: FieldSize) {
        setState {
            this.size = size
        }
    }

    fun setMixIntensity(intensity: MixIntensity) {
        setState {
            this.intensity = intensity
        }
    }

    private fun setState(stateChanger: () -> Unit) {
        stateChanger()
        val model = SettingsModel(size, intensity)
        _settings.value = model
        repository.save(model)
    }
}