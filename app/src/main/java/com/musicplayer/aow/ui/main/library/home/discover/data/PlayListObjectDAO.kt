package com.musicplayer.aow.ui.main.library.home.discover.data

import android.arch.persistence.room.*

@Dao
interface PlayListObjectDAO {

    @Insert
    fun insertOnlySinglePlayList (playlistObject: PlaylistObject)

    @Insert
    fun insertMultiplePlayList (playlistObjectList : List<PlaylistObject>)

    @Query("SELECT * FROM discovery WHERE id = :id")
    fun fetchOnePlaylistObjectbyId (id: Int): PlaylistObject

    @Query("SELECT * FROM discovery")
    fun fetchAllPlaylistObjectbyId (): List<PlaylistObject>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayList (playlistObject: PlaylistObject)

    @Delete
    fun deletePlayList (playlistObject: PlaylistObject)
}