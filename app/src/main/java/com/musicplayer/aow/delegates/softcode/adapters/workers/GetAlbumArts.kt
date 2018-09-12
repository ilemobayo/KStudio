package com.musicplayer.aow.delegates.softcode.adapters.workers

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.AlbumDatabase
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.db.model.AlbumDBModel
import com.musicplayer.aow.delegates.data.db.model.TracksDBModel
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.utils.images.BitmapDraws
import org.jetbrains.anko.onComplete
import java.io.File
import java.io.IOException

class GetAlbumArts(context: Context) {

    private var trackDatabase = TrackDatabase.getsInstance(context)
    private var albumDatabase = AlbumDatabase.getsInstance(context)

    fun runIn() {
        AppExecutors.instance?.diskIO()?.execute {
            trackDatabase?.trackDAO()?.fetchAllTracks()?.forEach {
                updateTrackAlbumArt(it, albumDatabase?.albumDAO()?.fetchAlbumByName(it.album!!)!!)
            }
        }
    }

    private fun updateTrackAlbumArt(track: Track, album: Album){
        Log.e(this.javaClass.name, "${track.title}  : ${album.albumName}  -  ${album.albumArt}")
        track.albumArt = album.albumArt
        trackDatabase?.trackDAO()?.updateTrack(track)
    }
}