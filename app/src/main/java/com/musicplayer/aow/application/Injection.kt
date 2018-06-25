package com.musicplayer.aow.application

import android.content.Context

object Injection {

    fun provideContext(): Context? {
        return MusicPlayerApplication.instance!!.applicationContext
    }
}
