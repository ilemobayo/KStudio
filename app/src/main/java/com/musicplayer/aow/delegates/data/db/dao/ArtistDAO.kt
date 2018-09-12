package com.musicplayer.aow.delegates.data.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.Artists

@Dao
interface ArtistDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneArtist (artist: Artists)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleArtistList (artistList : List<Artists>)

    @Query("SELECT * FROM artist WHERE id = :id")
    fun fetchOneArtistId (id: Int): LiveData<Artists>

    @Query("SELECT * FROM artist WHERE artist_id = :artist_id")
    fun fetchArtist_IdLiveData (artist_id: String): LiveData<Artists>

    @Query("SELECT * FROM artist WHERE artist_id = :artist_id")
    fun fetchArtist_Id (artist_id: String): Artists

    @Query("SELECT * FROM artist WHERE artist_name = :name")
    fun fetchOneAlbumName (name: String): LiveData<Artists>

    @Query("SELECT * FROM artist")
    fun fetchAllArtist (): LiveData<List<Artists>>

    @Query("SELECT * FROM artist")
    fun fetchAllArtists (): List<Artists>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateArtist (artist: Artists)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateArtist (artistList: List<Artists>)

    @Delete
    fun deleteArtist (artist: Artists)

}