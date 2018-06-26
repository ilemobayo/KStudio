package com.musicplayer.aow.ui.main

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.nisrulz.sensey.Sensey
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.event.PlayAlbumNowEvent
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.event.PlaySongEvent
import com.musicplayer.aow.delegates.firebase.ForceUpdateChecker
import com.musicplayer.aow.delegates.player.*
import com.musicplayer.aow.delegates.player.mediasession.BackgroundAudioService
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist.PlayListFavDatabase
import com.musicplayer.aow.ui.auth.AuthActivity
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.eq.EqActivity
import com.musicplayer.aow.ui.main.library.LibraryAdapter
import com.musicplayer.aow.ui.main.search.SearchActivity
import com.musicplayer.aow.ui.music.MusicPlayerActivity
import com.musicplayer.aow.ui.nowplaying.NowPlaying
import com.musicplayer.aow.ui.nowplaying.NowPlayingActivity
import com.musicplayer.aow.ui.settings.SettingsActivity
import com.musicplayer.aow.utils.ApplicationSettings
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottomsheet_layout.*
import kotlinx.android.synthetic.main.current_playing_panel.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers


class MainActivity : BaseActivity(),
        MusicPlayerContract.View,
        IPlayback.Callback,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        ForceUpdateChecker.OnUpdateNeededListener{

    private var auth: FirebaseAuth? = null
    //init objects
    private var audioFocus = AudioFocus.instance

    //Music BottomSheet Init Componet
    private var mPlayer: IPlayback? = null
    private val mHandler = Handler()
    private var mPresenter: MusicPlayerContract.Presenter? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    //ui element
    private var namePlayback: TextView? = null
    private var artistPlayback: TextView? = null

    //room for resntly played
    private var playListFavDatabase = PlayListFavDatabase.getsInstance(Injection.provideContext()!!)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        //GPU overdraw optimaization
        //window.setBackgroundDrawable(null)
        val tintManager = SystemBarTintManager(this)
        // enable status bar tint
        tintManager.isStatusBarTintEnabled = true
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true)

        // set a custom tint color for all system bars
        tintManager.setTintColor(R.color.translusent)
        // set a custom navigation bar resource
        tintManager.setNavigationBarTintResource(R.drawable.gradient_warning)
        // set a custom status bar drawable
        tintManager.setStatusBarTintResource(R.color.black)

        //mediasession
        mMediaBrowserCompat = MediaBrowserCompat(this, ComponentName(this, BackgroundAudioService::class.java),
                mMediaBrowserCompatConnectionCallback, null)

        namePlayback = findViewById(R.id.text_view_name)
        artistPlayback = findViewById(R.id.text_view_artist)

        nav_view.itemIconTintList = null
        nav_view.setNavigationItemSelectedListener(this)

        //Toolbar
        setupToolbar()
        setupDrawerToggle()
        MusicPlayerPresenter(applicationContext, this).subscribe()

        volumeControlStream = AudioManager.STREAM_MUSIC
        //END OF BOTTOMSHEET

        tablayout()
        //check for permission
        requestReadStoragePermission(this)
        
        //Firebase update with remote config
        ForceUpdateChecker.with(applicationContext).onUpdateNeeded(this).check()

    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle!!.syncState()

        //load resently played
        doAsync {
            onComplete {
                loadResent()
            }
        }

        //on clicking the speaker load the music activity
        mini_control_player.setOnClickListener {
            openPlayBack()
        }

        //Play/Pause button
        button_play_toggle.setOnClickListener{
            onPlayToggleAction()
        }
        //play Next
        button_play_next.setOnClickListener{
            onPlayNextAction()
        }
    }

    private fun setupToolbar() {
        toolbar.title = resources.getString(R.string.application_name)
        auth = FirebaseAuth.getInstance()
        val userdata = auth!!.currentUser
        if(userdata != null) {
            toolbar.subtitle = userdata.email
        }
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
    }

    private fun setupDrawerToggle() {
        mDrawerToggle  = android.support.v7.app.ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(mDrawerToggle!!)
        mDrawerToggle!!.syncState()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            //super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main2, menu)

        val searchItem = menu.findItem(R.id.nav_search)

        val searchManager: SearchManager = this.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        var searchView: SearchView? = null
        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView?
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.componentName))
        }
        return true
        //return super.onCreateOptionsMenu(menu)
        //return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //return super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.nav_search -> {
                val intent = Intent(applicationContext, SearchActivity::class.java)
                startActivity(intent)
                true
            }
