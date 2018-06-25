package com.musicplayer.aow.ui.main.library.playlist.online.playlistsongs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.gson.Gson
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_playlist_songs.*

class PlaylistSongsActivity : AppCompatActivity() {

    var playlist = PlayList()
    private var mList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            finish()
        }

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

        play_all.setOnClickListener {
            RxBus.instance!!.post(PlayListNowEvent(playlist, 0))
        }

        mList = findViewById(R.id.playlist_songs_recycler_views)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val name = intent.getStringExtra("name")
            val data = intent.getStringExtra("data")
            if (data != null) {
                val gson = Gson()
                val playlist = gson.fromJson(data, PlayList::class.java)
                supportActionBar!!.title = name
                loadData(playlist)
            }
            
        }

    }

    fun loadData(playlist: PlayList) {
        this.playlist = playlist
        playlist.songs?.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        val layoutManager = LinearLayoutManager(this)
        mList!!.addItemDecoration(
                DividerItemDecoration(
                        this.getDrawable(
                                R.drawable.drawble_divider
                        ),
                        false,
                        true)
        )
        mList!!.setHasFixedSize(true)
        mList!!.layoutManager = layoutManager
        mList!!.adapter = PlaylistSongAdapter(this, playlist, this)
    }
}
