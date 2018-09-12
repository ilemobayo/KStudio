package com.musicplayer.aow.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.webkit.URLUtil
import com.musicplayer.aow.application.InitDatabase
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.data.source.PreferenceManager
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase


class MusicPlayerPresenter(private var mContext: Context?, private var mView: MusicPlayerContract.View?) : MusicPlayerContract.Presenter {
    private var songFavDatabase: SongFavDatabase? = SongFavDatabase.getsInstance(mContext!!)
    private var playlistDatabase = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    private var mPlayback: Player? = Player.instance

    init {
        mView!!.setPresenter(this)
    }

    override fun subscribe() {

        retrieveLastPlayMode()

        if (mPlayback != null && mPlayback!!.isPlaying) {
            mView!!.onSongUpdated(mPlayback!!.playingTrack)
        } else {
            // - load last play list/folder/track
        }
    }

    override fun unsubscribe() {
        //Release context reference
        //mContext = null
        //mView = null
    }

    override fun retrieveLastPlayMode() {
        val lastPlayMode = PreferenceManager.lastPlayMode(mContext!!)
        mView!!.updatePlayMode(lastPlayMode)
    }



    override fun setSongAsFavorite(track: Track, favorite: Boolean) {
        if(URLUtil.isHttpUrl(track.path) || URLUtil.isHttpsUrl(track.path)){
            if (favorite) {
                //remove from favorite playlist
                AppExecutors.instance?.diskIO()?.execute {
                    songFavDatabase?.songFavDAO()?.deleteSong(track)
                    mView!!.onSongSetAsFavorite(track)
                }
            } else {
                //add to favorite playlist
                AppExecutors.instance?.diskIO()?.execute {
                    songFavDatabase?.songFavDAO()?.insertOneSong(track)
                    mView!!.onSongSetAsFavorite(track)
                }
            }
        }else {
            if (favorite) {
                //remove from favorite playlist
                AppExecutors.instance?.diskIO()?.execute {
                    val resultPlaylist = playlistDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
                    if (resultPlaylist != null) {
                        resultPlaylist.addSong(track)
                        playlistDatabase?.playlistDAO()?.updatePlayList(resultPlaylist)
                        mView!!.onSongSetAsFavorite(track)
                    }
                }
            } else {
                //add to favorite playlist
                AppExecutors.instance?.diskIO()?.execute {
                    val resultPlaylist = playlistDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
                    if (resultPlaylist != null){
                        resultPlaylist.removeTrack(track)
                        playlistDatabase?.playlistDAO()?.updatePlayList(resultPlaylist)
                        mView!!.onSongSetAsFavorite(track)
                    }
                }
            }
        }
    }

}
