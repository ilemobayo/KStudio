package com.musicplayer.aow.utils

import android.content.Context
import android.graphics.Point
import android.provider.Settings
import android.view.WindowManager


/**
 * Created by Arca on 12/5/2017.
 */
object DeviceUtils {

    /**
     * @param context
     * @return the screen height in pixels
     */
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    /**
     * @param context
     * @return the screen width in pixels
     */
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun serialID(context: Context): String? {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}