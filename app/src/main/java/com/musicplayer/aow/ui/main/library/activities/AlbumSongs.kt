package com.musicplayer.aow.ui.main.library.activities

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.PlayAlbumNowEvent
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.ui.main.library.activities.albumsonglist.AlbumSongListAdapter
import com.musicplayer.aow.utils.CursorDB
import kotlinx.android.synthetic.main.activity_album_song_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import java.util.*


/**
 * Created by Arca on 12/1/2017.
 */
class AlbumSongs : AppCompatActivity(){

    private var trackDatabase: TrackDatabase? = TrackDatabase.getsInstance(Injection.provideContext()!!)
    private var songs: PlayList = PlayList()

    var albumModelData: ArrayList<Track> = ArrayList()
    private var songsList:List<Track>? = null
    private var albumArtMain: ImageView? = null
    var playAlbumFab: Button? = null

    private var mAlbumList: RecyclerView? = null
    private var adapter: AlbumSongListAdapter? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_song_list)

        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), "")
        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbarLayout.title = "Item"
        collapsingToolbarLayout.setExpandedTitleColor(resources.getColor(android.R.color.transparent))

        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }

        
        albumArtMain = findViewById(R.id.image_view_album_art_main)
        mAlbumList = findViewById(R.id.album_songs_recycler_views)
        playAlbumFab = findViewById(R.id.fab_play_album)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val id = intent.getStringExtra("com.musicplayer.aow.album.id")
            val name = intent.getStringExtra("com.musicplayer.aow.album.name")
            val album_art = intent.getStringExtra("com.musicplayer.aow.album.album_art")
            if (id != null) {
                val bundle = Bundle()
                bundle.putString("_id", id)
                toolbar.title = name
                //collapsingToolbarLayout.title = name
                collapsingToolbarLayout.setContentScrimColor(Color.WHITE)
                //Album art
                val Art = Drawable.createFromPath(album_art)
                if(Art != null) {
                    albumArtMain!!.setImageDrawable(Art)

                    //color from image
                    Palette.from(SoftCodeAdapter().convertToBitmap(Art, 50, 50)!!)
                            .generate(Palette.PaletteAsyncListener { palette ->
                                val vibrant = palette!!.vibrantSwatch
                                val vibrantLV = palette.lightVibrantSwatch
                                val vibrantDV = palette.darkVibrantSwatch
                                val vibrantM = palette.mutedSwatch
                                val vibrantLM = palette.lightMutedSwatch
                                val vibrantDM = palette.darkMutedSwatch
                                if (vibrant == null || vibrantLV == null || vibrantDV == null ||
                                        vibrantM == null || vibrantLM == null || vibrantDM == null) {
                                    return@PaletteAsyncListener
                                }
                                setHomeUpIconColor(vibrant.bodyTextColor)
                                setOverflowButtonColor(vibrantLV.rgb)
                                collapsingToolbarLayout.setContentScrimColor(vibrant.rgb)
                                collapsingToolbarLayout.setStatusBarScrimColor(vibrantDV.population)
                            })
                } else {
                    albumArtMain!!.setImageResource(R.drawable.gradient_danger)
                }
                //sort the track list in ascending order
                songsList = albumModelData.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))

                adapter = AlbumSongListAdapter(this, songs, this)
                mAlbumList!!.adapter = adapter
                val layoutManager = LinearLayoutManager(this)
                mAlbumList!!.setHasFixedSize(true)
                mAlbumList!!.layoutManager = layoutManager
                AppExecutors.instance?.diskIO()?.execute {
                    val tracks = trackDatabase?.trackDAO()?.fetchAllTrackAlbum(name)
                    tracks?.observe(this, Observer<List<Track>> {
                        songs = PlayList(it as java.util.ArrayList<Track>)
                        adapter?.swapCursor(songs.tracks)
                    })
                }
            }
        }
    }

    private fun setOverflowButtonColor(color: Int) {
        var drawable = toolbar.overflowIcon
        if(drawable != null) {
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable.mutate(), color)
            toolbar.overflowIcon = drawable
        }
    }

    private fun setHomeUpIconColor(color: Int){
        toolbar.setTitleTextColor(color)
        val upArrow = resources.getDrawable(R.drawable.ic_arrow_back)
        upArrow.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = upArrow
    }


}