package com.musicplayer.aow.delegates.player

import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song

/**
 * Created by Arca on 2/16/2018.
 */
class SongResolver{

    var song: Song? = null
    var playlist: PlayList? = null
    var playingIndex: Int? = 0

    constructor(song: Song?, playlist: PlayList?, playingIndex: Int?) {
        this.song = song
        this.playlist = playlist
        this.playingIndex = playingIndex
    }

    constructor(song: Song?, playingIndex: Int?) {
        this.song = song
        this.playingIndex = playingIndex
    }


}