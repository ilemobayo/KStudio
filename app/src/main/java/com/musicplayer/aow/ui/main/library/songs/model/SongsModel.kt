package com.musicplayer.aow.ui.mvvm.repository

import android.provider.MediaStore
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.utils.CursorDB
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete

class SongsRepository{

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
    private var songs: PlayList = PlayList()

    fun getSongs(completion: (ArrayList<Song>, Error?) -> Unit){
        val data = context!!.contentResolver.query(MEDIA_URI,
                PROJECTIONS, WHERE, null,
                ORDER_BY)
        songs = PlayList()
        if (data != null) {
            while (data.moveToNext()) {
                songs.addSong(CursorDB().cursorToMusic(data))
            }
        }
        data.close()
        completion(songs.songs!!, null)
    }


    companion object {
        var sInstance: SongsRepository? = null
        fun instance(): SongsRepository {
            if (sInstance == null) {
                synchronized(SongsRepository) {
                    sInstance = SongsRepository()
                }
            }
            return sInstance!!
        }
    }
}