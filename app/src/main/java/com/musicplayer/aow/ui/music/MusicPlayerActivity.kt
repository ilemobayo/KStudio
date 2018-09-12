package com.musicplayer.aow.ui.music

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.URLUtil
import android.widget.*
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.player.IPlayback
import com.musicplayer.aow.delegates.player.PlayMode
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.main.MusicPlayerContract
import com.musicplayer.aow.ui.main.MusicPlayerPresenter
import com.musicplayer.aow.ui.main.library.activities.ArtistSongs
import com.musicplayer.aow.ui.nowplaying.NowPlayingActivity
import com.musicplayer.aow.utils.images.BitmapDraws
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.fragment_music.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.runOnUiThread
import java.io.File


class MusicPlayerActivity : BottomSheetDialogFragment(), MusicPlayerContract.View, IPlayback.Callback, View.OnClickListener{

    internal lateinit var view: View
    internal lateinit var this_activity: FragmentActivity
    //init objects
    private var songFavDatabase: SongFavDatabase? = SongFavDatabase.getsInstance(Injection.provideContext()!!)
    private var playlistDatabase = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    private var imageViewAlbum: ImageView? = null
    private var textViewName: TextView? = null
    private var textViewArtist: TextView? = null
    private var textViewProgress: TextView? = null
    private var textViewDuration: TextView? = null
    private var seekBarProgress: SeekBar? = null

    private var buttonPlayModeToggle: ImageView? = null
    private var buttonPlayToggle: ImageView? = null
    private var buttonFavoriteToggle: ImageView? = null
    private var buttonPlayNext: ImageView? = null
    private var buttonPlayPrev: ImageView? = null
    private var playBackVolume: SeekBar? = null
    private var audioManager: AudioManager? = null

    private var mPlayer: IPlayback? = Player.instance
    private val mHandler = Handler()
    private var mPresenter: MusicPlayerContract.Presenter? = null
    private  var mview: ViewGroup? = null
    private var lyric: File? = null

    /**
     * MEDIASESSION INTEGRATION
     */
    private val STATE_PAUSED = 0
    private val STATE_PLAYING = 1

    private var mCurrentState: Int = 0

    private val currentSongDuration: Int
        get() {
            return if (mPlayer!!.playingTrack != null) {
                val currentSong = mPlayer!!.playingTrack
                var duration = 0
                if(currentSong != null){
                    duration = Player.instance?.duration()?.toInt() ?: 0
                }
                duration
            }else{
                0
            }
        }

