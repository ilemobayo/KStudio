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
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.nowplaying.draganddropinterface.SimpleItemTouchHelperCallback
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_now_playing.*


class NowPlayingActivity : AppCompatActivity(), NowPlayingAdapter.OnDragStartListener {

    private var mItemTouchHelper: ItemTouchHelper? = null
    var adapter: NowPlayingAdapter? = null

    //room for resntly played
    private var playlistDatabase = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

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
        //sort the track list in ascending order
        val playingIndex = Player.instance!!.mPlayList!!.playingIndex
        playing_now_songs.setHasFixedSize(true)
        val lm = LinearLayoutManager(baseContext.applicationContext)
        lm.scrollToPosition(playingIndex)
        playing_now_songs.layoutManager = lm

        adapter = NowPlayingAdapter(Injection.provideContext()!!, playList,this, this)
        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(playing_now_songs)
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
        val resultPlaylist = playlistDatabase?.playlistDAO()?.fetchOnePlayListMxpId("nowplaying")
        resultPlaylist?.observe(this, Observer<PlayList> { result ->
            if(result != null ) {
                if (result.tracks?.size!! > 0) {
                    loadData(result)
                } else {
                    //loadAllSongs()
                }
            }else{
                val playList = PlayList()
                playList.name = "Recently Played"
                playList.mxp_id = "nowplaying"
                playlistDatabase?.playlistDAO()?.insertOnePlayList(playList)
                loadData(playList)
            }
        })

    }
}
