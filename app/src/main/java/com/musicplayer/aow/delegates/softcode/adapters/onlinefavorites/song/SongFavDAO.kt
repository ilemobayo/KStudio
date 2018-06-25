package com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song

import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.Song

@Dao
interface SongFavDAO {
    @Insert
    fun insertOneSong (song: Song)

    @Insert
    fun insertMultipleSongList (songList : List<Song>)

    @Query("SELECT * FROM song WHERE id = :id")
    fun fetchOneSongId (id: Int): Song

    @Query("SELECT * FROM song WHERE title = :title")
    fun fetchOneSongTitle (title: String): Song

    @Query("SELECT * FROM song WHERE path = :path")
    fun fetchOneSongPath (path: String): Song

    @Query("SELECT * FROM song")
    fun fetchAllSong (): List<Song>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSongFav (song: Song)

    @Delete
    fun deleteSong (song: Song)
}