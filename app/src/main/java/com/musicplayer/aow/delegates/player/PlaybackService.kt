package com.musicplayer.aow.delegates.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.webkit.URLUtil
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.ui.main.MainActivity
import com.musicplayer.aow.utils.receiver.AudioNoisy
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete

class PlaybackService : Service(), IPlayback, IPlayback.Callback, AudioManager.OnAudioFocusChangeListener {

    override val mPlayer: MediaPlayer?
        get() = null

    private var notification: Notification? = null
    private var mContentViewBig: RemoteViews? = null
    private var mContentViewSmall: RemoteViews? = null

    var mediaPlayer: Player? = Player.instance

    private var mAudioBecommingNoisy: AudioNoisy? = null

    private val mBinder = LocalBinder()

    override var isPlaying: Boolean = false
        get() = mediaPlayer!!.isPlaying

    override var progress: Int = 0
        get() = mediaPlayer!!.progress

    override var playingSong: Song? = null
        get() = mediaPlayer!!.playingSong

    override var playingList: PlayList? = null
        get() = mediaPlayer!!.playingList

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

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = Player.instance
        mediaPlayer!!.registerCallback(this)
        initNoisyReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PLAY_TOGGLE == action) {
                if (isPlaying) {
                    pause()
                } else {
                    if (successfullyRetrievedAudioFocus()) {
                        play()
                    }
                }
            } else if (ACTION_PLAY_NEXT == action) {
                playNext()
            } else if (ACTION_PLAY_LAST == action) {
                playLast()
            } else if (ACTION_STOP_SERVICE == action) {
                if (isPlaying) {
                    pause()
                }
            }
        }else{
            if (isPlaying) {
                pause()
            } else {
                if (successfullyRetrievedAudioFocus()) {
                    play()
                }
            }
        }
        return START_STICKY_COMPATIBILITY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return false
    }

    fun recreate(){
        mediaPlayer = Player.instance
        mediaPlayer!!.registerCallback(this)
        initNoisyReceiver()
    }

    override fun stopService(name: Intent): Boolean {
        //stopForeground(true)
        unregisterCallback(this)
        return super.stopService(name)
    }

    override fun onDestroy() {
        releasePlayer()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
        unregisterReceiver(mNoisyReceiver)
        super.onDestroy()
    }

    override fun setPlayList(list: PlayList) {
        mediaPlayer!!.setPlayList(list)
    }

    override fun play(): Boolean {
        return mediaPlayer!!.play()
    }

    override fun play(list: PlayList): Boolean {
        return mediaPlayer!!.play(list)
    }

    override fun play(list: PlayList, startIndex: Int): Boolean {
        return mediaPlayer!!.play(list, startIndex)
    }

    override fun play(song: Song): Boolean {
        return mediaPlayer!!.play(song)
    }

    override fun playLast(): Boolean {
        return mediaPlayer!!.playLast()
    }

    override fun playNext(): Boolean {
        return mediaPlayer!!.playNext()
    }

    override fun pause(): Boolean {
        return mediaPlayer!!.pause()
    }

    override fun seekTo(progress: Int): Boolean {
        return mediaPlayer!!.seekTo(progress)
    }

    override fun setPlayMode(playMode: PlayMode) {
        mediaPlayer!!.setPlayMode(playMode)
    }

    override fun registerCallback(callback: IPlayback.Callback) {
        mediaPlayer!!.registerCallback(callback)
    }

    override fun unregisterCallback(callback: IPlayback.Callback) {
        mediaPlayer!!.unregisterCallback(callback)
    }

    override fun removeCallbacks() {
        mediaPlayer!!.removeCallbacks()
    }

    override fun releasePlayer() {
        mediaPlayer!!.releasePlayer()
    }

    // Playback Callbacks
    override fun onSwitchLast(last: Song?) {
        showNotification()
    }

    override fun onSwitchNext(next: Song?) {
        showNotification()
    }

    override fun onComplete(next: Song?) {
        showNotification()
    }

    override fun onPlayStatusChanged(isPlaying: Boolean) {
        showNotification()
    }

    override fun onTriggerLoading(isLoading: Boolean) {
        //showNotification()
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
                .setSmallIcon(R.drawable.ic_logo)  // the status icon
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
        remoteView.setImageViewResource(R.id.image_view_play_toggle, if (isPlaying) {
            R.drawable.ic_remote_view_pause
        }else{
            R.drawable.ic_remote_view_play
        }
        )
        remoteView.setImageViewResource(R.id.image_view_close, R.drawable.ic_remote_view_close)
        remoteView.setImageViewResource(R.id.image_view_play_last, R.drawable.ic_remote_view_play_last)
        remoteView.setImageViewResource(R.id.image_view_play_next, R.drawable.ic_remote_view_play_next)
        remoteView.setOnClickPendingIntent(R.id.button_close, getPendingIntent(ACTION_STOP_SERVICE))
        remoteView.setOnClickPendingIntent(R.id.button_play_last, getPendingIntent(ACTION_PLAY_LAST))
        remoteView.setOnClickPendingIntent(R.id.button_play_next, getPendingIntent(ACTION_PLAY_NEXT))
        remoteView.setOnClickPendingIntent(R.id.button_play_toggle, getPendingIntent(ACTION_PLAY_TOGGLE))
    }

    private fun updateRemoteViews(remoteView: RemoteViews) {
        val currentSong = mediaPlayer!!.playingSong
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
                    val img = Glide.with(applicationContext)
                            .load(currentSong.albumArt).asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.gradient_info)
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

                doAsync {
                    val alb = Injection.provideContext()!!
                            .contentResolver.query(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            arrayOf(
                                    MediaStore.Audio.Albums._ID,
                                    MediaStore.Audio.Albums.ALBUM_ART),
                            MediaStore.Audio.Albums._ID + "=?",
                            arrayOf(currentSong.albumArt!!),
                            null)
                    var bit:Bitmap? = null
                    if (alb.moveToFirst()) {
                        val data = alb.getString(alb.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                        bit = Glide.with(applicationContext)
                                .load(data).asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .error(R.drawable.gradient_info)
                                .into(100, 100)
                                .get()
                    }
                    alb.close()
                    onComplete {
                        if (bit != null) {
                            remoteView.setImageViewBitmap(R.id.image_view_album, bit)
                        } else {
                            remoteView.setImageViewResource(R.id.image_view_album, R.drawable.gradient_danger)
                        }
                    }

                }
            }
        }else{
            remoteView.setImageViewResource(R.id.image_view_album, R.drawable.gradient_danger)
        }
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
            if (isPlaying) {
                pause()
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (isPlaying) {
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer != null) {
                    mediaPlayer!!.mPlayer!!.setVolume(0.3f, 0.3f)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer != null) {
                    if (!isPlaying) {
                        play()
                    }
                    mediaPlayer!!.mPlayer!!.setVolume(1.0f, 1.0f)
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