//            R.id.nav_browse -> {
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_account -> {
                val intent = Intent(applicationContext, AuthActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_now_playing -> {
                val intent = Intent(applicationContext, NowPlayingActivity::class.java)
                startActivity(intent)
            }
//            R.id.nav_identify -> {
//                val intent = Intent(applicationContext, IdentifySoundActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.nav_event -> {
//                val intent = Intent(applicationContext, EventsListActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.nav_record -> {
//                val intent = Intent(applicationContext, VoiceRecorderActivity::class.java)
//                startActivity(intent)
//            }
            R.id.nav_eq -> {
                val intent = Intent(applicationContext, EqActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                val i = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(i)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    private val mProgressCallback = Runnable {
        //
    }


    var fragment: Fragment? = null

    private fun tablayout(){
        library_tablayout.addTab(library_tablayout.newTab().setIcon(R.drawable.ic_home).setText("Browse"))
        library_tablayout.addTab(library_tablayout.newTab().setIcon(R.drawable.library_songs).setText("Songs"))
        library_tablayout.addTab(library_tablayout.newTab().setIcon(R.drawable.library_album).setText("Albums"))
        library_tablayout.addTab(library_tablayout.newTab().setIcon(R.drawable.library_artist).setText("Artists"))
        library_tablayout.addTab(library_tablayout.newTab().setIcon(R.drawable.library_playlist).setText("Playlists"))
        library_tablayout.tabGravity = TabLayout.GRAVITY_FILL
        library_tablayout.getTabAt(0)!!.icon!!.setColorFilter(resources.getColor(R.color.red_dim), PorterDuff.Mode.SRC_IN)

        library_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //for removing the color of first icon when switched to next tab
                library_tablayout.getTabAt(0)!!.icon!!.clearColorFilter()
                //for other tabs
                tab!!.icon!!.clearColorFilter()
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab!!.icon!!.setColorFilter(resources.getColor(R.color.red_dim), PorterDuff.Mode.SRC_IN)
                library_view_pager.setCurrentItem(tab.position , false)
            }

        })
    }

    private fun viewPager(){
        val adapter = LibraryAdapter(supportFragmentManager,library_tablayout.tabCount)
        library_view_pager.disableScroll(true)
        library_view_pager.offscreenPageLimit = 5
        library_view_pager.adapter = adapter
        library_view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(library_tablayout))
    }

    override fun onClick(v: View) {
        //nothing
    }

    fun indicator(){
        val player = Player.instance!!.mPlayer
        //MediaSession
        player?.setOnBufferingUpdateListener { mp, percent ->
            if (percent == 100){
                //Log.e(this.javaClass.name, "$percent")
            }
            val ratio: Double = percent / 100.0
            val bufferedLevel  = mp?.duration?.times(ratio)?.toInt()
            //Log.e(this.javaClass.name, "$bufferedLevel")
        }

        player?.setOnPreparedListener {
            it.start()
            updatePlayToggle(it.isPlaying)
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
        val playList = event.song
        playSong(playList)
    }

    // Music Controls
    private fun playSong(song: Song) {
        val playList = PlayList(song)
        playSong(playList, 0)
    }

    override fun updateFavoriteToggle(favorite: Boolean) {
        //
    }

    private fun playSong(songs: List<Song>) {
        val playList = PlayList(songs as java.util.ArrayList<Song>)
        playSong(playList, 0)
    }

    private fun playSong(playList: PlayList?, playIndex: Int) {
        if (playList == null) return

        playList.playMode = PreferenceManager.lastPlayMode(applicationContext)
        val result = Player.instance!!.play(playList, playIndex)
        val song = playList.currentSong
        // Step 1: Song name and artist
        if (song != null) {
            namePlayback!!.text = song.displayName
            artistPlayback!!.text = song.artist
        }
        //update playback button
        if (result) {
            indicator()
            //gain focus
            audioFocus!!.play()
            button_play_toggle.setImageResource(R.drawable.ic_pause)
        } else {
            button_play_toggle.setImageResource(R.drawable.ic_play)
        }
        //finished updating
        onSongUpdated(song)
        mHandler.removeCallbacks(mProgressCallback)
        mHandler.post(mProgressCallback)
    }

    private fun onPlayNextAction() {
        if (mPlayer == null) return

        mPlayer!!.playNext()
    }

    // Click Events
    private fun onPlayToggleAction() {
        //if (mediaPlayer == null) return
        if (mPlayer!!.playingList == null || mPlayer!!.playingList!!.numOfSongs == 0){
            val recentPlayList = NowPlaying.instance!!.playlist
            if (recentPlayList.numOfSongs != 0) {
                playSong(recentPlayList, recentPlayList.playingIndex)
                return
            }
        }

        if (mPlayer!!.isPlaying) {
            audioFocus!!.pause()
            mPlayer!!.pause()
        } else {
            audioFocus!!.play()
            mPlayer!!.play()
            //test MediaSession
            testMediaSession()
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
        if (Player.instance!!.mPlayList != null) {
            if (Player.instance!!.mPlayList!!.currentSong != null) {
                namePlayback!!.text = Player.instance!!.mPlayList!!.currentSong?.displayName
                artistPlayback!!.text = Player.instance!!.mPlayList!!.currentSong?.artist
            }
        }
        updatePlayToggle(isPlaying)
        indicator()
        if (isPlaying) {
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
        } else {
            mHandler.removeCallbacks(mProgressCallback)
        }
    }

    // MVP View
    override fun handleError(error: Throwable) {
        //
    }

    override fun onPlaybackServiceBound(service: PlaybackService) {
        mPlayer = service
        mPlayer!!.registerCallback(this)
    }

    override fun onPlaybackServiceUnbound() {
        mPlayer!!.unregisterCallback(this)
        mPlayer = null
    }

    override fun onSongSetAsFavorite(song: Song) {
        //
    }

    override fun onSongUpdated(song: Song?) {
        if (song == null) {
            button_play_toggle.setImageResource(R.drawable.ic_play)
            mHandler.removeCallbacks(mProgressCallback)
            return
        }
        mHandler.removeCallbacks(mProgressCallback)

        namePlayback!!.text = song.displayName
        artistPlayback!!.text = song.artist

        if (mPlayer!!.isPlaying) {
            mHandler.post(mProgressCallback)
            button_play_toggle.setImageResource(R.drawable.ic_pause)
        }
        doAsync {
            //update
            loadNowPlayingTable(mPlayer!!.playingList!!)
        }
    }

    override fun updatePlayMode(playMode: PlayMode) {
        //
    }

    override fun onTriggerLoading(isLoading: Boolean) {
        button_play_toggle!!.visibility = View.INVISIBLE
        loading!!.visibility = View.VISIBLE
    }

    override fun updatePlayToggle(play: Boolean) {
        button_play_toggle!!.visibility = View.VISIBLE
        loading!!.visibility = View.INVISIBLE
        button_play_toggle.setImageResource(
                if (play){
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
        )
    }

    override fun setPresenter(presenter: MusicPlayerContract.Presenter) {
        mPresenter = presenter
    }

    private fun loadResent(){
        val result = playListFavDatabase?.playlistFavDAO()?.fetchOnePlayListMxpId("nowplaying")
        if(result != null ) {
            if (result.songs?.size!! > 0) {
                lastPlayed(result)
            } else {
                //loadAllSongs()
            }
        }else{
            val playList = PlayList()
            playList.name = "Resently Played"
            playList.mxp_id = "nowplaying"
            playListFavDatabase?.playlistFavDAO()?.insertOnePlayList(playList)
        }
    }

    private fun lastPlayed(playlist: PlayList){
        playlist.playMode = PreferenceManager.lastPlayMode(applicationContext)
        //update player playing list
        NowPlaying.instance!!.setPlayList(playlist)
        val song = playlist.currentSong
        if (song != null) {
            // Step 1: Song name and artist
            namePlayback!!.text = song.displayName
            artistPlayback!!.text = song.artist
        }else{
            return
        }
        //display the palyback control panel
        mini_control_player.visibility = View.VISIBLE
    }

    private fun loadNowPlayingTable(playList: PlayList) {
        playList.name = "Resently Played"
        playList.mxp_id = "nowplaying"
        playListFavDatabase?.playlistFavDAO()?.updatePlayListFav(playList)
    }


    private fun openPlayBack(){
        if(mPlayer!!.playingSong != null){
            val intent = Intent(applicationContext, MusicPlayerActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Requesting camera permission
     * This uses single permission model from dexter
     * Once the permission granted, opens the camera
     * On permanent denial opens settings dialog
     */
    private fun requestReadStoragePermission(activity: Activity) {
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        // permission is granted
                        // Inflate ViewPager
                        viewPager()

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied) {
                            showSettingsDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS", { dialog, which ->
            dialog.cancel()
            openSettings()
        })
        builder.setNegativeButton("Cancel", { dialog, which ->
            dialog.cancel()
            finish()
        })
        builder.show()

    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = uri
        startActivityForResult(intent, 101)
    }



    //Force Update Firebase
    override fun onUpdateNeeded(updateUrl:String) {
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
        dialog.setTitle("New version available")
        dialog.setMessage("Please, update app to new version to continue.")
        dialog.setPositiveButton("Update",
                        { dialog, which ->
                            redirectStore(updateUrl)
                        })

        dialog.setNegativeButton("Cancel",
                        {dialog, which ->

                        }).create()
        dialog.show()
    }

    //Force Update Firebase
    private fun redirectStore(updateUrl:String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    /**
     * MEDIASESSION INTEGRATION
     */
    private val STATE_PAUSED = 0
    private val STATE_PLAYING = 1

    private var mCurrentState: Int = 0

    private var mMediaBrowserCompat: MediaBrowserCompat? = null
    private var mMediaControllerCompat: MediaControllerCompat? = null

    private val mMediaBrowserCompatConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            try {
                // Get the token for the MediaSession
                val token: MediaSessionCompat.Token = mMediaBrowserCompat!!.sessionToken
                // Create a MediaControllerCompat
                mMediaControllerCompat = MediaControllerCompat(this@MainActivity, // Context
                        token)
                // Save the controller
                MediaControllerCompat.setMediaController(this@MainActivity, mMediaControllerCompat);
                MediaControllerCompat.getMediaController(this@MainActivity).transportControls.playFromMediaId(1.toString(), null)
                MediaControllerCompat.getMediaController(this@MainActivity).transportControls.play()

                mMediaControllerCompat!!.registerCallback(mMediaControllerCompatCallback)
                
            } catch (e: RemoteException) {
                Toast.makeText(applicationContext, "connection error to ms", Toast.LENGTH_SHORT).show()
                Log.e(this.javaClass.name, e.localizedMessage)
            }

        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.e(this.javaClass.name, "connection error")
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            Log.e(this.javaClass.name, "connection suspended")
        }
    }

    private val mMediaControllerCompatCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state == null) {
                return
            }

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    mCurrentState = STATE_PLAYING
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    mCurrentState = STATE_PAUSED
                }
            }
        }
    }

    fun testMediaSession(){

            MediaControllerCompat.getMediaController(this@MainActivity).transportControls.playFromMediaId(1.toString(), null)
            if (mCurrentState == STATE_PAUSED) {
                MediaControllerCompat(this, mMediaBrowserCompat!!.sessionToken).transportControls.play()
                mCurrentState = STATE_PLAYING
            } else {
                if (MediaControllerCompat(this, mMediaBrowserCompat!!.sessionToken).playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    MediaControllerCompat(this, mMediaBrowserCompat!!.sessionToken).transportControls.pause()
                }

                mCurrentState = STATE_PAUSED
            }

    }

    override fun onStart() {
        super.onStart()
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
        }
        
        //mediasession connect
        if(!mMediaBrowserCompat!!.isConnected) {
            mMediaBrowserCompat!!.connect()
        }
    }

    override fun onResume() {
        ApplicationSettings().ShakeWithSensorDetectorResume()
        super.onResume()
    }

    override fun onStop() {
        mHandler.removeCallbacks(mProgressCallback)
        ApplicationSettings().ShakeWithSensorDetectorStop()
        super.onStop()
    }

    override fun onDestroy() {
        mPresenter!!.unsubscribe()
        //sensey gesture
        Sensey.getInstance().stop()
        ApplicationSettings.instance?.ShakeWithSensorDetectorDestroy()
        super.onDestroy()
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(mMediaControllerCompatCallback)
        }
//        if (MediaControllerCompat.getMediaController(this).playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
//            MediaControllerCompat.getMediaController(this).transportControls.pause()
//        }

        mMediaBrowserCompat?.disconnect()
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
        fun newIntent(context: Context): Intent {
            return Intent(context.applicationContext, MainActivity::class.java)
        }
    }
}
