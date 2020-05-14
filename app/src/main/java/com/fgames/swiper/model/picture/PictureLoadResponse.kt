package com.fgames.swiper.model.picture

import android.graphics.Bitmap

class PictureLoadResponse(
    val picture: Bitmap,
    val size: Int,
    val isRefresh: Boolean = false
) {
    companion object {
        fun refreshCopy(response: PictureLoadResponse) = PictureLoadResponse(
            response.picture,
            response.size,
            true
        )
    }
}