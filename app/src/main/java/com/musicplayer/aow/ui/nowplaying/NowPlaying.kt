package com.musicplayer.aow.ui.nowplaying

import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
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
        val playerList = Player.instance!!.mPlayList!!
        playlist.tracks = playerList.tracks
    }

    fun setSongs(tracks: ArrayList<Track>?){
        playlist.tracks = tracks
    }

    fun setPlayList(playList: PlayList){
        playlist = playList
    }

    fun setUpdate(){
        val playerList = Player.instance!!.mPlayList!!
        playlist.tracks = playerList.tracks
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