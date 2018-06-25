package com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist

import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.PlayList

@Dao
interface PlaylistFavDAO {
    @Insert
    fun insertOnePlayList (playList: PlayList)

    @Insert
    fun insertMultiplePlayList (songList : List<PlayList>)

    @Query("SELECT * FROM playlist WHERE id = :id")
    fun fetchOnePlayListId (id: Int): PlayList

    @Query("SELECT * FROM playlist WHERE name = :name")
    fun fetchOnePlayListName (name: String): PlayList

    @Query("SELECT * FROM playlist WHERE mxp_id = :mxp_id")
    fun fetchOnePlayListMxpId (mxp_id: String): PlayList

    @Query("SELECT * FROM playlist")
    fun fetchAllPlayList (): List<PlayList>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayListFav (playList: PlayList)

    @Delete
    fun deletePlayList (playList: PlayList)
}