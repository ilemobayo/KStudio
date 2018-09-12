package com.musicplayer.aow.delegates.player

import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track

interface PlayerCallback: IPlayback {

    override val isPlaying: Boolean

    override val progress: Int

    override val playingTrack: Track?

    override var playingList: PlayList?

    override fun setPlayList(list: PlayList)

    override fun play(): Boolean

    override fun play(list: PlayList): Boolean

    override fun play(list: PlayList, startIndex: Int): Boolean

    override fun play(track: Track): Boolean

    override fun playLast(): Boolean

    override fun playNext(): Boolean

    override fun pause(): Boolean

    override fun seekTo(progress: Int): Boolean

    override fun setPlayMode(playMode: PlayMode)

}