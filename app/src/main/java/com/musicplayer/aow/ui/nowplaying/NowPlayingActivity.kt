package com.musicplayer.aow.ui.nowplaying

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist.PlayListFavDatabase
import com.musicplayer.aow.ui.nowplaying.draganddropinterface.SimpleItemTouchHelperCallback
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_now_playing.*
import rx.subscriptions.CompositeSubscription


class NowPlayingActivity : AppCompatActivity(), NowPlayingAdapter.OnDragStartListener {

    private val mSubscriptions: CompositeSubscription? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    var adapter: NowPlayingAdapter? = null

    //room for resntly played
    private var playListFavDatabase = PlayListFavDatabase.getsInstance(Injection.provideContext()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

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
        tintManager.setStatusBarTintResource(R.drawable.gradient_info);


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black, resources.newTheme())
        toolbar.setNavigationOnClickListener{
            finish()
        }


        loadResent()
    }

    private fun loadData(playList: PlayList) {
        //sort the song list in ascending order
        val playingIndex = Player.instance!!.mPlayList!!.playingIndex
        playing_now_songs.setHasFixedSize(true)
        val lm = LinearLayoutManager(baseContext.applicationContext)
        lm.scrollToPosition(playingIndex)
        playing_now_songs.layoutManager = lm

        adapter = NowPlayingAdapter(Injection.provideContext()!!, playList, mSubscriptions, this, this)
        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(playing_now_songs);
        playing_now_songs.addItemDecoration(
                DividerItemDecoration(
                        applicationContext!!.getDrawable(
                                R.drawable.drawble_divider
                        ),
                        false,
                        false)
        )
        playing_now_songs.adapter =  adapter
    }

    override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }


    private fun loadResent(){
        val resultPlaylist = playListFavDatabase?.playlistFavDAO()?.fetchOnePlayListMxpId("nowplaying")
        resultPlaylist?.observe(this, object: Observer<PlayList> {
            override fun onChanged(result: PlayList?) {
                if(result != null ) {
                    if (result.songs?.size!! > 0) {
                        loadData(result)
                    } else {
                        //loadAllSongs()
                    }
                }else{
                    val playList = PlayList()
                    playList.name = "Recently Played"
                    playList.mxp_id = "nowplaying"
                    playListFavDatabase?.playlistFavDAO()?.insertOnePlayList(playList)
                    loadData(playList)
                }
            }
        })

    }
}
