package com.musicplayer.aow.ui.music

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.URLUtil
import android.widget.*
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.event.PlayAlbumNowEvent
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.event.PlaySongEvent
import com.musicplayer.aow.delegates.player.*
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.main.MusicPlayerContract
import com.musicplayer.aow.ui.main.MusicPlayerPresenter
import com.musicplayer.aow.ui.main.library.activities.ArtistSongs
import com.musicplayer.aow.ui.nowplaying.NowPlayingActivity
import com.musicplayer.aow.utils.TimeUtils
import com.musicplayer.aow.utils.images.BitmapDraws
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.fragment_music.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.io.File


class MusicPlayerActivity : BaseActivity(), MusicPlayerContract.View, IPlayback.Callback, View.OnClickListener{

    //init objects
    private var audioFocus = AudioFocus.instance
    private var songFavDatabase: SongFavDatabase? = SongFavDatabase.getsInstance(Injection.provideContext()!!)

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

    private var mPlayer: IPlayback? = null

    private val mHandler = Handler()

    private var mPresenter: MusicPlayerContract.Presenter? = null

    private  var mview: ViewGroup? = null

    private var lyric: File? = null

    private val mProgressCallback = object : Runnable {
        override fun run() {
            if (mPlayer!!.isPlaying) {
                val progress = (seekBarProgress!!.max * (mPlayer!!.progress.toFloat() / currentSongDuration.toFloat())).toInt()
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

    private val currentSongDuration: Int
        get() {
            return if (mPlayer!!.playingSong != null) {
                val currentSong = mPlayer!!.playingSong
                var duration = 0
                if (currentSong != null) {
                    //duration = if(mPlayer!!.mPlayer != null) mPlayer!!.mPlayer!!.duration else 0
                    duration = currentSong.duration
                }
                duration
            }else{
                0
            }
        }


    var anim = RotateAnimation(0.0f, 360.0f , Animation.RELATIVE_TO_SELF, .5f,
            Animation.RELATIVE_TO_SELF, .5f)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_music)

        val tintManager = SystemBarTintManager(this)
        // enable status bar tint
        tintManager.isStatusBarTintEnabled = true
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true)

        // set a custom tint color for all system bars
        tintManager.setTintColor(R.color.translusent);
        // set a custom navigation bar resource
        tintManager.setNavigationBarTintResource(R.drawable.gradient_warning);
        // set a custom status bar drawable
        tintManager.setStatusBarTintResource(R.color.translusent);

        setupToolbar()

        volumeControlStream = AudioManager.STREAM_MUSIC

        textViewProgress = findViewById(R.id.text_view_progress)
        textViewName = findViewById(R.id.text_view_name)
        textViewArtist = findViewById(R.id.text_view_artist)
        textViewDuration = findViewById(R.id.text_view_duration)
        imageViewAlbum = findViewById(R.id.image_view_album)
        buttonPlayToggle = findViewById(R.id.button_play_toggle)
        buttonFavoriteToggle = findViewById(R.id.button_favorite_toggle)
        buttonPlayModeToggle = findViewById(R.id.button_play_mode_toggle)
        buttonPlayNext = findViewById(R.id.button_play_next)
        buttonPlayPrev = findViewById(R.id.button_play_last)
        seekBarProgress = findViewById(R.id.seek_bar)
        playBackVolume = findViewById(R.id.playback_SeekBar)

        textViewName!!.isSelected = true

//        try {
//            val input = assets.open("file/test.lrc")
//            // myData.txt can't be more than 2 gigs.
//            val size = input.available()
//            val buffer = BufferedReader(InputStreamReader(input))
//            var line: String?
//            do {
//                line = buffer.readLine();
//                if (line != null) {
//                    val regex = """(])""".toRegex()
//                    val x = regex.split(line)
//                    val first = x.first().split("""(\[)""".toRegex()).last()
//                    val last = x.last()
//                    try {
//                        Log.e("Lyrics", (first).toString())
//                    }catch (e: NumberFormatException){}
//                    lrc_text.text = last;
//                } else {
//                    //you may want to close the file now since there's nothing more to be done here.
//                }
//            }while (line != null)
//            input.close()
//
//        }catch (e: IOException){
//        }

        textViewArtist!!.setOnClickListener {
            if(mPlayer != null) {
                if (mPlayer!!.isPlaying) {
                    val intent = Intent(applicationContext, ArtistSongs::class.java)
                    intent.putExtra("com.musicplayer.aow.artist.name", mPlayer!!.playingSong!!.artist)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ContextCompat.startActivity(applicationContext, intent, null)
                }
            }
        }

        //Display now playing list
        android_now_playing!!.setOnClickListener {
            optionMenu(this, Player.instance!!.playingSong!!)
        }

        //Play/Pause button
        buttonPlayToggle!!.setOnClickListener{
            onPlayToggleAction()
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
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
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

        MusicPlayerPresenter(applicationContext, this).subscribe()

    }


    override fun onClick(v: View) {
        //nothing
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
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
                finish()
                return true
            }
            else -> return false
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
        val toolbar = findViewById<RelativeLayout>(R.id.toolbar_layer)
        val toolbarNavigation = findViewById<AppCompatImageView>(R.id.toolbar_album_song_list)
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
        toolbarNavigation.setOnClickListener {
            finish()
        }
    }

