package com.musicplayer.aow.ui.main.library.album.view


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.database.Cursor
import android.os.Build
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
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.AlbumDatabase
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.softcode.adapters.AutoFitGridLayoutManager
import com.musicplayer.aow.ui.main.library.album.view.adapter.AlbumAdapter
import com.musicplayer.aow.ui.main.library.album.viewmodel.AlbumViewModel
import com.musicplayer.aow.utils.CursorDB
import kotlinx.android.synthetic.main.fragment_album.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete

class AlbumFragment : Fragment(){

    private var albumDatabase: AlbumDatabase? = AlbumDatabase.getsInstance(Injection.provideContext()!!)
    private var adapter: AlbumAdapter? = null
    private var albums: ArrayList<Album>? = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AlbumAdapter(context!!.applicationContext, activity!!, albums)
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            album_recycler_views!!.layoutManager = AutoFitGridLayoutManager(context!!.applicationContext, 430)
        }else {
            album_recycler_views!!.layoutManager = AutoFitGridLayoutManager(context!!.applicationContext, 230)
        }
        album_recycler_views!!.adapter = adapter
        AppExecutors.instance?.diskIO()?.execute {
            val albumList = albumDatabase?.albumDAO()?.fetchAllAlbum()
            albumList?.observe(this, Observer{
                adapter?.swapCursor(it as java.util.ArrayList<Album>)
            })
        }
    }

}