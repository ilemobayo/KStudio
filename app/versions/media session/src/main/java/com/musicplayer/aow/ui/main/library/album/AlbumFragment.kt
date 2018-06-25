package com.musicplayer.aow.ui.main.library.album


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
import com.musicplayer.aow.delegates.objects.AlbumLists
import com.musicplayer.aow.ui.base.BaseFragment
import com.musicplayer.aow.ui.main.library.album.adapter.AlbumAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

class AlbumFragment : BaseFragment() {

    private val mSubscriptions: CompositeSubscription? = null

    var songModelData:ArrayList<Song>? = ArrayList()
    private var mAlbumList: RecyclerView? = null
    private var mAlbumAdapter: AlbumAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAlbumList = view.find(R.id.album_recycler_views)
        mAlbumList!!.layoutManager = GridLayoutManager(context!!.applicationContext, 2)

        loadAllSongs()
    }

    fun loadData(playList: PlayList) {
        data(playList)
    }


    fun data(songs: PlayList){
        var albumPlaylists = ArrayList<AlbumLists>()

        songModelData = songs.songs as ArrayList<Song>?
        mAlbumList!!.setHasFixedSize(true)

        songs.setSongs(songs.songs.sortedWith(compareBy({ (it.album)!!.toLowerCase() })))
        doAsync {
            songs.songs.forEach { song ->
                if (!albumPlaylists.isEmpty()) {
                    var create_new = false
                    albumPlaylists.forEach {
                        create_new = if (it.albumName!!.toLowerCase() != song.album!!.toLowerCase()) {
                            true
                        } else {
                            it.PlayList.addSong(song)
                            false
                        }
                    }
                    if (create_new) {
                        var newPlayList: PlayList? = PlayList()
                        newPlayList!!.setSongs(ArrayList<Song>(0))
                        newPlayList.addSong(song)
                        var newPlay = AlbumLists().apply {
                            albumName = song.album
                            albumArtist = song.artist
                            albumArt = song.albumArt
                            PlayList = newPlayList
                        }
                        albumPlaylists.add(newPlay)
                    }
                } else {
                    var newPlay = AlbumLists().apply {
                        albumName = song.album
                        albumArtist = song.artist
                        albumArt = song.albumArt
                        PlayList.addSong(song)
                    }
                    albumPlaylists.add(newPlay)
                }

            }
        }
        mAlbumAdapter = AlbumAdapter(context!!.applicationContext, activity!!,albumPlaylists!!)
        mAlbumList!!.adapter = mAlbumAdapter
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