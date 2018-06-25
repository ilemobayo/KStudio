package com.musicplayer.aow.ui.main.library.home.browse

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.PlaySongEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.main.library.songs.Adapter.SongListAdapter
import kotlinx.android.synthetic.main.browse_list_activity.*
import org.json.JSONObject


/**
 * Created by Arca on 2/16/2018.
 */
class BrowseActivity: BaseActivity(){

    var mPlayer = Player.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var window = this.window
        window.statusBarColor = Color.RED

        setContentView(R.layout.browse_list_activity)
        ButterKnife.bind(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = getWindow()
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.black)
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }


        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("com.musicplayer.aow.section")
            if (data != null) {
                val jsonObj = JSONObject(data)
                item_name.text = jsonObj.getString("name")
                item_owner.text = jsonObj.getString("artist")

                var song = Song(jsonObj.getString("name"),jsonObj.getString("name"),
                        jsonObj.getString("artist"),"mucicxplay discovery", jsonObj.getString("link"),
                        30000, 1000, false, 0, "", jsonObj.getString("url"))
                play_all.setOnClickListener {
                    //mediaPlayer!!.mPlayList = PlayList(song)
                    //Player.instance!!.playStream(PlayList(song), 0)
                    RxBus.instance!!.post(PlaySongEvent(song))
                }
                Glide.with(this)
                        .load(jsonObj.getString("url"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .error(R.drawable.nigerian_artists)
                        .into(album_art)
                var songs = PlayList(song)
                recycler_views.layoutManager = LinearLayoutManager(applicationContext)
                recycler_views.adapter = SongListAdapter(applicationContext, songs, this )
            }
        }

    }


}