package com.musicplayer.aow.delegates.data.db.model

import android.content.Context
import android.provider.MediaStore
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.utils.CursorDB

class ArtistDBModel(var context: Context) {
    private val MEDIA_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    private val WHERE = null
    private val ORDER_BY = MediaStore.Audio.Artists.ARTIST + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
    private var artistModelData:ArrayList<Artists> = ArrayList()

    fun fetchArtist(completion: (ArrayList<Artists>, Error?) -> Unit){
        val data = context.contentResolver.query(MEDIA_URI,
                PROJECTIONS, WHERE, null,
                ORDER_BY)
        artistModelData = ArrayList()
        if (data != null) {
            while (data.moveToNext()) {
                artistModelData.add(CursorDB().cursorToArtistsList(data))
            }
        }
        data.close()
        completion(artistModelData, null)
    }
}