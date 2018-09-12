package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.AlbumDatabase
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.ui.main.library.activities.AlbumSongs
import com.musicplayer.aow.utils.CursorDB
import org.jetbrains.anko.doAsync
import java.util.*

class AlbumFunctions{

    private var albumDatabase: AlbumDatabase? = AlbumDatabase.getsInstance(Injection.provideContext()!!)
    private var trackDatabase: TrackDatabase? = TrackDatabase.getsInstance(Injection.provideContext()!!)

    fun openAlbumActivity(context: Context, album: Album){
        val intent = Intent(context, AlbumSongs::class.java).apply {
            putExtra("com.musicplayer.aow.album.id", album.album_id)
            putExtra("com.musicplayer.aow.album.name", album.albumName)
            putExtra("com.musicplayer.aow.album.artist", album.artist)
            putExtra("com.musicplayer.aow.album.album_art", album.albumArt)
            putExtra("com.musicplayer.aow.album.numberOfSongs", album.numberOfSongs)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent, null)
    }

    fun getAlbum(context: Context, album_id: String): Album {
        return CursorDB().getAlbum(context, album_id)
    }

    fun getAlbumTracks(context: Context, album: String, id: Boolean = true): ArrayList<Track> {
        val cursor = CursorDB().getAlbumSongs(context, album, id)
        val tracks: ArrayList<Track> = ArrayList()
        doAsync {
            while (cursor.moveToNext()) {
                tracks.add(CursorDB().cursorToMusic(cursor))
            }
        }
        return tracks
    }

    fun deleteAlbum(context: Context, album: String, id: Boolean = true){
        AppExecutors.instance?.diskIO()?.execute {
            val albumTodelete = albumDatabase?.albumDAO()?.fetchAlbum_Id(album)
            if(albumTodelete != null) {
                albumDatabase?.albumDAO()?.deleteAlbum((albumTodelete))
                val albumsongs = trackDatabase?.trackDAO()?.fetchAllTrackAlbum(albumTodelete.albumName ?: "")
                if (albumsongs != null) {
                    albumsongs.value?.forEach {
                        trackDatabase?.trackDAO()?.deleteTrack(it)
                    }
                    val albumSongsCursor = CursorDB().getAlbumSongs(context, album, id)
                    while (albumSongsCursor.moveToNext()) {
                        CursorDB().deleteMusic(albumSongsCursor, context)
                    }
                    albumSongsCursor.close()
                }
            }
        }
    }

}