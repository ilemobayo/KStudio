package com.musicplayer.aow.delegates.searchaudio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.github.ybq.android.spinkit.SpinKitView
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.TempSongs
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.ReloadEvent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription


class SearchAudio : AppCompatActivity() {

    private val mRepository: AppRepository = AppRepository.instance!!
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
        if (playList != null) {
            val subscription = mRepository.setInitAllSongs(playList).subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(object : Subscriber<PlayList>() {
                        override fun onStart() {}

                        override fun onCompleted() {}

                        override fun onError(e: Throwable) {
                            doneBtn!!.visibility = View.INVISIBLE
                            numbersOfSongs!!.text = "Please restart app."
                            numbersOfSongs!!.visibility = View.VISIBLE
                        }

                        override fun onNext(result: PlayList) {
                            if (result != null) {
                                //show numbers of audio found
                                numbersOfSongs!!.text = "Total numbers of tracks found on device: " + result.numOfSongs
                                loaderView!!.visibility = View.INVISIBLE
                                numbersOfSongs!!.visibility = View.VISIBLE
                                doneBtn!!.visibility = View.VISIBLE
                                RxBus.instance!!.post(ReloadEvent(null))
                            } else {
                                doneBtn!!.visibility = View.INVISIBLE
                                numbersOfSongs!!.text = "Error Searching device for tracks."
                                numbersOfSongs!!.visibility = View.VISIBLE
                                RxBus.instance!!.post(ReloadEvent(null))
                            }
                        }
                    })
            mSubscriptions?.add(subscription)
        }else{
            doneBtn!!.visibility = View.INVISIBLE
            numbersOfSongs!!.text = "Tracks not found on device."
            numbersOfSongs!!.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SearchAudio::class.java)
            return intent
        }
    }
}
