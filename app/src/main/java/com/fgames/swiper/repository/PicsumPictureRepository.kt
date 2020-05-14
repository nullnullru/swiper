package com.fgames.swiper.repository

import android.graphics.Bitmap
import com.fgames.swiper.BuildConfig
import com.fgames.swiper.model.picture.PictureLoadRequest
import com.fgames.swiper.utils.ImageLoadUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PicsumPictureRepository {
    companion object {
        fun loadPicture(pictureLoadRequest: PictureLoadRequest): Single<Bitmap?> {
            val url = BuildConfig.PICSUM_BASE +
                    "id/${pictureLoadRequest.pictureModel.id}/${pictureLoadRequest.size}" +
                    if(pictureLoadRequest.blur > 0) "?blur=${pictureLoadRequest.blur}" else ""

            return Single.just(url)
                .subscribeOn(Schedulers.io())
                .flatMap { Single.just(ImageLoadUtils.loadImage(it)) }
        }
    }
}