    private val mProgressCallback = object : Runnable {
        override fun run() {
            if (mPlayer!!.isPlaying) {
                val progress = (seekBarProgress!!.max * (mPlayer!!.progress.toFloat() / currentSongDuration.toFloat())).toInt()
                updateDuration()
                updateProgressTextWithDuration(mPlayer!!.progress)
                if (progress >= 0 && progress <= seekBarProgress!!.max) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        seekBarProgress!!.setProgress(progress, true)
                    } else {
                        seekBarProgress!!.progress = progress
                    }
                    mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL)
                }
            }
        }
    }

    var anim = RotateAnimation(0.0f, 360.0f , Animation.RELATIVE_TO_SELF, .5f,
            Animation.RELATIVE_TO_SELF, .5f)

    fun getInstance(): MusicPlayerActivity {
        return MusicPlayerActivity()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context != null) {
            this_activity = context as FragmentActivity
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { s_dialog ->
            val d = s_dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<FrameLayout>(android.support.design.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        view = inflater.inflate(R.layout.fragment_music, container, false)
        if (context != null) {
            this_activity = context as FragmentActivity
        }
        //BottomSheetBehavior.from(view).state = BottomSheetBehavior.STATE_EXPANDED

        setupToolbar()

        //volumeControlStream = AudioManager.STREAM_MUSIC

        textViewProgress = view.findViewById(R.id.text_view_progress)
        textViewName = view.findViewById(R.id.text_view_name)
        textViewArtist = view.findViewById(R.id.text_view_artist)
        textViewDuration = view.findViewById(R.id.text_view_duration)
        imageViewAlbum = view.findViewById(R.id.image_view_album)
        buttonPlayToggle = view.findViewById(R.id.button_play_toggle)
        buttonFavoriteToggle = view.findViewById(R.id.button_favorite_toggle)
        buttonPlayModeToggle = view.findViewById(R.id.button_play_mode_toggle)
        buttonPlayNext = view.findViewById(R.id.button_play_next)
        buttonPlayPrev = view.findViewById(R.id.button_play_last)
        seekBarProgress = view.findViewById(R.id.seek_bar)
        playBackVolume = view.findViewById(R.id.playback_SeekBar)


        textViewName!!.isSelected = true


        textViewArtist!!.setOnClickListener {
            if(mPlayer != null) {
                if (mPlayer!!.isPlaying) {
                    val intent = Intent(view.context, ArtistSongs::class.java)
                    intent.putExtra("com.musicplayer.aow.artist.name", mPlayer!!.playingTrack!!.artist)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ContextCompat.startActivity(view.context, intent, null)
                }
            }
        }

        //Display now playing list
        view.findViewById<AppCompatImageView>(R.id.android_now_playing).setOnClickListener {
            optionMenu(view.context, Player.instance!!.playingTrack!!)
        }

        //Play Next button
        buttonPlayNext!!.setOnClickListener{
            onPlayNextAction()
        }

        //Play Next button
        buttonPlayPrev!!.setOnClickListener{
            onPlayLastAction()
        }

        //Favourite button
        buttonFavoriteToggle!!.setOnClickListener{
            onFavoriteToggleAction()
        }

        //PlayMode button
        buttonPlayModeToggle!!.setOnClickListener{
            onPlayModeToggleAction()
        }

        //PlayBack Speed
        audioManager = view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        playBackVolume!!.max = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)

        playBackVolume!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        seekBarProgress!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateProgressTextWithProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mProgressCallback)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekTo(getDuration(seekBar.progress))
                if (mPlayer!!.isPlaying) {
                    mHandler.removeCallbacks(mProgressCallback)
                    mHandler.post(mProgressCallback)
                }
            }
        })

        buildTransportControls()

        MusicPlayerPresenter(this_activity, this).subscribe()

        view.setOnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE)
                    //Raise the Volume Bar on the Screen
                    playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    //Adjust the Volume
                    audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE)
                    //Lower the VOlume Bar on the Screen
                    playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    true
                }
                KeyEvent.KEYCODE_BACK -> {
                    //finish()
                    true
                }
                else -> false
            }
        }

        return view
    }

    override fun onClick(v: View) {
        //nothing
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE)
                //Raise the Volume Bar on the Screen
                playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                //Adjust the Volume
                audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE)
                //Lower the VOlume Bar on the Screen
                playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                //finish()
                return true
            }
            else -> return false
        }
    }



    fun buildTransportControls() {
        // Grab the view for the play/pause button
        //mPlayPause = (ImageView) findViewById(R.id.play_pause);

        val mediaController = MediaControllerCompat.getMediaController(this_activity)
        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)

        // Display the initial state
        val metadata = mediaController.metadata
        if (metadata != null){
            val name = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            val artistname = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
            if (name != null) {
                textViewName!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            }
            if (artistname != null) {
                textViewArtist!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
            }
            imageViewAlbum!!.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
            if (mPlayer!!.playingTrack != null) {
                isFavorite(mPlayer!!.playingTrack!!)
            }
        }

        val pbState = mediaController.playbackState
        when (pbState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                mCurrentState = STATE_PLAYING
                buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                mCurrentState = STATE_PAUSED
                buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
            }
            PlaybackStateCompat.STATE_BUFFERING -> {

            }
            PlaybackStateCompat.STATE_CONNECTING -> {

            }
        }

        // Attach a listener to the button
        //Play/Pause button
        buttonPlayToggle!!.setOnClickListener{
            onPlayToggleAction(MediaControllerCompat.getMediaController(this_activity).playbackState.state)
        }


    }

    private var controllerCallback:MediaControllerCompat.Callback = object:MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            textViewName!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            textViewArtist!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
            imageViewAlbum!!.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
            if (mPlayer!!.playingTrack != null) {
                isFavorite(mPlayer!!.playingTrack!!)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            super.onPlaybackStateChanged(state)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    mCurrentState = STATE_PLAYING
                    buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
                    if (mPlayer!!.isPlaying) {
                        mHandler.removeCallbacks(mProgressCallback)
                        mHandler.post(mProgressCallback)
                    }
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    mCurrentState = STATE_PAUSED
                    buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
                    if (mPlayer!!.isPlaying) {
                        mHandler.removeCallbacks(mProgressCallback)
                    }
                }
                PlaybackStateCompat.STATE_BUFFERING -> {

                }
                PlaybackStateCompat.STATE_CONNECTING -> {

                }
            }
        }
    }










    override fun onStart() {
        super.onStart()
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
        }
    }

    override fun onStop() {
        mHandler.removeCallbacks(mProgressCallback)
        super.onStop()
    }

    override fun onDestroy() {
        mPresenter!!.unsubscribe()
        super.onDestroy()
    }

    // A method to find height of the status bar
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun setupToolbar() {
        val toolbar = view.findViewById<RelativeLayout>(R.id.toolbar_layer)
        val toolbarNavigation = view.findViewById<AppCompatImageView>(R.id.toolbar_album_song_list)
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
        toolbarNavigation.setOnClickListener {
            this.dismiss()
        }
    }

    // Click Events
    private fun onPlayToggleAction(playerState: Int) {
        if (Player.instance == null) return

        if (playerState == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.getMediaController(this_activity).transportControls.pause()
            buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
        } else {
            MediaControllerCompat.getMediaController(this_activity).transportControls.play()
            buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun onPlayModeToggleAction() {
        if (mPlayer == null) return

        val current = PreferenceManager.lastPlayMode(view.context)
        val newMode = PlayMode.switchNextMode(current)
        PreferenceManager.setPlayMode(view.context, newMode)
        mPlayer?.setPlayMode(newMode)
        updatePlayMode(newMode)
    }


    private fun onPlayLastAction() {
        if (mPlayer == null) return

        MediaControllerCompat.getMediaController(this_activity).transportControls.skipToPrevious()
    }

    private fun onPlayNextAction() {
        if (mPlayer == null) return

        MediaControllerCompat.getMediaController(this_activity).transportControls.skipToNext()
    }


    private fun onFavoriteToggleAction() {
        if (mPlayer == null) return

        buttonFavoriteToggle!!.isEnabled = true
        if (mPlayer!!.playingTrack != null) {
            val currentSong = mPlayer!!.playingTrack
            if (currentSong != null) {
                AppExecutors.instance?.diskIO()?.execute {
                    if(URLUtil.isHttpUrl(currentSong.path) || URLUtil.isHttpsUrl(currentSong.path)){
                        val mSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(currentSong.path!!)
                        mSong?.observe(this, Observer<Track> { t ->
                            mSong.removeObservers(this)
                            var fav = true
                            fav = t != null
                            this_activity.runOnUiThread {
                                mPresenter!!.setSongAsFavorite(currentSong, fav)
                            }
                        })
                    } else {
                        val resultPlaylist = playlistDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
                        if (resultPlaylist != null) {
                            if (resultPlaylist.hasThisTrack(currentSong.path)) {
                                this_activity.runOnUiThread {
                                    mPresenter!!.setSongAsFavorite(currentSong, false)
                                }
                            } else {
                                this_activity.runOnUiThread {
                                    mPresenter!!.setSongAsFavorite(currentSong, true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun updateProgressTextWithProgress(progress: Int) {
        textViewProgress!!.text = Player.instance?.stringForTime(progress)
    }

    private fun updateProgressTextWithDuration(duration: Int) {
        textViewProgress!!.text = Player.instance?.stringForTime(duration)
    }

    private fun updateDuration(){
        textViewDuration!!.text = Player.instance?.stringForTime(Player.instance?.duration()?.toInt() ?: 0)
    }

    private fun seekTo(duration: Int) {
        mPlayer!!.seekTo(duration)
    }

    private fun getDuration(progress: Int): Int {
        return (currentSongDuration * (progress.toFloat() / seekBarProgress!!.max)).toInt()
    }

    private fun optionMenu(context: Context, model:Track){
            val ctx = context
            val mBottomSheetDialog = BottomSheetDialog(ctx)
            val sheetView =  LayoutInflater.from(ctx)
                    .inflate(R.layout.bottom_sheet_modal_dialog_playback, mview, false)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.show()
            mBottomSheetDialog.setOnDismissListener {
                //perform action on close
            }

            val nowPlaying = sheetView!!.find<LinearLayout>(R.id.menu_item_now_playing)
            val addToQueue = sheetView.find<LinearLayout>(R.id.menu_item_add_to_queue)
            val album = sheetView.find<LinearLayout>(R.id.menu_item_go_to_album)
            val artist = sheetView.find<LinearLayout>(R.id.menu_item_go_to_artist)
            val playlist = sheetView.find<LinearLayout>(id = R.id.menu_item_add_to_play_list)
            nowPlaying.setOnClickListener {
                val intent = Intent(it.context, NowPlayingActivity::class.java)
                startActivity(intent)
            }
            //add to now playing
            addToQueue.setOnClickListener {
                Player.instance!!.insertnext(
                        Player.instance!!.mPlayList!!.numOfSongs,
                        model)
                mBottomSheetDialog.dismiss()
            }
            album.setOnClickListener {
                val sAlbum = SoftCodeAdapter().getAlbum(context, model.albumArt!!)
                SoftCodeAdapter().openAlbumActivity(context, sAlbum)
                mBottomSheetDialog.dismiss()
            }
            artist.setOnClickListener {
                val intent = Intent(ctx, ArtistSongs::class.java)
                intent.putExtra("com.musicplayer.aow.artist.name",
                        model.artist)
                ContextCompat.startActivity(ctx, intent, null)
                mBottomSheetDialog.dismiss()
            }
            //Add to Playlist Operation
            playlist.setOnClickListener {
                mBottomSheetDialog.dismiss()
                //Dialog with ListView
                val mSelectPlaylistDialog = BottomSheetDialog(ctx)
                val sheetView =  LayoutInflater.from(ctx).inflate(R.layout.custom_dialog_select_playlist, null)
                val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                SoftCodeAdapter().addSongToPlaylist(context, mylist, mSelectPlaylistDialog, model)

                mSelectPlaylistDialog.setContentView(sheetView)
                mSelectPlaylistDialog.show()
                mSelectPlaylistDialog.setOnDismissListener {}
            }
    }

    // Player Callbacks
    override fun onSwitchLast(last: Track?) {
        onSongUpdated(last)
    }

    override fun onSwitchNext(next: Track?) {
        onSongUpdated(next)
    }

    override fun onComplete(next: Track?) {
        onSongUpdated(next)
    }

    override fun onPlayStatusChanged(isPlaying: Boolean) {
        updatePlayToggle(isPlaying)
        if (isPlaying) {
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
        } else {
            mHandler.removeCallbacks(mProgressCallback)
        }
    }

    // MVP View
    override fun handleError(error: Throwable) {
        //Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
    }

    override fun onPlaybackServiceBound(service: PlaybackService) {
        mPlayer = service
        mPlayer!!.registerCallback(this)
    }

    override fun onPlaybackServiceUnbound() {
        mPlayer!!.unregisterCallback(this)
        mPlayer = null
    }

    override fun onSongSetAsFavorite(track: Track) {
        AppExecutors.instance?.diskIO()?.execute {
            if(URLUtil.isHttpUrl(track.path) || URLUtil.isHttpsUrl(track.path)){
                val mSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(track.path!!)
                view.context.runOnUiThread {
                    updateFavoriteToggle(mSong != null)
                }
            } else {
                val favoriteSongs = playlistDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
                view.context.runOnUiThread {
                   updateFavoriteToggle(favoriteSongs?.hasThisTrack(track.path)!!)
                }
            }
        }
    }

    override fun onSongUpdated(track: Track?) {
        if (track == null) {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
            seekBarProgress!!.progress = 0
            updateProgressTextWithProgress(0)
            seekTo(0)
            mHandler.removeCallbacks(mProgressCallback)
            return
        }

        // Step 1: Track name and artist
        textViewName!!.text = track.displayName
        textViewArtist!!.text = track.artist
        // Step 2: favorite
        AppExecutors.instance?.diskIO()?.execute {
            if(URLUtil.isHttpUrl(track.path) || URLUtil.isHttpsUrl(track.path)){
                val mSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(track.path!!)
                mSong?.observe(this, Observer<Track> { t ->
                    view.context.runOnUiThread {
                        buttonFavoriteToggle!!.setImageResource(if (t != null) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
                    }
                })
            } else {
                val favoriteSongs = playlistDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
                view.context.runOnUiThread {
                    buttonFavoriteToggle!!.setImageResource(
                            if (favoriteSongs?.hasThisTrack(track.path)!!)
                                R.drawable.ic_favorite_yes
                            else
                                R.drawable.ic_favorite_no)
                }
            }
        }
        // Step 3: Keep these things updated
        // - Album rotation
        // - Progress(textViewProgress & seekBarProgress)
        if (track.albumArt != null && track.albumArt != "null") {
            if(URLUtil.isHttpUrl(track.albumArt) || URLUtil.isHttpsUrl(track.albumArt)){
                doAsync {
                    val img = Glide.with(this@MusicPlayerActivity).asBitmap()
                            .load(track.albumArt)
                            .submit(250, 250).get()
                    onComplete {
                        imageViewAlbum!!.setImageBitmap(img)
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
                            arrayOf(track.albumArt!!),
                            null)
                    onComplete {
                        if (alb.moveToFirst()) {
                            val data = alb.getString(alb.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                            val albumArt = BitmapDraws.createFromPath(data)
                            if (albumArt != null) {
                                imageViewAlbum!!.setImageDrawable(albumArt)
                            }else{
                                imageViewAlbum!!.setImageResource(R.drawable.gradient_danger)
                            }
                        }
                        alb.close()
                    }

                }
            }
        }else{
                //Drawable Text
                val generator = ColorGenerator.MATERIAL // or use DEFAULT
                // generate random color
                val color1 = generator.randomColor
                val icon = TextDrawable.builder().buildRect(track.displayName!!.substring(0,1), color1)
                imageViewAlbum!!.setImageDrawable(icon)
        }

        imageViewAlbum!!.clearAnimation()
        mHandler.removeCallbacks(mProgressCallback)
        if (mPlayer!!.isPlaying) {
            mHandler.post(mProgressCallback)
            buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
        }
        //Step 8: set duration
        textViewDuration!!.text = Player.instance?.stringForTime(Player.instance?.duration()?.toInt() ?: 0)
    }

    override fun updatePlayMode(playMode: PlayMode) {
        when (playMode) {
            PlayMode.LIST -> {
                buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_list)
                mPlayer!!.mPlayer?.shuffleModeEnabled = false
                mPlayer!!.mPlayer?.repeatMode = ExoPlayer.REPEAT_MODE_OFF
            }
            PlayMode.LOOP -> {
                buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_loop)
                mPlayer!!.mPlayer?.shuffleModeEnabled = false
                mPlayer!!.mPlayer?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            }
            PlayMode.SHUFFLE -> {
                buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_shuffle)
                mPlayer!!.mPlayer?.shuffleModeEnabled = true
                mPlayer!!.mPlayer?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            }
            PlayMode.SINGLE -> {
                buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_single)
                mPlayer!!.mPlayer?.shuffleModeEnabled = false
                mPlayer!!.mPlayer?.repeatMode = ExoPlayer.REPEAT_MODE_ONE
            }
            PlayMode.default -> {
                buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_loop)
                mPlayer!!.mPlayer?.shuffleModeEnabled = false
                mPlayer!!.mPlayer?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            }
        }
    }

    override fun onTriggerLoading(isLoading: Boolean) {
        buttonPlayToggle!!.setImageResource(R.drawable.loading)
    }

    override fun onPrepared(isPrepared: Boolean) {
        
    }

    override fun updatePlayToggle(play: Boolean) {
        buttonPlayToggle!!.setImageResource(if (play) R.drawable.ic_pause else R.drawable.ic_play)
    }

    fun isFavorite(track: Track){
        AppExecutors.instance?.diskIO()?.execute {
            if(URLUtil.isHttpUrl(track.path) || URLUtil.isHttpsUrl(track.path)){
                val mSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(track.path!!)
                mSong?.observe(this, Observer<Track> { t ->
                    this_activity.runOnUiThread {
                        buttonFavoriteToggle!!.setImageResource(if (t != null) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
                    }
                })
            } else {
                val favoriteSongs = playlistDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
                this_activity.runOnUiThread {
                    buttonFavoriteToggle!!.setImageResource(
                            if (favoriteSongs?.hasThisTrack(track.path)!!)
                                R.drawable.ic_favorite_yes
                            else
                                R.drawable.ic_favorite_no)
                }
            }
        }
    }

    override fun updateFavoriteToggle(favorite: Boolean) {
        buttonFavoriteToggle!!.setImageResource(if (favorite) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
    }

    override fun setPresenter(presenter: MusicPlayerContract.Presenter) {
        mPresenter = presenter
    }

    companion object {

        private const val TAG = "MusicPlayerActivity"

        // Update seek bar every second
        private const val UPDATE_PROGRESS_INTERVAL: Long = 1000

    }
}
