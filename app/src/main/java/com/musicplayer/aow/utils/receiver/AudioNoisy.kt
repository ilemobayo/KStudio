package com.musicplayer.aow.utils.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.musicplayer.aow.delegates.player.Player

/**
 * Created by Arca on 11/29/2017.
 */
class AudioNoisy : BroadcastReceiver() {

    var mPlayer = Player.instance

    override fun onReceive(context: Context, intent: Intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.action) && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
        }
    }

}