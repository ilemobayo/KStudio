package com.musicplayer.aow.application

import android.content.Context
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule



object Injection {

    fun provideContext(): Context? {
        return MusicPlayerApplication.instance!!.applicationContext
    }
}