    // Click Events
    private fun onPlayToggleAction() {
        if (mPlayer == null) return

        if (mPlayer!!.isPlaying) {
            audioFocus!!.pause()
            mPlayer!!.pause()
        } else {
            audioFocus!!.play()
            mPlayer!!.play()
        }
    }

    private fun onPlayModeToggleAction() {
        if (mPlayer == null) return

        val current = PreferenceManager.lastPlayMode(applicationContext)
        val newMode = PlayMode.switchNextMode(current)
        PreferenceManager.setPlayMode(applicationContext, newMode)
        mPlayer?.setPlayMode(newMode)
        updatePlayMode(newMode)
    }


    private fun onPlayLastAction() {
        if (mPlayer == null) return

        mPlayer!!.playLast()
    }

    private fun onPlayNextAction() {
        if (mPlayer == null) return

        mPlayer!!.playNext()
    }


    private fun onFavoriteToggleAction() {
        if (mPlayer == null) return

        buttonFavoriteToggle!!.isEnabled = true
        if (mPlayer!!.playingSong != null) {
            val currentSong = mPlayer!!.playingSong
            if (currentSong != null) {
                buttonFavoriteToggle!!.isEnabled = false
                mPresenter!!.setSongAsFavorite(currentSong, isSongFavorite(currentSong))
            }
        }
    }

    // RXBus Events
    override fun subscribeEvents(): Subscription {
        return RxBus.instance?.toObservable()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnNext({ o ->
                    when (o) {
                        is PlaySongEvent -> onPlaySongEvent(o)
                        is PlayListNowEvent -> onPlayListNowEvent(o)
                        is PlayAlbumNowEvent -> onPlayAlbumNowEvent(o)
                    }
                })?.subscribe(RxBus.defaultSubscriber())!!
    }

    private fun onPlaySongEvent(event: PlaySongEvent) {
        val song = event.song
        playSong(song)
    }

    private fun onPlayListNowEvent(event: PlayListNowEvent) {
        val playList = event.playList
        val playIndex = event.playIndex
        playSong(playList, playIndex)
    }

    private fun onPlayAlbumNowEvent(event: PlayAlbumNowEvent) {
        val playList = event.song as ArrayList
        playSong(playList)
    }

    // Music Controls
    private fun playSong(song: Song) {
        val playList = PlayList(song)
        playSong(playList, 0)
    }

    private fun playSong(songs: ArrayList<Song>) {
        val playList = PlayList(songs)
        playSong(playList, 0)
    }

    private fun playSong(playList: PlayList?, playIndex: Int) {
        if (playList == null) return

        playList.playMode = PreferenceManager.lastPlayMode(applicationContext)
        val result = mPlayer!!.play(playList, playIndex)
        val song = playList.currentSong
        //finished updating
        onSongUpdated(song)
        seekBarProgress!!.progress = 0
        seekBarProgress!!.isEnabled = result
        textViewProgress!!.setText(R.string.mp_music_default_duration)
        if (result) {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
            textViewDuration!!.text = TimeUtils.formatDuration(song!!.duration)
        } else {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
            textViewDuration!!.setText(R.string.mp_music_default_duration)
        }

        mHandler.removeCallbacks(mProgressCallback)
        mHandler.post(mProgressCallback)

        this.startService(Intent(applicationContext, PlaybackService::class.java))
    }

