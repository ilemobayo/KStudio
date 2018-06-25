package com.musicplayer.aow.delegates.player

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState.*
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.webkit.URLUtil
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.ui.main.MainActivity
import com.musicplayer.aow.utils.receiver.AudioNoisy
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete


class PlaybackService : IPlayback, IPlayback.Callback, MediaBrowserServiceCompat() {

    private var notification: Notification? = null
    private var mContentViewBig: RemoteViews? = null
    private var mContentViewSmall: RemoteViews? = null

    private var mPlayer: Player? = Player.instance
    private var audioFocus = AudioFocus.instance
    
    private var mNoisyIntentFilter: IntentFilter? = null
    private var mAudioBecommingNoisy: AudioNoisy? = null

    private val mBinder = LocalBinder()

    override var isPlaying: Boolean = false
        get() = mPlayer!!.isPlaying

    override var progress: Int = 0
        get() = mPlayer!!.progress

    override var playingSong: Song? = null
        get() = mPlayer!!.playingSong

    override var playingList: PlayList? = null
        get() = mPlayer!!.playingList

    private val smallContentView: RemoteViews
        get() {
            if (mContentViewSmall == null) {
                mContentViewSmall = RemoteViews(packageName, R.layout.remote_view_music_player_small)
                setUpRemoteView(mContentViewSmall!!)
            }
            updateRemoteViews(mContentViewSmall!!)
            return mContentViewSmall!!
        }

    private val bigContentView: RemoteViews
        get() {
            if (mContentViewBig == null) {
                mContentViewBig = RemoteViews(packageName, R.layout.remote_view_music_player)
                setUpRemoteView(mContentViewBig!!)
            }
            updateRemoteViews(mContentViewBig!!)
            return mContentViewBig!!
        }

    inner class LocalBinder : Binder() {
        val service: PlaybackService
            get() = this@PlaybackService
    }


    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    override fun onCreate() {
        super.onCreate()
        mPlayer = Player.instance
        mPlayer!!.registerCallback(this)
        mAudioBecommingNoisy = AudioNoisy()
        mNoisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }

