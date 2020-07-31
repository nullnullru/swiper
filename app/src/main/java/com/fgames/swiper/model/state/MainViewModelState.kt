package com.fgames.swiper.model.state

import com.fgames.swiper.model.picture.PictureModel

class MainViewModelState(
    val pictures: List<PictureModel>,
    val page: Int,
    val index: Int
)