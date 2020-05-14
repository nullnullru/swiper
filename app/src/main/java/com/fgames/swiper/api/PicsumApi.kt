package com.fgames.swiper.api

import android.graphics.Bitmap
import com.fgames.swiper.model.picture.PictureModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PicsumApi {
    @GET("v2/list")
    fun getPictures(@Query("page") page: Int, @Query("limit") limit: Int = 100): Single<List<PictureModel>>

    @GET("id/{id}/{size}")
    fun getPicture(@Path("id") id: Int, @Path("size") size: Int, @Query("blur") blur: Int = 0): Single<Bitmap>
}