    @Throws(RemoteException::class)
    private fun initMediaSession(song: Song? = null) {
        if (mediaSessionManager != null) return  //mediaSessionManager exists

        mediaSessionManager = this.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession!!.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession!!.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        //Set mediaSession's MetaData
        if (song == null) {
            updateMetaData()
        }else{
            updateMetaData(song)
        }

        // Attach Callback to receive MediaSession updates
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()
                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                //Stop the service
                //stopSelf()
            }

        })
    }

    private fun handleIncomingActions(playbackAction: Intent) {
        if (playbackAction == null || playbackAction.action == null) return

        var actionString = playbackAction.action
        when {
            actionString.equals(ACTION_PLAY.toString(), true) -> transportControls!!.play()
            actionString.equals(ACTION_PAUSE.toString(), true) -> transportControls!!.pause()
            actionString.equals(ACTION_SKIP_TO_NEXT.toString(), true) -> transportControls!!.skipToNext()
            actionString.equals(ACTION_SKIP_TO_PREVIOUS.toString(), true) -> transportControls!!.skipToPrevious()
            actionString.equals(ACTION_STOP.toString(), true) -> {
                //transportControls!!.stop()
                removeNotification()
                stopForeground(true)
            }
        }
    }


    private fun updateMetaData(song: Song? = null) {

        var currentSong: Song? = null
        if (song != null){
            currentSong = song
        }else if(mPlayer != null) {
            currentSong = mPlayer!!.playingSong
        }
        var albumArt: Bitmap? = null
        if (currentSong != null) {
            if (currentSong.albumArt != null) {
                albumArt = BitmapFactory.decodeFile(currentSong!!.albumArt)
            } else {
                albumArt = BitmapFactory.decodeResource(resources, R.drawable.ic_logo)
            }
            //replace with medias albumArt
            // Update the current metadata
            mediaSession!!.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong!!.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong!!.album)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong!!.displayName)
                .build())
        }
    }

    fun skipToPrevious(){
       playLast()
    }

    fun skipToNext(){
        playNext()
    }

    fun pauseMedia(){
        pause()
    }

    fun resumeMedia(){
        play()
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {
        var currentSong: Song?
        if (mPlayer != null) {
            currentSong = mPlayer!!.playingSong

            var notificationAction = android.R.drawable.ic_media_pause//needs to be initialized
            var play_pauseAction: PendingIntent? = null

            //Build a new notification according to the current state of the MediaPlayer
            if (playbackStatus == PlaybackStatus.PLAYING) {
                notificationAction = android.R.drawable.ic_media_pause
                //create the pause action
                play_pauseAction = playbackAction(1)
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                notificationAction = android.R.drawable.ic_media_play
                //create the play action
                play_pauseAction = playbackAction(0)
            }

            var largeIcon: Bitmap? = null
            if (currentSong!!.albumArt != null) {
                largeIcon = BitmapFactory.decodeFile(currentSong!!.albumArt) //replace with your own image
            } else {
                largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_logo)
            }

            // Create a new Notification
            val notificationBuilder = NotificationCompat.Builder(applicationContext, "musixplay")
                    .setShowWhen(false)
                    // Set the Notification style
                    .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                            // Attach our MediaSession token
                            .setMediaSession(mediaSession!!.sessionToken)
                            // Show our playback controls in the compact notification view.
                            .setShowActionsInCompactView(0, 1, 2))
                    // Set the Notification color
                    .setColor(resources.getColor(R.color.red_trans))
                    // Set the large and small icons
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    // Set Notification content information
                    .setContentText(currentSong!!.artist)
                    .setContentTitle(currentSong!!.title)
                    .setContentInfo("")
                    // Add playback actions
                    .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                    .addAction(notificationAction, "pause", play_pauseAction)
                    .addAction(android.R.drawable.ic_media_next, "next",
                            playbackAction(2)) as NotificationCompat.Builder

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).
                    notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(applicationContext, PlaybackService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY.toString()
                return PendingIntent.getService(applicationContext, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE.toString()
                return PendingIntent.getService(applicationContext, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_SKIP_TO_NEXT.toString()
                return PendingIntent.getService(applicationContext, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_SKIP_TO_PREVIOUS.toString()
                return PendingIntent.getService(applicationContext, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }


    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // I promise we’ll get to browsing
        result.sendResult(null)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // Returning null == no one can connect
        // so we’ll return something
        return MediaBrowserServiceCompat.BrowserRoot(
                getString(R.string.application_name), // Name visible in Android Auto
                null) // Bundle of optional extras
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent != null) {
                val action = intent.action
                if (ACTION_PLAY_TOGGLE == action) {
                    if (isPlaying) {
                        audioFocus!!.pause()
                        pause()
                    } else {
                        audioFocus!!.play()
                        play()
                    }
                } else if (ACTION_PLAY_NEXT == action) {
                    playNext()
                } else if (ACTION_PLAY_LAST == action) {
                    playLast()
                } else if (ACTION_STOP_SERVICE == action) {
                    if (isPlaying) {
                        audioFocus!!.pause()
                        pause()
                    }
                    stopForeground(true)
                    //unregisterReceiver(mAudioBecommingNoisy)
                    //unregisterCallback(this)
                }
            }else{
                if (isPlaying) {
                    pause()
                } else {
                    play()
                }
            }
        } catch (e: NullPointerException){
            stopSelf()
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                //initMediaPlayer();
            } catch (e:RemoteException) {
                //stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }

        if (intent != null) {
            handleIncomingActions(intent!!)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun stopService(name: Intent): Boolean {
        stopForeground(true)
        unregisterCallback(this)
        unregisterReceiver(mAudioBecommingNoisy)
        return super.stopService(name)
    }

    override fun onDestroy() {
        releasePlayer()
        unregisterReceiver(mAudioBecommingNoisy)
        super.onDestroy()
    }

    override fun setPlayList(list: PlayList) {
        mPlayer!!.setPlayList(list)
    }

    override fun play(): Boolean {
        Log.e(this.javaClass.name, "auto play called")
        registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter)
        return mPlayer!!.play()
    }

    override fun play(list: PlayList): Boolean {
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                //initMediaPlayer();
            } catch (e:RemoteException) {
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }
        registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter)
        return mPlayer!!.play(list)
    }

    override fun play(list: PlayList, startIndex: Int): Boolean {
        registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter)
        return mPlayer!!.play(list, startIndex)
    }

    override fun play(song: Song): Boolean {
        registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter)
        return mPlayer!!.play(song)
    }

    override fun playLast(): Boolean {
        return mPlayer!!.playLast()
    }

    override fun playNext(): Boolean {
        Log.e(this.javaClass.name, "auto play next called")
        return mPlayer!!.playNext()
    }

    override fun pause(): Boolean {
        unregisterReceiver(mAudioBecommingNoisy)
        return mPlayer!!.pause()
    }

    override fun seekTo(progress: Int): Boolean {
        return mPlayer!!.seekTo(progress)
    }

    override fun setPlayMode(playMode: PlayMode) {
        mPlayer!!.setPlayMode(playMode)
    }

    override fun registerCallback(callback: IPlayback.Callback) {
        mPlayer!!.registerCallback(callback)
    }

    override fun unregisterCallback(callback: IPlayback.Callback) {
        mPlayer!!.unregisterCallback(callback)
    }

    override fun removeCallbacks() {
        mPlayer!!.removeCallbacks()
    }

    override fun releasePlayer() {
        mPlayer!!.releasePlayer()
        mPlayer = null
        audioFocus = null
        super.onDestroy()
    }

    // Playback Callbacks
    override fun onSwitchLast(last: Song?) {
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
            } catch (e:RemoteException) {
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    override fun onSwitchNext(next: Song?) {
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
            } catch (e:RemoteException) {
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    override fun onComplete(next: Song?) {
        Log.e(this.javaClass.name, "auto play next {comp.} called "+ next?.title)
        if (mediaSessionManager == null) {
            try {
                initMediaSession(next)
            } catch (e:RemoteException) {
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    override fun onPlayStatusChanged(isPlaying: Boolean) {
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
            } catch (e:RemoteException) {
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    // Notification

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(
                applicationContext, 0, Intent(applicationContext, MainActivity::class.java), 0)

        // Set the info for the views that show in the notification panel.
        notification = NotificationCompat.Builder(applicationContext,"musixplay")
                .setSmallIcon(R.drawable.ic_stat_name)  // the status icon
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setCustomContentView(smallContentView)
                .setCustomBigContentView(bigContentView)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .build()

        // Send the notification.
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setUpRemoteView(remoteView: RemoteViews) {
        remoteView.setImageViewResource(R.id.image_view_close, R.drawable.ic_remote_view_close)
        remoteView.setImageViewResource(R.id.image_view_play_last, R.drawable.ic_remote_view_play_last)
        remoteView.setImageViewResource(R.id.image_view_play_next, R.drawable.ic_remote_view_play_next)

        remoteView.setOnClickPendingIntent(R.id.button_close, getPendingIntent(ACTION_STOP_SERVICE))
        remoteView.setOnClickPendingIntent(R.id.button_play_last, getPendingIntent(ACTION_PLAY_LAST))
        remoteView.setOnClickPendingIntent(R.id.button_play_next, getPendingIntent(ACTION_PLAY_NEXT))
        remoteView.setOnClickPendingIntent(R.id.button_play_toggle, getPendingIntent(ACTION_PLAY_TOGGLE))
    }

    private fun updateRemoteViews(remoteView: RemoteViews) {
        val currentSong = mPlayer!!.playingSong
        if (currentSong != null) {
            remoteView.setTextViewText(R.id.text_view_name, currentSong.displayName)
            remoteView.setTextViewText(R.id.text_view_artist, currentSong.artist)
        }
        remoteView.setImageViewResource(R.id.image_view_play_toggle, if (isPlaying) {
                R.drawable.ic_remote_view_pause
            }else{
                R.drawable.ic_remote_view_play
            }
        )

        if ((currentSong != null) && (currentSong.albumArt != null)) {
            if(URLUtil.isHttpUrl(currentSong.albumArt) || URLUtil.isHttpsUrl(currentSong.albumArt)){
                doAsync {
                    var img = Glide.with(applicationContext)
                            .load(currentSong.albumArt).asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(80, 80)
                            .get()
                    onComplete {
                        if(img != null) {
                            remoteView.setImageViewBitmap(R.id.image_view_album, img)
                        }else {
                            remoteView.setImageViewUri(R.id.image_view_album, Uri.parse(currentSong.albumArt))
                        }
                    }
                }
            }else {
                try {
                    remoteView.setImageViewUri(R.id.image_view_album, Uri.parse(currentSong.albumArt))
                } catch (e: Exception) {
                    remoteView.setImageViewResource(R.id.image_view_album, R.drawable.nigerian_artists)
                }
            }
        }else{
            remoteView.setImageViewResource(R.id.image_view_album, R.drawable.nigerian_artists)
        }
    }

    // PendingIntent
    private fun getPendingIntent(action: String): PendingIntent {
        return PendingIntent.getService(applicationContext, 0, Intent(action), 0)
    }

    companion object {
        private val TAG = "Player"
        private const val ACTION_PLAY_TOGGLE = "com.musicplayer.aow.ACTION.PLAY_TOGGLE"
        private const val ACTION_PLAY_LAST = "com.musicplayer.aow.ACTION.PLAY_LAST"
        private const val ACTION_PLAY_NEXT = "com.musicplayer.aow.ACTION.PLAY_NEXT"
        private const val ACTION_STOP_SERVICE = "com.musicplayer.aow.ACTION.STOP_SERVICE"
        private const val NOTIFICATION_ID = 1
    }
}

