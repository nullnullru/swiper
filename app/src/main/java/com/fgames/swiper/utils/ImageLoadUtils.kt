package com.fgames.swiper.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.URL

class ImageLoadUtils {
    companion object {
        fun loadImage(url: String): Bitmap {
            val url = URL(url);
            val connection =  url.openConnection()
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        }
    }
}