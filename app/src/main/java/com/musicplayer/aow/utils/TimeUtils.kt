package com.musicplayer.aow.utils

import android.annotation.SuppressLint

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 9/2/16
 * Time: 6:07 PM
 * Desc: TimeUtils
 */
object TimeUtils {

    /**
     * Parse the time in milliseconds into String with the format: hh:mm:ss or mm:ss
     *
     * @param duration The time needs to be parsed.
     */
    @SuppressLint("DefaultLocale")
    fun formatDuration(duration: Int): String {
        var nduration = duration
        nduration /= 1000 // milliseconds into seconds
        var minute = nduration / 60
        val hour = minute / 60
        minute %= 60
        val second = nduration % 60
        return if (hour != 0)
            String.format("%2d:%02d:%02d", hour, minute, second)
        else
            String.format("%02d:%02d", minute, second)
    }
}
