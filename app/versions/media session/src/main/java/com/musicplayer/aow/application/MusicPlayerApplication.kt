package com.musicplayer.aow.application


import android.app.Application
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.androidnetworking.AndroidNetworking
import com.github.nisrulz.sensey.Sensey
import com.google.firebase.messaging.FirebaseMessaging
import com.musicplayer.aow.delegates.player.AudioFocus
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.utils.Settings
import com.musicplayer.aow.utils.StorageUtil
import org.jetbrains.anko.doAsync

class MusicPlayerApplication : Application() {

    //Volley API call
    private var mRequestQueue: RequestQueue? = null

    //init objects
    private var audioFocus: AudioFocus? = null

    //Equalizer
    private var eq: Equalizer? = null
    private var bb: BassBoost? = null
    private var reverb: PresetReverb? = null
    private var virtualizer: Virtualizer? = null

    var sensory  = Sensey.getInstance()

    override fun onCreate() {
        super.onCreate()

        //Subscribe to firebase topics
        // subscribe
        FirebaseMessaging.getInstance().subscribeToTopic("update")
        FirebaseMessaging.getInstance().subscribeToTopic("music")

        //Unsubscribe
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("update")

        instance = this

        //Application Settings
        Settings.instance!!.intialization()

        audioFocus = AudioFocus.instance

        //Volley
        getRequestQueue()

        //Rx-Java Networking
        AndroidNetworking.initialize(applicationContext)

        //equalizer
        eq = Equalizer(0, Player.instance!!.mPlayer!!.audioSessionId)
        bb = BassBoost(0, Player.instance!!.mPlayer!!.audioSessionId)
        reverb = PresetReverb(0, Player.instance!!.mPlayer!!.audioSessionId)
        virtualizer = Virtualizer(0, Player.instance!!.mPlayer!!.audioSessionId)

        doAsync {
            StorageUtil(applicationContext).storageLocationDir()
        }
    }

    override fun onTerminate() {
        audioFocus!!.pause()
        super.onTerminate()
    }

    fun getEq(): Equalizer{
        if (eq == null){
            eq = Equalizer(0, Player.instance!!.mPlayer!!.audioSessionId)
        }
        return eq!!
    }

    fun getBassBoost(): BassBoost{
        if (bb == null){
            bb = BassBoost(0, Player.instance!!.mPlayer!!.audioSessionId)
        }
        return bb!!
    }

    fun getVirtualizer(): Virtualizer{
        if (virtualizer == null){
            virtualizer = Virtualizer(0, Player.instance!!.mPlayer!!.audioSessionId)
        }
        return virtualizer!!
    }

    fun getReverb(): PresetReverb{
        if (reverb == null){
            reverb = PresetReverb(0, Player.instance!!.mPlayer!!.audioSessionId)
        }
        return reverb!!
    }

    fun getRequestQueue(): RequestQueue? {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(applicationContext)
        }

        return mRequestQueue
    }


    companion object {
        var instance: MusicPlayerApplication? = null
            private set
    }
}
