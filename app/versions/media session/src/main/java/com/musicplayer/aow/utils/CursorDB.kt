package com.musicplayer.aow.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.support.v4.util.ArrayMap
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Song


/**
 * Created by Arca on 11/9/2017.
 */
class CursorDB {

    var albumArt: Cursor? = null
    private var mContext:Context? = null

    private val MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val WHERE = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
            + MediaStore.Audio.Media.SIZE + ">0" )
    private val ORDER_BY = MediaStore.Audio.Media.TITLE + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Media.DATA, // the real path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE)

    fun callCursor(context: Context): Cursor? {
        mContext = context
        albumArt = Injection.provideContext()!!.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                null,null, null)
        return context.contentResolver.query(
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY)
    }

    fun albumaCursor(context: Context): Cursor? {
        return context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                null,null, null)
    }

    fun cursorToMusic(cursor: Cursor, mMap: ArrayMap<Any,String>, indexPosition: Int): Song {
        val realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
//        val songFile = File(realPath)
        var song: Song?
        song = Song()
        song.id = indexPosition
        song.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
        var displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        if (displayName.endsWith(".mp3")) {
            displayName = displayName.substring(0, displayName.length - 4)
        }
        song.displayName = displayName
        if (song.displayName == null){
            song.displayName = "Unknown"
        }
        song.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        if (song.artist == null){
            song.artist = "Unknown"
        }
        song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        if (song.album == null){
            song.album = "Unknown"
        }


        if (mMap.containsKey(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)))){
            song.albumArt = mMap[cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))]
        }else{
            song.albumArt = ""
        }

        song.path = realPath
        song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        song.size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

        return song
    }


    companion object {
        @Volatile private var sInstance: CursorDB? = null
        val instance: CursorDB?
            get() {
                if (sInstance == null) {
                    synchronized(Settings.Companion) {
                        if (sInstance == null) {
                            sInstance = CursorDB()
                        }
                    }
                }
                return sInstance
            }
    }

}