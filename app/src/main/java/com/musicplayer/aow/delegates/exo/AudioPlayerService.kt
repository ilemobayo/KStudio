/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.musicplayer.aow.delegates.exo

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.exo.C.MEDIA_SESSION_TAG
import com.musicplayer.aow.delegates.exo.C.PLAYBACK_CHANNEL_ID
import com.musicplayer.aow.delegates.exo.C.PLAYBACK_NOTIFICATION_ID

class AudioPlayerService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var player: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private var playList = com.musicplayer.aow.delegates.player.Player.instance?.mPlayList

    override fun onCreate() {
        super.onCreate()
        val context = this

        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
        val dataSourceFactory = DefaultDataSourceFactory(
                context, Util.getUserAgent(context, getString(R.string.application_name)))
        val cacheDataSourceFactory = CacheDataSourceFactory(
                DownloadUtil.getCache(context),
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val concatenatingMediaSource = ConcatenatingMediaSource()
        playList!!.tracks?.forEach {
            val mediaSource = ExtractorMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(Uri.parse(it.path))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        player!!.prepare(concatenatingMediaSource)
        player!!.playWhenReady = true

        Equalizer(0, player!!.audioSessionId)

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                PLAYBACK_NOTIFICATION_ID,
                object : MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player): String? {
                        return playList!!.tracks?.get(player.currentWindowIndex)?.displayName
                    }

                    override fun createCurrentContentIntent(player: Player): PendingIntent? {
                        return null
                    }

                    override fun getCurrentContentText(player: Player): String? {
                        return playList!!.tracks?.get(player.currentWindowIndex)?.artist
                    }

                    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
                        return null
                    }
                }
        )


        playerNotificationManager!!.setColorized(true)
        playerNotificationManager!!.setColor(Color.LTGRAY)
        playerNotificationManager!!.setSmallIcon(R.drawable.ic_logo)
        playerNotificationManager!!.setNotificationListener(object : NotificationListener {
            override fun onNotificationStarted(notificationId: Int, notification: Notification) {
                startForeground(notificationId, notification)
            }

            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
            }
        })
        playerNotificationManager!!.setPlayer(player)

        mediaSession = MediaSessionCompat(context, MEDIA_SESSION_TAG)
        mediaSession!!.isActive = true
        playerNotificationManager!!.setMediaSessionToken(mediaSession!!.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession!!)
        mediaSessionConnector!!.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                return playList?.getMediaDescription(0,playList!!.tracks?.get(windowIndex)!!)!!
            }
        })
        mediaSessionConnector!!.setPlayer(player, null)
    }

    override fun onDestroy() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
        unregisterReceiver(mNoisyReceiver)

        mediaSession!!.release()
        mediaSessionConnector!!.setPlayer(null, null)
        playerNotificationManager!!.setPlayer(null)
        player!!.release()
        player = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PLAY_TOGGLE == action) {

            } else if (ACTION_PLAY_NEXT == action) {
                if(player != null){
                    player!!.seekToDefaultPosition(player!!.nextWindowIndex)
                }
            } else if (ACTION_PLAY_LAST == action) {
                if(player != null){
                    player!!.seekToDefaultPosition(player!!.previousWindowIndex)
                }
            } else if (ACTION_STOP_SERVICE == action) {

            }
        }
        return Service.START_STICKY
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    private fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (successfullyRetrievedAudioFocus()) {
                    player!!.playWhenReady = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {

            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (player != null) {
                    player!!.setVolume(0.3f)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (player != null) {
                    if (successfullyRetrievedAudioFocus()) {
                        player!!.playWhenReady = true
                    }
                    player!!.setVolume(1.0f)
                }
            }
        }
    }

    // PendingIntent
    private fun getPendingIntent(action: String): PendingIntent {
        return PendingIntent.getService(applicationContext, 0, Intent(action), 0)
    }

    companion object {
        private const val TAG = "PlayerService"
        private const val ACTION_PLAY_TOGGLE = "com.musicplayer.aow.ACTION.PLAY_TOGGLE"
        private const val ACTION_PLAY_LAST = "com.musicplayer.aow.ACTION.PLAY_LAST"
        private const val ACTION_PLAY_NEXT = "com.musicplayer.aow.ACTION.PLAY_NEXT"
        private const val ACTION_STOP_SERVICE = "com.musicplayer.aow.ACTION.STOP_SERVICE"
        private const val NOTIFICATION_ID = 1
    }

}
