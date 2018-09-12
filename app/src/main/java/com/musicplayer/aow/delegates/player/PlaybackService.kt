package com.musicplayer.aow.delegates.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.exo.C
import com.musicplayer.aow.delegates.exo.C.PLAYBACK_CHANNEL_ID
import com.musicplayer.aow.delegates.exo.C.PLAYBACK_NOTIFICATION_ID
import com.musicplayer.aow.delegates.player.mediasession.MediaStyleHelper
import com.musicplayer.aow.delegates.player.mediasession.PlayerNotificationManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.runOnUiThread

class PlaybackService : MediaBrowserServiceCompat(), IPlayback, IPlayback.Callback, AudioManager.OnAudioFocusChangeListener {

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private val mAudioManager = Injection.provideContext()?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mPlaybackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

    private var mFocusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mAudioManager.requestAudioFocus(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build())
    } else {
        //VERSION.SDK_INT < O
        mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }
    override val mPlayer: SimpleExoPlayer?
        get() = Player.instance?.mPlayer

    private var mediaSession: MediaSessionCompat? = MediaSessionCompat(Injection.provideContext()!!, C.MEDIA_SESSION_TAG)
    private var mMediaContentObserver: MyContentObserver? = null
    private var mFileObserver: FileObserver? = null

    var mediaPlayer: Player? = Player.instance

    override var isPlaying: Boolean = false
        get() = mediaPlayer!!.isPlaying

    override var progress: Int = 0
        get() = mediaPlayer!!.progress

    override var playingTrack: Track? = null
        get() = mediaPlayer!!.playingTrack

    override var playingList: PlayList? = null
        get() = mediaPlayer!!.playingList

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        initMediaSession()
        initNoisyReceiver()
        initObservers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
//        return super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

