package com.musicplayer.aow.application

import android.content.Context
import com.birbit.android.jobqueue.JobManager
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.AlbumDatabase
import com.musicplayer.aow.delegates.data.db.database.ArtistDatabase
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.db.model.AlbumDBModel
import com.musicplayer.aow.delegates.data.db.model.ArtistDBModel
import com.musicplayer.aow.delegates.data.db.model.PlaylistDBModel
import com.musicplayer.aow.delegates.data.db.model.TracksDBModel
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.scheduler.jobs.AlbumArtJob
import java.io.File
import java.io.IOException

class InitDatabase(var context: Context) {

    private var tracksDatabaseModel = TracksDBModel(context)
    private var trackDatabase = TrackDatabase.getsInstance(context)
    private var albumDatabaseModel = AlbumDBModel(context)
    private var albumDatabase = AlbumDatabase.getsInstance(context)
    private var artistDatabaseModel = ArtistDBModel(context)
    private var artistDatabase = ArtistDatabase.getsInstance(context)
    private var playlistDatabaseModel = PlaylistDBModel(context)

    //Job queue
    private var jobManager: JobManager? = null

    init {
        fetchAndUpdateTracks()
        fetchAndUpdateAlbum()
        fetchAndUpdateArtist()
        fetchAndUpdatePlaylist()
    }


    fun loadAllDatabase(runJobManager: Boolean = true){
        fetchAndUpdateTracks()
        fetchAndUpdateAlbum()
        fetchAndUpdateArtist()
        fetchAndUpdatePlaylist()
        invalidateUnAvailableTracks()
        if (runJobManager) {
            jobManager = MusicPlayerApplication.instance!!.getJobManager()
            jobManager?.addJobInBackground(AlbumArtJob("run"))
        }
    }

    private fun invalidateUnAvailableTracks(){
        tracksDatabaseModel.fetchTracks{ model, _ ->
            model.forEach {
                try{
                    if (!File(it.path).exists()){
                        AppExecutors.instance?.diskIO()?.execute {
                            trackDatabase?.trackDAO()?.deleteTrack(it)
                        }
                    }
                }catch (e: IOException){

                }
            }
        }
    }

    //Track Table
    private fun fetchAndUpdateTracks(){
        tracksDatabaseModel.fetchTracks{ model, _ ->
            updateTracks(model)
        }
    }

    private fun updateTracks(trackList: List<Track>){
        AppExecutors.instance?.diskIO()?.execute {
            trackDatabase?.trackDAO()?.insertMultipleTrackList(trackList)
        }
    }

    //Album Table
    private fun fetchAndUpdateAlbum(){
        albumDatabaseModel.fetchAlbum{ model, _ ->
            updateAlbum(model)
        }
    }

    private fun updateAlbum(trackList: List<Album>){
        AppExecutors.instance?.diskIO()?.execute {
            albumDatabase?.albumDAO()?.insertMultipleAlbumList(trackList)
        }
    }

    //Artist Table
    private fun fetchAndUpdateArtist(){
        artistDatabaseModel.fetchArtist{ model, _ ->
            updateArtist(model)
        }
    }

    private fun updateArtist(trackList: List<Artists>){
        AppExecutors.instance?.diskIO()?.execute {
            artistDatabase?.artistDAO()?.insertMultipleArtistList(trackList)
        }
    }

    //Playlist update
    private fun fetchAndUpdatePlaylist(){
        playlistDatabaseModel.initPlaylist()
    }

}