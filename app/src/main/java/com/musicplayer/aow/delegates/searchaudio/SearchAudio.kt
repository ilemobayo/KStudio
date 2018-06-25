package com.musicplayer.aow.delegates.searchaudio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.TextView
import com.github.ybq.android.spinkit.SpinKitView
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.TempSongs
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import rx.subscriptions.CompositeSubscription


class SearchAudio : AppCompatActivity() {

    private val mSubscriptions: CompositeSubscription? = null

    var loaderView: SpinKitView? = null
    var numbersOfSongs: TextView? = null
    var doneBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_audio)
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.title = "SEARCH AUDIO FILES."
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        //loader
        loaderView = findViewById<SpinKitView>(R.id.spin_kit)
        //back button
        doneBtn  = findViewById<Button>(R.id.search_label)
        doneBtn!!.setOnClickListener {
            finish()
        }

        numbersOfSongs = findViewById<TextView>(R.id.numbers_of_songs_label)

        loaderView!!.setOnClickListener {
            //run search in another thread using anko from jetbrains
            doAsync {
                TempSongs.instance!!.setSongs()
                onComplete {
                    updateAllSongsPlayList(playList = PlayList(TempSongs.instance!!.songs))
                }
            }

        }
    }

    private fun updateAllSongsPlayList(playList: PlayList) {

    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SearchAudio::class.java)
            return intent
        }
    }
}
