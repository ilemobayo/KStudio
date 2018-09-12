package com.musicplayer.aow.ui.main.library.artist.model

import android.provider.MediaStore
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.utils.CursorDB

class ArtistModel{
    private val context = Injection.provideContext()
    private val MEDIA_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    private val WHERE = null
    private val ORDER_BY = MediaStore.Audio.Artists.ARTIST + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
    var artistModelData:ArrayList<Artists> = ArrayList()

    fun getArtist(completion: (ArrayList<Artists>, Error?) -> Unit){
        val data = context!!.contentResolver.query(MEDIA_URI,
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


    companion object {
        var sInstance: ArtistModel? = null
        fun instance(): ArtistModel {
            if (sInstance == null) {
                synchronized(ArtistModel) {
                    sInstance = ArtistModel()
                }
            }
            return sInstance!!
        }
    }

}