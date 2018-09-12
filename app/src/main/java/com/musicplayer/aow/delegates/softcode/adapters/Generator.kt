package com.musicplayer.aow.delegates.softcode.adapters

import java.security.SecureRandom

class Generator {
    val AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    var rnd = SecureRandom()

    fun randomString(len:Int):String {
        val sb = StringBuilder(len)
        for (i in 0 until len)
            sb.append(AB.get(rnd.nextInt(AB.length)))
        return sb.toString()
    }
}