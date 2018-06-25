package com.musicplayer.aow.delegates.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/**
 * Created by Arca on 12/4/2017.
 */
class ServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        UtilServes().scheduleJob(context)
    }
}