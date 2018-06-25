package com.musicplayer.aow.ui.main.library.album


import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.ui.main.library.album.adapter.AlbumAdapter
import com.musicplayer.aow.utils.CursorDB
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete

class AlbumFragment : Fragment(){

    private val MEDIA_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    private val WHERE = null
    private val ORDER_BY = MediaStore.Audio.Albums.ALBUM + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Albums.ALBUM, // the real path
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ALBUM_KEY,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS)
    private var adapter: AlbumAdapter? = null
    var albumModelData:ArrayList<Album> = ArrayList()
    private var mAlbumList: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAlbumList = view.find(R.id.album_recycler_views)
        mAlbumList!!.layoutManager = GridLayoutManager(context!!.applicationContext, 2)

        data()
    }


    fun data(){
        loaderManager.initLoader(0, null, mLoaderCallbacks)
        adapter = AlbumAdapter(context!!.applicationContext, activity!!,albumModelData)
        mAlbumList!!.adapter = adapter
    }

    private val mLoaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return CursorLoader(context!!, MEDIA_URI,
                    PROJECTIONS, WHERE, null,
                    ORDER_BY)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            albumModelData = ArrayList()
            if (data != null) {
                doAsync {
                    while (data.moveToNext()) {
                        albumModelData.add(CursorDB().cursorToAlbumList(data))
                    }
                    onComplete {
                        adapter?.swapCursor(albumModelData)
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            adapter?.swapCursor(null)
        }
    }

}