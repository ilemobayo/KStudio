package com.musicplayer.aow.delegates.softcode.adapters

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.annotation.Nullable
import android.util.Log

class MXLiveService : Service() {
    var lives: Int = 0
    // Update seek bar every second
    val UPDATE_PROGRESS_INTERVAL: Long = 6000000
    private val mHandler = Handler()

    //BIND SERVICE
    private val mBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: MXLiveService
            get() = this@MXLiveService
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        lives = 0
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mHandler.removeCallbacks(mProgressCallback)
        mHandler.post(mProgressCallback)
        return START_STICKY
    }

    override fun onDestroy() {
        mHandler.removeCallbacks(mProgressCallback)
    }

    private val mProgressCallback = object : Runnable {
        override fun run() {
            lives++
            Log.e(this.javaClass.name, "${lives}")
            //Toast.makeText(applicationContext, "${lives}", Toast.LENGTH_SHORT).show()
            mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL)
        }
    }
}
