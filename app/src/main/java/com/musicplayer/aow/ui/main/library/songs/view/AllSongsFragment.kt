package com.musicplayer.aow.ui.main.library.songs.view


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.ui.main.library.songs.view.Adapter.SongListAdapter
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_all_songs.*

class AllSongsFragment : Fragment(){
    private var songs: PlayList = PlayList()
    private var adapter: SongListAdapter? = null
    private var trackDatabase: TrackDatabase? = TrackDatabase.getsInstance(Injection.provideContext()!!)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongListAdapter(songs, activity!!)

        super.onViewCreated(view, savedInstanceState)
        recycler_views.layoutManager = LinearLayoutManager(context!!.applicationContext)
        recycler_views.addItemDecoration(DividerItemDecoration(activity!!.getDrawable(R.drawable.drawble_divider), false, true))
        recycler_views.adapter = adapter
        AppExecutors.instance?.diskIO()?.execute {
            val tracks = trackDatabase?.trackDAO()?.fetchAllTrack()
            tracks?.observe(this, Observer{
                songs = PlayList(it as java.util.ArrayList<Track>)
                adapter?.swapCursor(songs.tracks)
            })
        }
    }

    var actionMode: ActionMode? = null
    companion object {
        var isMultiSelectOn = false
    }
}
