package com.musicplayer.aow.ui.main.library.playlist.online


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist.PlayListFavDatabase
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import org.jetbrains.anko.find

class OnlinePlaylistFragment : Fragment() {

    internal var recycler_view: RecyclerView? = null
    var adapter: OnlinePlaylistAdapter? = null
    var modelData:ArrayList<PlayList> = ArrayList()

    private var playListFavDatabase = PlayListFavDatabase.getsInstance(Injection.provideContext()!!)
    private var songFavDatabase = SongFavDatabase.getsInstance(Injection.provideContext()!!)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_online_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OnlinePlaylistAdapter(context!!.applicationContext, modelData, this.layoutInflater)

        recycler_view = view.find(R.id.recyclerview)
        recycler_view!!.visibility = View.VISIBLE
        //Setup layout manager
        val layoutManager = PreCachingLayoutManager(activity!!)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(activity!!))
        recycler_view!!.addItemDecoration(
                DividerItemDecoration(
                        activity!!.getDrawable(
                                R.drawable.drawble_divider
                        ),
                        false,
                        true)
        )
        recycler_view!!.setHasFixedSize(true)
        recycler_view!!.layoutManager = layoutManager
        recycler_view!!.adapter = adapter

        loadData()
    }

    fun loadData(){
        AppExecutors.instance?.diskIO()?.execute {
            val favoriteSongsList = songFavDatabase?.songFavDAO()?.fetchAllSong()
            favoriteSongsList?.observe(this, Observer<List<Track>> { reloadFavoriteSongs() })

            val favoritePlaylist = playListFavDatabase?.playlistFavDAO()?.fetchAllPlayListWithNoRecentlyPlayed()
            favoritePlaylist?.observe(this, Observer<List<PlayList>> { reloadFavoriteSongs() })
        }
    }


    private fun reloadFavoriteSongs(){
        modelData = ArrayList()
        AppExecutors.instance?.diskIO()?.execute {
            val favoriteSongsList = songFavDatabase?.songFavDAO()?.fetchAllSongs()
            val favoriteSongs = PlayList()
            favoriteSongs.name = "Favorite Songs"
            favoriteSongs.tracks = favoriteSongsList as ArrayList
            modelData.clear()
            modelData.add(favoriteSongs)

            val favoritePlaylist = playListFavDatabase?.playlistFavDAO()?.fetchAllPlayListWithNoRecentlyPlayedList()
            modelData.addAll(favoritePlaylist as ArrayList)

            this.activity?.runOnUiThread {
                adapter?.swapCursor(modelData)
            }
        }
    }

}
