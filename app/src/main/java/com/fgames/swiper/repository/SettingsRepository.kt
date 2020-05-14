package com.fgames.swiper.repository

import com.fgames.swiper.model.FieldSize
import com.fgames.swiper.model.MixIntensity
import com.fgames.swiper.model.SettingsModel
import io.paperdb.Paper

class SettingsRepository {
    fun save(model: SettingsModel) {
        Paper.book(BOOK).write(KEY, model)
    }

    fun get(): SettingsModel {
        return Paper.book(BOOK).read(KEY, SettingsModel(FieldSize.S3x3, MixIntensity.LOW))
    }

    companion object {
        const val BOOK = "SETTINGS_BOOK"
        const val KEY = "model"
    }
}