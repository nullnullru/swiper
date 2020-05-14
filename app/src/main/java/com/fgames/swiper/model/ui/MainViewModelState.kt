package com.fgames.swiper.model.ui

import com.fgames.swiper.model.picture.PictureModel

class MainViewModelState(
    val pictures: List<PictureModel>,
    val page: Int,
    val index: Int
)