package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.ArtistDatabase
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.utils.CursorDB
import org.jetbrains.anko.doAsync

class ArtistFunctions {

    private var artistDatabase: ArtistDatabase? = ArtistDatabase.getsInstance(Injection.provideContext()!!)
    private var trackDatabase: TrackDatabase? = TrackDatabase.getsInstance(Injection.provideContext()!!)

    fun getArtistTracks(context: Context, artist: String, id: Boolean = true): ArrayList<Track>{
        val cursor = CursorDB().getArtistSongs(context, artist, id)
        val tracks: ArrayList<Track> = ArrayList()
        doAsync {
            while (cursor.moveToNext()) {
                tracks.add(CursorDB().cursorToMusic(cursor))
            }
        }
        return tracks
    }

    fun deleteArtist(context: Context, artist: String, id: Boolean = true){
        AppExecutors.instance?.diskIO()?.execute {
            val artistTodelete = artistDatabase?.artistDAO()?.fetchArtist_Id(artist)
            if(artistTodelete != null) {
                artistDatabase?.artistDAO()?.deleteArtist((artistTodelete))
                val artistsongs = trackDatabase?.trackDAO()?.fetchAllTrackArtist(artistTodelete.artist_name ?: "")
                if (artistsongs != null) {
                    artistsongs.value?.forEach {
                        trackDatabase?.trackDAO()?.deleteTrack(it)
                    }
                    val artistSongsCursor = CursorDB().getArtistSongs(context, artist, id)
                    while (artistSongsCursor.moveToNext()){
                        CursorDB().deleteMusic(artistSongsCursor, context)
                    }
                    artistSongsCursor.close()
                }
            }
        }
    }

}