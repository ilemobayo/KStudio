package com.musicplayer.aow.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.github.nisrulz.sensey.Sensey
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.event.PlayAlbumNowEvent
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.event.PlaySongEvent
import com.musicplayer.aow.delegates.player.AudioFocus
import com.musicplayer.aow.delegates.player.IPlayback
import com.musicplayer.aow.delegates.player.PlayMode
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.eq.EqActivity
import com.musicplayer.aow.ui.main.library.LibraryAdapter
import com.musicplayer.aow.ui.main.library.playlist.PlayListAction
import com.musicplayer.aow.ui.music.MusicPlayerActivity
import com.musicplayer.aow.ui.nowplaying.NowPlaying
import com.musicplayer.aow.ui.nowplaying.NowPlayingActivity
import com.musicplayer.aow.ui.settings.SettingsActivity
import com.musicplayer.aow.ui.splashscreen.SplashScreen
import com.musicplayer.aow.ui.webview.NaijaLoadedWeb
import com.musicplayer.aow.utils.ApplicationSettings
import com.musicplayer.aow.utils.StorageUtil
import com.musicplayer.aow.utils.gesture.GestureListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottomsheet_layout.*
import kotlinx.android.synthetic.main.current_playing_panel.*
import org.jetbrains.anko.doAsync
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription


