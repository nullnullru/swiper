package com.fgames.swiper.repository

import com.fgames.swiper.model.state.MainViewModelState
import io.paperdb.Paper

class MainStateRepository {
    fun save(model: MainViewModelState) {
        Paper.book(BOOK).write(KEY, model)
    }

    fun get(): MainViewModelState {
        return Paper.book(BOOK).read(KEY, MainViewModelState(mutableListOf(), 1, 0))
    }

    companion object {
        const val BOOK = "MAIN_STATE_BOOK"
        const val KEY = "model"
    }
}