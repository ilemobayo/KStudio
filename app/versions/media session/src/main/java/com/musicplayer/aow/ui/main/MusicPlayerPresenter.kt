package com.musicplayer.aow.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.event.FavoriteChangeEvent
import com.musicplayer.aow.delegates.player.PlaybackService
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription


class MusicPlayerPresenter(private var mContext: Context?, private var mView: MusicPlayerContract.View?) : MusicPlayerContract.Presenter {
    private val mSubscriptions: CompositeSubscription = CompositeSubscription()

    private var mPlaybackService: PlaybackService? = null
    private var mIsServiceBound: Boolean = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mPlaybackService = (service as PlaybackService.LocalBinder).service
            mView!!.onPlaybackServiceBound(mPlaybackService!!)
            if (mPlaybackService!!.playingSong != null) {
                mView!!.onSongUpdated(mPlaybackService!!.playingSong)
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mPlaybackService = null
            mView!!.onPlaybackServiceUnbound()
        }
    }

    init {
        mView!!.setPresenter(this)
    }

    override fun subscribe() {
        bindPlaybackService()

        retrieveLastPlayMode()

        if (mPlaybackService != null && mPlaybackService!!.isPlaying) {
            mView!!.onSongUpdated(mPlaybackService!!.playingSong)
        } else {
            // - load last play list/folder/song
        }
    }

    override fun unsubscribe() {
        unbindPlaybackService()
        // Release context reference
        mContext = null
//        mView = null
        mSubscriptions.clear()
    }

    override fun retrieveLastPlayMode() {
        val lastPlayMode = PreferenceManager.lastPlayMode(mContext!!.applicationContext)
        mView!!.updatePlayMode(lastPlayMode)
    }



    override fun setSongAsFavorite(song: Song, favorite: Boolean) {
        val subscription = AppRepository().setSongAsFavorite(song, favorite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Song>() {
                    override fun onCompleted() {
                        // Empty
                    }

                    override fun onError(e: Throwable) {
                        mView!!.handleError(e)
                    }

                    override fun onNext(song: Song) {
                        mView!!.onSongSetAsFavorite(song)
                        RxBus.instance?.post(FavoriteChangeEvent(song))
                    }
                })
        mSubscriptions.add(subscription)
    }

    override fun bindPlaybackService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        mContext!!.bindService(Intent(mContext!!.applicationContext, PlaybackService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsServiceBound = true
    }

    override fun unbindPlaybackService() {
        if (mIsServiceBound) {
            // Detach our existing connection.
            mContext!!.unbindService(mConnection)
            mIsServiceBound = false
        }
    }
}
