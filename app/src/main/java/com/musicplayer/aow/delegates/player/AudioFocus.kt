package com.musicplayer.aow.delegates.player

import android.content.Context
import android.media.AudioManager
import com.musicplayer.aow.application.Injection

class AudioFocus: AudioManager.OnAudioFocusChangeListener{

    private var mPlayer: Player? = Player.instance
    private var mAudioManager: AudioManager? = Injection.provideContext()!!.
            getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun play(): Boolean{
        val requestAudioFocusResult = mAudioManager!!.
                requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun pause(){
       mAudioManager!!.abandonAudioFocus(this)
    }

    //Audio Focus
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {

            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mPlayer!!.isPlaying) {
                    mPlayer!!.pause()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mPlayer!!.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mPlayer != null && mPlayer!!.mPlayer != null) {
                    mPlayer!!.mPlayer!!.setVolume(0.3f, 0.3f)
                }
            }

            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                if (mPlayer != null && mPlayer!!.mPlayer != null) {
                    mPlayer!!.mPlayer!!.setVolume(1.0f, 1.0f)
                }
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mPlayer != null && mPlayer!!.mPlayer != null) {
                    if (!mPlayer!!.isPlaying) {
                        mPlayer!!.play()
                    }
                    mPlayer!!.mPlayer!!.setVolume(1.0f, 1.0f)
                }
            }

            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT ->{
                if (mPlayer != null && mPlayer!!.mPlayer != null) {
                    if (!mPlayer!!.isPlaying) {
                        mPlayer!!.play()
                    }
                }
            }

            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE -> {
                if (mPlayer != null && mPlayer!!.mPlayer != null) {
                    if (!mPlayer!!.isPlaying) {
                        mPlayer!!.play()
                    }
                }
            }

        }
    }

    companion object {

        private const val TAG = "Audio Focus."

        @Volatile private var sInstance: AudioFocus? = null

        val instance: AudioFocus?
            get() {
                if (sInstance == null) {
                    synchronized(AudioFocus.Companion) {
                        if (sInstance == null) {
                            sInstance = AudioFocus()
                        }
                    }
                }
                return sInstance
            }
    }

}