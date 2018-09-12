package com.musicplayer.aow.delegates.data.db.model

import android.content.Context
import android.provider.MediaStore
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.utils.CursorDB

class AlbumDBModel (var context: Context) {
    private val MEDIA_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    private val WHERE = null
    private val ORDER_BY = MediaStore.Audio.Albums.ALBUM + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Albums.ALBUM, // the real path
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ALBUM_KEY,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS)
    var albumModelData:ArrayList<Album> = ArrayList()

    fun fetchAlbum(completion: (ArrayList<Album>, Error?) -> Unit){
        val data = context.contentResolver.query(MEDIA_URI,
                PROJECTIONS, WHERE, null,
                ORDER_BY)
        albumModelData = ArrayList()
        if (data != null) {
            while (data.moveToNext()) {
                albumModelData.add(CursorDB().cursorToAlbumList(data))
            }
        }
        data.close()
        completion(albumModelData, null)
    }
}