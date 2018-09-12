package com.musicplayer.aow.delegates.data.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.PlayList

@Dao
interface PlaylistDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOnePlayList (playList: PlayList)

    @Insert
    fun insertMultiplePlayList (songList : List<PlayList>)

    @Query("SELECT * FROM playlist WHERE id = :id")
    fun fetchOnePlayListId (id: Int): LiveData<PlayList>

    @Query("SELECT * FROM playlist WHERE name = :name")
    fun fetchOnePlayListName (name: String): LiveData<PlayList>

    @Query("SELECT * FROM playlist WHERE name = :name")
    fun fetchPlayListName (name: String): PlayList

    @Query("SELECT * FROM playlist WHERE mxp_id = :mxp_id")
    fun fetchOnePlayListMxpId (mxp_id: String): LiveData<PlayList>

    @Query("SELECT * FROM playlist WHERE mxp_id = :mxp_id")
    fun fetchPlayListMxpId (mxp_id: String): PlayList

    @Query("SELECT * FROM playlist")
    fun fetchAllPlayList (): LiveData<List<PlayList>>

    @Query("SELECT * FROM playlist")
    fun fetchAllPlayListNLD (): List<PlayList>

    @Query("SELECT * FROM playlist WHERE name != :name")
    fun fetchAllPlayListWithNoRecentlyPlayed (name: String = "Recently Played"): LiveData<List<PlayList>>

    @Query("SELECT * FROM playlist WHERE name != :name")
    fun fetchAllPlayListWithNoRecentlyPlayedNLD (name: String = "Recently Played"): List<PlayList>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayList (playList: PlayList)

    @Delete
    fun deletePlayList (playList: PlayList)
}