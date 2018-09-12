package com.musicplayer.aow.ui.main.library.playlist.offline.PlaylistSongs

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.main.library.playlist.offline.PlaylistSongs.adapter.PlaylistSongsAdapter
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.musicplayer.aow.utils.CursorDB
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_play_list_details.*

/**
 * Created by Arca on 1/7/2018.
 */
class PlaylistSongsListActivity : AppCompatActivity() {

    private var mList: RecyclerView? = null
    private var playlistDatabase = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_list_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener{
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


        mList = findViewById(R.id.playlist_songs_recycler_views)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("name")
            if (data != null) {
                AppExecutors.instance?.diskIO()?.execute {
                    val resultPlaylist = playlistDatabase?.playlistDAO()?.fetchOnePlayListName(data)
                    resultPlaylist?.observe(this, Observer<PlayList> {
                        resultPlaylist.removeObservers(this)
                        if (it != null) {
                            loadData(it)
                        }else{
                            playlist_songs_recycler_views.visibility = View.GONE
                            empty.visibility = View.VISIBLE
                        }
                    })
                }
            }else{
                playlist_songs_recycler_views.visibility = View.GONE
                empty.visibility = View.VISIBLE
            }
        }

    }

    fun loadData(playlist: PlayList) {
        play_all.setOnClickListener {
            Player.instance?.play(playlist, 0)
        }
        playlist.tracks?.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        val layoutManager = LinearLayoutManager(this)
        mList!!.addItemDecoration(
                DividerItemDecoration(
                        this.getDrawable(
                                R.drawable.drawble_divider
                        ),
                        false,
                        true)
        )

        if(playlist.tracks!!.isEmpty() || playlist.tracks?.size!! <= 0) {
            playlist_songs_recycler_views.visibility = View.GONE
            empty.visibility = View.VISIBLE
        }else{
            mList!!.setHasFixedSize(true)
            mList!!.layoutManager = layoutManager
            mList!!.adapter = PlaylistSongsAdapter(this, playlist, this)
        }
    }
}