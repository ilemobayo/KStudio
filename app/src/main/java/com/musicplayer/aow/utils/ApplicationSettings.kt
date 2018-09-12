package com.musicplayer.aow.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.Log
import com.github.nisrulz.sensey.FlipDetector
import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.utils.FileUtilities.fileToMusic
import org.jetbrains.anko.doAsync
import java.io.File
import java.util.*


/**
 * Created by Arca on 11/23/2017.
 */
class ApplicationSettings {

    var context: Activity? = null
    var mPlayer: Player? = Player.instance
    var flipGesture = flipListener()
    var shakeGesture = shakeListner()
    val shakeaction = "shake"
    val flipaction = "flip"
    val cacheaction = "cache"

    private fun shakeListner(): ShakeDetector.ShakeListener {
        //Player instance
        val shakeListener = object : ShakeDetector.ShakeListener {
            override fun onShakeDetected() {
                if (mPlayer != null) {
                    if (mPlayer!!.isPlaying) {
                        mPlayer!!.playNext()
                    }
                }
            }
            override fun onShakeStopped() {
                // Shake stopped, do something
            }
        }
        return shakeListener
    }

    private fun flipListener(): FlipDetector.FlipListener {
        //Sensey Flip Gesture
        val flipListener = object : FlipDetector.FlipListener {
            override fun onFaceUp() {
                if (mPlayer != null) {
                    if (!mPlayer!!.isPlaying) {
                        mPlayer!!.play()
                    }
                }
            }
            override fun onFaceDown() {
                if (mPlayer != null) {
                    if (mPlayer!!.isPlaying) {
                        mPlayer!!.pause()
                    }
                }
            }
        }
        return flipListener
    }

    var a = 0


    companion object {

        private val TAG = "Setings"

        @Volatile private var sInstance: ApplicationSettings? = null

        val instance: ApplicationSettings?
            get() {
                if (sInstance == null) {
                    synchronized(ApplicationSettings.Companion) {
                        if (sInstance == null) {
                            sInstance = ApplicationSettings()
                        }
                    }
                }
                return sInstance
            }
    }

}