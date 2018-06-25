package com.musicplayer.aow.ui.main.library.activities

import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.graphics.drawable.DrawableCompat
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
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.PlayAlbumNowEvent
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.ui.main.library.activities.artistsonglist.ArtistSongsListAdapter
import com.musicplayer.aow.utils.CursorDB
import com.musicplayer.aow.utils.images.BitmapDraws
import kotlinx.android.synthetic.main.activity_artist_song_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import java.util.*


/**
 * Created by Arca on 12/1/2017.
 */
class ArtistSongs : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>  {

    private var ARTIST: String = ""
    private val MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private var WHERE = (MediaStore.Audio.Media.SIZE + ">0 AND " + MediaStore.Audio.Media.ARTIST + "=\"$ARTIST\"")
    private val ORDER_BY = MediaStore.Audio.Media.TITLE + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Media.DATA, // the real path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE)
    private var songs: PlayList = PlayList()

    private var adapter: ArtistSongsListAdapter? = null
    private var artistModelData: ArrayList<Song> = ArrayList()
    var songsList:List<Song>? = null
    private var artistArtMain: ImageView? = null
    var playArtistFab: Button? = null
    private var mArtistList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_song_list)

        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }

        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbarLayout.title = "Item"
        collapsingToolbarLayout.setExpandedTitleColor(resources.getColor(android.R.color.transparent))

        artistArtMain = findViewById(R.id.image_view_artist_art_main)
        mArtistList = findViewById(R.id.artist_songs_recycler_views)
        playArtistFab = findViewById(R.id.fab_play_artist)
        //paying audio from other apps
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("com.musicplayer.aow.artist.name")
            if (data != null) {
                val bundle = Bundle()
                bundle.putString("name", data)
                supportLoaderManager.initLoader(0, bundle, this)
                toolbar.title = data
                collapsingToolbarLayout.title = data
                collapsingToolbarLayout.setContentScrimColor(Color.WHITE)
                //Album Art
                doAsync {
                    val alb = Injection.provideContext()!!
                            .contentResolver.query(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            arrayOf(
                                    MediaStore.Audio.Albums._ID,
                                    MediaStore.Audio.Albums.ALBUM_ART),
                            MediaStore.Audio.Albums.ARTIST + "=?",
                            arrayOf(data),
                            null)
                    onComplete {
                        if (alb.moveToFirst()) {
                            val data = alb.getString(alb.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                            val albumArt = BitmapDraws.createFromPath(data)
                            if (albumArt != null) {
                                artistArtMain!!.setImageDrawable(albumArt)
                                //color from image
                                Palette.from(SoftCodeAdapter().convertToBitmap(albumArt, 50, 50))
                                        .generate(Palette.PaletteAsyncListener { palette ->
                                            val vibrant = palette.vibrantSwatch
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
                                            collapsingToolbarLayout.setContentScrimColor(vibrant.rgb);
                                            collapsingToolbarLayout.setStatusBarScrimColor(vibrantDV.population)
                                        })
                            }else{
                                artistArtMain!!.setImageResource(R.drawable.gradient_danger)
                            }
                        }
                        alb.close()
                    }

                }
                data()
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

    fun data(){
        //sort the song list in ascending order
        songsList = artistModelData.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        //play as playlist when album art is clicked
        playArtistFab!!.setOnClickListener {
            Toast.makeText(this," hello", Toast.LENGTH_SHORT).show()
            RxBus.instance!!.post(PlayAlbumNowEvent(songsList!!))
        }

        adapter = ArtistSongsListAdapter(this, songs, this)
        mArtistList!!.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        mArtistList!!.setHasFixedSize(true)
        mArtistList!!.layoutManager = layoutManager
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        ARTIST = args?.getString("name")!!
        WHERE = (MediaStore.Audio.Media.SIZE + ">0 AND " + MediaStore.Audio.Media.ARTIST + "=\"$ARTIST\"")
        return CursorLoader(applicationContext, MEDIA_URI,
                PROJECTIONS, WHERE, null,
                ORDER_BY)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        songs = PlayList()
        if (data != null) {
            doAsync {
                while (data.moveToNext()) {
                    songs.addSong(CursorDB().cursorToMusic(data))
                }
                onComplete {
                    adapter?.swapCursor(songs.songs as ArrayList<Song>)
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter?.swapCursor(null)
    }

}