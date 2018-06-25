package com.musicplayer.aow.ui.splashscreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.TempSongs
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.ui.main.MainActivity
import com.musicplayer.aow.utils.StorageUtil
import com.tedpark.tedpermission.rx1.TedRxPermission
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription


/**
 * Created by Arca on 10/2/2017.
 */

class SplashScreen : Activity() {

    private val mSubscriptions: CompositeSubscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this
        //permission
        startNextActivity()
    }

    private fun permission(){
        TedRxPermission.with(applicationContext)
                .setDeniedMessage("This permissions, are required, you can not use some services within the app.\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request()
                .subscribe({ tedPermissionResult ->
                    if (tedPermissionResult.isGranted) {
                        var storage = StorageUtil(applicationContext)
                        if(storage.loadStringValue("init").equals("empty",true)) {
                            AppRepository().createDefaultAllSongs()
                            doAsync {
                                TempSongs.instance!!.setSongs()
                                onComplete {
                                    updateAllSongsPlayList(PlayList(TempSongs.instance!!.songs))
                                    storage.saveStringValue("init","not empty")
                                    var intent = MainActivity.newIntent(applicationContext)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    ActivityCompat.startActivity(this@SplashScreen, intent, null)
                                }
                            }
                        }else{
                            var intent = MainActivity.newIntent(applicationContext)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ActivityCompat.startActivity(this, intent, null)
                        }
                    } else {
                        //
                    }
                }, { }) {
                    //
                }
    }

    private fun startNextActivity()
    {
        Handler().postDelayed(
        {
            permission()
        }, timeoutMillis.toLong())
    }

    private fun updateAllSongsPlayList(playList: PlayList) {
        val subscription = AppRepository().setInitAllSongs(playList).subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() { }

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                    }
                })
        mSubscriptions?.add(subscription)
    }

    companion object {
        // Splash screen timer
        private var timeoutMillis = 1000

        var instance: SplashScreen? = null
            private set
    }
}
