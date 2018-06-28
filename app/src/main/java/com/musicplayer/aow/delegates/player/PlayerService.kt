package com.musicplayer.aow.delegates.player

import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.os.ResultReceiver
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.player.mediasession.MediaStyleHelper
import com.musicplayer.aow.utils.CursorDB
import java.util.*

class PlayerService: MediaBrowserServiceCompat(),
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener,
        IPlayback{

    var mediaPlayer: Player? = Player.instance
    private var mMediaPlayer: MediaPlayer? = null
    private var mMediaSessionCompat: MediaSessionCompat? = null
    private val mPlaylist = ArrayList<Song>()

    override var isPlaying: Boolean = false
        get() = mediaPlayer!!.isPlaying

    override var progress: Int = 0
        get() = mediaPlayer!!.progress

    override var playingSong: Song? = null
        get() = mediaPlayer!!.playingSong

    override var playingList: PlayList? = null
        get() = mediaPlayer!!.playingList

    override fun setPlayList(list: PlayList) {
        mediaPlayer!!.setPlayList(list)
    }

    override fun play(): Boolean {
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        initMediaSessionMetadata()
        showPlayingNotification()
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
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        initMediaSessionMetadata()
        showPausedNotification()
        return mediaPlayer!!.pause()
    }

    override fun seekTo(progress: Int): Boolean {
        return mediaPlayer!!.seekTo(progress)
    }

    override fun setPlayMode(playMode: PlayMode) {
        mediaPlayer!!.setPlayMode(playMode)
    }

    override val mPlayer: MediaPlayer?
        get() = null

    override fun registerCallback(callback: IPlayback.Callback) {

    }

    override fun unregisterCallback(callback: IPlayback.Callback) {

    }

    override fun removeCallbacks() {

    }

    override fun releasePlayer() {

    }


    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isPlaying) {
                pause()
            }
        }
    }

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            if (!successfullyRetrievedAudioFocus()) {
                return
            }

            //mMediaSessionCompat!!.isActive = true
            play()
        }

        override fun onPause() {
            super.onPause()

            if (isPlaying) {
                pause()
            }
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            playLast()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            playNext()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            play()
            //Work with extras here if you want
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            if (COMMAND_EXAMPLE.equals(command!!, ignoreCase = true)) {
                //Custom command here
            }
        }

    }

    override fun onCreate() {
        super.onCreate()

        initMediaPlayer()
        initMediaSession()
        initNoisyReceiver()
        initSongs()
    }

    private fun initSongs() {
        val data = CursorDB().songs(this)
        if (data != null) {
            while (data.moveToNext()) {
                mPlaylist.add(CursorDB().cursorToMusic(data))
            }
            data.close()
        }
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
        unregisterReceiver(mNoisyReceiver)
        mMediaSessionCompat!!.release()
        NotificationManagerCompat.from(this).cancel(1)
    }

    private fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer!!.setVolume(1.0f, 1.0f)
    }

    private fun showPlayingNotification() {
        val builder = MediaStyleHelper.from(this, mMediaSessionCompat!!)
                ?: return

        builder.addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
        builder.addAction(NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
        builder.addAction(NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
        builder.setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat!!.sessionToken))
        builder.setSmallIcon(R.drawable.ic_logo)
        NotificationManagerCompat.from(this).notify(1, builder.build())
    }

    private fun showPausedNotification() {
        val builder = MediaStyleHelper.from(this, mMediaSessionCompat!!) ?: return

        builder.addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
        builder.addAction(NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
        builder.addAction(NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
        builder.setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat!!.sessionToken))
        builder.setSmallIcon(R.drawable.ic_logo)
        NotificationManagerCompat.from(this).notify(1, builder.build())
    }


    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        mMediaSessionCompat = MediaSessionCompat(applicationContext, "Musixplay", mediaButtonReceiver, null)

        mMediaSessionCompat!!.setCallback(mMediaSessionCallback)
        mMediaSessionCompat!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mMediaSessionCompat!!.isActive = true

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        mMediaSessionCompat!!.setMediaButtonReceiver(pendingIntent)

        sessionToken = mMediaSessionCompat!!.sessionToken
    }

    private fun setMediaPlaybackState(state: Int) {
        val playbackstateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        mMediaSessionCompat!!.setPlaybackState(playbackstateBuilder.build())
    }

    private fun initMediaSessionMetadata() {
        val metadataBuilder = MediaMetadataCompat.Builder()
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(resources, R.drawable.ic_logo))
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Display Title")
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Display Subtitle")
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1)

        mMediaSessionCompat!!.setMetadata(metadataBuilder.build())
    }

    private fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        return result == AudioManager.AUDIOFOCUS_GAIN
    }


    //Not important for general audio service, required for class
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return if (TextUtils.equals(clientPackageName, packageName)) {
            MediaBrowserServiceCompat.BrowserRoot(getString(R.string.application_name), null)
        } else null

    }

    //Not important for general audio service, required for class
    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
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

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer!!.mPlayer!!.release()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {

        val COMMAND_EXAMPLE = "command_"
    }
}