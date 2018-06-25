package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.utils.CursorDB
import org.jetbrains.anko.doAsync

class ArtistFunctions {


    fun getArtistTracks(context: Context, artist: String, id: Boolean = true): ArrayList<Song>{
            val cursor = CursorDB().getArtistSongs(context, artist, id)
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

    fun deleteArtist(context: Context, artist: String, id: Boolean = true){
        val artistSongsCursor = CursorDB().getArtistSongs(context, artist, id)
        while (artistSongsCursor.moveToNext()){
            CursorDB().deleteMusic(artistSongsCursor, context)
        }
        artistSongsCursor.close()
    }

}