package com.musicplayer.aow.ui.main.library.activities.albumsonglist

import com.musicplayer.aow.delegates.data.model.Track

/**
 * Created by Arca on 12/1/2017.
 */
class AlbumSongsModel(songName: String, songDuration: String, songArtist: String, songPath: String, track: Track){
    var mSongName = songName
    var mSongDuration = songDuration
    var mSongArtist = songArtist
    var mSongPath = songPath
    var mSong = track
}