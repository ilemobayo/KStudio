package com.musicplayer.aow.ui.nowplaying

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.player.Player
import kotlinx.android.synthetic.main.activity_now_playing.*
import rx.subscriptions.CompositeSubscription


class NowPlayingActivity : AppCompatActivity() {

    private val mSubscriptions: CompositeSubscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val playList = NowPlaying.instance!!.playlist
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
        loadData(playList)
    }

    private fun loadData(playList: PlayList) {
        //sort the song list in ascending order
        val playingIndex = Player.instance!!.mPlayList!!.playingIndex
        playing_now_songs.setHasFixedSize(true)
        val lm = LinearLayoutManager(baseContext.applicationContext)
        lm.scrollToPosition(playingIndex)
        playing_now_songs.layoutManager = lm
        playing_now_songs.adapter = NowPlayingAdapter(Injection.provideContext()!!, playList, mSubscriptions, this)
    }
}
