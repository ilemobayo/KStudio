package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class HttpHelperFunctions (val context: Context){

    private var httpClient: OkHttpClient = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build()

    private fun callRequest(url: String): Request? {
        return Request.Builder()
                .url(url)
                .build()
    }

    fun getJsonString(url: String): String? {
        return httpClient.newCall(callRequest(url)!!).execute().body()?.string()
    }

}