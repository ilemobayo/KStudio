package com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.Track

@Dao
interface SongFavDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneSong (track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleSongList (trackList : List<Track>)

    @Query("SELECT * FROM track WHERE id = :id")
    fun fetchOneSongId (id: Int): LiveData<Track>

    @Query("SELECT * FROM track WHERE title = :title")
    fun fetchOneSongTitle (title: String): LiveData<Track>

    @Query("SELECT * FROM track WHERE path = :path")
    fun fetchOneSongPath (path: String): LiveData<Track>

    @Query("SELECT * FROM track")
    fun fetchAllSong (): LiveData<List<Track>>

    @Query("SELECT * FROM track")
    fun fetchAllSongs (): List<Track>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSongFav (track: Track)

    @Delete
    fun deleteSong (track: Track)
}