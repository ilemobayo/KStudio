package com.musicplayer.aow.delegates.player

import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track

/**
 * Created by Arca on 2/16/2018.
 */
class SongResolver{

    var track: Track? = null
    var playlist: PlayList? = null
    var playingIndex: Int? = 0

    constructor(track: Track?, playlist: PlayList?, playingIndex: Int?) {
        this.track = track
        this.playlist = playlist
        this.playingIndex = playingIndex
    }

    constructor(track: Track?, playingIndex: Int?) {
        this.track = track
        this.playingIndex = playingIndex
    }


}