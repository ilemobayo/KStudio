package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import android.content.Intent
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.ui.main.library.activities.AlbumSongs
import com.musicplayer.aow.utils.CursorDB
import org.jetbrains.anko.doAsync
import java.util.*

class AlbumFunctions{

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

    fun getAlbumTracks(context: Context, album: String, id: Boolean = true): ArrayList<Song> {
        val cursor = CursorDB().getAlbumSongs(context, album, id)
        val songs: ArrayList<Song> = ArrayList()
        if (cursor != null) {
            doAsync {
                while (cursor.moveToNext()) {
                    songs.add(CursorDB().cursorToMusic(cursor))
                }
            }
        }
        return songs
    }

    fun deleteAlbum(context: Context, album: String, id: Boolean = true){
        val albumSongsCursor = CursorDB().getAlbumSongs(context, album, id)
        while (albumSongsCursor.moveToNext()){
            CursorDB().deleteMusic(albumSongsCursor, context)
        }
        albumSongsCursor.close()
    }

}