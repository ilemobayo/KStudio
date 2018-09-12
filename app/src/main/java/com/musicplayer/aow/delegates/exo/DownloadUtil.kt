/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.musicplayer.aow.delegates.exo


import android.content.Context
import android.net.Uri

import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.musicplayer.aow.R

import java.io.File
import com.google.android.exoplayer2.upstream.cache.CacheUtil.getKey



object DownloadUtil {

    private var cache: Cache? = null
    private var downloadManager: DownloadManager? = null

    @Synchronized
    fun getCache(context: Context): Cache? {
        if (cache == null) {
            val cacheDirectory = File(context.externalCacheDir, "offline")
            cache = SimpleCache(cacheDirectory, NoOpCacheEvictor())
        }
        return cache
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager? {
        if (downloadManager == null) {
            val actionFile = File(context.externalCacheDir, "rules")
            downloadManager = DownloadManager(
                    getCache(context),
                    DefaultDataSourceFactory(
                            context,
                            Util.getUserAgent(context, context.getString(R.string.application_name))),
                    actionFile,
                    ProgressiveDownloadAction.DESERIALIZER)
        }
        return downloadManager
    }

    fun remove(path: String) {
        val uri = Uri.parse(path)
        val dataSpec = DataSpec(uri)
        CacheUtil.remove(cache, CacheUtil.getKey(dataSpec))
    }

    fun removeAll(){

    }

}
