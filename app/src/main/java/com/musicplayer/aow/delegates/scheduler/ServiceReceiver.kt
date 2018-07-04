package com.musicplayer.aow.delegates.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.musicplayer.aow.delegates.player.PlaybackService


/**
 * Created by Arca on 12/4/2017.
 */
class ServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, PlaybackService::class.java))
    }
    
}