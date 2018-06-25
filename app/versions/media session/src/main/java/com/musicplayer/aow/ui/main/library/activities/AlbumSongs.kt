package com.musicplayer.aow.ui.main.library.activities

import android.annotation.SuppressLint
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
import com.musicplayer.aow.ui.main.library.activities.albumsonglist.AlbumSongListAdapter
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 * Created by Arca on 12/1/2017.
 */
class AlbumSongs : AppCompatActivity() {

    private val mRepository: AppRepository? = AppRepository.instance
    private val mSubscriptions: CompositeSubscription? = null

    var albumModelData: ArrayList<Song> = ArrayList()
    private var songsList:List<Song>? = null
    private var albumArt: ImageView? = null
    private var albumArtMain: ImageView? = null
    private var albumArtName: TextView? = null
    var playAlbumFab: Button? = null

    private var numberOfSongs: TextView? = null
    private var mAlbumList: RecyclerView? = null
    private var mAlbumAdapter: AlbumSongListAdapter? = null
    private var albumNotNull: String? = null

    // A method to find height of the status bar
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_song_list)
        val toolbarNavigation = findViewById<AppCompatImageView>(R.id.toolbar_album_song_list)

        // Set the padding to match the Status Bar height
        toolbarNavigation.setOnClickListener {
            finish()
        }

        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)

        numberOfSongs = find<TextView>(R.id.numbers_of_songs)
        albumArtMain = findViewById<ImageView>(R.id.image_view_album_art_main)
        albumArtName = findViewById<TextView>(R.id.image_view_album_name_main)
        mAlbumList = findViewById<RecyclerView>(R.id.album_songs_recycler_views)
        playAlbumFab = findViewById<Button>(R.id.fab_play_album)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("com.musicplayer.aow.album.name")
            if (data != null) {
                albumArtName!!.text = data
                collapsingToolbarLayout.title = data
                collapsingToolbarLayout.setContentScrimColor(Color.WHITE)
                loadAllSongs(data)
            }
        }
    }

    fun loadData(list: ArrayList<Song>){
        //context or activity
        songsList = list.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        if(songsList!! != null) {
            if (songsList!![0].albumArt != null) {
                val Art = Drawable.createFromPath(songsList!![0].albumArt)
                albumArtMain!!.setImageDrawable(Art)
            } else {
                //Drawable Text
                var generator = ColorGenerator.MATERIAL // or use DEFAULT
                // generate random color
                var color1 = generator.randomColor
                var icon = TextDrawable.builder().beginConfig()
                        .width(55)  // width in px
                        .height(55) // height in px
                        .endConfig().buildRect(songsList!![0].title!!.substring(0,1), color1)
                albumArtMain!!.setImageDrawable(icon)
            }
        }

        //sort the song list in ascending order
        songsList = albumModelData.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        //play as playlist when album art is clicked
        playAlbumFab!!.setOnClickListener {
            RxBus.instance!!.post(PlayAlbumNowEvent(songsList!!))
        }

        var sizeOfSongs = songsList!!.size
        if(sizeOfSongs > 1){
            numberOfSongs!!.text = sizeOfSongs.toString() + " Tracks"
        }else{
            numberOfSongs!!.text = sizeOfSongs.toString() + " Track"
        }

        mAlbumAdapter = AlbumSongListAdapter(this, PlayList(songsList), this)
        mAlbumList!!.adapter = mAlbumAdapter
        var layoutManager = LinearLayoutManager(this)
        mAlbumList!!.setHasFixedSize(true)
        mAlbumList!!.layoutManager = layoutManager
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
                            if(it.album!!.toLowerCase().equals(name.toLowerCase())){
                                albumModelData.add(it)
                            }
                        }
                        loadData(albumModelData)
                    }
                })
        mSubscriptions?.add(subscription)
    }
}