package com.musicplayer.aow.ui.main.library.songs.view.model

import com.musicplayer.aow.delegates.data.model.Track

/**
 * Created by Arca on 11/9/2017.
 */
class SongModel(songName: String, songDuration: String, songArtist: String, songPath: String, track: Track){
    var mSongName = songName
    var mSongDuration = songDuration
    var mSongArtist = songArtist
    var mSongPath = songPath
    var mSong = track
}