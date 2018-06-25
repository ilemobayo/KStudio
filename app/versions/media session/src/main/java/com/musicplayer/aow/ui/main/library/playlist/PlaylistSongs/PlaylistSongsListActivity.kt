package com.musicplayer.aow.ui.main.library.playlist.PlaylistSongs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.ui.main.library.playlist.PlaylistSongs.adapter.PlaylistSongsAdapter
import kotlinx.android.synthetic.main.activity_play_list_details.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * Created by Arca on 1/7/2018.
 */
class PlaylistSongsListActivity : AppCompatActivity() {

    var playlist = PlayList()
    private val mSubscriptions: CompositeSubscription? = null
    private var mList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_list_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black, resources.newTheme())
        toolbar.setNavigationOnClickListener{
            finish()
        }

        play_all.setOnClickListener {
            RxBus.instance!!.post(PlayListNowEvent(playlist, 0))
        }

        mList = findViewById(R.id.playlist_songs_recycler_views)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("name")
            if (data != null) {
                toolbar.title = data
                playlist(data)
            }
        }

    }

    fun loadData(data: PlayList) {
        playlist = data
        playlist.songs.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        var layoutManager = LinearLayoutManager(this)
        mList!!.setHasFixedSize(true)
        mList!!.layoutManager = layoutManager
        mList!!.adapter = PlaylistSongsAdapter(this, playlist, this)
    }

    fun playlist(name: String) {
        val subscription = AppRepository().playlist(name)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() { }

                    override fun onCompleted() { }

                    override fun onError(e: Throwable) { }

                    override fun onNext(playList: PlayList) {
                        loadData(playList)
                    }
                })
        mSubscriptions?.add(subscription)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, PlaylistSongsListActivity::class.java)
            return intent
        }
    }
}