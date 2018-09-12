package com.musicplayer.aow.delegates.data.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.Album

@Dao
interface AlbumDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneAlbum (album: Album)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleAlbumList (albumList : List<Album>)

    @Query("SELECT * FROM album WHERE id = :id")
    fun fetchOneAlbumId (id: Int): LiveData<Album>

    @Query("SELECT * FROM album WHERE album_id = :album_id")
    fun fetchAlbum_IdLiveData (album_id: String): LiveData<Album>

    @Query("SELECT * FROM album WHERE album_id = :album_id")
    fun fetchAlbum_Id (album_id: String): Album

    @Query("SELECT * FROM album WHERE title = :title")
    fun fetchOneAlbumTitle (title: String): LiveData<Album>

    @Query("SELECT * FROM album WHERE albumName = :name")
    fun fetchOneAlbumName (name: String): LiveData<Album>

    @Query("SELECT * FROM album WHERE albumName = :name")
    fun fetchAlbumByName (name: String): Album

    @Query("SELECT * FROM album")
    fun fetchAllAlbum (): LiveData<List<Album>>

    @Query("SELECT * FROM album")
    fun fetchAllAlbums (): List<Album>

    @Query("SELECT * FROM album WHERE artist = :artist")
    fun fetchAllAlbumArtist (artist: String): LiveData<List<Album>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlbum (album: Album)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlbums (albumList: List<Album>)

    @Delete
    fun deleteAlbum (album: Album)
}