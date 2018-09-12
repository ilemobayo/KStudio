package com.musicplayer.aow.ui.main.library.songs.model

import android.provider.MediaStore
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.utils.CursorDB

class SongsModel{
    private val context = Injection.provideContext()
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
    var tracks: ArrayList<Track> = ArrayList()

    fun getSongs(completion: (ArrayList<Track>, Error?) -> Unit){
        val data = context!!.contentResolver.query(MEDIA_URI,
                PROJECTIONS, WHERE, null,
                ORDER_BY)
        tracks = ArrayList()
        if (data != null) {
            while (data.moveToNext()) {
                tracks.add(CursorDB().cursorToMusic(data))
            }
        }
        data.close()
        completion(tracks, null)
    }


    companion object {
        var sInstance: SongsModel? = null
        fun instance(): SongsModel {
            if (sInstance == null) {
                synchronized(SongsModel) {
                    sInstance = SongsModel()
                }
            }
            return sInstance!!
        }
    }
}