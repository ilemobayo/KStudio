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
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
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

        upDate()
        loadData()
    }

    fun loadData(){
        val favoriteSongsList = songFavDatabase?.songFavDAO()?.fetchAllSong()
        favoriteSongsList?.observe(this, object: Observer<List<Song>>{
            override fun onChanged(t: List<Song>?) {
                upDate()
            }
        })

        val favoritePlaylist = playListFavDatabase?.playlistFavDAO()?.fetchAllPlayListWithNoRecentlyPlayed()
        favoritePlaylist?.observe(this, object: Observer<List<PlayList>> {
            override fun onChanged(t: List<PlayList>?) {
                upDate()
            }
        })

    }

    fun upDate(){
        reloadFavoriteSongs()
    }


    private fun reloadFavoriteSongs(){
        modelData = ArrayList()
        val favoriteSongsList = songFavDatabase?.songFavDAO()?.fetchAllSong()
        favoriteSongsList?.observe(this, object: Observer<List<Song>>{

            override fun onChanged(t: List<Song>?) {
                favoriteSongsList.removeObserver(this)
                val favoriteSongs = PlayList()
                favoriteSongs.name = "Favorite Songs"
                favoriteSongs.songs = t as ArrayList
                modelData.add(favoriteSongs)
                reloadPlaylists()
            }
        })
    }

    fun reloadPlaylists(){
        val favoritePlaylist = playListFavDatabase?.playlistFavDAO()?.fetchAllPlayListWithNoRecentlyPlayed()
        favoritePlaylist?.observe(this, object: Observer<List<PlayList>> {
            override fun onChanged(t: List<PlayList>?) {
                favoritePlaylist.removeObserver(this)
                modelData.addAll(t as ArrayList)
                adapter?.swapCursor(modelData)
            }
        })
    }


}
