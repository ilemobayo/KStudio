package com.musicplayer.aow.ui.main.library.artist


import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.objects.ArtistLists
import com.musicplayer.aow.ui.base.BaseFragment
import com.musicplayer.aow.ui.main.library.artist.adapter.ArtistAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

class ArtistFragment : BaseFragment() {

    private val mSubscriptions: CompositeSubscription? = null
    private var mArtistList: RecyclerView? = null
    private var mArtistAdapter: ArtistAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mArtistList = view.find<RecyclerView>(R.id.artist_recycler_views)

//        var bundle = arguments
//        val json = bundle!!.getString("artist", null)
//        val type = object : TypeToken<ArrayList<ArtistLists>>() {}.type
//        if (json.isNullOrEmpty()) {
//            Log.e("album list", "null")
//        }else {
//            var data = Gson().fromJson<ArrayList<ArtistLists>>(json, type)
//            data(data)
//            Log.e("album list", data?.size.toString())
//        }

        loadAllSongs()
    }

    fun loadData(playList: PlayList) {
        data(playList)
    }

    fun data(artistPlaylists : ArrayList<ArtistLists>){
        mArtistAdapter = ArtistAdapter(context!!.applicationContext, activity!! , artistPlaylists!!)
        mArtistList!!.adapter = mArtistAdapter
        mArtistList!!.layoutManager = GridLayoutManager(activity, 3)
    }

    fun data(songs: PlayList){
        var artistPlaylists = ArrayList<ArtistLists>()
        songs.setSongs(songs.songs.sortedWith(compareBy({ (it.artist)!!.toLowerCase() })))
        doAsync {
            songs.songs.forEach { song ->
                if (!artistPlaylists.isEmpty()) {
                    var create_new = false
                    artistPlaylists.forEach {
                        create_new = if (it.artistName!!.toLowerCase() != song.artist!!.toLowerCase()) {
                            true
                        } else {
                            var num = it.noTracks!!
                            it.noTracks = num + 1
                            it.PlayList.addSong(song)
                            false
                        }
                    }
                    if (create_new) {
                        var newPlayList: PlayList? = PlayList()
                        newPlayList!!.setSongs(ArrayList<Song>(0))
                        newPlayList.addSong(song)
                        var newPlay = ArtistLists().apply {
                            artistName = song.artist
                            albumArt = song.albumArt
                            noTracks = 1
                            PlayList = newPlayList
                        }
                        artistPlaylists.add(newPlay)
                    }
                } else {
                    var newPlay = ArtistLists().apply {
                        artistName = song.artist
                        albumArt = song.albumArt
                        noTracks = 1
                        PlayList.addSong(song)
                    }
                    artistPlaylists.add(newPlay)
                }

            }
        }
        mArtistAdapter = ArtistAdapter(context!!.applicationContext, activity!! , artistPlaylists!!)
        mArtistList!!.adapter = mArtistAdapter
        mArtistList!!.layoutManager = GridLayoutManager(activity, 3)
    }


    private fun loadAllSongs() {
        val subscription = AppRepository().playlist(resources.getString(R.string.mp_play_list_songs))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                        loadData(result)
                    }
                })
        mSubscriptions?.add(subscription)
    }

}

