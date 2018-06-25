package com.musicplayer.aow.delegates.data.model

import android.provider.MediaStore
import android.support.v4.util.ArrayMap
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.utils.CursorDB
import java.util.*

/**
 * Created by Arca on 12/31/2017.
 */
class TempSongs {

    var songs: ArrayList<Song>? = null

    fun setSongs(){
        var songModelData: ArrayList<Song> = ArrayList()
        var songCursor = CursorDB.instance!!.callCursor(Injection.provideContext()!!)
        val mMap = ArrayMap<Any,String>()
        var albumCursor = CursorDB.instance!!.albumaCursor(Injection.provideContext()!!)
        while (albumCursor != null && albumCursor.moveToNext()) {
            mMap[albumCursor.getString(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))] =
                    albumCursor.getString(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
        }
        if (songCursor != null) {
            var indexPosition = 0
            while (songCursor.moveToNext()) {
                indexPosition += 1
                songModelData.add(CursorDB.instance!!.cursorToMusic(songCursor, mMap, indexPosition))
            }
            songCursor.close()
        }
        this.songs = songModelData
    }

    companion object {

        @Volatile private var sInstance: TempSongs? = null

        val instance: TempSongs?
            get() {
                if (sInstance == null) {
                    synchronized(TempSongs::class.java) {
                        if (sInstance == null) {
                            sInstance = TempSongs()
                        }
                    }
                }
                return sInstance
            }
    }
}