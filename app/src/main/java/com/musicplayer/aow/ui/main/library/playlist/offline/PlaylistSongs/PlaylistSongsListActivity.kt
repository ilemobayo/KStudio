package com.musicplayer.aow.ui.main.library.playlist.offline.PlaylistSongs

import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.ui.main.library.playlist.offline.PlaylistSongs.adapter.PlaylistSongsAdapter
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.musicplayer.aow.utils.CursorDB
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_play_list_details.*

/**
 * Created by Arca on 1/7/2018.
 */
class PlaylistSongsListActivity : AppCompatActivity() {

    var playlist = PlayList()
    private var mList: RecyclerView? = null

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

        play_all.setOnClickListener {
            RxBus.instance!!.post(PlayListNowEvent(playlist, 0))
        }

        mList = findViewById(R.id.playlist_songs_recycler_views)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("name")
            val id =  intent.getLongExtra("_id", 0)
            if (data != null) {
                supportActionBar!!.title = data
                val Uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id)
                val cursor = contentResolver.query(Uri, null, null, null, null)
                if(cursor != null){
                    while (cursor.moveToNext()){
                        playlist.addSong(CursorDB().cursorToMusicPlaylist(cursor))
                    }
                }else{
                    empty.visibility = View.VISIBLE
                }
                cursor.close()
            }
            loadData(id)
        }

    }

    fun loadData(id: Long) {
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
        mList!!.adapter = PlaylistSongsAdapter(this, id, playlist, this)
    }
}