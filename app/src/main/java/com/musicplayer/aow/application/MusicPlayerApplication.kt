package com.musicplayer.aow.application


import android.app.Application
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration
import com.birbit.android.jobqueue.log.CustomLogger
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService
import com.github.nisrulz.sensey.Sensey
import com.google.firebase.messaging.FirebaseMessaging
import com.musicplayer.aow.delegates.firebase.ForceUpdateChecker
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.scheduler.jobs.MyGcmJobService
import com.musicplayer.aow.delegates.scheduler.jobs.MyJobService
import org.jetbrains.anko.doAsync
import java.util.*


class MusicPlayerApplication : Application() {

    //Firebase update
    private val TAG = this.javaClass.name

    //Volley API call
    private var mRequestQueue: RequestQueue? = null

    //Equalizer
    private var eq: Equalizer? = null
    private var bb: BassBoost? = null
    private var reverb: PresetReverb? = null
    private var virtualizer: Virtualizer? = null

    var sensory  = Sensey.getInstance()

    private var jobManager: JobManager? = null

    override fun onCreate() {
        super.onCreate()
        applicationContext.startService(Intent(applicationContext, PlaybackService::class.java))
        //if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            //return;
        //}
        //LeakCanary.install(this);

        //stop detection
        sensory.stop()

        instance = this


        doAsync {
            //firebase remote config update code
            val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            // set in-app defaults
            val remoteConfigDefaults = HashMap<String, Any>()
            remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false)
            remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "0.1.1")
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

            //Volley
            getRequestQueue()

            //equalizer
            try {
                eq = Equalizer(0, Player.instance?.mPlayer?.audioSessionId!!)
                bb = BassBoost(0, Player.instance?.mPlayer?.audioSessionId!!)
                reverb = PresetReverb(0, Player.instance?.mPlayer?.audioSessionId!!)
                virtualizer = Virtualizer(0, Player.instance?.mPlayer?.audioSessionId!!)
            }catch (ex:Exception){
                //
            }

            //AndroidNetworking.initialize(applicationContext)
        }
    }


    override fun onTerminate() {
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


    private fun configureJobManager() {
        val builder = Configuration.Builder(applicationContext)
                .customLogger(object : CustomLogger {
                    private val TAG = "JOBS"
                    override fun isDebugEnabled(): Boolean {
                        return true
                    }

                    override fun d(text: String, vararg args: Any) {
                        Log.d(TAG, String.format(text, *args))
                    }

                    override fun e(t: Throwable, text: String, vararg args: Any) {
                        Log.e(TAG, String.format(text, *args), t)
                    }

                    override fun e(text: String, vararg args: Any) {
                        Log.e(TAG, String.format(text, *args))
                    }

                    override fun v(text: String, vararg args: Any) {

                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(applicationContext,
                    MyJobService::class.java), true)
        } else {
            val enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
            if (enableGcm == ConnectionResult.SUCCESS) {
                builder.scheduler(GcmJobSchedulerService.createSchedulerFor(applicationContext,
                        MyGcmJobService::class.java), true)
            }
        }
        jobManager = JobManager(builder.build())
    }

    fun getJobManager(): JobManager {
        if (jobManager == null) {
            configureJobManager()
        }
        return jobManager!!
    }


    companion object {

        var instance: MusicPlayerApplication? = null
            private set
    }
}
