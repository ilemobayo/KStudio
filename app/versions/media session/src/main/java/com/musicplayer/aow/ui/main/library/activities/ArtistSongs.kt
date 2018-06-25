package com.musicplayer.aow.ui.main.library.activities

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.PlayAlbumNowEvent
import com.musicplayer.aow.ui.main.library.activities.artistsonglist.ArtistSongsListAdapter
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 * Created by Arca on 12/1/2017.
 */
class ArtistSongs : AppCompatActivity() {

    private val mRepository: AppRepository? = AppRepository.instance
    private val mSubscriptions: CompositeSubscription? = null
    private var artistModelData: ArrayList<Song> = ArrayList()
    var songsList:List<Song>? = null
    private var numberOfSongs: TextView? = null
    private var artistArtMain: ImageView? = null
    private var artistArtName: TextView? = null
    var playArtistFab: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_song_list)
        val toolbarNavigation = findViewById<AppCompatImageView>(R.id.toolbar_album_song_list)

        // Set the padding to match the Status Bar height
        toolbarNavigation.setOnClickListener {
            finish()
        }

        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)

        numberOfSongs = find<TextView>(R.id.numbers_of_songs)
        artistArtMain = findViewById<ImageView>(R.id.image_view_artist_art_main)
        artistArtName = findViewById<TextView>(R.id.image_view_artist_name_main)
        mArtistList = findViewById<RecyclerView>(R.id.artist_songs_recycler_views)
        playArtistFab = findViewById<Button>(R.id.fab_play_artist)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("com.musicplayer.aow.artist.name")
            if (data != null) {
                artistArtName!!.text = data
                collapsingToolbarLayout.title = data
                collapsingToolbarLayout.setContentScrimColor(Color.WHITE)
                loadAllSongs(data)
            }
        }

    }


    private var mArtistList: RecyclerView? = null
    private var mArtistAdapter: ArtistSongsListAdapter? = null

    fun loadData(list: ArrayList<Song>) {
        //sort the song list in ascending order
        songsList = list.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))

        if(songsList!! != null) {
            if (songsList!![0].albumArt != null) {
                val Art = Drawable.createFromPath(songsList!![0].albumArt)
                artistArtMain!!.setImageDrawable(Art)
            } else {
                //Drawable Text
                var generator = ColorGenerator.MATERIAL // or use DEFAULT
                // generate random color
                var color1 = generator.randomColor
                var icon = TextDrawable.builder().beginConfig()
                        .width(55)  // width in px
                        .height(55) // height in px
                        .endConfig().buildRect(songsList!![0].title!!.substring(0,1), color1)
                artistArtMain!!.setImageDrawable(icon)
            }
        }

        //play as playlist when album art is clicked
        playArtistFab!!.setOnClickListener {
            RxBus.instance!!.post(PlayAlbumNowEvent(songsList!!))
        }

        var sizeOfSongs = songsList!!.size
        if(sizeOfSongs > 1){
            numberOfSongs!!.text = sizeOfSongs.toString() + " Tracks"
        }else{
            numberOfSongs!!.text = sizeOfSongs.toString() + " Track"
        }
        mArtistAdapter = ArtistSongsListAdapter(applicationContext, PlayList(songsList), this)
        mArtistList!!.adapter = mArtistAdapter
        var layoutManager = LinearLayoutManager(applicationContext)
        mArtistList!!.setHasFixedSize(true)
        mArtistList!!.layoutManager = layoutManager
    }

    private fun loadAllSongs(name: String) {
        val subscription = mRepository
                ?.playlist(resources.getString(R.string.mp_play_list_songs))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {

                        result.songs.forEach {
                            if(it.artist.equals(name)){
                                artistModelData.add(it)
                            }
                        }
                        loadData(artistModelData)
                    }
                })
        mSubscriptions?.add(subscription)
    }

}