    private fun updateProgressTextWithProgress(progress: Int) {
        val targetDuration = getDuration(progress)
        textViewProgress!!.text = TimeUtils.formatDuration(targetDuration)
    }

    private fun updateProgressTextWithDuration(duration: Int) {
        textViewProgress!!.text = TimeUtils.formatDuration(duration)
    }

    private fun seekTo(duration: Int) {
        mPlayer!!.seekTo(duration)
    }

    private fun getDuration(progress: Int): Int {
        return (currentSongDuration * (progress.toFloat() / seekBarProgress!!.max)).toInt()
    }

    private fun optionMenu(context: Context, model:Song){
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
                val intent = Intent(applicationContext, NowPlayingActivity::class.java)
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

                SoftCodeAdapter().addSongToPlaylist(this,context, mylist, mSelectPlaylistDialog, model)

                mSelectPlaylistDialog.setContentView(sheetView)
                mSelectPlaylistDialog.show()
                mSelectPlaylistDialog.setOnDismissListener {}
            }
    }

    // Player Callbacks
    override fun onSwitchLast(last: Song?) {
        onSongUpdated(last)
    }

    override fun onSwitchNext(next: Song?) {
        onSongUpdated(next)
    }

    override fun onComplete(next: Song?) {
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

    private fun isSongFavorite(song: Song): Boolean {
        if(URLUtil.isHttpUrl(song.path) || URLUtil.isHttpsUrl(song.path)){
            val fSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(song.path!!)
            return fSong != null
        } else {
            val favoriteId = SoftCodeAdapter().getFavoritesId(applicationContext)
            val favoriteSongsId = SoftCodeAdapter().getPlaylistTracksIds(applicationContext, favoriteId)
            return favoriteSongsId.contains(song.id.toString())
        }
    }

    override fun onSongSetAsFavorite(song: Song) {
        updateFavoriteToggle(isSongFavorite(song))
    }

    override fun onSongUpdated(song: Song?) {
        if (song == null) {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
            seekBarProgress!!.progress = 0
            updateProgressTextWithProgress(0)
            seekTo(0)
            mHandler.removeCallbacks(mProgressCallback)
            return
        }

        // Step 1: Song name and artist
        textViewName!!.text = song.displayName
        textViewArtist!!.text = song.artist
        // Step 2: favorite
        buttonFavoriteToggle!!.setImageResource(if (isSongFavorite(song)) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
        // Step 3: Duration
        textViewDuration!!.text = "-"+TimeUtils.formatDuration(song.duration)
        // Step 4: Keep these things updated
        // - Album rotation
        // - Progress(textViewProgress & seekBarProgress)
        if (song.albumArt != null && song.albumArt != "null") {
            if(URLUtil.isHttpUrl(song.albumArt) || URLUtil.isHttpsUrl(song.albumArt)){
                doAsync {
                    val img = Glide.with(applicationContext)
                            .load(song.albumArt).asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(150, 150)
                            .get()
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
                            arrayOf(song?.albumArt!!),
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
                val icon = TextDrawable.builder().buildRect(song.displayName!!.substring(0,1), color1)
                imageViewAlbum!!.setImageDrawable(icon)
            }

        imageViewAlbum!!.clearAnimation()
        mHandler.removeCallbacks(mProgressCallback)
        if (mPlayer!!.isPlaying) {
            mHandler.post(mProgressCallback)
            buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
        }
    }

    override fun updatePlayMode(playMode: PlayMode) {
        when (playMode) {
            PlayMode.LIST -> buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_list)
            PlayMode.LOOP -> buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_loop)
            PlayMode.SHUFFLE -> buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_shuffle)
            PlayMode.SINGLE -> buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_single)
            PlayMode.default -> buttonPlayModeToggle!!.setImageResource(R.drawable.ic_play_mode_loop)
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
