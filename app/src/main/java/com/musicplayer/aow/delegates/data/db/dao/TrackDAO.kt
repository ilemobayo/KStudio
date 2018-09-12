package com.musicplayer.aow.delegates.data.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.delegates.data.model.Track


@Dao
interface TrackDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneTrack (track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleTrackList (trackList : List<Track>)

    @Query("UPDATE track SET albumArt = :albumArt WHERE path = :path")
    fun updateAlbumArt(path: String, albumArt: String)

    @Query("SELECT * FROM track WHERE id = :id")
    fun fetchOneTrackId (id: Int): LiveData<Track>

    @Query("SELECT * FROM track WHERE title = :title")
    fun fetchOneTrackTitle (title: String): LiveData<Track>

    @Query("SELECT * FROM track WHERE path = :path")
    fun fetchOneTrackPath (path: String): LiveData<Track>

    @Query("SELECT * FROM track")
    fun fetchAllTrack (): LiveData<List<Track>>

    @Query("SELECT * FROM track")
    fun fetchAllTracks (): List<Track>

    @Query("SELECT * FROM track WHERE album = :album")
    fun fetchAllTrackAlbum (album: String): LiveData<List<Track>>

    @Query("SELECT * FROM track WHERE artist = :artist")
    fun fetchAllTrackArtist (artist: String): LiveData<List<Track>>

    @Query("SELECT * FROM track WHERE genre = :genre")
    fun fetchAllTrackGenre (genre: String): LiveData<List<Track>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTrack (track: Track)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTracks (trackList: List<Track>)

    @Delete
    fun deleteTrack (track: Track)

    @Query("DELETE FROM track WHERE album = :album")
    fun deleteTrack (album: String)
}