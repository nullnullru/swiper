package com.fgames.swiper.model.picture

import com.google.gson.annotations.SerializedName

class PictureModel(
    @SerializedName("id") val id: Int,
    @SerializedName("download_url") val url: String
)