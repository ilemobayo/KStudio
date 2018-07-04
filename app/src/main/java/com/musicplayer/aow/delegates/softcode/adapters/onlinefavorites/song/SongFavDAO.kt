package com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.Song

@Dao
interface SongFavDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneSong (song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleSongList (songList : List<Song>)

    @Query("SELECT * FROM song WHERE id = :id")
    fun fetchOneSongId (id: Int): LiveData<Song>

    @Query("SELECT * FROM song WHERE title = :title")
    fun fetchOneSongTitle (title: String): LiveData<Song>

    @Query("SELECT * FROM song WHERE path = :path")
    fun fetchOneSongPath (path: String): LiveData<Song>

    @Query("SELECT * FROM song")
    fun fetchAllSong (): LiveData<List<Song>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSongFav (song: Song)

    @Delete
    fun deleteSong (song: Song)
}