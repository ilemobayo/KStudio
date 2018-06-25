package com.musicplayer.aow.ui.nowplaying

import android.util.Log
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.player.Player

/**
 * Created by Arca on 2/7/2018.
 */
class NowPlaying {
    var playlist = PlayList()

    init {
        playlist.name = Injection.provideContext()!!.resources.getString(R.string.mp_play_list_nowplaying)
    }

    fun setSongs(){
        var playerList = Player.instance!!.mPlayList!!
        playlist.setSongs(
            if (playerList != null){
                playerList.songs
            }else{
                null
            }
        )
        Log.e("Now Playing", "updated songs")
    }

    fun setSongs(songs: List<Song>){
        playlist.setSongs(songs)
        Log.e("Now Playing", "updated songs 1")
    }

    fun setPlayList(playList: PlayList){
        playlist = playList
        Log.e("Now Playing", "updated playlist")
    }

    fun setUpdate(){
        var playerList = Player.instance!!.mPlayList!!
        playlist.setSongs(
            if (playerList != null){
                playerList.songs
            }else{
                null
            }
        )
        Log.e("Now Playing", "updated")
    }

    companion object {

        private val TAG = "NowPlaying"

        @Volatile private var sInstance: NowPlaying? = null

        val instance: NowPlaying?
            get() {
                if (sInstance == null) {
                    synchronized(NowPlaying.Companion) {
                        if (sInstance == null) {
                            sInstance = NowPlaying()
                        }
                    }
                }
                return sInstance
            }
    }
}