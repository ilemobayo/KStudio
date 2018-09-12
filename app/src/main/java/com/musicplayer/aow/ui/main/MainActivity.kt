package com.musicplayer.aow.ui.main

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
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
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.firebase.ForceUpdateChecker
import com.musicplayer.aow.delegates.game.RouletteActivity
import com.musicplayer.aow.delegates.player.IPlayback
import com.musicplayer.aow.delegates.player.PlayMode
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.about.AboutUsActivity
import com.musicplayer.aow.ui.auth.AuthActivity
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.eq.EqActivity
import com.musicplayer.aow.ui.main.library.LibraryAdapter
import com.musicplayer.aow.ui.main.library.home.podcast.PodcastActivity
import com.musicplayer.aow.ui.main.search.SearchActivity
import com.musicplayer.aow.ui.music.MusicPlayerActivity
import com.musicplayer.aow.ui.settings.SettingsActivity
import com.musicplayer.aow.utils.StorageUtil
import com.musicplayer.aow.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottomsheet_layout.*
import kotlinx.android.synthetic.main.current_playing_panel.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.doAsync


class MainActivity : BaseActivity(),
        MusicPlayerContract.View,
        IPlayback.Callback,
        NavigationView.OnNavigationItemSelectedListener,
        ForceUpdateChecker.OnUpdateNeededListener{

    //Job queue
    //private var jobManager: JobManager? = null
    /**
     * MEDIASESSION INTEGRATION
     */
    private val STATE_PAUSED = 0
    private val STATE_PLAYING = 1

    private var mCurrentState: Int = 0
    private var mMediaBrowserCompat: MediaBrowserCompat? = null

    private var auth: FirebaseAuth? = null

    //Music BottomSheet Init Componet
    private var mPlayer: IPlayback? = null
    private var mediaPlayer = Player.instance
    private var mPresenter: MusicPlayerContract.Presenter? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    //ui element
    private var namePlayback: TextView? = null
    private var artistPlayback: TextView? = null

    //room for resntly played
    private var playlistDatabase = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mMediaBrowserCompat = MediaBrowserCompat(
                applicationContext, // a Context
                ComponentName(applicationContext, PlaybackService::class.java),
                mConnectionCallbacks,
                null)


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

        //tablayout()
        //check for permission
        requestReadStoragePermission(this)

    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle!!.syncState()
        //appWidget(this.applicationContext)
    }

    private fun setupToolbar() {
        toolbar.title = resources.getString(R.string.application_name)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_home_up)
    }

    private fun setupDrawerToggle() {
        mDrawerToggle  = android.support.v7.app.ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(mDrawerToggle!!)
        mDrawerToggle!!.syncState()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            moveTaskToBack(true)
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val dialogN = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
            dialogN.setTitle("Exit Musixplay")
            dialogN.setMessage("To stop this application from running in the background, press Exit else press No to keep it running in the background.")
            dialogN.setPositiveButton("Exit",
                    { _, _ ->
                        if (mMediaBrowserCompat != null) {
                            if (mMediaBrowserCompat!!.isConnected) {
                                mMediaBrowserCompat!!.disconnect()
                            }
                        }
                        finish()
                    })

            dialogN.setNegativeButton("No",
                    { _, _ ->
                        moveTaskToBack(true)
                    }).create()
            dialogN.show()
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
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //return super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.nav_search -> {
                //val intent = Intent(applicationContext, SearchActivity::class.java)
                //startActivity(intent)
                onSearchRequested()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onSearchRequested(): Boolean {
//        return super.onSearchRequested()
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_account -> {
                startActivity(Intent(applicationContext, AuthActivity::class.java))
            }
            R.id.nav_podcast -> {
                startActivity(Intent(applicationContext, PodcastActivity::class.java))
            }
            R.id.nav_game -> {
                startActivity(Intent(applicationContext, RouletteActivity::class.java))
            }
            R.id.nav_eq -> {
                startActivity(Intent(applicationContext, EqActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
            }
            R.id.nav_about -> {
                startActivity(Intent(applicationContext, AboutUsActivity::class.java))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private val mConnectionCallbacks = object:MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            Log.e(this.javaClass.name, "i am connected")
            try {
                // Get the token for the MediaSession
                val token: MediaSessionCompat.Token = mMediaBrowserCompat!!.sessionToken
                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(applicationContext, // Context
                        token)
                // Save the controller
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
                //MediaControllerCompat.getMediaController(this@MainActivity).transportControls.playFromMediaId(1.toString(), null)
                //MediaControllerCompat.getMediaController(this@MainActivity).transportControls.pause()

                buildTransportControls()

            } catch (e: RemoteException) {
                Log.e(this.javaClass.name, e.localizedMessage)
            }

        }
    }

    fun buildTransportControls() {

        val mediaController = MediaControllerCompat.getMediaController(this)
        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)

        // Display the initial state
        val metadata = mediaController.metadata
        if(metadata != null){
            val name = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            val artistname = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
            if (name != null) {
                namePlayback!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            }
            if (artistname != null){
                artistPlayback!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
            }

        }
        val pbState = mediaController.playbackState
        when (pbState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                mCurrentState = STATE_PLAYING
                button_play_toggle.setImageResource(R.drawable.ic_pause)

            }
            PlaybackStateCompat.STATE_PAUSED -> {
                mCurrentState = STATE_PAUSED
                button_play_toggle.setImageResource(R.drawable.ic_play)
            }
            PlaybackStateCompat.STATE_BUFFERING -> {

            }
            PlaybackStateCompat.STATE_CONNECTING -> {

            }
        }

        //load resently played
        loadResent()

        //on clicking the speaker load the music activity
        mini_control_player.setOnClickListener {
            openPlayBack()
        }

        // Attach a listener to the button
        //Play/Pause button
        button_play_toggle.setOnClickListener{
            val playbackState = MediaControllerCompat.getMediaController(this).playbackState.state
            onPlayToggleAction(playbackState)
        }

        //play Next
        button_play_next.setOnClickListener{
            onPlayNextAction()
        }

    }

    private var controllerCallback:MediaControllerCompat.Callback = object:MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            namePlayback!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            artistPlayback!!.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
        }
        override fun onPlaybackStateChanged(state:PlaybackStateCompat) {
            super.onPlaybackStateChanged(state)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    mCurrentState = STATE_PLAYING
                    button_play_toggle.setImageResource(R.drawable.ic_pause)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    mCurrentState = STATE_PAUSED
                    button_play_toggle.setImageResource(R.drawable.ic_play)
                }
                PlaybackStateCompat.STATE_BUFFERING -> {

                }
                PlaybackStateCompat.STATE_CONNECTING -> {

                }
            }
        }
    }


    var fragment: Fragment? = null

    private fun tablayout(){
        library_tablayout.tabGravity = TabLayout.GRAVITY_FILL
        library_tablayout.getTabAt(0)!!.icon!!.setColorFilter(applicationContext.getColor(R.color.red_dim), PorterDuff.Mode.SRC_IN)

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
                tab!!.icon!!.setColorFilter(applicationContext.getColor(R.color.red_dim), PorterDuff.Mode.SRC_IN)
                library_view_pager.setCurrentItem(tab.position , false)
            }

        })

        val adapter = LibraryAdapter(supportFragmentManager,library_tablayout.tabCount)
        library_view_pager.disableScroll(true)
        library_view_pager.offscreenPageLimit = 5
        library_view_pager.adapter = adapter
        library_view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(library_tablayout))

    }

    // Music Controls
    override fun updateFavoriteToggle(favorite: Boolean) {
        //
    }

    private fun playSong(playList: PlayList?, playIndex: Int) {
        if (playList == null) return

        playList.playMode = PreferenceManager.lastPlayMode(applicationContext)
        mediaPlayer?.play(playList, playIndex)
    }

    private fun onPlayNextAction() {
        if (Player.instance == null) return

        MediaControllerCompat.getMediaController(this).transportControls.skipToNext()
    }

    // Click Events
    private fun onPlayToggleAction(playerState: Int) {
        //if (mediaPlayer == null) return
        if (Player.instance!!.playingList == null || Player.instance!!.playingList!!.numOfSongs == 0){
            AppExecutors.instance?.diskIO()?.execute {
                val recentPlayList = playlistDatabase?.playlistDAO()?.fetchOnePlayListMxpId("nowplaying")
                recentPlayList?.observe(this, object : Observer<PlayList> {
                    override fun onChanged(it: PlayList?) {
                        recentPlayList.removeObserver(this)
                        if (it?.tracks?.size != 0) {
                            playSong(it, it?.playingIndex!!)
                        } else {
                            runOnUiThread {
                                if (playerState == PlaybackStateCompat.STATE_PLAYING) {
                                    MediaControllerCompat.getMediaController(this@MainActivity).transportControls.pause()
                                    button_play_toggle.setImageResource(R.drawable.ic_play)
                                } else {
                                    MediaControllerCompat.getMediaController(this@MainActivity).transportControls.play()
                                    button_play_toggle.setImageResource(R.drawable.ic_pause)
                                }
                            }
                        }
                    }
                })
            }
        }else{
            if (playerState == PlaybackStateCompat.STATE_PLAYING) {
                MediaControllerCompat.getMediaController(this).transportControls.pause()
                button_play_toggle.setImageResource(R.drawable.ic_play)
            } else {
                MediaControllerCompat.getMediaController(this).transportControls.play()
                button_play_toggle.setImageResource(R.drawable.ic_pause)
            }
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
        //testMediaSession()
        if (Player.instance!!.mPlayList != null) {
            Player.instance!!.mPlayList!!.currentTrack?.observe(this, Observer<Track> { t ->
                namePlayback!!.text = t?.displayName
                artistPlayback!!.text = t?.artist
            })
        }
        updatePlayToggle(isPlaying)
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

    override fun onSongSetAsFavorite(track: Track) {
        //
    }

    override fun onSongUpdated(track: Track?) {
        if (track == null) {
            button_play_toggle.setImageResource(R.drawable.ic_play)
            return
        }

        namePlayback!!.text = track.displayName
        artistPlayback!!.text = track.artist
        if (mPlayer != null) {
            if (mPlayer!!.isPlaying) {
                button_play_toggle.setImageResource(R.drawable.ic_pause)
            }
            doAsync {
                //update
                loadNowPlayingTable(mPlayer!!.playingList!!)
            }
        }
    }

    override fun updatePlayMode(playMode: PlayMode) {
        //
    }

    override fun onTriggerLoading(isLoading: Boolean) {
        button_play_toggle!!.visibility = View.GONE
        loading!!.visibility = View.VISIBLE
    }

    override fun onPrepared(isPrepared: Boolean) {
        
    }

    override fun updatePlayToggle(play: Boolean) {
        button_play_toggle!!.visibility = View.VISIBLE
        loading!!.visibility = View.GONE
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
        AppExecutors.instance?.diskIO()?.execute {
            val result = playlistDatabase?.playlistDAO()?.fetchOnePlayListMxpId("nowplaying")
            result?.observe(this, object : Observer<PlayList> {
                override fun onChanged(t: PlayList?) {
                    result.removeObserver(this)
                    if (t == null) {
                        val playList = PlayList()
                        playList.name = "Recently Played"
                        playList.mxp_id = "nowplaying"
                        AppExecutors.instance?.diskIO()?.execute {
                            playlistDatabase?.playlistDAO()?.insertOnePlayList(playList)
                        }
                    } else {
                        if (t.tracks?.size!! > 0) {
                            lastPlayed(t)
                        }
                    }
                }
            })
        }
    }

    private fun lastPlayed(playlist: PlayList){
        playlist.playMode = PreferenceManager.lastPlayMode(applicationContext)
        //update player playing list
        playlist.name = "Recently Played"
        playlist.mxp_id = "nowplaying"
        AppExecutors.instance?.diskIO()?.execute {
            playlistDatabase?.playlistDAO()?.updatePlayList(playlist)
        }

        val song = playlist.currentTrack
        song?.observe(this, Observer<Track> { t ->
            if (t != null) {
                // Step 1: Track name and artist
                namePlayback!!.text = t.displayName
                artistPlayback!!.text = t.artist
            }
        })


        //display the palyback control panel
        mini_control_player.visibility = View.VISIBLE
    }

    private fun loadNowPlayingTable(playList: PlayList) {
        playList.name = "Recently Played"
        playList.mxp_id = "nowplaying"
        AppExecutors.instance?.diskIO()?.execute {
            playlistDatabase?.playlistDAO()?.updatePlayList(playList)
        }
    }


    private fun openPlayBack(){
        if(Player.instance!!.playingTrack != null){
            val bottomSheetDialog = MusicPlayerActivity().getInstance()
            bottomSheetDialog.show(supportFragmentManager, "Playback Bottom Sheet")
//            val intent = Intent(applicationContext, MusicPlayerActivity::class.java)
//            startActivity(intent)
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
                        tablayout()
                        //Application Settings
                        com.musicplayer.aow.utils.Settings.instance!!.intialization()
                        StorageUtil(applicationContext).storageLocationDir()
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
        builder.setPositiveButton("GOTO SETTINGS", { dialog, _ ->
            dialog.cancel()
            openSettings()
        })
        builder.setNegativeButton("Cancel", { dialog, _ ->
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
        val dialogN = AlertDialog.Builder(applicationContext, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
        dialogN.setTitle("New update available")
        dialogN.setMessage("Please, update app to new version to continue.")
        dialogN.setPositiveButton("Update",
                        { _, _ ->
                            redirectStore(updateUrl)
                        })

        dialogN.setNegativeButton("Cancel",
                        { _, _ ->

                        }).create()
        //dialogN.show()
    }

    //Force Update Firebase
    private fun redirectStore(updateUrl:String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private val OVERLAY_PERMISSION_REQ_CODE: Int = 1000

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // now you can show audio widget
                //
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (mMediaBrowserCompat != null) {
            if (!mMediaBrowserCompat?.isConnected!!) {
                mMediaBrowserCompat?.connect()
            }
        }
    }

    override fun onDestroy() {
        mPresenter!!.unsubscribe()
        //sensey gesture
        Sensey.getInstance().stop()
        if (mMediaBrowserCompat != null) {
            if (mMediaBrowserCompat!!.isConnected) {
                mMediaBrowserCompat!!.disconnect()
            }
        }
        super.onDestroy()
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
