package com.fgames.swiper

import android.app.Application
import com.fgames.swiper.api.PicsumApi
import io.paperdb.Paper
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TheApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Paper.init(this)

        val picsumRetrofit = Retrofit
            .Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BuildConfig.PICSUM_BASE)
            .build()

        picsumApi = picsumRetrofit.create(PicsumApi::class.java)
    }

    companion object {
        var picsumApi: PicsumApi? = null
            private set
    }
}