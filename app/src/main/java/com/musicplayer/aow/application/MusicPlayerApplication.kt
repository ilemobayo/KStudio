package com.musicplayer.aow.application


import android.app.Application
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.androidnetworking.AndroidNetworking
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.nisrulz.sensey.Sensey
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.firebase.ForceUpdateChecker
import com.musicplayer.aow.delegates.player.AudioFocus
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.utils.Settings
import com.musicplayer.aow.utils.StorageUtil
import org.jetbrains.anko.doAsync
import java.util.*


class MusicPlayerApplication : Application() {
    //Firebase update
    private val TAG = Application::class.java.simpleName

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
    var appUpDater: AppUpdater? = null

    override fun onCreate() {
        super.onCreate()

        //firebase remote config update code
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        // set in-app defaults
        val remoteConfigDefaults = HashMap<String, Any>()
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false)
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "0.0.1")
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_URL,
                "https://play.google.com/store/apps/details?id=com.musicplayer.aow")

        firebaseRemoteConfig.setDefaults(remoteConfigDefaults)
        firebaseRemoteConfig.fetch(60) // fetch every minutes
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "remote config is fetched.")
                        firebaseRemoteConfig.activateFetched()
                    }
                }

        //Subscribe to firebase topics
        // subscribe
        FirebaseMessaging.getInstance().subscribeToTopic("update")
        FirebaseMessaging.getInstance().subscribeToTopic("music")

        //Unsubscribe
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("update")

        appUpDater = AppUpdater(this)
        setUpAppUpDater(appUpDater!!)
        appUpDater?.start()

        instance = this

        //Application Settings
        Settings.instance!!.intialization()

        audioFocus = AudioFocus.instance

        //Volley
        getRequestQueue()

        //Rx-Java Networking
        AndroidNetworking.initialize(applicationContext)

        //equalizer
        try {
            eq = Equalizer(0, Player.instance!!.mPlayer!!.audioSessionId)
            bb = BassBoost(0, Player.instance!!.mPlayer!!.audioSessionId)
            reverb = PresetReverb(0, Player.instance!!.mPlayer!!.audioSessionId)
            virtualizer = Virtualizer(0, Player.instance!!.mPlayer!!.audioSessionId)
        }catch (ex:Exception){
            //
        }

        doAsync {
            StorageUtil(applicationContext).storageLocationDir()
        }
    }

    fun setUpAppUpDater(appUpdater: AppUpdater){
        appUpdater.setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                .setTitleOnUpdateAvailable("Update available")
                .setContentOnUpdateAvailable("Check out the latest version available of my app!")
                .setTitleOnUpdateNotAvailable("Update not available")
                .setContentOnUpdateNotAvailable("No update available. Check for updates again later!")
                .setButtonUpdate("Update now?")
                //.setButtonUpdateClickListener()
                .setButtonDismiss("Maybe later")
                //.setButtonDismissClickListener(...)
                .setButtonDoNotShowAgain("Huh, not interested")
                //.setButtonDoNotShowAgainClickListener(...)
                .setIcon(R.drawable.ic_logo) // Notification icon
                .setCancelable(false) // Dialog could not be dismissable
    }

    override fun onTerminate() {
        if(appUpDater != null){
            appUpDater?.stop()
        }
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
