package com.musicplayer.aow.delegates.player.livesession

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import java.security.SecureRandom

class LivingSession {
    var reload: MutableLiveData<String>? = null
    val AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    var rnd = SecureRandom()

    fun randomString(len:Int):String {
        val sb = StringBuilder(len)
        for (i in 0 until len)
            sb.append(AB.get(rnd.nextInt(AB.length)))
        return sb.toString()
    }

    companion object {
        var sInstance: LivingSession? = null
        fun instance(): LivingSession {
            if (sInstance == null) {
                synchronized(LivingSession) {
                    sInstance = LivingSession()
                }
            }
            return sInstance!!
        }
    }
}