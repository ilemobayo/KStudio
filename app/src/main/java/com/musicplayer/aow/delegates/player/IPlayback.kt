package com.musicplayer.aow.delegates.player

import com.google.android.exoplayer2.SimpleExoPlayer
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track


interface IPlayback {

    val mPlayer: SimpleExoPlayer?

    val isPlaying: Boolean

    val progress: Int

    val playingTrack: Track?

    var playingList: PlayList?

    fun setPlayList(list: PlayList)

    fun play(): Boolean

    fun play(list: PlayList): Boolean

    fun play(list: PlayList, startIndex: Int): Boolean

    fun play(track: Track): Boolean

    fun playLast(): Boolean

    fun playNext(): Boolean

    fun pause(): Boolean

    fun seekTo(progress: Int): Boolean

    fun setPlayMode(playMode: PlayMode)

    fun registerCallback(callback: Callback)

    fun unregisterCallback(callback: Callback)

    fun removeCallbacks()

    fun releasePlayer()

    interface Callback {

        fun onSwitchLast(last: Track?)

        fun onSwitchNext(next: Track?)

        fun onComplete(next: Track?)

        fun onPlayStatusChanged(isPlaying: Boolean)

        fun onTriggerLoading(isLoading: Boolean)

        fun onPrepared(isPrepared: Boolean)
    }
}