//    override fun stopService(name: Intent): Boolean {
//        stopForeground(true)
//        unregisterCallback(this)
//        return super.stopService(name)
//    }

    override fun onDestroy() {
        releasePlayer()
        abandonAudioFocus()
        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null, null);
        releaseObservers()
        mMediaContentObserver = null
        stopForeground(true)
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

    override fun play(track: Track): Boolean {
        return mediaPlayer!!.play(track)
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
        mediaPlayer!!.pause()
        mediaPlayer!!.releasePlayer()
    }

    // Playback Callbacks
    override fun onSwitchLast(last: Track?) {
        showNotification()
    }

    override fun onSwitchNext(next: Track?) {
        showNotification()
    }

    override fun onComplete(next: Track?) {
        showNotification()
    }

    override fun onPlayStatusChanged(isPlaying: Boolean) {
        showNotification()
    }

    override fun onTriggerLoading(isLoading: Boolean) {

    }

    override fun onPrepared(isPrepared: Boolean) {

    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            Log.e(TAG, "Play Media Button")
            if (successfullyRetrievedAudioFocus()) {
                play()
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                showNotification()
            }
            super.onPlay()
        }

        override fun onPause() {
            Log.e(TAG, "Pause Media Button")
            if (isPlaying) {
                pause()
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                showNotification()
            }
            super.onPause()
        }

        override fun onSkipToNext() {
            Log.e(TAG, "play next")
            playNext()
            showNotification()
            super.onSkipToNext()
        }

        override fun onSkipToPrevious() {
            Log.e(TAG, "play previous")
            playLast()
            showNotification()
            super.onSkipToPrevious()
        }

        override fun onStop() {
            Log.e(TAG, "stop foreground")
            stopForeground(true)
            releasePlayer()
            super.onStop()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            if (successfullyRetrievedAudioFocus()) {
                play(mediaPlayer?.playingList!!, mediaId?.toInt()!!)
                showNotification()
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            val intentAction = mediaButtonEvent?.action
            if (Intent.ACTION_MEDIA_BUTTON != intentAction)
            {
                return false
            }
            val mediaButtonEventKey = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent
            val keycode = mediaButtonEventKey.keyCode
            when (keycode) {
                KeyEvent.KEYCODE_HEADSETHOOK -> {

                }
                KeyEvent.KEYCODE_MEDIA_PLAY ->{
                    if (successfullyRetrievedAudioFocus()) {
                        play()
                        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                        showNotification()
                    }
                }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    if (isPlaying) {
                        pause()
                        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                        showNotification()
                        stopForeground(false)
                    }
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (isPlaying) {
                        pause()
                        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                        showNotification()
                        stopForeground(false)
                    }else{
                        if (successfullyRetrievedAudioFocus()) {
                            play()
                            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                            showNotification()
                        }
                    }
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    playNext()
                    showNotification()
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    playLast()
                    showNotification()
                }
                KeyEvent.KEYCODE_MEDIA_STOP -> {
                    stopForeground(true)
                    releasePlayer()
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

    }

    private fun initMediaPlayer() {
        mediaPlayer = Player.instance
        mediaPlayer!!.registerCallback(this)
        mediaPlayer!!.attachEventListener()
    }

    /**
     * Show a notification while this service is running.
     */
    fun showNotification(){
        if (isPlaying) {
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
        val song = mediaPlayer!!.playingTrack
        if (song != null) {
            if (song.albumArt != null && song.albumArt != "null") {
                if (URLUtil.isHttpUrl(song.albumArt) || URLUtil.isHttpsUrl(song.albumArt)) {
                    doAsync {
                        val img = Glide.with(applicationContext)
                                .asBitmap()
                                .load(song.albumArt)
                                .apply(
                                        RequestOptions()
                                                .placeholder(R.drawable.gradient_info)
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                )
                                .submit(250, 250)
                                .get()
                        onComplete {
                            buildNotification(song, img)
                        }
                    }
                } else {
                    doAsync {
                        val img = Glide.with(applicationContext)
                                .asBitmap()
                                .load(song.albumArt)
                                .apply(
                                        RequestOptions()
                                                .placeholder(R.drawable.gradient_danger)
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                )
                                .submit(400, 400)
                                .get()
                        onComplete {
                            if (img != null) {
                                buildNotification(song, img)
                            } else {
                                buildNotification(song)
                            }
                        }
                    }
                }
            } else {
                buildNotification(song)
            }
        }
    }

    private fun mediaSessionMetadata(track: Track?, bitmap: Bitmap? = null): MediaMetadataCompat? {
        val metadataBuilder = MediaMetadataCompat.Builder()
        if (bitmap != null) {
            //Notification icon in card
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            //lock screen icon for pre lollipop
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
        }else{
            //Notification icon in card
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                    BitmapFactory.decodeResource(resources, R.drawable.gradient_danger))
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    BitmapFactory.decodeResource(resources, R.drawable.gradient_danger))
            //lock screen icon for pre lollipop
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                    BitmapFactory.decodeResource(resources, R.drawable.gradient_danger))
        }
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, track?.title)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, track?.artist)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, track?.description)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, playingList?.playingIndex?.toLong()!!)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playingList?.playingIndex?.toLong()!!)
        return metadataBuilder.build()
    }

    private fun buildNotification(track: Track, bitmap: Bitmap? = null) {
        mediaSession?.setMetadata(mediaSessionMetadata(track, bitmap))
        val builder = MediaStyleHelper.from(applicationContext, mediaSession!!)
        builder.addAction(
                NotificationCompat.Action(
                        android.R.drawable.ic_media_previous,
                        "Previous",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                applicationContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))

        if (isPlaying) {
            builder.addAction(
                    NotificationCompat.Action(
                            android.R.drawable.ic_media_pause,
                            "Pause",
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    applicationContext, PlaybackStateCompat.ACTION_PAUSE)))
            builder.setOngoing(true)
        }else{
            builder.addAction(
                    NotificationCompat.Action(
                            android.R.drawable.ic_media_play,
                            "Play",
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    applicationContext, PlaybackStateCompat.ACTION_PLAY)))
            builder.setOngoing(false)
        }

        builder.addAction(
                NotificationCompat.Action(
                        android.R.drawable.ic_media_next,
                        "Next",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                applicationContext, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))

        builder.addAction(android.R.drawable.ic_notification_clear_all, "Stop",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                applicationContext, PlaybackStateCompat.ACTION_STOP))

        builder.setStyle(
                android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession?.sessionToken)
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(1,2)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        applicationContext, PlaybackStateCompat.ACTION_STOP)))
        builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                PlaybackStateCompat.ACTION_STOP))

        builder.setSmallIcon(R.drawable.ic_logo)
        builder.color = ContextCompat.getColor(applicationContext, R.color.red_dim)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun setMediaPlaybackState(state: Int) {
        mediaSession?.setPlaybackState(PlaybackStateCompat.Builder().
            setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).
                setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f).build())
    }

    private fun initMediaSession() {
        val state = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_STOP or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(applicationContext, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0)
        mediaSession = MediaSessionCompat(applicationContext, C.MEDIA_SESSION_TAG, mediaButtonReceiver, pendingIntent)

        mediaSession!!.setCallback(mediaSessionCallback)
        mediaSession!!.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession!!.setPlaybackState(state.build())

        mediaSession!!.isActive = true
        mediaPlayer?.mediaSessionToken = mediaSession?.sessionToken
        sessionToken = mediaPlayer?.mediaSessionToken
    }

    private fun initObservers(){
        val URI_AUDIO_MEDIA = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val URI_AUDIO_PLAYLIST = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val URI_AUDIO_ALBUM = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val URI_AUDIO_ARTIST = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        mMediaContentObserver = MyContentObserver(Handler(), applicationContext)
        contentResolver.registerContentObserver(URI_AUDIO_MEDIA,true, mMediaContentObserver)
        contentResolver.registerContentObserver(URI_AUDIO_PLAYLIST,true, mMediaContentObserver)
        contentResolver.registerContentObserver(URI_AUDIO_ALBUM,true, mMediaContentObserver)
        contentResolver.registerContentObserver(URI_AUDIO_ARTIST,true, mMediaContentObserver)

        //mFileObserver = MyFileObserver(applicationContext, applicationContext?.externalCacheDir?.absolutePath + "/onlinedata")
        //mFileObserver?.startWatching()
    }

    private fun releaseObservers(){
        contentResolver.unregisterContentObserver(mMediaContentObserver)
        mMediaContentObserver = null
        //mFileObserver?.stopWatching()
        mFileObserver = null
    }


    //Not important for general audio service, required for class
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return if (TextUtils.equals(clientPackageName, packageName)) {
            MediaBrowserServiceCompat.BrowserRoot("__ROOT__", null)
        } else {
            MediaBrowserServiceCompat.BrowserRoot("__ROOT__", null)
        }
    }

    //Not important for general audio service, required for class
    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val list: ArrayList<MediaBrowserCompat.MediaItem> = ArrayList()
        result.detach()
        mediaPlayer?.mPlayList?.tracks?.forEachIndexed { index, track ->
            list.add(MediaBrowserCompat.MediaItem(
                    mediaPlayer?.mPlayList?.getMediaDescription(index,track)!!,
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE or MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
        }

        result.sendResult(list)
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    private fun successfullyRetrievedAudioFocus(): Boolean {
        initNoisyReceiver()
        return mFocusRequest == AudioManager.AUDIOFOCUS_GAIN
    }

    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isPlaying) {
                pause()
            }
        }
    }

    private fun abandonAudioFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            mAudioManager.abandonAudioFocusRequest(focusRequest)
        }else{
            mAudioManager.abandonAudioFocus(this)
        }
        unregisterReceiver(mNoisyReceiver)
    }

    fun registerAudioFocus(){

    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer != null && isPlaying) {
                    pause()
                    abandonAudioFocus()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer != null && isPlaying) {
                    pause()
                    abandonAudioFocus()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer != null) {
                    mediaPlayer?.mPlayer?.volume = 0.3f
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer != null) {
                    if (!isPlaying) {
                        play()
                    }
                    mediaPlayer?.mPlayer?.volume = 1.0f
                }
            }
        }
    }

    companion object {
        private const val TAG = "PlayerService"
        private const val NOTIFICATION_ID = 101
    }
}