class MainActivity : BaseActivity(),
        MusicPlayerContract.View,
        IPlayback.Callback,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    //init objects
    private var audioFocus = AudioFocus.instance
    private var mMediaBrowser: MediaBrowserCompat? = null

    //Music BottomSheet Init Componet
    private var mPlayer: IPlayback? = null
    private val mHandler = Handler()
    private var mPresenter: MusicPlayerContract.Presenter? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    //ui element
    private var name_playback: TextView? = null
    private var artist_Playback: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        var window = window
        window.allowEnterTransitionOverlap
        MusicPlayerPresenter(applicationContext, this).subscribe()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //GPU overdraw optimaization
        getWindow().setBackgroundDrawable(null)

        name_playback = findViewById<TextView>(R.id.text_view_name)
        artist_Playback = findViewById<TextView>(R.id.text_view_artist)

        //load resently played
        doAsync {
            loadResent()
        }

        nav_view.itemIconTintList = null
        nav_view.setNavigationItemSelectedListener(this)

        //Toolbar
        setupToolbar()
        setupDrawerToggle()

        volumeControlStream = AudioManager.STREAM_MUSIC
        //END OF BOTTOMSHEET

        // Inflate ViewPager
        tablayout()
        viewPager()

        //MediaSession

    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle!!.syncState()

        //close the splash screen activity
        try {
            SplashScreen.instance!!.finish()
        }catch (e: KotlinNullPointerException){
            //
        }catch (e: Exception){
            //
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
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.title = resources.getString(R.string.application_name)
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
        //return super.onCreateOptionsMenu(menu)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.nav_browse -> {
                val webUrl = ""
                val intent = Intent(applicationContext,NaijaLoadedWeb::class.java)
                intent.putExtra("address", webUrl)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
//            R.id.nav_account -> {
//                val intent = Intent(applicationContext, AuthActivity::class.java)
//                startActivity(intent)
//            }
            R.id.nav_now_playing -> {
                val intent = Intent(applicationContext, NowPlayingActivity::class.java)
                startActivity(intent)
            }
//            R.id.nav_event -> {
//                val intent = Intent(applicationContext, EventsListActivity::class.java)
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

    override fun onResume() {
        ApplicationSettings().ShakeWithSensorDetectorResume()
        super.onResume()
    }

    override fun onStop() {
        //update resently played table
        if(mPlayer != null) {
            if(mPlayer!!.playingList != null) {
                loadNowPlayingTable(mPlayer!!.playingList!!)
            }
        }
        mHandler.removeCallbacks(mProgressCallback)
        ApplicationSettings().ShakeWithSensorDetectorStop()
        super.onStop()
    }

    override fun onDestroy() {
        //update resently played table
        if(mPlayer != null) {
            if(mPlayer!!.playingList != null) {
                loadNowPlayingTable(mPlayer!!.playingList!!)
            }
        }
        mPresenter!!.unsubscribe()
        //sensey gesture
        Sensey.getInstance().stop()
        ApplicationSettings.instance?.ShakeWithSensorDetectorDestroy()
        //MediaSession destroy
        //mMediaBrowser!!.disconnect();
        super.onDestroy()
    }


    private val mProgressCallback = Runnable {
        //
    }


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
            //tab.getIcon().clearColorFilter();
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //for removing the color of first icon when switched to next tab
                library_tablayout.getTabAt(0)!!.icon!!.clearColorFilter()
                //for other tabs
                tab!!.icon!!.clearColorFilter()
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab!!.icon!!.setColorFilter(resources.getColor(R.color.red_dim), PorterDuff.Mode.SRC_IN)
                library_view_pager.currentItem = tab.position
            }

        })

        library_tablayout.setOnTouchListener(object: GestureListener(applicationContext, null) {
            override fun onSwipeLeft(){
                onPlayNextAction()
            }
            override fun onSwipeRight(){
                onPlayNextAction()
            }
        })
    }

    private fun viewPager(){
        var adapter = LibraryAdapter(supportFragmentManager,library_tablayout.tabCount)
        library_view_pager.offscreenPageLimit = 5
        library_view_pager.adapter = adapter
        library_view_pager.setOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(library_tablayout))
    }

    override fun onClick(v: View) {
        //nothing
    }


    override fun onStart() {
        super.onStart()
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
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
        val playList = PlayList(songs)
        playSong(playList, 0)
    }

    private fun playSong(playList: PlayList?, playIndex: Int) {
        if (playList == null) return

        playList.playMode = PreferenceManager.lastPlayMode(applicationContext)
        val result = mPlayer!!.play(playList, playIndex)
        val song = playList.currentSong
        // Step 1: Song name and artist
        if (song != null) {
            name_playback!!.text = song.displayName
            artist_Playback!!.text = song.artist
        }
        //update playback button
        if (result) {
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
            val recent_play_list = NowPlaying.instance!!.playlist
            if (recent_play_list.numOfSongs != 0) {
                playSong(recent_play_list, recent_play_list.playingIndex)
                return
            }
        }

        if (mPlayer!!.isPlaying) {
            audioFocus!!.pause()
            mPlayer!!.pause()
        } else {
            audioFocus!!.play()
            mPlayer!!.play()
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

        name_playback!!.text = song.displayName
        artist_Playback!!.text = song.artist

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

    override fun updatePlayToggle(play: Boolean) {
        button_play_toggle.setImageResource(if (play) R.drawable.ic_pause else R.drawable.ic_play)
    }

    override fun setPresenter(presenter: MusicPlayerContract.Presenter) {
        mPresenter = presenter
    }

    private val mSubscriptions: CompositeSubscription? = null

    private fun loadResent(){
        var storage = StorageUtil(applicationContext)
        val result = PlayList()
        result.setSongs(storage.loadAudio()!!)
        if (result.numOfSongs > 0) {
            result.playingIndex = StorageUtil(applicationContext).loadStringValue("playing_index")!!.toInt()
            lastPlayed(result)
        }else{
            loadAllSongs()
        }
    }

    private fun loadAllSongs() {
        val subscription = AppRepository().playlist(this.resources.getString(R.string.mp_play_list_songs)).subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                        result.playingIndex = 0
                        lastPlayed(result)
                    }
                })
        mSubscriptions?.add(subscription)
    }

    private fun lastPlayed(playlist: PlayList){
        playlist.playMode = PreferenceManager.lastPlayMode(applicationContext)
        //update player playing list
        NowPlaying.instance!!.setPlayList(playlist)
        var song = playlist.currentSong
        if (song != null) {
            // Step 1: Song name and artist
            name_playback!!.text = song.displayName
            artist_Playback!!.text = song.artist
        }else{
            return
        }
        //display the palyback control panel
        mini_control_player.visibility = View.VISIBLE
    }

    private fun loadNowPlayingTable(playList: PlayList) {
        var storage = StorageUtil(applicationContext)
        var arraylist = ArrayList<Song>()
        playList.songs.forEach {
            arraylist.add(it)
        }
        storage.storeAudio(arraylist)
        storage.saveStringValue("playing_index", playList.playingIndex.toString())
        //Update UI
        RxBus.instance!!.post(PlayListAction(true))
    }


    private fun openPlayBack(){
        if(mPlayer!!.playingSong != null){
            val intent = Intent(applicationContext, MusicPlayerActivity::class.java)
            startActivity(intent)
        }
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
