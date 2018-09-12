package com.musicplayer.aow.ui.main.library.artist.view


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.ArtistDatabase
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.delegates.softcode.adapters.AutoFitGridLayoutManager
import com.musicplayer.aow.ui.main.library.artist.view.adapter.ArtistAdapter
import com.musicplayer.aow.ui.main.library.artist.viewmodel.ArtistViewModel
import kotlinx.android.synthetic.main.fragment_artist.*
import kotlin.collections.ArrayList

class ArtistFragment : Fragment(){

    private var artistDatabase: ArtistDatabase? = ArtistDatabase.getsInstance(Injection.provideContext()!!)
    private var adapter: ArtistAdapter? = null
    private var artists: ArrayList<Artists>? = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ArtistAdapter(context!!.applicationContext, activity!!, artists)
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            artist_recycler_views.layoutManager = AutoFitGridLayoutManager(context!!.applicationContext, 430)
        }else {
            artist_recycler_views.layoutManager = AutoFitGridLayoutManager(context!!.applicationContext, 230)
        }
        artist_recycler_views.adapter = adapter
        AppExecutors.instance?.diskIO()?.execute {
            val albumList = artistDatabase?.artistDAO()?.fetchAllArtist()
            albumList?.observe(this, Observer{
                adapter?.swapCursor(it as java.util.ArrayList<Artists>)
            })
        }
    }

}

