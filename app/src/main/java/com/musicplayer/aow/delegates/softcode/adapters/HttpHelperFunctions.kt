package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class HttpHelperFunctions (val context: Context){

    private val HEADER_CACHE = "android-cache"
    private val CACHE_DIR = "httpCache"
    private var httpClient: OkHttpClient = OkHttpClient()

    private fun callRequest(url: String): Request? {
        //check cache
        val httpCacheDirectory = File(context.applicationContext.cacheDir, "httpCache")
        val cache = Cache(httpCacheDirectory, 2 * 1024 * 1024)
        httpClient = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request()
                    if (request.header(HEADER_CACHE) != null) {
                        val offlineRequest = request.newBuilder()
                                .header("Cache-Control", "only-if-cached, " +
                                        "max-stale=" + request.header(HEADER_CACHE))
                                .build()
                        val response = chain.proceed(offlineRequest)
                        if (response.isSuccessful) {
                            response
                        }
                    }
                    try {
                        chain.proceed(chain.request())
                    } catch (e: Exception) {
                        val offlineRequest = chain.request().newBuilder()
                                .header("Cache-Control", "public, only-if-cached," +
                                        "max-stale=" + request.header(HEADER_CACHE))
                                .build()
                        chain.proceed(offlineRequest)
                    }
                }
                .build()

        return Request.Builder()
                .url(url)
                .build()

    }

    fun getJsonString(url: String): String? {
        return httpClient.newCall(callRequest(url)).execute().body()?.string()
    }

}