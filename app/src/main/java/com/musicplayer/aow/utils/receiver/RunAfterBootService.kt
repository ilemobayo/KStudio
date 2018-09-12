package com.musicplayer.aow.utils.receiver

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.utils.StorageUtil
import java.util.*

class RunAfterBootService : Service {

    var counter = 0
    //var context: Context = applicationContext

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    constructor() : super() {
        //context = applicationContext
        Log.d("HERE", "here service created!")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //startTimer()
        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("EXIT", "ondestroy!")

        val broadcastIntent = Intent("musix.play.Activit.Restart")
        sendBroadcast(broadcastIntent)
        stoptimertask()
    }

    fun startTimer() {
        //set a new Timer
        timer = Timer()

        //initialize the TimerTask's job
        initializeTimerTask()

        //schedule the timer, to wake up every 1 second
        timer!!.schedule(timerTask, 50000) //
    }

    fun initializeTimerTask() {
        timerTask = object : TimerTask() {
            override fun run() {

                //runCheck()

            }
        }
    }

    fun runCheck(){
        var storage = StorageUtil(Injection.provideContext()!!)

    }

    private fun updateAllSongsPlayList(playList: PlayList) {

    }

    fun stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}